package KondOSC;

/**
 * Created by yn on 5/8/17.
 */
import com.cycling74.msp.MSPPerformer;
import com.cycling74.msp.MSPSignal;
import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;

import java.util.ArrayList;
import java.util.Arrays;

import static KondOSC.SmartWave.TWO_PI;

public class AutoCorr extends MSPPerformer {

    private double srate;
    private float[][] waves;
    private float[] inBuff;
    private int buff_w_index;
    private int buff_r_index;

    private static final String[] INLET_ASSIST = new String[]{
            "signal to be auto-correlated"
    };
    private static final String[] OUTLET_ASSIST = new String[]{
            "auto-correlated output (sig)"
    };

    public static float[] sineWave(double srate, int midi){
        double freq = 27.5 * Math.pow(2, ((midi - 21)/12));
        int samples_per_Cycle =  Math.toIntExact(Math.round(Math.floor(srate / freq)));
        float[] period = new float[samples_per_Cycle + 1];
        for(int i = 0; i < period.length; i ++){
            double phase = (double) i / (double) samples_per_Cycle;
            period[i] = (float) Math.sin(phase * TWO_PI);
        }
        return period;
    }


    public AutoCorr()
    {
        declareInlets(new int[]{SIGNAL});
        declareOutlets(new int[]{SIGNAL});

        setInletAssist(INLET_ASSIST);
        setOutletAssist(OUTLET_ASSIST);

        srate = 44100; //default
    }

    public void dspsetup(MSPSignal[] ins, MSPSignal[] outs)
    {
        //If you forget the fields of MSPSignal you can select the classname above
        //and choose Open Class Reference For Selected Class.. from the Java menu
        srate = ins[0].sr;
        waves = new float[128][];
        for(int i = 0; i < 128; i++){
            waves[i] = sineWave(srate, i);
        }
        inBuff = new float[waves[0].length + 1];
        Arrays.fill(inBuff, 0);
        buff_r_index = 0;
        buff_w_index = inBuff.length - 1;
    }

    void print(String msg, double [] x) {
        System.out.println(msg);
        for (double d : x) System.out.println(d);
    }

    /**
     * This is a "wrapped" signal processing-style autocorrelation.
     * For "true" autocorrelation, the data must be zero padded.
     */
    public void bruteForceAutoCorrelation(float [] x, float [] ac) {
        Arrays.fill(ac, 0);
        int n = x.length;
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                ac[j] += x[i] * x[(n + i - j) % n];
            }
        }
    }

    private void push(float newVal){
        
    }

    public float bruteForceMaxCorrelation(float [] fund, float[] sig) {
        int n = fund.length;
        float maxCorr = 0;
        for (int j = 0; j < n; j++) {
            float thisCorr = 0;
            for (int i = 0; i < n; i++) {
                thisCorr += sig[i] * fund[(n + i - j) % n];
            }
            maxCorr = maxCorr < thisCorr ? thisCorr : maxCorr;
        }
        return maxCorr;
    }

    private double sqr(double x) {
        return x * x;
    }

    public void fftAutoCorrelation(float [] x, float [] ac) {
        int n = x.length;
        // Assumes n is even.
        FloatFFT_1D fft = new FloatFFT_1D(n);
        fft.realForward(x);
        ac[0] = (float) sqr(x[0]);
        // ac[0] = 0;  // For statistical convention, zero out the mean
        ac[1] = (float) sqr(x[1]);
        for (int i = 2; i < n; i += 2) {
            ac[i] = (float) (sqr(x[i]) + sqr(x[i+1]));
            ac[i+1] = 0;
        }
        FloatFFT_1D ifft = new FloatFFT_1D(n);
        ifft.realInverse(ac, true);
        // For statistical convention, normalize by dividing through with variance
        //for (int i = 1; i < n; i++)
        //    ac[i] /= ac[0];
        //ac[0] = 1;
    }

    void test() {
        float [] data = { 1, -81, 2, -15, 8, 2, -9, 0};
        float [] ac1 = new float [data.length];
        float [] ac2 = new float [data.length];
        //bruteForceAutoCorrelation(data, ac1);
        fftAutoCorrelation(data, ac2);
        //print("bf", ac1);
        //print("fft", ac2);
        double err = 0;
        for (int i = 0; i < ac1.length; i++)
            err += sqr(ac1[i] - ac2[i]);
        System.out.println("err = " + err);
    }

    public static void main(String[] args) {
        new AutoCorr().test();
    }

    @Override
    public void perform(MSPSignal[] ins, MSPSignal[] outs) {
        float[] in = ins[0].vec;
        float[] out = outs[0].vec;
        float maxCorr = 0;
        int midiVal = 0;
        for(int i = 0; i < 128; i ++){
            float corr = this.bruteForceMaxCorrelation(waves[i], in);
            if(maxCorr < corr){
                maxCorr = corr;
                midiVal = i;
            }
        }
        Arrays.fill(out, midiVal);
        //this.bruteForceAutoCorrelation(in, out);
    }
}