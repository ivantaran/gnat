/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rinex;

/**
 *
 * @author Ivan
 */
public class ObserveSample {
    private String name;
    private double[] data;
    private double time;

    public ObserveSample(String name, double time, double[] data) {
        this.name = name;
        this.time = time;
        this.data = data;
    }

    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

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
     * @return the time
     */
    public double getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(double time) {
        this.time = time;
    }
}
