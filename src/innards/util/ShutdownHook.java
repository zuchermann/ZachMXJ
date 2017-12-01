package innards.util;

import innards.Launcher;

/**
 * <p>
 * This class is designed to be used for shutdown hooks in processes started by the Launcher.
 * The run method will not be called until the Launcher has finished updating, to avoid threading
 * issues resulting from cleaning up resources during an update cycle.  ShutdownHooks will still run
 * concurrently with each other.</p>
 *
 * <p>Since this class extends Thread, what before would be:</p>
 *
 *<pre>
 *      Runtime.getRuntime().addShutdownHook(new Thread(new Runnable(){
 *          public void run(){
 *              System.out.println("Terminating!");
 *          }
 *      }));
 *</pre>
 *
 *<p> is now: </p>
 *
 *<pre>
 *      Runtime.getRuntime().addShutdownHook(new ShutdownHook(){
 *          public void safeRun(){
 *              System.out.println("Terminating!");
 *          }
 *      });
 *</pre>
 *
 * User: jg
 * Date: Jun 10, 2005
 * Time: 6:00:23 PM
 */
public abstract class ShutdownHook extends Thread{


	public final void run(){
	    Launcher l = Launcher.getLauncher(); 
		while(l != null && !l.mainTimerTerminated()){
			try{
				Thread.sleep(1);
			} catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		safeRun();
	}

	/**
	 * override this method to perform your shutdown hook tasks.
	 * this method will be called AFTER the last update of the main thread.
	 */
	public abstract void safeRun();

}
