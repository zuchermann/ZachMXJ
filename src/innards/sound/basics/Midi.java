package innards.sound.basics;

import java.util.*;
import java.util.List;


import innards.util.*;
import innards.util.TaskQueue;
import innards.util.TaskQueue.Task;


/**
	all the classes that you need to do basic midi on the mac. right here. right now.
	
 */

public class Midi
{

	static public boolean pairNoteOffsWithOns= true;
	static List[] noteOns= new List[16 * 255];

	// thread singleton design pattern - will return null if Midi was created in a different thread.
	static public Midi getMidi() throws Exception
	{
		if (sMidi == null)
		{
			sMidi= new Midi();
			sMidiCreatedThread= Thread.currentThread();
		}

		if (Thread.currentThread() != sMidiCreatedThread)
			System.out.println(
				" midi instantiated on thread <"
					+ sMidiCreatedThread
					+ ">, accessed by thread <"
					+ Thread.currentThread()
					+ ">");

		return Thread.currentThread() == sMidiCreatedThread ? sMidi : null;
	}

	// some general purpose midi things
	public static final int ACTIVE_SENSING= 254;
	public static final int CHANNEL_PRESSURE= 208;
	public static final int CONTINUE= 251;
	public static final int CONTROL_CHANGE= 176;
	public static final int END_OF_EXCLUSIVE= 247;
	public static final int MIDI_TIME_CODE= 241;
	public static final int NOTE_OFF= 128; // decoy - everybody just sends a note on with zero velocity
	public static final int NOTE_ON= 144;
	public static final int PITCH_BEND= 224;
	public static final int POLY_PRESSURE= 160;
	public static final int PROGRAM_CHANGE= 192;
	public static final int SONG_POSITION_POINTER= 242;
	public static final int SONG_SELECT= 243;
	public static final int START= 250;
	public static final int STOP= 252;
	public static final int SYSTEM_RESET= 255;
	public static final int TIMING_CLOCK= 248;
	public static final int TUNE_REQUEST= 246;

	// classes that represent our midi messasges _and_ represent higher level things ---------------------------------------------

	static public class Message
	{
		public int[] data= new int[3]; // three bytes for a standard short midi message
		public Message()
		{
		}
		public Message(int i1, int i2, int i3)
		{
			data[0]= i1;
			data[1]= i2;
			data[2]= i3;
		}

		public long hostTime;
		public Message stampTime(long hosttime)
		{
			this.hostTime= hosttime;
			return this;
		}

		public int hashCode()
		{
			return (data[0]+1)*(data[1]+17)*(data[2]+129037);
		}
		/** @see java.lang.Object#equals(java.lang.Object) */
		public boolean equals(Object obj)
		{
			if (!(obj instanceof Message)) return false;
			return (((Message)obj).data[0] == data[0]) &&(((Message)obj).data[1] == data[1]) && (((Message)obj).data[2] == data[2]);
			
		}
	}

	static public class NoteOn extends Message
	{
		public NoteOn(int channel, int note, int velocity)
		{
			data[0]= NOTE_ON + channel;
			data[1]= note;
			data[2]= velocity;
			List l= noteOns[channel * 255 + note];
			if (l == null)
				noteOns[channel * 255 + note]= l= new LinkedList();
			l.add(this);
		}

		public NoteOn setVelocity(float v)
		{
			int b= (int) (v * 127);
			if (b > 127)
				b= 127;
			if (b < 0)
				b= 0;
			data[2]= b;
			return this;
		}

		public int getNote()
		{
			return data[1];
		}

		public int getChannel()
		{
			return data[0] - NOTE_ON;
		}

		public float getVelocity()
		{
			return data[2] / 127.0f;
		}

		public String toString()
		{
			return "noteOn:" + getChannel() + ":" + getNote() + ":" + getVelocity();
		}

