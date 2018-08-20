package Songs;

import java.util.*;

import com.cycling74.max.Atom;
import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;

public class CallAndRespond_Simple extends MaxObject{



    Timer timer1 = new Timer();
    Timer timer2 = new Timer();
    Timer timer3 = new Timer();
    Timer timer4 = new Timer();
    Timer timer5 = new Timer();
    volatile List<Integer> noteBuffer = new ArrayList<Integer>(5000);
    volatile List<Long> timeStamps = new ArrayList<Long>(5000);
    long lastTime;
    volatile int timerIndex;
    volatile boolean listening = true;
    boolean hiphopOn = false;
    float headNodInterval;
    double tempo;
    boolean looked_at_jason = false;
    boolean first_note = false;

    volatile int hipHopSection=0;
    volatile int currentSection=-1;
    volatile int hiphopIndex=0;
    float[][] countIn = new float[][]{{62,62,62,62,62,62,62,62},{.5f,.5f,.5f,.5f,.5f,.5f,.5f,.5f}};
    float[][] hipHopMotif1 = new float[][]{{62,62,62,62,62,62,62,62,  62,64,65,62,64,62,69  ,67,65,64},{.5f,.5f,.5f,.5f,.5f,.5f,.5f,.5f, .25f,.25f,.25f,.25f,.25f,.25f,1f,.5f,.5f,.5f}};
    float[][] hipHopMotif2 = new float[][]{{69,71,72,71,69, 69,71,72,71,69,  76,74,72, 74,71,72,},{.25f,.25f,.25f,.25f,1.f, .25f,.25f,.25f,.25f,1.f, .75f,.75f,1.5f,.25f,.25f,.5f}};
    float[][] hipHopMotif3 = new float[][]{{65,64,62,57, 65,64,62,57, 65,64,62,57, 70,69, 65,64,62,57, 65,64,62,57, 65,64,62,57, 67,65},{.25f,.25f,.25f,.25f,.25f,.25f,.25f,.25f,.25f,.25f,.25f,.25f,.5f,.5f,.25f,.25f,.25f,.25f,.25f,.25f,.25f,.25f,.25f,.25f,.25f,.25f,.5f,.5f}};
    float[][] hipHopMotif4 = new float[][]{{70,67,69,65,67,64,65,62, 70,67,69,65,67,64,65,62}, {.5f,.5f,.5f,.5f,.5f,.5f,.5f,.5f, .5f,.5f,.5f,.5f,.5f,.5f,.5f,.5f}};

    volatile int rhythmIndex = 0;
    volatile long extraRhythm = 0;
    float[] rhythm1 = new float[] {(.5f/3f),(.5f/3f), (.5f/3f), .125f, .125f,.125f, .125f, 1.f, (.5f/3f),(.5f/3f), (.5f/3f), (.5f/3f),(.5f/3f), (.5f/3f), 1.f, .5f, .5f, 1.f, .5f, .5f, 1.f};
    float[] rhythm2 = new float[] {(.5f/6f),(.5f/6f), (.5f/6f),(.5f/6f),(.5f/6f), (.5f/6f), .5f, .25f, .25f, .5f, 1.f, 1.f, .25f, .25f, .25f, .25f, .25f, .25f, .25f, .25f, 1f/3f, 1f/3f, 1f/3f, 1f/3f, 1f/3f, 1f/3f};
    float[] rhythm3 = new float[] {.25f, .25f, .125f, .125f, .125f, .125f, .5f, .5f, 1f/3f, 1f/3f, 1f/3f, 1f/3f, 1f/3f, 1f/3f, .5f, .5f, .125f, .125f, .125f, .125f,.125f, .125f, .125f, .125f, .5f, .5f, .125f, .125f, .125f, .125f,.125f, .125f, .125f, .125f};
    float[] rhythm4 = new float[] {.25f,.25f,.25f,.25f, .5f, .5f, .125f, .125f, .125f, .125f,.125f, .125f, .125f, .125f, .5f, .5f, (.5f/3f),(.5f/3f), (.5f/3f), (.5f/3f),(.5f/3f), (.5f/3f), (.5f/6f),(.5f/6f), (.5f/6f),(.5f/6f),(.5f/6f), (.5f/6f),.5f, .5f,.5f, .5f,.5f};

