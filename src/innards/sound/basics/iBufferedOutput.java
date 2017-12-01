package innards.sound.basics;

import java.nio.FloatBuffer;

/**
 * @author marc
 * created on Jul 23, 2003
 */
public interface iBufferedOutput {
	public boolean write(FloatBuffer buffer);
	public iBufferedOutput start();
	public int getInternalBufferSize();
}