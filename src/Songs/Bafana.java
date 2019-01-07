package Songs;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;

public class Bafana extends MaxObject{

    List<int[]> motifs_pitches = new ArrayList<int[]>();
    List<List<Integer>> listeningBuffer = new ArrayList<List<Integer>>();
    List<Integer> noteBuffer = new ArrayList<Integer>(5000);
    List<Long> timeStamps = new ArrayList<Long>(5000);
    Timer timer = new Timer();

    int[] pitchArray;
    int[] durArray;
    int[] armArray;
    int count=0;
    float waitTime;
    volatile float beatDuration = 380.0f;


    int[] pitchArray1;
    int[] durArray1;
    int[] armArray1;
    int count1=0;
    float waitTime1;


    boolean playing =false;
    boolean listening = true;

    volatile int section =0;
    int repeatCount=0;
    int playerRepeatCount=0;

    int currentMotif=0;
    int detectedMotif=-1;
    int firstDetectedMotif=2;
    int firstBassMotif=5;

    volatile boolean headNodInitialized=false;

    long lastTime = System.currentTimeMillis();
    Random rand = new Random();

    public Bafana(){
        declareInlets(new int[]{DataTypes.ALL,DataTypes.ALL});
        declareOutlets(new int[]{DataTypes.ALL,DataTypes.ALL,DataTypes.ALL,DataTypes.ALL,DataTypes.ALL,DataTypes.ALL,DataTypes.ALL,DataTypes.ALL, DataTypes.ALL});
        motifs_pitches.add(getMelody1(72));
        motifs_pitches.add(getMelody2(72));
        motifs_pitches.add(getMelody4(83)); //
        motifs_pitches.add(getMelody9(72));//blues
        //motifs_pitches.add(getMelody16(60));//arab
        motifs_pitches.add(getMelody13(79));//augmented
        motifs_pitches.add(getMelody14(48));//bass
        motifs_pitches.add(getMelody15(48));//bass
        motifs_pitches.add(getMelody16(48));//arab


        listeningBuffer.add(new ArrayList<Integer>(motifs_pitches.get(0).length));
        listeningBuffer.add(new ArrayList<Integer>(motifs_pitches.get(1).length));
        listeningBuffer.add(new ArrayList<Integer>(motifs_pitches.get(2).length));
        listeningBuffer.add(new ArrayList<Integer>(motifs_pitches.get(3).length));
        listeningBuffer.add(new ArrayList<Integer>(motifs_pitches.get(4).length));
        listeningBuffer.add(new ArrayList<Integer>(motifs_pitches.get(5).length));
        listeningBuffer.add(new ArrayList<Integer>(motifs_pitches.get(6).length));
        listeningBuffer.add(new ArrayList<Integer>(motifs_pitches.get(7).length));
        System.out.println("updated!!");
    }

