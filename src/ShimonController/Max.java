package ShimonController;

import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;
import com.sun.javafx.binding.DoubleConstant;

/**
 * Created by yn on 3/13/17.
 */
public class Max extends MaxObject{

    private int currentOutlet = 0;
    public static final int NUM_OF_ARMS = 4;
    private Shimon shimon;

    public Max() {
        //createInfoOutlet(false);

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

    public void mididata(int midiNote) {
        double time = System.currentTimeMillis();
        String serialMessage =  shimon.mididata(midiNote, time);
        if(serialMessage != null) {
            outlet(NUM_OF_ARMS, serialMessage);
        }
    }

    public void bang(){
        double time = System.currentTimeMillis();
        for(int i = 0; i < NUM_OF_ARMS; i++){
            outlet(i, shimon.getArmMidi(i, time));
        }
    }

}
