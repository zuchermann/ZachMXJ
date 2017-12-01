package innards.input.device;

import innards.math.linalg.Vec3;

/**
 * Interface for objects that wish to be notified of mouse events, for example by the <code>TrivialGLController</code>.
 * @author synchar
 */
public interface iMouseDelegate
{
	public void receiveMouseDownEvent(Vec3 worldMouseDownPosition);
}
