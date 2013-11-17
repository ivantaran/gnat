/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gnat;

/**
 *
 * @author Taran
 */
public abstract class Filter {
    protected double[] data;

    /**
     * @return the data
     */
    public double[] getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(double[] data) {
        this.data = data;
    }

    /**
     * @return the result
     */
    public abstract double[] getResult();
}
