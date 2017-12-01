package innards.math.linalg;

import innards.*;

/**
   Implements Vec3NumericalDerivative. Calculates Vec3 derivatives given two samples and a <code>dt</code>.
*/ 
public class Vec3TwoSampleDerivative extends NamedObject
  implements Vec3NumericalDerivative
{
  private VecTwoSampleDerivative rep = new VecTwoSampleDerivative(3);
  private Vec my_x = new Vec(3);
  private Vec my_dx = new Vec(3);
  
    /**
       Constructor, with name.
    */
  public Vec3TwoSampleDerivative(String n) { super(n); }

    /**
       Initializes the name to <code>"(unnamed)"</code>
    */
  public Vec3TwoSampleDerivative() { this("(unnamed)"); }

    /**
       Clears all added samples and wipes the state.
    */
  public void init()
  {
    rep.init();
  }

  public void addSample(Vec3 x, double dt)
  {
    my_x.fromVec3(x);
    rep.addSample(my_x, dt);
  }

  public void getDerivative(Vec3 dx)
  {
    rep.getDerivative(my_x);
    dx.setValue(my_x.get(0),
		my_x.get(1),
		my_x.get(2));
  }


  public void calcDerivativeWithoutAddingSample(Vec3 x, double dt, Vec3 dx)
  {
    my_x.setValue(x);
    rep.calcDerivativeWithoutAddingSample(my_x, dt, my_dx);
    dx.setValue(my_dx.get(0),
		my_dx.get(1),
		my_dx.get(2));
  }

}
