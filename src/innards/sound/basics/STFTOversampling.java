package innards.sound.basics;

import innards.debug.ANSIColorUtils;
import innards.math.jvec.Jvec;
import innards.math.jvec.Jvec.FFTInit;

import java.nio.FloatBuffer;

/**
	handles analysis of overlapped windows with 4 overlaps
	
	in the process of being changed to use floatbuffers / jvec
	*/

public class STFTOversampling {

	Jvec jvec= new Jvec();
	protected FFTInit init;

	// time domain input
	FloatBuffer beforeWindow;
	FloatBuffer nowWindow;

	// these are the four overlap and add windows that we have, post windowing
	FloatBuffer window1;
	FloatBuffer window2;
	FloatBuffer window3;
	FloatBuffer window4;

	// this is our precomputed window function
	FloatBuffer baseWindow;

	// this is where our fft data goes
	FloatBuffer fWindowM1;
	FloatBuffer fWindowM2;
	FloatBuffer fWindowM3;
	FloatBuffer fWindowM4;

	FloatBuffer fWindowP0; // used for super-frame resolution freq
	FloatBuffer fWindowP1;
	FloatBuffer fWindowP2;
	FloatBuffer fWindowP3;
	FloatBuffer fWindowP4;

	FloatBuffer fWindowT0;
	FloatBuffer fWindowT1;

	FloatBuffer zero;

	public int frameSize;
	public float sampleRate;

	public STFTOversampling(int frameSize, float sampleRate) {
		this.frameSize= frameSize;
		this.init= jvec.initializeFFT((int) (Math.log(frameSize) / Math.log(2)));
		this.sampleRate= sampleRate;

		// declare memory and window
		baseWindow= jvec.newFloatBuffer(frameSize);

		window1= jvec.newFloatBuffer(frameSize);
		window2= jvec.newFloatBuffer(frameSize);
		window3= jvec.newFloatBuffer(frameSize);
		window4= jvec.newFloatBuffer(frameSize);

		fWindowP0= jvec.newFloatBuffer(frameSize);
		fWindowP1= jvec.newFloatBuffer(frameSize);
		fWindowP2= jvec.newFloatBuffer(frameSize);
		fWindowP3= jvec.newFloatBuffer(frameSize);
		fWindowP4= jvec.newFloatBuffer(frameSize);
		fWindowM1= jvec.newFloatBuffer(frameSize);
		fWindowM2= jvec.newFloatBuffer(frameSize);
		fWindowM3= jvec.newFloatBuffer(frameSize);
		fWindowM4= jvec.newFloatBuffer(frameSize);

		fWindowT0= jvec.newFloatBuffer(frameSize);
		fWindowT1= jvec.newFloatBuffer(frameSize);

		zero= jvec.newFloatBuffer(frameSize);
		for (int i= 0; i < zero.capacity(); i++)
			zero.put(i, 0);

		for (int i= 0; i < frameSize; i++) {
			baseWindow.put(i, (float) (0.5 - 0.5 * Math.cos(2 * Math.PI * (i) / (float) (frameSize-1))));
		}
	}

	public void provideTimeDomainData(FloatBuffer input) {
		beforeWindow= nowWindow;
		nowWindow= input;

		if (beforeWindow != null) {
			recomputeWindows();
			computeFFTs();
		}
	}

	public boolean isFull() {
		return beforeWindow != null;
	}

	protected void recomputeWindows() {
		// copy
		AFA.copy(nowWindow, window4);

		AFA.copy(nowWindow, 0, window3, frameSize / 4, 3 * frameSize / 4);
		AFA.copy(beforeWindow, 3 * frameSize / 4, window3, 0, frameSize / 4);

		//System.out.println("nowWindow "+jvec.toString(nowWindow)+" beforeWindow"+jvec.toString(beforeWindow)+" window3 : "+jvec.toString(window3));

		AFA.copy(nowWindow, 0, window2, 2 * frameSize / 4, 2 * frameSize / 4);
		AFA.copy(beforeWindow, 2 * frameSize / 4, window2, 0, 2 * frameSize / 4);

		AFA.copy(nowWindow, 0, window1, 3 * frameSize / 4, 1 * frameSize / 4);
		AFA.copy(beforeWindow, 1 * frameSize / 4, window1, 0, 3 * frameSize / 4);
		
		
	}

