/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rinex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

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
                for (int i = 0; i < types.length; i++) {
                    line += types[i] + " ";
                }
                line += "\n";
                bw.write(line);
            }
            
            for (Iterator<Double[]> it = data.iterator(); it.hasNext();) {
                Double[] doubles = it.next();
                line = "";
                for (int i = 0; i < doubles.length; ++i) {
                    line += String.valueOf(doubles[i]) + " ";
                }
                line += "\n";
                bw.write(line, 0, line.length());
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
