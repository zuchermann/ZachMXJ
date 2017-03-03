
import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;

/**
 * Created by yn on 3/3/17.
 *
 * Will output the java version being used
 */
public class JavaVersionTester extends MaxObject {
    JavaVersionTester() {
        createInfoOutlet(false);
        declareInlets(new int[]{ DataTypes.ALL});
        setInletAssist(new String[] {
                "bang to output java version",
        });
    }

    public void bang() {
        String version = System.getProperty("java.version");
        post(version);
    }
}
