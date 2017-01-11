package polya.ddcrp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math3.util.Pair;

import polya.crp.utils.ClusterId;
import polya.parametric.SufficientStatistic;
import blang.annotations.Processors;
import blang.annotations.Samplers;

@Samplers({DDCRP2DImageStateSampler.class})
@Processors({DDCRPProcessor.class})
public class DDCRP2DImageState implements DDCRPStateInterface
{
	private Map<Customer,SufficientStatistic> customer2Statistic;
  private Map<ClusterId, SufficientStatistic> cluster2Statistic = new HashMap<ClusterId, SufficientStatistic>();

	private Map<ClusterId, Set<Customer>> cluster2Customers = new HashMap<ClusterId, Set<Customer>>();
	private Map<Customer, ClusterId> customer2Cluster = new HashMap<>();
	
	private List<CustomerColumn> customerArray;;
	
	public DDCRP2DImageState(Map<Customer,SufficientStatistic> customer2Statistic, List<CustomerColumn> customerArray) {
		this.customer2Statistic = customer2Statistic;
    for (Customer customer : customer2Statistic.keySet())
      addCustomerToNewCluster(customer);
    
    this.customerArray = customerArray;
	}

	public static DDCRP2DImageState fullyDisconnectedClustering(Map<Customer,SufficientStatistic> customer2Statistic, List<CustomerColumn> customerArray) {
    return new DDCRP2DImageState(customer2Statistic, customerArray);
	}
	
  private void checkCustomerNotAlreadyThere(Customer customer)
  {
    if (customer2Cluster.containsKey(customer))
      throw new RuntimeException("Customer already in there. Remove the customer first.");
  }

	public ClusterId addCustomerToNewCluster(Customer customer) {
		checkCustomerNotAlreadyThere(customer);

    Set<Customer> newCluster = new HashSet<Customer>();
    newCluster.add(customer);

    ClusterId clusterId = getNextClusterId();
    cluster2Customers.put(clusterId, newCluster);
    customer2Cluster.put(customer, clusterId);
    // note that we make a copy in this case because we want customer2Statistic
    // to stay unchanged
    cluster2Statistic.put(clusterId, customer2Statistic.get(customer).copy());

    return clusterId;
	}
	
	/*
  public void addCustomerToExistingCluster(Customer customer, ClusterId clusterId)
  {
    checkCustomerNotAlreadyThere(customer);
    
    cluster2Customers.get(clusterId).add(customer);
    customer2Cluster.put(customer, clusterId);
    cluster2Statistic.get(clusterId).plusEqual(customer2Statistic.get(customer));
  }
  */
  
	public void updateCustomerLink(Customer customer, Customer link) {
		customer.pointer = link;
		ClusterId customerClusterId = customer2Cluster.get(customer);
		ClusterId linkClusterId = customer2Cluster.get(link);
		if (customerClusterId.equals(linkClusterId)) {
			// no merging of the tables so nothing to update except for the customer link
		} else {
			// merging, so we need to update bunch of things
			Set<Customer> customerTable = cluster2Customers.get(customerClusterId);
			Set<Customer> linkTable = cluster2Customers.get(linkClusterId);
			
			// update the customerTable membership
			customerTable.addAll(linkTable);
			// update the sufficient stat
			cluster2Statistic.get(customerClusterId).plusEqual(cluster2Statistic.get(linkClusterId));
			// update the cluster information for the link table
			for (Customer cc : linkTable) {
				customer2Cluster.put(cc, customerClusterId);
			}

			// remove the linkTable from all of the data structures
      cluster2Statistic.remove(linkClusterId);
      cluster2Customers.remove(linkClusterId);
			_lastRemoved.add(linkClusterId);
		}
	}
  
