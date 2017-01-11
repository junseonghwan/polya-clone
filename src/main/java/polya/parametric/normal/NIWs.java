package polya.parametric.normal;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.ejml.ops.CovarianceRandomDraw;
import org.ejml.ops.NormOps;
import org.ejml.simple.SimpleMatrix;

import briefj.BriefArrays;
import briefj.BriefIO;
import briefj.BriefMath;
import polya.ddcrp.Customer;
import polya.parametric.Parametrics;
import polya.parametric.SufficientStatistic;


/**
 * Some utilities for NIW models.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class NIWs
{
  /**
   * Return a function that given a point gives the probability of this point under the provided
   * hyperparameters. Typically used to plot the posterior (achieved via updated hyper-params and
   * PlotContour)
   * 
   * @param updated
   * @return
   */
  public static MultivariateFunction logMarginalAsFunctionOfData(final NIWHyperParameter updated)
  {
    return new MultivariateFunction() {
      
      @Override
      public double value(double[] point)
      {
        TwoMomentsSufficientStatistics stat = TwoMomentsSufficientStatistics.fromOnePoint(point);
        return Parametrics.logMarginal(new CollapsedNIWModel(updated), updated, stat);
      }
    };
  }
  
  /**
   * 
   * @param param
   * @return
   */
  public static MVNParameter maximumAPosteriori(NIWHyperParameter param)
  {
    double scaling = param.nu() / (param.nu() + param.dim() - 1);
    if (scaling < 0)
      scaling = Double.NaN;
    return new MVNParameter(param.scriptV(), param.delta().scale(scaling));
  }
  
  /**
   * Samples from a multivariate normal distribution.
   * 
   * @param rand
   * @param mean
   * @param covar
   * @return
   */
  public static SimpleMatrix nextMVN(Random rand, SimpleMatrix mean, SimpleMatrix covar)
  {
    final int dim = covar.numRows();
    if (dim != mean.numRows() ||
        dim != covar.numCols())
      throw new RuntimeException();
    covar = covar.copy(); // important
    CovarianceRandomDraw normalGenerator = new CovarianceRandomDraw(rand, covar.getMatrix());
    SimpleMatrix result = new SimpleMatrix(dim,1);
    normalGenerator.next(result.getMatrix());
    return result.plus(mean);
  }
  
  /**
   * Samples from a normal inverse Wishart distribution
   * 
   * @param rand
   * @param param
   * @return
   */
  public static MVNParameter nextNIW(Random rand, NIWHyperParameter param)
  {
    SimpleMatrix covar = nextInverseWishart(rand, param.delta(), BriefMath.getAndCheckInt(param.nu()));
    SimpleMatrix mean = nextMVN(rand, param.scriptV(), covar.scale(1.0 / param.kappa()));
    return new MVNParameter(mean, covar);
  }
  
  /**
   * Samples from an INVERSE Wishart distribution
   * 
   * @param rand
   * @param V
   * @param degreesOfFreedom
   * @return
   */
  private static SimpleMatrix nextInverseWishart(Random rand, SimpleMatrix V, int degreesOfFreedom)
  {
    return nextWishart(rand, V.invert(), degreesOfFreedom).invert();
  }
  
  /**
   * Sample from a Wishart distribution (NOT inverse)
   * 
   * @param rand
   * @param V
   * @param degreesOfFreedom
   * @return
   */
  private static SimpleMatrix nextWishart(Random rand, SimpleMatrix V, int degreesOfFreedom)
  {
    final int dim = V.numRows();
    if (V.numCols() != dim || degreesOfFreedom <= dim - 1)
      throw new RuntimeException();
    // simulate 
    CovarianceRandomDraw normalGenerator = new CovarianceRandomDraw(rand, V.getMatrix());
    SimpleMatrix X = new SimpleMatrix(degreesOfFreedom, dim);
    
    
    for (int i = 0; i < degreesOfFreedom; i++)
    {
      SimpleMatrix draw = new SimpleMatrix(dim, 1);
      normalGenerator.next(draw.getMatrix());
      for (int d = 0; d < dim; d++)
        X.set(i, d, draw.get(d));
    }
    
    return X.transpose().mult(X).scale(1.0/degreesOfFreedom);
  }
  
  /**
   * Creates observations ready to use by the NIW machinery from a CSV file.
   * 
   * Reads a CSV dataset where each row is a point, and columns are dimensions.
   * Transform each datapoint into a TwoMomentsSufficientStatistics.
   * 
   * @param file
   * @return A map from datapoint index (row in file) to the sufficient stat of that row
   */
  public static Map<Integer,SufficientStatistic> loadFromCSVFile(File file)
  {
    Map<Integer,SufficientStatistic> result = new HashMap<Integer,SufficientStatistic>();
    int i = 0;
    for (List<String> datum : BriefIO.readLines(file).splitCSV())
    {
      double [] parsedDatum = BriefArrays.parseDoublesToArray(datum);
      SufficientStatistic stat = TwoMomentsSufficientStatistics.fromOnePoint(parsedDatum);
      result.put(i++, stat);
    }
    return result;
  }

  public static Map<Customer,SufficientStatistic> loadCustomersFromCSVFile(File file)
  {
    Map<Customer,SufficientStatistic> result = new HashMap<Customer,SufficientStatistic>();
    int id = 0;
    for (List<String> datum : BriefIO.readLines(file).splitCSV())
    {
      double [] parsedDatum = BriefArrays.parseDoublesToArray(datum);
      SufficientStatistic stat = TwoMomentsSufficientStatistics.fromOnePoint(parsedDatum);
      result.put(new Customer(id++, (int)parsedDatum[0], (int)parsedDatum[1], parsedDatum[2]), stat);
    }
    return result;
  }

  /**
   * 
   * @param p1
   * @param p2
   * @return l_infinity norm over both mean and covariance matrices
   */
  public static double lInfinityDistance(MVNParameter p1, MVNParameter p2)
  {
    double norm1 = lInfinityDistance(p1.getMeanParameter(), p2.getMeanParameter());
    double norm2 = lInfinityDistance(p1.getCovarianceParameter(), p2.getCovarianceParameter());
    return Math.max(norm1, norm2);
  }
  
  /**
   * 
   * @param m1
   * @param m2
   * @return
   */
  public static double lInfinityDistance(SimpleMatrix m1, SimpleMatrix m2)
  {
    return NormOps.normPInf(m1.minus(m2).getMatrix());
  }

}
