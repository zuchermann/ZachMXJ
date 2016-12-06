package PatternDetection;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Hanoi on 12/5/16.
 */
public class CSVParser {
    public static String[][] parse(File file) throws IOException {
        ArrayList<String[]> result = new ArrayList<String[]>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        int lineCount = 0;
        while ((line = br.readLine()) != null) {
            result.add(line.split(","));
            lineCount++;
        }
        return result.toArray(new String[lineCount][]);
    }

    public static void printParsedCSV(String[][] parsed){
        for(String[] row : parsed) {
            System.out.println(Arrays.toString(row));
        }
    }

    public static void main(String[] big_boy_args_for_big_toys) throws IOException {
        File file = new File("src/PatternDetection/CSV/101Motifs.csv");
        printParsedCSV(parse(file));
    }
}
