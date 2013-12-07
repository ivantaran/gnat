/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gnat;

/**
 *
 * @author Taran
 */
public class RobustPolyFitter extends PolyFitter {
    
    private final static int CountOfIterations = 6;
    
    public RobustPolyFitter(double[] data, int order) {
        super(data, order);
    }

    
    private int weights(double[] r, double[] w, double mean) {
        int err = 0;
        mean = Statistics.mean(r);
        double m = Statistics.mad(r, mean) * 6.0;

        for (int i = 0; i < r.length; i++) {
            if (Math.abs(r[i]) > m) {
                w[i] = 0.0;
                err++;
            }
            else {
                if ((w[i] > Double.MIN_VALUE) && (m > Double.MIN_VALUE)) {
                    w[i] = Math.pow(1 - Math.pow(r[i] / m, 2), 2);
                }
            }
        }

        return err;
    }
    
    @Override
    public double[] getResult() {
        boolean ok;
        int n = 0;
        double mean;
        double[] result = new double[data.length];
        double[][] m   = Blas.getMatrix(data.length, order);
        double[][] mw  = Blas.getMatrix(order, data.length);
        double[][] qim = Blas.getMatrix(order, data.length);
        double[][] q  = Blas.getMatrix(order, order);
        double[][] qi = Blas.getMatrix(order, order);
        double[] x = Blas.getVector(data.length);
        double[] w = Blas.getVector(data.length);
        double[] r = Blas.getVector(data.length);

        Blas.fill(w, 1.0);
        makeScale();
        fillx(x, scaleMean, scaleStdDev, data.length);


        for (int i = 0; i < order; i++)
            filly(x, mw[i], i, data.length);
        
        Blas.trp(mw, m);

        for (int j = 0; j < CountOfIterations; j++) {
            Blas.ttmul(w, m, mw);
            Blas.mul(mw, m, q);
            ok = Blas.hinv(q, qi);

            if (ok) {
                Blas.mul(qi, mw, qim);
                Blas.mul(qim, data, coefficients);
                fx(result);
                Blas.sub(data, result, r);
                mean = Statistics.mean(r);
                Blas.mul(r, w, r);
                n = weights(r, w, mean);
            }
            else {
                break;
            }
        }
        
        return result;
    }

}
