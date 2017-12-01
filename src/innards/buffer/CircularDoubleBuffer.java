package innards.buffer;
import java.io.Serializable;

/**    
    warning, this class is delta - time ignorant - that is why it
    does not implement DoubleFilter or DoubleProvider
    @author marc
    */
    
    /** 
    Added getRange and changed internalGet and Set so as not to use
    mod on 05/02/00 AYB.
    */
    
public class CircularDoubleBuffer implements Serializable {
    double[] dataArray = new double[1];
    int length = 1;
    int index = 0;
    int numValid = 0;
    
    public CircularDoubleBuffer(int size)
    {
        dataArray = new double[size];
        length = size;
    }
    
    /** Initialize by copying the CircularDoubleBuffer passed in */
    public CircularDoubleBuffer(CircularDoubleBuffer cdb) {
        synchronized(cdb) {
    	    length = cdb.length;
    	    numValid = cdb.numValid;
    	    index = cdb.index;
    	    dataArray = new double[length];
    	    for (int c=0; c < length; c++) {
    		    dataArray[c] = cdb.dataArray[c];
    	    }
    	}
    }
    
    public CircularDoubleBuffer copy() {
        return new CircularDoubleBuffer(this);
    }
    
    public void setSize(int size)
    {
        double[] newDataArray = new double[size];
        
        int minLength = (length>size) ? size : length;
        for(int i=0;i<minLength;i++)
        {
            newDataArray[i] = internalGet(i-length + 1);
        }
        dataArray = newDataArray;
        index = minLength-1;
        length = size;
    }
    
    private double internalGet(int offset)
    {
      int ind=index+offset;
      
      // Do thin modulo (should just drop through)
      for(;ind>=length;ind-=length);
      for(;ind<0;ind+=length);
      // Set value
      return dataArray[ind];
    }
    
    private void internalSet(int offset, double value)
    {
      int ind=index+offset;
      
      // Do thin modulo (should just drop through)
      for(;ind>length;ind-=length);
      for(;ind<0;ind+=length);
      // Set value
      dataArray[ind] = value;
    }

    public void reset()
    {
        index = 0;
        numValid = 0;
    }
    
    /**
        returns the value of what comes off the stack 
        */
    public double push(double newValue)
    {
        numValid++; if (numValid>length) numValid = length;
        double ret = 0.0;
        if (numValid == length) ret = dataArray[index];
        dataArray[index] = newValue;
        index++;
        index %= length;
        return ret;
    }
    
    public int getLength() {
    	return numValid;
    }
    
    /** returns what would fall off the stack on a push
    */
    public double peek()
    {
        return internalGet(length);
    }
     
    /**
        e.g. getOffset(0) gets the current value
        */
    public double getOffset(int positiveSizeBack)
    {
        return internalGet(-1-positiveSizeBack);
    }

    /**
        e.g. getOffset(0) gets the current value
        */
    public void setOffset(int positiveSizeBack, double value)
    {
        internalSet(-1-positiveSizeBack, value);
    }
    
    
    /**
        returns how far back it is wise to look 
        */
    public int backSize()
    {
        return numValid;
    }
    
    /** 
        returns the actual size fo the buffer
        */
    public int getSize()
    {
        return length;   
    }
    
    /**
        Returns the desired range (in terms of Offsets) in a double array in chronological order
        */
    public double[] getRange(int str, int stp)
    {
       double out[] = new double[str-stp+1];
       
       for(int i=str,j=0;i>=stp;i--,j++)
       {
          out[j] = this.getOffset(i);
       }
       
       return out;
    }
    
    public String toString()
    {
        String ret = "";
        for(int i=0;i<dataArray.length;i++)
        {
            ret+= dataArray[i]+" ";
        }
        ret += "\n index = "+index+" backSize = "+backSize();
        return ret;
    }
    
    
    static public void main(String[] contradiction)
    {
        CircularDoubleBuffer testMe = new CircularDoubleBuffer(10);
        
        System.out.println("initial = "+testMe);
        
        for(int i=0;i<25;i++)
        {
            double ret = testMe.push(i+1);
        
            System.out.println(" pushed ("+(i+1)+"), ret was = "+ret+", five back was = "+testMe.getOffset(5));
            System.out.println(" now = "+testMe);            
        } 
        
        double range[] = testMe.getRange(7,3);
        System.out.print("The 7th oldest through 3rd oldest data points are");
        for(int i=0;i<5;i++)
        {
          System.out.print(" " + range[i]); 
        }
        System.out.println(".");
    }    
}