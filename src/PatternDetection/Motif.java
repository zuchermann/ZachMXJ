package PatternDetection;

import java.util.HashMap;

/**
 * Created by Hanoi on 12/5/16.
 */



public class Motif {
    private String name;
    private HashMap<String, Double> properties;

    public Motif (String[] propertyNames, String[] propertyValues){
        if(propertyValues.length != propertyNames.length){
            throw new MotifError("problem with creating motif " + propertyValues[0] + ". Check csv file!");
        }

        //first property is always name
        this.name = propertyValues[0];
        this.properties = new HashMap<String, Double>();

        for(int i = 1; i < propertyNames.length; i++){
            String property = propertyNames[i];
            this.properties.put(property.trim().toLowerCase(), toDouble(propertyValues[i]));
        }
    }

    public double getProperty(String property){
        return properties.get(property.trim().toLowerCase());
    }

    public String getName() {
        return this.name;
    }

    private double toDouble(String val){
        double result;
        if(val.equals("NO") || val.equals("OFF")){
            result = 0.0;
        } else if (val.equals("YES")) {
            result = 1.0;
        } else {
            result = Double.parseDouble(val);
        }
        return result;
    }

    public String toString(){
        return "Motif: " + this.name + ". properties = " + this.properties;
    }
}

