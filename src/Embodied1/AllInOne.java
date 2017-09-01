package Embodied1;

import Interaction1.NGram;
import Interaction1.RhythmList;
import ShimonController.Shimon;
import com.cycling74.max.Atom;
import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

/**
 * Created by yn on 4/9/17.
 */
public class AllInOne extends MaxObject{
    private int rhythmNgramSize;
    private int pitchNgramSize;
    private NGram<String> rhythm;
    private NGram<String> pitch;
    private boolean isRhythmMostLikely;
    private boolean isPitchMostLikely;
    private Stack<String> rhythmQueue;
    private Stack<String> pitchQueue;
    private int accuracy; //defines decimal precision
    private Shimon shimon;
    private RhythmList rhythmList;
    private long lastTime;
    private long thisTime;
    private static final boolean IS_DEBUGGING = false;
    private static final int DEFAULT_RHYTHM_NGRAM_SIZE = 32;
    private static final int DEFAULT_PITCH_NGRAM_SIZE = 16;
    private static final long MAX_TIME = 5000;
    public static final int NUM_OF_ARMS = 4;
    public static final double DELAY_OFFSET = 50;

    public AllInOne() {
        this(DEFAULT_RHYTHM_NGRAM_SIZE, DEFAULT_PITCH_NGRAM_SIZE);
    }

    public AllInOne(int rhythmNgramSize, int pitchNgramSize) {
        Class nGramClass = "".getClass();
        this.rhythmNgramSize = rhythmNgramSize;
        this.pitchNgramSize = pitchNgramSize;
        this.rhythm = new NGram<String>(nGramClass);
        this.pitch = new NGram<String>(nGramClass);
        this.rhythmQueue = new Stack<String>();
        this.pitchQueue = new Stack<String>();
        this.isRhythmMostLikely = false;
        this.isPitchMostLikely = true;
        this.thisTime = System.currentTimeMillis();;
        this.lastTime = thisTime;
        this.accuracy = 1000;
        this.shimon = new Shimon();
        this.rhythmList = new RhythmList(1);

        createInfoOutlet(false);

        declareInlets(new int[]{ DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL,
                DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL });
        declareOutlets(new int[]{ DataTypes.ALL, DataTypes.ALL,
                DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});

        setInletAssist(new String[] {
                "CONTROL: hit (pitch, vel, delay) after 'delay' ms",
                "CONTROL: (arm index[0-3], pitch, vel, delay)",
                "NGRAM: (pitches, vel) predict stuff",
                "0 or 1 sets is probabilistic(0) or most likely(1) rhythm",
                "0 or 1 sets is probabilistic(0) or most likely(1) pitch",
                "set rhythm ngram size",
                "set pitch ngram size",
                "bang -> home",
                "bang -> report arm positions"
        });
        setOutletAssist(new String[] {
                "location of arm 1",
                "location of arm 2",
                "location of arm 3",
                "location of arm 4",
                "messages to arms",
                "messages to head"
        });
    }

    private void debug(String message){
        if (IS_DEBUGGING) post("[mxj Embodied1.AllInOne]: " + message);
    }

    private void err(String message){
        post("ERROR from [mxj Embodied1.AllInOne]: " + message);
    }

    private String arrToStr(String[] arr){
        String result = "";
        for (int i = 0; i < arr.length - 1; i++){
            result = result + arr[i] + " ";
        }
        result = result + arr[arr.length - 1];
        return result;
    }

    private void ngramInsert(String[] notes, int vel) {
        //getRhythm
        double time = thisTime - lastTime;
        rhythmList.inlet((float) time);
        double timeProp = rhythmList.getRhythmList()[0];
        if(timeProp > 0.d) {
            this.rhythmQueue.add(Double.toString(timeProp));
            while (rhythmQueue.size() > rhythmNgramSize) {
                rhythmQueue.remove(0);
            }
            rhythm.insert(rhythmQueue);
        }
        this.pitchQueue.add(arrToStr(notes));
        while (pitchQueue.size() > pitchNgramSize) {
            pitchQueue.remove(0);
        }
        pitch.insert(pitchQueue);
        //outlet(0, valueQueue.toString());
    }

    private int[] atomsToInts(Atom[] arr){
        int[] result = new int[arr.length];
        for (int i = 0; i < arr.length; i++){
            result[i] = arr[i].getInt();
        }
        return result;
    }

