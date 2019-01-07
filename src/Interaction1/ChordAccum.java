package Interaction1;

import com.cycling74.max.Atom;
import com.cycling74.max.MaxObject;

import java.util.ArrayList;

/**
 * Created by yn on 12/27/18.
 * listens for notes, outputs list of all notes after all have been released.
 * used for step sequencer.
 */
public class ChordAccum extends MaxObject{
    ArrayList<int[]> all_notes = new ArrayList<>();
    ArrayList<int[]> held_notes = new ArrayList<>();

    public void notein(int pitch, int velocity, int chan){
        int[] cur_note = new int[]{pitch, velocity, chan};
        if(velocity > 0){
            if(!check(all_notes, cur_note)){
                all_notes.add(cur_note);
            }
            if(!check(held_notes, cur_note)){
                held_notes.add(cur_note);
            }
        } else {
            if(check(held_notes, cur_note)){
                rem(held_notes, cur_note);
            }
        }
        if(held_notes.size() == 0){
            outlet(0, "notes");
            for(int[] i : all_notes){
                outlet(0, i);
            }
            outlet(0, "done");
            all_notes = new ArrayList<>();
            held_notes = new ArrayList<>();
        }
    }

    public void clear(){
        all_notes = new ArrayList<>();
        held_notes = new ArrayList<>();
    }

    private void rem(ArrayList<int[]> notelist, int[] n){
        int[] delete_note = null;
        for(int[] i : notelist){
            boolean same = n[0] == i[0] && n[2] == i[2];
            if(same){
                delete_note = i;
                break;
            }
        }
        notelist.remove(delete_note);
    }

    private boolean check(ArrayList<int[]> notelist, int[] n){
        boolean same = false;
        for(int[] i : notelist){
            same = n[0] == i[0] && n[2] == i[2];
            if(same){
                break;
            }
        }
        return same;
    }
}
