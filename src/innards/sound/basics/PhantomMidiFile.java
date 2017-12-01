/* Created on Aug 20, 2003 */
package innards.sound.basics;

import java.io.*;
import java.io.File;


/**
 * easy with CoreAudio ? 
 * @author marc
 * created on Aug 20, 2003
 */
public class PhantomMidiFile {
	
//	MusicSequence sequence;
//	MusicPlayer musicPlayer;
	
	public PhantomMidiFile(String filename)
	{
		try
		{
//			sequence = new MusicSequence(new File(filename));
//			sequence.setMIDIEndpoint(Midi.getMidi().getEndpoint());
//			musicPlayer = new MusicPlayer();
//			musicPlayer.setSequence(sequence);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public PhantomMidiFile play()
	{
		try
		{
//			if (musicPlayer.isPlaying())
//				musicPlayer.stop();
//			musicPlayer.setTime(0);
//			musicPlayer.start();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return this;
	}
}
