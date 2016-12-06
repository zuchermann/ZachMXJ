package PatternDetection;

import java.io.File;
import java.io.IOException;

/**
 * Created by Hanoi on 12/5/16.
 */
public class MotifCollection {
    Motif[] motifs;

    public MotifCollection() throws IOException {
        File file = new File("src/PatternDetection/CSV/101Motifs.csv");
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
}
