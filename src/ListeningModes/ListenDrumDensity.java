package ListeningModes;

import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;

import java.util.ArrayList;

/**
 * Created by Hanoi on 12/6/16.
 */

// This function needs a counter to bang the max values of each of the drum components

public class ListenDrumDensity extends MaxObject {
    private int kickCount = 0;
    private int snareCount = 0;
    private int tomsCount = 0;
    private int hihatCount = 0;
    private int rideCount = 0;
    private int maxKickCount = 1;
    private int maxSnareCount = 1;
    private int maxTomsCount = 1;
    private int maxHihatCount = 1;
    private int maxRideCount = 1;
    ArrayList smoother = new ArrayList();


    public ListenDrumDensity() {

        createInfoOutlet(false);
        declareInlets(new int[]{DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});
        declareOutlets(new int[]{DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});

        setInletAssist(new String[]{
                "Message functions",
                "Kick input - bang",
                "Snare input - bang",
                "Toms input - bang",
                "Hihat input - bang",
                "Ride input - bang",
                "Max kick count - float",
                "Max snare count - float",
                "Max toms count - float",
                "Max hihat count - float",
                "Max ride count - float",
                "Query current density - bang"
        });
        setOutletAssist(new String[]{
                "outputs kick density",
                "outputs snare density",
                "outputs toms density",
                "outputs hihat density",
                "outputs ride density",
                "outputs overall density"
        });
    }

    public void bang() {

        //outputDensities(); //for every bang message update the densities

        int inlet_no;
        inlet_no = getInlet();
        switch (inlet_no) {
            case 1: //KICK counter
                kickCount = kickCount + 1;
                //post("kickCount: " + kickCount + '\n');
                break;
            case 2: //SNARE counter
                snareCount = snareCount + 1;
                //post("snareCount: " + snareCount + '\n');
                break;
            case 3: //TOMS counter
                tomsCount = tomsCount + 1;
                //post("tomsCount: " + tomsCount + '\n');
                break;
            case 4: //HH counter
                hihatCount = hihatCount + 1;
                //post("hihatCount: " + hihatCount + '\n');
                break;
            case 5: //RIDE counter
                rideCount = rideCount + 1;
                //post("rideCount: " + rideCount + '\n');
                break;
            case 11: //Query density
                outputDensities();
                break;
        }
    }

    public void outputDensities(){
        outlet(0, kickCount / maxKickCount);
        outlet(1, snareCount / maxSnareCount);
        outlet(2, tomsCount / maxTomsCount);
        outlet(3, hihatCount / maxHihatCount);
        outlet(4, rideCount / maxRideCount);
        double overall_density = ((kickCount / maxKickCount) + (snareCount / maxSnareCount) + (hihatCount / maxHihatCount))/3;
        outlet(5, overall_density);
    }

    public void resetCounter() {
        kickCount = 0;
        snareCount = 0;
        tomsCount = 0;
        hihatCount = 0;
        rideCount = 0;
    }
}
