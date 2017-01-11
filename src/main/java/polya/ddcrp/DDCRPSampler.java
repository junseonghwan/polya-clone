package polya.ddcrp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

public class DDCRPSampler implements MHProposalDistribution
{
	@SampledVariable
	DDCRPState ddCRPState;
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
      DDCRPState state, 
      HyperParameter hp, 
      CollapsedConjugateModel collapsedModel,
      DDCRPPrior prior)
  {
  	ClusterId customerTableId = state.removeCustomerLink(customer);

    // consider all the ways to re-insert the customer
    // 1. compute the probabilities for merging the customer's table with another table
    List<ClusterId> existingTables = state.getAllClusterIds();
    Map<ClusterId, Double> logUnnormalizedTableMergePrs = new HashMap<>();
    SufficientStatistic currentCustomerTable = state.getClusterStatistics(customerTableId);
    // compute the likelihood of the customer's current table
    double unnormalizedCustomerTablePr = Parametrics.logMarginal(collapsedModel, hp, currentCustomerTable);
    for (int i = 0; i < state.nTables(); i++)
    {
      ClusterId current = existingTables.get(i);
      if (current.equals(customerTableId))
      	continue;
      
      SufficientStatistic customerAlreadyAtTable = state.getClusterStatistics(current).copy();
      double unnormalizedCurrentTablePr = Parametrics.logMarginal(collapsedModel, hp, customerAlreadyAtTable);
      customerAlreadyAtTable.plusEqual(currentCustomerTable);
      double unnormalizedTableMergePr = Parametrics.logMarginal(collapsedModel, hp, customerAlreadyAtTable);
      logUnnormalizedTableMergePrs.put(current, unnormalizedTableMergePr - (unnormalizedCurrentTablePr + unnormalizedCustomerTablePr)); 
    }

    // 2. compute the probability for pointing to each of the customers and apply the appropriate logUnnormalizedTableMergePrs
    List<Customer> customers = new ArrayList<>(state.getAllCustomers());
    int nOutcomes = customers.size();
    double [] logUnnormalizedPrs = new double[nOutcomes];
    for (int i = 0; i < nOutcomes; i++) {
    	Customer cc = customers.get(i);
  		logUnnormalizedPrs[i] = prior.logUnnormalizedPredictive(customer, cc);

  		if (logUnnormalizedPrs[i] == Double.NEGATIVE_INFINITY)
  			continue;
  		
  		// find out if joining cc and customer will result in merging of the tables
  		ClusterId otherTableId = state.getClusterId(cc);
  		if (!customerTableId.equals(otherTableId)) {
  			logUnnormalizedPrs[i] += logUnnormalizedTableMergePrs.get(otherTableId);
  		}
    }

    Multinomial.expNormalize(logUnnormalizedPrs);

    // sample
    int sampledIndex = Multinomial.sampleMultinomial(rand , logUnnormalizedPrs);

    // do the assignment
    Customer pointer = customers.get(sampledIndex);

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
