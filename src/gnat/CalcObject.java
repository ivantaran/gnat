/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gnat;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import rinex.GlonassNavData;

/**
 *
 * @author Taran
 */
public class CalcObject {
    private final static int MaximumPoints = 0x7FFFFFFF;
    private final static int StateWidth = 7;
    
    private final String name = "default";
    private GlonassNavData navData = null;
    private double stepTime  =    1.0;
    private double startTime = -900.0;
    private double endTime   =  900.0;
    private final GregorianCalendar time = new GregorianCalendar();
    private final GiModel model = new GiModel();
//    private final TreeMap<Double, double[]> state = new TreeMap();
    private final HashMap<Integer, TreeMap<Double, double[]>> map = new HashMap();
    
    CalcObject() {

    }
    
    CalcObject(GlonassNavData navData) {
        this.navData = navData;
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
            tm.put(t, gset.getCurrent().clone());
            model.step(gset);
            t += stepTime;
        }
        
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
    
}
