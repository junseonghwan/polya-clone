package polya.ddcrp;

public class IndicatorDecay implements DecayFunction
{
	public final double a;
	
	public IndicatorDecay(double a) {
	  this.a = a;
  }
	
	@Override
	public double decay(Customer c1, Customer c2) {
		if (Math.abs(c1.x - c2.x) <= a && Math.abs(c1.y - c2.y) <= a)
			return 1.0;
		return 0.0;
	}

}
