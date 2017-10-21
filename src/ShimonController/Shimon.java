package ShimonController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by yn on 3/13/17.
 */
public class Shimon {
    private double delay; // time in ms to delay
    private Arm[] arms;
    private double[] marimbaPositions;
    public static final int DEFAULT_NUMBER_OF_ARMS = 4;
    public static final double DEFAULT_DELAY = 465;
    public int HIGHEST_NOTE;
    public static final int LOWEST_NOTE = 48;
    public static final double MAXIMUM_ARM_SPEED = 2500; //maximum arm speed is 2500 mm/s
    public static final double MAXIMUM_ACCEL = 3.; //maximum arm accel is 3 g's
    //position of the acoustic marimba bars. Change this.marimbaPositions in constructor to make this take effect
    private static final double[] ACOUSTIC_MARIMBA_POSITIONS = {
            0, 10, 44, 73, 102, 157, 184, 212, 240, 267, 294, 324, 377, 406, 434, 463, 490, 546, 574, 599, 624, 651,
            673, 698, 749, 771, 798, 820, 846, 894, 919, 945, 969, 993, 1018, 1044, 1092, 1118, 1142, 1167, 1193, 1240,
            1266, 1291, 1315, 1339, 1364, 1385
    };

    //distance between bars on MALLETKAT midi marimba
    private static final double MALLETKAT_DIST = 28;

    //positions of MALLETKAT midi marimba bars: set algorithmically
    private static final double[] MALLETKAT_MARIMBA_POSITIONS = makeEvenSpacePositions(MALLETKAT_DIST);
    public static final double ERROR = -1;
    public static final double BIG_BOY = 999999999;

    //method for calculating evenly-spaced marimba marimba bars
    private static double[] makeEvenSpacePositions(double size){
        double[] octave = {2, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1};
        LinkedList<Double> temp = new LinkedList<>();
        int octIndex = 0;
        double currentNote = 0;
        double lastNote = -1;
        while (currentNote <= 1385){
            if (lastNote != -1) {
                currentNote += (octave[octIndex] * size);
            }
            temp.add(currentNote);
            octIndex = (octIndex + 1) % 12;
            lastNote = currentNote;
        }
        double[] result = new double[temp.size()];
        for (int i = 0; i < temp.size(); i++){
            result[i] = temp.get(i);
        }
        return result;
    }

    //constructor for Shimon. Delay is the amount of time in ms that the arms will take to reach a desired destination,
    //this delay can also be set at the time of issuing the move command
    public Shimon(double delay) {
        this.arms = new Arm[DEFAULT_NUMBER_OF_ARMS];
        this.delay = delay;
        this.marimbaPositions = ACOUSTIC_MARIMBA_POSITIONS;
        this.HIGHEST_NOTE = LOWEST_NOTE + marimbaPositions.length - 1;

        arms[0] = new Arm(0, 0, 0, 20);
        arms[1] = new Arm(10, 1, 20, 75);
        arms[2] = new Arm(1364, 2, 75, 20);
        arms[3] = new Arm(1385, 3, 20, 0);
    }

    public Shimon() {
        this(DEFAULT_DELAY);
    }

    //method for converting a midi note to a mm distance given the current marimba configuration.
    public double midiToDist(int midiNote){
        if (midiNote > HIGHEST_NOTE || midiNote < LOWEST_NOTE) {
            return ERROR;
        } else return marimbaPositions[midiNote - LOWEST_NOTE];
    }

    //this homing method sets arms to default locations in software (this is not hardware coming). Hardware homing
    //should be performed before software homing.
    public String home(int armIndex){
        return arms[armIndex].home();
    }

    //like midiToDist method above, but the other way around.
    public double distToMidi(double dist){
        for(int i = 0; i < marimbaPositions.length; i++){
            double first = marimbaPositions[i];
            double base = LOWEST_NOTE + i;
            if(i == marimbaPositions.length - 1 || first == dist) return base;
            double second = marimbaPositions[i + 1];
            if(first < dist && second > dist){
                return base + ((dist - first) / (second - first));
            }
        }
        return ERROR;
    }

    public String controlArm(int armIndex, int midiNote, int vel, double time, double deltaTime){
        double dist = midiToDist(midiNote);
        return controlArmDist(armIndex, dist, vel, time, deltaTime);
    }

    //schedules arm movement if possible
    public String controlArmDist(int armIndex, double dist, int vel, double time, double deltaTime){
        String serialMessage = null;
        Arm arm = arms[armIndex];
        boolean danger = isCollideDanger(arm, dist);
        if(!arm.isMoving(time) && !danger){
            delay = deltaTime;
            serialMessage = arm.scheduleCommand(dist, time, delay, vel, time);
        }
        return serialMessage;
    }


    public String scheduleIfPossible (int midiNote, int vel, double time, double deltaTime){
        return mididata(midiNote, vel, time, deltaTime, false);
    }

    private boolean isCollideDanger(Arm arm, double goal){
        double leftDanger = goal - arm.getLeftBound();
        double rightDanger = goal + arm.getRightBound();
        int armIndex = arm.getIndex();
        double[] dangerToLeft = armIndex > 0 ? arms[armIndex - 1].getDangerZone() : null;
        double[] dangerToRight = armIndex < arms.length - 1 ? arms[armIndex + 1].getDangerZone() : null;
        boolean danger = false;
        if (dangerToLeft != null) {
            danger = dangerToLeft[1] >= leftDanger;
        }
        if (dangerToRight != null) {
            if (!danger) {
                danger = dangerToRight[0] <= rightDanger;
            }
        }
        return danger;
    }

