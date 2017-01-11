package polya.crp;

import blang.annotations.FactorArgument;
import blang.factors.Factor;
import blang.variables.RealVariable;


/**
 * A Pitman-Yor prior.
 * 
 * See: http://en.wikipedia.org/wiki/Pitman%E2%80%93Yor_process
 * 
 * Note: the values alpha0 and discount contained in an instance of 
 * this class may change during its existence, because of
 * resampling.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class PYPrior implements Factor
{
  /**
   * The strength parameter. Sometimes denoted theta or alpha0.
   * 
   * Higher values tends to create more table.
   */
	@FactorArgument(makeStochastic=true)
	private final RealVariable alpha0;
  
  /**
   * The discount parameter. 
   * 
   * Boosts table creation proportionally
   * to the number of existing tables.
   */
	@FactorArgument//(makeStochastic=true) // do not sample it, which makes it into a DPMM 
	private final RealVariable discount; // this should be in the interval [0, 1), with discount = 0 implying DPMM instead of PYMM
	
	@FactorArgument(makeStochastic=true)
	private final CRPState crpState;
  
  /**
   * 
   * @param alpha0
   * @param discount
   */
  public PYPrior(double alpha0, double discount, CRPState crpState)
  {
    this.alpha0 = new RealVariable(alpha0);
    this.discount = new RealVariable(discount);
    this.crpState = crpState;
    checkBounds();
  }

  /**
   * @throws RuntimeException if bounds not respected
   */
  public void checkBounds()
  {
    if (!inBounds())
      throw new RuntimeException();
  }
  
  /**
   * 
   * @return
   */
  public boolean inBounds()
  {
    return discount.getValue() >= 0 && discount.getValue() < 1 && alpha0.getValue() > -discount.getValue();
  }

  /**
   * The predictive probability  of assigning a customer to a table
   * where there was already nCustomersAtTable people (before the 
   * queried insertion), and where there are already nTables (before 
   * the queried insertion).
   * 
   * Use nCustomersAtTable set to zero for table creation.
   *   
   * @param nCustomersAtTable
   * @param nTables
   * @return The predictive probability
   */
  public double logUnnormalizedPredictive(int nCustomersAtTable, int nTables)
  {
    if (nCustomersAtTable == 0)
      return Math.log(alpha0.getValue() + nTables * discount.getValue());
    else
      return Math.log(nCustomersAtTable - discount.getValue());
  }
  
  /**
   * The discount parameter. 
   * 
   * Boosts table creation proportionally
   * to the number of existing tables.
   */
  public double discount()
  {
    return discount.getValue();
  }

  /**
   * The strength parameter. Sometimes denoted theta or alpha0.
   * 
   * Higher values tends to create more table.
   */
  public double alpha0()
  {
    return alpha0.getValue();
  }


  @Override
  public double logDensity() 
  {
	  if (!inBounds())
		  return Double.NEGATIVE_INFINITY;

	  // TODO: test this (check that it's correct)
	  double logDensity = CRPs.crpAssignmentLogProbabilitiy(this, crpState);
	  return logDensity;
  }
}
