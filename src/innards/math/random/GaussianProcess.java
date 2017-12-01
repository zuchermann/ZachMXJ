package innards.math.random;

import innards.buffer.CircularFloatBuffer;
import innards.debug.Debug;
import innards.math.linalg.*;

import java.io.*;

/**
    A one dimensional (one input or spacial dimension 't' with samples 'y') 
    guassian process
    */
    
public class GaussianProcess 
{
    public interface CovarianceFunction
    {
        public double compute(double t1,double t2);
    }
    
    public interface SampleCollection
    {
        public int getNumSamples();
        public double getSampleT(int i);
        public double getSampleY(int i);
        public void addSample(double t,double y);
        public void reset();
    }
    
    public interface GaussianPrior
    {
        public double getMean(double t);
        public double getSigma(double t);
    }

    /**
        see below for implementations
        */
    public GaussianProcess(CovarianceFunction c, SampleCollection s, GaussianPrior p)
    {
        covarianceFunction = c;
        sampleCollection = s;
        prior = p;
    }
    
    CovarianceFunction covarianceFunction;
    SampleCollection sampleCollection;
    GaussianPrior prior;
    
    // below depend on the N samples in the buffer
    Matrix Cn;
    Matrix CnInv;
    Vec TN;
    
    int globalSampleNum = -1;
    
    // below depend on the N+1 sample position 
    Vec m;
    Vec k;
    double kappa;
    double mu;
    Matrix Cnplus1Inv;
    
    double TNplus1;
    
    // (kappa,k and CnInv) are then used to compute:
    double TNplus1Hat;
    double SigmaTNplus1Hat;
    double currentSamplePosition = -Double.NEGATIVE_INFINITY;
    
    /**
        if you ask for a sample that is samplePositionEpsilon close to 
        the last sample you asked for, we don't recompute all those covariance functions
        to get to TNplus1Hat, SigmaTNplus1Hat because that damn is expensive
        */
    static public double samplePositionEpsilon = 0.01;

    /**
        maxSigma and defaultMean are returned in the event that this thing has no samples
        in it. Note: this is then multiplied by the prior, s.t. you'll probably get the
        prior out again
        */
    static public double maxSigma = 1000;
    static public double defaultMean = 0;

    protected void newSamplePosition(double t)
    {
        if (Math.abs(t-currentSamplePosition)<=samplePositionEpsilon) return;
        
        // Cn and CnInv must be valid
        buildCn();
        
        // build kappa        
        kappa = covarianceFunction.compute(t,t);
        Cnplus1Inv = realloc(Cnplus1Inv,globalSampleNum+1);
        
        // build k vector
        k = realloc(k,globalSampleNum);

        for(int i=0;i<k.size();i++)
        {
            double d = covarianceFunction.compute(sampleCollection.getSampleT(i),t);
            k.set(i,d);
        }

        // build kappa
        kappa = covarianceFunction.compute(t,t);
 
        if (Double.isNaN(kappa)) Debug.doReport("warning", "Gaussian process: kappa is nan ");
 
        // build TNplus1Hat
        TNplus1Hat = Matrix.multTriple(k,CnInv,TN);
        SigmaTNplus1Hat = kappa - Matrix.multTriple(k,CnInv,k); // inefficency ?      

        if (Double.isNaN(TNplus1Hat)) Debug.doReport("warning", "Gaussian process: TNplus1Hat is nan");
        if (Double.isNaN(SigmaTNplus1Hat)) Debug.doReport("warning", "Gaussian process: SigmaTNplus1Hat is nan");
        
        currentSamplePosition = t;
    }
    protected Matrix realloc(Matrix m, int size)
    {
        if (m==null) return new Matrix(size,size);
        if (m.numColumns()!=size) return new Matrix(size,size); else return m;
    }
    protected Vec realloc(Vec m, int size)
    {
        if (m==null) return new Vec(size);
        if (m.dim()!=size) return new Vec(size); else return m;
    }
    
    private boolean dirty = true;
    protected void buildCn()
    {
        if (!dirty) return;
        dirty = false;
        
        int oldGlobalSampleNum = globalSampleNum;
        globalSampleNum = sampleCollection.getNumSamples();
        if (globalSampleNum!=oldGlobalSampleNum)
        {
            // reallocate storage
            Cn = new Matrix(globalSampleNum,globalSampleNum);
            CnInv = new Matrix(globalSampleNum,globalSampleNum);
            TN = new Vec(globalSampleNum);
        }
        
        // build Cn
        for(int i=0;i<globalSampleNum;i++)
        {
            for(int m=i;m<globalSampleNum;m++)
            {
                double d = covarianceFunction.compute(sampleCollection.getSampleT(i),sampleCollection.getSampleT(m));
                Cn.set(i,m,d);
                Cn.set(m,i,d);
            }
        }
        
        // invert it
        try{
            Debug.doReport("warning", "Gaussian process: about to invert :"+Cn+" "+CnInv);
            if (Cn.numRows()!=0) Cn.inverse(CnInv);
        }
        catch(SingularMatrixException ex)
        {
            throw new IllegalStateException(" SingularMatrixException caught in GaussianProcess - it's game over <"+ex+">");
        }
        catch(NonSquareMatrixException ex)
        {
            throw new IllegalStateException(" NonSquareMatrixException caught in GaussianProcess - this will never happen");
        }
        
        // build Tn
        for(int i=0;i<globalSampleNum;i++) TN.set(i,sampleCollection.getSampleY(i));        
    }
    
