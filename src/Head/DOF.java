package Head;

import com.cycling74.max.MaxObject;

/**
 * Created by yn on 10/24/17.
 */

public class DOF extends MaxObject {
    private JointModel joint;

    public DOF(double low_bound, double high_bound, double maxV, double accel){
        joint = new JointModel(low_bound, high_bound, maxV, accel);
    }

    public void command(double position, double velocity){
        joint.move(position, velocity);
    }

    public void bang(){
        outlet(0, joint.getPosition());
    }
}
