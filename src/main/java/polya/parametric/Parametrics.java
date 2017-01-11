package polya.parametric;

import polya.parametric.normal.CollapsedNIWModel;
import tutorialj.Tutorial;




/**
 * Utilities common to all conjugate models.
 * 
 * NOTE: if commone to all conjugate models, shouldn't this go under the CollapsedConjugateModel.java?
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 */
public class Parametrics
{
  /**
   * ### Parametrics: Utilities common to all conjugate models.
   * 
   * #### Function to implement: logMarginal
   * 
   * You should fill in this function so that it computes p\_hp(data), 
   * marginalizing over parameters. See Equation (12,13) in
   * http://www.stat.ubc.ca/~bouchard/courses/stat547-sp2013-14/lecture/2014/01/12/notes-lecture3.html
   * 
   */
  @Tutorial(showSource = false, showLink = true)
  public static double logMarginal(
      CollapsedConjugateModel model,
      HyperParameter hp,
      SufficientStatistic data)
  {
    /* startRem throw new RuntimeException(); */ 
    final double logPrior = model.logPriorDensityAtThetaStar(hp);
    final double logLikelihood = model.logLikelihoodGivenThetaStar(data);
    final HyperParameter updated = model.update(hp, data);
    final double logPosterior = model.logPriorDensityAtThetaStar(updated);
    return logPrior + logLikelihood - logPosterior;
    /* endRem */
  }
  
  /**
   * #### Function to implement: logPredictive
   * 
   * You should fill in this function so that it computes p\_hp(newPoints|oldPoints).
   */
  @Tutorial(showSource = false, showLink = true, nextStep = CollapsedNIWModel.class)
  public static double logPredictive(
      CollapsedConjugateModel model,
      HyperParameter hp, 
      SufficientStatistic newPoints, 
      SufficientStatistic oldPoints)
  {
    /* startRem throw new RuntimeException(); */ 
    HyperParameter updated = model.update(hp, oldPoints);
    return logMarginal(model, updated, newPoints);
    /* endRem */
  }

}