		public void setNote(int i)
		{
			List l= noteOns[(data[0] - NOTE_ON) * 255 + data[1]];

			if (l != null)
				l.remove(this);
			data[1]= i;
			l= noteOns[(data[0] - NOTE_ON) * 255 + data[1]];
			if (l == null)
				noteOns[(data[0] - NOTE_ON) * 255 + data[1]]= l= new LinkedList();
			l.add(this);
		}

		/** @return */
		public Midi.NoteOn copy()
		{
			return new NoteOn(getChannel(), getNote(), (int) (getVelocity()*127));
		}

	}

	static public class NoteOff extends NoteOn
	{
		NoteOn correspondingTo;

		public NoteOff(int channel, int note)
		{
			super(channel, note, 0);
			if (pairNoteOffsWithOns)
			{
				List l= noteOns[channel * 255 + note];
				if (l != null)
				{
					for (Iterator i= l.iterator(); i.hasNext();)
					{
						NoteOn noteOn= (NoteOn) i.next();
						correspondingTo= noteOn;
						i.remove();
						break;
					}
				}
			}
		}

		public NoteOn getNoteOn()
		{
			return correspondingTo;
		}
	}

	static public class ControlChange extends Message
	{
		public ControlChange()
		{
			data[0]= CONTROL_CHANGE;
		}
		public ControlChange(int channel, int control, int value)
		{
			data[0]= CONTROL_CHANGE + channel;
			data[1]= control;
			data[2]= value;
		}

		public ControlChange setController(int controller)
		{
			data[1]= controller;
			return this;
		}
		public ControlChange setValue(int value)
		{
			data[2]= value;
			return this;
		}

		public int getChannel()
		{
			return data[0] - CONTROL_CHANGE;
		}

		public int getController()
		{
			return data[1];
		}

		public int getValue()
		{
			return data[2];
		}
	}

	static public class GeneralMessage extends Message
	{
		public GeneralMessage(int type, int channel, int d1, int d2)
		{
			super(type + channel, d1, d2);
		}
		public GeneralMessage setChannel(int c)
		{
			data[0]= (data[0] & 0xf0) | c;
			return this;
		}
		public GeneralMessage setData1(int d)
		{
			data[1]= d;
			return this;
		}
		public GeneralMessage setData2(int d)
		{
			data[2]= d;
			return this;
		}
	}

	// classes for receiving (in a thread safe manner) midi events ---------------------------------------------------

	static public interface InputHandler
	{
		public void handle(long hostTime, Message message);
	}

	static public interface UnthreadedInputHandler extends InputHandler
	{
		public void handleNow(long hostTime, Message message);
	}

