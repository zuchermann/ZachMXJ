package innards.sound.basics;

import innards.*;
import innards.iLaunchable;
import innards.sound.basics.Midi.Message;
import innards.util.TaskQueue;

/**
 * @author marc
 */
public class T_DensityFilter implements iLaunchable
{
	public void launch()
	{
		try
		{
//			Midi.outputToNewVirtualSource("source");
			FanOutMidiInputHandler fanOut= new FanOutMidiInputHandler();
			fanOut.register(new Midi.InputHandler()
			{
				public void handle(long hostTime, Message message)
				{
					System.out.println(message);
				}
			});
//			TaskQueue q= Midi.getMidi().registerInputHandler(fanOut);
//			Launcher.getLauncher().registerUpdateable(q);

			fanOut.setFilter(new FanOutMidiInputHandler.DensityFilter(1, (float) 1.5)
			{
				/**
				 * @see innards.sound.basics.FanOutMidiInputHandler.DensityFilter#pass(innards.sound.basics.Midi.Message)
				 */
				public boolean pass(Message m)
				{
					boolean b = super.pass(m);
					System.out.println(" got message <"+m+"> <"+b+">");
					return b;
				}
			});
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
