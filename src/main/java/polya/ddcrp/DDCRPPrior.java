package polya.ddcrp;

import java.util.Set;

import blang.annotations.FactorArgument;
import blang.factors.Factor;
import blang.variables.RealVariable;

public class DDCRPPrior implements Factor
{
	private DecayFunction f;
	@FactorArgument
	private final DDCRPStateInterface state;
	@FactorArgument//(makeStochastic=true)
	private final RealVariable alpha;

	public DDCRPPrior(double alpha, DecayFunction f, DDCRPStateInterface state) {
		this.alpha = new RealVariable(alpha);
		this.state = state;
		this.f = f;
	}	
	
	@Override
  public double logDensity() {
		Set<Customer> customers = state.getAllCustomers();
		double logDensity = 0.0;
		for (Customer customer : customers) {
			if (customer.equals(customer.pointer)) 
				logDensity += Math.log(alpha.getValue());
			else
				logDensity += Math.log(f.decay(customer, customer.pointer));
		}
	  return logDensity;
  }
	
	public double logUnnormalizedPredictive(Customer c1, Customer c2) {
		if (c1.equals(c2))
			return Math.log(alpha.getValue());
		return Math.log(f.decay(c1, c2));
	}
	
	public double getAlpha() { return alpha.getValue(); }
}
