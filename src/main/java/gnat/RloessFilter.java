/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gnat;

import java.util.Arrays;

/**
 *
 * @author Taran
 */
public class RloessFilter extends Filter {
    private final static int Order = 3;
    protected int width;

    public RloessFilter(double[] data, int width) {
        this.data = data;
        width += width % 2 - 1;
        this.width = width;
    }
    
    @Override
    public double[] getResult() {
        boolean ok;
        int index = 0;
        double[] result = new double[data.length];
        double[] fx;

        RobustPolyFitter rpf = new RobustPolyFitter(data, Order, index, width);
        fx = rpf.getResult();
        
        for (int i = 0; i < width / 2 + 1; i++)
            result[i] = fx[i];
        
        for (int i = width / 2 + 1; i < result.length - width / 2; i++) {
            index++;
            rpf.setIndex(index);
            fx = rpf.getResult();
            result[i] = rpf.getCoefficients()[0];
        }
        

        for (int i = width / 2 + 1; i < width; i++)
            result[result.length + i - width] = fx[i];

        return result;
    }
    
}
