package innards.debug;

import java.io.PrintStream;
import java.util.*;

import innards.Launcher;

/**
 * This class is just a container for simple debugging functionality e.g
 * reports and assert.
 * <p>
 * we should move over to 1.4's assert keyword as soon as possible. why?  1) it has almost zero cost when debugging is switched off
 * 2) the string expression that is made during failure is note evaluated until failure occurs. This means that it is also zero garbage.
 * <p>
 * way back in the mists of time, we added 10Hz to a demo by commenting out all the assert(condition, explanation)'s. 
 * <p>
 * @author synchar
 *
 */
public class Debug
{
	private static boolean isDebugOn = false;
	private final static PrintStream printStream = System.err;
	private static HashMap debugChannelStatus = new HashMap();
	private static ArrayList debugChannelListeners = new ArrayList();
	private static int formattingOffset = 25;
	
	public Debug()
	{
	}

	/**
	 * This is only here to provide backwards compatability to code deep in the
	 * heart of darkness. Use doAssert you damn fool! Or at least us it until we
	 * switch to java 1.4
	 * 
	 * @deprecated
	 * 
	 */
	public static void die(String msg)
	{
		Debug.doAssert(false, msg);
	}

	/**
	 * mmmm....assertalicious. This is just a plain old vanilla assert, but the
	 * key word is reserved, so it's called doAssert for now. If <code>
	 * condition </code> is false then the assert will print out <code> msg
	 * </code> and a stack backtrace. Use these! They're better than candy.
	 * @param condition
	 * @param msg
	 */
	public static void doAssert(boolean condition, String msg)
	{
		if (!condition)
		{
			try
			{
				throw new Exception(msg);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				Launcher.getLauncher().shutdown();
			}
		}
	}

	/**
	 * This method prints out <code>msg </code> if the debug channel named
	 * <code> channel </code> has been activated and if <code> isDebugOn()
	 * </code> returns true. If the channel has not previously been declared, it
	 * is created. This should be used whenever you have that knee-jerk desire
	 * to use System.out. println to debug something, since it keeps System.out
	 * from being so full of untraceable print lines that nothing can be found.
	 * 
	 */
	public static void doReport(String channel, String msg)
	{
		if (! isDebugOn()) 
		{
			return;
		}
		
		else if (! debugChannelStatus.containsKey(channel)) 
		{
			debugChannelStatus.put(channel, new Boolean(false));
			for (Iterator iter = debugChannelListeners.iterator(); iter.hasNext();)
			{
				((iDebugChannelListener)iter.next()).channelAdded(channel);
			}
		}
		
		if (((Boolean)debugChannelStatus.get(channel)).booleanValue())
		{
			if (channel.length() < formattingOffset)
			{
				int spaces = formattingOffset - channel.length();
				printStream.print(channel);
				for (int i = 0; i < spaces; i++) printStream.print(" ");
				printStream.println(msg);
			}
			else printStream.println(channel+": "+msg);
		}
	}
	
	public static void doReport(String msg)
	{
		doReport("unknown", msg);
	}
	
	
	/**
	 * returns whether debugChannel functionality has been turned on.
	 * 
	 */
	public static boolean isDebugOn() { return Debug.isDebugOn; }

	/**
	 * 
	 * @param b - if b is true then debugChannel functionality is turned on,
	 * it's turned off if b is false.
	 */
	public static void setDebugOn(boolean b)
	{
		isDebugOn = b;
	}
	
	/**
	 * Turns on or off a particular debug channel.
	 * 
	 * 
	 */
	public static void setChannelStatus(String channel, boolean b)
	{
		debugChannelStatus.put(channel, new Boolean(b));
	}
	
	/**
	 * Register a new <code>iDebugChannelListener</code> to receive status
	 * information regarding the debug channels.
	 * @param listener
	 */
	public static void addDebugChannelListener(iDebugChannelListener listener)
	{
		debugChannelListeners.add(listener);
	}
	
	/**
	 * An interface for objects which are going to listen
	 * to the status of the debug channels maintained by <code>Debug</code>
	 * .
	 * @author synchar
	 */
	public interface iDebugChannelListener
	{
		/**
		 * <code>Debug</code> will call this method whenever a new debug
		 * channel (named, creatively enough, <code>channel</code> is added.
		 */
		public void channelAdded(String channel);
	}
}