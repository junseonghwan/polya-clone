package polya.crp;

import java.util.List;
import java.util.Random;
import java.util.Set;

import bayonet.distributions.Multinomial;
import blang.factors.Factor;
import blang.mcmc.ConnectedFactor;
import blang.mcmc.MHProposalDistribution;
import blang.mcmc.SampledVariable;
import polya.crp.utils.ClusterId;
import polya.parametric.CollapsedConjugateModel;
import polya.parametric.HyperParameter;
import polya.parametric.Parametrics;
import polya.parametric.SufficientStatistic;


/**
 * Samplers for CRPs.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class CRPSampler implements MHProposalDistribution
{
	@SampledVariable
	CRPState crpState;
	@ConnectedFactor
	List<Factor> connectedFactors; 
	// Note: need CollapsedConjugateModel and PYPrior but CRPFactor and PYPrior are the connected factors
	// this means that CRPFactor needs to provide a way to get the CollapsedModel (the likelihood model)

	@Override
	public Proposal propose(Random rand) 
	{
		PYPrior pyPrior = null;
		CollapsedConjugateModel collapsedModel = null;
		HyperParameter hp = null;
		
		for (Factor f : connectedFactors)
		{
			if (f instanceof PYPrior)
			{
				pyPrior = (PYPrior)f;
			}
			if (f instanceof CRPFactor)
			{
				CRPFactor crpFactor = (CRPFactor)f;
				collapsedModel = crpFactor.getCollapsedConjugateModel();
				hp = collapsedModel.getHyperParameter();
			}
		}
		
		if (pyPrior == null)
			throw new RuntimeException("PYPrior is not connected to the CRPState");
		if (collapsedModel == null)
			throw new RuntimeException("CRPFactor is not connected to the CRPState");
		
		// randomly select a customer to re-seat
		Set<Integer> customers = crpState.getAllCustomers();
		Integer customer= rand.nextInt(customers.size());
		
		gibbs(rand, customer, crpState, hp, collapsedModel, pyPrior);
		
		// return the new Proposal object with acceptance prob of log(1) = 0
		Proposal proposal = new Proposal() {
			
			@Override
			public double logProposalRatio() {
				return 0;
			}

			@Override
			public void acceptReject(boolean accept) {
			}
		};
		return proposal;
	}

  public static void gibbs(
      Random rand, 
      Integer customer,
      CRPState state, 
      HyperParameter hp, 
      CollapsedConjugateModel collapsedModel,
      PYPrior prior)
  {
    /* startRem throw new RuntimeException(); */ 
    state.removeCustomer(customer);
    // consider all the way to re-insert them
    int nOutcomes = state.nTables() + 1;
    List<ClusterId> existingTables = state.getAllClusterIds();
    double [] logUnnormalizedPrs = new double[nOutcomes];
    SufficientStatistic currentCustomer = state.getCustomerStatistic(customer);
    for (int i = 0; i < state.nTables(); i++)
    {
      ClusterId current = existingTables.get(i);
      SufficientStatistic customerAlreadyAtTable = state.getClusterStatistics(current);
      logUnnormalizedPrs[i] = 
        Parametrics.logPredictive(collapsedModel, hp, currentCustomer, customerAlreadyAtTable) // G_0
        + prior.logUnnormalizedPredictive(state.getTable(current).size(), state.nTables()); // table prior probabilities
    }
    int createTableIndex = state.nTables();
    logUnnormalizedPrs[createTableIndex] = 
      Parametrics.logMarginal(collapsedModel, hp, currentCustomer) // G_0
      + prior.logUnnormalizedPredictive(0, state.nTables());
    
    Multinomial.expNormalize(logUnnormalizedPrs);

    // sample
    int sampledIndex = Multinomial.sampleMultinomial(rand , logUnnormalizedPrs);
    
    // do the assignment
    if (sampledIndex == createTableIndex)
      state.addCustomerToNewTable(customer);
    else
      state.addCustomerToExistingTable(customer, existingTables.get(sampledIndex));
    /* endRem */
  }

}
