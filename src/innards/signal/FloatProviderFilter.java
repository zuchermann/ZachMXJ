package innards.signal;

import innards.provider.iFloatProvider;
import innards.*;

/**
 * Low Pass Filter acts as Float Provider - buffers signal from another floatProvider.
 * TODO - have people pass in actual filtering system, so it can be other than simple low pass. (also do that to SmoothSetener, which is kinda the opposite of this class (push in stead of pull))
 */
public class FloatProviderFilter implements iFloatProvider, iUpdateable{
	iFloatProvider source;
	float filterAmount;
	boolean iGetUpdated = false;
	
	//1 means never move from initial.
	//0 means no filtering, value just goes staight through.
	public FloatProviderFilter(iFloatProvider source, float filterAmount){
		if(filterAmount < 0 || filterAmount > 1.0f){
			throw new IllegalArgumentException("filterAmount must be between 0 and 1; got <"+filterAmount+">");
		}
		this.source = source;
		this.filterAmount = filterAmount;
		this.last = source.evaluate();
	}

	protected float last;
	public float evaluate(){
		if(iGetUpdated){
			return last;
		}else{
			float lastRequested = source.evaluate();
			float r = lastRequested*(1-filterAmount) + last * filterAmount;
			last = r;
			return r;
		}
	}

	//if i get updated, then only update my value every update.
	//otherwise, update every evaluate.
	public void update(){
		iGetUpdated = true;
		float lastRequested = source.evaluate();
		float r = lastRequested*(1-filterAmount) + last*filterAmount;
		last = r;
	}
}
