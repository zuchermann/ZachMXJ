package MotifDetection;

/**
 * Created by yn on 6/19/18.
 *
 * This class is a representation of a note. We only consider note-ons since this is all Shimon deals with.
 * Maybe consider expanding to consider note-offs in the future if detection too trigger-happy, ya know?
 */
public class Note {
    private double pitch;
    private double time;

    Note(double pitch, double time){
        this.pitch = pitch;
        this.time = time;
    }

    public double getPitch() {
        return pitch;
    }

    public double getTime() {
        return time;
    }

    public String toString(){
        return "[Note: " + pitch + " " + time + "ms]";
    }
}
