/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gnat;

/**
 *
 * @author Taran
 */
public class FirFilter extends Filter{
    private double[] fir;

    public FirFilter(double[] data, double[] fir) {
        this.data = data;
        this.fir = fir;
    }
    
    @Override
    public double[] getResult() {
        int k;
        double[] result = new double[data.length];
        
        for (int j = 0; j < data.length; j++) {
            result[j] = 0;
            k = (j - fir.length < 0) ? j : fir.length - 1;
            for (int i = (j - fir.length < 0) ? 0 : j - fir.length + 1; i < j + 1; i++) {
                result[j] += fir[k] * data[i];
                k--;
            }
        }
        
        return result;
    }
}
