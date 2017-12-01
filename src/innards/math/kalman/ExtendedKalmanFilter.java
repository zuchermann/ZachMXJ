package innards.math.kalman;			


import innards.math.linalg.*;

/**
    @author Chris Kline (ckline@media.mit.edu) wrote the original, linear version of this Kalman
            Filter class, which this class is based on. The original version is still in the old
            CVS code base.
    @author AYB 03/30/00 
*/
public class ExtendedKalmanFilter
{
  // debugging stuff.
  private static final boolean debug_on = false;
  private static final java.io.PrintStream out = System.err;

  /** n = N, m = M, l = L, where N is the state vector length,
      M is the observation vector length and L is the control 
      vector length */
  private int n = 0, m = 0, l = 0;
  
  /** The necessary matrices and functions are contained in here */
  private EKFuncs ekf = null;

  /** Storage for constant matrices */
  private Matrix A = null;
  private Matrix AT = null;
  private Matrix B = null;
  private Matrix H = null;
  private Matrix HT = null;
  private Matrix K = null;
  private Matrix Q = null;
  private Matrix R = null;

  /** temporary storage */
  private Matrix tempNxM = null;
  private Matrix tempMxM = null;
  private Matrix tempNxN = null;
  private Matrix tempNxN_2 = null;
  private Matrix IdentityNxN = null;
  private Vec tempMx1 = null;
  private Vec tempNx1 = null;

  /**
     Create a new Extended Kalman filter and initialize it.

     @param ekf An instance of the EKFuncs interface which contains all the necessary information
              about the structure of this filter.
  */
  public ExtendedKalmanFilter(EKFuncs ekf)
  { 
    // Store filter definition
    this.ekf = ekf;
    
    // Store dimensions
    n = ekf.getStateSize();
    m = ekf.getObservationSize();
    l = ekf.getControlSize();
    
    // Allocate space for matrices
    A = new Matrix(n,n);
    AT = new Matrix(n,n);
    if (l!=0) {B = new Matrix(n,l);}
    H = new Matrix(m,n);
    HT = new Matrix(n,m);
    K = new Matrix(n,m);
    Q = new Matrix(n,n);
    R = new Matrix(m,m);
    
    // Allocate space for temps
    tempNxM = new Matrix(n, m);
    tempMxM = new Matrix(m, m);
    tempNxN = new Matrix(n, n);
    tempNxN_2 = new Matrix(n, n);
    tempMx1 = new Vec(m);
    tempNx1 = new Vec(n);
    IdentityNxN = new Matrix(n, n);
    IdentityNxN.identity();
    
    // Store constant matrices
    if (ekf.isAconst() == true)
    {
        ekf.getA(null,null,A);
        A.makeTranspose(AT);
    }
    if (ekf.isBconst() == true) {ekf.getB(null,null,B);}
    if (ekf.isHconst() == true)
    {
        ekf.getH(null,H);
        H.makeTranspose(HT);
    }
    if (ekf.isQconst() == true) {ekf.getQ(null,null,Q);}
    if (ekf.isRconst() == true) {ekf.getR(null,R);}
  }
  
  /** Get the length of the observation vector.

      @return the length of the observation vector
  */
  public int getObservationSize()
  {
    return m;
  }
  
  /** Get the length of the state vector.

      @return the length of the state vector
  */
  public int getStateSize()
  {
    return n;
  }
  
  /** Get the length of the control vector.

      @return the length of the control vector
  */
  public int getControlSize()
  {
    return l;
  }

  /**
     Given the state and covariance and an observation, generates a predictions
     of what the next state will be and updates the state and covariance. This 
     is the method you probably want to be calling most of the time.
     
     @param state a subclass of the EKFVec class with the current state
     @param P the current covariance matrix for the state
     @param obs a subclass of the EKFVec class with the newest observation
     
     @exception DimensionMismatchException if the new observation is not of the
                                        correct size (as specified in the
			         	constructor)
					
     @exception SingularMatrixException if, during the computation of the new
                                     Kalman gain, an attempt is made to invert
				     a singular matrix. 
  */
  public void update(EKFVec state, Matrix P, EKFVec obs)
    throws DimensionMismatchException, SingularMatrixException, NonSquareMatrixException
  {
    update(state, P, obs, null);
  }

