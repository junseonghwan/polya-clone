package polya.parametric.normal;

import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;

import bayonet.distributions.Normal;
import blang.annotations.FactorComponent;
import polya.parametric.CollapsedConjugateModel;
import polya.parametric.HyperParameter;
import polya.parametric.Parameter;
import polya.parametric.SufficientStatistic;
import polya.parametric.TestedModel;

/**
 * Simple collapsed model of Normal likelihood with known variance and Normal prior on 
 * the unknown mean parameter
 * 
 * Following the notation of Equation (12,13) in 
 * http://www.stat.ubc.ca/~bouchard/courses/stat547-sp2013-14/lecture/2014/01/12/notes-lecture3.html
 * 
 * l(x|mu, sigma^2) = N(mu, sigma^2)
 * mu ~ p(mu | mu0, sigma0^2) = N(mu0, sigma0^2)
 * 
 * Notation: x represents the data, mu represents the model parameter
 * Note 1: sigma is known
 * Note 2: mu0 and sigma0 are the hyper parameters
 * 
 * @author Seong-Hwan Jun (s2jun.uw@gmail.com)
 *
 */
public class CollapsedNormalNormalModel implements CollapsedConjugateModel, TestedModel
{
	private double var;

	@FactorComponent
	public final NormalParameter hp;

	public CollapsedNormalNormalModel(double var, HyperParameter hp)
	{
		this.var = var;
		this.hp = (NormalParameter)hp;
	}
	
	@Override
	public boolean checkHyperParameterBound()
	{
		if (hp.var() < 0.0)
			return false;
		
		return true;
	}
	
	@Override
	public double logPriorDensityAtThetaStar(HyperParameter hp) 
	{
		NormalParameter param = (NormalParameter)hp;

		// evaluate the prior, Normal(mu0, sigma0^2), at z=0.0
		double result = Normal.logDensity(0.0, param.mean(), param.var());
		return result;
	}

	@Override
	public double logLikelihoodGivenThetaStar(SufficientStatistic data) 
	{
		// evaluate the Normal likelihood at x with (z, sigma^2) = (0.0, var)
		TwoMomentsSufficientStatistics suff = (TwoMomentsSufficientStatistics)data;
		if (suff.dim() > 1)
			throw new RuntimeException();
		
		double sum = suff.sumOfValues().get(0, 0);
		double sumSq = suff.sumOfOuterProducts().get(0, 0);
		int n = suff.numberOfPoints();
		double result = Normal.logProb(0.0, var, sum, sumSq, n);
		return result;
	}

	@Override
	/**
	 * Update formula can be found in 
	 * http://en.wikipedia.org/wiki/Conjugate_prior#Continuous_distributions
	 * under the case "Normal likelihood with known variance"
	 */
	public HyperParameter update(HyperParameter before, SufficientStatistic data) 
	{
		TwoMomentsSufficientStatistics suff = (TwoMomentsSufficientStatistics)data;
		NormalParameter hp0 = (NormalParameter)before;
		
		double sum = suff.sumOfValues().get(0, 0);
		int n = suff.numberOfPoints();
		
		double mean1 = (hp0.mean()/hp0.var() + sum/var) / (1/hp0.var() + n/var);
		double var1 = 1.0/(1/hp0.var() + n/var);

		NormalParameter hp1 = new NormalParameter(mean1, var1);
		return hp1;
	}

	@Override
	public HyperParameter getHyperParameter()
	{
		return hp;
	}

	@Override
	public Pair<Parameter, SufficientStatistic> generateData(Random rand, HyperParameter _hp, int nDataPoints) 
	{
		NormalParameter hp = (NormalParameter)_hp;
		
		// generate the parameter, mu (in this case, only the mean is considered as unknown, mu ~ N(hp.mean(), hp.var())
		double mu = Normal.generate(rand, hp.mean(), hp.var());
		Parameter param = new NormalParameter(mu, var);

		// generate the data from N(mu, var)
		// TODO: using TwoMomentSufficientStatistics may not be the most optimal way to do it, make it better later (not critical)
		SufficientStatistic suff = TwoMomentsSufficientStatistics.fromEmpty(1);
		for (int i = 0; i < nDataPoints; i++)
		{
			double x = Normal.generate(rand, mu, var);
			TwoMomentsSufficientStatistics dataPoint = TwoMomentsSufficientStatistics.fromOnePoint(new double[]{x});
			suff.plusEqual(dataPoint);
		}
		
		// wrap it as a Parameter and return it
		Pair<Parameter, SufficientStatistic> retval = Pair.of(param, suff);
		return retval;
	  }

	/**
	 * The posterior distribution is Normal, for Normal distribution, mean = mode
	 */
	@Override
	public Parameter maximumAPosteriori(HyperParameter _hp) 
	{
		NormalParameter hp = (NormalParameter)_hp;
		return hp;
	}

	/**
	 * computes L2 norm of the difference in the reconstructed mean and the true mean
	 */
	@Override
	public double distance(Parameter _truth, Parameter _reconstructed) 
	{
		NormalParameter truth = (NormalParameter)_truth;
		NormalParameter reconstructed = (NormalParameter)_reconstructed;
		
		// compare only the mean
		double dist = Math.pow(truth.mean() - reconstructed.mean(), 2);
		
	  return dist;
	}

}
