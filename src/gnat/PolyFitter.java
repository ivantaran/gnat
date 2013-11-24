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
    private int order;
    private double[] coefficients;
    private double scaleMean = 0;
    private double scaleStdDev = 1;

    public PolyFitter(double[] data, int order) {
        this.data = data;
        this.order = order;
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

    private void fillx(double[] buffer, double mean, double std, int n) {
        for (int i = 0; i < n; i++)
            buffer[i] = ((double)i - mean) / std;
    }

    private void filly(double[] x, double[] buffer, int degree, int n) {
        for (int i = 0; i < n; i++) 
            buffer[i] = Math.pow(x[i], degree);
    }

    private void fx(double[] d) {
        double t;

        for (int i = 0; i < d.length; i++) {
            t = (i - scaleMean) / scaleStdDev;
            d[i] = poly(t, coefficients);
        }
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
        
        scaleMean = makeScaleMean(data.length);
        scaleStdDev = makeScaleStdDev(scaleMean, data.length);

        fillx(x, scaleMean, scaleStdDev, data.length);

        for (int i = 0; i < order; i++)
            filly(x, m[i], i, data.length);
        
        Blas.sqr(m, q);
        ok = Blas.hinv(q, qi);

        if (ok) {
            Blas.mul(qi, m, qim);
            Blas.mul(qim, data, coefficients);
            fx(result);
        }

        return result;
    }
    
}