  /**
     As above, except take a control input vector as well.

     @param control if non-null, a subclass of the EKFVec class with the newest control input

     @exception IllegalStateException if this version of the method is called but
                                        there is no control matrix (l=0).

     @exception DimensionMismatchException if either the new observation or the
                                        control input is not of the correct
					size (as specified in the constructor)
					
     @exception SingularMatrixException if, during the computation of the new
                                     Kalman gain, an attempt is made to invert
				     a singular matrix. 
  */
  
  public void update(EKFVec state, Matrix P, EKFVec obs, EKFVec control)
    throws IllegalStateException, DimensionMismatchException,
           SingularMatrixException, NonSquareMatrixException
  {
    // time update equations ("predict").
    predict(state, P, control);

    // measurement update equations ("correct")
    correct(state, P, obs);
  }

  /** Time update ("predict") part of the kalman filter update. This method
      assumes no control input.

      @param state The filter state (subclass of EKFVec)
      @param P Current state covariance matrix
  */
  public void predict(EKFVec state, Matrix P)
    throws SingularMatrixException, NonSquareMatrixException
  {
    predict(state,P,null);
  }
  
  /** Time update ("predict") part of the kalman filter update. 
      As above, except:

      @param control 
  */
  public void predict(EKFVec state, Matrix P, EKFVec control)
    throws SingularMatrixException, NonSquareMatrixException
  {
    Vec x = state.getVec();
    Vec newControlInput = null;
    if(control!=null) {newControlInput = control.getVec();}
    
    if(!ekf.isAconst()) {ekf.getA(x,newControlInput,A);A.makeTranspose(AT);}
    if(!ekf.isBconst()) {ekf.getB(x,newControlInput,B);}
    if(!ekf.isQconst()) {ekf.getQ(x,newControlInput,Q);}
    
    if(ekf.isPredictLinear())
    {
        // compute new estimate of the state, x = A*x
        Matrix.mult(A, x, tempNx1);
        if (debug_on) { out.println("tempNx1:\n" + tempNx1); }
        x.copyFrom(tempNx1);
        if (debug_on) { out.println("x:\n" + x); }
        
        // if applicable, take any control input (u) into account
        if (newControlInput != null)
        {
        if (B == null)
        {
	    throw new IllegalStateException("A control input was given but a mapping from the control input to the state has not yet been specified with setObservationMapping()");
        }
        else if (newControlInput.size() != l)
        {
	    throw new DimensionMismatchException("The newControlInput is of size " +
					        newControlInput.size() +
					        " but is supposed to be of size " + l);
        }

        // take into account the control input (x = A*x + B*u instead of x = A*x)
        Matrix.mult(B, newControlInput, tempNx1);
        if (debug_on) { out.println("tempNx1:\n" + tempNx1); }
        Vec.add(x, tempNx1, x);
        if (debug_on) { out.println("x:\n" + x); }
        }

        if (debug_on) { out.println("xp:\n" + x); }
    }
    else
    {
        // compute new estimate of the state, x = f(x,u)
        ekf.predict(x,newControlInput,tempNx1); // This need not be in-place
        x.copyFrom(tempNx1);
        if (debug_on) { out.println("x:\n" + x); }
    }
    
    // Always update the covariance
    // computer new estimate error covariance, P = A*P*AT + Q
    Matrix.mult(A, P, tempNxN);
    if (debug_on) { out.println("tempNxN:\n" + tempNxN); }
    Matrix.mult(tempNxN, AT, tempNxN_2);
    if (debug_on) { out.println("tempNxN_2:\n" + tempNxN_2); }
    Matrix.add(tempNxN_2, Q, P);
    if (debug_on) { out.println("P:\n" + P); }
  }

