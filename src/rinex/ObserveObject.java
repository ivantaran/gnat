/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rinex;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Ivan
 */
public class ObserveObject {
    private String name;
    private String[] types;
    private TreeMap<Double, double[]> data = new TreeMap();

    public ObserveObject(String name, String[] types) {
        this.name = name;
        this.types = types;
    }
    
    /**
     * @return the data
     */
    public TreeMap<Double, double[]> getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(TreeMap<Double, double[]> data) {
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
            for (Map.Entry<Double, double[]> entry : data.entrySet()) {
                line = String.format("%d\t", entry.getKey().longValue());
                for (int i = 0; i < entry.getValue().length; ++i) {
                    line += String.format(Locale.ROOT, "%.12e\t", entry.getValue()[i]);
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
