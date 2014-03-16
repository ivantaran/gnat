/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rinex;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Ivan
 */
public class ObserveObject {
    private String name;
    private String[] types;
    private ArrayList<Double[]> data = new ArrayList();

    public ObserveObject(String name, String[] types) {
        this.name = name;
        this.types = types;
    }
    
    /**
     * @return the data
     */
    public ArrayList<Double[]> getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(ArrayList<Double[]> data) {
        this.data = data;
    }
    
    public void save(String fileName, boolean writeHeader) {
        String line;
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, false));
            
            if (writeHeader) {
                line = "";
                for (String type : types) {
                    line += type + " ";
                }
                line += "\n";
                bw.write(line);
            }
            for (Double[] doubles : data) {
                line = "";
                for (int i = 0; i < doubles.length; ++i) {
                    line += String.valueOf(doubles[i]) + " ";
                }
                line += "\n";
                bw.write(line);
            }
            bw.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the typeObserves
     */
    public String[] getTypes() {
        return types;
    }
}
