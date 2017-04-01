package ShimonController;

/**
 * Created by yn on 3/13/17.
 */
public class MoveCommand {
    private double startTime;
    private double goalTime;
    private double startPosition;
    private double goalPosition;
    private double startV;
    private double maxV;
    private double accel;
    private double midTime; // represents midpoint between startTime goalTime
    private double timeOfMax; // time at which max velocity is hit
    private double observedMaxV;
    private int armIndex;

    public static final double G_MULT = 1/9800.; //multiply acceleration of the form mm/s^2 with this to get g's
    public static final double HALF = 0.5;

    public MoveCommand(double startTime,
                       double goalTime,
                       double startPosition,
                       double goalPosition,
                       double startV,
                       double maxV,
                       double accel,
                       int armIndex){
        this.startTime = startTime;
        this.goalTime = goalTime;
        this.startPosition = startPosition;
        this.goalPosition = goalPosition;
        this.startV = startV; // *****!!!***** CURRENTLY ONLY WORKS WITH START VELOCITY OF 0!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        this.maxV = maxV;
        this.accel = accel;
        this.midTime = (goalTime + startTime) * HALF;
        this.timeOfMax = accel == 0 ? startTime : ((maxV - startV) / accel);
        this.observedMaxV = Math.signum(maxV) * Math.min(Math.abs(startV + (timeOfMax * accel)), Math.abs(maxV));
        this.armIndex = armIndex;
        //System.out.println(getDirectControl());
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

    public double getPosition(double currentTime){
        double deltaT = currentTime - startTime;
        //first region
        double firstBound = Math.min(this.midTime, this.timeOfMax + startTime);
        deltaT = Math.min((firstBound - startTime), Math.max((currentTime - startTime), 0));
        double displacement1 = (startV * deltaT) + (HALF * accel * deltaT * deltaT);

        //second region
        double secondBound = Math.max(this.midTime, goalTime - this.timeOfMax);
        deltaT = Math.min((secondBound - firstBound), Math.max((currentTime - firstBound), 0));
        double displacement2 = (maxV * deltaT);

        //third region
        deltaT = Math.min((goalTime - secondBound), Math.max((currentTime - secondBound), 0));
        double displacement3 = (observedMaxV * deltaT) + (HALF * (-1) * accel * deltaT * deltaT);

        double totalDisplacement = displacement1 + displacement2 + displacement3;

        return (currentTime > goalTime) ? goalPosition : totalDisplacement + startPosition;
    }

    public double getGoalPosition(){
        return this.goalPosition;
    }

    //gets string representing arm (1 index), Xtarget(mm), A(g), vmax, Arrival-time
    public String getDirectControl(){
        return Integer.toString(armIndex + 1) + " " +
                Double.toString(goalPosition) +  " " +
                Double.toString(accel * G_MULT) +  " " +
                Double.toString(maxV) +  " " +
                Double.toString(goalTime);
    }

    public String getSerialMessage(){
        return null;
    }

    public boolean isDone(double time){
        return time >= goalTime;
    }
}
