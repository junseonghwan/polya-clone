package polya.parametric.normal;

import org.ejml.simple.SimpleMatrix;

import polya.parametric.SufficientStatistic;


/**
 * The sufficient statistic for the first two moments.
 * For example for a multivariate normal likelihood model.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class TwoMomentsSufficientStatistics implements SufficientStatistic
{
  /**
   * The number of points this object is summarizing. (Different than dim())
   */
  private int numberOfPoints;
  
  /**
   * The sum of the vectors, \sum_i^numberOfPoints x
   */
  private SimpleMatrix sumOfValues;
  
  /**
   * The sum of the outerproducts, \sum_i^numberOfPoints x x^T
   * where x^T denotes transpose
   */
  private SimpleMatrix sumOfOuterProducts;
  
  /**
   * Create an object containing the sufficient statistic for a single data point
   * @param point Array, with entry p denoting the value of the point for coordinate p
   * @return
   */
  public static TwoMomentsSufficientStatistics fromOnePoint(double [] point)
  {
    final int dim = point.length;
    SimpleMatrix 
      sumOfValues = new SimpleMatrix(dim, 1);
    for (int i = 0; i < dim; i++)
      sumOfValues.set(i, 0, point[i]);
    SimpleMatrix sumOfOuterProd = sumOfValues.mult(sumOfValues.transpose());
    return new TwoMomentsSufficientStatistics(1, sumOfValues, sumOfOuterProd);
  }
  
  /**
   * Create an object containing the sufficient statistic of zero points.
   * @param dim The dimensionality of the points that will inserted in the future
   * @return
   */
  public static TwoMomentsSufficientStatistics fromEmpty(int dim)
  {
    return new TwoMomentsSufficientStatistics(0, new SimpleMatrix(dim, 1), new SimpleMatrix(dim, dim));
  }

  private TwoMomentsSufficientStatistics(int numberOfPoints,
      SimpleMatrix sumOfValues, SimpleMatrix sumOfOuterProducts)
  {
    this.numberOfPoints = numberOfPoints;
    this.sumOfValues = sumOfValues;
    this.sumOfOuterProducts = sumOfOuterProducts;
    checks();
  }

  /**
   * Check the dimensions of the matrices match
   */
  private void checks()
  {
    int dim = dim();
    if (dim < 1 ||
        numberOfPoints < 0 ||
        dim != sumOfOuterProducts.numCols() ||
        dim != sumOfOuterProducts.numRows() ||
        dim != sumOfValues.numRows() ||
        1 != sumOfValues.numCols())
      throw new RuntimeException();
  }

  /**
   * 
   * @return The dimensionality of each point summarized by this object
   */
  public int dim()
  {
    return sumOfOuterProducts.numRows();
  }
  
  /**
   * 
   * @return The sum of the vectors, \sum_i^numberOfPoints x
   */
  public SimpleMatrix sumOfValues()
  {
    return sumOfValues;
  }
  
  /**
   * @return The sum of the outerproducts, \sum_i^numberOfPoints x x^T
   * where x^T denotes transpose
   */
  public SimpleMatrix sumOfOuterProducts()
  {
    return sumOfOuterProducts;
  }

  @Override
  public int numberOfPoints()
  {
    return numberOfPoints;
  }

  @Override
  public void plusEqual(SufficientStatistic _other)
  {
    TwoMomentsSufficientStatistics other = (TwoMomentsSufficientStatistics) _other;
    this.numberOfPoints += other.numberOfPoints;
    this.sumOfValues = this.sumOfValues.plus(other.sumOfValues);
    this.sumOfOuterProducts = this.sumOfOuterProducts.plus(other.sumOfOuterProducts);
  }

  @Override
  public void minusEqual(SufficientStatistic _other)
  {
    TwoMomentsSufficientStatistics other = (TwoMomentsSufficientStatistics) _other;
    this.numberOfPoints -= other.numberOfPoints;
    this.sumOfValues = this.sumOfValues.minus(other.sumOfValues);
    this.sumOfOuterProducts = this.sumOfOuterProducts.minus(other.sumOfOuterProducts);
  }

  @Override
  public SufficientStatistic copy()
  {
    TwoMomentsSufficientStatistics copy = TwoMomentsSufficientStatistics.fromEmpty(dim());
    copy.plusEqual(this);
    return copy;
  }

  @Override
  public String toString()
  {
    return "TwoMomentsSufficientStatistics [numberOfPoints=" + numberOfPoints + "]";
  }


}