    public void addSample(double t,double y)
    {
        sampleCollection.addSample(t,y);
        dirty = true;
    }
    
    public void resetSamples(){sampleCollection.reset();}
    
    public double obtainProbability(double atT, double ofY)
    {
        newSamplePosition(atT);        
        
        // munge mean and variance to account for prior
        
        double pmean = prior.getMean(atT);        
        double psigma = prior.getSigma(atT);
        
        double s2 = SigmaTNplus1Hat*SigmaTNplus1Hat;
        double s22 = psigma*psigma;
        
        double mean = (TNplus1Hat*s22 + pmean*s2)/(s2+s22);
        double sigma = Math.sqrt(Math.sqrt( (s2*s22)/(s2+s22)));
        
        if (sigma<0.000001) return ofY==mean ? 1.0 : 0.0;
        
        // -- look this up marc
        return Math.sqrt(0.5/sigma)*Math.exp(-(ofY-mean)*(ofY-mean)/(2*sigma*sigma));
    }

    public double obtainSample(double atT)
    {
        newSamplePosition(atT);        
        
        // munge mean and variance to account for prior
        
        double pmean = prior.getMean(atT);        
        double psigma = prior.getSigma(atT);
        
        double s2 = SigmaTNplus1Hat*SigmaTNplus1Hat;
        double s22 = psigma*psigma;
        
        double mean = (TNplus1Hat*s22 + pmean*s2)/(s2+s22);
        double sigma = Math.sqrt(Math.sqrt( (s2*s22)/(s2+s22)));
                
        double r = sampleGaussian(mean,sigma);

        if (Double.isNaN(r))
        {
             Debug.doReport("warning", "Gaussian process: nan sample from <"+mean+","+sigma+">");
            // typical cause is variance underflow so,
            r = TNplus1Hat;
        }
        return r;
    }
    
    GaussianRandomVariable gaussianSource = new GaussianRandomVariable(1,0.5);
    
    protected double sampleGaussian(double mean, double sigma)
    {
        gaussianSource.setMeanAndVariance(mean,sigma);
        return gaussianSource.sample();
    }


    // implementations of covariance functions

    /**
        alpha * exp(-1/2 * beta^2 * (delta T)^T);
        
        e.g. 1/beta is a timescale for change and large alpha implies high confidence in the data
        
        */
    static public class GaussianCovariance implements CovarianceFunction
    {
        private double beta;
        private double alpha;
        
        public GaussianCovariance(double alpha, double beta)
        {
            this.beta = beta;
            this.alpha = alpha;
        }
        
        public double compute(double t1, double t2)
        {
            if (t1==t2) return alpha;
            double dt = (t1-t2)*beta;
            return alpha * Math.exp(-0.5*dt*dt*beta);
        }
    }
    
    /**
        the simplest (and least appropriate perhaps) sampleCollection
        - some that keeps up to a maximum number of entries
        */
    static public class FiniteSpaceSampleCollection implements SampleCollection
    {
        CircularFloatBuffer sampleT;
	CircularFloatBuffer sampleY;
        
        public FiniteSpaceSampleCollection(int maxSize)
        {
            sampleT = new CircularFloatBuffer(maxSize);
            sampleY = new CircularFloatBuffer(maxSize);
        }
        
        public int getNumSamples()
        {
            return sampleT.backSize();
        }
        public double getSampleT(int i)
        {
            return sampleT.getOffset(i);
        }
        public double getSampleY(int i)
        {
            return sampleY.getOffset(i);
        }
        public void addSample(double t,double y)
        {
            sampleT.push((float)t);
            sampleY.push((float)t);
        }        
        public void reset()
        {
            sampleT.reset();
            sampleY.reset();
        }
    }
    
    /**
        the simplest guassian prior - a constant
        */
    static public class ConstantGaussianPrior implements GaussianPrior
    {
        public double mean;
        public double sigma;
        
        public ConstantGaussianPrior(double mean,double sigma)
        {
            this.mean = mean;
            this.sigma = sigma;
        }
        
        public double getMean(double t)
        {
            return mean;
        }
        public double getSigma(double t)
        {
            return sigma;
        }
    }
    
    static public void main(String[] hmm)
    {
        GaussianProcess gp 
            = new GaussianProcess(new GaussianProcess.GaussianCovariance(0.1,1), 
                                  new GaussianProcess.FiniteSpaceSampleCollection(10),
                                  new GaussianProcess.ConstantGaussianPrior(0.5,100));

        for(int i=0;i<10;i++)
        {
            double s = gp.obtainSample(0);
            System.out.println(" sample : "+s);
        }
        System.out.println();
        
        gp.addSample(1, 1);
        gp.addSample(2, 2);
        gp.addSample(3, 0.5);
        gp.addSample(4, 0.5);
        gp.addSample(8, 0.5);
        gp.addSample(8.2, 2);
        gp.addSample(1, 1);
        gp.addSample(2, 2);
        gp.addSample(3, 0.5);
        gp.addSample(4, 0.5);
        gp.addSample(8, 0.5);
        gp.addSample(8.2, 2);
        
        try{
            PrintWriter pw = new PrintWriter(new FileOutputStream(new File("c:/tmp/gpOutput.txt")));
            for(int t=0;t<100;t++)
            {
                double nt = t/10.0;
                for(int i=0;i<100;i++)
                {
                    pw.print(gp.obtainSample(nt)+" ");
                }
                pw.println();
                System.out.println("<"+nt+"> of 10");
            }
            pw.close();
        }
        catch(Exception ex){ex.printStackTrace();}     
    }
}