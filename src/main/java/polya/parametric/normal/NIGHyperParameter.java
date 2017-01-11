package polya.parametric.normal;

import blang.annotations.FactorArgument;
import blang.variables.RealVariable;
import polya.parametric.HyperParameter;

public class NIGHyperParameter implements HyperParameter
{
	@FactorArgument(makeStochastic=true)
	private final RealVariable mu0;
	@FactorArgument(makeStochastic=true)
	private final RealVariable nu;
	@FactorArgument(makeStochastic=true)
	private final RealVariable alpha;
	@FactorArgument(makeStochastic=true)
	private final RealVariable beta;
	
	public NIGHyperParameter(double mu0, double nu, double alpha, double beta)
	{
		this.mu0 = new RealVariable(mu0);
		this.nu = new RealVariable(nu);
		this.alpha = new RealVariable(alpha);
		this.beta = new RealVariable(beta);
	}
	
	public double mu0()
	{
		return mu0.getValue();
	}
	
	public double nu()
	{
		return nu.getValue();
	}
	
	public double alpha()
	{
		return alpha.getValue();
	}
	
	public double beta()
	{
		return beta.getValue();
	}
}
