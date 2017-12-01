package innards.signal;

import innards.math.BaseMath;

/**
 * TODO: there seems to be a bug here tracking really smoothly changing signals.  maybe not anymore?
 * This is an acceleration/velocity filter but that goes where you tell it without overshooting, unless you make it by changing the target back underneath it.
 *
 * User: jg
 * Date: Mar 10, 2004
 * Time: 5:23:08 PM
 */
public class DampedChangeLimiter{
	float velocityLimit;
	float accelerationLimit;
	float overshootCorrectionAcceleration;
	//float[] debugInput = new float[200], debugOutput = new float[200];
	//int debugIndex;

	public DampedChangeLimiter(float velocityLimit, float accelerationLimit){
		this.velocityLimit = velocityLimit;
		this.accelerationLimit = accelerationLimit;
		this.lastVelocity = 0;
		this.initialized = false;
		this.overshootCorrectionAcceleration = accelerationLimit;
	}

	public DampedChangeLimiter(float velocityLimit, float accelerationLimit, float overShootCorrectionA){
		this.velocityLimit = velocityLimit;
		this.accelerationLimit = accelerationLimit;
		this.lastVelocity = 0;
		this.initialized = false;
		this.overshootCorrectionAcceleration = overShootCorrectionA;
	}

	public DampedChangeLimiter(float velocityLimit, float accelerationLimit, float overShootCorrectionA, float initialValue)
	{
		this.velocityLimit = velocityLimit;
		this.accelerationLimit = accelerationLimit;
		this.lastValue = initialValue;
		this.lastVelocity = 0;
		this.initialized = true;
		this.overshootCorrectionAcceleration = overShootCorrectionA;
	}

	float lastValue;
	float lastVelocity;
	boolean initialized;

	public void setVelocityLimit(float f){
		this.velocityLimit = f;
	}

	public void setAccelerationLimit(float accelerationLimit){
		this.accelerationLimit = accelerationLimit;
	}

	public void setOvershootCorrectionAcceleration(float overshootCorrectionAcceleration){
		this.overshootCorrectionAcceleration = overshootCorrectionAcceleration;
	}

	/**
	 * gives the location at which the filter can stop soonest given the current velocity
	 * @return 
	 */
	public float whereCanYouStop(){
		float location = lastValue;
		float velocity = lastVelocity;
		while(Math.abs(velocity) > accelerationLimit){
			if(velocity > 0){
				velocity -= accelerationLimit;
			}else{
				velocity += accelerationLimit;
			}
			location += velocity;
		}
		return location;
	}
	
