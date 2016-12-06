package ListeningModes;
import PatternDetection.*;
import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by Hanoi on 12/6/16.
 */
public class ListenChooseMotif extends MaxObject{

    private int drum_pattern;
    private double density;
    private String dir;

    private ArrayList<String> fileNames = new ArrayList<String>();
    private MotifCollection myMotifs;

    public ListenChooseMotif() throws IOException {

        this.dir = this.getCodeSourcePath();
        int index = dir.lastIndexOf('/');
        dir = dir.substring(0,index);
        index = dir.lastIndexOf('/');
        dir = dir.substring(0,index);
        post("parsed csv from" + dir);

        myMotifs = new MotifCollection(dir);

        this.drum_pattern = -1;
        this.density = 0.5;

        createInfoOutlet(false);
        declareInlets(new int[]{DataTypes.ALL, DataTypes.ALL,  DataTypes.ALL, DataTypes.ALL});
        declareOutlets(new int[]{DataTypes.ALL});

        setInletAssist(new String[]{
                "Message functions",
                "Input Section - input drum pattern number",
                "Input Density - input float",
                "Make Decision - input bang"
        });
        setOutletAssist(new String[]{
                "outputs motif string"
        });

    }

    public void inlet(float val) {

        int intlet_no = getInlet();
        switch (intlet_no) {
            case 0:
                break;
            case 1:
                updateDrumPattern((int) val);
                break;
            case 2:
                updateDensity(val);
                break;
            default:
                post("INLET NOT SUPPORTED");
        }
    }

    private void updateDrumPattern(int val){
        this.drum_pattern = val;
    }

    private void updateDensity(double val){
        this.density = val;
    }


    public void matchDensity() {
        outlet(0, myMotifs.get_closest_density(this.density));
    }

    public void matchPatternDensity() {
        outlet(0, myMotifs.get_closest_pattern_and_density(this.drum_pattern, this.density));
    }

    /*
    public static void main(String[] i_love_my_car_and_i_love_my_wife_and_i_love_my_kids_too) throws IOException {
        ListenChooseMotif myListener = new ListenChooseMotif();
        System.out.println(myListener.match_density_motif(0.11));
    }
    */
}
