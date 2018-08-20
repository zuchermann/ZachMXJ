package Songs;

import java.util.ArrayList;

/**
 * Created by yn on 7/22/18.
 */
public class MotifLearner {
    private static final double MAX_BPM = 180;
    private static final double MIN_BPM = 90;
    private static final double MIN_NOTES_PER_SECOND = 2;
    private static final int MIN_NOTES = 4;
    private static final double MIN_MOTIF_BEAT_LENGTH = 4;
    private static final double MAX_MOTIF_BEAT_LENGTH = 32;
    private static final double QUANTIZE_TO = .25;
    private static final double MIN_NOTE_LENGTH = 25;


    private ArrayList<Double> notes; // list of midi pitches of notes
    private ArrayList<Double> times; // list of times of notes in ms

    MotifLearner(){
        notes = new ArrayList<>();
        times = new ArrayList<>();
    }

    public void clear(){
        notes = new ArrayList<>();
        times = new ArrayList<>();
    }

    public void addNote(double note){
        double current_time = System.currentTimeMillis();
        notes.add(note);
        times.add(current_time);
    }

    public void addNoteInstrument(double note) {
        addNoteInstrument(note, 0);
    }

    public void addNoteInstrument(double note, double latency) {
        double current_time = System.currentTimeMillis() - latency;
        double adjusted_time = current_time;
        if(notes.size() > 0){
            double previous_time = times.get(times.size() - 1);
            if(adjusted_time - previous_time > MIN_NOTE_LENGTH && note > 0) {
                notes.add(note);
                times.add(adjusted_time);
            } else {
                if(note > 0) {
                    notes.set(notes.size() - 1, note);
                } else{
                    notes.remove(notes.size() - 1);
                    times.remove(times.size() - 1);
                }
            }
        } else if (note > 0){
            notes.add(note);
            times.add(adjusted_time);
        }
    }

    private static ArrayList<Double> absToRel(ArrayList<Double> abs_beats, double beat_length){
        double previous_time = abs_beats.get(0);
        ArrayList<Double> quantized = new ArrayList<>();
        for(int i = 1; i < abs_beats.size(); i++){
            double this_time = abs_beats.get(i);
            double d_time = this_time - previous_time;
            quantized.add(d_time);
            previous_time = this_time;
        }
        quantized.add(beat_length - previous_time);
        return quantized;
    }

    private ArrayList<Double> quantizeMotif(double bpm, double beat_length){
        double first_time = times.get(0);
        ArrayList<Double> beats = new ArrayList<>();
        for(Double d : times){
            double time_from_start = d - first_time;
            double minutes_from_start = time_from_start / 60000.;
            double beats_from_start = minutes_from_start * bpm;
            double num_quants = Math.round(beats_from_start / QUANTIZE_TO);
            double quant = num_quants * QUANTIZE_TO;
            beats.add(quant);
        }
        for(int i = 0; i < beats.size(); i ++){
            double next_beat = i < beats.size() - 1 ? beats.get(i + 1) : beat_length;
            double try_beat = 0;
            while(try_beat < next_beat){
                double prev_beat = beats.get(i);
                if(try_beat > prev_beat){
                    double time_before = try_beat - prev_beat;7t
                    double time_after = next_beat - try_beat;
                    if(time_before < time_after || next_beat == beat_length){
                        beats.set(i, try_beat);
                    } else{
                        beats.set(i+1, try_beat);
                    }
                }
                try_beat = try_beat + 2;
            }
        }
        return absToRel(beats, beat_length);
    }

    private static double getMotifLength(float[][] motif) {
        float[] motif_beats = motif[1];
        double beat_accum = 0;
        for (float motif_beat : motif_beats) {
            beat_accum += (double) motif_beat;
        }
        return beat_accum;
    }