	public ClusterId removeCustomerLink(Customer customer) {
		
		// removing customer from its current table can 
		// 1. eliminate the table if it is the last customer sitting at the table
		// 2. split the table into two
		// 3. leave the table as is
		// case 2 is a bit difficult to handle -- the others are easy
		
    ClusterId customerClusterId = customer2Cluster.get(customer);
    Set<Customer> table = cluster2Customers.get(customerClusterId);
    int tableSize = table.size();
    
    if (tableSize == 1) {
      // if the customer was the last member of the table, just return the clusterId (nothing to be done, which is different from the CRP)
    	// the table gets removed when it merges with another table 
    } else {

      // otherwise, there is a possibility of a split
  
      Set<Customer> customerTable = new HashSet<>();
      Set<Customer> otherTable = new HashSet<>();
  
      // first determine if the table will be split into two -- this can be determined by following the next pointer
      // if it reaches the customer, then there is an alternative path if not, then there is a split
      Customer next = customer.pointer;
      while (!otherTable.contains(next)) {
      	otherTable.add(next);
      	//table.remove(next);
      	next = next.pointer;
      }
      
      if (!otherTable.contains(customer)) {
      	// split!
      	
      	// update the customer's table
        cluster2Customers.put(customerClusterId, customerTable);
        // assign the clusterId to the splitted table
        ClusterId newClusterId = getNextClusterId();
        cluster2Customers.put(newClusterId, otherTable);
        
      	// splits the table into two
        customerTable.add(customer);
        for (Customer cc : table) {
      		if (otherTable.contains(cc) || customerTable.contains(cc))
      			continue;
  
      		Pair<Boolean, List<Customer>> ret  = cc.pointsToCustomerTable(customerTable, otherTable);
      		if (ret.getFirst()) {
      			customerTable.addAll(ret.getSecond());
      		} else {
      			otherTable.addAll(ret.getSecond());
      		}
      	}
        
        if ((otherTable.size() + customerTable.size()) != tableSize)
        	throw new RuntimeException("Bug.");
  
        recomputeSufficientStat(customerTable, customerClusterId);
        recomputeSufficientStat(otherTable, newClusterId);
      }
    }
    
    return customerClusterId;
	}
	
	private void recomputeSufficientStat(Set<Customer> customers, ClusterId clusterId) {
		
    cluster2Statistic.put(clusterId, null);

		for (Customer cc : customers) {
			customer2Cluster.put(cc, clusterId);
			if (cluster2Statistic.get(clusterId) == null) {
		    cluster2Statistic.put(clusterId, customer2Statistic.get(cc).copy());
			} else {
				cluster2Statistic.get(clusterId).plusEqual(customer2Statistic.get(cc));
			}
		}
	}

  public int nTables()
  {
    return cluster2Customers.size();
  }

  public List<ClusterId> getAllClusterIds()
  {
    List<ClusterId> result = new ArrayList<ClusterId>(cluster2Customers.keySet());
    Collections.sort(result);
    return result;
  }
  
  public ClusterId getClusterId(Customer customer) {
  	return customer2Cluster.get(customer);
  }

  public SufficientStatistic getCustomerStatistic(Customer customer)
  {
    return customer2Statistic.get(customer);
  }
  
  public SufficientStatistic getClusterStatistics(ClusterId current)
  {
    return cluster2Statistic.get(current);
  }

  public Set<Customer> getTable(ClusterId clusterId)
  {
    return cluster2Customers.get(clusterId);
  }

  public Set<Customer> getNeighbors(Customer customer) 
  {
  	Set<Customer> neighbors = new HashSet<>();

  	CustomerColumn currColumn = customerArray.get(customer.x);
  	if (currColumn == null) throw new RuntimeException("Data processing bug.");
		
		addNeighbor(customer, -1, neighbors);
		addNeighbor(customer, +1, neighbors);
		addNeighbor(customer, 0, neighbors);
		
  	return neighbors;
  }
  
  private void addNeighbor(Customer customer, int a, Set<Customer> neighbors) {
  	try {
  		TreeSet<Customer> set = new TreeSet<>();
  		CustomerColumn neighborColumn = customerArray.get(customer.x - a);
  		set.addAll(neighborColumn.getCustomers());
  		Customer higher = set.higher(customer);
  		Customer lower = set.lower(customer);
  		if (higher != null)
  			neighbors.add(higher);
  		if (lower != null)
  			neighbors.add(lower);
  	} catch (Exception ex) {
  		// array bounds exception -- can be ignored
  	}
  }
  
  /*
  int [][] a = new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, +1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
  public Set<Customer> getNeighbors(Customer customer) 
  {
  	Set<Customer> neighbors = new HashSet<>();
  	for (int j = 0; j < a.length; j++) {
  		try {
  			Customer neighbor = customerArray[customer.y + a[j][0]][customer.x + a[j][1]];
  			if (neighbor != null)
  				neighbors.add(neighbor);
  		} catch (ArrayIndexOutOfBoundsException ex) {
  			
  		}
  	}
  	return neighbors;
  }
  */
  
  private int _nextClusterId = 0;
  private List<ClusterId> _lastRemoved = new ArrayList<>();
  
  /**
   * Return a cluster id not in use.
   * @return
   */
  private ClusterId getNextClusterId()
  {
    if (_lastRemoved.size() > 0)
    {
      ClusterId result = _lastRemoved.remove(0);
      return result;
    }
    if (_nextClusterId == Integer.MAX_VALUE)
      throw new RuntimeException("Ids exhausted.");
    return new ClusterId(_nextClusterId++);
  }

	public Set<Customer> getAllCustomers() {
		return customer2Statistic.keySet();
	}
}
