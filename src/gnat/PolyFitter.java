/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gnat;

/**
 *
 * @author Taran
 */
public class PolyFitter extends Filter{
    protected int order;
    protected int index;
    protected int length;
    protected double[] coefficients;
    protected double scaleMean = 0;
    protected double scaleStdDev = 1;

    public PolyFitter(double[] data, int order, int index, int length) {
        this.data = data;
        this.order = order;
        this.index = index;
        this.length = length;
        coefficients = new double[order];
    }
        
    private double makeScaleMean(int len) {
        return ((double)len - 1.0) / 2.0;
    }

    private double makeScaleStdDev(double mean, int len) {
        double std = 0;
        double dq;

        for (int i = 0; i < len; i++) {
            dq = i - mean;
            dq *= dq;
            std += dq;
        }
        
        std = Math.sqrt(std / (len - 1.0));

        return std;
    }

    private double poly(double t, double[] k) {
        double result = k[0];

        for (int i = 1; i < k.length; i++)
            result += k[i] * Math.pow(t, i);

        return result;
    }

    protected void fillx(double[] buffer, double mean, double std, int n) {
        for (int i = 0; i < n; i++)
            buffer[i] = ((double)i - mean) / std;
    }

    protected void filly(double[] x, double[] buffer, int degree, int n) {
        for (int i = 0; i < n; i++) 
            buffer[i] = Math.pow(x[i], degree);
    }

    protected void fx(double[] d) {
        double t;

        for (int i = 0; i < d.length; i++) {
            t = (i - scaleMean) / scaleStdDev;
            d[i] = poly(t, getCoefficients());
        }
    }
    
    protected void makeScale() {
        scaleMean = makeScaleMean(data.length);
        scaleStdDev = makeScaleStdDev(scaleMean, data.length);
    }
    
    @Override
    public double[] getResult() {
        boolean ok;
        double[] result = new double[data.length];
        
        double[][] m   =  Blas.getMatrix(order, data.length);
        double[][] qim =  Blas.getMatrix(order, data.length);
        double[][] q   =  Blas.getMatrix(order, order);
        double[][] qi  =  Blas.getMatrix(order, order);
        double[] x = Blas.getVector(data.length);
        
        makeScale();
        
        fillx(x, scaleMean, scaleStdDev, data.length);

        for (int i = 0; i < order; i++)
            filly(x, m[i], i, data.length);
        
        Blas.sqr(m, q);
        ok = Blas.hinv(q, qi);

        if (ok) {
            Blas.mul(qi, m, qim);
            Blas.mul(qim, data, getCoefficients());
            fx(result);
        }

        return result;
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * @return the coefficients
     */
    public double[] getCoefficients() {
        return coefficients;
    }
    
}
