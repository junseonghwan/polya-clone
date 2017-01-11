package polya.ddcrp;

import java.util.Set;

import polya.crp.utils.ClusterId;
import blang.processing.NodeProcessor;
import blang.processing.ProcessorContext;

public class DDCRPProcessor implements NodeProcessor<DDCRPStateInterface>
{
	public static final int MIN_CLUSTER_SIZE_THREHOLD = 5;
	private DDCRPStateInterface variable;

	@Override
  public void process(ProcessorContext context) {
		if (context.isLastProcessCall()) {
			Set<Customer> customers = variable.getAllCustomers();
			
			int count = 0;
			for (ClusterId clId : variable.getAllClusterIds()) {
				if (variable.getTable(clId).size() > MIN_CLUSTER_SIZE_THREHOLD) {
					count++;
				}
			}
			System.out.println("Num Clusters=" + variable.getAllClusterIds().size());
			System.out.println("Num Clusters (size > " + MIN_CLUSTER_SIZE_THREHOLD + ")=" + count);
			System.out.println("Num Customers=" + customers.size());
		}
  }

	@Override
  public void setReference(DDCRPStateInterface variable) {
	  this.variable = variable;
  }

}