	// returns a TaskQueue that has events inserted into it. Service this to service input.
//	MIDIReadProc input;
//
//	public TaskQueue registerInputHandler(final InputHandler handler) throws Exception
//	{
//		final TaskQueue queue= new TaskQueue();
//		input= new MIDIReadProc()
//		{
//			public void execute(
//				com.apple.audio.midi.MIDIInputPort in,
//				com.apple.audio.midi.MIDIEndpoint to,
//				com.apple.audio.midi.MIDIPacketList is)
//			{
//				System.out.println(" ??");
//				try
//				{
//					System.out.println(is + " has <" + is.numPackets() + "> <" + in + "> <" + to + ">");
//					for (int j= 0; j < is.numPackets(); ++j)
//					{
//						MIDIPacket packet= (MIDIPacket) is.getPacket(j);
//						if (packet.getLength() == 3)
//						{
//
//							// so here goes -
//							int i1= intify(packet.getData().getByteAt(0));
//							int i2= intify(packet.getData().getByteAt(1));
//							int i3= intify(packet.getData().getByteAt(2));
//
//							int channel= i1 & 0x0F;
//							int id= i1 & 0xF0;
//
//							Message message= null;
//							// and parse the int
//							switch (id)
//							{
//								case NOTE_ON :
//									message= (i3 == 0) ? new NoteOff(channel, i2) : new NoteOn(channel, i2, i3);
//									break;
//								case CONTROL_CHANGE :
//									message= new ControlChange(channel, i2, i3);
//									break;
//								default :
//									System.out.println(
//										" problem, unrecognized id <"
//											+ id
//											+ "> <"
//											+ channel
//											+ "> (<"
//											+ i1
//											+ ">) <"
//											+ i2
//											+ "> <"
//											+ i3
//											+ ">");
//							}
//
//							System.out.println(" message is <" + message + ">");
//							if (i2 == 255)
//								message= null;
//
//							final Message fmessage= message;
//							final long hostTime= HostTime.getCurrentHostTime();
//							if (message != null)
//								message.stampTime(hostTime);
//							queue.new Task()
//							{
//								public void run()
//								{
//									handler.handle(hostTime, fmessage);
//								}
//							};
//
//							if (handler instanceof UnthreadedInputHandler)
//							{
//								((UnthreadedInputHandler) handler).handleNow(hostTime, fmessage);
//							}
//						} else
//							System.out.println(
//								" ( warning, got packet length <"
//									+ packet.getLength()
//									+ "> != 3 not quite sure what to do with it) ");
//					}
//				} catch (Exception ex)
//				{
//					ex.printStackTrace();
//				}
//			}
//		};
//		MIDIInputPort inPort= client.inputPortCreate(new CAFString("Input port"), input);
//		// open connections from all sources
//		int n= MIDISetup.getNumberOfSources();
//		System.out.println(" there are <" + n + "> sources");
//		for (int i= 0; i < n; ++i)
//		{
//			MIDIEndpoint src= MIDISetup.getSource(i);
//			inPort.connectSource(src);
//			System.out.println(" name: " + src.getStringProperty(MIDIConstants.kMIDIPropertyName).toString());
//
//			System.out.println(" port <" + inPort + "> <" + src + "> src");
//		}
//
//		// further, let people inside the building send to us:
//		dest= client.destinationCreate(new CAFString("hello"), input);
//
//		// workaround apple bug			
//		Object o= ReflectionTools.illegalGetObject(dest, "readHandler");
//		ReflectionTools.illegalSetObject(o, "port", inPort);
//
//		return queue;
//	}
//	
//	
//	MIDIEndpoint dest;
//
//	public MIDIEndpoint getEndpoint()
//	{
//		return dest;
//	}
//
//	// for sending events (not thread safe, you cannot access this from multiple threads, nor from a
//	// a thread that didn't create the first "midi" object
//	// otherwise, you should use a task queue
//
//	public void send(float offsetFromNowInSeconds, Message message) throws CAException
//	{
//		MIDIPacketList list= new MIDIPacketList();
//		MIDIData event= MIDIData.newMIDIRawData(3);
//		event.addRawData(message.data[0], message.data[1], message.data[2]);
//		long f= (long) (HostTime.convertNanosToHostTime((long) (1E9)) * offsetFromNowInSeconds);
//		list.add(HostTime.getCurrentHostTime() + f, event);
//		//
//		//System.out.println(" list is <"+list+">");
//		if (virtualSource)
//			gDest.received(list);
//		else
//		{
//			gOutPort.send(gDest, list);
//		}
//	}
//
//	// -------------------------------------------------------------------------------------------------------------------------
//
//	MIDIOutputPort gOutPort;
//	MIDIEndpoint gDest;
//	boolean virtualSource= false;
//
	static protected Midi sMidi= null;
	static protected Thread sMidiCreatedThread= null;
//	MIDIClient client= null;
//
//	public Midi() throws Exception
//	{
//	}
//
//	static protected MIDINotifyProc notifyProc;
//
//	static public Midi outputToDestination(String destName) throws CAException
//	{
//		Midi midi= new Midi();
//		sMidiCreatedThread= Thread.currentThread();
//		midi.client= new MIDIClient(new CAFString("MIDIjava"), notifyProc= new MIDINotifyProc()
//		{
//			public void execute(MIDIClient arg0, MIDINotification arg1)
//			{
//				System.out.println(" notify proc called for client");
//			}
//		});
//		MIDIOutputPort outPort= midi.client.outputPortCreate(new CAFString("Output port"));
//		midi.gOutPort= outPort;
//
//		// enumerate devices (not really related to purpose of the echo program
//		// but shows how to get information about devices)
//
//		int i, n;
//		String pname= null;
//		String pmanuf, pmodel;
//
//		n= MIDIDevice.getNumberOfDevices();
//		for (i= 0; i < n; ++i)
//		{
//			MIDIDevice dev= MIDIDevice.getDevice(i);
//
//			pname= dev.getStringProperty(MIDIConstants.kMIDIPropertyName).toString();
//			pmanuf= dev.getStringProperty(MIDIConstants.kMIDIPropertyManufacturer).toString();
//			pmodel= dev.getStringProperty(MIDIConstants.kMIDIPropertyModel).toString();
//			System.out.println(" name = " + pname + " manuf = " + pmanuf + " model = " + pmodel);
//		}
//
//		// find the first destination
//		n= MIDISetup.getNumberOfDestinations();
//		System.out.println(" number of destinations <" + n + ">");
//		for (int m= 0; m < n; m++)
//		{
//
//			midi.gDest= MIDISetup.getDestination(m);
//			pname= midi.gDest.getStringProperty(MIDIConstants.kMIDIPropertyName).toString();
//			System.out.println(" dest:<" + pname + ">");
//			if (destName == null || pname.equals(destName))
//			{
//				break;
//			}
//		}
//
//		if (midi.gDest != null)
//		{
//			System.out.println("ouput on <" + pname + ">");
//		} else
//		{
//			System.out.println("couldn't find <" + pname + ">");
//			return null;
//		}
//
//		sMidi= midi;
//		return midi;
//	}
//
//	static public Midi outputToNewVirtualSource(String sourceName) throws CAException
//	{
//		Midi midi= new Midi();
//		sMidiCreatedThread= Thread.currentThread();
//		midi.client= new MIDIClient(new CAFString("MIDIjava"), null);
//		MIDIOutputPort outPort= midi.client.outputPortCreate(new CAFString("Output port"));
//		midi.gOutPort= outPort;
//		midi.gDest= midi.client.sourceCreate(new CAFString(sourceName));
//
//		midi.virtualSource= true;
//
//		sMidi= midi;
//		return midi;
//	}
//
//	public static void listDest()
//	{
//		try
//		{
//			MIDIClient client= new MIDIClient(new CAFString("MIDIjava"), null);
//			MIDIOutputPort outPort= client.outputPortCreate(new CAFString("Output port"));
//			MIDIOutputPort gOutPort;
//			MIDIEndpoint gDest= null;
//
//			// enumerate devices (not really related to purpose of the echo program
//			// but shows how to get information about devices)
//
//			int i, n;
//			String pname= null;
//			String pmanuf, pmodel;
//
//			n= MIDIDevice.getNumberOfDevices();
//			for (i= 0; i < n; ++i)
//			{
//				MIDIDevice dev= MIDIDevice.getDevice(i);
//
//				pname= dev.getStringProperty(MIDIConstants.kMIDIPropertyName).toString();
//				pmanuf= dev.getStringProperty(MIDIConstants.kMIDIPropertyManufacturer).toString();
//				pmodel= dev.getStringProperty(MIDIConstants.kMIDIPropertyModel).toString();
//				System.out.println(" name = " + pname + " manuf = " + pmanuf + " model = " + pmodel);
//			}
//
//			// find the first destination
//			n= MIDISetup.getNumberOfDestinations();
//			System.out.println(" number of destinations <" + n + ">");
//			for (int m= 0; m < n; m++)
//			{
//
//				gDest= MIDISetup.getDestination(m);
//				pname= gDest.getStringProperty(MIDIConstants.kMIDIPropertyName).toString();
//				System.out.println(" dest:<" + pname + ">");
//			}
//			outPort.dispose();
//			client.dispose();
//		} catch (CAException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	public static int intify(byte argh)
	{
		int i= (argh < 0 ? ((int) argh) + 256 : (int) argh);
		return i;
	}


}
