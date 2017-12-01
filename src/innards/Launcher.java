package innards;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.Timer;

import innards.debug.Debug;
import innards.namespace.context.ContextTree;
import innards.util.*;

/**
 * The basic entry point into our system.  The Launcher provides some useful, lightweight
 * initialization, and sets up our main update loop.  To use the Launcher, create a
 * class that implements the iLaunchable interface.  Instead of writing the standard main()
 * method, put all of your setup code in the launch() method specified by the interface.
 * On startup, the Launcher will instantiate your class and invoke this launch() method.
 * <p>
 * Usage: java innards.Launcher [-property1 value1] [-p2 v2] . . .              <br>
 * important properties include:                                                <br>
 *    -launchable.class  package.ClassToLaunch  (must implement iLaunchable)    <br>
 *    -timer.interval    intervalInSeconds      - main update timer interval    <br>
 * Note that these properties can also be specified in the InnardsDefaults preferences file.
 * If a property is specified both in the preferences file and on the command-line, then the
 * command-line property takes precedence for the duration of the run.  Command-line properties
 * are temporary - they will not be saved out to the InnardsDefaults preferences file on shutdown.
 * <p>
 * On startup, the Launcher performs the following steps in order:   <br>
 * 1) Parses the command-line arguments as temporary InnardsDefaults properties.  <br>
 * 2) Sets up the main menu.   <br>
 * 3) Constructs the main update timer, using the update interval specified by timer.interval. <br>
 * 4) Constructs an instance of the main iLaunchable class specified by launchable.class. <br>
 * 5) Invokes the main launchable's launch() method on the Swing thread. <br>
 * 6) Starts the main update timer, which will repeatedly invoke the main update() method from the Swing thread.
 * <p>
 * To add menu items to the main menu, clients should call the Launcher's addMenuItem method,
 * or talk directly to the MenuManager class.
 * <p>
 * The Launcher maintains a list of iUpdateable objects, which are updated in order on each update cycle.
 * To add an object to the update list, clients should call the Launcher's registerUpdateable method.
 * The Launcher also provides methods for deregistering, pausing, and unpausing updateables.
 * <p>
 * Important note: To shutdown the Launcher from within an updateable, call the Launcher's
 * shutdown() method - the standard System.exit() will NOT work within the main update cycle.
 * <p>
 * To create shutdown hooks that are guaranteed not to conflict with the main update cycle,
 * use subclasses of innards.util.ShutdownHook.  See the documentation in that class for more details.
 * <p>
 * One of the Launcher's main jobs is to guarantee our thread-compatibility with Swing.
 * This guarantee holds during the main iLaunchable's launch() method, as well as during the main
 * update cycle.  Thus, it is safe to create, modify, and access Swing objects from within the
 * launch() method and from within the update() method of any iUpdateable registered with the Launcher.
 * <p>
 * The important parameters which the Launcher uses, including the name of the main launchable class 
 * and the interval for the main update timer, are read out of InnardsDefaults.  Keys for these
 * parameters are defined as static Strings below.
 * <p>
 * 
 * @see innards.iLaunchable
 * @see innards.iUpdateable
 * @see innards.util.ShutdownHook
 * //@see innards.util.MenuManager
 * @see innards.util.InnardsDefaults
 * 
 * @author many
*/
public class Launcher
{
	protected static Launcher launcher;
	protected iLaunchable launchable;
	protected Timer timer;
	protected boolean isPaused= false;
	
	/**
	 * Keys for reading parameter values out of InnardsDefaults.
	 */
	public static final String LAUNCHABLE_CLASS= "launchable.class";
	public static final String TIMER_INTERVAL= "timer.interval";
	public static final String ASSERTS = "asserts";
	public static final String EXIT_ON_EXCEPTION = "exitOnException";

	protected List<iUpdateable> updateables= new ArrayList<iUpdateable>();

	protected Set<iUpdateable> paused= new HashSet<iUpdateable>();
	protected Set<iUpdateable> willPause= new HashSet<iUpdateable>();
	protected Set<iUpdateable> willUnPause= new HashSet<iUpdateable>();

	protected Set<iUpdateable> willRemove = new HashSet<iUpdateable>();
	
	protected iUpdateable currentUpdating;
	
	/**
	 * thread-synchronized boolean flag.
	 * the value is true only if the main update() loop will never run again or if it is
	 * permanently blocked by System.exit().
	 */
	protected SynchronizedBoolean mainTimerOffline = new SynchronizedBoolean(false);
	/**
	 * this lock is obtained and released by the main update() loop.
	 * obtaining this lock outside of the update loop guarantees that the update loop can't run.
	 */
	protected Object updateLock = new Object();
	
