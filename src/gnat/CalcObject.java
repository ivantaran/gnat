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
    private final static int MAX_POINTS = 0x7FFFFFFF;
    private final static double SPEED_OF_LIGHT = 299792458.0;
    private final static int DELTA_WIDTH = 5;
    private final static int DELTA_DX = 0;
    private final static int DELTA_DY = 1;
    private final static int DELTA_DZ = 2;
    private final static int DELTA_DT = 3;
    private final static int DELTA_DR = 4;
    
    private GlonassNavData navData = null;
    private double stepTime  =    1.0;
    private double startTime = -900.0;
    private double endTime   =  900.0;
    private final GiModel model = new GiModel();
    private final double position[];// = new double[6];
    private final HashMap<Integer, TreeMap<Double, double[]>> map = new HashMap();
    private final HashMap<Integer, TreeMap<Double, double[]  >> delta = new HashMap();
    
    CalcObject() {
        //55.753649, 37.754987
        position = new double[] {
            -5125976.8065 - 12.565,
             2688801.6022 - 36.198,
            -2669891.5334 - 6.750,

//            -5125912.0, 
//             2688768.0, 
//            -2669858.0, 
            
//            -5125977.0,
//             2688802.0,
//            -2669892.0,
            -0.0000004, 0.0, 0.0, 
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
        
        if ((int)((endTime - startTime) / stepTime) - 1 > MAX_POINTS) {
            endTime = startTime + stepTime * MAX_POINTS;
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
        result[GlonassSet.VectorLength] -= (navData.getTimeOffset() + position[3]) * SPEED_OF_LIGHT;
        
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
                
                TreeMap<Double, double[]> deltaRecord = delta.get(key);
                if (deltaRecord == null) {
                    deltaRecord = new TreeMap();
                    delta.put(key, deltaRecord);
                }
                for (Map.Entry<Double, double[]> ea : a.entrySet()) {
                    double bv[] = b.get(ea.getKey());
                    if (bv != null && ea.getValue()[2] != 0.0) {
                        double deltaValues[] = new double[DELTA_WIDTH];
                        deltaValues[DELTA_DX] = (position[0] - bv[0]) / ea.getValue()[2];
                        deltaValues[DELTA_DY] = (position[1] - bv[1]) / ea.getValue()[2];
                        deltaValues[DELTA_DZ] = (position[2] - bv[2]) / ea.getValue()[2];
                        deltaValues[DELTA_DT] = SPEED_OF_LIGHT;
                        deltaValues[DELTA_DR] = bv[GlonassSet.VectorLength] - ea.getValue()[2]; // TODO replace index 2 to const value
                        deltaRecord.put(ea.getKey(), deltaValues);
                    }
                }
            }
        }
    }
    
    public void saveDelta(String fileName) {
        String line;

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, false));
            for (HashMap.Entry<Integer, TreeMap<Double, double[]>> tm : delta.entrySet()) {
                for (Map.Entry<Double, double[]> entry : tm.getValue().entrySet()) {
                    
                    line = String.format(Locale.ROOT, "%d\t%d\t%.12e\n", 
                            tm.getKey(), 
                            entry.getKey().longValue(), 
                            entry.getValue()[DELTA_DR]
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
        int value = 0;
        for (HashMap.Entry<Integer, TreeMap<Double, double[]>> tm : delta.entrySet()) {
            value += tm.getValue().size();
        }
        return value;
    }
    
    public double sse() {
        double result = 0.0;
        double value;
        
        for (HashMap.Entry<Integer, TreeMap<Double, double[]>> tm : delta.entrySet()) {
            for (Map.Entry<Double, double[]> entry : tm.getValue().entrySet()) {
                value = entry.getValue()[DELTA_DR];
                result += value * value;
            }
        }
        
        return result;
    }
    
    public void copyJacobianAndDelta(double jacobian[][], double delta[]) {
        int i = 0;
        
        for (HashMap.Entry<Integer, TreeMap<Double, double[]>> tm : this.delta.entrySet()) {
            for (Map.Entry<Double, double[]> entry : tm.getValue().entrySet()) {
                jacobian[0][i] = entry.getValue()[DELTA_DX];
                jacobian[1][i] = entry.getValue()[DELTA_DY];
                jacobian[2][i] = entry.getValue()[DELTA_DZ];
                jacobian[3][i] = entry.getValue()[DELTA_DT];
                delta[i] = entry.getValue()[DELTA_DR];
                i++;
            }
        }
    }
}
