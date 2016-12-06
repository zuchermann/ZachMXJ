package ListeningModes;
import PatternDetection.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by Hanoi on 12/6/16.
 */
public class ListenPatternChooseMotif {

    private ArrayList<String> fileNames = new ArrayList<String>();
    private MotifCollection myMotifs = new MotifCollection();


    public ListenPatternChooseMotif() throws IOException {

    }

    public String match_density_motif(double density){ // Takes the pattern name and current density
        String cue_motif = myMotifs.get_closest_density(density);
        return cue_motif;
    }

    public static void main(String[] i_love_my_car_and_i_love_my_wife_and_i_love_my_kids_too) throws IOException {
        ListenPatternChooseMotif myListener = new ListenPatternChooseMotif();
        System.out.println(myListener.match_density_motif(0.11));
    }
}
