package polya.crp;

import blang.processing.NodeProcessor;
import blang.processing.ProcessorContext;

/**
 * Processor for CRPState
 * @author Seong-Hwan Jun (s2jun.uw@gmail.com)
 *
 */
public class CRPProcessor implements NodeProcessor<CRPState>
{
	private CRPState variable;

	@Override
	public void process(ProcessorContext context) 
	{
		System.out.println("MCMC iter:" + context.getMcmcIteration());
		System.out.println("nTables=" + variable.getAllClusterIds().size());
	}

	@Override
	public void setReference(CRPState variable) 
	{
		this.variable = variable;
	}

}
