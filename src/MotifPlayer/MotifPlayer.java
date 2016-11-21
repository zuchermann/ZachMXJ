package MotifPlayer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


/**
 * Created by yn on 11/17/16.
 */
import com.cycling74.max.*;

import javax.sound.midi.InvalidMidiDataException;

public class MotifPlayer extends MaxObject {

    private double tempo;
    private String dir;
    private JSONObject motif;
    private JSONArray motifNames;
    private JSONObject motifs;
    private boolean shouldOutput;
    private JSONArray beatEventList;
    private int beatEventIndex;
    private double lastDelay;

    public MotifPlayer() throws IOException {
        this(120);
    }

    public MotifPlayer(double tempo) throws IOException {
        //this.dir = this.getParentPatcher().getPath();
        this.dir = this.getCodeSourcePath();
        int index = dir.lastIndexOf('/');
        dir = dir.substring(0,index);
        this.tempo = tempo;
        this.shouldOutput = true;
        this.beatEventIndex = 0;
        this.lastDelay = 0;

        //load the json file
        JSONObject motifsJSON = new JSONObject();
        try {
            /*post("now parsing motifs...");
            MotifParser.parse(dir);
            post("now loading motifs from " + dir + "/motifs.json \n");
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(dir + "/motifs.json"));*/
            post("now parsing motifs...");
            motifsJSON = MotifParser.parse(dir);
            PrintWriter out = new PrintWriter(dir + "/motifs.json");
            motifsJSON.writeJSONString(out);
            //post((String) ((JSONArray) motifsJSON.get("motifNames")).get(0));
        } catch (FileNotFoundException e) {
            bail(e.getMessage());
        } catch (IOException e) {
            bail(e.getMessage());
        }catch (InvalidMidiDataException e) {
            bail(e.getMessage());
        }

        this.motifNames = (JSONArray) motifsJSON.get("motifNames");
        this.motifs = (JSONObject) motifsJSON.get("motifs");
        this.motif = (JSONObject) motifs.get(motifNames.get(0));

        createInfoOutlet(false);

        declareInlets(new int[]{ DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});
        declareOutlets(new int[]{ DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});

        setInletAssist(new String[] {
                "number current beat",
                "number - motif",
                "number current tempo",
                "0 or 1 play/don't play"
        });
        setOutletAssist(new String[] { "go directly to leftmost inlet of qlist",
                "name of playing motif",
                "go into bang into leftmost inlet of qlist"
        });
    }


    //just for testing
    public void list(Atom[] args) {
        int intlet_no;
        intlet_no = getInlet();
        outlet(1, args);
    }

    public void bang() {
        outlet(0, "zzzzzz!");
    }

    //setters
    private void setMotif(int val) {
        motif = (JSONObject) motifs.get(motifNames.get(val));
        outlet(1, (String) motifNames.get(val));
    }

    private void setTempo(double val) {
        tempo = val;
        post("new tempo: "+val+"bpm\n");
    }

    private void toggleOutput(double val) {
        shouldOutput = val > 0;
    }

    //convert beat offset to milliseconds
    private double offsetToMs(double offset) {
        double beatsPerSecond = tempo* 1.0 / 60.0;
        double secondsPerBeat = 1.0/(beatsPerSecond* 1.0);
        double msPerBeat = secondsPerBeat * 1000.0;
        return (offset * msPerBeat);
    }

    //function schedules next beat to be played
    private void playBeat(int beat) {
        int length = motif.size();
        //post(beat.toString() + "\n");
        JSONArray currentBeat = (JSONArray) motif.get("" + (beat % length));
        if(currentBeat != null && currentBeat.size() > 0){
            outlet(0, "clear");
            //tsk.cancel(); // cancel the previous beat, if still going

            double firstDelay = offsetToMs((Double)(((JSONObject)currentBeat.get(0)).get("offset")));
            lastDelay = (Double)((JSONObject)currentBeat.get(0)).get("offset");
            beatEventList = currentBeat;
            beatEventIndex = 0;
            double lastOffset = 0;
            //tsk.interval = firstDelay; // set the initial task interval
            //tsk.repeat(); // start the playing
            for(Object beatEvent : beatEventList) {
                JSONArray eventData = (JSONArray) ((JSONObject) beatEvent).get("controlList");
                double eventOffset = ((Double) ((JSONObject) beatEvent).get("offset"));
                double nextEvent = offsetToMs(eventOffset - lastOffset);
                outlet(0, flatten("insert", nextEvent, "otto", eventData));
                lastOffset = eventOffset;
            }
            outlet(2, "bang");
        }

    }

    private Atom[] flatten(String insert, double nextEvent, String otto, JSONArray eventData) {
        Atom[] result = new Atom[eventData.size() + 3];
        result[0] = Atom.newAtom(insert);
        result[1] = Atom.newAtom(nextEvent);
        result[2] = Atom.newAtom(otto);
        for(int i = 0; i < eventData.size(); i++){
            result[i + 3] = Atom.newAtom((Integer) eventData.get(i));
        }
        return result;
    }

   /* function beatScheduler() {
        //post(beatEventIndex);
        var nextEvent = beatEventList[beatEventIndex].constrolList;
        outlet(0, nextEvent);
        post(beatEventList[beatEventIndex].offset + "\n");
        beatEventIndex++;
        if(beatEventIndex < beatEventList.length){
            var delay = offsetToMs(beatEventList[beatEventIndex].offset - lastDelay);
            post(offsetToMs(beatEventList[beatEventIndex].offset - lastDelay) + "\n");
            lastDelay = beatEventList[beatEventIndex].offset;
            arguments.callee.task.interval = delay;
        } else{
            arguments.callee.task.cancel();
        }
        //post("go");
        //
    }
//beatScheduler.local = 1;*/

    public void inlet(int val) {

        //inlet 0: number curent beat
        //inlet 1: number or symbolcurrent motif
        //inlet 2: number current tempo
        //inlet 3: 0 or 1 play/don't play
        int intlet_no = getInlet();
        switch(intlet_no) {
            case 0:
                playBeat(val);
                break;
            case 1:
                setMotif(val);
                break;
            case 2:
                setTempo(val);
                break;
            case 3:
                toggleOutput(val);
                break;
            default:
                post("INLET NOT SUPPORTED");
        }
    }
}
