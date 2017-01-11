package polya.ddcrp;

import polya.crp.utils.ClusterId;
import polya.parametric.CollapsedConjugateModel;
import polya.parametric.Parametrics;
import blang.annotations.FactorArgument;
import blang.annotations.FactorComponent;
import blang.factors.Factor;

public class DDCRPFactor implements Factor
{
	@FactorComponent
	public final CollapsedConjugateModel collapsedModel;

	@FactorArgument(makeStochastic=true)
	public final DDCRPStateInterface ddCRPState;

	public DDCRPFactor(CollapsedConjugateModel collapsedModel, DDCRPStateInterface ddCRPState)
	{
		this.collapsedModel = collapsedModel;
		this.ddCRPState = ddCRPState;
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
		for (ClusterId id : ddCRPState.getAllClusterIds())
			result += Parametrics.logMarginal(collapsedModel, collapsedModel.getHyperParameter(), ddCRPState.getClusterStatistics(id));
      
		return result;
	}

}
