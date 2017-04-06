package ShimonController;

/**
 * Created by yn on 3/13/17.
 */
public class Arm {
    private double homePosition;
    private MoveCommand moveCommand;
    private int armIndex;
    private double leftBound;
    private double rightBound;
    public static final double TWO = 2.0;
    public static final double ZERO = 0;

    public Arm(double homePosition, int armIndex, double leftBound, double rightBound){
        this.homePosition = homePosition;
        this.armIndex = armIndex;
        this.leftBound = leftBound;
        this.rightBound = rightBound;
    }

    //returns double array representing the danger zone about this arm. If arm another enters this area, there may be a
    //collision
    public double[] getDangerZone(){
        double time = System.currentTimeMillis();
        double position = this.getPosition(time);
        if(moveCommand != null){
            double commandGoal = moveCommand.getGoalPosition();
            double[] dangerZone  =
                    {
                    Math.min((position - leftBound), (commandGoal - leftBound)),
                    Math.max((position + rightBound), (commandGoal + rightBound))
                    };
            return dangerZone;
        } else {
            double[] dangerZone  = {position - leftBound, position + rightBound};
            return dangerZone;
        }
    }

    public double getPosition(double currentTime){
        if (moveCommand == null) { // only null when we haven't moved to home position
            return homePosition;
        } else {
            return moveCommand.getPosition(currentTime);
        }
    }

    public double getLeftBound() { return leftBound; }

    public double getRightBound() { return rightBound; }

    public String getSerialMessage() { return moveCommand == null ? null : moveCommand.getSerialMessage();}

    public int getIndex() { return armIndex; }

    public boolean isMoving(double time){
        return moveCommand != null && !moveCommand.isDone(time);
    }

    public String home(double initialTime) {
        double time = System.currentTimeMillis();
        double deltaT = 1000;
        double goalTime = time + deltaT;
        double startPosition = homePosition - 200;
        double goalPosition = homePosition;
        double displacement = goalPosition - startPosition;
        double maxV = Math.min(Shimon.MAXIMUM_ARM_SPEED, TWO * displacement/deltaT);
        double accel = maxV == 0 ? 0 : (maxV * maxV)/((deltaT * maxV) - displacement);
        double startV = ZERO;

        moveCommand = new MoveCommand(time, goalTime, startPosition, goalPosition,
                startV, maxV, accel, armIndex, 0, initialTime);
        return moveCommand.getDirectControl();
    }

    public String scheduleCommand(double dist, double time, double deltaT, int vel, double initialTime){
        double goalTime = time + deltaT;
        double startPosition = moveCommand == null ? homePosition : moveCommand.getPosition(time);
        double goalPosition = dist;
        double displacement = goalPosition - startPosition;
        double maxV = Math.min(Shimon.MAXIMUM_ARM_SPEED, TWO * displacement/deltaT);
        double accel = maxV == 0 ? 0 : (maxV * maxV)/((deltaT * maxV) - displacement);
        double startV = ZERO;

        moveCommand = new MoveCommand(time, goalTime, startPosition, goalPosition,
                startV, maxV, accel, armIndex, vel, initialTime);
        return moveCommand.getDirectControl();
    }
}
