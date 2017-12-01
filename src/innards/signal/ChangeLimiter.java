package innards.signal;


/**
velocity and acceleration limiting
 //imported from c43.  does not 
*/
public class ChangeLimiter
{
    float velocityLimit; 
    float accelerationLimit;
	boolean initialized;
	
	public ChangeLimiter(float velocityLimit, float accelerationLimit){
        this.velocityLimit = velocityLimit;
        this.accelerationLimit = accelerationLimit;
        this.lastVelocity = 0; 
		this.initialized = false;
	}

	public ChangeLimiter(float velocityLimit, float accelerationLimit, float initialValue)
    {
        this.velocityLimit = velocityLimit;
        this.accelerationLimit = accelerationLimit;
        this.lastValue = initialValue;
        this.lastVelocity = 0;
		this.initialized = true;
    }

    float lastValue;
    float lastVelocity;

    public float filter(float z)
    {
		if(!initialized){
			initialized = true;
			lastValue = z;
		}
        float currentVelocity = z-lastValue;

        if (currentVelocity>velocityLimit)
        {
            z = lastValue+velocityLimit;
            currentVelocity = velocityLimit;
        }
        else if (currentVelocity<-velocityLimit)
        {
            z = lastValue-velocityLimit;
            currentVelocity = -velocityLimit;            
        }

        double currentAcceleration = currentVelocity-lastVelocity;

        if (currentAcceleration>accelerationLimit)
        {
            z = accelerationLimit + lastVelocity + lastValue;
        }
        else if (currentAcceleration<-accelerationLimit)
        {
            z = -accelerationLimit + lastVelocity + lastValue;
        }

        lastVelocity = z-lastValue;
        lastValue = z;

        return z;
    }

	public void reset(float value, float velocity){
		this.lastValue = value;
		this.lastVelocity = velocity;
		this.initialized = true;
	}
	
    static public void main(String[] s)
    {
        ChangeLimiter cl = new ChangeLimiter(0.1f, 0.005f, 0);

        for(int i=0;i<400;i++)
        {
            float z = (float)(-(i/10));

            if (i>100) z = 10;
            float o = cl.filter(z);

            System.out.println("target:"+z+" result:"+o);
        }
    }
}