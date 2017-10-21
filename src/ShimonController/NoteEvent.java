package ShimonController;

import java.util.Comparator;

/**
 * Created by yn on 9/28/17.
 */
public class NoteEvent{
    double scheduledTime;
    double deadline;
    int midiPitch;
    int midiVelocity;

    NoteEvent(int midiPitch, int midiVelocity, double delay){
        this.scheduledTime = System.currentTimeMillis();
        this.deadline = this.scheduledTime + delay;
        this.midiPitch = midiPitch;
        this.midiVelocity = midiVelocity;
    }

    public double[] getMidiData(){
        double[] result = null;
        double time = System.currentTimeMillis();
        double delay = deadline - time;
        if(delay > 0){
            result = new double[] {midiPitch, midiVelocity, delay};
        }
        return result;
    }

    public double getDeadline(){
        return deadline;
    }

}

