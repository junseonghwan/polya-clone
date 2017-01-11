package polya.parametric.normal;

import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;
import org.ejml.simple.SimpleMatrix;

import polya.parametric.CollapsedConjugateModel;
import polya.parametric.HyperParameter;
import polya.parametric.Parameter;
import polya.parametric.SufficientStatistic;
import polya.parametric.TestedModel;
import tutorialj.Tutorial;
import bayonet.distributions.Normal;
import bayonet.math.EJMLUtils;
import bayonet.math.SpecialFunctions;
import blang.annotations.FactorComponent;


/**
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class CollapsedNIWModel implements CollapsedConjugateModel, TestedModel
{
	@FactorComponent
	private final NIWHyperParameter hp;

	public CollapsedNIWModel(HyperParameter hp) 
	{
		this.hp = (NIWHyperParameter) hp;
	}

  /**
   * ### CollapsedNIWModel: Implementation of a NIW model
   * 
   * Make sure you check this source carefully: p.46 of 
   * 
   * http://cs.brown.edu/~sudderth/papers/sudderthPhD.pdf
   * 
   * Also, for matrix computation you will be using EJML:
   * 
   * https://code.google.com/p/efficient-java-matrix-library/wiki/SimpleMatrix
   * 
   * I suggest to start with ``SimpleMatrix`` operations (but see optional
   * questions for suggested optional improvements in this 
   * area).
   * 
   * #### Method to implement: logPriorDensityAtThetaStar
   * 
   * See 
   * ``CollapsedConjugateModel``, ``NIWHyperParameter``, as well
   * as ``bayonet.SpecialFunctions.multivariateLogGamma()``.
   * 
   */
  @Tutorial(showSource = false, showLink = true)
  @Override
  public double logPriorDensityAtThetaStar(HyperParameter _hp)
  {
    NIWHyperParameter hp = (NIWHyperParameter) _hp;
    
    /* startRem throw new RuntimeException(); */
    double result = 0.0;
    final SimpleMatrix deltaTimeNu = hp.delta().scale(hp.nu());
    result -= 0.5 * deltaTimeNu.trace();
    result -= (hp.kappa()/2.0) * EJMLUtils.vectorL2NormSquared(hp.scriptV());
    result += (hp.nu() / 2.0) * Math.log(deltaTimeNu.determinant());
    result -= (hp.nu() * hp.dim()) * Math.log(2.0);
    result -= SpecialFunctions.multivariateLogGamma(hp.dim(), hp.nu()/2.0);
    result -= ((double) hp.dim()) / 2.0 * Math.log(2.0 * Math.PI / hp.kappa());
    return result;
    /* endRem */
  }

  /**
   * #### Method to implement: logLikelihoodGivenThetaStar
   * 
   * See 
   * ``CollapsedConjugateModel``, ``SufficientStatistic``, as well as
   * ``bayonet.distributions.Normal``.
   * 
   * Hint: pick theta* to have mean zero and identity covariance.
   * 
   */
  @Tutorial(showSource = false, showLink = true)
  @Override
  public double logLikelihoodGivenThetaStar(SufficientStatistic _data)
  {
    TwoMomentsSufficientStatistics data = (TwoMomentsSufficientStatistics) _data;

    /* startRem throw new RuntimeException(); */
    double sum = 0.0;
    for (int i = 0; i < data.dim(); i++)
      sum += Normal.logProb(0.0, 1.0, data.sumOfValues().get(i), data.sumOfOuterProducts().get(i, i), data.numberOfPoints());
    return sum;
    /* endRem */
  }
  
  /**
   * #### Method to implement: update
   * 
   * This is the last one
   * before the end of the parametric part of this exercise!
   * 
   * See 
   * ``CollapsedConjugateModel``, ``NIWHyperParameter``, ``SufficientStatistic``.
   */
  @Tutorial(showSource = false, showLink = true)
  @Override
  public HyperParameter update(HyperParameter _before, SufficientStatistic _data)
  {
    NIWHyperParameter before = (NIWHyperParameter) _before;
    TwoMomentsSufficientStatistics data = (TwoMomentsSufficientStatistics) _data;
    
    checkCompatible(before, data);
    
    /* startRem throw new RuntimeException(); */
    final SimpleMatrix sumObs = data.sumOfValues();
    final SimpleMatrix outerProducts = data.sumOfOuterProducts();
    final int dim = data.dim();
    final int numPoints = data.numberOfPoints();
    final double nu = before.nu();
    final double kappa = before.kappa();
    final double kappaPrime = kappa + numPoints;
    final double nuPrime = nu + numPoints; 
    final SimpleMatrix scriptV = before.scriptV();
    final SimpleMatrix delta = before.delta();
    
    final SimpleMatrix scriptVPrime = new SimpleMatrix(dim,1);
    final SimpleMatrix deltaPrime = new SimpleMatrix(dim, dim);
    
    for (int i = 0; i < dim; i++)
      scriptVPrime.set(i, 0, (kappa * scriptV.get(i,0) + sumObs.get(i, 0)) / kappaPrime);
    
    for (int i = 0; i < dim; i++)
      for (int j = 0; j < dim; j++)
        deltaPrime.set(i,j, 
            (nu * delta.get(i,j) + outerProducts.get(i,j) + kappa * scriptV.get(i,0) * scriptV.get(j,0) - kappaPrime * scriptVPrime.get(i,0) * scriptVPrime.get(j,0)) 
              / nuPrime);
    
    return new NIWHyperParameter(kappaPrime, scriptVPrime, nuPrime, deltaPrime);
    /* endRem */
  }

  /**
   * Performs a simple sanity check on dimensionality.
   * @param before
   * @param data
   */
  private void checkCompatible(NIWHyperParameter before,
      TwoMomentsSufficientStatistics data)
  {
    if (before.dim() != data.dim())
      throw new RuntimeException();
  }

  /**
   * Used for testing purpose. See ParametricsTutorial and TestedModel.
   */
  @SuppressWarnings({"unchecked","rawtypes"})
  @Override
  public Pair<Parameter, SufficientStatistic> generateData(
      Random rand,
      HyperParameter _hp, 
      int nDataPoints)
  {
    NIWHyperParameter hp = (NIWHyperParameter) _hp;
    // generate prior
    MVNParameter parameter = NIWs.nextNIW(rand, hp);
    // generate data
    TwoMomentsSufficientStatistics stats = TwoMomentsSufficientStatistics.fromEmpty(hp.dim());
    for (int i = 0; i < nDataPoints; i++)
    {
      SimpleMatrix sample = NIWs.nextMVN(rand, parameter.getMeanParameter(), parameter.getCovarianceParameter());
      stats.plusEqual(TwoMomentsSufficientStatistics.fromOnePoint(sample.getMatrix().getData()));
    }
    Pair result = Pair.of(parameter, stats);
    return result;
  }

  /**
   * Used for testing purpose. See ParametricsTutorial and TestedModel
   */
  @Override
  public Parameter maximumAPosteriori(HyperParameter _hp)
  {
    NIWHyperParameter hp = (NIWHyperParameter) _hp;
    return NIWs.maximumAPosteriori(hp);
  }

  /**
   * Used for testing purpose. See ParametricsTutorial and TestedModel
   */
  @Override
  public double distance(Parameter _truth, Parameter _reconstructed)
  {
    MVNParameter truth = (MVNParameter) _truth;
    MVNParameter reconstructed = (MVNParameter) _reconstructed;
    double norm = NIWs.lInfinityDistance(truth, reconstructed);
    return norm;
  }

  @Override
  public HyperParameter getHyperParameter() 
  {
	  return hp;
  }

  @Override
  public boolean checkHyperParameterBound() 
  {
	  int D = hp.dim();
	  if (hp.nu() <= D - 1) 
		  return false;
	  if (hp.kappa() <= 0)
		  return false;
	  
	  return true;
  }
}
