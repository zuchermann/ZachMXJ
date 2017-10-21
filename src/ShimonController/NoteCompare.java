package ShimonController;

import java.util.Comparator;

public class NoteCompare implements Comparator<NoteEvent> {
    @Override
    public int compare(NoteEvent o1, NoteEvent o2) {
        if (o1.getDeadline() < o2.getDeadline()){
            return  -1;
        } else if (o1.getDeadline() > o2.getDeadline()) {
            return  1;
        }
        return 0;
    }
}