    public void bang()
    {
        if(playing==true){
            int inlet=getInlet();

            switch(section){
                case 0:
                    //unison
                    if(inlet ==0){
                        if(headNodInitialized == false){
                            headNodInitialized = true;
                            timer.schedule(new headNodBehavior(),500);
                        }
                        if(count>= pitchArray.length-1){
                            repeatCount++;
                            //count=0;
                            System.out.println("shimon repeated");
                            if(repeatCount==2){
                                System.out.println("go into canon mode");
                                firstDetectedMotif = currentMotif;
                                System.out.println(firstDetectedMotif);
                                switch(currentMotif){
                                    case 1:
                                        waitTime = beatDuration*9f;
                                        //wait(beatDuration*10);
                                        break;

                                    case 2:
                                        waitTime = beatDuration*8;
                                        //wait(beatDuration*14);
                                        break;
                                    case 3:
                                        //ragaPenta();
                                        waitTime = beatDuration*13.5f;
                                        //wait(beatDuration*10);
                                        break;
                                    default:
                                        break;
                                }
                                section=1;
                                repeatCount=0;
                            }
                        }
                        sendOutputs();
                        if(count>= pitchArray.length){
                            count=0;
                        }
                    }
                    break;


                case 1:
                    //canon
                    if(inlet ==0){
                        if(count>= pitchArray.length-1){
                            //count=0;
                            repeatCount++;
                            if(repeatCount==2){
                                System.out.println("Shimon waits for new motif and new tempo");
                                section = 2;
                                repeatCount=0;
                                listening = true;
                            }
                        }
                        sendOutputs();
                        if(count>= pitchArray.length){
                            count=0;
                        }
                    }
                    break;

                case 2:
                    System.out.println("Shimon listening");
                    headNodInitialized = false;
                    timer.cancel();
                    outlet(8, "gil");
                    //outlet(4,-.8f);
                    //outlet(5,2000);

                    break;

                case 3:

                    //unison
                    if(inlet ==0){
                        if(headNodInitialized == false){
                            headNodInitialized = true;
                            timer = new Timer();
                            timer.schedule(new headNodBehavior(),500);
                        }
                        if(count>= pitchArray.length-1){
                            repeatCount++;
                            if(repeatCount==2){
                                System.out.println("go into canon mode with new motif");
                                switch(currentMotif){
                                    case 1:
                                        waitTime = beatDuration*(.5f)+beatDuration*9f;
                                        //wait(beatDuration*10);
                                        break;

                                    case 2:
                                        waitTime = beatDuration*(1.0f)+beatDuration*8;
                                        //wait(beatDuration*14);
                                        break;
                                    case 3:
                                        //ragaPenta();
                                        waitTime = beatDuration*(.5f)+beatDuration*13.5f;
                                        //wait(beatDuration*10);
                                        break;
                                    default:
                                        break;
                                }
                                section=4;
                                repeatCount=0;
                            }
                        }
                        sendOutputs();
                        if(count>= pitchArray.length){
                            count=0;
                        }
                    }
                    break;

                case 4:
                    //canon
                    if(inlet ==0){
                        if(count>= pitchArray.length-1){
                            //count=0;
                            repeatCount++;

                            if(repeatCount==2){
                                repeatCount=0;
                                System.out.println("picking a new motif and repeating");
                                switch(currentMotif){
                                    case 1:
                                        raga();
                                        waitTime = beatDuration*(.5f)+beatDuration*16;
                                        //wait(beatDuration*10);
                                        break;

                                    case 2:
                                        penta();
                                        waitTime = beatDuration*(1.0f)+beatDuration*20;
                                        //wait(beatDuration*14);
                                        break;
                                    case 3:
                                        ragaPenta();
                                        waitTime = beatDuration*(.5f) + beatDuration*21;
                                        break;
                                    default:
                                        break;
                                }
                                section=5;
                                repeatCount=0;
                                listening = true;
                                count = 0;
                                //wait(beatDuration*10);
                            }
                        }
                        sendOutputs();
                        if(count>= pitchArray.length){
                            count=0;
                        }
                    }
                    break;


                case 5:

                    //playing new motif and listening for new motif by human
                    if(inlet == 0){
                        if(count>= pitchArray.length-1){
                            repeatCount++;

                            if(repeatCount==4){
                                System.out.println("take a rest");
                                repeatCount=0;
                                switch(currentMotif){
                                    case 1:
                                        waitTime = beatDuration*(.5f)+beatDuration*16.0f;
                                        break;

                                    case 2:
                                        waitTime = beatDuration*(1.0f)+beatDuration*20;
                                        break;
                                    case 3:
                                        waitTime =  beatDuration*(.5f) +beatDuration*21;
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }

                        sendOutputs();
                        if(count>= pitchArray.length){
                            count=0;
                        }
                    }
                    break;

                case 6:
                    //playing new motif and listening for new bass motif by human
                    if(inlet == 0){
                        if(count>= pitchArray.length-1){
                            repeatCount++;

                            if(repeatCount==4){
                                System.out.println("take a rest and switch motifs");
                                repeatCount=0;
                                switch(currentMotif){
                                    case 1:
                                        penta();
                                        waitTime = beatDuration*(1.0f)+beatDuration*18.0f;
                                        break;
                                    case 2:
                                        augmented();
                                        waitTime = beatDuration*(1.0f)+beatDuration*18.0f;
                                        break;
                                    case 3:
                                        raga();
                                        waitTime = beatDuration*(1.0f)+beatDuration*18.0f;
                                        break;
                                    case 4:
                                        raga();
                                        waitTime = beatDuration*(1.0f)+beatDuration*18.0f;
                                        break;

                                    case 6:
                                        blues();
                                        waitTime = beatDuration*32;
                                        break;
                                    case 9:
                                        augmented();
                                        waitTime =  beatDuration*(.5f) +beatDuration*16;
                                        break;
                                    default:
                                        break;
                                }
                                count = 0;
                            }

                        }
                        sendOutputs();
                        if(count>= pitchArray.length){
                            count=0;
                        }
                    }
                    break;

                case 7:
                    //bass line detected, repeating twice then adding additional motifs
                    if(inlet ==1){
                        if(count1>= pitchArray1.length-1){
                            repeatCount++;

                            if(repeatCount==2){
                                System.out.println("listening for last bass line now");
                                pentaHigh();
                                waitTime = beatDuration*(1.0f);
                                count=0;
                                section = 8;
                                repeatCount=0;
                                sendOutputs();
                                listening = true;
                            }

                        }
                        sendOutputs1();
                        if(count1>= pitchArray1.length){
                            count1=0;
                        }
                    }
                    break;

                case 8:
                    //listening for final bass line to trigger ending
                    if(inlet ==1){
                        sendOutputs1();
                        if(count1>= pitchArray1.length){
                            count1=0;
                        }
                    }

                    if(inlet ==0){
                        if(count>= pitchArray.length-1){
                            repeatCount++;

                            if(repeatCount==3){
                                augmented();
                                waitTime = beatDuration*(1.0f);
                                count=0;
                            }

                            if(repeatCount==7){
                                blues();
                                waitTime = beatDuration*(1.0f);
                                count=0;
                            }

                            if(repeatCount==11){
                                ragaPenta();
                                waitTime = beatDuration*(1.0f);
                                count=0;
                            }

                            if(repeatCount==15){
                                raga();
                                waitTime = beatDuration*(1.0f);
                                count=0;
                            }

                            if(repeatCount==20){
                                pentaHigh();
                                waitTime = beatDuration*(1.0f);
                                count=0;
                            }

                        }
                        sendOutputs();
                        if(count>= pitchArray.length){
                            count=0;
                        }
                    }
                    break;

                case 9:
                    if(inlet ==1){
                        sendOutputs1();
                        if(count1>= pitchArray1.length){
                            count1=0;
                        }
                    }

                    if(inlet ==0){
                        if(count>= pitchArray.length-1){
                            repeatCount++;

                            if(repeatCount==4){
                                section=10;
                                repeatCount=0;
                                timer.cancel();
                                headNodInitialized=false;
                            }
                        }
                        sendOutputs();
                        if(count>= pitchArray.length){
                            count=0;
                        }
                    }
                    break;

                default:
                    break;

            }
        }
    }

    private void sendOutputs(){
        outlet(0,pitchArray[count]);
        outlet(1,armArray[count]-1);
        outlet(2,durArray[count]/100.0f * beatDuration + waitTime);
        count++;
        waitTime=0;
    }

    private void sendOutputs1(){
        outlet(0,pitchArray1[count1]);
        outlet(1,armArray1[count1]-1);
        outlet(3,durArray1[count1]/100.0f * beatDuration + waitTime1);
        count1++;
        waitTime=0;
    }

    public void stop(){
        playing = false;
    }
    public void play(){
        playing = true;
        listening = true;
        section = 0;
        repeatCount=0;
        playerRepeatCount=0;
        currentMotif=0;
        detectedMotif=-1;
        count1=0;
        count=0;
    }

    public void setSection(int i){
        section = i;
        count=0;
        count1=0;
        repeatCount=0;
        playerRepeatCount=0;
        detectedMotif=-1;
        currentMotif=1;
        System.out.println("section = " + section);
        ragaPenta();
        listening = true;
    }


    public void listen(int notePlayed){

        if(System.currentTimeMillis() - lastTime> 60){

            //add to buffer
            noteBuffer.add(notePlayed);
            timeStamps.add(System.currentTimeMillis());


            switch(section){
                case 0:
                    //listening for first motif to set tempo
                    if(listening == true){
                        detectedMotif = analyzeInput(new int[]{0,1});
                        if(detectedMotif != -1){
                            playerRepeatCount+=1;
                            //System.out.println("repeated");
                        }
                        if(playerRepeatCount == 1){
                            setMotif(detectedMotif);
                            count=0;
                            setTempo();
                            int motifLen = motifs_pitches.get(detectedMotif).length;
                            float delay =0.0f;
                            for(int i=1;i<6;i++){
                                delay += (durArray[motifLen-i]/100.0f * beatDuration);
                            }
                            //System.out.println(delay);
                            outlet(2,(int)(delay - 500));
                            listening = false;
                            playerRepeatCount = 0;
                        }
                    }
                    break;
                case 2:
                    //listening 2nd time around
                    if(listening == true){
                        detectedMotif = analyzeInput(new int[]{0,1,2});
                        if(detectedMotif != -1){
                            playerRepeatCount+=1;
                            //System.out.println("repeated");
                        }
                        if(playerRepeatCount == 2){
                            setMotif(detectedMotif);
                            setTempo();
                            int motifLen = motifs_pitches.get(detectedMotif).length;
                            float delay =0.0f;
                            for(int i=1;i<6;i++){
                                delay += (durArray[motifLen-i]/100.0f * beatDuration);
                            }
                            System.out.println(delay);
                            outlet(2,(int)(delay - 500));
                            section = 3;
                            listening = false;
                            playerRepeatCount=0;
                            count=0;
                        }
                    }
                    break;

                case 5:
                    //listening for augmented,arab, or blues
                    if(listening == true){
                        detectedMotif = analyzeInput(new int[]{3,4,7});
                        if(detectedMotif != -1){
                            playerRepeatCount+=1;
                            System.out.println("detected Motif = "+ detectedMotif);
                        }
                        if(playerRepeatCount == 1){
                            setMotif(detectedMotif);
                            int motifLen = motifs_pitches.get(detectedMotif).length;
                            float delay =0.0f;
                            for(int i=1;i<6;i++){
                                delay += (durArray[motifLen-i]/100.0f * beatDuration);
                            }

                            System.out.println(delay);
                            outlet(2,(int)(delay - 500));
                            section = 6;
                            playerRepeatCount=0;
                            repeatCount=0;
                            count=0;
                            count1=0;
                        }
                    }
                    break;

                case 6:
                    //listening for 2 bass motifs
                    if(listening == true){
                        detectedMotif = analyzeInput(new int[]{5,6});
                        if(detectedMotif != -1){
                            playerRepeatCount+=1;
                            //System.out.println("repeated");
                        }
                        if(playerRepeatCount == 2){

                            setMotif(detectedMotif);
                            int motifLen = motifs_pitches.get(detectedMotif).length;
                            float delay =0.0f;
                            for(int i=1;i<6;i++){
                                delay += (durArray[motifLen-i]/100.0f * beatDuration);
                            }

                            System.out.println(delay);
                            outlet(2,(int)(delay - 500));
                            outlet(3,(int)(delay - 500));
                            section = 7;
                            //listening = true;
                            playerRepeatCount=0;
                            count=0;
                            count1=0;
                            repeatCount=0;
                            firstBassMotif=detectedMotif;
                        }
                    }
                    break;

                case 8:

                    //listening for 2 bass motifs to trigger the ending
                    if(listening == true){
                        if(firstBassMotif == 5){
                            detectedMotif = analyzeInput(new int[]{6});
                        }else{
                            detectedMotif = analyzeInput(new int[]{5});
                        }
                        if(detectedMotif != -1){
                            playerRepeatCount+=1;
                            //System.out.println("repeated");
                        }
                        if(playerRepeatCount == 2 && detectedMotif != -1){

                            setMotif(detectedMotif);
                            System.out.println("section 8 detectedMotif = " + detectedMotif);
                            int motifLen = motifs_pitches.get(detectedMotif).length;
                            float delay =0.0f;
                            for(int i=1;i<6;i++){
                                delay += (durArray1[motifLen-i]/100.0f * beatDuration);
                            }
                            System.out.println(delay);
                            switch(firstDetectedMotif){
                                case 1:
                                    ragaPenta();
                                    break;
                                case 2:
                                    raga();
                                    break;
                                case 4:
                                    penta();
                                    break;
                                default:
                                    break;
                            }

                            outlet(2,(int)(delay - 500));
                            outlet(3,(int)(delay - 500));
                            section = 9;
                            listening = true;
                            playerRepeatCount=0;
                            repeatCount=0;
                            count1=0;
                            count=0;
                        }
                    }
                    break;

			/*case 9:
				if(listening == true){
					detectedMotif = analyzeInput(new int[]{3,4,7});
					if(detectedMotif != -1){
						playerRepeatCount+=1;
						//System.out.println("repeated");
					}
					if(playerRepeatCount == 2 && detectedMotif != -1){

						setMotif(detectedMotif);
						System.out.println("detectedMotif = " + detectedMotif);
						int motifLen = motifs_pitches.get(detectedMotif).length;
						float delay =0.0f;
						for(int i=1;i<6;i++){
							delay += (durArray1[motifLen-i]/100.0f * beatDuration);
						}
						System.out.println(delay);
						outlet(3,(int)(delay - 500));


						switch(firstDetectedMotif){
						case 1:
							ragaPenta();
							break;
						case 2:
							raga();
							break;
						case 4:
							penta();
							break;
						default:
						break;
						}

						outlet(2,(int)(delay - 500));

						section = 9;
						listening = false;
						playerRepeatCount=0;
						count1=0;
						count=0;
						System.out.println("Ending triggered");
					}
				}
				break;
				*/
                default:
                    break;
            }

            lastTime = System.currentTimeMillis();
        }

    }

    public int analyzeInput(int[] motifOptions){
        int motifDetected = -1;

        //compare
        float countCorrect = 0.0f;
        float percentMatch;
        int motif;
        for(int index=0;index<motifOptions.length;index++){
            motif = motifOptions[index];
            if(noteBuffer.size()>(motifs_pitches.get(motif).length-4)){
                //System.out.println(noteBuffer.get(noteBuffer.size()-1));
                countCorrect = 0;
                for(int i=0;i<(motifs_pitches.get(motif).length-4);i++){
                    //System.out.println(motifs_pitches.get(motif)[i] + "  " + noteBuffer.get(i));
                    if(motifs_pitches.get(motif)[i] == noteBuffer.get((noteBuffer.size()-(motifs_pitches.get(motif).length-4))+i)){
                        countCorrect+=1.0f;
                    }
                }
                percentMatch = countCorrect / (motifs_pitches.get(motif).length-4);
                if(percentMatch >=.7f){
                    //System.out.println("motif detected!");
                    motifDetected = motif;
                    break;
                }
            }
        }
        return motifDetected;
    }

    private void setTempo(){

        int duration =  (int) (timeStamps.get(timeStamps.size()-1) - timeStamps.get(timeStamps.size()-(motifs_pitches.get(detectedMotif).length-4)));

        switch(detectedMotif){
            case 0:
                //beatDuration = duration / 16.5f;
                beatDuration = duration / 13.5f;
                break;
            case 1:
                //beatDuration = duration / 19.0f;
                beatDuration = duration / 16f;
                break;
            case 2:
                //beatDuration = duration / 20.5f;
                beatDuration = duration / 18.5f;
                break;
            default:
                break;
        }
        System.out.println("beat duration = "+beatDuration);
    }


    protected double compareStrings(String first, String second){

        int maxLength = Math.max(first.length(), second.length());
        //Can't divide by 0
        if (maxLength == 0) return 1.0d;
        return ((double) (maxLength - computeEditDistance(first, second))) / (double) maxLength;

    }

    private void wait(float duration){

        try {
            Thread.sleep((int)duration);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    protected int computeEditDistance(String first, String second) {
        first = first.toLowerCase();
        second = second.toLowerCase();

        int[] costs = new int[second.length() + 1];
        for (int i = 0; i <= first.length(); i++) {
            int previousValue = i;
            for (int j = 0; j <= second.length(); j++) {
                if (i == 0) {
                    costs[j] = j;
                }
                else if (j > 0) {
                    int useValue = costs[j - 1];
                    if (first.charAt(i - 1) != second.charAt(j - 1)) {
                        useValue = Math.min(Math.min(useValue, previousValue), costs[j]) + 1;
                    }
                    costs[j - 1] = previousValue;
                    previousValue = useValue;

                }
            }
            if (i > 0) {
                costs[second.length()] = previousValue;
            }
        }
        return costs[second.length()];
    }


    private void setMotif(int i){
        switch(i){
            case 0:
                ragaPenta();
                break;
            case 1:
                raga();
                break;
            case 2:
                penta();
                break;
            case 3:
                blues();
                break;
            case 4:
                augmented();
                break;
            case 5:
                bass1();
                break;
            case 6:
                bass2();
                break;
            case 7:
                arab();
                break;
            default:
                break;

        }
    }

    public void ragaPenta(){
        melody1(72,3);

    }

    public void raga(){
        melody2(72,3);

    }

    public void pentaHigh(){
        melody4(83,3);
    }

    public void penta(){
        //melody4(83,3);
        melody4(71,3);
    }


    public void blues(){
        melody9(72,3);
    }

    public void augmented(){
        melody13(79,3);
    }

    public void arabHigh(){
        melody16(72,1);
    }
    public void arab(){
        melody16(60,1);
    }

    public void bass1(){
        melody14(48,1);
    }
    public void bass2(){
        melody15(48,1);
    }


    private void melody1(int note,int arm)
    {

        currentMotif=1;

        System.out.println("raga penta set");

        //		9 beats *highest note = note+10, *lowest note= note-2
        durArray=new int[] {100,50,50,100,50,50,100,50,50,100,100,50,50,100,50,50,50,50,100,50,50,100,50,50};
        pitchArray=new int[] {note,note+4,note+5,note+7,note+5,note+4,note+5,note+7,note+10,note+7,note+5,note,note-2,note,note+2,note+5,note+4,note+5,note+7,note+9,note+12,note+14,note+12,note+9};


        armArray=new int[pitchArray.length];
        for (int i=0;i<pitchArray.length;i++)
        {
            armArray[i]=arm+1;
        }
        armArray[0]=arm;
        armArray[11]=arm;
        armArray[12]=arm;
        armArray[13]=arm;
    }

    private int[] getMelody1(int note){
        return new int[] {note,note+4,note+5,note+7,note+5,note+4,note+5,note+7,note+10,note+7,note+5,note,note-2,note,note+2,note+5,note+4,note+5,note+7,note+9,note+12,note+14,note+12,note+9};

    }


    public void melody2(int note,int arm)
    {
        currentMotif=2;
        System.out.println("raga set");
        //		20 beats arm+1 starts at note+4, *highest note = note+12 *lowest note=note-5
        armArray=new int[] {arm,arm,arm+1,arm,arm+1,arm,arm+1,arm,arm+1,arm+1,arm+1,arm+1,arm+1,arm+1,arm+1,arm+1,arm,arm,arm,arm,arm,arm+1,arm,arm,arm,arm,arm,arm+1};
        durArray=new int[] {100,50,100,50,100,50,100,50,50,50,100,100,50,100,50,100,50,50,50,50,50,50,100,100,50,100,50,100};
        pitchArray=new int[] {note,note,note+4,note,note+5,note,note+7,note,note+10,note+12,note+10,note+7,note+5,note+4,note+5,note+4,note,note-2,note-5,note-2,note,note+4,note,note,note-5,note-2,note,note+4};
    }

    private int[] getMelody2(int note){
        return new int[] {note,note,note+4,note,note+5,note,note+7,note,note+10,note+12,note+10,note+7,note+5,note+4,note+5,note+4,note,note-2,note-5,note-2,note,note+4,note,note,note-5,note-2,note,note+4};

    }

    public void melody4(int note,int arm)
    {
        currentMotif=3;
        System.out.println("penta set");
        //		21 beats
        int vel = 65;
        armArray=new int[] {arm+1,	arm,	arm+1,	arm,	arm,	arm,	arm+1,	arm+1,	arm+1,	arm+1,	arm,	arm+1,		arm,	arm+1,		arm,	arm+1,		arm,	arm,	arm+1,	arm+1,	arm+1,	arm+1,	arm+1,	arm,	arm+1,		arm,	arm+1,		arm,	arm+1,		arm,		arm+1,	arm+1,	arm+1,	arm+1,	arm+1};
        durArray=new int[] {50,		50,		50,		50,		50,		50,		50,		50,		50,		50,		50,		100,		50,		100,		50,		100,		50,		50,		50,		50,		50,		50,		50,		50,		100,		50,		100,		50,		100,		50,			50,		50,		100,	50,		50};
        pitchArray=new int[] {note,	note-4,	note,	note-4,	note-2,	note-4,	note+1,	note+3,	note+5,	note+1,	note-4,	note+3,		note-4,	note+1,		note-7,	note+1,		note-4,note+-2,	note+3,	note+1,	note+3,	note+5,	note+1,	note-4,	note+10,	note-4,	note+8,		note-4,	note+5,		note-4,		note+3,	note+5,	note+1,	note+5,	note+1};
    }

    private int[] getMelody4(int note){
        return new int[]{note,	note-4,	note,	note-4,	note-2,	note-4,	note+1,	note+3,	note+5,	note+1,	note-4,	note+3,		note-4,	note+1,		note-7,	note+1,		note-4,note+-2,	note+3,	note+1,	note+3,	note+5,	note+1,	note-4,	note+10,	note-4,	note+8,		note-4,	note+5,		note-4,		note+3,	note+5,	note+1,	note+5,	note+1};

    }


    public void melody9(int note,int arm)
    {

        currentMotif= 4;
        armArray = new int[] {    arm,    arm,    	arm,    	arm,    	arm,    	arm,    	arm,    	arm,    	arm+1,    	arm+1,   	arm,    	arm,    	arm,    	arm,    	arm+1,    	arm+1,    	arm+1,    	arm+1,    	arm+1,    	arm+1,    	arm+1,    	arm+1,    	arm,    	arm,    	arm+1,		arm,    	arm,    	arm,    	arm,    	arm,    	arm+1,    	arm,    	arm,    arm+1,    arm+1,    arm+1,    arm+1,    arm,    arm,    arm,    arm+1,    arm,    arm,    arm,    arm,    arm,    arm,    arm,    arm,    arm,    arm,    arm,    arm,    arm,    arm+1,    arm+1,    arm+1,    arm+1,    arm,    arm+1,    arm,    arm,    arm,    arm,    arm,    arm,    arm,    arm,    arm    };
        durArray = new int[] {    50,    50,    	50,    		50,    		100,    	50,    		50,    		50,    		50,    		100,    	50,    		50,    		50,    		50,    		50,    		50,    		50,    		50,    		50,    		50,    		50,    		50,    		50,    		50,    		0,			50,    		50,    		100,    	50,    		50,    		50,    		50,    		100,    	100,    50,    100,    50,    50,    50,    50,    50,    50,    50,    50,    50,    100,    150,    100,    50,    100,    50,    50,    150,    50,    100,    50,    50,    100,    50,    150,    50,    50,    50,    50,    100,    100,    100,    100        };
        pitchArray = new int[] { note,    note+3,   note+5,     note+7,    	note+6,    	note+0,    	note+3,    	note+5,    	note+10,    note+12,    note+0,    	note+3,    	note+5,    	note+6,    	note+12,    note+10,    note+15,    note+12,    note+17,    note+15,    note+12,    note+10,    note+7,    note+6,    	note+15,	note+5,    	note+3,    	note+0,    	note+12,    note+10,    note+15,    note+10,    note+12 };

    }
    private int[] getMelody9(int note){
        return new int[] { note,    note+3,   note+5,     note+7,    	note+6,    	note+0,    	note+3,    	note+5,    	note+10,    note+12,    note+0,    	note+3,    	note+5,    	note+6,    	note+12,    note+10,    note+15,    note+12,    note+17,    note+15,    note+12,    note+10,    note+7,    note+6,    	note+15,	note+5,    	note+3,    	note+0,    	note+12,    note+10,    note+15,    note+10,    note+12 };

    }


    public void melody13(int note,int arm)
    {
        currentMotif = 6;
        //		16 beats *highest = note+23  **arm+1 starts at note+16
        durArray=new int[] {50,50,50,50,100,100,150,150,100,150,150,100,150,150,100,50,50,50,50,100,100,150,150,100,150,150,100,400};
        pitchArray=new int[] {note,note+4,note+8,note+11,note+12,note+16,note+20,note+23,note+22,note+19,note+15,note+11,note+10,note+7,note+3,note,note+4,note+8,note+11,note+12,note+16,note+20,note+23,note+22,note+19,note+15,note+11,note+11};
        armArray=new int[pitchArray.length];
        for (int i=0;i<pitchArray.length;i++)
        {
            armArray[i]=arm+1;
        }
        armArray[0]=arm;
        armArray[1]=arm;
        armArray[14]=arm;
        armArray[15]=arm;
        armArray[16]=arm;
        armArray[pitchArray.length-1]=arm;

    }
    private int[] getMelody13(int note){
        return new int[]{note,note+4,note+8,note+11,note+12,note+16,note+20,note+23,note+22,note+19,note+15,note+11,note+10,note+7,note+3,note,note+4,note+8,note+11,note+12,note+16,note+20,note+23,note+22,note+19,note+15,note+11,note+11};

    }

    public void melody16(int note,int arm)
    {
        //		16 beats
        //		base note = 48


        currentMotif = 9;
        pitchArray=new int[] {note+10,		note+8,		note+7,		note+5,		note+4,		note+7,		note+1,		note+0,		note-2,		note+0,		note+1,		note-2,		note+0,		note+1,		note+4,		note+7,		note+10,		note+8,		note+7,		note+5,		note+4,		note+7,		note+1,		note+0,		note-2,		note+0,		note+1,		note-2,		note+0,		note+1,		note+0};
        durArray=new int[pitchArray.length];
        for (int i=0;i<pitchArray.length;i++)
        {
            durArray[i]=50;
        }

        armArray=new int[pitchArray.length];
        for (int i=0;i<pitchArray.length;i++)
        {
            armArray[i]=arm;
        }



    }

    private int[] getMelody16(int note){
        return new int[] {note+10,		note+8,		note+7,		note+5,		note+4,		note+7,		note+1,		note+0,		note-2,		note+0,		note+1,		note-2,		note+0,		note+1,		note+4,		note+7,		note+10,		note+8,		note+7,		note+5,		note+4,		note+7,		note+1,		note+0,		note-2,		note+0,		note+1,		note-2,		note+0,		note+1,		note+0};

    }


    public void melody14(int note,int arm)
    {

        currentMotif = 7;
        //durArray1=new int[] {150,100,100,150,100,100};
		/*durArray1=new int[] {50,50,50,50,50,50,50,50,50,50,50,50,50,50};
		pitchArray1=new int[] {note,note,note,note+3,note+3,note+3,note+3,note+2,note+2,note+2,note+1,note+1,note+1,note+1};
		//pitchArray1=new int[] {note,note+3,note+3,note+2,note+1,note+1};
		armArray1=new int[pitchArray1.length];
		for (int i=0;i<pitchArray1.length;i++)
		{
			armArray1[i]=arm;
		}
		*/

        durArray1=new int[] {150,100,100,150,100,100,150,100,100,150,100,100};
        pitchArray1=new int[] {note,note+3,note+3,note+2,note+1,note+1,note,note+3,note+3,note+2,note+1,note+1};
        armArray1=new int[pitchArray.length];
        for (int i=0;i<pitchArray.length;i++)
        {
            armArray1[i]=arm;
        }
    }

    private int[] getMelody14(int note){
        return new int[] {note,note+3,note+3,note+2,note+1,note+1,note,note+3,note+3,note+2,note+1,note+1};
    }

    public void melody15(int note,int arm)
    {
        currentMotif = 8;
//		9 beats  *highest note = note+12
        armArray1=new int[] {arm,arm+1,arm+1,arm,arm,arm,arm,arm,arm,arm,arm,arm,arm};
        durArray1=new int[] {150,150,200,100,100,100,150,150,200,100,100,100};
        pitchArray1=new int[] {note,note+10,note+12,note,note+5,note+6,note+7,note+10,note+9,note+8,note+7,note+6};

		/*for (int i=0;i<pitchArray.length;i++)
		{
			armArray[i]=arm;
		}*/
    }
    private int[] getMelody15(int note){
        return new int[] {note,note+10,note+12,note,note+5,note+6,note+7,note+10,note+9,note+8,note+7,note+6};
    }


    class headNodBehavior extends TimerTask {

        public void run(){

            if(headNodInitialized){

                if(rand.nextFloat()>.6f){
                    float lookAtPosition = rand.nextFloat()*1 - .5f;
                    outlet(4,lookAtPosition);
                }


                if(section <4){
                    outlet(7,.1f);
                    outlet(5,(int)(beatDuration*2));
                }

                if(section==4){
                    if(rand.nextFloat()>.6f){
                        float neckPos = rand.nextFloat()*1 - .7f;
                        outlet(6,neckPos);
                    }
                }

                if(section == 5 || section == 6){
                    if(rand.nextFloat()>.6f){
                        float neckPos = rand.nextFloat()*.8f - .7f;
                        outlet(7,neckPos);
                    }
                    outlet(5,(int)(beatDuration*2));
                }


                if(section == 7){
                    if(rand.nextFloat()>.6f){
                        float neckPos = rand.nextFloat()*1 - .7f;
                        outlet(6,neckPos);
                    }
                }

                if(section >=8){

                    if(rand.nextFloat()>.6f){
                        float neckPos = rand.nextFloat()*0.5f - .2f;
                        outlet(7,neckPos);
                    }
                    outlet(5,(int)(beatDuration*2));
                }

                timer.schedule(new headNodBehavior(),(int)(beatDuration*2));

            }
        }

    }

}
