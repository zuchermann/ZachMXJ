package ShimonController;

/**
 * Created by yn on 3/13/17.
 */
public class Shimon {
    private double delay; // time in ms to delay
    private Arm[] arms;
    private double[] marimbaPositions;
    public static final int DEFAULT_NUMBER_OF_ARMS = 4;
    public static final double DEFAULT_DELAY = 465;
    public static final int HIGHEST_NOTE = 95;
    public static final int LOWEST_NOTE = 48;
    public static final double MAXIMUM_ARM_SPEED = 2500; //maximum arm speed is 2500 mm
    public static final double[] ACOUSTIC_MARIMBA_POSITIONS = {
            0, 10, 44, 73, 102, 157, 184, 212, 240, 267, 294, 324, 377, 406, 434, 463, 490, 546, 574, 599, 624, 651,
            673, 698, 749, 771, 798, 820, 846, 894, 919, 945, 969, 993, 1018, 1044, 1092, 1118, 1142, 1167, 1193, 1240,
            1266, 1291, 1315, 1339, 1364, 1385
    };
    public static final double ERROR = -1;

    public Shimon(double delay) {
        this.arms = new Arm[DEFAULT_NUMBER_OF_ARMS];
        this.delay = delay;
        this.marimbaPositions = ACOUSTIC_MARIMBA_POSITIONS;

        arms[0] = new Arm(0, 0, 0, 100);
        arms[1] = new Arm(10, 1, 100, 100);
        arms[2] = new Arm(1364, 2, 100, 100);
        arms[3] = new Arm(1385, 3, 100, 0);
    }

    public Shimon() {
        this(DEFAULT_DELAY);
    }

    public double midiToDist(int midiNote){
        if (midiNote > HIGHEST_NOTE || midiNote < LOWEST_NOTE) {
            return ERROR;
        } else return marimbaPositions[midiNote - LOWEST_NOTE];
    }

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

    public String mididata(int midiNote, double time){
        Arm closest =  null;
        double dist = midiToDist(midiNote);
        for (Arm arm : arms) {
            if(! arm.isMoving(time)){
                if (    closest == null ||
                        Math.abs(arm.getPosition(time) - dist) < Math.abs(closest.getPosition(time) - dist)){
                    double goal = midiToDist(midiNote);
                    double leftDanger = goal - arm.getLeftBound();
                    double rightDanger = goal + arm.getRightBound();
                    int armIndex = arm.getIndex();
                    double[] dangerToLeft = armIndex > 0 ? arms[armIndex - 1].getDangerZone() : null;
                    double[] dangerToRight = armIndex < arms.length - 1 ? arms[armIndex + 1].getDangerZone() : null;
                    boolean danger = false;
                    if (dangerToLeft != null){
                        danger = dangerToLeft[1] >= leftDanger;
                    } if (dangerToRight != null){
                        if (!danger) {
                            danger = dangerToRight[0] <= rightDanger;
                        }
                    }
                    if(!danger) {
                        closest = arm;
                    }
                }
            }
        }
        String serialMessage = null;
        if(closest != null){
            closest.scheduleCommand(dist, time, delay);
            serialMessage = closest.getSerialMessage();
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
