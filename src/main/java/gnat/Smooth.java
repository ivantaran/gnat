/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gnat;

/**
 *
 * @author Taran
 */
public class Smooth extends Filter {
    private int order;

    public Smooth(double[] data, int order) {
        this.data = data;
        this.order = order;
    }
    
    @Override
    public double[] getResult() {
        double[] result = new double[data.length];
        
        double value = data[0];
        int window = order + (order % 2) - 1;
        result[0] = value;

        for(int i = 1; i < window / 2 + 1; i++) {
            value += data[2 * i - 1] + data[2 * i];
            result[i] = value / (2 * i + 1);
        }

        for (int i = window; i < data.length; i++) {
            value += data[i] - data[i - window];
            result[i - window / 2] = value / window;
        }

        for(int i = data.length - window / 2; i < data.length; i++) {
            value -= data[2 * i - data.length - 1] + data[2 * i - data.length];
            result[i] = value / (2 * (data.length - i) - 1);
        }
        
        return result;
    }
}
