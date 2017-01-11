package polya.crp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import polya.crp.CRPFactor;
import polya.crp.CRPState;
import polya.crp.PYPrior;
import polya.crp.utils.ClusterId;
import polya.parametric.SufficientStatistic;
import polya.parametric.normal.CollapsedNormalNormalModel;
import polya.parametric.normal.NIWs;
import polya.parametric.normal.NormalParameter;
import polya.parametric.normal.TwoMomentsSufficientStatistics;
import bayonet.distributions.Multinomial;
import bayonet.distributions.Normal;
import blang.MCMCAlgorithm;
import blang.MCMCFactory;
import blang.annotations.DefineFactor;
import blang.processing.Processor;
import blang.processing.ProcessorContext;
import briefj.opt.OptionSet;
import briefj.run.Mains;

/**
 * Building a DPMM for Normal-Normal conjugate pair where only the mean is considered unknown
 * 
 * @author Seong-Hwan Jun (s2jun.uw@gmail.com)
 *
 */
public class NormalNormalDPMMTest implements Runnable, Processor
{
	public final File dataFile = new File("data/normal_normal_data.csv");
	public final NormalParameter hp = new NormalParameter(0.0, 1.0);
	public final CRPState crpState = CRPState.fullyDisconnectedClustering(NIWs.loadFromCSVFile(dataFile));

	public class Model
	{
		@DefineFactor
		public final CRPFactor crpFactor = new CRPFactor(new CollapsedNormalNormalModel(1.0, hp), crpState);
	
		@DefineFactor
		public final PYPrior prior = new PYPrior(1.0, 0.0, crpState);
	}

	@OptionSet(name = "factory")
	public final MCMCFactory factory = new MCMCFactory();

	public Model model;

	@Override
	public void run() 
	{
		factory.addProcessor(this);
		factory.mcmcOptions.nMCMCSweeps = 100000;
		factory.mcmcOptions.burnIn = 10000;
		factory.mcmcOptions.thinningPeriod = 50;
		factory.mcmcOptions.progressCODA  = true;
		model = new Model();
	    MCMCAlgorithm mcmc = factory.build(model, false);
	    System.out.println(mcmc.model);
	    mcmc.run();
	}

	@Override
	public void process(ProcessorContext context) 
	{
		if (context.getMcmcIteration() < (factory.mcmcOptions.nMCMCSweeps-1))
			return;
		
		for (ClusterId clusterId : crpState.getAllClusterIds())
		{
			TwoMomentsSufficientStatistics suffStat = (TwoMomentsSufficientStatistics)crpState.getClusterStatistics(clusterId);
			double clusterMu = suffStat.sumOfValues().get(0, 0)/suffStat.numberOfPoints();
			System.out.println(clusterMu);
		}
	}

	public static void main(String [] args)
	{
		Mains.instrumentedRun(args, new NormalNormalDPMMTest());
	}

	/**
	 * Generate the data given the variance and the hyper parameters
	 * 1. Generate clustering based on CRP for i=1,...,numDataPoints
	 * 2. Generate mu_{z_i} ~ N(mu0, var0) for {z_i}, i = 1 , ..., numDataPoints
	 * 3. Generate x_{z_i} ~ N(mu_{z_i}, var)
	 * 
	 * @param rand
	 * @param numDataPoints
	 * @param var
	 * @param alpha0
	 * @param mu0
	 * @param var0
	 * @return
	 */
	public static Map<Integer, SufficientStatistic> generateData(Random rand, int numDataPoints, double var, double alpha0, double mu0, double var0)
	{
	    // generate the data:
	    // 1. choose a table z_i ~ CRP(alpha0)
	    // 2 a. For each z_i, mu_{z_i} ~ N(mu0, var0) 
	    // 2 b. draw x_i | z_i ~ N(mu_{z_i}, var)

	    // Step 1.
	    List<Integer> tables = new ArrayList<Integer>();
	    int nTables = 0;
	    for (int i = 0; i < numDataPoints; i++)
	    {
		    nTables = tables.size();
	    	double [] assignmentProbs = new double[nTables + 1];
	    	for (int j = 0; j < nTables; j++)
	    	{
	    		assignmentProbs[j] = tables.get(j)/(i + alpha0); 
	    	}
	    	assignmentProbs[nTables] = alpha0/(i + alpha0);

	    	int tableIndex = Multinomial.sampleMultinomial(rand, assignmentProbs);
	    	if (tableIndex == nTables)
	    	{
	    		tables.add(1);
	    	} 
	    	else 
	    	{
	    		tables.set(tableIndex, tables.get(tableIndex) + 1);
	    	}
	    }
	    System.out.println("nTables generated=" + tables.size());

	    // Step 2.
	    Map<Integer, SufficientStatistic> data = new HashMap<Integer, SufficientStatistic>();
	    int id = 0;
	    double [] trueMean = new double[nTables];
		for (int i = 0; i < nTables; i++)
	    {
			int numCustomers = tables.get(i);

			// draw mu ~ N(mu0, sigma0^2)
	    	double mu = Normal.generate(rand, mu0, var0);
	    	trueMean[i] = mu;
	    	System.out.println("mu[" + i + "]: " + mu + ", numCustomers: " + numCustomers);

	    	for (int n = 0; n < numCustomers; n++)
	    	{
	    		// generate the data point from N(mu, var)
	    		double x = Normal.generate(rand, mu, var);
	    		data.put(id++, TwoMomentsSufficientStatistics.fromOnePoint(new double[]{x}));
	    	}
	    }

	    return data;
	}

}
