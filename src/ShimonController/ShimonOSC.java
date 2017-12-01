package ShimonController;

import com.illposed.osc.*;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by yn on 11/14/17.
 */

public class ShimonOSC {
    private int OSCport;
    private int OSCoutport;
    private OSCPortIn receiver;
    private boolean isHomed;
    private boolean isHoming;
    private boolean servosOn;
    private boolean isOSCConnected;
    private boolean isSerialConnected;
    private boolean poison;
    private MotorController motors;
    private Shimon shimonModel;
    private BlockingQueue<ActionCommand> motorCommands;
    private Strikers strikers;
    private OSCPortOut sender;
    private static final double MAX_SAFETY_DIFF = 10;
    private boolean isSimulation;

    ShimonOSC() {
        OSCport = 2112;
        OSCoutport = 2113;
        isHomed = false;
        poison = false;
        servosOn = false;
        isSimulation = false;
        isOSCConnected = false;
        isSerialConnected = true; // set to false later when serial stuff is implemented
        shimonModel = new Shimon();
        strikers = new Strikers();
        motorCommands = new LinkedBlockingQueue<ActionCommand>();
        try {
            this.sender = new OSCPortOut(InetAddress.getLocalHost(), OSCoutport);
            this.receiver = new OSCPortIn(OSCport);
        } catch (SocketException e) {
            e.printStackTrace();
            poison = true;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try {
            this.motors = new MotorController("/dev/tty.SLAB_USBtoUART");
        } catch (Exception e) {
            System.out.println("Not connected to Shimon! Entering simulation mode.");
            System.out.println("\"/note_out [midi_note] [velocity]\" are being sent on port " + OSCoutport);
            isSimulation = true;
            isHomed = true;
            servosOn = true;
            strikers = new Strikers(true, sender);
        }
        //set up listeners
        OSCListener mididatalistener = new OSCListener() {
            public void acceptMessage(java.util.Date time, OSCMessage message) {
                if (servosOn & isHomed & isOSCConnected & isSerialConnected) {
                    List<Object> argList = message.getArguments();
                    int note = 60;
                    int vel = 120;
                    double deltaT = 500;
                    if (argList.size() >= 1) {
                        note = (int) (argList.get(0));
                    }
                    if (argList.size() >= 2) {
                        vel = (int) (argList.get(1));
                    }
                    if (argList.size() >= 3) {
                        deltaT = (int) (argList.get(2));
                    }
                    double currentTime = System.currentTimeMillis();
                    String serial_command = shimonModel.mididata(note, vel, currentTime, deltaT);
                    if (serial_command != null) {
                        String[] split_serial = serial_command.split("\\s+");
                        //for(String str : split_serial) System.out.print(str + " ");
                        //System.out.println();
                        int axis = Integer.parseInt(split_serial[0]) - 1;
                        double moveTime = Double.parseDouble(split_serial[1]) + currentTime;
                        int position = Integer.parseInt(split_serial[2]);
                        float accel = Float.parseFloat(split_serial[3]);
                        int vmax = Integer.parseInt(split_serial[4]);
                        double strikeTime = currentTime + deltaT;
                        MotorCommand newCommand = new MotorCommand(axis, position, vmax, note, accel, strikeTime, moveTime, shimonModel, motors, strikers);
                        try {
                            motorCommands.put(newCommand);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            poison = true;
                        }
                    }

                } else {
                    String msg = "Cannot move arms for following reason(s): ";
                    if (!servosOn) msg += "Servos are off. ";
                    if (!isHomed) msg += "Arms not homed. ";
                    if (!isOSCConnected) msg += "OSC disconnected. ";
                    if (!isSerialConnected) msg += "Serial disconnected. ";
                    System.out.println(msg);
                }
                //System.out.println("/mididata: " + message.getArguments());
            }
        };
        OSCListener homelistener = new OSCListener() {
            public void acceptMessage(java.util.Date time, OSCMessage message) {
                try {
                    if (servosOn) {
                        if(!isSimulation) {
                            System.out.print("Homing... ");
                            TimeUnit.MILLISECONDS.sleep(100);
                            motors.sendHome(0);
                            TimeUnit.MILLISECONDS.sleep(100);
                            motors.sendHome(3);
                            TimeUnit.MILLISECONDS.sleep(100);
                            motors.sendHome(1);
                            TimeUnit.MILLISECONDS.sleep(100);
                            motors.sendHome(2);
                            while (true) {
                                long doneHoming = motors.queryHomeEnd(0);
                                TimeUnit.MILLISECONDS.sleep(100);
                                doneHoming += motors.queryHomeEnd(1);
                                TimeUnit.MILLISECONDS.sleep(100);
                                doneHoming += motors.queryHomeEnd(2);
                                TimeUnit.MILLISECONDS.sleep(100);
                                doneHoming += motors.queryHomeEnd(3);
                                TimeUnit.MILLISECONDS.sleep(100);
                                if (doneHoming == 4) break;
                            }
                        }
                        shimonModel = new Shimon();
                        System.out.println("DONE!");
                        isHomed = true;
                    } else {
                        System.out.println("Cannot home - servos are off!");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    poison = true;
                }
            }
        };
        OSCListener strikerlistener = new OSCListener() {
            public void acceptMessage(java.util.Date time, OSCMessage message) {
                List<Object> argList = message.getArguments();
                int striker_index = (int) argList.get(0);
                strikers.strike(striker_index);
            }
        };
        OSCListener servosOnlistener = new OSCListener() {
            public void acceptMessage(java.util.Date time, OSCMessage message) {
                try {
                    if(!isSimulation) {
                        motors.servoOn(0);
                        TimeUnit.MILLISECONDS.sleep(100);
                        motors.servoOn(1);
                        TimeUnit.MILLISECONDS.sleep(100);
                        motors.servoOn(2);
                        TimeUnit.MILLISECONDS.sleep(100);
                        motors.servoOn(3);
                        System.out.println("Servos On");
                        servosOn = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    poison = true;
                }
            }
        };
        OSCListener servosOfflistener = new OSCListener() {
            public void acceptMessage(java.util.Date time, OSCMessage message) {
                try {
                    if(!isSimulation){
                        motors.servoOff(0);
                        TimeUnit.MILLISECONDS.sleep(100);
                        motors.servoOff(1);
                        TimeUnit.MILLISECONDS.sleep(100);
                        motors.servoOff(2);
                        TimeUnit.MILLISECONDS.sleep(100);
                        motors.servoOff(3);
                        System.out.println("Servos Off");
                        isHomed = false;
                        servosOn = false;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    poison = true;
                }
            }
        };
        OSCListener killlistener = new OSCListener() {
            public void acceptMessage(java.util.Date time, OSCMessage message) {
                try {
                    motorCommands.put(new KillCommand());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    poison = true;
                }
            }
        };
        OSCListener requestpositionslistener = new OSCListener() {
            public void acceptMessage(java.util.Date time, OSCMessage message) {
                if (isHomed && isSerialConnected && servosOn) {
                    ArrayList<Object> args = new ArrayList<>();
                    for (int i = 0; i < 4; i++) {
                        double currentTime = System.currentTimeMillis();
                        args.add((int) shimonModel.getArmPosition(i, currentTime));
                    }
                    OSCMessage msg = new OSCMessage("/positions", args);
                    try {
                        sender.send(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        //add listeners
        receiver.addListener("/mididata", mididatalistener);
        receiver.addListener("/on", servosOnlistener);
        receiver.addListener("/off", servosOfflistener);
        receiver.addListener("/home", homelistener);
        receiver.addListener("/kill", killlistener);
        receiver.addListener("/get/positions", requestpositionslistener);
        receiver.addListener("/strike", strikerlistener);

        isOSCConnected = true;
        receiver.startListening();
    }

    private void servosOff(){
        try {
            if(!isSimulation) {
                motors.servoOff(0);
                TimeUnit.MILLISECONDS.sleep(100);
                motors.servoOff(1);
                TimeUnit.MILLISECONDS.sleep(100);
                motors.servoOff(2);
                TimeUnit.MILLISECONDS.sleep(100);
                motors.servoOff(3);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            poison = true;
        }
    }

    //runs main
    private void runMainLoop() throws IOException, InterruptedException {
        if(isOSCConnected && isSerialConnected && !poison) {
            System.out.println("Now listening on port " + this.OSCport);
            System.out.println("Now transmitting on port " + this.OSCoutport);
            System.out.println("Send \'/kill\' OSC command or press [ENTER] to end progrm");

            int size = -1;
            LinkedList<ActionCommand> commands = new LinkedList<ActionCommand>();
            while (System.in.available() == 0 && isOSCConnected && isSerialConnected) {
                ArrayList<ActionCommand> removeList = new ArrayList<ActionCommand>();
                double currentTime = System.currentTimeMillis();
                for(ActionCommand mv : commands){
                    boolean toRemove = mv.schedule(currentTime);
                    if(toRemove){
                        removeList.add(mv);
                        //TimeUnit.MILLISECONDS.sleep(100);
                    }
                }
                if (commands.size() != size) {
                    //System.out.println(motorCommands.size());
                    size = commands.size();
                }
                commands.removeAll(removeList);
                //if(servosOn && isHomed) {
                //    if(motors.testModel(shimonModel, MAX_SAFETY_DIFF)){
                        //servosOn =false;
                        //isHomed = false;
                        //commands = new LinkedList<ActionCommand>();
                        //servosOff();
                //    }
                //}
                //for(int i = 0; i < 4; i++){
                //    if (motors.queryAlarm(i) == 1) {
                //        System.out.println("Error! Arm number "+i+" Alarm");
                //        servosOn =false;
                //        isHomed = false;
                //        commands = new LinkedList<ActionCommand>();
                //        servosOff();
                //    }
                //}
                ActionCommand newCommand = motorCommands.poll();
                if(newCommand instanceof KillCommand) break;
                else if(newCommand != null & isHomed && servosOn) {
                    commands.add(newCommand);
                }
            }
        }
        servosOff();
        String msg = "";
        msg = msg + (!isSerialConnected ? "SERIAL CONNECTION NOT FUNCTIONAL\n" : "");
        msg = msg + (!isOSCConnected ? "OSC CONNECTION NOT FUNCTIONAL ON PORT " + OSCport : "");
        System.out.println(msg);
        System.exit(0);
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        ShimonOSC osc = new ShimonOSC();
        osc.runMainLoop();
    }
}