    private static ArrayList<ArrayList<Double>> getAbsoluteBeats(float[][] motif){
        float[] motif_notes = motif[0];
        float[] motif_beats = motif[1];
        ArrayList<Double> result_notes = new ArrayList<>();
        ArrayList<Double> result_beats = new ArrayList<>();
        double beat_acc = 0;
        for(int i = 0; i < motif_beats.length; i++){
            result_beats.add(beat_acc);
            result_notes.add((double) motif_notes[i]);
            double current_beat = motif_beats[i];
            beat_acc = beat_acc + current_beat;
        }
        ArrayList<ArrayList<Double>> result = new ArrayList<>();
        result.add(result_notes);
        result.add(result_beats);
        return result;
    }

    public static float[][] addPulse(float[][] motif, double pulse_freq, double pulse_note){
        ArrayList<ArrayList<Double>> abs_motif = getAbsoluteBeats(motif);
        ArrayList<Double> motif_notes = abs_motif.get(0);
        ArrayList<Double> motif_beats = abs_motif.get(1);
        ArrayList<Double> result_notes = new ArrayList<>();
        ArrayList<Double> result_beats = new ArrayList<>();
        double beat_length = getMotifLength(motif);
        for(int i = 0; i < motif_notes.size(); i++){
            result_notes.add(motif_notes.get(i));
            result_beats.add(motif_beats.get(i));
            double next_beat = i < motif_beats.size() - 1 ? motif_beats.get(i + 1) : beat_length;
            double try_beat = 0;
            while(try_beat < next_beat){
                double prev_beat = motif_beats.get(i);
                if(try_beat >= prev_beat){
                    result_notes.add(pulse_note);
                    result_beats.add(try_beat);
                }
                try_beat = try_beat + pulse_freq;
            }
        }
        result_beats = absToRel(result_beats, beat_length);
        return toFloatArray(result_notes, result_beats);
    }

    private static float[] float_array_help(ArrayList<Double> array_vals){
        float[] result = new float[array_vals.size()];
        for(int i = 0; i < array_vals.size(); i++){
            result[i] = (array_vals.get(i)).floatValue();
        }
        return result;
    }

    private static float[][] toFloatArray(ArrayList<Double> motif_notes, ArrayList<Double> motif_beats){
        ArrayList<ArrayList<Double>> combined = new ArrayList<>();
        combined.add(motif_notes);
        combined.add(motif_beats);
        return toFloatArray(combined);
    }

    private static float[][] toFloatArray(ArrayList<ArrayList<Double>> motif){
        if(motif.size() == 2){
            float[] notes = float_array_help(motif.get(0));
            float[] beats = float_array_help(motif.get(1));
            return new float[][] {notes, beats};
        } else {
            return null;
        }
    }

    public float[][] generateMotif(double bpm){
        return generateMotif(bpm - 10.0, bpm + 10.0);
    }

    public float[][] generateMotif(){
        return generateMotif(MIN_BPM, MAX_BPM);
    }

    public float[][] generateMotif(double min_bpm, double max_bpm){
        int num_notes = notes.size();
        ArrayList<ArrayList<Double>> result = new ArrayList<>();
        if(num_notes >= MIN_NOTES) {
            double num_beats = MIN_MOTIF_BEAT_LENGTH;
            double current_time = System.currentTimeMillis();
            double first_time = times.get(0);
            while (num_beats <= MAX_MOTIF_BEAT_LENGTH) {
                double motif_time = current_time - first_time;
                double motif_time_minutes = motif_time / 60000.;
                double beats_per_minute = num_beats / motif_time_minutes;
                double motif_time_seconds = 60. * (num_beats / beats_per_minute);
                if(beats_per_minute <= max_bpm && beats_per_minute >= min_bpm){ // check if allowable bpm
                    if(((double)num_notes) / motif_time_seconds > MIN_NOTES_PER_SECOND){ // check note density
                        ArrayList<Double> quantized = quantizeMotif(beats_per_minute, num_beats);
                        result = new ArrayList<>();
                        result.add(notes);
                        result.add(quantized);
                        break;
                    }
                }
                num_beats = num_beats * 2;
            }
        }
        notes = new ArrayList<>();
        times = new ArrayList<>();
        //System.out.println(result.get(1));
        return toFloatArray(result);
    }
}
