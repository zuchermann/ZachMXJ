package ShimonController;

import com.cycling74.max.Atom;
import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;
import com.sun.javafx.binding.DoubleConstant;
import com.sun.javafx.binding.IntegerConstant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/**
 * Created by yn on 3/13/17.
 */
public class Max extends MaxObject{

    private int currentOutlet = 0;
    public static final int NUM_OF_ARMS = 4;
    private Shimon shimon;
    private boolean outputDist;
    private boolean isOutputTarget;

    public Max() {
        this(false);
    }

    public Max(boolean outputDist){
        this(outputDist, false);
    }

    public Max(boolean outputDist, boolean outputTarget){
        //createInfoOutlet(false);

        this.outputDist = outputDist;
        this.isOutputTarget = outputTarget;

        declareInlets(new int[]{ DataTypes.ALL });
        declareOutlets(new int[]{ DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});

        setInletAssist(new String[] {
                "int midinote input",
        });
        setOutletAssist(new String[] {
                "position of arm 1",
                "position of arm 2",
                "position of arm 3",
                "position of arm 4",
                "serial command"});

        shimon = new Shimon();
    }

    public void mididata(int midiNote, int noteVel, double deltaTime) {
        double time = System.currentTimeMillis();
        String serialMessage =  shimon.mididata(midiNote, noteVel, time, deltaTime);
        if(serialMessage != null) {
            outlet(NUM_OF_ARMS, Atom.parse(serialMessage));
        }
    }

    public void controlArm(int armIndex, int midiNote, int noteVel, double deltaTime) {
        double dist = shimon.midiToDist(midiNote);
        controlArmDist(armIndex, dist, noteVel, deltaTime);
    }

    public void controlArms(String arms, int midiNote, int noteVel, double deltaTime, boolean transpose) {
        String[] splitted = arms.split("\\s+");
        int[] armIndexes = new int[splitted.length];
        for(int i = 0; i < splitted.length; i++){
            armIndexes[i] = Integer.parseInt(splitted[i]);
        }
        double time = System.currentTimeMillis();
        String serialMessage =  shimon.controlArms(armIndexes, midiNote, noteVel, time, deltaTime, transpose);
        if(serialMessage != null) {
            outlet(NUM_OF_ARMS, Atom.parse(serialMessage));
            if(noteVel > 0 && this.isOutputTarget) {
                int arm_index = Integer.parseInt(serialMessage.split("\\s")[0]);
                outlet(arm_index-1, midiNote);
            }
        }
    }

    public void chord(int midi1, int midi2, int midi3, int midi4, double a_mult, double v_mult){
        int[] notes = {midi1, midi2, midi3, midi4};
        String[] commands = shimon.chord(notes, 0, a_mult * Shimon.MAXIMUM_ACCEL, v_mult * Shimon.MAXIMUM_ARM_SPEED);
        for(String s : commands){
            if(s != null) {
                outlet(NUM_OF_ARMS, Atom.parse(s));
            }
        }
    }

    public void controlArms(String arms, int midiNote, int noteVel, double deltaTime) {
        controlArms(arms, midiNote, noteVel, deltaTime, true);
    }

    public void controlArmDist(int armIndex, double dist, int noteVel, double deltaTime) {
        double time = System.currentTimeMillis();
        String serialMessage =  shimon.controlArmDist(armIndex, dist, noteVel, time, deltaTime);
        if(serialMessage != null) {
            outlet(NUM_OF_ARMS, Atom.parse(serialMessage));
        }
    }

    public void home() throws InterruptedException {
        outlet(NUM_OF_ARMS, Atom.parse(shimon.home(0)));
        TimeUnit.SECONDS.sleep(1);
        outlet(NUM_OF_ARMS, Atom.parse(shimon.home(3)));
        TimeUnit.SECONDS.sleep(1);
        outlet(NUM_OF_ARMS, Atom.parse(shimon.home(1)));
        TimeUnit.SECONDS.sleep(1);
        outlet(NUM_OF_ARMS, Atom.parse(shimon.home(2)));
        TimeUnit.SECONDS.sleep(1);
    }

    public void bang(){
        double time = System.currentTimeMillis();
        for(int i = 0; i < NUM_OF_ARMS; i++){
            if(outputDist){
                outlet(i, shimon.getArmPosition(i, time));
            }else {
                outlet(i, shimon.getArmMidi(i, time));
            }
        }
    }

}
