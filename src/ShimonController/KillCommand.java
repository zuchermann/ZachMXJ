package ShimonController;

/**
 * Created by yn on 11/27/17.
 */
public class KillCommand implements ActionCommand{

    @Override
    public boolean isDone(double time) {
        return false;
    }

    @Override
    public boolean schedule(double time) {
        return false;
    }
}
