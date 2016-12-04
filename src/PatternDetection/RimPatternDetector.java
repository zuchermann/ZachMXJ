package PatternDetection;

/**
 * Created by Hanoi on 12/3/16.
 */
public class RimPatternDetector {

    double[] rhythmVector;
    double bar_time;
    private int next_note_slot;

    public RimPatternDetector(int quantizationStep){
        this.rhythmVector = new double[quantizationStep];
        this.next_note_slot = -1;

        for(int i = 0; i < this.rhythmVector.length; i++){
            this.rhythmVector[i] = 0.0;
        }
    }

    private void newBarTime(double bar_time){
        this.bar_time = bar_time;
    }

    private void newEventTime(double event_time){
        double val;
        val = event_time % this.bar_time;
    }

    private void newMeasureTime(double time){
        if(next_note_slot != -1) {
            rhythmVector[0] = 1;
        }
    }


    private void printVector() {
        System.out.print("vector: ");
        for(int i = 0; i < this.rhythmVector.length; i++) {
            System.out.print(""+ this.rhythmVector[i] + " ");
        }
        System.out.println();
    }



    public static void main (String[] args) {
        RimPatternDetector myDetector = new RimPatternDetector(16);
        myDetector.newMeasureTime(220.0);

    }
}
