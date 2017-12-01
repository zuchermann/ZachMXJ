package innards;

/**
 * A class must implement <code>iLaunchable</code> in order to be launched by the, uh, <code>Launcher</code>.
 * <p>
 * While this can't be specified rigorously in the interface, it should be noted that all <code>iLaunchables</code>
 * should have a default, no argument constructor.  In order to be sufficiently general purpose, the 
 * <code>Launcher</code> must assume that such a constructor is defined.
 * 
 * @author synchar
 */
public interface iLaunchable
{
	/**
	 * Don't forget your no argument constructor!
	 * 
	 * public LaunchableThing()
	 */
	
	/**
	 * The <code>Launcher</code> will invoke this method on its target class once the main Cocoa application
	 * has finished launching.
	 */
	public void launch();
}
