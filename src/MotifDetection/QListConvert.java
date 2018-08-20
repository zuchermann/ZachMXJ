package MotifDetection;

import com.cycling74.max.Atom;
import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;

/**
 * Created by yn on 7/2/18.
 *
 * class takes in two lists, one representing a all the pitches in a motif, the other represents the inter-onset
 * intervals between each pitch (specified in milliseconds). For an example of what these lists will look like,
 * consult the getPitchesAtom and getRhythmsAtom methods in the motif class.
 */
public class QListConvert extends MaxObject{
    private double shimon_delay = 500; // delay present for shimon to play a note, specified in ms
    private Atom[] inter_onset_intervals;

    QListConvert() {
        declareInlets(new int[]{DataTypes.LIST, DataTypes.LIST});
        declareOutlets(new int[]{DataTypes.ALL});
        setInletAssist(new String[]{
                "name of qlist followed by a list a pitches of motif (midi-pitch), produced by Max1 MXJ object",
                "list: inter-onset intervals of motif (ms), produced by Max1 MXJ object"
        });
        setOutletAssist(new String[]{
                "messages to qlist",
        });
    }

    public void list(Atom[] args) {
        int inlet = getInlet();
        if(inlet == 0){
            generateQlist(args);
        } else if(inlet == 1){
            inter_onset_intervals = args;
        }
    }

    private void generateQlist(Atom[] tagged_notes){
        if (inter_onset_intervals == null) {
            throw new AssertionError("QListConvert error: not given a list of inter-onet intervals");
        } else if(tagged_notes.length != inter_onset_intervals.length + 1){
            throw new AssertionError("QListConvert error: expects lists of notes and inter-onset intervals to be " +
                    "the same length, instead got " + (tagged_notes.length - 1) + " notes and " +
                    inter_onset_intervals.length + " inter-onset intervals");
        } else{
            //separate tag from note list
            Atom tag = tagged_notes[0];
            Atom[] notes = new Atom[tagged_notes.length - 1];
            for(int i = 1; i < tagged_notes.length; i++){
                notes[i-1] = tagged_notes[i];
            }

            //account for shimon delay
            Atom[][] sifted_motif = MotifProcessing.accountForDelay(notes, inter_onset_intervals, shimon_delay);
            Atom[] shiftedPitches = sifted_motif[0];
            Atom[] shiftedRhythms = sifted_motif[1];
            Atom start_delay = sifted_motif[2][0];
            int original_start_index = sifted_motif[3][0].toInt();
            double delay = 0.;
            for(int i = 0; i < shiftedPitches.length; i++) {
                Atom pitch = shiftedPitches[i];
                Atom[] msg = new Atom[]{Atom.newAtom("insert"), Atom.newAtom(delay), tag, shiftedPitches[i]};
                if (i == original_start_index){
                    msg = new Atom[]{Atom.newAtom("insert"), Atom.newAtom(delay), tag, Atom.newAtom("start"),
                            shiftedPitches[i]};
                }
                outlet(0, msg);
                delay = shiftedRhythms[i].toDouble();
            }
            outlet(0, new Atom[] {Atom.newAtom("insert"), Atom.newAtom(delay)});
            outlet(0, new Atom[] {Atom.newAtom("/start_delay"), start_delay});
        }
    }
}
