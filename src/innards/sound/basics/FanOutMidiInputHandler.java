package innards.sound.basics;

import java.util.*;
import java.util.ArrayList;

//import marc.exile.tile.TileMain;

import innards.sound.basics.Midi.Message;

/**
 * on handler feeding into several handlers
 * 
 * @author marc
 * created on Jul 22, 2003
 */
public class FanOutMidiInputHandler implements Midi.InputHandler {
	
	
	public interface iFilter
	{
		public boolean pass(Midi.Message on)	;
	}
	
	List handlers = new ArrayList();
	public FanOutMidiInputHandler register(Midi.InputHandler handler)
	{
		if (!handlers.contains(handler)) handlers.add(handler);
		return this;
	}
	
	public FanOutMidiInputHandler deregister(Midi.InputHandler handler)
	{
		handlers.remove(handler);
		return this;
	}
	
	iFilter filter;
	
	public FanOutMidiInputHandler setFilter(iFilter f)
	{
		filter = f;
		return this;
	}
	
	public void handle(long hostTime, Message message) 
	{
		//TileMain.out.println(" fanning out to <"+handlers.size()+"> message");
		if (filter == null || filter.pass(message))
		{
			for(int i=0;i<handlers.size();i++)
			{
				((Midi.InputHandler)handlers.get(i)).handle(hostTime, message);
			}
		}
	}
	
	static public class DensityFilter implements iFilter
	{
		float volumePerSecondSlope;
		float volumeMultiplier;
		float[] lastTimes = new float[16];
		float[] lastVolumes = new float[16];
		long timeStarted;
		
		// I think 1 and 1.5 is a good number
		public DensityFilter(float volumePerSecondSlope, float volumeMultiplier)
		{
			this.volumeMultiplier = volumeMultiplier;
			this.volumePerSecondSlope = volumePerSecondSlope;
			timeStarted = System.currentTimeMillis();
		}
		public boolean pass(Message m)
		{
			//System.out.println(" !! ");
			if (!(m instanceof Midi.NoteOn)) return true;
			if ((m instanceof Midi.NoteOff)) return true;
			
			int c = ((Midi.NoteOn)m).getChannel();
			float v = ((Midi.NoteOn)m).getVelocity();
			
			float timeNow = (System.currentTimeMillis()-timeStarted)/1000.0f;
			
			if (lastVolumes[c]-(timeNow-lastTimes[c])*volumePerSecondSlope < v)
			{
				//System.out.println("PASSED: "+lastVolumes[c]+" "+(timeNow-lastTimes[c])+" "+v);
				lastTimes[c] = timeNow;
				lastVolumes[c] = v*volumeMultiplier;
				return true;
			}
			System.err.println("FAILED: "+lastVolumes[c]+" "+(timeNow-lastTimes[c])+" "+timeNow+" "+lastTimes[c]+" "+v);
			
			if (m instanceof Midi.NoteOn)
			{
				if (((Midi.NoteOn)m).getChannel()!=3)
				{
					lastTimes[c] = timeNow;
					lastVolumes[c] = v*volumeMultiplier;
				}
			}
			//System.out.println(" --");
			return false;
		}
	}

}
