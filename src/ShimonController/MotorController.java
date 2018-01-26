package ShimonController;

import innards.data.PSerial;
import innards.debug.Debug;

import java.io.IOException;

public class MotorController {

    public static final int MAX_POSITION = 1000;
    private static final int DEFAULT_PRECISION = 1;  // mm
    private boolean comPortInitialized;
    private PSerial port;
//    private SerialPortJavaComm comPort;

    private long sentTime;

    private byte [] readBuffer = new byte [1024];

    private static final int PATIENCE = 15; // ms

    private boolean isHomed = false;

    public MotorController(String comPortName) throws IOException {

        comPortInitialized = false;
        port = new PSerial(comPortName, 230400);
//            comPort = new SerialPortJavaComm(comPortName);
//            comPort.setSpeed(230400);
        comPortInitialized = true;

    }

    /**
     *  Note the position is relative to the axis, whoever calls this needs to know where the axis 0 is and what
     *  direction it's going
     *
     *  All inputs are in mm or mm/sec except for accelleration which is in G
     */
    public void setTarget(int axis, int position, int vMax, float accel)
    {
        String msg = formatSetTarget (axis, position, DEFAULT_PRECISION, vMax, accel, 0);
        sendMessage(msg);
    }

    private String formatSetTarget(int axis, int position, int precision, int vMax, float accel, int push)
    {
        // The Query is as follows:
        //  0n (axis - 1 based) 10 (func code) 9900 (start addr) 0009 (# regs) 12 (# bytes)
        //  nnnnnnnn (target pos / 0.01mm) nnnnnnnn (precision / 0.01mm) nnnnnnnn (speed/ 0.01mm/sec)
        //  nnnn (accel / 0.01G) nnnn (push) 0000 (control)
        return String.format("%02d109900000912%08X%08X%08X%04X%04X0000",
                axis+1, position*100, precision*100, vMax*100, (int)(accel*100), push);
    }

    public void servoOn(int axis)
    {
        String msg = String.format("%02d060D001000", axis+1);
        sendMessage(msg);
    }

    public void servoOff(int axis)
    {
        String msg = String.format("%02d0504030000", axis+1);
        sendMessage(msg);
    }

    public void sendHome(int axis)
    {
        String msg = String.format("%02d060D001010", axis+1);
        sendMessage(msg);
    }

    /**
     *
     * @param axis
     * @return -1 : don't know  0: no alarm  1: alarm
     */
    public int queryAlarm(int axis)
    {
        String msg = String.format("%02d0390020001", axis+1);
        String resp = sendMessage(msg, true, 15);
        if (resp == null)
            return -1;
        return resp.substring(7, 11).equals("0000") ? 0 : 1;
    }

    public void resetAlarm(int axis)
    {
        String msg = String.format("%02d050407FF00", axis+1);
        sendMessage(msg);
    }

    /**
     *
     * @param axis
     * @return -1 : don't know  0: not home yet  1: home end
     */
    public int queryHomeEnd(int axis)
    {
        int s = queryStatus(axis);
        return s == -1 ? -1 : (s & 8) >> 3;
    }

    public boolean testModel(Shimon shimon, double maxDiff) {
        boolean result = false;
        for(int i = 0; i < 4; i++){
            double currentTime = System.currentTimeMillis();
            double modelPosition = shimon.getArmPosition(i, currentTime);
            double actualPosition = this.queryPosition(i);
            result = actualPosition < 0 || Math.abs(modelPosition - actualPosition) > maxDiff;
            if(result){
                //System.out.println("arm "+i+" is at " + actualPosition + " but was expected to be at " +modelPosition );
                break;
            }
        }
        return result;
    }

    /**
     *
     * @param axis
     * @return -1 : don't know  0: not home yet  1: home end
     */
    public int queryStatus(int axis)
    {
//        System.out.println("axis = " + axis);
        String msg = String.format("%02d0390080002", axis+1);
        String resp = sendMessage(msg, true, 19);
        if (resp == null)
            return -1;

        return Integer.parseInt(resp.substring(14,15),16);
    }

    /**
     *
     * @param axis
     * @return -1 : don't know  or position (>= 0)
     */
    public int queryPosition(int axis) {

        String msg = String.format("%02d0390000002", axis+1);
        String resp = sendMessage(msg, true, 19);
        if (resp == null)
            return -1;

        return Integer.parseInt(resp.substring(7,15),16) / 100;

    }


    private void sendMessage (String message)
    {
        sendMessage(message, false, 0);
    }

    private String sendMessage (String message, boolean readResponse, int responseLength)
    {
        Debug.doReport("SCONController", "Got message of " + message.length()+ " chars : ["+ message+  "]");

        // Calculate LRC checksum and add the leading ':' and trailing '\r\n'
        byte[] msg = String.format(":%s%02X\r\n", message, computeLRC(message)).getBytes();

        if (comPortInitialized) {
            try {


                // flush input
//                comPort.read(readBuffer, 1024);

                sentTime = System.currentTimeMillis();

                Debug.doReport("SCONController", "Sending " + msg.length + " bytes : ["+ new String(msg) +  "]");
                port.write(msg);
//                comPort.write(msg, msg.length);

//                try {Thread.sleep(50);} catch(InterruptedException ignored){}

                if (readResponse)
                    return readResponse(responseLength);

            } catch (IOException e) {
                System.out.println("Couldn't send Serial message");
                e.printStackTrace();
            }
        }

        return null;
    }

    private String readResponse(int responseLength) throws IOException {


        int readLength = 0;

        long now = System.currentTimeMillis();

        boolean done = false;
        boolean started = false;
        byte[] b = new byte[1];


        readBuffer[0] = 0;

        while (!done && (now-sentTime) < PATIENCE) {

            if (!started) {
                readBuffer[0] = port.readByte();
                int n = readBuffer[0] == (byte)-1 ? -1 : 1;
//                int n = comPort.read(readBuffer, 1);
                if (n > 0) {
                    if (readBuffer[0] == ':') {
                        started = true;
                        readLength = 1;
                        continue;
                    }
                }
            }

            readBuffer[readLength] = port.readByte();
            int n = readBuffer[readLength] == (byte)-1 ? -1 : 1;
            //   int n = comPort.read(readBuffer, readLength, responseLength-readLength);
            now = System.currentTimeMillis();

            if (n > 0) {
                readLength += n;

                if (readLength > 1 && readBuffer[readLength-2] == '\r' && readBuffer[readLength-1] == '\n')
                    done = true;
            }

        }
        if (done && readLength == responseLength) {
            Debug.doReport("SCONController", "Read succeeded (" + readLength + " bytes ): ["+ new String(readBuffer) +  "] \n*** Total time: " + (now - sentTime));
            return new String(readBuffer);
        } else {
            Debug.doReport("SCONController", "Read failed (" + readLength + " bytes ): [" + new String(readBuffer) +  "] \n*** Total time: " + (now - sentTime));
            return null;
        }

    }

    private byte computeLRC(String message) {

        int sum = 0;
        for (int i = 0; i < message.length(); i+=2) {
            sum += Integer.parseInt(message.substring(i, i+2), 16);
        }

        return (byte)(256-sum);
    }


}
