package innards.sound.basics;

/**
 * @author marc
 * Created on May 10, 2003
 */
public interface iClockSource
{

	// returns the sample number that, should we call 'write' (or read) that the start of this block will make it to the speaker
	public long getSampleNumberAtAccess();

	// returns the sample number that, we just wrote to the speaker (which I believe is latency corrected) 
	//should we call 'write' (or read) that the start of this block will make it to the speaker
	// (getSampleNumberAtAccess - getSampleNumberAtAccess) / sample rate gives you the write latency
	public long getSampleNumberAtNow();

}
