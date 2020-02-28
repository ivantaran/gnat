/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rinex;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
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
    private final TreeMap<Long, HashMap<String, Double>> data = new TreeMap();

    public ObserveObject(String name) {
        this.name = name;
    }
    
    /**
     * @return the data
     */
    public TreeMap<Long, HashMap<String, Double>> getData() {
        return data;
    }

    public void putObsData(long time, String[] types, double[] obs) {
        HashMap<String, Double> lineObs = data.getOrDefault(time, new HashMap());
        for (int i = 0; i < Math.min(types.length, obs.length); i++) {
            lineObs.put(types[i], obs[i]);
        }
        data.put(time, lineObs);
    }
    
    /**
     * @param fileName
     * @param writeHeader
     */
    public void save(String fileName, boolean writeHeader) {
        String line;
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, false));

            TreeSet<String> typesSet = new TreeSet();
            data.entrySet().forEach((entry) -> {
                entry.getValue().entrySet().forEach((value) -> {
                    typesSet.add(value.getKey());
                });
            });
            
            if (writeHeader) {
                line = "Time\t";
                line = typesSet.stream().map((type) -> type + '\t')
                        .reduce(line, String::concat);                
                line += "\n";
                bw.write(line);
            }
            
            for (Map.Entry<Long, HashMap<String, Double>> entry : data.entrySet()) {
                line = String.format("%d\t", entry.getKey());
                
                line = typesSet.stream().map((type) -> 
                        String.format(Locale.ROOT, "%.12e\t", 
                                entry.getValue().getOrDefault(type, 0.0)))
                        .reduce(line, String::concat);
                
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
}
