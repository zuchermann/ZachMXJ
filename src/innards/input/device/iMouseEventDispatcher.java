package innards.input.device;


/**
 * Interface for objects which dispatch mouse events to <code>iMouseEventDelegates</code>.  In most cases,
 * the <code>GLController</code> will be the only <code>iMouseEventDelegate</code>.
 * 
 * @author Derek
 */
public interface iMouseEventDispatcher
{
	/**
	 * Register <code>iMouseEventDelegate</code> for notification of mouse events.
	 */
	public void registerMouseEventDelegate(iMouseEventDelegate delegate);
	
	/**
	 * Deregister a previously registered <code>iMouseEventDelegate</code>.
	 */
	public void deregisterMouseEventDelegate(iMouseEventDelegate delegate);	
}