    float[][] randomMotif = new float[2][];
    float[][] learnedMotif = null;
    int randomizer = 0;

    ArrayList<float[][]> motif_collection = new ArrayList<>();
    int motif_counter = 0;
    int num_motifs = 4;
    int num_musicians = 4;
    boolean is_keyboard = true;
    int listening_to = 0;
    int num_repeats = 2;
    int current_repeat = 0;


    int triggerCount=0;
    int mode =0;
    int hiphopTriggerNote=38;

    int ShimonListenPosition;
    int ShimonPlayPosition;
    int nodCount=0;

    int endNodCount=0;
    int behaviorNodCount=0;
    int nodType = 3;
    int lastNote = -1;
    MotifLearner motifs;
    MotifLearner instrument;

    Random rand = new Random();

    public CallAndRespond_Simple(){
        declareInlets(new int[]{DataTypes.ALL,DataTypes.ALL});
        declareOutlets(new int[]{
                DataTypes.ALL,DataTypes.ALL,DataTypes.ALL,
                DataTypes.ALL,DataTypes.ALL,DataTypes.ALL,
                DataTypes.ALL, DataTypes.ALL, DataTypes.ALL,
                DataTypes.ALL});
        lastTime = System.currentTimeMillis();
        motifs = new MotifLearner();
        instrument = new MotifLearner();
        timer3.schedule(new set_defaults(), 100);
    }

    public void printStates(){
        System.out.println("listening: "+ listening);
        System.out.println("hiphopNod: "+ hiphopOn);
        System.out.println("notes in buffer: " + noteBuffer.size());
    }


    public void bang(){

        int inlet = getInlet();
        if(inlet == 0){


            if(hiphopOn == false){

                playBack();

            }else{
                hipHopSection++;
                System.out.println(hipHopSection);
            }

        }
    }

    public void listen(int notePlayed){
        listen(notePlayed, -1, 0);
    }

    public void listen(int notePlayed, int instrumentIndex, double latency){
        is_keyboard = (instrumentIndex == -1);

        if(listening){
            if(hiphopOn){
                //hip hop listening
                if (notePlayed %12 == 2 && notePlayed != hiphopTriggerNote){
                    noteBuffer.add(notePlayed);
                    timeStamps.add(System.currentTimeMillis());

                    if(noteBuffer.size()==8){
                        float duration = (float)(timeStamps.get(timeStamps.size()-1) - timeStamps.get(1));
                        duration = duration/6.0f;
                        headNodInterval = (int)(duration *2);
                        tempo = (60000.0 / headNodInterval);
                        System.out.println("tempo detected = "+ tempo);
                        outlet(7, headNodInterval);
                        listening = false;
                        startHeadNod((int)(headNodInterval/2));
                    }
                }

            }else{

                //regular listening
                if(System.currentTimeMillis() - lastTime> 60){
                    noteBuffer.add(notePlayed);
                    timeStamps.add(System.currentTimeMillis());
                    if(instrumentIndex >= 0) {motifs.addNoteInstrument(notePlayed);}
                    else {motifs.addNote(notePlayed);}
                }

                if(notePlayed == hiphopTriggerNote){
                    triggerHiphop();
                }
            }
        } else {
            if(instrumentIndex >= 0) {
                instrument.addNoteInstrument(notePlayed, latency);
                lastNote = notePlayed;
            }
        }
    }

