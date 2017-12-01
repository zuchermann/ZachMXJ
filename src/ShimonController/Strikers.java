package ShimonController;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;

import javax.sound.midi.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by yn on 11/27/17.
 */
public class Strikers {
    private static final int[] whiteKeys = {0, 2, 4, 5, 7, 9, 11};
    private MidiDevice midiOutDevice;
    Receiver midiOutReceiver;
    private boolean isSimulation;
    private OSCPortOut OSCsender;

    Strikers() {this(false, null);}

    Strikers(boolean isSimulation, OSCPortOut OSCsender){
        this.isSimulation = isSimulation;
        if (isSimulation) {
            this.OSCsender = OSCsender;
        }
        else{
            MidiDevice.Info[] MidiDeviceInfos = MidiSystem.getMidiDeviceInfo();
            int foundPort = -1;
            for (int i = 0; i < MidiDeviceInfos.length; i++) {
                if (MidiDeviceInfos[i].getDescription().equals("USB MIDI 1x1 Port 1")) {
                    foundPort = i;
                }
            }
            if (foundPort == -1) {
                System.out.println("Striker's midi device (USB MIDI 1x1) not found");
            } else {
                try {
                    MidiDevice midiOutDevice = MidiSystem.getMidiDevice(MidiDeviceInfos[foundPort]);
                    midiOutDevice.open();
                    midiOutReceiver = midiOutDevice.getReceiver();
                } catch (MidiUnavailableException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean isWhiteKey(int note){
        for(int whiteNote : whiteKeys){
            if(note % 12 == whiteNote) return true;
        }
        return false;
    }

    public void strike(int striker_index){
        if (midiOutReceiver != null) {
            ShortMessage myMsg = new ShortMessage();
            try {
                long timeStamp = -1;
                myMsg.setMessage(ShortMessage.NOTE_ON, 0, striker_index, 100);
                midiOutReceiver.send(myMsg, timeStamp);
                TimeUnit.MILLISECONDS.sleep(40);
                myMsg.setMessage(ShortMessage.NOTE_OFF, 0, striker_index, 100);
                midiOutReceiver.send(myMsg, timeStamp);
                //System.out.println("sent midi note " + striker_index);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void strike(int striker_index, int midi_note){
        strike(striker_index, midi_note, 100);
    }

    public void strike(int striker_index, int midi_note, int midi_vel){
        if(isSimulation) {
            ArrayList<Object> args = new ArrayList<>();
            args.add(midi_note);
            args.add(midi_vel);
            OSCMessage msg = new OSCMessage("/note_out", args);
            try {
                OSCsender.send(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            if (midiOutReceiver != null) {
                ShortMessage myMsg = new ShortMessage();
                int strikerMidi = striker_index * 2;
                try {
                    if (striker_index == 0 || striker_index == 1) {
                        if (isWhiteKey(midi_note)) {
                            strikerMidi += 1;
                        }
                    } else if (isWhiteKey(midi_note)) {
                        strikerMidi += 1;
                    }
                    myMsg.setMessage(ShortMessage.NOTE_ON, 0, strikerMidi, 93);
                    long timeStamp = -1;
                    midiOutReceiver.send(myMsg, timeStamp);
                    TimeUnit.MILLISECONDS.sleep(40);
                    myMsg.setMessage(ShortMessage.NOTE_OFF, 0, strikerMidi, 0);
                    midiOutReceiver.send(myMsg, timeStamp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