	//StripChart debugChart = new StripChart(200, -1, 1);
	//StripChart debugChart2 = new StripChart(200, -1, 1);
	public float filter(float z)
	{
		//debugInput[debugIndex] = z;
		//debugChart.set(z);
		if(!initialized){
			initialized = true;
			lastValue = z;
		}
		//float currentVelocity = z-lastValue;

		//accelorate towards target as much as you can;
		//clamp velocity;
		//calc accel needed to stop at target using proposed info (ie, next tick)
		//if its > maxAccel, then we need to start out decelerating now at rate for this tick.

		//we're there, just stop!
		if((Math.abs(z - lastValue) <= BaseMath.epsilon)&& //we are near the goal
		   (Math.abs(lastVelocity) <= accelerationLimit)){ //and we can stop without violating maxAccel
			//we can just stay here.
			lastVelocity = 0;
			lastValue = z;
			//debugChart2.set(z);
			//debugOutput[debugIndex++] = z;
			//debugPrint();
			return z;
		}

		//we're one tick away, just go there
		if((Math.abs(z - lastValue) < accelerationLimit) && //if we could stop after going directly there.
		   (Math.abs(Math.abs(lastVelocity) - Math.abs(z-lastValue)) < accelerationLimit)){ //and we can go directly there without violating maxAccel
			lastVelocity = z - lastValue;
			lastValue = z;
			//debugOutput[debugIndex++] = z;
			//debugPrint();
			return z;
		}


		//accelerate towards target, must go in correct direction.
		float newVelocity;
		if(lastValue < z){
			if(lastVelocity < 0){
				//we're heading the wrong way.
				newVelocity = lastVelocity + overshootCorrectionAcceleration;
			}else{
				newVelocity = lastVelocity + accelerationLimit;
			}
		}else{
			if(lastVelocity > 0){
				//we're heading the wrong way.
				newVelocity = lastVelocity - overshootCorrectionAcceleration;
			}else{
				newVelocity = lastVelocity - accelerationLimit;
			}
		}

		//clamp velocity to max.
		if(newVelocity > velocityLimit){
			newVelocity = velocityLimit;
		}else if(newVelocity < -velocityLimit){
			newVelocity = -velocityLimit;
		}


		//either we're heading towards z and maybe should slow,
		//or we're past it and going the wrong way (do nothing here for this, above will cover it)
		//or we're past it and just turning around to head back,
		//or we're on it but going too fast to stop

		//only do this if we're heading towards z!
		if((lastVelocity >= 0 && newVelocity > BaseMath.epsilon && lastValue < z - BaseMath.epsilon) ||
		   (lastVelocity <= 0 && newVelocity < -BaseMath.epsilon && lastValue > z + BaseMath.epsilon)){
			//find deccel required to stop at z from next tick (ie, if we proceed with these values)
			float newValue = lastValue + newVelocity;
			float newDisplacement = z - newValue;
			float accelRequiredNextTime = -(newVelocity*newVelocity)/(2*newDisplacement + newVelocity);

			if(Math.abs(accelRequiredNextTime)>accelerationLimit){
				//need to decel now.
				float curDisplacement = z - lastValue;
				float accelRequiredNow = -(lastVelocity*lastVelocity)/(2*curDisplacement + lastVelocity);

				if(accelRequiredNow > accelerationLimit){
					accelRequiredNow = accelerationLimit;
				}else if(accelRequiredNow < -accelerationLimit){
					accelRequiredNow = -accelerationLimit;
				}

				newVelocity = lastVelocity + accelRequiredNow;
			}
		}else if((lastVelocity > 0 && newVelocity < 0)||
		         (lastVelocity < 0 && newVelocity > 0)){
			//we're heading away!  make sure we don't overshoot by overly accelorating towards goal at this stage.

			//System.out.println("heading away and coming back, trying to avoid overshoot comint back");
			if(Math.abs(newVelocity) > Math.abs(z-lastValue)){
				newVelocity = z-lastValue;
			}
		}else if(Math.abs(z - lastValue) <= BaseMath.epsilon){
			if(lastVelocity < 0){
				newVelocity += accelerationLimit;
			}else{
				newVelocity -= accelerationLimit;
			}
		}


		//clamp deceleration
		//System.out.println("Vel(last)"+lastVelocity+" (new)"+newVelocity);

		float newValue = lastValue+newVelocity;
		lastVelocity = newValue-lastValue;
		lastValue = newValue;
		//debugChart2.set(newValue);

		//debugOutput[debugIndex++] = newValue;
		//debugPrint();
		return newValue;
	}

	/**
	 * if you are followign something you may want to filter later, keep calling this, so the filter will be
	 * prepped to filter without messing up the velocity.  unless its greater than max that is, then it will be clamped to max
	 * when you start filtering.
	 * @param z
	 */
	public void follow(float z){
		if(!initialized){
			initialized = true;
			lastValue = z;
		}
		lastVelocity = z-lastValue;
		lastValue = z;
	}
	/*
	protected void debugPrint(){
		if(debugIndex == debugInput.length){
			System.out.println("The Data!\nINPUT: ");
			for(int i = 0; i < debugInput.length; i++){
				System.out.print(debugInput[i]+", ");
			}
			System.out.println("\n\nOUTPUT:");
			for(int i = 0; i < debugInput.length; i++){
				System.out.print(debugOutput[i]+", ");
			}
			System.out.println("\n\n");
			System.exit(0);
		}
	}
	*/

	//public void reset(float value, float velocity){
	//	this.lastValue = value;
	//	this.lastVelocity = velocity;
	//	this.initialized = true;
	//}

}
