package innards.signal;

public class TimeBasedChangeLimiter {
    float velocityLimit; 
    float accelerationLimit;
	boolean initialized;
	
	/**
	 * Time unit is a second.
	 * 
	 * @param velocityLimit
	 * @param accelerationLimit
	 */
	public TimeBasedChangeLimiter(float velocityLimit, float accelerationLimit){
        this.velocityLimit = velocityLimit;
        this.accelerationLimit = accelerationLimit;
        this.lastVelocity = 0; 
		this.initialized = false;
	}

	/**
	 * Time unit is a second.
	 * 
	 * @param velocityLimit
	 * @param accelerationLimit
	 * @param initialValue
	 */
	public TimeBasedChangeLimiter(float velocityLimit, float accelerationLimit, float initialValue) {
        this.velocityLimit = velocityLimit;
        this.accelerationLimit = accelerationLimit;
        this.lastValue = initialValue;
        this.lastVelocity = 0;
		this.initialized = true;
    }
	
	public float timePassed() {
		currentTime = System.currentTimeMillis();
		return (currentTime - lastTime)/1000f;
	}

    float lastValue;
    float lastVelocity;
    
    long lastTime;
    long currentTime;

    public float filter (float z)  {
		if (!initialized) {
			initialized = true;
			lastValue = z;
			lastTime = System.currentTimeMillis();
			return z;
		}
		
		float dt = timePassed();
		
        float currentVelocity = (z-lastValue)/dt;

        if (currentVelocity > velocityLimit)  {
            z = lastValue + dt*velocityLimit;
            currentVelocity = velocityLimit;
        } else if (currentVelocity < -velocityLimit) {
            z = lastValue - dt*velocityLimit;
            currentVelocity = -velocityLimit;            
        }

        double currentAcceleration = (currentVelocity-lastVelocity)/dt;

        if (currentAcceleration > accelerationLimit)  {
            z = dt*dt*accelerationLimit + dt*lastVelocity + lastValue;
        } else if (currentAcceleration <- accelerationLimit) {
            z = -dt*dt*accelerationLimit + dt*lastVelocity + lastValue;
        }

        lastVelocity = (z-lastValue)/dt;
        lastValue = z;
		lastTime = currentTime;

        return z;
    }

	public void reset(float value, float velocity){
		this.lastValue = value;
		this.lastVelocity = velocity;
		this.initialized = true;
	}
	
	public void setVelocityLimit(float f){
		this.velocityLimit = f;
	}

	public void setAccelerationLimit(float accelerationLimit){
		this.accelerationLimit = accelerationLimit;
	}
	
    static public void main(String[] s)  {
        TimeBasedChangeLimiter cl = new TimeBasedChangeLimiter(10f, 0.005f, 0);

        long startTime = System.currentTimeMillis();
        
        for (int i = 0;i < 1000; i++) {
            float z = (float)(-(i/10));

            if (i > 100) z = 10;
            float o = cl.filter(z);

            System.out.println("target:"+z+" result:"+o);
        }
        
        long finishTime = System.currentTimeMillis();
        System.out.println("Finished in " + (finishTime-startTime)/1000f + " seconds");
    }
}
