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
import java.util.GregorianCalendar;
import rinex.GlonassNavData;

/**
 *
 * @author Taran
 */
public class CalcObject {
    private final static int MaximumPoints = 0x7FFFFFFF;
    private final static int StateWidth = 7;
    
    private String name = "default";
    private GlonassNavData navData;
    private double stepTime  =    1.0;
    private double startTime = -899.0;
    private double endTime   =  900.0;
    private GregorianCalendar time = new GregorianCalendar();
    private GiModel model = new GiModel();
    private ArrayList<double[]> state = new ArrayList();
    
    CalcObject(GlonassNavData navData) {
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
        
        if (startTime < 0) {
            gset.setStepTime(-stepTime);
        }
        else {
            gset.setStepTime(stepTime);
        }
        
        startTimeAbs = Math.abs(startTime);
        
        for (double i = 0; i < startTimeAbs; i += stepTime) {
            model.step(gset);
        }
        
        gset.setStepTime(stepTime);
        
        return gset;
    }
    
    private void calculate() {
        GlonassSet gset = init();
        
        for (double i = startTime; i < endTime; i += stepTime) {
            model.step(gset);
            state.add(gset.getCurrent().clone());
        }
        
    }
    
    public void save(String fileName) {
        String line;

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, false));
    
            for (double[] ds : state) {
                line = "";
                for (int i = 0; i < ds.length; i++) {
                    line += String.valueOf(ds[i]) + " ";
                }
                line += "\n";
                bw.write(line);
            }
            
            bw.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        
    }
    
}