	public void computeFFTs() {
		AFA.copy(fWindowP4, fWindowP0);

		// window them
		windowMultiplication(baseWindow, window4);
		windowMultiplication(baseWindow, window3);
		windowMultiplication(baseWindow, window2);
		windowMultiplication(baseWindow, window1);
		//System.out.println(ANSIColorUtils.red(" window 1")+" "+jvec.toString(window1));
		//		System.out.println(ANSIColorUtils.red(" window 2")+" "+jvec.toString(window2));
		//		System.out.println(ANSIColorUtils.red(" window 3")+" "+jvec.toString(window3));
		//		System.out.println(ANSIColorUtils.red(" window 4")+" "+jvec.toString(window4));
		/*fftEngine.fft(window4, fWindowM4, fWindowP4, true);
		fftEngine.fft(window3, fWindowM3, fWindowP3, true); // do we need to do the phases on all of them if we don't look at them ?, do we even need the FFT's or the windows !
		fftEngine.fft(window2, fWindowM2, fWindowP2, true);
		fftEngine.fft(window1, fWindowM1, fWindowP1, true);*/

		jvec.performComplexFFTOutOfPlace(init, window4, zero, fWindowT0, fWindowT1, true);
		jvec.complexToMagnitudePhase(fWindowT0, fWindowT1, fWindowM4, fWindowP4);
		jvec.performComplexFFTOutOfPlace(init, window3, zero, fWindowT0, fWindowT1, true);
		jvec.complexToMagnitudePhase(fWindowT0, fWindowT1, fWindowM3, fWindowP3);
		jvec.performComplexFFTOutOfPlace(init, window2, zero, fWindowT0, fWindowT1, true);
		jvec.complexToMagnitudePhase(fWindowT0, fWindowT1, fWindowM2, fWindowP2);
		jvec.performComplexFFTOutOfPlace(init, window1, zero, fWindowT0, fWindowT1, true);
		jvec.complexToMagnitudePhase(fWindowT0, fWindowT1, fWindowM1, fWindowP1);

		//System.out.println("mag :" + jvec.toString(fWindowM1) + " phase:" + jvec.toString());
	}

	// on window 4
	public float getRawTrueFrequency(int bin) {
		return getRawTrueFrequency(fWindowP4, fWindowP3, bin);
	}
	public float getRawTrueFrequency(FloatBuffer phaseWin1, FloatBuffer phaseWin2, int bin) {
		// base frequency
		float base= bin * sampleRate / frameSize;

		// delta phase correction
		float dfc= getRawDFC(phaseWin1, phaseWin2, bin);

		return base + dfc;
	}

	public float getRawRoughFrequency(int bin) {
		// base frequency
		float base= bin * sampleRate / frameSize;

		// delta phase correction
		//float dfc = getRawDFC(bin);

		return base; //+dfc;
	}

	public float getInterpolatedTrueFrequency(int bin) {
		double d= 0;
		double t= 0;
		double e= 0;
		t += e= fWindowM4.get(bin);
		d += getRawTrueFrequency(fWindowP4, fWindowP3, bin) * e;
		t += e= fWindowM3.get(bin);
		d += getRawTrueFrequency(fWindowP3, fWindowP2, bin) * e;
		t += e= fWindowM2.get(bin);
		d += getRawTrueFrequency(fWindowP2, fWindowP1, bin) * e;
		t += e= fWindowM1.get(bin);
		d += getRawTrueFrequency(fWindowP1, fWindowP0, bin) * e;
		return (float) ((float) d / (t + 1e-16));
	}

	public float getInterpolatedMagnitude(int bin) {
		double d= 0;
		d += fWindowM4.get(bin);
		d += fWindowM3.get(bin);
		d += fWindowM2.get(bin);
		d += fWindowM1.get(bin);

		return (float) d / 4;
	}

