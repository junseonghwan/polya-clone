package polya.crp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import blang.annotations.Processors;
import blang.annotations.Samplers;
import polya.crp.utils.ClusterId;
import polya.parametric.SufficientStatistic;
import tutorialj.Tutorial;

/**
 * The seating arrangement data structure required by the Chinese Restaurant Process.
 * 
 * http://www.stat.ubc.ca/~bouchard/courses/stat547-sp2013-14/lecture/2014/01/02/notes-lecture2.html
 * 
 * Simple Hash-based maps between customers and tables.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
@Samplers({CRPSampler.class})
@Processors({CRPProcessor.class})
public class CRPState
{
  
  /**
   * #### Method to implement: CRPState.removeCustomer
   * 
   * ``CRPState.removeCustomer()`` is the first function you should fill in.
   * You can base what you write on your work from the lab, but make
   * sure you also update ``cluster2Statistic``.
   * 
   * You can get some information on how to do this by looking
   * at how I modified ``addCustomerToNewTable()`` and 
   * ``addCustomerToExistingTable()``. See also the class
   * ``SufficientStatistic`` (to open a class, go in the menu
   * ``Navigate`` then ``Open Type`` and just type ``SufficientStatistic``).
   * 
   * Recall that ``removeCustomer()`` should behave as follows: it should remove 
   * one customer, destroying the table if the customer was the last. The method
   * should throw a RuntimeException if customer was not in restaurant.
   */
  @Tutorial(showSource = false, showLink = true)
  public void removeCustomer(Integer customer)
  {
    /* startRem throw new RuntimeException(); */
    Set<Integer> table = getTableOfCustomer(customer);

    boolean deleteSuccessful = table.remove(customer);
    if (!deleteSuccessful)
      throw new RuntimeException("Customer " + customer + " was not in the CRPState");
    
    ClusterId clusterId = customer2Cluster.get(customer);
    if (table.isEmpty())
    {
      cluster2Statistic.remove(clusterId);
      cluster2Customers.remove(clusterId);
      _lastRemoved = clusterId;
    }
    else
      cluster2Statistic.get(clusterId).minusEqual(customer2Statistic.get(customer));
    
    customer2Cluster.remove(customer);
    /* endRem */
  }
  
  
  /**
   * Maps tables to the data points assigned to that table
   */
  private Map<ClusterId,Set<Integer>> cluster2Customers = new HashMap<ClusterId,Set<Integer>>();
  
  /**
   * Inverse of cluster2Customers
   */
  private Map<Integer,ClusterId> customer2Cluster = new HashMap<Integer, ClusterId>();
  
  /**
   * For each table, keeps sufficient statistics for the data at that table
   */
  private Map<ClusterId, SufficientStatistic> cluster2Statistic = new HashMap<ClusterId, SufficientStatistic>();
  
  /**
   * Sufficient statistics of individual table, to quickly add or remove customers
   * from tables
   */
  private final Map<Integer, SufficientStatistic> customer2Statistic;
  
  /**
   * Creates a new CRPState where each customer sits at that table. This is
   * the standard recommended initialization for CRP-based samplers.
   * 
   * @param customer2Statistic
   * @return
   */
  public static CRPState fullyDisconnectedClustering(Map<Integer,SufficientStatistic> customer2Statistic)
  {
    CRPState result = new CRPState(customer2Statistic);
    for (Integer customer : customer2Statistic.keySet())
      result.addCustomerToNewTable(customer);
    return result;
  }
  
  /**
   * 
   * @param customer2Statistic
   */
  public CRPState(Map<Integer,SufficientStatistic> customer2Statistic)
  {
    this.customer2Statistic = customer2Statistic;
  }
  
  /**
   * 
   * @param customer
   * @return The ClusterId of the table on which the customer currently sits at
   */
  public ClusterId getClusterIdOfCustomer(Integer customer)
  {
    return customer2Cluster.get(customer);
  }
  
  /**
   * 
   * @param customer
   * @return The set of customer indices sitting with the provided customer
   */
  public Set<Integer> getTableOfCustomer(Integer customer)
  {
    ClusterId clusterId = customer2Cluster.get(customer);
    return cluster2Customers.get(clusterId);
  }
  
  public Set<Integer> getTable(ClusterId clusterId)
  {
    return cluster2Customers.get(clusterId);
  }
  
  /**
   * 
   * @return The id of all non-empty tables
   */
  public List<ClusterId> getAllClusterIds()
  {
    List<ClusterId> result = new ArrayList<ClusterId>(cluster2Customers.keySet());
    Collections.sort(result);
    return result;
  }
  
  /**
   * 
   * @param customer A customer not currently in restaurant
   * @param clusterId The existing, non-empty cluster to join
   * 
   * @throws RuntimeException If customer was already in restaurant
   */
  public void addCustomerToExistingTable(Integer customer, ClusterId clusterId)
  {
    checkCustomerNotAlreadyThere(customer);
    
    cluster2Customers.get(clusterId).add(customer);
    customer2Cluster.put(customer, clusterId);
    cluster2Statistic.get(clusterId).plusEqual(customer2Statistic.get(customer));
  }
  
  /**
   * Create a new table containing only the provided customer
   * 
   * @param customer A customer not currently in restaurant
   * @return clusterId The ClusterId of the newly created table
   * 
   * @throws RuntimeException If customer was already in restaurant
   */
  public ClusterId addCustomerToNewTable(Integer customer)
  {
    checkCustomerNotAlreadyThere(customer);
    
    Set<Integer> newTable = new HashSet<Integer>();
    newTable.add(customer);
    
    ClusterId clusterId = getNextClusterId();
    cluster2Customers.put(clusterId, newTable);
    customer2Cluster.put(customer, clusterId);
    // note that we make a copy in this case because we want customer2Statistic
    // to stay unchanged
    cluster2Statistic.put(clusterId, customer2Statistic.get(customer).copy());
    
    return clusterId;
  }
  
  /**
   * 
   * @return Total number of customers in restaurant.
   */
  public int nCustomers() 
  {
    return customer2Cluster.size();
  }
  
  /**
   * Makes some basic integrity checks
   */
  public void checkIntegrity()
  {
    int check = 0;
    for (ClusterId id : cluster2Statistic.keySet())
    {
      SufficientStatistic current = cluster2Statistic.get(id);
      int curNCust = current.numberOfPoints();
      if (curNCust != cluster2Customers.get(id).size())
        throw new RuntimeException();
      check += curNCust;
    }
    if (check != nCustomers())
      throw new RuntimeException();
  }
  
  /**
   * 
   * @return Total number of tables with at least one customer on it
   */
  public int nTables()
  {
    return cluster2Customers.size();
  }


  
  /**
   * 
   * @return Set of blocks, where each block is as set of customers at the same table
   */
  public Set<Set<Integer>> partition()
  {
    return new HashSet<Set<Integer>>(cluster2Customers.values());
  }
  
  private void checkCustomerNotAlreadyThere(Integer customer)
  {
    if (customer2Cluster.containsKey(customer))
      throw new RuntimeException("Customer already in there. Remove the customer first.");
  }
  
  private int _nextClusterId = 0;
  private ClusterId _lastRemoved = null;
  
  /**
   * Return a cluster id not in use.
   * @return
   */
  private ClusterId getNextClusterId()
  {
    if (_lastRemoved != null)
    {
      ClusterId result = _lastRemoved;
      _lastRemoved = null;
      return result;
    }
    if (_nextClusterId == Integer.MAX_VALUE)
      throw new RuntimeException("Ids exhausted.");
    return new ClusterId(_nextClusterId++);
  }

  /**
   * 
   * @param customer
   * @return The statistic of a single customer
   */
  public SufficientStatistic getCustomerStatistic(Integer customer)
  {
    return customer2Statistic.get(customer);
  }

  /**
   * 
   * @param current
   * @return The statistic of all the customers at a table
   */
  public SufficientStatistic getClusterStatistics(ClusterId current)
  {
    return cluster2Statistic.get(current);
  }

  /**
   * All the customers currently sitting,
   * across all tables in the restaurant.
   *  
   * Note: could potentially be different
   *  than the keyset of customer2Statistic,
   *  which is the set of all possible customers,
   *  whereas they have been sited or not. 
   * 
   * @return All the customers currently sitting,
   *  across all tables. 
   */
  public Set<Integer> getAllCustomers()
  {
    return customer2Cluster.keySet();
  }

}
