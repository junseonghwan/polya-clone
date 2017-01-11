package polya.parametric.normal;

import java.util.Random;

import org.junit.Test;

import polya.ParametricsTutorial;
import polya.parametric.HyperParameter;
import polya.parametric.TestedModel;

/**
 * This class tests the implementation of the case of Normal likelihood with known variance,
 * unknown mean implemented in CollapsedNormalNormal.java
 *  
 * @author Seong-Hwan Jun (s2jun.uw@gmail.com)
 *
 */
public class CollapsedNormalNormalModelTest 
{

	@Test
	public void runTests()
	{
	    Random rand = new Random(1);
	    
	    double var = 10.0;
	    HyperParameter hp = new NormalParameter(0.0, 1.0);
	    TestedModel model = new CollapsedNormalNormalModel(var, hp);
	    ParametricsTutorial.testParametricModel(rand, model, hp);
	    System.out.println("-------");

	}
}
