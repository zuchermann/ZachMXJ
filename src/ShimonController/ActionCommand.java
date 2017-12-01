package ShimonController;

/**
 * Created by yn on 11/27/17.
 */
public interface ActionCommand {
    public boolean isDone(double time);
    public boolean schedule(double time);
}
