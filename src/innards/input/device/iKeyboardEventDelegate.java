package innards.input.device;

import java.awt.event.KeyEvent;

/**
 * Interface for objects that wish to be notified of keyboard events by an <code>iKeyboardEventDispatcher</code>.
 * 
 * @author Derek
 */
public interface iKeyboardEventDelegate
{
	/**
	 * Note it is the implementers responsibility to determine what <i>kind</i> of keyboard event has occurred.
	 * <p>
	 */
	public void handleKeyboardEvent(KeyEvent keyboardEvent);
}