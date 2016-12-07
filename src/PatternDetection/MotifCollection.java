package PatternDetection;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Hanoi on 12/5/16.
 */
public class MotifCollection {
    Motif[] motifs;

    public MotifCollection(String dir) throws IOException {
        //File file = new File("src/PatternDetection/CSV/101Motifs.csv");
        File file = new File(dir + "/PatternDetection/CSV/101Motifs.csv");
        String[][] motifArrays = CSVParser.parse(file);
        String[] propertyNames = motifArrays[0];
        this.motifs = new Motif[motifArrays.length - 1];
        for(int i = 1; i < motifArrays.length; i++){
            this.motifs[i - 1] = new Motif(propertyNames, motifArrays[i]);
        }
    }

    public void dumpCollection(){
        for(Motif motif: this.motifs){
            System.out.println(motif);
        }
    }

    public String get_closest_density(double density){
        String target_motif = null;
        double closest_density = 2000.0; // big boy
        for (int i = 0; i < motifs.length; i++){
            double dens = motifs[i].getProperty("density");
            if (Math.abs(dens - density) < closest_density) {
                closest_density = Math.abs(dens - density);
                target_motif = motifs[i].getName();
            }
        }
        return target_motif;
    }

    public String get_closest_density_of_selected_motifs(Motif[] selected_motifs, double density){
        String target_motif = null;
        double closest_density = 2000.0; // big boy
        for (int i = 0; i < selected_motifs.length; i++){
            double dens = selected_motifs[i].getProperty("density");
            if (Math.abs(dens - density) < closest_density) {
                closest_density = Math.abs(dens - density);
                target_motif = selected_motifs[i].getName();
            }
        }
        return target_motif;
    }

    public String get_closest_pattern_and_density(int drum_pattern, double density){
        Motif[] selected_motifs;
        String target_motif = null;
        switch (drum_pattern) {
            case 0:
                selected_motifs = selectMotifType("poly");
                target_motif = get_closest_density_of_selected_motifs(selected_motifs,density);
                break;
            case 1:
                selected_motifs = selectMotifType("guit");
                target_motif = get_closest_density_of_selected_motifs(selected_motifs,density);
                break;
            case 2:
                selected_motifs = selectMotifType("solo");
                target_motif = get_closest_density_of_selected_motifs(selected_motifs,density);
                break;
            case 3:
                selected_motifs = selectMotifType("chor");
                target_motif = get_closest_density_of_selected_motifs(selected_motifs,density);
                break;
            case 4:
                selected_motifs = selectMotifType("solo");
                target_motif = get_closest_density_of_selected_motifs(selected_motifs,density);
                break;
            case 5:
                selected_motifs = selectMotifType("ost");
                target_motif = get_closest_density_of_selected_motifs(selected_motifs,density);
                break;
            default:
                target_motif = "bass_1";
        }
        return target_motif;
    }

    public Motif[] selectMotifType(String motif_type) {
        Motif[] selected_motifs;
        ArrayList<Motif> motifsList = new ArrayList<Motif>();
        for (int i = 0; i < motifs.length; i++) {
            String name = motifs[i].getName();
            if (name.startsWith(motif_type)) {
                motifsList.add(motifs[i]);
            }
        }
        selected_motifs = motifsList.toArray(new Motif[motifsList.size()]);
        return selected_motifs;
    }


    /*
    public static void main(String[] i_love_my_car_and_i_love_my_wife_and_i_love_my_kids_too) throws IOException {
        MotifCollection myMotifs = new MotifCollection();
        myMotifs.dumpCollection();
        System.out.println(
                "Motif "
                        + myMotifs.motifs[0].getName()
                        + " has major minor tonality = "
                        + myMotifs.motifs[0].getProperty("major minor tonality"));
        System.out.println("There are " + myMotifs.motifs.length + " motifs");
    }
    */
}
