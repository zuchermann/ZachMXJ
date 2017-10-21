package ShimonController;

import java.util.ArrayList;

/**
 * Created by yn on 3/13/17.
 */
public class Arm {
    private double homePosition;
    private ArrayList<MoveCommand> moveCommand;
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
        this.moveCommand = new ArrayList<MoveCommand>();
    }

    //returns double array representing the danger zone about this arm. If arm another enters this area, there may be a
    //collision
    public double[] getDangerZone(){
        double time = System.currentTimeMillis();
        double position = this.getPosition(time);
        if(moveCommand.size() > 0){
            double lb = (position - leftBound);
            double rb = (position + rightBound);
            for(MoveCommand mc : moveCommand){
                double commandGoal = mc.getGoalPosition();
                lb = Math.min(lb, (commandGoal - leftBound));
                rb = Math.max(rb, (commandGoal + rightBound));
            }
            return new double[]{lb, rb};
        } else {
            return new double[]{position - leftBound, position + rightBound};
        }
    }

    //returns double corresponding to arm's danger zone at point dist
    public double[] getDangerZone(double dist){
        return new double[]{dist - leftBound, dist + rightBound};
    }

    public double getPosition(double currentTime){
        if (moveCommand.size() == 0) { // only null when we haven't moved to home position
            return homePosition;
        } else {
            MoveCommand mostRecent = moveCommand.get(0);
            for (MoveCommand mc : moveCommand){
                if(currentTime > mc.getStartTime() && !mc.isDone(currentTime)){
                    return mc.getPosition(currentTime);
                }
                else if(mc.getGoalTime() > mostRecent.getGoalTime()){
                    mostRecent = mc;
                }
            }
            return mostRecent.getPosition(currentTime);
        }
    }

    public double getLeftBound() { return leftBound; }

    public double getRightBound() { return rightBound; }

    public int getIndex() { return armIndex; }

    //returns whether arm is currently on it's way to hit a note - note that this will return true even if
    //the scheduled note does not require the arms move
    public boolean isMoving(double time){
        for (MoveCommand mc : moveCommand){
            if(time > mc.getStartTime() && !mc.isDone(time)){
                return true;
            }
        }
        return false;
    }

    //like opposite isMoving, but will return true if arm has scheduled note that does not require arm movement
    public boolean isStationary(double time) {
        for (MoveCommand mc : moveCommand){
            if(time > mc.getStartTime() && !mc.isDone(time)){
                if(mc.getPosition(time) != mc.getGoalPosition()) {
                    return false;
                }
            }
        }
        return true;
    }

    public String home() {
        double time = System.currentTimeMillis();
        double deltaT = 1000;
        double goalTime = time + deltaT;
        double startPosition = homePosition - 200;
        double goalPosition = homePosition;
        double displacement = goalPosition - startPosition;
        double maxV = Math.min(Shimon.MAXIMUM_ARM_SPEED, TWO * displacement/deltaT);
        double accel = maxV == 0 ? 0 : (maxV * maxV)/((deltaT * maxV) - displacement);
        double startV = ZERO;

        MoveCommand newCommand = new MoveCommand(time, goalTime, startPosition, goalPosition,
                startV, maxV, accel, armIndex, 0, time);
        pushCommand(newCommand);
        return newCommand.getDirectControl();
    }

    public boolean canSchedule(double dist, double time, double deltaT, int vel, double initialTime){
        double startPosition = getPosition(time);
        double goalPosition = dist;
        double displacement = goalPosition - startPosition;
        double maxV = TWO * displacement/deltaT;
        double accel = maxV == 0 ? 0 : (maxV * maxV)/((deltaT * maxV) - displacement);

        return (Math.abs(maxV) * 1000.) < Shimon.MAXIMUM_ARM_SPEED &&
                (Math.abs(accel) * MoveCommand.G_MULT) < Shimon.MAXIMUM_ACCEL;

    }

    public String scheduleCommand(double dist, double time, double deltaT, int vel, double initialTime){
        double goalTime = time + deltaT;
        double startPosition = getPosition(time);
        double goalPosition = dist;
        double displacement = goalPosition - startPosition;
        double maxV = TWO * displacement/deltaT;
        double accel = maxV == 0 ? 0 : (maxV * maxV)/((deltaT * maxV) - displacement);
        double startV = ZERO;

        //have to multiply maxV by 1000 because maxV is in mm/ms
        if ((Math.abs(maxV) * 1000.) < Shimon.MAXIMUM_ARM_SPEED &&
                (Math.abs(accel) * MoveCommand.G_MULT) < Shimon.MAXIMUM_ACCEL) {
            MoveCommand newCommand = new MoveCommand(time, goalTime, startPosition, goalPosition,
                    startV, maxV, accel, armIndex, vel, initialTime);
            pushCommand(newCommand);
            return newCommand.getDirectControl();
        }
        else return null;
    }

    double getGoalTime(double time){
        double result = time;
        for (MoveCommand mc : moveCommand){
            double goal = mc.getGoalTime();
            if (goal > result){
                result = goal;
            }
        }
        return result;
    }

    //schedules movement given acceleration in g's (0-Shimon.MAXIMUM_ACCEL)
    // and max velocity in mm/s (0-Shimon.MAXIMUM_ARM_SPEED)
    String scheduleASAP(double dist, double time, int vel, double initialTime, double gs, double v){
        if(gs < Shimon.MAXIMUM_ACCEL && v < Shimon.MAXIMUM_ARM_SPEED){
            double startPosition = getPosition(time);
            double displacement = dist - startPosition;
            if(displacement != 0) {
                double maxV = Math.signum(displacement) * v / 1000.; // mm/s -> mm/ms
                double accel = Math.signum(displacement) * (gs / MoveCommand.G_MULT);

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
                MoveCommand newCommand = new MoveCommand(time, goalTime, startPosition, dist,
                        ZERO, maxV, accel, armIndex, vel, initialTime);
                pushCommand(newCommand);
                return newCommand.getDirectControl();
            } else return null;

        } else return null;
    }

    void pushCommand(MoveCommand newCommand){
        ArrayList<MoveCommand> toDelete = new ArrayList<>();
        for(MoveCommand mc : moveCommand){
            double time = System.currentTimeMillis();
            if(mc.isDone(time)){
                toDelete.add(mc);
            }
        }
        for(MoveCommand mc : toDelete){
            moveCommand.remove(mc);
        }
        this.moveCommand.add(newCommand);
    }
}
