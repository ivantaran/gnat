/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gnat;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import rinex.GlonassNavData;
import rinex.ObserveObject;

/**
 *
 * @author Taran
 */
public class CalcObject {
    private final static int MaximumPoints = 0x7FFFFFFF;
    private final static int StateWidth = 7;
    
    private GlonassNavData navData = null;
    private double stepTime  =    1.0;
    private double startTime = -900.0;
    private double endTime   =  900.0;
    private final GiModel model = new GiModel();
    private final double position[];// = new double[6];
    private final HashMap<Integer, TreeMap<Double, double[]>> map = new HashMap();
    private final HashMap<Integer, TreeMap<Double, Double>> delta = new HashMap();
    
    CalcObject() {
        //55.753649, 37.754987
        position = new double[] {
//            2844410.44715917, 
//            2202773.90845159, 
//            5249162.97072981,

            -5125976.8065,
             2688801.6022,
            -2669891.5334,

//            -5125912.0, 
//             2688768.0, 
//            -2669858.0, 
            
//            -5125977.0,
//             2688802.0,
//            -2669892.0,
            0.0, 0.0, 0.0, 
            0.0
        };
    }
    
    CalcObject(GlonassNavData navData) {
        this();
        calculate();
    }
    
    public void add(GlonassNavData navData) {
        this.navData = navData;
        calculate();
    }
    
    private GlonassSet init() {
        GlonassSet gset = new GlonassSet();
        double[] initial = new double[GlonassSet.InitialLength];
        double startTimeAbs;
        
        if (stepTime < Double.MIN_NORMAL) {
            stepTime  = Double.MIN_NORMAL;
        }
        
        if ((endTime - startTime) < Double.MIN_NORMAL) {
            startTime = 0.0;
            endTime = 0.0;
        }
        
        if ((int)((endTime - startTime) / stepTime) - 1 > MaximumPoints) {
            endTime = startTime + stepTime * MaximumPoints;
        }
        
        System.arraycopy(navData.getState(), 0, initial, 0, navData.getState().length);
        System.arraycopy(navData.getAcceleration(), 0, initial, navData.getState().length, navData.getAcceleration().length);
        
        gset.setInitial(initial);
        gset.setCurrent(initial.clone());
        
        if (startTime < 0) {
            gset.setStepTime(-stepTime);
        }
        else {
            gset.setStepTime( stepTime);
        }
        
        startTimeAbs = Math.abs(startTime);
        
        for (double i = 0; i < startTimeAbs; i += stepTime) {
            model.step(gset);
        }
                
        gset.setStepTime(stepTime);
        
        return gset;
    }
    
    private void calculate() {
        
        if (navData == null) {
            return;
        }
        
        GlonassSet gset = init();
        double t = navData.getTime().getTimeInMillis() / 1000L + startTime;
        
        TreeMap<Double, double[]> tm = map.get(navData.getNumber());
        
        if (tm == null) {
            tm = new TreeMap();
            map.put(navData.getNumber(), tm);
        }
        
        for (double i = startTime; i < endTime; i += stepTime) {
            tm.put(t, getMeasureArray(gset.getCurrent()));
            model.step(gset);
            t += stepTime;
        }
        
    }
    
    private double[] getMeasureArray(double[] coords) {
        double result[] = new double[GlonassSet.VectorLength + 1];
        double d[] = new double[3];

        System.arraycopy(coords, 0, result, 0, GlonassSet.VectorLength);

        d[0] = coords[0] - position[0];
        d[1] = coords[1] - position[1];
        d[2] = coords[2] - position[2];

        result[GlonassSet.VectorLength] = 
                Math.sqrt(d[0] * d[0] + d[1] * d[1] + d[2] * d[2]);
        //TODO make cvel as const
        result[GlonassSet.VectorLength] -= navData.getTimeOffset() * 299792458.0;
        
        return result;
    }
    
    public void save(String fileName) {
        String line;

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, false));
            for (HashMap.Entry<Integer, TreeMap<Double, double[]>> tm : map.entrySet()) {
                for (Map.Entry<Double, double[]> entry : tm.getValue().entrySet()) {
                    double ds[] = entry.getValue();
                    
                    line = String.format("%d\t%d\t", 
                            tm.getKey(), 
                            entry.getKey().longValue()
                    );
                    
                    for (int i = 0; i < ds.length; i++) {
                        line += String.format(Locale.ROOT, "%.12e\t", ds[i]);
                    }
                    line += "\n";
                    bw.write(line);
                }
            }
            bw.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        
    }
    
    public void setObserves(HashMap<String, ObserveObject> observes) {
        for (HashMap.Entry<String, ObserveObject> entry : observes.entrySet()) {
            if (entry.getKey().charAt(0) != 'R') {
                continue;
            }
            int key = Integer.parseInt(entry.getKey().replaceAll("^\\D", ""));
            TreeMap<Double, double[]> a = entry.getValue().getData();
            TreeMap<Double, double[]> b = map.get(key);
            if (a != null && b != null) {
                TreeMap<Double, Double> d = delta.get(key);
                if (d == null) {
                    d = new TreeMap();
                    delta.put(key, d);
                }
                for (Map.Entry<Double, double[]> ea : a.entrySet()) {
                    double bv[] = b.get(ea.getKey());
                    if (bv != null && ea.getValue()[2] != 0.0) {
                        d.put(ea.getKey(), bv[GlonassSet.VectorLength] - ea.getValue()[2]);
                    }
                }
            }
        }
    }
    
    public void saveDelta(String fileName) {
        String line;

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, false));
            for (HashMap.Entry<Integer, TreeMap<Double, Double>> tm : delta.entrySet()) {
                for (Map.Entry<Double, Double> entry : tm.getValue().entrySet()) {
                    
                    line = String.format(Locale.ROOT, "%d\t%d\t%.12e\n", 
                            tm.getKey(), 
                            entry.getKey().longValue(), 
                            entry.getValue()
                    );
                    bw.write(line);
                }
            }
            bw.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public int getLength() {
        return delta.size();
    }
    
    public double sse() {
        double result = 0.0;
        double value;
        
        for (HashMap.Entry<Integer, TreeMap<Double, Double>> tm : delta.entrySet()) {
            for (Map.Entry<Double, Double> entry : tm.getValue().entrySet()) {
                value = entry.getValue();
                result += value * value;
            }
        }
        
        return result;
    }
    
    public void jacobian(double jac[][]) {
        for (int i = 0; i < jac[0].length; ++i) {
            jac[0][i] = position[0] - 
        }
    }
}
