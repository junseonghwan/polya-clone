package polya.crp;

import java.io.File;

import polya.parametric.normal.CollapsedNIWModel;
import polya.parametric.normal.NIWHyperParameter;
import polya.parametric.normal.NIWs;
import blang.MCMCAlgorithm;
import blang.MCMCFactory;
import blang.annotations.DefineFactor;
import blang.processing.Processor;
import blang.processing.ProcessorContext;
import briefj.opt.OptionSet;
import briefj.run.Mains;

public class NormalInverseWishartDPMMTest implements Runnable, Processor 
{

	public final File dataFile = new File("data/circle.csv");
	public final NIWHyperParameter hp = NIWHyperParameter.withDimensionality(2);
	public final CRPState crpState = CRPState.fullyDisconnectedClustering(NIWs.loadFromCSVFile(dataFile));

	public class Model
	{
		@DefineFactor
		public final CRPFactor crpFactor = new CRPFactor(new CollapsedNIWModel(hp), crpState);

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
		factory.mcmcOptions.nMCMCSweeps = 10000;
		factory.mcmcOptions.burnIn = 5000;
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
		System.out.println("nTables=" + crpState.nTables());
	}
	
	public static void main(String [] args)
	{
		Mains.instrumentedRun(args, new NormalInverseWishartDPMMTest());
	}

}
