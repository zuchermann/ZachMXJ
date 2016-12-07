package ListeningModes;

import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;

import java.util.ArrayList;

/**
 * Created by Hanoi on 12/6/16.
 */

// This function needs a counter to bang the max values of each of the drum components

public class ListenDrumDensity extends MaxObject {
    private double kickCount = 0;
    private double snareCount = 0;
    private double tomsCount = 0;
    private double hihatCount = 0;
    private double rideCount = 0;
    private double maxKickCount = 1;
    private double maxSnareCount = 1;
    private double maxTomsCount = 1;
    private double maxHihatCount = 1;
    private double maxRideCount = 1;
    ArrayList smoother = new ArrayList();


    public ListenDrumDensity(double maxKick, double maxSnare, double maxToms, double maxHihat, double maxRide) {

        maxKickCount = maxKick;
        maxSnareCount = maxSnare;
        maxTomsCount = maxToms;
        maxHihatCount = maxHihat;
        maxRideCount = maxRide;

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
        outputDensities(); //for every bang message update the densities

        int inlet_no;
        inlet_no = getInlet();
        switch (inlet_no) {
            case 1: //KICK counter
                kickCount = kickCount + 1.0;
                //post("kickCount: " + kickCount + '\n');
                break;
            case 2: //SNARE counter
                snareCount = snareCount + 1.0;
                //post("snareCount: " + snareCount + '\n');
                break;
            case 3: //TOMS counter
                tomsCount = tomsCount + 1.0;
                //post("tomsCount: " + tomsCount + '\n');
                break;
            case 4: //HH counter
                hihatCount = hihatCount + 1.0;
                //post("hihatCount: " + hihatCount + '\n');
                break;
            case 5: //RIDE counter
                rideCount = rideCount + 1.0;
                //post("rideCount: " + rideCount + '\n');
                break;
            case 11: //Query density
                outputDensities();
                break;
        }
    }
    public void inlet(float val) {

        //inlet 0: number curent beat
        //inlet 1: number or symbolcurrent motif
        //inlet 2: number current tempo
        //inlet 3: 0 or 1 play/don't play

        int intlet_no = getInlet();
        switch(intlet_no) {
            case 6:
                maxKickCount = val;
                post("maxKickCount " + maxKickCount);
                break;
            case 7:
                maxSnareCount = val;
                post("maxSnareCount " + maxSnareCount);
                break;
            case 8:
                maxTomsCount = val;
                post("maxTomsCount " + maxTomsCount);
                break;
            case 9:
                maxHihatCount = val;
                post("maxHihatCount " + maxHihatCount);
                break;
            case 10:
                maxRideCount = val;
                post("maxRideCount " + maxRideCount);
                break;
            default:
                post("INLET NOT SUPPORTED");
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
