/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gnat;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
    private final static double WE = 7.2921151467e-5;
    private final static int DELTA_WIDTH = 5;
    private final static int DELTA_DX = 0;
    private final static int DELTA_DY = 1;
    private final static int DELTA_DZ = 2;
    private final static int DELTA_DT = 3;
    private final static int DELTA_DR = 4;
    
    private final ArrayList<GlonassNavData> navDataList = new ArrayList();
    
    private double stepTime  =    1.0;
    private double startTime = -900.0;
    private double endTime   =  900.0;
    private final GiModel model = new GiModel();
    private final double position[];// = new double[6];
    private final HashMap<Integer, TreeMap<Double, double[]>> map = new HashMap();
    private final HashMap<Integer, TreeMap<Double, double[]  >> delta = new HashMap();
    private final HashMap<String, ObserveObject> obsMap = new HashMap();
    CalcObject() {
        //55.753649, 37.754987
        position = new double[] {
             1448636.9300,
            -3385243.6700,
             5191046.9500,
            0.0,
            0.0,
            0.0,  
            0.0
        };
    }
    
    public double[] getPosition() {
        return position;
    }
    
    public void addGlonassNavDataList(ArrayList<GlonassNavData> navDataList) {
        this.navDataList.addAll(navDataList);
        updateNavData(position);
    }
    
    private GlonassSet init(GlonassNavData navData) {
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
        
//        gset.setStepTime(-navData.getTimeOffset());
//        System.out.println(navData.getTimeOffset());
        gset.setStepTime(0.000); //TODO correct rotation -0.07101
        model.step(gset);
        
        gset.setStepTime(stepTime);
        
        return gset;
    }
    
    private void updateNavData(double[] subject) {
        
        navDataList.forEach((navData) -> {
            GlonassSet gset = init(navData);
            double toe     = (double)navData.getTime().getTimeInMillis() * 0.001;
            double current = toe + startTime;
            TreeMap<Double, double[]> tm = map.get(navData.getNumber());

            if (tm == null) {
                tm = new TreeMap();
                map.put(navData.getNumber(), tm);
            }

            for (double i = startTime; i < endTime; i += stepTime) {
                tm.put(current, getMeasureArray(navData, current - toe, subject, gset.getCurrent()));
                model.step(gset);
                current += stepTime;
            }
        });        
    }
    
    private double[] getMeasureArray(GlonassNavData navData, double dt, double[] subject, double[] object) {
        double result[] = new double[GlonassSet.VectorLength + 1];
        System.arraycopy(object, 0, result, 0, GlonassSet.VectorLength);
        result[GlonassSet.VectorLength] = (-8.055940270424e-08 + navData.getTimeOffset() + navData.getFrequencyOffset() * dt) * SPEED_OF_LIGHT - subject[3];
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
    
    public void addObservesMap(HashMap<String, ObserveObject> observes) {
        obsMap.putAll(observes);
        updateObserves(position);
    }
    
    private void updateObserves(double[] subject) {
        int objectName;
        for (HashMap.Entry<String, ObserveObject> entry : obsMap.entrySet()) {
            if (entry.getKey().charAt(0) != 'R') {
                continue;
            }
            
            try {
                objectName = Integer.parseInt(entry.getKey().replaceAll("^\\D", "").trim());
            } catch (NumberFormatException e) {
                System.out.println(e.getMessage());
                System.out.println(entry.getKey());
                continue;
            }
            
            TreeMap<Double, double[]> a = entry.getValue().getData();
            TreeMap<Double, double[]> b = map.get(objectName);
            if (a != null && b != null) {
                
                TreeMap<Double, double[]> deltaRecord = delta.get(objectName);
                if (deltaRecord == null) {
                    deltaRecord = new TreeMap();
                    delta.put(objectName, deltaRecord);
                }

                for (Map.Entry<Double, double[]> ea : a.entrySet()) {
                    double bv[] = b.get(ea.getKey());
                    if (bv != null && ea.getValue()[4] != 0.0 && ea.getValue()[6] > 30.0) {
                        
                        double theta = -(ea.getValue()[4] + bv[GlonassSet.VectorLength]) * GiModel.WE / GiModel.CVEL;
                        double x = bv[0] * Math.cos(theta) - bv[1] * Math.sin(theta);
                        double y = bv[0] * Math.sin(theta) + bv[1] * Math.cos(theta);
                        double z = bv[2];
                        double gr = Math.sqrt((subject[0] - x) * (subject[0] - x) + (subject[1] - y) * (subject[1] - y) + (subject[2] - z) * (subject[2] - z));
                        double dr = ((subject[0] - x) * bv[3] + (subject[1] - y) * bv[4] + (subject[2] - z) * bv[5]) / SPEED_OF_LIGHT;

                        double deltaValues[] = new double[DELTA_WIDTH];
                        deltaValues[DELTA_DX] = (subject[0] - x) / gr;
                        deltaValues[DELTA_DY] = (subject[1] - y) / gr;
                        deltaValues[DELTA_DZ] = (subject[2] - z) / gr;
                        deltaValues[DELTA_DT] = 1.0;
                        deltaValues[DELTA_DR] = ea.getValue()[4] + bv[GlonassSet.VectorLength] - gr - dr;// - sagnac;
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
        value = delta.entrySet().stream().map((tm) -> tm.getValue().size()).reduce(value, Integer::sum);
        return value;
    }
    
    public double sse(double[] subject) {
        double result = 0.0;
        double value;
        int c = 0;
        
        updateNavData(subject);
        updateObserves(subject);
        
        for (HashMap.Entry<Integer, TreeMap<Double, double[]>> tm : delta.entrySet()) {
            for (Map.Entry<Double, double[]> entry : tm.getValue().entrySet()) {
                value = entry.getValue()[DELTA_DR];
                result += value * value;
                c++;
            }
        }
        
        result /= (double)c;
        
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
