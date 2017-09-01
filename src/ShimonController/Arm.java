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
    public static final double HALF = 0.5;
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

    //returns double corresponding to arm's danger zone at point dist
    public double[] getDangerZone(double dist){
        double[] dangerZone  = {dist - leftBound, dist + rightBound};
        return dangerZone;
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

    //returns whether arm is currently on it's way to hit a note - note that this will return true even if
    //the scheduled note does not require the arms move
    public boolean isMoving(double time){
        return moveCommand != null && !moveCommand.isDone(time);
    }

    //like opposite isMoving, but will return true if arm has scheduled note that does not require arm movement
    public boolean isStationary(double time) {
        return moveCommand == null || moveCommand.isDone(time) ||
                moveCommand.getPosition(time) == moveCommand.getGoalPosition();
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

    public boolean canSchedule(double dist, double time, double deltaT, int vel, double initialTime){
        double startPosition = moveCommand == null ? homePosition : moveCommand.getPosition(time);
        double goalPosition = dist;
        double displacement = goalPosition - startPosition;
        double maxV = TWO * displacement/deltaT;
        double accel = maxV == 0 ? 0 : (maxV * maxV)/((deltaT * maxV) - displacement);

        return (Math.abs(maxV) * 1000.) < Shimon.MAXIMUM_ARM_SPEED &&
                (Math.abs(accel) * MoveCommand.G_MULT) < Shimon.MAXIMUM_ACCEL;

    }

    public String scheduleCommand(double dist, double time, double deltaT, int vel, double initialTime){
        double goalTime = time + deltaT;
        double startPosition = moveCommand == null ? homePosition : moveCommand.getPosition(time);
        double goalPosition = dist;
        double displacement = goalPosition - startPosition;
        double maxV = TWO * displacement/deltaT;
        double accel = maxV == 0 ? 0 : (maxV * maxV)/((deltaT * maxV) - displacement);
        double startV = ZERO;

        //have to multiply maxV by 1000 because maxV is in mm/ms
        if ((Math.abs(maxV) * 1000.) < Shimon.MAXIMUM_ARM_SPEED &&
                (Math.abs(accel) * MoveCommand.G_MULT) < Shimon.MAXIMUM_ACCEL) {
            moveCommand = new MoveCommand(time, goalTime, startPosition, goalPosition,
                    startV, maxV, accel, armIndex, vel, initialTime);
            return moveCommand.getDirectControl();
        }
        else return null;
    }

    //schedules movement given acceleration in g's (0-Shimon.MAXIMUM_ACCEL)
    // and max velocity in mm/s (0-Shimon.MAXIMUM_ARM_SPEED)
    public String scheduleASAP(double dist, double time, int vel, double initialTime, double gs, double v){
        if(gs < Shimon.MAXIMUM_ACCEL && v < Shimon.MAXIMUM_ARM_SPEED){
            double startPosition = moveCommand == null ? homePosition : moveCommand.getPosition(time);
            double goalPosition = dist;
            double displacement = goalPosition - startPosition;
            if(displacement != 0) {
                double maxV = Math.signum(displacement) * v / 1000.; // mm/s -> mm/ms
                double accel = Math.signum(displacement) * (gs / MoveCommand.G_MULT);
                double startV = ZERO;

                double t_1 = Math.abs(maxV / accel);
                double d_1 = Math.abs((HALF) * t_1 * maxV);
                boolean isTrapazoid = (TWO * d_1) < Math.abs(displacement);
                double deltaT = 0;
                if (isTrapazoid) {
                    double d_2 = Math.abs(displacement) - (TWO * d_1);
                    double t_2 = Math.abs(d_2 / maxV);
                    deltaT = (TWO * t_1) + t_2;
                } else {
                    deltaT = Math.sqrt(Math.abs((TWO * displacement) / accel));
                    maxV = displacement/deltaT;
                }
                double goalTime = time + deltaT;
                moveCommand = new MoveCommand(time, goalTime, startPosition, goalPosition,
                        startV, maxV, accel, armIndex, vel, initialTime);
                return moveCommand.getDirectControl();
            } else return null;

        } else return null;
    }
}
