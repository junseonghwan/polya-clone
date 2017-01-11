package polya.ddcrp;

import java.util.ArrayList;
import java.util.List;

public class CustomerColumn {
	
	private List<Customer> sortedCustomerColumn = new ArrayList<Customer>();
	public double x;
	
	public CustomerColumn(double x) {
		this.x = x;
	}

	public void addCustomer(Customer customer) {
		sortedCustomerColumn.add(customer);
	}
	public List<Customer> getCustomers() { return sortedCustomerColumn; }
}
