package innards.input.device;


/**
 * Interface for objects which dispatch keyboard events to <code>iKeyboardEventDelegates</code>.  In most cases,
 * the <code>GLController</code> will be the only <code>iKeyboardEventDelegate</code>.
 * 
 * @author Derek
 */
public interface iKeyboardEventDispatcher
{
	/**
	 * Register <code>iKeyboardEventDelegate</code> for notification of mouse events.
	 */
	public void registerKeyboardEventDelegate(iKeyboardEventDelegate delegate);
	
	/**
	 * Deregister a previously registered <code>iKeyboardeEventDelegate</code>.
	 */
	public void deregisterKeyboardEventDelegate(iKeyboardEventDelegate delegate);
}