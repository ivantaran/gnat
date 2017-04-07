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
import java.util.List;
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
    private final static int DELTA_WIDTH = 9;
    private final static int DELTA_DX   = 0;
    private final static int DELTA_DY   = 1;
    private final static int DELTA_DZ   = 2;
    private final static int DELTA_DT   = 3;
    private final static int DELTA_DR   = 4;
    private final static int DELTA_MP1  = 5;
    private final static int DELTA_MP2  = 6;
    private final static int DELTA_IONP = 7;
    private final static int DELTA_IONL = 8;
    
    private final static int NAVMAP_WIDTH = 10;
    private final static int NAVMAP_X   = 0;
    private final static int NAVMAP_Y   = 1;
    private final static int NAVMAP_Z   = 2;
    private final static int NAVMAP_VX  = 3;
    private final static int NAVMAP_VY  = 4;
    private final static int NAVMAP_VZ  = 5;
    private final static int NAVMAP_T   = 6;
    private final static int NAVMAP_L1  = 7;
    private final static int NAVMAP_L2  = 8;
    private final static int NAVMAP_L3  = 9;
    
    private final ArrayList<GlonassNavData> navDataList = new ArrayList();
    
    private double stepTime  =    1.0;
    private double startTime = -900.0;
    private double endTime   =  900.0;
    private final GiModel model = new GiModel();
    private final double position[];
    private final HashMap<Integer, TreeMap<Double, double[]>> navMap = new HashMap();
    private final HashMap<Integer, TreeMap<Double, double[]>> delta  = new HashMap();
    private final HashMap<String, ObserveObject> obsMap = new HashMap();
    
    CalcObject() {
        position = new double[] {
//            2821841.0,
//            2202230.0,
//            5261499.0,
            0.0,
            0.0,
            0.0,  
            0.0,
            0.0,
            0.0,  
            0.0
        };
    }
    
    public void setPositionXyz(double[] xyz) {
        position[0] = xyz[0];
        position[1] = xyz[1];
        position[2] = xyz[2];
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
//        gset.setStepTime(1.47500); //TODO correct rotation -0.07101
//        model.step(gset);
        
        gset.setStepTime(stepTime);
        
        return gset;
    }
    
    private void updateNavData(double[] subject) {
        
        navDataList.forEach((navData) -> {
            GlonassSet gset = init(navData);
            double toe     = (double)navData.getTime().getTimeInMillis() * 0.001;
            double current = toe + startTime;
            TreeMap<Double, double[]> tm = navMap.get(navData.getNumber());

            if (tm == null) {
                tm = new TreeMap();
                navMap.put(navData.getNumber(), tm);
            }

            for (double i = startTime; i < endTime; i += stepTime) {
                tm.put(current, getMeasureArray(navData, current - toe, subject, gset.getCurrent()));
                model.step(gset);
                current += stepTime;
            }
        });        
    }
    
    private double[] getMeasureArray(GlonassNavData navData, double dt, double[] subject, double[] object) {
        double result[] = new double[NAVMAP_WIDTH];
        System.arraycopy(object, 0, result, 0, GlonassSet.VectorLength);
        double glotime = 0.0; //TODO read from obs file
        result[NAVMAP_T] = (glotime + navData.getTimeOffset() + navData.getFrequencyOffset() * dt) * GiModel.CVEL - subject[3];
        result[NAVMAP_L1] = navData.getFrequencyL1() * navData.getFrequencyOffset() + navData.getFrequencyL1();
        result[NAVMAP_L2] = navData.getFrequencyL2() * navData.getFrequencyOffset() + navData.getFrequencyL2();
        result[NAVMAP_L3] = navData.getFrequencyL3() * navData.getFrequencyOffset() + navData.getFrequencyL3();
        return result;
    }
    
    public void save(String fileName) {
        String line;

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, false));
            for (HashMap.Entry<Integer, TreeMap<Double, double[]>> tm : navMap.entrySet()) {
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
        double[] aerv = new double[3];
        double elv;
        boolean ok;
        
        delta.clear();
        
        for (HashMap.Entry<String, ObserveObject> obsObject : obsMap.entrySet()) {
            if (obsObject.getKey().charAt(0) != 'R') {
                continue;
            }
            
            try {
                objectName = Integer.parseInt(obsObject.getKey().replaceAll("^\\D", "").trim());
//                if (objectName != 17) continue;
            } catch (NumberFormatException e) {
                System.out.println(e.getMessage());
                System.out.println(obsObject.getKey());
                continue;
            }
            
            TreeMap<Double, HashMap<String, Double>> a = obsObject.getValue().getData();
            TreeMap<Double, double[]> b = navMap.get(objectName);
            if (a != null && b != null) {
                
                TreeMap<Double, double[]> deltaRecord = delta.get(objectName);
                if (deltaRecord == null) {
                    deltaRecord = new TreeMap();
                    delta.put(objectName, deltaRecord);
                }

                for (Map.Entry<Double, HashMap<String, Double>> ea : a.entrySet()) {
                    double object[] = b.get(ea.getKey());
                    
                    if (object == null) {
                        continue;
                    }
                    
                    double obsP1    = ea.getValue().getOrDefault("P1", 0.0);
                    double obsL1    = ea.getValue().getOrDefault("L1", 0.0) * GiModel.CVEL / object[NAVMAP_L1];
                    double obsSnr1  = ea.getValue().getOrDefault("S1", 0.0);
                    double obsP2    = ea.getValue().getOrDefault("P2", 0.0);
                    double obsL2    = ea.getValue().getOrDefault("L2", 0.0) * GiModel.CVEL / object[NAVMAP_L2];
                    double obsSnr2  = ea.getValue().getOrDefault("S2", 0.0);
                    double snr      = Math.min(obsSnr1, obsSnr2);
                    double f1q      = object[NAVMAP_L1] * object[NAVMAP_L1];
                    double f2q      = object[NAVMAP_L2] * object[NAVMAP_L2];
                    double range    = (obsP1 * f1q - obsP2 * f2q) / (f1q - f2q);
                    double ionl     = obsL1 - obsL2;
                    double ionp     = obsP2 - obsP1;
                    double mp1      = obsP1 - obsL1 + 2.0 * ionl * f2q / (f2q - f1q);
                    double mp2      = obsP2 - obsL2 + 2.0 * ionl * f1q / (f2q - f1q);
                    
                    ok = aerv(subject, object, aerv);
                    elv = aerv[1] * 180.0 / Math.PI;

//                        if (ok) {
//                            ok = elv > 15.0;
//                        }
//                        else {
//                            ok = true;
//                        } 

                    if (range != 0.0 && snr > 30.0 && elv > 15.0) {
                        
                        //Sagnac
                        double theta = -(range + object[NAVMAP_T]) * GiModel.WE / GiModel.CVEL;
                        double x = object[NAVMAP_X] * Math.cos(theta) - object[NAVMAP_Y] * Math.sin(theta);
                        double y = object[NAVMAP_X] * Math.sin(theta) + object[NAVMAP_Y] * Math.cos(theta);
                        double z = object[NAVMAP_Z];
                        double gr = Math.sqrt(
                                (subject[0] - x) * (subject[0] - x) + 
                                (subject[1] - y) * (subject[1] - y) + 
                                (subject[2] - z) * (subject[2] - z)
                        );
                        double dr = (
                                (subject[0] - x) * object[NAVMAP_VX] + 
                                (subject[1] - y) * object[NAVMAP_VY] + 
                                (subject[2] - z) * object[NAVMAP_VZ]) / GiModel.CVEL;

                        double deltaValues[] = new double[DELTA_WIDTH];
                        deltaValues[DELTA_DX] = (subject[0] - x) / gr;
                        deltaValues[DELTA_DY] = (subject[1] - y) / gr;
                        deltaValues[DELTA_DZ] = (subject[2] - z) / gr;
                        deltaValues[DELTA_DT] = 1.0;
                        deltaValues[DELTA_DR] = range + object[NAVMAP_T] - gr - dr;
                        deltaValues[DELTA_MP1] = mp1;
                        deltaValues[DELTA_MP2] = mp2;
                        deltaValues[DELTA_IONP] = ionp;
                        deltaValues[DELTA_IONL] = ionl;
                        deltaRecord.put(ea.getKey(), deltaValues);
                    }
                }
            }
        }
    }
    
    public boolean aerv(double[] subject, double[] object, double[] aerv) {
        double[][] m = new double[3][3];
        double[] d = new double[3];
        double[] dlt = new double[Math.min(Math.min(subject.length, object.length), aerv.length)];
        double p = Math.sqrt(subject[0] * subject[0] + subject[1] * subject[1]);
	double r = Math.sqrt(
                subject[0] * subject[0] + 
                subject[1] * subject[1] + 
                subject[2] * subject[2]
        );
        
        for (int i = 0; i < dlt.length; i++) {
            dlt[i] = object[i] - subject[i];
        }
        
	aerv[2] = Math.sqrt(
                dlt[0] * dlt[0] + 
                dlt[1] * dlt[1] + 
                dlt[2] * dlt[2]
        );

	if (p == 0.0) {
            return false;
        }
                
	m[0][0] = -subject[1] / p;
	m[0][1] = subject[0] / p;
	m[0][2] = 0.0;
	m[1][0] = -(subject[0] * subject[2] / (p * r));
	m[1][1] = -(subject[1] * subject[2] / (p * r));
	m[1][2] = p / r;
	m[2][0] = subject[0] / r;
	m[2][1] = subject[1] / r;
	m[2][2] = subject[2] / r;

	for (int j = 0; j < 3; j++) {
            for (int i = 0; i < 3; i++) {
                d[j] += dlt[i] * m[j][i];
            }
        }

	double s = d[2] / Math.sqrt(d[0] * d[0] + d[1] * d[1] + d[2] * d[2]);

        aerv[1] = (s == 1.0) ? 0.5 * Math.PI : Math.atan(s / Math.sqrt(1.0 - s * s));
        
	if (d[1] == 0.0) {
            aerv[0] = (d[0] > 0.0) ? 0.5 * Math.PI : 1.5 * Math.PI;
            return true;
	}
        
	aerv[0] = Math.atan(d[0] / d[1]);
        
	if (d[1] < 0.0) {
            aerv[0] += Math.PI;
        }
	else {
            if (d[0] < 0.0) {
                aerv[0] += 2.0 * Math.PI;
            }
        }
        
        if (dlt.length >= 6) {
            aerv[3] = (
                    dlt[0] * dlt[3] + 
                    dlt[1] * dlt[4] + 
                    dlt[2] * dlt[5]) / aerv[2];
        }
        
        return true;
    }
    
    private void dropDeltaOffsets() {
//                tm.getKey(),
//                entry.getKey().longValue(),
        delta.entrySet().forEach((tm) -> {
            double smp1 = 0.0;
            double smp2 = 0.0;
            int cmp1 = 0;
            int cmp2 = 0;
            double ionl;
            double t;
            
            ArrayList<Double> mean = new ArrayList();
            
            if (tm.getValue().firstEntry() != null) {
                ionl = tm.getValue().firstEntry().getValue()[DELTA_IONL];
                t = tm.getValue().firstEntry().getKey();
            }
            else {
                ionl = 0.0;
                t = 0.0;
            }
            
            for (Map.Entry<Double, double[]> entry : tm.getValue().entrySet()) {
                if (Math.abs(entry.getValue()[DELTA_IONL] - ionl) > 0.01 || entry.getKey() - t > 1.0) {
                    mean.add(smp1 / (double)cmp1);
                    smp1 = entry.getValue()[DELTA_MP1];
                    cmp1 = 1;
                }
                else {
                    smp1 += entry.getValue()[DELTA_MP1];
                    cmp1++;
                }
                ionl = entry.getValue()[DELTA_IONL];
                t = entry.getKey();
            }

            cmp1 = 0;
            if (tm.getValue().firstEntry() != null) {
                ionl = tm.getValue().firstEntry().getValue()[DELTA_IONL];
                t = tm.getValue().firstEntry().getKey();
            }
            else {
                ionl = 0.0;
                t = 0.0;
            }
            for (Map.Entry<Double, double[]> entry : tm.getValue().entrySet()) {
                if (cmp1 >= mean.size()) {
                    break;
                }
                entry.getValue()[DELTA_MP1] -= mean.get(cmp1);
                if (Math.abs(entry.getValue()[DELTA_IONL] - ionl) > 0.01 || entry.getKey() - t > 1.0) {
                    cmp1++;
                }
                ionl = entry.getValue()[DELTA_IONL];
                t = entry.getKey();
            }
        });
    }
    
    public void saveDelta(String fileName) {
//        dropDeltaOffsets();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, false))) {
            for (HashMap.Entry<Integer, TreeMap<Double, double[]>> tm : delta.entrySet()) {
                for (Map.Entry<Double, double[]> entry : tm.getValue().entrySet()) {
                    
                    String line = String.format(Locale.ROOT, "%d\t%d\t%.12e\t%.12e\t%.12e\t%.12e\t%.12e\n", 
                            tm.getKey(), 
                            entry.getKey().longValue(), 
                            entry.getValue()[DELTA_DR], 
                            entry.getValue()[DELTA_MP1], 
                            entry.getValue()[DELTA_MP2],
                            entry.getValue()[DELTA_IONP], 
                            entry.getValue()[DELTA_IONL]
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
        value = delta.entrySet().stream().map((tm) -> 
                tm.getValue().size()).reduce(value, Integer::sum);
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
