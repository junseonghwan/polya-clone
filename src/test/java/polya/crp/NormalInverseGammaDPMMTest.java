package polya.crp;

import java.io.File;

import polya.parametric.normal.CollapsedNIGModel;
import polya.parametric.normal.NIGHyperParameter;
import polya.parametric.normal.NIWs;
import blang.MCMCAlgorithm;
import blang.MCMCFactory;
import blang.annotations.DefineFactor;
import blang.processing.Processor;
import blang.processing.ProcessorContext;
import briefj.opt.OptionSet;
import briefj.run.Mains;

public class NormalInverseGammaDPMMTest implements Runnable, Processor
{
	public final File dataFile = new File("data/normal_normal_data.csv");
	public final NIGHyperParameter hp =new NIGHyperParameter(0, 1.0, 1.0, 1.0);
	public final CRPState crpState = CRPState.fullyDisconnectedClustering(NIWs.loadFromCSVFile(dataFile));

	public class Model
	{
		@DefineFactor
		public final CRPFactor crpFactor = new CRPFactor(new CollapsedNIGModel(hp), crpState);
	
		@DefineFactor
		public final PYPrior prior = new PYPrior(1.0, 0.0, crpState);
	}

	@OptionSet(name = "factory")
	public final MCMCFactory factory = new MCMCFactory();

	public Model model;

	@Override
	public void process(ProcessorContext context) 
	{
		// TODO Auto-generated method stub
		
	}

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

	public static void main(String [] args)
	{
		Mains.instrumentedRun(args, new NormalInverseGammaDPMMTest());
	}
}