	/**
	 * Private constructor - there can be only one <code>Launcher</code>.
	 */
	private Launcher()
	{
		//this terminates the timer, allowing ShutdownHook subclasses to run their code after all launcher updates. -jg
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable(){
			public void run()
			{
				// make sure that the main timer hasn't already been terminated by the shutdown() method
				if (!mainTimerOffline.get())
				{
					// the main timer is still running: obtain the update lock by waiting until the current update cycle completes
					synchronized(updateLock)
					{
						// set the mainTimerOffline flag to true to guarantee that the update() loop won't run again
						// - setting this flag to true will also permit the safe ShutdownHooks to run
						mainTimerOffline.set(true);
					}
				}
			}
		}, "Main Launcher Shutdown Hook"));
	}
	
	/**
	 * Return the <code>Launcher</code> so that clients can make
	 * calls on it.
	 */
	public static Launcher getLauncher()
	{
		return launcher;
	}

	public static void main(String[] args)
	{
		// make the launcher
		launcher = new Launcher();
		
		try
		{
			// parse the command-line arguments + set defaults
			parseCommandLineIntoDefaults(args);
			
			// set the assertion status
			// - setting command line parameters for the VM is a chore, instead we define a Default "asserts"
			if (InnardsDefaults.getIntProperty(ASSERTS,-1)==1) Launcher.class.getClassLoader().setDefaultAssertionStatus(true);
		
			// setup the main menu
			//MenuManager.getMenuManager();
						
			// launch!
			launcher.doLaunch();
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			System.err.println("Launcher reports that an error occured while initializing in the main() method.");
			System.err.println(" the error was a throwable of type <"+t+"> <"+t.getClass()+">");
			System.exit(1);
		}
	}
	
	protected static void parseCommandLineIntoDefaults(String[] args)
	{
		if (args.length % 2 != 0)
		{
			printUsage();
			System.exit(1);
		}
		
		for (int i=0; i<args.length; i+=2)
		{
			String property = args[i];
			String value = args[i+1];
			
			if (property.charAt(0) != '-')
			{
				printUsage();
				System.exit(1);
			}
			
			property = property.substring(1);
			InnardsDefaults.setProperty(property, value, true);
		}
	}
	
	public static void printUsage()
	{
		System.err.println("Usage: java innards.Launcher [-property1 value1] [-p2 v2] . . .");
		System.err.println("important properties include:");
		System.err.println("   -"+LAUNCHABLE_CLASS+"  package.ClassToLaunch  (must implement iLaunchable)");
		System.err.println("   -"+TIMER_INTERVAL+"    intervalInSeconds      - main update timer interval");
	}
	
	/**
	 * The doLaunch method performs initialization as follows:
	 * <p>
	 * 1) Constructs the main update timer, using the update interval specified by timer.interval. <br>
	 * 2) Constructs an instance of the main <code>iLaunchable</code> class specified by launchable.class. <br>
	 * 3) Invokes the main launchable's <code>launch()</code> method on the Swing thread. <br>
	 * 4) Starts the main update timer, which will repeatedly invoke the main <code>update()</code> method from the Swing thread.
	 */
	private void doLaunch()
	{
		// Build the main timer
		constructMainTimer();

		//Instantiate the main iLaunchable
		launchable = getLaunchable();

		//Launch!
		try
		{
			SwingUtilities.invokeAndWait(new Runnable()
			{
				public void run()
				{
					launchable.launch();				
				}
			});
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			System.err.println("Launcher reports that an error occured while initializing in the doLaunch() method.");
			System.err.println(" the error was a throwable of type <"+t+"> <"+t.getClass()+">");
			System.exit(1);
		}

		startMainTimer();
	}

	/**
	 * Constructs our main timer, uses timer.interval as our default update interval (defaults to 1/60);
	 * now you can reconstruct!  change the TIMER_INTERVAL and recall this.
     */
	public void constructMainTimer()
	{
        double interval = InnardsDefaults.getDoubleProperty(TIMER_INTERVAL, 0);
		if (interval == 0)
		{
			interval = 1 / 60.0;
		}
		int intervalMillis = (int)Math.round(interval * 1000f);
		
		if(timer!=null){
			timer.stop();
		}
		
		timer = new Timer(intervalMillis, new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				update();
			}
		});
	}

	public void startMainTimer()
	{
		timer.start();		
	}

	/**
	 * Returns the update interval for the main timer in seconds.
	 */
	public double getMainTimerInterval()
	{
		return timer.getDelay() / 1000f;
	}

	private void update()
	{
		synchronized(updateLock)  // obtain the update lock
		{
			if(mainTimerOffline.get())
			{
				// we can only get here if we're shutting down
				System.err.println("stopping the main timer!");
				timer.stop();
				return;
			}
			if (!isPaused)
			{
				for (int i= 0; i < updateables.size(); i++)
				{
					iUpdateable up= (iUpdateable) updateables.get(i);
					if (!paused.contains(up))
						try
						{
							currentUpdating = up;
							
							Object enter = ContextTree.where();
							
							up.update();
							
							Object exit = ContextTree.where();
							
							if (enter!=exit)
							{
								throw new Error(" updateable ended in a context that it didn't start in <"+enter+"> != <"+exit+">")		;
							}
							
						} catch (Throwable tr)
						{
							System.err.println(("Launcher reporting an exception while updating <" + up + ">"));
							tr.printStackTrace();
							if (InnardsDefaults.getIntProperty(EXIT_ON_EXCEPTION, 0)==1){
								shutdown();
							}
						}
					currentUpdating = null;
				}
				
				paused.addAll(willPause);
				paused.removeAll(willUnPause);
				willPause.clear();
				willUnPause.clear();
			}
			updateables.removeAll(willRemove);
			willRemove.clear();
		}
	}
	
	/**
	 * this method returns true only if the main update() loop will never run again or if it is
	 * permanently blocked by System.exit().
	 * @return
	 */
	public boolean mainTimerTerminated(){
		return mainTimerOffline.get();
	}

	/**
	 * Call this method to register an <code>iUpdateable</code> for updating at the specified divisor
	 * (<code>updateDivisor</code>) of the main timer's update frequency.  For example, if 
	 * <code>updateDivisor</code> is 2 and the main timer runs at 60 Hz, then the target will be updated
	 * at 30 Hz.
	 * @param target The <code>iUpdateable</code> which will be updated.
	 * @param updateDivisor Will be updated at the main timer's frequency divided by this parameter.
	 */
	public void registerUpdateable(final iUpdateable target, final int updateDivisor)
	{
		//The anonymous class used here wraps the update target and implements
		//the logic necessary to support updating at a specified frequency
		//relative to the main timer.
		registerUpdateable(new iUpdateable()
		{
			int tick= 0;
			public void update()
			{
				tick++;
				if (tick % updateDivisor == 0)
				{
					target.update();
				}
			}
		});
	}

	/**
	 * Call this method to register a <code>iUpadteable</code> for updating at the main timer's
	 * frequency.
	 * @param target - the <code>iUpdateable</code> which will be updated.
	 */
	public void registerUpdateable(iUpdateable target)
	{
		if(!updateables.contains(target)){
			updateables.add(target);
		}
		if(willRemove.contains(target)){
			willRemove.remove(target); //if someone wants it back, don't go ahead and remove it next tick.
		}
	}

	/**
	 * Call this method to deregister a previously registered <code>iUpdateable</code>, such that it
	 * will no longer be updated.
	 */
	public void deregisterUpdateable(iUpdateable target)
	{
		// subtle bug, removing during the update cycle, will mean that the next updateable is skipped
//		boolean success= updateables.remove(target);
//		Debug.doAssert(success, "Launcher reports an attempt to deregister an iUpdateable which was not previously registered.  This can't be good.");

		// rather, we'll do this
		willRemove.add(target);
		assert updateables.contains(target) : "Launcher reports an attempt to deregister an iUpdateable which was not previously registered.  This can't be good.";
	}
	
	public boolean isRegisteredUpdateable(iUpdateable target){
		return updateables.contains(target);
	}
	
	public void deregisterCurrentUpdateable()
	{
		updateables.remove(this.currentUpdating);
	}
	
	/**
	 * Pauses all the updateables, except those that are in the exception array
	 * the exception set can be null.
	 * Updateables get paused after the end of this execution cycle. This is usually what one actually wants to do.
	 */
	public void pauseAllUpdateablesExcept(iUpdateable[] except)
	{
		willPause.addAll(updateables);
		willPause.removeAll(Arrays.asList(except));
		willPause.removeAll(paused);
		willUnPause.removeAll(willPause);
	}
	
	/**
	 * unpauses all updateables at the end of this execution cycle
	 */
	public void unpauseAllUpdateables()
	{
		willUnPause.addAll(updateables);
		willPause.removeAll(willUnPause);
	}
	
	public void unpauseCurrent()
	{
		willUnPause.add(currentUpdating);
		willPause.remove(currentUpdating);
	}
	
	/**
	 * pauses all updateables except current one (the one that is currently executing*/
	public void pauseAllUpdateablesExceptCurrent()
	{
		pauseAllUpdateablesExcept(new iUpdateable[]{currentUpdating});
	}

	/**
	* Call this method to add a new menu item, with associated call back, to a designated submenu. 
	* If a submenu with the designated name does not already exist, it will be created automatically.
	* @param subMenuName Name of the submenu to which the item is to be added.
	* @param itemName The name that will appear in the submenu.
	* @param callBackTarget  The object that will handle the call back for the new item.
	* @param callBackMethodName  The name of the call back method.
	* @param updateDivisor If non-zero this parameter specifies the divisor at which our main timer should call <code>update</code> on the callBackTarget.
	*/
	public void addMenuItem(String subMenuName, String itemName, Object callBackTarget, String callBackMethodName, int updateDivisor)
	{
		addMenuItem(subMenuName, itemName, "", callBackTarget, callBackMethodName, updateDivisor);
	}

	/**
	* Call this method to add a new menu item, with associated call back, to a designated submenu. 
	* If a submenu with the designated name does not already exist, it will be created automatically.
	* <p>
	* This version of the method allows a shortcut key to be associated with the new item.
	* @param subMenuName Name of the submenu to which the item is to be added.
	* @param itemName The name that will appear in the submenu.
	* @param shortcutKey Shortcut for the added item.
	* @param callBackTarget  The object that will handle the call back for the new item.
	* @param callBackMethodName  The name of the call back method.
	* @param updateDivisor If non-zero this parameter specifies the divisor at which our main timer should call <code>update</code> on the callBackTarget.
	*/
	public void addMenuItem(String subMenuName, String itemName, String shortcutKey, Object callBackTarget, String callBackMethodName, int updateDivisor)
	{
		//MenuManager.getMenuManager().addMenu(subMenuName, itemName, callBackTarget, callBackMethodName);

		//If the argument updateDivisor was non-zero, we need to register the callBackTarget for updating.
		if (updateDivisor != 0)
		{
			Debug.doAssert((callBackTarget instanceof iUpdateable), "Launcher reports that it can't update the specified call back target <" + callBackTarget + ">!");
			registerUpdateable((iUpdateable) callBackTarget, updateDivisor);
		}
	}

	/**
	 * Returns an instance of the <code>iLaunchable</code> main class specified in launchable.class
	 */
	protected iLaunchable getLaunchable()
	{
		Object launchable = null;

		String launchClassName = InnardsDefaults.getProperty(LAUNCHABLE_CLASS).trim();
		if (launchClassName == null || launchClassName.equals(""))
		{
			System.err.println("Launcher could not find the expected default <" + LAUNCHABLE_CLASS + ">");
			printUsage();
			System.exit(1);
		}

		// Try to load the specified launch class
		try
		{
			Class<?> launchClass = Class.forName(launchClassName);
			launchable = launchClass.newInstance();
		}
		catch (Exception e)
		{
			System.out.println("exception preventing iLaunchable instantiation:");
			e.printStackTrace();
			System.exit(1);
		}

		Debug.doAssert(launchable instanceof iLaunchable, "Launcher reports that the specified launch class was not an iLaunchable.");
		return (iLaunchable) launchable;
	}

	/**
	 * Pause updating.
	 */
	public void pause()
	{
		isPaused= true;
	}

	/**
	 * Resume updating.
	 */
	public void resume()
	{
		isPaused= false;
	}

	/**
	 * Use this to shutdown the launcher from within an updateable - System.exit() will not work from there. <br>
	 * It's also ok, though not necessary, to call this method from other places.
	 */
	public void shutdown()
	{
		// first, obtain the update lock: this is a no-op if we're within an
		// updateable; otherwise, we'll wait until the current update cycle completes
		synchronized(updateLock)
		{
			// set the mainTimerOffline flag to true to guarantee that the update() loop won't run again
			// - if we're inside of the update() loop, the subsequent System.exit() will cause it to block permanently
			// - setting this flag to true will also permit the safe ShutdownHooks to run on exit
			mainTimerOffline.set(true);
			
			// block this thread and exit!
			System.exit(0);
		}
	}
}