    public void playBack(){
        listening = false;
        if(true) {
            float[][] new_motif = motifs.generateMotif();
            if (new_motif != null) {
                switch (motif_counter) {
                    case 0:
                        hipHopMotif1 = MotifLearner.addPulse(new_motif, 0.5, 62);
                        break;
                    case 1:
                        hipHopMotif2 = new_motif;
                        break;
                    case 2:
                        hipHopMotif3 = MotifLearner.addPulse(new_motif, 1, 50);
                        break;
                    case 3:
                        hipHopMotif4 = new_motif;
                        break;
                }
                motif_collection.add(new_motif);
                motif_counter = (motif_counter + 1) % num_motifs;
                randomMotif = motif_collection.get(rand.nextInt(motif_collection.size()));
                System.out.println("learned new motif");
            } else {
                outlet(8, "no");
            }
        }
        listening_to = (listening_to + 1) % num_musicians;
        Atom[] output = new Atom[] {Atom.newAtom("instrument"), Atom.newAtom(listening_to)};
        outlet(8, output);
        timer1 = new Timer();
        timerIndex=0;
        timer1.schedule(new playBackThread(),0);
    }

    public void playBackReverse(){
        listening = false;
        timer1 = new Timer();
        timerIndex=0;
        timer1.schedule(new playBackThread(),0);
    }

    public void cancelPlayBack(){
        timer1.cancel();
        timer2.cancel();
    }

    public void stopListening(){
        listening = false;
    }

    public void startListening(){
        clearBuffers();
        listening = true;
    }

    public void triggerHiphop(){
        clearBuffers();
        listening = true;
        hiphopOn = true;
    }

    public void playHipHop(int delay){
        listening = false;
        timer2 = new Timer();
        timer2.schedule(new playHipHop(),delay);
    }

    public void clearBuffers(){
        noteBuffer.clear();
        timeStamps.clear();
        motifs.clear();
        //System.out.println("cleared buffer");
    }

    public void startHeadNod(int delay){
        listening = false;
        timer1 = new Timer();
        timer1.schedule(new headNod(),delay);
    }
    public void startHeadNodBehavior(){
        listening = false;
        timer1 = new Timer();
        timer1.schedule(new headNodBehavior(),0);
    }

    public void lookRight(){
        outlet(1,1.1f);
        outlet(3,1000);
    }

    public void lookCenter(){
        outlet(1,0f);
        outlet(3,1000);
    }


    class headNod extends TimerTask {

        long waitTime;
        public void run(){

            if(nodCount <=16){
                if(nodCount<8){
                    outlet(1,1.1f);
                }else{
                    if(!looked_at_jason) {
                        outlet(8, "jason");
                        looked_at_jason = true;
                    }

                    outlet(1,-1.1f);
                }
                waitTime = (int)headNodInterval;
                outlet(2,headNodInterval);
                nodCount++;
                timer1.schedule(new headNod(),waitTime);
                System.out.println();

            }else{
                waitTime = (int)(headNodInterval * 4);
                int waitTimeToPlay = (int)(headNodInterval * 4.5) - 500;
                playHipHop(waitTimeToPlay);
                outlet(1,0);
                timer1.schedule(new headNodBehavior(),waitTime);
            }


        }
    }


    class headNodBehavior extends TimerTask {

        public void run(){


            if(rand.nextFloat()>.7f){
                float lookAtPosition = rand.nextFloat()*2 - 1.1f;
                outlet(1,lookAtPosition);
            }

            if(behaviorNodCount<10){
                outlet(3,headNodInterval);
            }else{

                if(rand.nextFloat()>.7f){

                    float randNum = rand.nextFloat();
                    if(randNum < .5f){
                        //outlet(2,headNodInterval);
                        nodType = 2;
                    }else if(randNum >=.5f && randNum<.85f){
                        //outlet(3,headNodInterval);
                        nodType = 3;
                    }else{
                        nodType=3;
                        //outlet(4,headNodInterval);
                    }
                }

                outlet(nodType,headNodInterval);

            }
            behaviorNodCount++;


            timer1.schedule(new headNodBehavior(),(int)headNodInterval);

        }

    }

