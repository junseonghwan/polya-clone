package polya.parametric.normal;

import blang.annotations.FactorArgument;
import blang.variables.RealVariable;
import polya.parametric.HyperParameter;
import polya.parametric.Parameter;

/**
 * Class representing the parameters of Normal distribution (mu, sigma^2)
 * This class implements both HyperParameter and Parameter interfaces with the intention that
 * it maybe used to represent hyper parameter of a Normal-Normal, mu ~ N(mu0, sigma0^2) or 
 * simply as a class representing parameters of a Normal distribution
 * 
 * @author Seong-Hwan Jun (s2jun.uw@gmail.com)
 *
 */
public class NormalParameter implements HyperParameter, Parameter
{
	@FactorArgument(makeStochastic=true)
	private final RealVariable mean;
	@FactorArgument(makeStochastic=true)
	private final RealVariable var;
	
	public NormalParameter(double mean, double var)
	{
		this.mean = new RealVariable(mean);
		this.var = new RealVariable(var);
	}
	
	public double mean()
	{
		return mean.getValue();
	}
	
	public double var()
	{
		return var.getValue();
	}

}
