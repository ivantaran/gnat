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
    private long time;

    public ObserveSample(String name, long time, double[] data) {
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
    public long getTimeInMillis() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTimeInMillis(long time) {
        this.time = time;
    }
}
