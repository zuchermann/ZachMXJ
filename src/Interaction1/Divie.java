package Interaction1;

import com.cycling74.max.*;

/**
 * Created by yn on 12/1/16.
 */
public class Divie extends MaxObject{
    int counter;
    int outs;

    public Divie (){
        this(2);
    }

    public Divie (int outs){
        this.outs = outs;
        this.counter = 0;

        createInfoOutlet(false);

        declareInlets(new int[]{ DataTypes.ALL });

        int[] outletList = new int[outs];
        for(int i = 0; i < outletList.length; i++){
            outletList[i] = DataTypes.ALL;
        }

        declareOutlets(outletList);

        setInletAssist(new String[] {
                "float input to be stored if novel",
        });

    }

    public void inlet(float value) {
        outlet(counter, value);
        counter = (counter + 1) % outs;
    }
}
