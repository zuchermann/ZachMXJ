package HanoiStuff;
import java.util.*;

/**
 * Created by Hanoi on 11/19/16.
 */

import com.cycling74.max.*;

public class ListenDrums extends MaxObject {

    private int kickCount = 0;
    private int snareCount = 0;
    private int tomsCount = 0;
    private int hihatCount = 0;
    private int rideCount = 0;
    ArrayList smoother = new ArrayList();


    public ListenDrums() {

        createInfoOutlet(false);
        declareInlets(new int[]{DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});
        declareOutlets(new int[]{DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});

        setInletAssist(new String[]{
                "Message functions",
                "Kick input",
                "Snare input",
                "Toms input",
                "Hihat input",
                "Ride input",
                "Force Decision"
        });
        setOutletAssist(new String[]{
                "x",
                "y",
                "z"
        });
    }

    public void bang() {
        int inlet_no;
        inlet_no = getInlet();
        switch (inlet_no) {
            case 1: //KICK counter
                kickCount = kickCount + 1;
                post("kickCount: " + kickCount + '\n');
                break;
            case 2: //SNARE counter
                snareCount = snareCount + 1;
                post("snareCount: " + snareCount + '\n');
                break;
            case 3: //TOMS counter
                tomsCount = tomsCount + 1;
                post("tomsCount: " + tomsCount + '\n');
                break;
            case 4: //HH counter
                hihatCount = hihatCount + 1;
                post("hihatCount: " + hihatCount + '\n');
                break;
            case 5: //RIDE counter
                rideCount = rideCount + 1;
                post("rideCount: " + rideCount + '\n');
                break;

        }
    }

    public void makeDecision() { //in Max, makeDecision() is called every beat
        post("Making a decision");
        int motif = decideMotif();
        outlet(0, motif);
    }

    private int decideMotif() {
        // Calculate the proportions of K, S, HH, T, R
        double[] prop = calcProportions();
        post("Proportions: " + Arrays.toString(prop) + '\n');

        if (prop[4] > 0) { // Ride being hit
            return 2;
        }
        else if (findMaxIndex(prop) == 3) { // lots of hihats
            return 7;
        }

        else if (prop[3] > 0.8) {
            return 7;
        }

        else if (prop[1] > prop[2]) { //more snares than toms
            return 4;
        }
        else if (prop[1] < prop[2]) { //more toms than snares
            return 5;
        }
        return 2;
    }

    private double[] calcProportions() {
        //declares an array of doubles, local to calcProportions()
        double[] proportions;
        //allocates memory for 5 doubles
        proportions = new double[5];
        //calculate total count
        double totalCount = kickCount + snareCount + tomsCount + hihatCount + rideCount;
        //update the values in the array
        proportions[0] = kickCount / totalCount;
        proportions[1] = snareCount / totalCount;
        proportions[2] = tomsCount / totalCount;
        proportions[3] = hihatCount / totalCount;
        proportions[4] = rideCount / totalCount;
        return proportions;
    }

    public void ride_reset() { //resets the ride counter, bang this every measure otherwise ride will always dominate the decision tree
        rideCount = 0;
    }

    public void reset() { //resets the counters of each drum element
        post("Counters reset \n");
        kickCount = 0;
        snareCount = 0;
        tomsCount = 0;
        hihatCount = 0;
        rideCount = 0;
    }

    private static double findMaxIndex(double array[])
    {
        double largest = array[0];
        double largestIndex = 0;

        for(int i = 0; i < array.length; i++)
        {
            if(array[i] > largest) {
                largest = array[i];
                largestIndex =i;
            }
        }

        return largestIndex;
    }


}


