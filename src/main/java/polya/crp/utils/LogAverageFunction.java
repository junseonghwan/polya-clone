package polya.crp.utils;

import java.util.List;

import org.apache.commons.math3.analysis.MultivariateFunction;

import bayonet.math.NumericalUtils;

import com.google.common.collect.Lists;


/**
 * Given functions logF1, logF2, .. and
 * corresponding log weights logW1, logW2, ..
 * 
 * return a function that evaluates like:
 * 
 * log(exp(logW1) exp(logF1) + exp(logW2) exp(logF2) + ... )
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class LogAverageFunction implements MultivariateFunction
{
  private final List<Double> logWeights = Lists.newArrayList();
  private final List<MultivariateFunction> functions = Lists.newArrayList();
  
  /**
   * 
   * @param logWeight
   * @param function
   */
  public void addFunction(double logWeight, MultivariateFunction function)
  {
    logWeights.add(logWeight);
    functions.add(function);
  }

  /**
   * 
   */
  @Override
  public double value(double[] point)
  {
    double sum = Double.NEGATIVE_INFINITY;
    for (int i =0; i < logWeights.size(); i++)
    {
      double lw = logWeights.get(i);
      double pred = functions.get(i).value(point);
      sum = NumericalUtils.logAdd(sum, lw + pred);
    }
    return sum;
  }
  
}