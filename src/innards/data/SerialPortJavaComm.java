package innards.data;

/** 
  Interface for talking to serial
  ports on various platforms of computers.  Since a serial port is a
  real resource (non virtual), all the implementations of this will
  require native methods.  
  */

import innards.util.ShutdownHook;

import java.io.*;
import javax.comm.*;
import java.util.*;

public class SerialPortJavaComm extends SerialDataPort
{

  protected InputStream inputStream;
  protected OutputStream outputStream;
  protected SerialPort serialPort;
  protected Thread readThread;

  /**
   * Make and open the serial port identified by comPortName, like "/dev/blah.blah"
   * @param comPortName the name of the com port, in unix the path to the device file
   * @throws IOException
   */
  public SerialPortJavaComm(String comPortName) throws IOException
  {
    init(comPortName, 1, true);
  }


  public SerialPortJavaComm(String comPortName, int tries) throws IOException
  {
	  init(comPortName, tries, true);
  }

  /**
   * Make and open the serial port identified by comPortName, like "/dev/blah.blah"
   * @param comPortName the name of the comp port, in unix the path to the device file
   * @param tries attempt a few times - i left this in from the earlier implementation, maybe it helps with incorrectly closed ports?
   * @throws IOException
   */
  public SerialPortJavaComm(String comPortName, int tries, boolean autoclose) throws IOException
  {
    init(comPortName, tries,autoclose);
  }

  protected void init(String comPortName, int tries, boolean autoclose) throws IOException{
    CommPortIdentifier portID = getPortID(comPortName);
	if(portID == null || !portID.getName().equals(comPortName)){
      throw new IOException(" couldn't find port <"+comPortName+"> on this computer ");
	}
	boolean success = false;
    while(!success && tries-->0){
	  try{
	    serialPort = initPort(portID);
	    success = true;
      }catch(IOException ex){
	    if(tries > 0){
          System.out.println("Failed to init serial port, will rety "+tries+" more time"+((tries>1)?"s":""));
	    }
      }
    }
	if(autoclose){
	  System.out.println("Adding shutdown close hook for serial port");
	  addShutdownCloseHook();
	}else{
      System.out.println("not adding shutdown close hook for serial port, close it manually!");
	}
  }

  protected CommPortIdentifier getPortID(String comPortName){
    CommPortIdentifier portID = null;
    Enumeration portList = CommPortIdentifier.getPortIdentifiers();
    boolean found = false;
    while (portList.hasMoreElements()) {
      portID = (CommPortIdentifier) portList.nextElement();
      if (portID.getPortType() == CommPortIdentifier.PORT_SERIAL) {

        System.out.println(" computer has port <"+portID.getName()+">");

        if (portID.getName().equals(comPortName)) {
          found = true;
          break;
        }
      }
    }
    if(found){
      return portID;
    }else{
      return null;
    }
  }

  protected SerialPort initPort(CommPortIdentifier portID) throws IOException{
    try{
      serialPort = (SerialPort) portID.open("SerialPortJavaComm", 2000);
    }catch(PortInUseException e){
      throw new IOException("Port In Use");
    }

    inputStream = serialPort.getInputStream();
    outputStream= serialPort.getOutputStream();

    try{
      serialPort.enableReceiveTimeout(100);
    }catch(UnsupportedCommOperationException e){
      System.out.println("Cannot set Receive Timeout, not support, will continue anyway:");
	  e.printStackTrace();
    }

	  //serialPort.setSerialPortParams(9600,
	  //    SerialPort.DATABITS_8,
	  //    SerialPort.STOPBITS_1,
	  //    SerialPort.PARITY_NONE);
	return serialPort;
  }


  
  
  public void enableReceiveTimeout(int timeout) throws IOException 
  {
    try{
      serialPort.enableReceiveTimeout(timeout);
    }catch(Exception ex){throw new IOException("problem in enableReceiveTimeout - embedded exception is <"+ex+">");}
  }
  
  
  /** Reads as many as possible without blocking and  returns number
      successfulyl read into buffer.  Returns -1 if no data currently
      available, in which case the caller should sleep and try later. */ 
  public int read(byte[] buffer, int num_to_read) throws IOException
  {
    return read(buffer,0,num_to_read);
  }

  /** same as read(buffer, n) but first byte starts at buffer[offset] */
  public int read(byte[] buffer, int offset, 
			   int num_to_read) throws IOException
  {
    if (inputStream!=null)
    {        
        return inputStream.read(buffer,offset,num_to_read);
    }
    else
    {
        throw new NullPointerException(" port <"+this+"> did not open successfully or has been closed ");
    }
  }


  public void sendBreak(int millis){
    serialPort.sendBreak(millis);
  }
  //tracks if the port has been closed, so it will not be closed twice.
  private boolean closed = false;
  /**
   * release the resource.  if you do not call this, it will be closed when this class is released
   * or when the program exits, unless you specified not-autoclose.
   */
  public void close()
  {
    synchronized(this){
	  if(!closed){
	    serialPort.close();
	    closed = true;
		if(addedShutdownCloseHook!=null){
          System.out.println("Removing shutdown hook for serial port closing because we're being closed manually");
	      Runtime.getRuntime().removeShutdownHook(addedShutdownCloseHook);
		}
	  }else{
		  new Exception("Cannot Close Serial port as it is already closed, redundant close called from here").printStackTrace();
	  }
    }
    inputStream = null;
  }
  
  /** return number successfully written */
  public int write(byte[] buffer, int num_to_write) throws IOException
  {
    outputStream.write(buffer, 0, num_to_write);  
    return num_to_write;
  }
  
