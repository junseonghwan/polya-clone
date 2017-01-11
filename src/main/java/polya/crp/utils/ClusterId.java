package polya.crp.utils;




/**
 * 
 * An id for a table/cluster
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public class ClusterId implements Comparable<ClusterId>
{
  private final int id;


  public ClusterId(int id)
  {
    this.id = id;
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + id;
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ClusterId other = (ClusterId) obj;
    if (id != other.id)
      return false;
    return true;
  }

  @Override
  public String toString()
  {
    return "ClusterId(" + id + ")";
  }

  @Override
  public int compareTo(ClusterId o)
  {
    return new Integer(this.id).compareTo(o.id);
  }
}