package innards.input.device;

import java.awt.event.MouseEvent;

/**
 * Interface for objects that wish to be notified of mouse events by an <code>iMouseEventDispatcher</code>.
 * 
 * @author Derek
 */
public interface iMouseEventDelegate
{
	/**
	 * Note it is the implementers responsibility to determine what <i>kind</i> of mouse event has occurred.
	 * <p>
	 * The implementer can also pass the <code>mouseEvent</code> back to the <code>iMouseEventDispatcher</code>
	 * to obtain more specialized information, for example to deproject the view coordinates of the mouse click
	 * onto the ground plane of the scene.
	 * 
	 * @see innards.input.device.iMouseEventDispatcher
	 */
	public void handleMouseEvent(MouseEvent mouseEvent, iMouseEventDispatcher dispatcher);
}
