package polya.ddcrp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.util.Pair;

public class Customer implements Comparable<Customer>
{
	public int id;
	public int x, y;
	public Customer pointer;
	public double datum;
	public int clusterId;
	
	public Customer(int id, int x, int y, double datum) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.datum = datum;
		this.pointer = this; // initialized to be the self pointer
	}
	
	@Override
	public int hashCode() {
		return id;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		return false;
	}

  public Pair<Boolean, List<Customer>> pointsToCustomerTable(Set<Customer> customerTable, Set<Customer> otherTable) {
  	Customer next = this;
  	List<Customer> visitedCustomers = new ArrayList<Customer>();
  	while (true) {
  		if (customerTable.contains(next))
  			return Pair.create(true, visitedCustomers);
  		else if (otherTable.contains(next))
  			break;
  		visitedCustomers.add(next);
  		next = next.pointer;
  	}
  	return Pair.create(false, visitedCustomers);
  }
	
  @Override
  public String toString() {
  	return ("(" + x + ", " + y + ")");
  }

	@Override
  public int compareTo(Customer o2) {
	  if (this.x < o2.x) {
	  	return -1;
	  } else if (this.x > o2.x) {
	  	return 1;
	  } else {
	  	if (this.y < o2.y){
	  		return -1;
	  	} else if (this.y > o2.y) {
	  		return 1;
	  	} else {
	  		return 0;
	  	}
	  }
  }

}
