package KondOSC;

/**
 * Created by yn on 12/20/16.
 */

import com.cycling74.max.*;
import com.cycling74.msp.*;

import java.util.ArrayList;
import java.util.List;

public class KondOSC extends MSPPerformer
{

    private double srate;
    private double lastVal;
    private static final double minDiff = 0.5;
    private ArrayList<SmartWave> oscList;

    private static final String[] INLET_ASSIST = new String[]{
            "frequency",
            "drive from phasor"
    };
    private static final String[] OUTLET_ASSIST = new String[]{
            "output (sig)"
    };


    public KondOSC(float gain)
    {
        declareInlets(new int[]{SIGNAL, SIGNAL});
        declareOutlets(new int[]{SIGNAL});

        setInletAssist(INLET_ASSIST);
        setOutletAssist(OUTLET_ASSIST);

        srate = 44100; //default
        lastVal = 0;
        oscList = new ArrayList<SmartWave>();
    }

    public void dspsetup(MSPSignal[] ins, MSPSignal[] outs)
    {
        //If you forget the fields of MSPSignal you can select the classname above
        //and choose Open Class Reference For Selected Class.. from the Java menu
        srate = ins[0].sr;
        lastVal = 0;
        oscList = new ArrayList<SmartWave>();
    }

    private boolean isTrig(double first, double second){
        return Math.abs(first-second) > minDiff;
    }

    public void perform(MSPSignal[] ins, MSPSignal[] outs)
    {
        int i;
        float[] grain_freq = ins[0].vec;
        float[] control_sig = ins[1].vec;
        float[] out = outs[0].vec;
        for(i = 0; i < grain_freq.length;i++)
        {
            boolean addToWave = false;

            //find out if we should trigger new wave
            if(i == 0){
                //first samp in buff
                addToWave = isTrig(control_sig[i], lastVal);
            } else {
                addToWave = isTrig(control_sig[i], control_sig[i - 1]);
                if(i == grain_freq.length - 1){
                    //last samp in block
                    lastVal = control_sig[i];
                }
            }
            if(addToWave) {
                oscList.add(new SmartWave(srate, grain_freq[i]));
            }

            double nextSamp = 0;
            List toRemove = new ArrayList<SmartWave>();
            for(SmartWave sw : oscList){
                double newVal = sw.getNext();
                if(newVal == SmartWave.ERROR){
                    toRemove.add(sw);
                } else {
                    nextSamp += newVal;
                }
            }
            oscList.removeAll(toRemove);
			/*do something*/
            out[i] = (float) nextSamp;

        }
    }
}