    class playBackThread extends TimerTask {
        long waitTime;
        public void run() {
            // do stuff here
            if(timerIndex == 0){
                lookCenter();
            }

            if(timerIndex <noteBuffer.size()){
                if(rand.nextFloat()<0.87f){
                    outlet(0,noteBuffer.get(timerIndex));
                }else{
                    outlet(0,noteBuffer.get(timerIndex)+3);
                }
            }

            if(timerIndex <noteBuffer.size()-1){
                waitTime = timeStamps.get(timerIndex+1) - timeStamps.get(timerIndex);
                timer1.schedule(new playBackThread(),waitTime);

            }else{
                listening = true;
                lookRight();
                clearBuffers();
            }
            timerIndex++;
        }
    }

    class set_defaults extends TimerTask{
        public void run(){
            Atom[] output = new Atom[] {Atom.newAtom("instrument"), Atom.newAtom(0)};
            outlet(8, output);
        }
    }


    class playRhythms extends TimerTask{
        public void run(){
            outlet(9, "drum hit");
        }
    }


    public void scheduleRhythms(long waitTime) {
        float[] rhythm = new float[]{};
        switch (currentSection) {
            case 0:
                rhythm = rhythm1;
                break;
            case 1:
                rhythm = rhythm2;
                break;
            case 2:
                rhythm = rhythm3;
                break;
            case 3:
                rhythm = rhythm4;
                break;
            case 4:
                rhythm = rhythm1;
                break;
            default:
                break;
        }
        ArrayList<Long> wait_times = new ArrayList<>();
        long curent_wait_time = extraRhythm;
        for (float aRhythm : rhythm) {
            if (curent_wait_time > waitTime) {
                extraRhythm = curent_wait_time - waitTime;
                break;
            } else {
                wait_times.add(curent_wait_time);
                curent_wait_time = curent_wait_time + (int) (rhythm[rhythmIndex] * headNodInterval);
                rhythmIndex = (rhythmIndex + 1) % rhythm.length;
            }
        }
        for (long wt : wait_times) {
            timer4.schedule(new playRhythms(), wt);
        }
    }
    class playHipHop extends TimerTask{

