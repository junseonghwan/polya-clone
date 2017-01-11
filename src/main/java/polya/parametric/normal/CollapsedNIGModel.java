package polya.parametric.normal;

import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;

import bayonet.distributions.Gamma;
import bayonet.distributions.Normal;
import bayonet.math.SpecialFunctions;
import blang.annotations.FactorComponent;
import polya.parametric.CollapsedConjugateModel;
import polya.parametric.HyperParameter;
import polya.parametric.Parameter;
import polya.parametric.SufficientStatistic;
import polya.parametric.TestedModel;

/**
 * x ~ N(mu, sigma^2)
 * mu ~ N(mu0, sigma^2/nu)
 * sigma^2 ~ IG(alpha, beta)
 * 
 * @author Seong-Hwan Jun (s2jun.uw@gmail.com)
 *
 */
public class CollapsedNIGModel implements CollapsedConjugateModel, TestedModel
{
	@FactorComponent
	private final NIGHyperParameter hp;
	
	public CollapsedNIGModel(HyperParameter hp)
	{
		this.hp = (NIGHyperParameter)hp;
	}

	@Override
	public double logPriorDensityAtThetaStar(HyperParameter hp)
	{
		NIGHyperParameter param = (NIGHyperParameter)hp;

		// prior is Normal-Inverse-Gamma evaluated at some arbitrary value, theta* = (mu, sigma^2) = (0, 1)
		double retval = 0.5*Math.log(param.nu());
		retval += param.alpha() * Math.log(param.beta());
		//retval += (param.alpha() + 1)*Math.log(1.0/1.0);
		
		retval -= (2*param.beta() + param.nu() * Math.pow(param.mu0(), 2.0)) / 2;

		retval -= 0.5 * Math.log(2*Math.PI);
		retval -= SpecialFunctions.lnGamma(param.alpha());
		
		return retval;
	}

	@Override
	public double logLikelihoodGivenThetaStar(SufficientStatistic data)
	{
		TwoMomentsSufficientStatistics suff = (TwoMomentsSufficientStatistics)data;

		double retval = Normal.logProb(0.0, 1.0, suff.sumOfValues().get(0, 0), suff.sumOfOuterProducts().get(0, 0), suff.numberOfPoints());
		return retval;
	}

	@Override
	public HyperParameter update(HyperParameter before, SufficientStatistic data)
	{
		// make sure this code is correct
		NIGHyperParameter param = (NIGHyperParameter)before;
		TwoMomentsSufficientStatistics suff = (TwoMomentsSufficientStatistics)data;

		int n = data.numberOfPoints();
		double xbar = suff.sumOfValues().get(0, 0)/n;

		double mu0 = (param.nu()*param.mu0() + n*xbar)/(param.nu() + n);
		double nu = param.nu() + n;
		double alpha = param.alpha() + n/2.0;
		double beta = param.beta() + 0.5*(suff.sumOfOuterProducts().get(0,0) - n*Math.pow(xbar, 2.0)) + (n*param.nu()/(param.nu() + n))*(Math.pow(xbar - param.mu0(), 2)/2);

		return new NIGHyperParameter(mu0, nu, alpha, beta);
	}

	@Override
	public HyperParameter getHyperParameter()
	{
	  return hp;
	}

	@Override
	public boolean checkHyperParameterBound()
	{
		if (hp.alpha() <= 0 || hp.beta() <= 0 || hp.nu() <= 0)
			return false;
					
		return true;
	}

	/**
	 * Generate the model parameter, mu and sigma^2
	 * 
	 */
	@Override
	public Pair<Parameter, SufficientStatistic> generateData(Random rand,
			HyperParameter _hp, int nDataPoints) {
		NIGHyperParameter hp = (NIGHyperParameter)_hp;

		// generate var ~ IG(alpha, beta)
		double var = 1/Gamma.generate(rand, hp.beta(), hp.alpha());
		//double var = InverseGamma.generate(rand, hp.alpha(), hp.beta());
		// generate mu ~ N(mu0, sigma^2)
		double mean = Normal.generate(rand, hp.mu0(), var/hp.nu());

		Parameter param = new NormalParameter(mean, var);

		// generate the data
		SufficientStatistic suff = TwoMomentsSufficientStatistics.fromEmpty(1);
		for (int i = 0; i < nDataPoints; i++)
		{
			double x = Normal.generate(rand, mean, var);
			suff.plusEqual(TwoMomentsSufficientStatistics.fromOnePoint(new double[]{x}));
		}
		
		return Pair.of(param, suff);
	}

	@Override
	public Parameter maximumAPosteriori(HyperParameter _hp) 
	{
		NIGHyperParameter hp = (NIGHyperParameter)_hp;
		double varMAP = 2*hp.beta() / (2*(hp.alpha() + 2));
		NormalParameter param = new NormalParameter(hp.mu0(), varMAP);
		return param;
	}

	/**
	 * Euclidean norm
	 */
	@Override
	public double distance(Parameter _truth, Parameter _reconstructed) 
	{
		NormalParameter truth = (NormalParameter)_truth;
		NormalParameter reconstructied = (NormalParameter)_reconstructed;
		
		double dist = Math.pow(truth.mean() - reconstructied.mean(), 2.0);
		dist += Math.pow(truth.var() - reconstructied.var(), 2.0);
		dist = Math.sqrt(dist);
		return dist;
	}

}