  /** Measurement update ("correct") part of the kalman filter update. 

      @param state The filter state (subclass of EKFStateVec)
      @param P Current state covariance matrix
      @param newObservation the new observation input vector 
  */
  public void correct(EKFVec state, Matrix P, EKFVec obs)
    throws SingularMatrixException, NonSquareMatrixException
  {
    Vec x = state.getVec();
    Vec newObservation = obs.getVec();
    if(!ekf.isHconst()) {ekf.getH(x,H);H.makeTranspose(HT);}
    if(!ekf.isRconst()) {ekf.getR(x,R);}
    
    if (newObservation.size() != m)
    {
      throw new DimensionMismatchException("The newObservation is of size " +
					   newObservation.size() +
					   " but is supposed to be of size " + m);
    }

    // first compute the new Kalman gain, K = P*HT*(H*P*HT + R)^-1
    Matrix.mult(P, HT, tempNxM);
    if (debug_on) { out.println("tempNxM:\n" + tempNxM); }
    Matrix.mult(H, tempNxM, tempMxM);
    if (debug_on) {  out.println("tempMxM:\n" + tempMxM); }
    Matrix.add(tempMxM, R, tempMxM);
    if (debug_on) { out.println("tempMxM:\n" + tempMxM); }
    tempMxM = tempMxM.inverse();
    
    if (debug_on) { out.println("tempMxM Inverse:\n" + tempMxM); }
    Matrix.mult(tempNxM, tempMxM, K);
    if (debug_on) { out.println("K:\n" + K); }


    // compute a posteriori estimate of current state (x) base on
    // the new observation (newObservation is called 'z' in some texts)
    //
    if(ekf.isCorrectLinear())
    {        
        // x = x + K(newObservation - H*x)
        Matrix.mult(H, x, tempMx1);
        if (debug_on) { out.println("tempMx1:\n" + tempMx1); }
    }
    else
    {
        // x = x + K(newObservation - h(x))
        ekf.correct(x,tempMx1);
        if (debug_on) { out.println("tempMx1:\n" + tempMx1); }
    }
    Vec.sub(newObservation, tempMx1, tempMx1);
    if (debug_on) { out.println("tempMx1:\n" + tempMx1); }
    Matrix.mult(K, tempMx1, tempNx1);
    if (debug_on) { out.println("tempNx1:\n" + tempNx1); }
    Vec.add(x, tempNx1, x);
    if (debug_on) { out.println("xe:\n" + x); }

    
    // compute new estimate error covariance, P = (I - K*H)P
    Matrix.mult(K, H, tempNxN);
    if (debug_on) { out.println("tempNxN:\n" + tempNxN); }
    Matrix.sub(IdentityNxN, tempNxN, tempNxN);
    if (debug_on) { out.println("tempNxN:\n" + tempNxN); }
    Matrix.mult(tempNxN, P, tempNxN_2);
    if (debug_on) { out.println("tempNxN_2:\n" + tempNxN_2); }
    P.set(tempNxN_2);
    if (debug_on) { out.println("Pe:\n" + P); }
  }