    public String[] chord(int[] notes, int vel, double gs, double v){
        int commandCount = 0;
        String[] commands = new String[notes.length];
        double time = System.currentTimeMillis();
        if(notes.length == arms.length){
            Arrays.sort(notes);
            for(int i = 0; i < notes.length; i++){
                Arm arm  = arms[i];
                if(arm.isStationary(time)){
                    double leftDanger = midiToDist(notes[i]) - arm.getLeftBound();
                    double rightDanger = midiToDist(notes[i]) + arm.getRightBound();
                    double[] dangerToLeft = i > 0 ? arms[i - 1].getDangerZone(midiToDist(notes[i-1])) : null;
                    double[] dangerToRight = i < arms.length - 1 ? arms[i + 1].getDangerZone(midiToDist(notes[i+1]))
                            : null;
                    boolean danger = false;
                    if (dangerToLeft != null) {
                        danger = dangerToLeft[1] >= leftDanger;
                    }
                    if (dangerToRight != null) {
                        if (!danger) {
                            danger = dangerToRight[0] <= rightDanger;
                        }
                    }
                    if(!danger){
                        commandCount++;
                    } else System.out.println("danger!");
                }
            }
        }
        if(commandCount == notes.length){
            for(int i = 0; i < notes.length; i++){
                commands[i] = arms[i].scheduleASAP(midiToDist(notes[i]), time, vel, time, gs, v);
            }
            return commands;
        } else return new String[0];
    }

    public String mididata(int midiNote, int vel, double time, double deltaTime){
        return mididata(midiNote, vel, time, deltaTime, true);
    }

    public String mididata(int midiNote, int vel, double time, double deltaTime, boolean transpose){
        int[] armIndexes = new int[arms.length];
        for(int i = 0; i < arms.length; i++){
            armIndexes[i] = i;
        }
        return controlArms(armIndexes, midiNote, vel, time, deltaTime, transpose);
    }

    public String controlArms(int[] armIndexes, int midiNote, int vel, double time, double deltaTime){
        return controlArms(armIndexes, midiNote, vel, time, deltaTime, true);
    }

    public String controlArms(int[] armIndexes, int midiNote, int vel, double time, double deltaTime, boolean transpose){
        Arm closest =  null;
        double lowestV = BIG_BOY;
        int currentOctave = 0;
        double dist = midiToDist(midiNote);
        int actualMarimbaLength = this.marimbaPositions.length;
        int octaveCount = (actualMarimbaLength / 12);
        int adjustedMarimbaLength = 12 * octaveCount;
        int extranotes = adjustedMarimbaLength - actualMarimbaLength;
        while(midiNote < LOWEST_NOTE){
            midiNote += 12;
        }
        while (closest == null && currentOctave < (transpose ? octaveCount : 1)) {
            int mid = ((midiNote + (12 * currentOctave)) - LOWEST_NOTE);
            int transposed = mid % actualMarimbaLength + LOWEST_NOTE - (mid < actualMarimbaLength ? 0  : extranotes);
            dist = midiToDist(transposed);
            for (int i : armIndexes) {
                Arm arm = arms[i];
                /*
                if (!arm.isMoving(time) || arm.getPosition(time + deltaTime) == dist) {
                    if (closest == null ||
                            Math.abs(arm.getPosition(time) - dist) < Math.abs(closest.getPosition(time) - dist)) {
                        double goal = midiToDist(transposed);
                        boolean danger = isCollideDanger(arm, goal);
                        if (!danger) {
                            closest = arm;
                        }
                    }
                }
                */
                double doneAt = arm.getGoalTime(time);
                double goalTime = time + deltaTime;
                double availableDeltaTime = goalTime - doneAt;

                if(arm.getPosition(time + deltaTime) == dist){
                    closest = arm;
                    lowestV = 0;
                }
                else if(availableDeltaTime > 0){
                    double goal = midiToDist(transposed);
                    boolean danger = isCollideDanger(arm, goal);
                    if(!danger){
                        double dDist = arm.getPosition(time) - dist;
                        double speed = Math.abs(dDist/availableDeltaTime);
                        if(closest == null || speed < lowestV){
                            if(arm.canSchedule(dist, doneAt, availableDeltaTime, vel, time)) {
                                closest = arm;
                                lowestV = speed;
                            }
                        }
                    }
                }
            }
            currentOctave += 1;
        }
        String serialMessage = null;
        if(closest != null){
            double doneAt = closest.getGoalTime(time);
            double goalTime = time + deltaTime;
            double availableDeltaTime = goalTime - doneAt;
            serialMessage = closest.scheduleCommand(dist, doneAt, availableDeltaTime, vel, time);
        }
        return serialMessage;
    }





    public double getArmPosition(int index, double time){
        return arms[index].getPosition(time);
    }

    public double getArmMidi(int index, double time){
        return distToMidi(getArmPosition(index, time));
    }

    public String toString(){
        String result = "Shimon with arms at ";
        for(Arm arm : arms){
            result = result + Double.toString(arm.getPosition(System.currentTimeMillis())) + " ";
        }
        return result;
    }
}
