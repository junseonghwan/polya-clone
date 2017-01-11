package polya.parametric.normal;

import org.ejml.simple.SimpleMatrix;

import polya.parametric.Parameter;


/**
 * 
 * The parameters of a multivariate normal distribution.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class MVNParameter implements Parameter
{
  private final SimpleMatrix meanParameter, covarianceParameter;

  public MVNParameter(
      SimpleMatrix meanParameter,
      SimpleMatrix covarianceParameter)
  {
    this.meanParameter = meanParameter;
    this.covarianceParameter = covarianceParameter;
  }

  public SimpleMatrix getMeanParameter()
  {
    return meanParameter;
  }

  public SimpleMatrix getCovarianceParameter()
  {
    return covarianceParameter;
  }
}
