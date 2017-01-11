package polya;

import polya.crp.CRPSampler;
import tutorialj.Tutorial;



public class PolyaTutorial
{
  /**
   * # polya
   * 
   * Installing from source
   * ----------------------
   * 
   * Requires: gradle, git, eclipse
   * 
   * - Clone the repository
   * - Type ``gradle eclipse`` from the root of the repository
   * - From eclipse:
   *   - ``Import`` in ``File`` menu
   *   - ``Import existing projects into workspace``
   *   - Select the root of the newly created repo
   *   - Deselect ``Copy projects into workspace`` to avoid having duplicates
   * 
   */
  @Tutorial(startTutorial = "README.md", showSource = false)
  public void usage() {}
  
  
  /**
   * CRP State
   * ---------
   */
  @Tutorial(showSource = false, nextStep = CRPStateTutorial.class)
  public void crpState()
  {
    
  }
  
  /**
   * Parametric machinery
   * --------------------
   * 
   * Each cluster in a DP has its own mini parametric model. You
   * will develop this mini parametric model in the case of a
   * Normal-inverse-Wishart prior coupled with a Normal likelihood,
   * and perhaps others if time permits.
   */
  @Tutorial(showSource = false, nextStep = ParametricsTutorial.class)
  public void parametrics() {}
  
  
  /**
   * Gibbs sampling of the customers
   * -------------------------------
   * 
   * We are now ready to combine the parametric machinery with you CRPState
   * implementation into a collapsed Gibbs sampler of the seating 
   * arrangements.
   */
  @Tutorial(showSource = false, nextStep = CRPSampler.class)
  public void gibbs() {}
  
  /**
   * Glossary and abbreviations
   * --------------------------
   * 
   * General:
   * 
   * - Class with a plural name/ending in s (Maps, Parametrics, etc): 
   *   Contains static functions, usually utilities.
   * 
   * Specific to this project:
   * 
   * - diagDelta: One of the NIW hyper-parameters. See NIWHyperParameters
   * - hp: Abbreviation used for HyperParameters
   * - kappa: One of the NIW hyper-parameters. See NIWHyperParameters
   * - MAP: Maximum A Posteriori. See TestedModel
   * - MVN: MultiVariate Normal.
   * - NIW: Normal Inverse Wishart. A conjugate prior for mean and var of a MVN.
   * - nuPrime: One of the NIW hyper-parameters. See NIWHyperParameters
   * - PY: Pitman-Yor process
   */
  @Tutorial(showSource = false)
  public void glossary()
  {
  }
  
}
