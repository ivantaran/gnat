/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rinex;

import java.util.ArrayList;

/**
 *
 * @author Ivan
 */
public class ObserveData {
    private String name;
    private Double[] data;

    public ObserveData(String name, Double[] data) {
        this.name = name;
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
    public Double[] getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(Double[] data) {
        this.data = data;
    }
}
