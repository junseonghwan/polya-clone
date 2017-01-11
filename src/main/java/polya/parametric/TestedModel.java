package polya.parametric;

import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;


/**
 * 
 * A model that can be tested by generating data (doing forward 
 * simulation), and checking if the MAP converges to the true 
 * parameter.
 * 
 * See how these are used in ParametricsTutorial
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public interface TestedModel extends CollapsedConjugateModel
{
  /**
   * 
   * @param rand
   * @param hp
   * @param nDataPoints The number of data points
   * @return A pair containing (1) a realization of the random parameter 
   * (e.g. a mean and covar matrix in the NIW case) and (2) nDataPoints observations
   * generated iid from these parameters, summarized via their
   * SufficientStatistic.
   */
  public Pair<Parameter, SufficientStatistic> generateData(
      Random rand, 
      HyperParameter hp, 
      int nDataPoints);
  
  /**
   * 
   * @param hp
   * @return
   */
  public Parameter maximumAPosteriori(HyperParameter hp);
  
  /**
   * 
   * @param truth
   * @param reconstructed
   * @return
   */
  public double distance(Parameter truth, Parameter reconstructed);
}