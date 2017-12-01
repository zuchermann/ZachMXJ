package ShimonController;

/**
 * Created by yn on 11/27/17.
 */
public class MotorCommand implements ActionCommand{
    private int axis;
    private int position;
    private int vMax;
    private int midiNote;
    private float accel;
    private double strikeTime;
    private double moveTime;
    private Shimon shimon;
    private boolean moved;
    private boolean striked;
    MotorController motors;
    Strikers strikers;


    MotorCommand(int axis, int position, int vMax, int midiNote, float accel, double strikeTime, double moveTime, Shimon shimon, MotorController motors, Strikers strikers) {
        this.axis = axis;
        this.position = position;
        this.vMax = vMax;
        this.accel = accel;
        this.strikeTime = strikeTime;
        this.moveTime = moveTime;
        this.midiNote = midiNote;
        this.shimon = shimon;
        this.motors = motors;
        this.strikers = strikers;
        this.moved = false;
        this.striked = false;
    }

    public boolean isDone(double time){
        return time >= strikeTime && time >= moveTime;
    }

    public boolean schedule(double time){
        int rel_position = (int) shimon.getRelativePosition(axis, position);
        if(time >= moveTime && !moved) {
            if(position >= 0 && vMax > 0 && accel > 0){
                if(motors != null) {
                    motors.setTarget(axis, rel_position, vMax, accel);
                }
            }
            System.out.println(time - moveTime);
            //System.out.println(""+axis+" "+rel_position+" "+vMax+" "+accel);
            moved = true;
        } if(time >= strikeTime && !striked){
            strikers.strike(axis, midiNote);
            striked = true;
        }
        return isDone(time);
    }
}
