package polya;

import java.io.File;
import java.util.Locale;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Test;

import polya.parametric.HyperParameter;
import polya.parametric.Parameter;
import polya.parametric.Parametrics;
import polya.parametric.SufficientStatistic;
import polya.parametric.TestedModel;
import polya.parametric.normal.CollapsedNIWModel;
import polya.parametric.normal.NIWHyperParameter;
import polya.parametric.normal.MVNParameter;
import polya.parametric.normal.NIWs;
import bayonet.math.EJMLUtils;
import bayonet.rplot.PlotContour;

import tutorialj.Tutorial;


/**
 * Tests and tutorials for the Bayesian parametric part of the code base.
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 */
public class ParametricsTutorial
{
  /**
   * For the next test case to run, you will need to implement 
   * the NIW conjugacy machinery. The main things to do will
   * be in ``Parametrics``, which contains some behaviors that 
   * applies to all conjugate models, and in ``CollapsedNIWModel``,
   * which contains behaviors specific to NIW.
   */
  @Tutorial(showSource = false, nextStep = Parametrics.class)
  @Test
  public void runTests()
  {
    // Accuracy of reconstructions on simulated data
    Locale.setDefault(Locale.US);
    Random rand = new Random(1);
    NIWHyperParameter hp = NIWHyperParameter.withDimensionality(2);
    TestedModel model = new CollapsedNIWModel(hp);
    testParametricModel(rand, model, hp);
    System.out.println("-------");
    
    // Visualization of the predictive
    visualize(rand, hp);
  }
  
  /**
   * ### Test cases
   * 
   * #### Expected results of the first half of the test case.
   * 
   * After you implement the above mentioned functions, in the first test you
   * should see the average distance between the inferred (MAP) parameters and
   * the generated true ones decrease as the size of the generated dataset increases.
   * It should get down to a distance of  about 3 (Note that these distances are
   * fairly large because they are max norms, and the hyperparameters are picked
   * such that the distribution on parameters is vague (more specifically, 
   * nu = dim, which makes the expectation of the NIW not finite (see wiki
   * acticle on NIW for detail))).
   */
  @Tutorial(showSource = false, showLink = true, linkPrefix = "src/test/java/")
  public static void testParametricModel(Random rand, TestedModel model, HyperParameter initialHP)
  {
    // generate datasets of increasing size
    for (int datasetSize = 10; datasetSize < 1000000; datasetSize *= 10)
    {
      // compute average reconstruction error for datasets of this size:
      SummaryStatistics distanceStatistics = new SummaryStatistics();
      for (int nReplicates = 0; nReplicates < 100; nReplicates++)
      {
        Pair<Parameter, SufficientStatistic> generatedData = model.generateData(rand, initialHP, datasetSize);
        Parameter trueParam = generatedData.getLeft();
        SufficientStatistic data = generatedData.getRight();
        HyperParameter updatedHP = model.update(initialHP, data);
        Parameter map = model.maximumAPosteriori(updatedHP);
        distanceStatistics.addValue(model.distance(trueParam, map));
      }
      System.out.println(
          "dataSize=" + datasetSize + ", " +
          "distanceMean=" + distanceStatistics.getMean() + ", " +
          "distanceSD=" + distanceStatistics.getStandardDeviation());
    }
  }
  
  /**
   * #### Second half of the test case
   * 
   * In the second half of the test case, we use a similar data generation
   * strategy as in the first half, but this time we plot the predictive distribution
   * in the folder ``parametricResults``. The objective is 
   * to get more intuition on the NIW model. The true mean and covariance are 
   * also printed to be able to assess visually if the system is doing something
   * reasonable.
   */
  @Tutorial(showSource = false, showLink = true, linkPrefix = "src/test/java/")
  public static void visualize(Random rand, NIWHyperParameter initialHP)
  {
    File parametricResultsFolder = new File("parametricResults"); 
    parametricResultsFolder.mkdir();
    TestedModel model = new CollapsedNIWModel(initialHP);
    for (int datasetSize = 10; datasetSize < 1000000; datasetSize *= 10)
    {
      for (int nReplicates = 0; nReplicates < 3; nReplicates++)
      {
        Pair<Parameter, SufficientStatistic> generatedData = 
          model.generateData(rand, initialHP, datasetSize);
        MVNParameter trueParam = (MVNParameter) generatedData.getLeft();
        SufficientStatistic data = generatedData.getRight();
        HyperParameter updatedHP = model.update(initialHP, data);
        NIWHyperParameter nhp = (NIWHyperParameter) updatedHP;
        System.out.println("size=" + datasetSize + ",rep=" + nReplicates);
        System.out.println("trueMean=\n" + EJMLUtils.toString(trueParam.getMeanParameter()));
        System.out.println("trueCovar=\n" + EJMLUtils.toString(trueParam.getCovarianceParameter()));
        
        PlotContour pc = PlotContour.fromFunction(NIWs.logMarginalAsFunctionOfData(nhp));
        pc.centerToZero(100);
        
        pc.toPDF(new File(parametricResultsFolder, "size=" + datasetSize + ",rep=" + nReplicates + ".pdf"));
        System.out.println("---");
      }
    }
  }

}
