package Head;

/**
 * Created by yn on 10/23/17.
 */
public class JointModel {
    private double low_bound;
    private double high_bound;
    private double maxV;
    private double accel;
    private double startV;
    private double startTime;
    private double startPosition;
    private double goalPosition;
    private double delta_t_1;
    private double delta_t_2;
    private double delta_t_3;
    private double time_finished;
    private double v_t_2;

    private static final double HALF = 0.5;

    JointModel(double low_bound, double high_bound, double maxV, double accel) {
        this.low_bound = low_bound;
        this.high_bound = high_bound;
        this.maxV = maxV;
        this.accel = accel;
        this.startV  = 0;
        double current_time = System.currentTimeMillis();
        startTime = current_time;
        delta_t_1 = 0;
        delta_t_2 = 0;
        delta_t_3 = 0;
        v_t_2 = 0;
        time_finished = current_time;
    }

    // Returns position based on movement information. Movement can be thought of as a velocity vs time trapezoid. As
    // such, the position can be calculated by the sum of the areas of the three sections of the trapezoid.
    //
    //  |            ____________
    //  |           /|          |\
    // v|          / |          | \
    // e|         /  |          |  \
    // l|        /   |          |   \
    // o|       /    |          |    \
    // c|      /     |          |     \
    // i|     /      |          |      \
    // t|    /       |          |       \
    // y|   /        |          |        \
    //  |  /         |          |         \
    //  | / region 1 | region 2 | region 3 \
    //  ________________________________________
    //                  time -->

    public void move(double in_position, double in_velocity){
        double position = in_position;
        double velocity = Math.abs(in_velocity);
        if(in_position < low_bound){
            position = low_bound;
        } else if(in_position > high_bound){
            position = high_bound;
        }
        if(in_velocity > maxV){
            velocity = maxV;
        }
        startV = getVelocity();
        startPosition = getPosition();
        goalPosition = position;
        double signed_distance = position - startPosition;
        double signed_velocity = Math.signum(signed_distance) * velocity;
        double distance_to_max;
        boolean speeds_up = true;
        double total_time_up;
        double total_time_down;
        if(Math.signum(startV) != Math.signum(signed_velocity)){ // has to change directions
            double time_to_direction_switch = Math.abs(startV/accel);
            double time_to_goal_v = velocity/accel;
            distance_to_max = HALF * ((time_to_direction_switch * startV) + (time_to_goal_v * signed_velocity));
            total_time_up = time_to_direction_switch + time_to_goal_v;
        } else {
            double slower_v;
            double faster_v;
            if(Math.abs(startV) < velocity){ // has to speed up
                slower_v = startV;
                faster_v = signed_velocity;
            }
            else { // has to slow down
                speeds_up = false;
                slower_v = signed_velocity;
                faster_v = startV;
            }
            double t_to_now = Math.abs(slower_v/accel);
            double t_to_max = Math.abs(faster_v/accel);
            total_time_up = t_to_max - t_to_now;
            distance_to_max = HALF * ((faster_v * t_to_max) - (slower_v * t_to_now));
        }
        double t_to_zero = velocity/accel;
        total_time_down = t_to_zero;
        double distance_to_zero = HALF * (signed_velocity * t_to_zero);
        double total_signed_distance = distance_to_max + distance_to_zero;
        double signed_dist_diff =  signed_distance - total_signed_distance;
        if(Math.abs(total_signed_distance) > Math.abs(signed_distance)){ // overshoot
            if(speeds_up){
                double mid_T = Math.sqrt(Math.abs(signed_dist_diff)/accel);
                double delta_V = accel * mid_T * Math.signum(signed_velocity) * -1;
                double potential_adjusted_V = signed_velocity + delta_V;
                double adjusted_V = potential_adjusted_V;
                double new_delta = delta_V;
                if(Math.abs(potential_adjusted_V) < Math.abs(startV)){
                    adjusted_V = startV;
                    new_delta = startV - signed_velocity;
                }
                if(Math.abs(delta_V) > velocity){
                    adjusted_V = 0;
                    new_delta = signed_velocity * -1;
                }
                mid_T = Math.abs(new_delta/accel);
                delta_t_2 = mid_T;
                v_t_2 = adjusted_V;
                delta_t_1 = total_time_up - (HALF * delta_t_2);
                delta_t_3 = total_time_down - (HALF * delta_t_2);
            } else {
                v_t_2 = signed_velocity;
                delta_t_1 = total_time_up;
                delta_t_2 = 0;
                delta_t_3 = total_time_down;
                // nothing you can do, decelerate to zero
            }
        } else { // undershoot
            v_t_2 = signed_velocity;
            delta_t_1 = total_time_up;
            delta_t_2 = Math.abs(signed_dist_diff/velocity);
            delta_t_3 = total_time_down;
        }
        startTime = System.currentTimeMillis();
        time_finished = startTime + delta_t_1 + delta_t_2 + delta_t_3;
    }

    public double getVelocity(){
        double currentTime = System.currentTimeMillis();
        double velocity = 0;
        if(currentTime >= startTime && currentTime < startTime + delta_t_1){
            //first region
            double current_delta = currentTime - startTime;
            double time_proportion = current_delta / delta_t_1;
            double deltaV = v_t_2 - startV;
            velocity = startV + (time_proportion * deltaV);
        } else if(currentTime >= startTime + delta_t_1 && currentTime < startTime + delta_t_1 + delta_t_2){
            //second region
            velocity = v_t_2;
        } else if(currentTime >= startTime + delta_t_1 + delta_t_2 && currentTime < time_finished){
            //third region
            double current_delta = currentTime - (startTime + delta_t_1 + delta_t_2);
            double time_proportion = current_delta / delta_t_3;
            double deltaV = 0 - v_t_2;
            velocity = v_t_2 + (time_proportion * deltaV);
        }
        return velocity;
    }

    public double getPosition() {
        double currentTime = System.currentTimeMillis();
        double deltaT = currentTime - startTime;
        //first region
        double firstBound = startTime + delta_t_1;
        deltaT = Math.min((firstBound - startTime), Math.max((currentTime - startTime), 0));
        double displacement1 = (startV * deltaT) + (HALF * accel * deltaT * deltaT);

        //second region
        double secondBound = startTime + delta_t_1 + delta_t_2;
        deltaT = Math.min((secondBound - firstBound), Math.max((currentTime - firstBound), 0));
        double displacement2 = (maxV * deltaT);

        //third region
        deltaT = Math.min((time_finished - secondBound), Math.max((currentTime - secondBound), 0));
        double displacement3 = (v_t_2 * deltaT) + (HALF * (-1) * accel * deltaT * deltaT);

        double totalDisplacement = displacement1 + displacement2 + displacement3;

        System.out.println(Boolean.toString(currentTime > time_finished));
        return (currentTime > time_finished) ? goalPosition : totalDisplacement + startPosition;
    }

}
