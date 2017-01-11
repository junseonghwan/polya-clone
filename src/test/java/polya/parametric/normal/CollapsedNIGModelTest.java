package polya.parametric.normal;

import java.util.Random;

import org.junit.Test;

import polya.ParametricsTutorial;
import polya.parametric.HyperParameter;
import polya.parametric.TestedModel;

public class CollapsedNIGModelTest 
{

	@Test
	public void runTests()
	{
	    Random rand = new Random(1);
	    
	    double mu0 = 1.0;
	    double nu = 1.0;
	    double alpha = 10.0;
	    double beta = 1.0;

	    HyperParameter hp = new NIGHyperParameter(mu0, nu, alpha, beta);
	    TestedModel model = new CollapsedNIGModel(hp);
	    ParametricsTutorial.testParametricModel(rand, model, hp);
	    System.out.println("-------");

	}
}
