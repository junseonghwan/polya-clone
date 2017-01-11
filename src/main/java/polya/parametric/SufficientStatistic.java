package polya.parametric;


/**
 * Additive sufficient statistic for a  parametric model.
 * 
 * Note: to see implementations of this interface in eclipse,
 * right click on the name of the class, and pick 
 * ``Open Type Hierarchy``
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public interface SufficientStatistic
{
  /**
   * Warning: modifies this object in place
   * 
   * Add the contents of another sufficient statistics to this
   * @param other
   */
  public void plusEqual(SufficientStatistic other);
  
  /**
   * Warning: modifies this object in place
   * 
   * Subtract the contents of another sufficient statistics to this
   * @param other
   */
  public void minusEqual(SufficientStatistic other);
  
  /**
   * @return The number of points this object is summarizing. (Different than dim())
   */
  public int numberOfPoints();
  
  /**
   * 
   * @return A deep clone of this object
   */
  public SufficientStatistic copy();
}