        long waitTime;
        int rnd_len;
        float note;
        public void run(){
            if (first_note){
                outlet(6, currentSection);
                first_note = false;
            }
            if(learnedMotif == null) {
                switch (currentSection) {
                    case -1:
                        note = countIn[0][hiphopIndex];
                        outlet(0, note);
                        waitTime = (int) (headNodInterval * countIn[1][hiphopIndex]);
                        hiphopIndex++;
                        if (hiphopIndex >= countIn[0].length) {
                            checkSection();
                        }
                        break;
                    case 0:
                        rnd_len = randomizer == 1 ? randomMotif[0].length : 0;
                        note = randomizer == 1 ? randomMotif[0][hiphopIndex % rnd_len] : hipHopMotif1[0][hiphopIndex];
                        outlet(0, note);
                        waitTime = (int) (headNodInterval * hipHopMotif1[1][hiphopIndex]);
                        scheduleRhythms(waitTime);
                        hiphopIndex++;
                        if (hiphopIndex >= hipHopMotif1[0].length) {
                            checkSection();
                        }

                        break;
                    case 1:
                        rnd_len = randomizer == 1 ? randomMotif[0].length : 0;
                        note = randomizer == 1 ? randomMotif[0][hiphopIndex % rnd_len] : hipHopMotif2[0][hiphopIndex];
                        outlet(0, note);
                        waitTime = (int) (headNodInterval * hipHopMotif2[1][hiphopIndex]);
                        scheduleRhythms(waitTime);
                        hiphopIndex++;
                        if (hiphopIndex >= hipHopMotif2[0].length) {
                            checkSection();
                        }
                        break;
                    case 2:
                        rnd_len = randomizer == 1 ? randomMotif[0].length : 0;
                        note = randomizer == 1 ? randomMotif[0][hiphopIndex % rnd_len] : hipHopMotif3[0][hiphopIndex];
                        outlet(0, note);
                        waitTime = (int) (headNodInterval * hipHopMotif3[1][hiphopIndex]);
                        scheduleRhythms(waitTime);
                        hiphopIndex++;
                        if (hiphopIndex >= hipHopMotif3[0].length) {
                            checkSection();
                        }
                        break;
                    case 3:
                        rnd_len = randomizer == 1 ? randomMotif[0].length : 0;
                        note = randomizer == 1 ? randomMotif[0][hiphopIndex % rnd_len] : hipHopMotif4[0][hiphopIndex];
                        outlet(0, note);
                        waitTime = (int) (headNodInterval * hipHopMotif4[1][hiphopIndex]);
                        scheduleRhythms(waitTime);
                        hiphopIndex++;
                        if (hiphopIndex >= hipHopMotif4[0].length) {
                            checkSection();
                        }
                        break;
                    case 4:
                        rnd_len = randomizer == 1 ? randomMotif[0].length : 0;
                        note = randomizer == 1 ? randomMotif[0][hiphopIndex % rnd_len] : hipHopMotif1[0][hiphopIndex];
                        outlet(0, note);
                        waitTime = (int) (headNodInterval * hipHopMotif1[1][hiphopIndex]);
                        scheduleRhythms(waitTime);
                        hiphopIndex++;
                        if (hiphopIndex >= hipHopMotif1[0].length) {
                            checkSection();
                        }
                        break;

                    case 5:
                        endNodCount++;
                        waitTime = (int) headNodInterval;
                        if (endNodCount >= 8) {
                            currentSection = 6;
                        }
                        break;
                    default:
                        break;

                }
            }
            else {
                note = learnedMotif[0][hiphopIndex];
                outlet(0, note);
                waitTime = (int) (headNodInterval * learnedMotif[1][hiphopIndex]);
                scheduleRhythms(waitTime);
                hiphopIndex++;
                if (hiphopIndex >= learnedMotif[0].length) {
                    checkSection();
                }
            }

            if(currentSection<6){
                timer1.schedule(new playHipHop(),waitTime);
            }else{
                cancelPlayBack();
            }
        }



        private void random_instrument(){
            int coin_flip = rand.nextInt(2);
            if(coin_flip == 0) {
                listening_to = rand.nextInt(num_musicians) + 1;
                if (listening_to != num_musicians) {
                    listening_to = rand.nextInt(num_musicians) + 1;
                    Atom[] output = new Atom[]{Atom.newAtom("instrument"), Atom.newAtom(listening_to)};
                    outlet(8, output);
                }
            }
        }

        private void increment_instrument(){
            if(current_repeat >= num_repeats) {
                listening_to = (listening_to % num_musicians) + 1;
                current_repeat = 0;
            }
            current_repeat += 1;
            //System.out.println("increment");
            Atom[] output = new Atom[]{Atom.newAtom("instrument"), Atom.newAtom(listening_to)};
            outlet(8, output);
        }

        private void checkSection(){
            hiphopIndex = 0;
            rhythmIndex = 0;
            extraRhythm = 0;
            //random_instrument();
            increment_instrument();

            learnedMotif = instrument.generateMotif(tempo);
            if (learnedMotif != null) {
                learnedMotif = MotifLearner.addPulse(learnedMotif, 1.0, 62);
            }
            if(lastNote > 0){
                instrument.addNoteInstrument(lastNote);
            }
            if(motif_collection.size() > 0) {
                randomMotif = motif_collection.get(rand.nextInt(motif_collection.size()));
                randomizer = (randomizer + 1) % 2;
            }
            currentSection = hipHopSection;
            first_note = true;
        }
    }
}
