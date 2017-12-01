/* Created on Jul 22, 2003 */
package innards.sound.util;


import innards.Launcher;
import innards.provider.iFloatProvider;
import innards.sound.basics.*;
import innards.sound.basics.Midi;
import innards.sound.basics.Midi.Message;
import innards.util.TaskQueue;

/**
 * @author marc
 * created on Jul 22, 2003
 */
public class ControllerHandler implements Midi.InputHandler
{

	float[][] controllerTable;

	public ControllerHandler()
	{
		controllerTable= new float[16][127];
		for (int i= 0; i < 16; i++)
		{
			for (int j= 0; j < 127; j++)
			{
				controllerTable[i][j]= -1;
			}
		}
	}

	public void handle(long hostTime, Message message)
	{
		if (message instanceof Midi.ControlChange)
		{
			Midi.ControlChange cm= (Midi.ControlChange) message;
			int c= cm.getChannel();
			int v= cm.getValue();
			int cc= cm.getController();
			System.out.println(c + " " + v + " " + cc);
			controllerTable[c][cc]= v / (127.0f);
		}
	}

	public float getController(int channel, int controller, float def)
	{
		float c= controllerTable[channel][controller];
		return c == -1 ? def : c;
	}

	public iFloatProvider getControllerProvider(final int channel, final int controller, final float def)
	{
		return new iFloatProvider()
		{
			public float evaluate()
			{
				return getController(channel, controller, def);
			}
		};
	}

	static public ControllerHandler install()
	{
		try
		{
			Midi midi= Midi.getMidi();
			ControllerHandler c= new ControllerHandler();
//			TaskQueue queue= midi.registerInputHandler(c);
//			Launcher.getLauncher().registerUpdateable(queue);
			return c;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/** @param midiInput
	/** @return */
	public static ControllerHandler install(FanOutMidiInputHandler midiInput)
	{
		ControllerHandler c= new ControllerHandler();
		midiInput.register(c);
		return c;
	}
}
