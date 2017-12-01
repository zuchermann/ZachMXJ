package innards.data;

/** 
  An abstract class to define an interface for talking to serial
  ports on various platforms of computers.  Since a serial port is a
  real resource (non virtual), all the implementations of this will
  require native methods.  
  */

import java.io.IOException;

public abstract class SerialDataPort 
{
  
  /** Reads as many as possible without blocking and  returns number
      successfulyl read into buffer.  Returns -1 if no data currently
      available, in which case the caller should sleep and try later. */ 
  abstract public int read(byte[] buffer, int num_to_read) throws IOException;

  /** same as read(buffer, n) but first byte starts at buffer[offset] */
  abstract public int read(byte[] buffer, int offset, 
			   int num_to_read) throws IOException;

  /** release the resource */
  abstract public void close();
  
  /** return number successfully written */
  abstract public int write(byte[] buffer, int num_to_write) throws IOException;
  
  /** flush the buffers */
  abstract public void flush() throws IOException;

  /** flush the input buffer */
  abstract public void flushInputBuffer() throws IOException;

  /** set the baudrate */
  abstract public void setSpeed(int baud) throws IOException;

  /** set the hardware flow control on or off according to state */
  abstract public void setHardwareFlowControl(boolean state) throws IOException;

  /** return if there is data available on the read buffer */
  abstract public boolean dataPending() throws IOException;

}
