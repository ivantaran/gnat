/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gnat;

/**
 *
 * @author Taran
 */
public final class Statistics {
    
    public static double mean(double[] x) {
        double value = 0;
    
        for (int i = 0; i < x.length; i++) {
            value += x[i];
        }
        
        return value / x.length;
    }
    
    public static double stddev(double[] x, double mean) {
        double std = 0;
        double dq;

        for (int i = 0; i < x.length; ++i) {
            dq = x[i] - mean;
            dq *= dq;
            std += dq;
        }
        
        std = Math.sqrt(std / (x.length - 1));

        return std;
    }
    
    public static double mad(double[] x, double mean) {
        double md = 0;

        for (int i = 0; i < x.length; i++) {
            md += Math.abs(x[i] - mean);
        }
        md /= (double)x.length;

        return md;
    }

    public static double sse(double[] x) {
        double value = 0;

        for (int i = 0; i < x.length; i++) {
            value += x[i] * x[i];
        }

        return value;
    }
    
}


