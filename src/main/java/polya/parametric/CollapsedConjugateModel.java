package polya.parametric;

import blang.factors.Factor;



/**
 * A CollapsedConjugateModel is a model that admits tractable 
 * collapsed inference. 
 * 
 * The model conceptually contains three variable:
 * - Hyperparameter variable (deterministic)
 * - Parameter (random; the part that is collapsed, i.e. marginalized)
 * - Observation(s) (random and conditioned on; the dataset is 
 * stored in SufficientStatistic objects)
 * 
 * The main use of this interface is that given an implementation of
 * - logPriorDensityAtThetaStar
 * - logLikelihoodGivenThetaStar
 * - update
 * it is then easy to compute the marginal probability of a dataset
 * using Parametrics.logMarginal()
 * 
 * Throughout this class, ThetaStar is an arbitrary Parameter realization.
 * (See Equation (12,13) in
 * http://www.stat.ubc.ca/~bouchard/courses/stat547-sp2013-14/lecture/2014/01/12/notes-lecture3.html)
 * 
 * @author Alexandre Bouchard (alexandre.bouchard@gmail.com)
 *
 */
public interface CollapsedConjugateModel
{
  /**
   * See p\_hp(theta*) Equation (12,13) in
   * http://www.stat.ubc.ca/~bouchard/courses/stat547-sp2013-14/lecture/2014/01/12/notes-lecture3.html
   * @param hp
   * @return
   */
  public double logPriorDensityAtThetaStar(HyperParameter hp);
  
  /**
   * See l(x|z) Equation (12,13) in
   * http://www.stat.ubc.ca/~bouchard/courses/stat547-sp2013-14/lecture/2014/01/12/notes-lecture3.html
   * @param data
   * @return
   */
  public double logLikelihoodGivenThetaStar(SufficientStatistic data);
  
  /**
   * See u(data, before) in Equation (12,13) in
   * http://www.stat.ubc.ca/~bouchard/courses/stat547-sp2013-14/lecture/2014/01/12/notes-lecture3.html
   * @param before
   * @param data
   * @return
   */
  public HyperParameter update(HyperParameter before, SufficientStatistic data);

  /** 
   * The following changes are made to the original implementation to reflect Figure 1 of the paper: 
   * 
   * 1. This interface extends Factor interface so as to make all CollapsedConjugateModel into factors
   * The current use of CollapsedConjugateModel is in the context of DPMM, in which case CollapsedConjugateModel
   * shows up as a factor
   * 
   * 2. CollapsedConjugateModel should be pointing to hyper parameters represented as 
   * factor component/argument, which may be sampled if desired
   * 
   */
  
  public HyperParameter getHyperParameter();
  public boolean checkHyperParameterBound();
}
