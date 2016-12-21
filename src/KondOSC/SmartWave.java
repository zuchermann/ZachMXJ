package KondOSC;

/**
 * Created by yn on 12/20/16.
 */
public class SmartWave {
    private double[] period;
    private int index;
    public static final double TWO_PI = 2.0 * Math.PI;
    public static final double ERROR = -999;

    public SmartWave(double srate, double freq){
        int samples_per_Cycle =  Math.toIntExact(Math.round(Math.floor(srate / freq)));
        period = new double[samples_per_Cycle + 1];
        for(int i = 0; i < period.length; i ++){
            double phase = (double) i / (double) samples_per_Cycle;
            period[i] = Math.sin(phase * TWO_PI);
        }
        index = 0;
    }

    public double getNext(){
        if(index < period.length){
            double next = period[index];
            index++;
            return next;
        } else return ERROR;
    }

    public static void main(String[] args) {
        SmartWave myWave = new SmartWave(32, 4);
        double next = myWave.getNext();
        while(next != ERROR){
            System.out.println(next);
            next = myWave.getNext();
        }
        System.out.println(next);
    }

}
