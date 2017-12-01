package innards.util;

import java.awt.event.*;

import javax.swing.Timer;

import innards.iUpdateable;

/**
 * want an updateable to be called every so often from within Swing's update
 * thread? this is your class
 * new UpdateableTimer(myUpdateable, 0.1)
 * and you'll be called every 0.1s
 * 
 * wraps this up inside a try{}catch block
 * @author marc
 * 
 */
public class UpdateableTimer
{
	protected Timer timer;
	
	public UpdateableTimer(final iUpdateable update, float interval)
	{
		int intervalMillis = (int)Math.round(interval * 1000f);
		
		timer = new Timer(intervalMillis, new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					update.update();
				}
				catch (Throwable t)
				{
					System.err.println(" exception  thrown inside update loop ");
					t.printStackTrace();
				}
			}
		});

		timer.start();
	}
	
	public void stop()
	{
		timer.stop();
	}
}