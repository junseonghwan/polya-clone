package polya.crp;

import polya.crp.utils.ClusterId;
import polya.parametric.CollapsedConjugateModel;
import polya.parametric.Parametrics;
import blang.annotations.FactorArgument;
import blang.annotations.FactorComponent;
import blang.factors.Factor;

/**
 * A (root) composite factor that holds CollapsedConjugateModel as a factor component and
 * a CRPState as a variable
 * @author Seong-Hwan Jun (s2jun.uw@gmail.com)
 *
 */
public class CRPFactor implements Factor
{
	@FactorComponent
	public final CollapsedConjugateModel collapsedModel;

	@FactorArgument//(makeStochastic=true) makeStochastic for the data CRPState
	public final CRPState clustering;

	public CRPFactor(CollapsedConjugateModel collapsedModel, CRPState crpState)
	{
		this.collapsedModel = collapsedModel;
		this.clustering = crpState;
	}

	public CollapsedConjugateModel getCollapsedConjugateModel()
	{
		return collapsedModel;
	}

	@Override
	public double logDensity()
	{
		if (!collapsedModel.checkHyperParameterBound())
			return Double.NEGATIVE_INFINITY;

		double result = 0.0;
		for (ClusterId id : clustering.getAllClusterIds())
			result += Parametrics.logMarginal(collapsedModel, collapsedModel.getHyperParameter(), clustering.getClusterStatistics(id));
      
		return result;
	}

}