    private ArrayList<String> ngramPredict(String[] notes, int vel){
        double deltaTime = thisTime - lastTime;
        if(deltaTime < MAX_TIME) {
            List<String> possibleRhythms =
                    (isRhythmMostLikely ? rhythm.getAllSorted(rhythmQueue) : rhythm.getAllShuffled(rhythmQueue));
            List<String> possiblePitches =
                    (isPitchMostLikely ? pitch.getAllSorted(pitchQueue) : pitch.getAllShuffled(pitchQueue));
            for (int rhythmIndex = 0; rhythmIndex < possibleRhythms.size(); rhythmIndex++) {
                double delay = Math.max(0.,
                        (deltaTime * Double.parseDouble(possibleRhythms.get(rhythmIndex))) - DELAY_OFFSET);
                for (int pitchIndex = 0; pitchIndex < possiblePitches.size(); pitchIndex++) {
                    int[] pitches = atomsToInts(Atom.parse(possiblePitches.get(pitchIndex)));
                    ArrayList<String> serialMessages = new ArrayList<>();
                    for(int i = 0; i < pitches.length; i++){
                        int thisPitch = pitches[i];
                        String serial = null;
                        if(delay > 0.d) delay -= Math.max(0.d, (System.currentTimeMillis() - thisTime));
                        serial = shimon.scheduleIfPossible(thisPitch, vel, thisTime, delay);
                        if(serial != null){
                            serialMessages.add(serial);
                        }
                    }
                    if (serialMessages.size() > 0) return serialMessages;
                }
            }
        }
        return null;
    }

    private void home() throws InterruptedException {
        outletHigh(NUM_OF_ARMS, Atom.parse(shimon.home(0)));
        TimeUnit.SECONDS.sleep(1);
        outletHigh(NUM_OF_ARMS, Atom.parse(shimon.home(3)));
        TimeUnit.SECONDS.sleep(1);
        outletHigh(NUM_OF_ARMS, Atom.parse(shimon.home(1)));
        TimeUnit.SECONDS.sleep(1);
        outletHigh(NUM_OF_ARMS, Atom.parse(shimon.home(2)));
        TimeUnit.SECONDS.sleep(1);
    }

    private void refreshArmPositions(){
        double time = System.currentTimeMillis();
        for(int i = 0; i < NUM_OF_ARMS; i++){
            outlet(i, shimon.getArmMidi(i, time));
        }
    }

    private int wrapPitch(int pitch){
        int result = pitch;
        while (result < Shimon.LOWEST_NOTE){
            result += 12;
        }
        while (result > shimon.HIGHEST_NOTE){
            result -= 12;
        }
        return result;
    }
    public void list(Atom[] args){
        int inlet_num = getInlet();
        if(inlet_num == 0){
            //(pitch, velocity, delay) note to be played by Shimon after 'delay' number of ms
            debug("playing note (pitch, velocity, delay) = " + Atom.toOneString(args));
            double time = System.currentTimeMillis();
            shimon.mididata(args[0].toInt(), args[1].toInt(), time, args[2].toDouble());
        } else if(inlet_num == 1){
            //(arm index[0-3], pitch, velocity, delay) note to be played by arm at 'arm index'
            // after" 'delay' number of ms
            double time = System.currentTimeMillis();
            shimon.controlArm(args[0].toInt(), args[1].toInt(), args[2].toInt(), time, args[3].toDouble());
            debug("playing note (arm, pitch, velocity, delay) = " + Atom.toOneString(args));
        } else if(inlet_num == 2){
            //(pitches, velocity) input to be listened to by NGram."
            thisTime = System.currentTimeMillis();

            String[] pitches = new String[args.length - 1];
            for(int i = 0; i < args.length - 1; i ++){
                int pitch = wrapPitch(args[i].toInt());
                pitches[i] = Integer.toString(pitch);
            }

            ngramInsert(pitches, args[args.length - 1].toInt());
            ArrayList<String> serialMessages = ngramPredict(pitches, args[1].toInt());
            if (serialMessages != null) {
                for(int i = 0; i < serialMessages.size(); i ++) {
                    outletHigh(NUM_OF_ARMS, Atom.parse(serialMessages.get(i)));
                }
            }

            lastTime = thisTime;
            debug("inserting note into ngrams (pitch, velocity) = " + Atom.toOneString(args));
        } else err("this inlet does not understand list messages!");
    }

    public void inlet(int arg){
        int inlet_num = getInlet();
        if(inlet_num == 3){
            //0 or 1 sets whether rhythm is probabilistic(0) or most likely(1)
            debug("Set to predict " + (arg == 1 ? "most likely " : "probable ") + "rhythm.");
            this.isRhythmMostLikely = arg == 1;
        } else if(inlet_num == 4){
            //0 or 1 sets whether pitch is probabilistic(0) or most likely(1)
            debug("Set to predict " + (arg == 1 ? "most likely " : "probable ") + "pitch.");
            this.isPitchMostLikely = arg == 1;
        } else if(inlet_num == 5){
            //set ngram size
            debug("Set rhythm ngram size to " + Integer.toString(arg));
            this.rhythmNgramSize = arg;
        }
        else if(inlet_num == 6){
            //set ngram size
            debug("Set pitch ngram size to " + Integer.toString(arg));
            this.pitchNgramSize = arg;
        } else err("this inlet does not understand int messages!");
    }

    public void bang(){
        int inlet_num = getInlet();
        if(inlet_num == 7){
            //bang to home shimon
            debug("homing...... ");
            try {
                home();
            } catch (InterruptedException e){
                err(e.getStackTrace().toString());
            }
            debug("done homing! ");
        } else if(inlet_num == 8){
            //bang to report arm positions from outlets
            refreshArmPositions();
        } else err("this inlet does not understand bang messages!");
    }
}
