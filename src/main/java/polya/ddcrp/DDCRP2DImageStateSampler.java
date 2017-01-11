package polya.ddcrp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import polya.crp.utils.ClusterId;
import polya.parametric.CollapsedConjugateModel;
import polya.parametric.HyperParameter;
import polya.parametric.Parametrics;
import polya.parametric.SufficientStatistic;
import bayonet.distributions.Multinomial;
import blang.factors.Factor;
import blang.mcmc.ConnectedFactor;
import blang.mcmc.MHProposalDistribution;
import blang.mcmc.SampledVariable;

public class DDCRP2DImageStateSampler implements MHProposalDistribution
{
	@SampledVariable
	DDCRP2DImageState ddCRPState;
	@ConnectedFactor
	List<Factor> connectedFactors; 

	@Override
  public Proposal propose(Random rand) {
		DDCRPPrior ddCRPPrior = null;
		CollapsedConjugateModel collapsedModel = null;
		HyperParameter hp = null;
		
		for (Factor f : connectedFactors)
		{
			if (f instanceof DDCRPPrior)
			{
				ddCRPPrior = (DDCRPPrior)f;
			}
			if (f instanceof DDCRPFactor)
			{
				DDCRPFactor ddCRPFactor = (DDCRPFactor)f;
				collapsedModel = ddCRPFactor.getCollapsedConjugateModel();
				hp = collapsedModel.getHyperParameter();
			}
		}
		
		if (ddCRPPrior == null)
			throw new RuntimeException("DDCRPPrior is not connected to the DDCRPState");
		if (collapsedModel == null)
			throw new RuntimeException("DDCRPFactor is not connected to the DDCRPState");
		
		// randomly select a customer to re-seat
		List<Customer> customers = new ArrayList<Customer>(ddCRPState.getAllCustomers());
		Customer customer = customers.get(rand.nextInt(customers.size()));
		
		gibbs(rand, customer, ddCRPState, hp, collapsedModel, ddCRPPrior);
		
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
      Customer customer,
      DDCRP2DImageState state, 
      HyperParameter hp, 
      CollapsedConjugateModel collapsedModel,
      DDCRPPrior prior)
  {
  	ClusterId customerTableId = state.removeCustomerLink(customer);
  	SufficientStatistic currentCustomerTableSuffStat = state.getClusterStatistics(customerTableId);
  	double unnormalizedCustomerTablePr = Parametrics.logMarginal(collapsedModel, hp, currentCustomerTableSuffStat);
    
    // consider all the ways to re-insert the customer -- for this problem, the connection can only be made to the neighbors
    // 1. compute the probabilities for merging the customer's table with another table (consider only the neighbors' tables)
  	List<Customer> neighbors = new ArrayList<>(state.getNeighbors(customer));
  	int nOutcomes = neighbors.size() + 1;
    double [] logUnnormalizedPrs = new double[nOutcomes];
  	for (int i = 0; i < nOutcomes - 1; i++) {
  		// compute the probability of pointing to this neighbor
  		// 1. no need to compute the prior (the decay function) because the neighbors all have a value of 1
  		// 2. if the cluster of the neighbor and the customer are the same, no need to compute anything
  		// 3. if the cluster of the neighbor and the customer are different, need to consider the table join probability
  		Customer neighbor = neighbors.get(i);
  		ClusterId neighborClusterId = state.getClusterId(neighbor);
  		if (neighborClusterId.equals(customerTableId)) {
  			logUnnormalizedPrs[i] = 0.0; // not necessary piece of code -- but for clarity, leave it for now (might need to debug later)
  		} else {
        SufficientStatistic customerAlreadyAtTable = state.getClusterStatistics(neighborClusterId).copy();
        double unnormalizedCurrentTablePr = Parametrics.logMarginal(collapsedModel, hp, customerAlreadyAtTable);
        customerAlreadyAtTable.plusEqual(currentCustomerTableSuffStat);
        double unnormalizedTableMergePr = Parametrics.logMarginal(collapsedModel, hp, customerAlreadyAtTable);

  			logUnnormalizedPrs[i] = unnormalizedTableMergePr - (unnormalizedCurrentTablePr + unnormalizedCustomerTablePr);
  		}
  	}
  	
  	logUnnormalizedPrs[nOutcomes - 1] = Math.log(prior.getAlpha());

    Multinomial.expNormalize(logUnnormalizedPrs);

    // sample
    int sampledIndex = Multinomial.sampleMultinomial(rand , logUnnormalizedPrs);

    // do the assignment
    Customer pointer = null;
    if (sampledIndex == (nOutcomes - 1)) {
    	// self-pointer
    	pointer = customer;
    } else {
    	pointer = neighbors.get(sampledIndex);
    }

    /*
    if (pointer == customer) {
    	System.out.println("Self link");
    } else {
    	System.out.println(customer.toString() + " points to " + pointer.toString());
    }
    */
    state.updateCustomerLink(customer, pointer);
  }

}