	public float getRawDFC(int bin) {
		return getRawDFC(fWindowP4, fWindowP3, bin);
	}

	
	public float getRawDFC(FloatBuffer phaseWin1, FloatBuffer phaseWin2, int bin) {
		float p1= phaseWin1.get(bin);
		float p2= phaseWin2.get(bin);

		// how much of a phase difference would we actually expect ? 
		float expectedPhaseDifference= (float) ((bin % 4) * 2*Math.PI / 4);

		// p1 and p2 are -Math.PI to Math.PI, win1 happens after win2 so we'd expect win1-win2 to be equal to expectedPhaseDifference
		float diff=
			(float) Math.atan2(
				Math.sin(expectedPhaseDifference - p2 + p1),
				Math.cos(expectedPhaseDifference - p2 + p1));

		float adiff=
			(float) Math.atan2(
				Math.sin(  p2 - p1),
				Math.cos( p2 - p1));
		
		//System.out.println(bin+" "+fWindowM4.get(bin)+" ("+p1+","+p2+","+expectedPhaseDifference+") " +diff+" "+adiff);
		return -(float) ((diff*sampleRate/frameSize)/(Math.PI/2));
	}

	public float getRawDFC1(FloatBuffer phaseWin1, FloatBuffer phaseWin2, int bin) {
		float p1= phaseWin1.get(bin);
		float p2= phaseWin2.get(bin);

		// how much of a phase difference would we actually expect ? 
		float expectedPhaseDifference= (float) ((bin ) * Math.PI / 4 * 2);

		// p1 and p2 are -Math.PI to Math.PI, win1 happens after win2 so we'd expect win1-win2 to be equal to expectedPhaseDifference
		float diff=
			(float) Math.atan2(
				Math.sin(expectedPhaseDifference - p1 + p2),
				Math.cos(expectedPhaseDifference - p1 + p2));

		System.out.println(
			fWindowM4.get(bin)
				+ " "
				+ bin
				+ " "
				+ (bin * sampleRate / (float) frameSize)
				+ " "
				+ diff
				+ " "
				+ p1
				+ " "
				+ p2
				+ " "
				+ expectedPhaseDifference);

		// this phase offset means that we've misjudged the frequency
		float fTrue= -diff / (frameSize / (float) sampleRate);
		return fTrue / 4;
	}

	public float getRawDFC0(FloatBuffer phaseWin1, FloatBuffer phaseWin2, int bin) {

		float p1= phaseWin1.get(bin);
		float p2= phaseWin2.get(bin);
		float delta= (float) ((p2 - p1 + 4 * Math.PI) % (2 * Math.PI));

		//		delta -= ( (bin%4) * 2 * Math.PI * 0.25); // 0.25 is 4 overlap and add

		float exp= (float) ((bin % 4) * 2 * Math.PI / 4);
		//System.out.println("phase 1 is <" + p1 + "> phase 2 is <" + p2 + "> delta <"+delta+"> exp <"+exp+">");
		delta -= exp;

		if (delta > Math.PI)
			delta -= Math.PI * 2;

		//		float delta= p1 - p2;

		/*
				// expected phase difference is
				delta -= bin * 2 * Math.PI * 0.25; // 0.25 is 4 overlap and add
		
				// remap to -Pi, PI
				int q= (int) (delta / Math.PI);
				if (q >= 0)
					q += q & 1;
				else
					q -= q & 1;
				delta -= Math.PI * q;
		*/

		//delta/=bin!=0 ? bin : 1;
		// deviation is 
		float dev= (sampleRate / frameSize) * (float) (4 * delta / (2 * Math.PI)); // 4 is overlap

		/*if (delta<-Math.PI) delta = (float)(Math.PI + (delta + Math.PI));
		else if (delta>Math.PI) delta = (float)(-Math.PI + (delta - Math.PI));
		*/
		return dev;
	}

	public float getRawMagnitude(int bin) {
		return fWindowM4.get(bin);
	}

	public void windowMultiplication(FloatBuffer windowFunction, FloatBuffer dest) {
		jvec.vmul(windowFunction, dest, dest);
	}

}
