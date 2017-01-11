package polya;

import java.io.File;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import polya.crp.CRPState;
import polya.parametric.normal.NIWs;
import tutorialj.Tutorial;

import com.beust.jcommander.internal.Sets;


/**
 * Tutorial and test cases for the CRP datastructure.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 */
public class CRPStateTutorial
{
  
  /**
   * ``CRPState`` is exactly like the version we worked on 
   * during Lab 3, except for a few minor modifications:
   * 
   * 1. Instead of denoting each cluster with an ``Integer``, we 
   * use ``ClusterId`` instead. This is to avoid potential errors
   * of mixing up customer id (which are still integers) and cluster id.
   * In other words such error
   * will now be detected at compile time instead of running time.
   * This is generally a good principle: trying to discover problems as 
   * early as possible (fail fast).
   * 2. CRPState now has an extra responsibility: keeping sufficient 
   * statistics for each table (cluster).
   * 
   * You will have one task to perform in order to make the first test case work.
   * To run the test case, right click on the class in the left-hand panel,
   * here under ``src/test/java/polya/CRPStateTutorial``, select ``Run as``, 
   * and pick ``JUnit test``. You should see a red flag until you make 
   * the change described below work, in which case the flag will become green.
   * Such test case can be created by simply adding the ``@Test`` 
   * flag above the function you want to test.
   */
  @Tutorial(nextStep = CRPState.class, showSource = false)
  @Test
  public void tests()
  {
    CRPState state = new CRPState(NIWs.loadFromCSVFile(new File("data/tiny-data.csv")));
    
    state.checkIntegrity();
    state.addCustomerToNewTable(0);
    state.checkIntegrity();
    state.addCustomerToExistingTable(1, state.getClusterIdOfCustomer(0));
    state.checkIntegrity();
    // This means that the flag will go red if these two are not equal
    Assert.assertEquals(state.partition(), part(block(0,1)));
    
    state.addCustomerToNewTable(2);
    state.checkIntegrity();
    Assert.assertEquals(state.nTables(), 2);
    Assert.assertEquals(state.nCustomers(), 3);
    state.removeCustomer(2);
    state.checkIntegrity();
    Assert.assertEquals(state.nTables(), 1);
    Assert.assertEquals(state.nCustomers(), 2);
    state.removeCustomer(1);
    state.checkIntegrity();
    state.addCustomerToExistingTable(3, state.getClusterIdOfCustomer(0));
    Assert.assertEquals(state.partition(), part(block(0,3)));    
    state.checkIntegrity();
  }
  
  private static Set<Set<Integer>> part(@SuppressWarnings("rawtypes") Set ... blocks)
  {
    Set<Set<Integer>> result = Sets.newHashSet();
    for (Set<Integer> block : blocks)
      result.add(block);
    return result;
  }
  
  private static Set<Integer> block(int ... numbers )
  {
    Set<Integer> result = Sets.newHashSet();
    for (int number : numbers)
      result.add(number);
    return result;
  }
}