  //---------------------------------------------------------------------------
  //                        TEST PROGRAM
  //---------------------------------------------------------------------------
  /*
  public static void main(String[] args)
  {     
    // observation size
    int M = 1;

    // step width
    final double dt = 1;

    class TestFuncs implements EKFuncs
    {
        // position measurement noise (feet)
        public double measnoise = 5; 

        // acceleration noise (feet/sec^2)
        public double accelnoise = 0.01; 
        
        public boolean isAconst() {return true;}
        public boolean isBconst() {return true;}
        public boolean isHconst() {return true;}
        public boolean isQconst() {return false;}
        public boolean isRconst() {return false;}
        
        public void getA(Vec x,Vec u,Matrix out)
        {
            // Set the mapping from time t to time t+1 for the two state
            // variables P and V (P_t+1 = 1*P_t + dt*V_t and V_t+1 = 0*P_t + 1*V_t)
            out.scale(0);
            out.set(0,0, 1.0);
            out.set(0,1, dt);
            out.set(1,0, 0.0);
            out.set(1,1, 1.0);
        }
        public void getB(Vec x,Vec u,Matrix out) {;}
        public void getH(Vec x,Matrix out)
        {
            // mapping from observation to state. Essentially, we're saying that
            // the single observation we're getting is a direct measurement of state
            // variable P and contains no information about state variable V
            out.scale(0);
            out.set(0,0, 1.0);
            out.set(0,1, 0.0);
        }
        public void getQ(Vec x,Vec u,Matrix out)
        {
            out.set(0,0, accelnoise*accelnoise);
            out.set(0,1, accelnoise*accelnoise);
            out.set(1,0, accelnoise*accelnoise);
            out.set(1,1, accelnoise*accelnoise);          
        }
        public void getR(Vec x,Matrix out)
        {
          out.identity();
          out.scale(measnoise*measnoise);
        }
        
        public int getStateSize() {return 2;}
        public int getObservationSize() {return 1;}
        public int getControlSize() {return 0;}
        
        public boolean isPredictLinear() {return true;}
        public boolean isCorrectLinear() {return true;}
        
        // These functions do not have to allow in-place operation.
        public void predict(Vec x,Vec u,Vec out) {;}
        public void correct(Vec x,Vec out) {;}
    };
    
    final TestFuncs funcs = new TestFuncs();
    
    // make the Kalman Filter
    final ExtendedKalmanFilter kf = new ExtendedKalmanFilter(funcs);
    
    // Make the state vector
    EKFVec x = new EKFVec(2); // Automatically zeroed
    
    // initial guess at what the initial estimate error covariance might be
    final double PGuess = funcs.accelnoise*funcs.accelnoise;   
    
    // Make the covariance matrix
    Matrix P = new Matrix(2,2);
    P.identity();
    P.scale(PGuess);
    
    // Because I can't be bothered to change the code below
    Matrix A = new Matrix(2,2);
    funcs.getA(null,null,A);
    Matrix H = new Matrix(1,2);
    funcs.getH(null,H);

    // number of values to retain for plotting
    int width = 75; 
    
    // set up GUI stuff
    JPanel parametersPanel = new JPanel(new GridLayout(2,3));
    final JTextField estimateErrorCovarianceTF =
      new JTextField("" + P.get(0,0));
    estimateErrorCovarianceTF.setEditable(false);
    final JTextField measurementVarianceTF =
      new JTextField("" + funcs.measnoise*funcs.measnoise);
    final JTextField processVarianceTF =
      new JTextField("" + funcs.accelnoise*funcs.accelnoise);
    parametersPanel.add(new JLabel("estimateErrorCovariance Matrix element [0,0]"));
    parametersPanel.add(new JLabel("measurementVariance"));
    parametersPanel.add(new JLabel("processVariance"));
    parametersPanel.add(estimateErrorCovarianceTF);
    parametersPanel.add(measurementVarianceTF);    
    parametersPanel.add(processVarianceTF);    
    measurementVarianceTF.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent evt) {
	  String text = measurementVarianceTF.getText();
	  funcs.measnoise = Double.valueOf(text).doubleValue();
	}
      });
    processVarianceTF.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent evt) {
	  String text = processVarianceTF.getText();
	  funcs.accelnoise = Double.valueOf(text).doubleValue();
	}
      });

    JPanel buttonPanel = new JPanel();
    final JButton observationsB = new JButton("Predict w/o observations");    
    buttonPanel.add(observationsB);
    observationsB.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent evt) {
	  String text = measurementVarianceTF.getText();
	  if (observationsB.getText().equals("Predict with observations"))
	    observationsB.setText("Predict w/o observations");
	  else
	    observationsB.setText("Predict with observations");
	}
      });

    JPanel inputPanel = new JPanel();
    inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
    inputPanel.add(parametersPanel);
    inputPanel.add(buttonPanel);

    JPanel mainPanel = new JPanel(new BorderLayout());
    
    GraphPanel panel = new GraphPanel();
    panel.setBackground(Color.darkGray);
    mainPanel.add("South", inputPanel);
    mainPanel.add("Center", panel);

    JFrame frame = new JFrame("Kalman Filter Test: observation = position, state = position + velocity");
    frame.setBounds(0, 0, 600, 400);
    frame.setContentPane(mainPanel);   
    frame.setVisible(true);

    // done setting up GUI stuff
    
    try
    {
      // for simulating the process and its measurement
      Random random = new java.util.Random(System.currentTimeMillis());
      Vec actualProcess = new Vec(2);
      EKFVec measurementOfProcess = new EKFVec(1);

      // storage for plotting
      double[] actuals = new double[width];
      double[] measurements = new double[width];
      double[] estimates = new double[width];
      double min = 20, max = 20;
      
      int i = 0;
      while (true)
      {
	// Simulate the process (Position P and Velocity V varying over time)
	Vec ProcessNoise = new Vec(new double[] { funcs.accelnoise/2.0, funcs.accelnoise/2.0 });
	actualProcess = A.postMult(actualProcess);
	Vec.add(actualProcess, ProcessNoise, actualProcess);
	
	// Simulate the noisy measurement (Measuring Position P)
	double MeasNoise = funcs.measnoise * random.nextGaussian();
	Vec temp = H.postMult(actualProcess);
	measurementOfProcess.set(0,temp.get(0));
	for (int k = 0; k < measurementOfProcess.size(); k++)
	{
	  measurementOfProcess.set(k, measurementOfProcess.get(k) + MeasNoise);
	}

	// use the KF to estimate the process given the noisy measurement
	try {
	  if (observationsB.getText().equals("Predict w/o observations"))
	    kf.update(x,P,measurementOfProcess);
	  else
	    kf.predict(x,P);
	}
	catch (SingularMatrixException e1) {
	  System.err.println("Singular matrix exception during update()" + e1);
	  e1.printStackTrace();
	}
	catch (NonSquareMatrixException e2) {
	  System.err.println("Non-square matrix exception during update: " + e2);
	  e2.printStackTrace();
	}

	// Save some values in vectors for plotting, tossing old values out
	int j = 0;
	if (i < width)
	{
	  j = i;
	}
	else
	{
	  for (int k = 1; k < width; k++)
	  {
	    actuals[k-1] = actuals[k];
	    measurements[k-1] = measurements[k];
	    estimates[k-1] = estimates[k];
	  }
	  j = width - 1;
	}
	
	actuals[j] = actualProcess.get(0);
	measurements[j] = measurementOfProcess.get(0);
	estimates[j] = x.get(0);

	// calculate min and max range for plotting
	min = Double.MAX_VALUE; max = Double.MIN_VALUE;
	for (int k = 0; k < width; k++)
	{
	  min = Math.min(min, Math.min(actuals[k], Math.min(measurements[k], estimates[k])));
	  max = Math.max(max, Math.min(actuals[k], Math.max(measurements[k], estimates[k])));
	}
	
	double center = min + (max-min)/2;
	double discrepency = Math.max(Math.abs(max-center), Math.abs(center - min));
	min = center - discrepency;
	max = center + discrepency;
	
	// plot
	panel.clear();
	panel.setAxes(Math.max(0, i-width), Math.max(i, width), min-5, max+5);
	panel.plot(actuals, Color.black, GraphPanel.LINE);
	panel.plot("Actual", Math.max(0, i-width) + width/6, center, Color.black);
	panel.plot(measurements, Color.blue, GraphPanel.LINE);
	panel.plot("Noisy Measurement", Math.max(0, i-width) + width/2, center, Color.blue);
	panel.plot(estimates, Color.red, GraphPanel.LINE);
	panel.plot("KF Estimate", Math.max(i, width) - width/6, center, Color.red);
	
	estimateErrorCovarianceTF.setText("" + P.get(0,0));

	//System.err.println("t = " + i);
	
	try {Thread.currentThread().sleep(75);} catch (InterruptedException ex) {};	
	i++;
      }


    }
    catch (Throwable e3)
    {
      System.err.println("Caught exception: " + e3);
      e3.printStackTrace();
    }
    finally
    {
      System.err.println("\n\n-- press Control-c to exit\n\n");
      try { System.in.read(); } catch( Exception e2 ) {}
    }
  }

  */
}

