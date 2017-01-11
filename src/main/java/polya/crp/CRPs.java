package polya.crp;

import java.util.Map;

import org.apache.commons.math3.special.Gamma;

import polya.crp.utils.ClusterId;

import com.beust.jcommander.internal.Maps;
import com.google.common.base.Optional;



public class CRPs
{
  public static double crpAssignmentLogProbabilitiy(PYPrior prior, CRPState state)
  {
    if (!prior.inBounds())
      return Double.NEGATIVE_INFINITY;
    Map<Integer,Integer> tableSizeCount = Maps.newHashMap();
    for (ClusterId id : state.getAllClusterIds())
    {
      int currentTableSize = state.getTable(id).size();
      int updatedCount = 1 + Optional.fromNullable(tableSizeCount.get(currentTableSize)).or(0);
      tableSizeCount.put(currentTableSize, updatedCount);
    }
    final int n = state.nTables();
    final int m = state.nCustomers();
    
    // TODO: alpha0 can potentially be < 0, for example, 0 <= discount < 1 => -discount < alpha0 < 0
    // In this case, the result = NaN, so do we only want alpha0 to be > 0?
    double result =   - logRisingFactorial(prior.alpha0(), m);
    
    if (prior.discount() == 0)
      result += n * Math.log(prior.alpha0());
    else
      result += n * Math.log(prior.discount()) + logRisingFactorial(prior.alpha0()/prior.discount(), n);
    
    for (int k : tableSizeCount.keySet())
    {
      int nk = tableSizeCount.get(k);
      double current = blockLogFactor(prior.discount(), k);
      result += nk * current;
    }
    return result;
  }
  public static double logRisingFactorial(double x, int n)
  {
    return Gamma.logGamma(x + n) - Gamma.logGamma(x);
  }
  
  public static double blockLogFactor(double d, int k)
  {
    return logRisingFactorial(1.0-d, k-1);
  }
  

}
