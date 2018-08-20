package MotifDetection;

import com.cycling74.max.Atom;

import java.util.ArrayList;

/**
 * Created by yn on 7/2/18.
 *
 * Utility class with static methods ONLY PLEASE. Processes and returns motifs motifs, NOT IN PLACE PLEASE.
 *
 * Works with motifs in the form of pitch/inter-onset lists as Max/MSP Atom arrays
 */
public class MotifProcessing {

    //turns input Atom array into an ArrayList of doubles
    public static ArrayList<Double> toDoubleList(Atom[] atomArray){
        ArrayList<Double> result = new ArrayList<>();
        for(Atom a : atomArray){
            result.add(a.toDouble());
        }
        return result;
    }

    //same as toDoubleList, but reversed

    public static Atom[] toAtomArray(ArrayList<Double> doubleList){
        Atom[] atomArray = new Atom[doubleList.size()];
        for(int i = 0; i < doubleList.size(); i++){
            atomArray[i] = Atom.newAtom(doubleList.get(i));
        }
        return atomArray;
    }

    //rotates a motif array around to account for delay of being sent to the robot. for instance if notes occurred
    //before the delay amount, will shift these notes around so that the motif begins playing in time with the player.
    //returns a 2-d array consisting of the adjusted pitches array and the rhythms array.
    //accounts for new first note not perfectly landing on delay amount by alse returning the number
    //of milliseconds you should delay the initial playback.
    public static Atom[][] accountForDelay(Atom[] pitches, Atom[] rhythms, double delay){
        ArrayList<Double> pitchesList = toDoubleList(pitches);
        ArrayList<Double> rhythmsList = toDoubleList(rhythms);
        double time_accumulation = 0.0; //accumules inter-onset intervals to determine at white time each note occurs.

        //iterate over each note and shit around to the back if time is less than delay
        ArrayList<Double> shiftedPitches = new ArrayList<>();
        ArrayList<Double> shiftedRhythms = new ArrayList<>();
        ArrayList<Double> endPitches = new ArrayList<>();
        ArrayList<Double> endRhythms = new ArrayList<>();
        boolean new_first = true;
        double start_delay = 0.;
        for(int i = 0; i < pitchesList.size(); i++){
            if(time_accumulation < delay){
                endPitches.add(pitchesList.get(i));
                endRhythms.add(rhythmsList.get(i));
            } else {
                if(new_first){
                    start_delay = time_accumulation - delay;
                    new_first = false;
                }
                shiftedPitches.add(pitchesList.get(i));
                shiftedRhythms.add(rhythmsList.get(i));
            }
            time_accumulation += rhythmsList.get(i);
        }
        int num_notes_shifted = shiftedPitches.size();
        Atom[] start_index = new Atom[] {Atom.newAtom(num_notes_shifted)};
        shiftedPitches.addAll(endPitches);
        shiftedRhythms.addAll(endRhythms);
        Atom[] start_delay_atom = new Atom[] {Atom.newAtom(start_delay)};
        return new Atom[][] {toAtomArray(shiftedPitches), toAtomArray(shiftedRhythms), start_delay_atom, start_index};
    }

    public static void main(String[] args){
        ArrayList<Double> pitches = new ArrayList<>();
        ArrayList<Double> rhythms = new ArrayList<>();
        pitches.add(1.);
        pitches.add(2.);
        pitches.add(3.);
        pitches.add(4.);
        pitches.add(5.);
        pitches.add(6.);
        pitches.add(7.);
        pitches.add(8.);

        rhythms.add(100.);
        rhythms.add(50.);
        rhythms.add(200.);
        rhythms.add(20.);
        rhythms.add(110.);
        rhythms.add(30.);
        rhythms.add(3.);
        rhythms.add(310.);

        Atom[][] shifted = accountForDelay(toAtomArray(pitches), toAtomArray(rhythms), 500.);
        Atom[] shiftedPitches = shifted[0];
        Atom[] shiftedRhythms = shifted[1];
        Atom start_delay = shifted[2][0];
        Atom start_note = shifted[3][0];
        System.out.print("pitches: ");
        for(Atom a : shiftedPitches){
            System.out.print(a + " ");
        }
        System.out.println();

        System.out.print("rhythms: ");
        for(Atom a : shiftedRhythms){
            System.out.print(a + " ");
        }
        System.out.println();

        System.out.println("start delay: " + start_delay);
        System.out.println("start note index: " + start_note);
    }
}