  /** flush the buffers */
  public void flush() throws IOException
  {
    outputStream.flush();
  }
  

  /** flush the input buffer  - not supported */
  public void flushInputBuffer() throws IOException
  {
  //  inputStream.flush();
  }

  public void setSerialPortParams(int baud, int dataBits, int stopBits, int parity) throws IOException{
    try{
      serialPort.setSerialPortParams(baud, dataBits, stopBits, parity);
      System.out.println(" set serial port to port <baud:"+serialPort.getBaudRate()+" dataBits:"+serialPort.getDataBits()+" stopBits:"+serialPort.getStopBits()+" parity:"+serialPort.getParity()+">");
    }catch(Exception ex){
      throw new IOException("problem in setSerialPortParams - embedded exception is <"+ex+">");
    }
  }

  /** set the baudrate */
  public void setSpeed(int baud) throws IOException
  {
    try{
        serialPort.setSerialPortParams(baud,serialPort.getDataBits(),serialPort.getStopBits(),serialPort.getParity());
        
        System.out.println(" port <"+serialPort.getBaudRate()+" "+serialPort.getDataBits()+" "+serialPort.getStopBits()+" "+serialPort.getParity());
    }catch(Exception ex){throw new IOException("problem in setSpeed - embedded exception is <"+ex+">");}
  }

   public void setRTS(boolean rts){
      serialPort.setRTS(rts);
   }

   public void setDTR(boolean dtr){
      serialPort.setDTR(dtr);
   }

  /** set the hardware flow control on or off according to state */
  public void setHardwareFlowControl(boolean state) throws IOException
  {
    try{
        if (state) serialPort.setFlowControlMode(serialPort.FLOWCONTROL_RTSCTS_IN | serialPort.FLOWCONTROL_RTSCTS_OUT);
        else serialPort.setFlowControlMode(serialPort.FLOWCONTROL_NONE);
    }catch(Exception ex){throw new IOException("problem in setHardwareFlowControl - embedded exception is <"+ex+">");}
  }

  /** return if there is data available on the read buffer */
  public boolean dataPending() throws IOException
  {
    return (inputStream.available() != 0);
  }

  //tracks if the shutdown hook has been added, so it will not be added twice.
  private ShutdownHook addedShutdownCloseHook = null;
  /**
   * setup a shutdown hook to automatically release this resource when quitting.
   */
  protected void addShutdownCloseHook(){
    if(addedShutdownCloseHook != null){
      new Exception("Error addShutdownCloseHook already called, only meaningful to call once.  called from here").printStackTrace();
    }else{
      Runtime.getRuntime().addShutdownHook(addedShutdownCloseHook = new ShutdownHook(){
        public void safeRun(){
          synchronized(this){
            if(!closed){
              System.out.println("ShutdownHook closing serial port");
              serialPort.close();
	          System.out.println("ShutdownHook Done closing");
              closed = true;
            }
          }
        }
      });
    }
  }

  protected void finalize() throws Throwable{
    super.finalize();
	synchronized(this){
      if(!closed){
	    System.out.println("Finalizer for Serial Port closing port.");
        serialPort.close();
        closed = true;
	    if(addedShutdownCloseHook!=null){
		  System.out.println("Removing shutdown hook in serial port finalizer");
          Runtime.getRuntime().removeShutdownHook(addedShutdownCloseHook);
	    }
      }
	}
  }

  public String toString(){
    String info = "SerialPortJavaComm Serial Port "+super.toString()+"\n"+
           "Baud Rate:"+serialPort.getBaudRate()+"\n"+
           "Data Bits:";

	  switch(serialPort.getDataBits()){
		  case SerialPort.DATABITS_5:
			  info+="5\n";break;
		  case SerialPort.DATABITS_6:
			  info+="6\n";break;
		  case SerialPort.DATABITS_7:
			  info+="7\n";break;
		  case SerialPort.DATABITS_8:
			  info+="8\n";break;
	  }
      info+="Flow Control Mode:\n";
	  if(serialPort.getFlowControlMode()==SerialPort.FLOWCONTROL_NONE)
		  info+="\tFlow Control None\n";
	  if((serialPort.getFlowControlMode()&SerialPort.FLOWCONTROL_RTSCTS_IN)!=0)
		  info+="\tHardware Flow Control for Input\n";
	  if((serialPort.getFlowControlMode()&SerialPort.FLOWCONTROL_RTSCTS_OUT)!=0)
		  info+="\tHardware Flow Control for Output\n";
	  if((serialPort.getFlowControlMode()&SerialPort.FLOWCONTROL_XONXOFF_IN)!=0)
		  info+="\tSoftare Flow Control for Input\n";
	  if((serialPort.getFlowControlMode()&SerialPort.FLOWCONTROL_XONXOFF_OUT)!=0)
		  info+="\tSoftare Flow Control for Output\n";
	info+="Parity:";
	switch(serialPort.getParity()){
		case SerialPort.PARITY_NONE:
			info+="None\n"; break;
		case SerialPort.PARITY_EVEN:
			info+="Even\n"; break;
		case SerialPort.PARITY_ODD:
			info+="Odd\n"; break;
		case SerialPort.PARITY_MARK:
			info+="Mark\n"; break;
		case SerialPort.PARITY_SPACE:
			info+="Space\n"; break;
	}
	info+="Stop Bits:";
	  switch(serialPort.getStopBits()){
		  case SerialPort.STOPBITS_1:
			 info+="1\n";break;
		  case SerialPort.STOPBITS_2:
			 info+="2\n";break;
		  case SerialPort.STOPBITS_1_5:
			 info+="1_5\n";break;
	  }

	 return info;
  }
}
