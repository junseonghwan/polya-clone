package polya.ddcrp;

import java.util.List;
import java.util.Set;

import polya.crp.utils.ClusterId;
import polya.parametric.SufficientStatistic;

public interface DDCRPStateInterface {

  public List<ClusterId> getAllClusterIds();
  public SufficientStatistic getClusterStatistics(ClusterId current);
	public Set<Customer> getAllCustomers();
	public Set<Customer> getTable(ClusterId clusterId);
}