/************* Here's some matlab code that is close to the
	       test program

% kalman.m
% try kalman(100, 1)

function kalman(duration, dt)

% function kalman(duration, dt) - Kalman filter simulation
% duration = length of simulation (seconds)
% dt = step size (seconds)
% Copyright 1998 Innovatia Software.  All rights reserved.
% http://www.innovatia.com/software

measnoise = 10; % position measurement noise (feet)
accelnoise = 0.5; % acceleration noise (feet/sec^2)

a = [1 dt; 0 1]; % transition matrix
c = [1 0]; % measurement matrix
x = [0; 0]; % initial state vector
xhat = x; % initial state estimate

Q = accelnoise^2 * [dt^4/4 dt^3/2; dt^3/2 dt^2]; % process noise covariance
P = Q; % initial estimation covariance
R = measnoise^2; % measurement error covariance

% set up the size of the innovations vector
Inn = zeros(size(R));

pos = []; % true position array
poshat = []; % estimated position array
posmeas = []; % measured position array

Counter = 0;
for t = 0 : dt: duration,
    Counter = Counter + 1;
    % Simulate the process
    ProcessNoise = accelnoise * [(dt^2/2)*randn; dt*randn];
    x = a * x + ProcessNoise;
    % Simulate the measurement
    MeasNoise = measnoise * randn;
    z = c * x + MeasNoise;
    % Innovation
    Inn = z - c * xhat;
    % Covariance of Innovation
    s = c * P * c' + R;
    % Gain matrix
    K = a * P * c' * inv(s);
    % State estimate
    xhat = a * xhat + K * Inn;
    % Covariance of prediction error
    P = a * P * a' + Q - a * P * c' * inv(s) * c * P * a';
    % Save some parameters in vectors for plotting later
    pos = [pos; x(1)];
    posmeas = [posmeas; z];
    poshat = [poshat; xhat(1)];
end

% Plot the results
t = 0 : dt : duration;
t = t';
plot(t,pos,'r',t,poshat,'g',t,posmeas,'b');
grid;
xlabel('Time (sec)');
ylabel('Position (feet)');
title('Kalman Filter Performance');




*/
