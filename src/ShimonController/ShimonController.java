package ShimonController;

import java.util.concurrent.TimeUnit;

/**
 * Created by yn on 3/27/17.
 */
public class ShimonController {
    public static void main(String[] args) throws InterruptedException {
        Shimon shimon = new Shimon();
        System.out.println(shimon);
        double time = System.currentTimeMillis();
        shimon.mididata(60, time);
        TimeUnit.SECONDS.sleep(1);
        time = System.currentTimeMillis();
        shimon.mididata(59, time);
    }
}
