package innards.sound.basics;


public interface ProvidesFFT 
{

	public void initFFT(int log2n);
	public void fft(float in[], float outMag[], float outPhase[], boolean doPhase);	
	//public void fft(FloatBuffer in , FloatBuffer outMag, FloatBuffer outPhase, boolean doPhase)
}
