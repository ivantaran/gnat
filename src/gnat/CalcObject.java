
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
    private final static int DELTA_WIDTH = 11;
    private final static int DELTA_DX   = 0;
    private final static int DELTA_DY   = 1;
    private final static int DELTA_DZ   = 2;
    private final static int DELTA_DT   = 3;
    private final static int DELTA_DR   = 4;
    private final static int DELTA_MP1  = 5;
    private final static int DELTA_MP2  = 6;
    private final static int DELTA_IONP = 7;
    private final static int DELTA_IONL = 8;
    private final static int DELTA_AZM  = 9;
    private final static int DELTA_ELV  = 10;
    
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
    
    private long stepTime = 1000; //0.125;
    private long startTime = -900000;
    private long endTime = 900000;
    private double minSnr = 45.0;
    private double minElevation = Math.toRadians(10.0);
    private String singleMode = "";
    
    private final GiModel model = new GiModel();
    private final double position[] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    private final double m_latLonAlt[] = {0.0, 0.0, 0.0};
    private final HashMap<Integer, TreeMap<Long, double[]>> navMap = new HashMap();
    private final HashMap<Integer, TreeMap<Long, double[]>> delta  = new HashMap();
    private final HashMap<String, ObserveObject> obsMap = new HashMap();
    
    CalcObject() {
        
    }
    
    public final void setPosition(double[] p, int len) {  // TODO: remove len
        System.arraycopy(p, 0, position, 0, 
                Math.min(p.length, position.length));
        NavUtils.ecefToLatLonAlt(p, m_latLonAlt);
    }

    /**
     * @return the singleMode
     */
    public String getSingleMode() {
        return singleMode;
    }

    /**
     * @param singleMode the singleMode to set
     */
    public void setSingleMode(String singleMode) {
        this.singleMode = singleMode;
    }

    /**
     * @return the minSnr
     */
    public double getMinSnr() {
        return minSnr;
    }

    /**
     * @param minSnr the minSnr to set
     */
    public void setMinSnr(double minSnr) {
        this.minSnr = minSnr;
    }

    /**
     * @return the minElevation
     */
    public double getMinElevation() {
        return minElevation;
    }

    /**
     * @param minElevation the minElevation to set
     */
    public void setMinElevation(double minElevation) {
        this.minElevation = minElevation;
    }

    /**
     * @return the stepTime
     */
    public long getStepTime() {
        return stepTime;
    }

    /**
     * @param stepTime the stepTime to set
     */
    public void setStepTime(long stepTime) {
        this.stepTime = stepTime;
    }
        
    public void setPositionXyz(double[] xyz) {
        position[0] = xyz[0];
        position[1] = xyz[1];
        position[2] = xyz[2];
    }
    
    public final double getLatitude() {
        return m_latLonAlt[0];
    }
    
    public final double getLongitude() {
        return m_latLonAlt[1];
    }
    
    public final double getAltitude() {
        return m_latLonAlt[2];
    }
    
    public final double[] getPosition() {
        return position;
    }
    
    public void addGlonassNavDataList(ArrayList<GlonassNavData> navDataList) {
        this.navDataList.addAll(navDataList);
        updateNavData(position);
    }
    
    private GlonassSet init(GlonassNavData navData) {
        GlonassSet gset = new GlonassSet();
        double[] initial = new double[GlonassSet.InitialLength];
        long startTimeAbs;
        
        if (stepTime < 1) {
            System.out.println(String.format("invalid step time: %d", 
                    stepTime));
            stepTime = 1;
        }
        
        if (startTime > endTime) {
            startTime = 0;
            endTime = 0;
            System.out.println(String.format("startTime > endTime: %d > %d", 
                    startTime, endTime));
        }
        
        if ((endTime - startTime) / stepTime - 1 > MAX_POINTS) {
            endTime = startTime + stepTime * MAX_POINTS;
            System.out.println("overflow points");
        }
        
        System.arraycopy(navData.getState(), 0, initial, 0, 
                navData.getState().length);
        System.arraycopy(navData.getAcceleration(), 0, initial, 
                navData.getState().length, navData.getAcceleration().length);
        
        gset.setInitial(initial);
        gset.setCurrent(initial.clone());
        
        if (startTime < 0) {
            gset.setStepTimeInMillis(-stepTime);
        }
        else {
            gset.setStepTimeInMillis( stepTime);
        }
        
        startTimeAbs = Math.abs(startTime);
        
        for (long i = 0; i < startTimeAbs; i += stepTime) {
            model.step(gset);
        }
        
//        gset.setStepTime(-navData.getTimeOffset());
//        System.out.println(navData.getTimeOffset());
//        gset.setStepTime(1.4560); //TODO correct rotation -0.07101
//        gset.setStepTime(1010000.0 / GiModel.CVEL); //TODO correct rotation -0.07101
//        gset.setStepTime(0.003); //TODO correct rotation -0.07101
//        model.step(gset);
        
        gset.setStepTimeInMillis(stepTime);
        
        return gset;
    }
    
    private void updateNavData(double[] subject) {
        
        navDataList.forEach((navData) -> {
            GlonassSet gset = init(navData);
            long toe     = navData.getTime().getTimeInMillis();
            long current = toe + startTime;
            TreeMap<Long, double[]> tm = navMap.get(navData.getNumber());

            if (tm == null) {
                tm = new TreeMap();
                navMap.put(navData.getNumber(), tm);
            }

            for (long i = startTime; i < endTime; i += stepTime) {
                tm.put(current, getMeasureArray(navData, current - toe, subject, gset.getCurrent()));
                model.step(gset);
                current += stepTime;
            }
        });        
    }
    
    private double[] getMeasureArray(GlonassNavData navData, long deltaMillis, double[] subject, double[] object) {
        double result[] = new double[NAVMAP_WIDTH];
        System.arraycopy(object, 0, result, 0, GlonassSet.VectorLength);
        double glotime = 0.0; //TODO read from obs file
        result[NAVMAP_T] = (glotime + navData.getTimeOffset() 
                + navData.getFrequencyOffset() * (double)deltaMillis * 0.001) 
                * GiModel.CVEL - subject[3];
        result[NAVMAP_L1] = navData.getFrequencyL1() * navData.getFrequencyOffset() + navData.getFrequencyL1();
        result[NAVMAP_L2] = navData.getFrequencyL2() * navData.getFrequencyOffset() + navData.getFrequencyL2();
        result[NAVMAP_L3] = navData.getFrequencyL3() * navData.getFrequencyOffset() + navData.getFrequencyL3();
        return result;
    }
    
    public void save(String fileName) {
        String line;

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, false));
            for (HashMap.Entry<Integer, TreeMap<Long, double[]>> tm : navMap.entrySet()) {
                for (Map.Entry<Long, double[]> entry : tm.getValue().entrySet()) {
                    double ds[] = entry.getValue();
                    line = String.format("%d\t%d\t", tm.getKey(), 
                            entry.getKey());
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
        boolean ok;
        
        delta.clear();
        
        for (HashMap.Entry<String, ObserveObject> obsObject : obsMap.entrySet()) {
            if (obsObject.getKey().charAt(0) != 'R') {
                continue;
            }
            
            try {
                objectName = Integer.parseInt(obsObject.getKey().replaceAll("^\\D", "").trim());
//                if (objectName == 19 || objectName == 8) continue;
            } catch (NumberFormatException e) {
                System.out.println(e.getMessage());
                System.out.println(obsObject.getKey());
                continue;
            }
            
            TreeMap<Long, HashMap<String, Double>> a = obsObject.getValue().getData();
            TreeMap<Long, double[]> b = navMap.get(objectName);
            if (a != null && b != null) {
                
                TreeMap<Long, double[]> deltaRecord = delta.get(objectName);
                if (deltaRecord == null) {
                    deltaRecord = new TreeMap();
                    delta.put(objectName, deltaRecord);
                }

                for (Map.Entry<Long, HashMap<String, Double>> ea : a.entrySet()) {
                    double object[] = b.get(ea.getKey());
                    
                    if (object == null) {
                        continue;
                    }
                    
                    double range, snr, ionl, ionp, mp1, mp2;
                    
                    if (singleMode.isEmpty()) {
                        double obsP1    = ea.getValue().getOrDefault("C1P", 0.0);
                        double obsL1    = ea.getValue().getOrDefault("L1P", 0.0) * GiModel.CVEL / object[NAVMAP_L1];
                        double obsSnr1  = obsP1 > 0.0 ? ea.getValue().getOrDefault("S1C", 0.0) : 0.0;
                        double obsP2    = ea.getValue().getOrDefault("C2P", 0.0);
                        double obsL2    = ea.getValue().getOrDefault("L2P", 0.0) * GiModel.CVEL / object[NAVMAP_L2];
                        double obsSnr2  = obsP2 > 0.0 ? ea.getValue().getOrDefault("S2C", 0.0) : 0.0;
                        double f1q      = object[NAVMAP_L1] * object[NAVMAP_L1];
                        double f2q      = object[NAVMAP_L2] * object[NAVMAP_L2];
                        
                        snr   = Math.min(obsSnr1, obsSnr2);
                        range = (obsP1 * f1q - obsP2 * f2q) / (f1q - f2q);
                        ionl  = obsL1 - obsL2;
                        ionp  = obsP2 - obsP1;
                        mp1   = obsP1 - obsL1 + 2.0 * ionl * f2q / (f2q - f1q);
                        mp2   = obsP2 - obsL2 + 2.0 * ionl * f1q / (f2q - f1q);
                    }
                    else {
                        range    = ea.getValue().getOrDefault(singleMode, 0.0);
                        String snrType = 'S' + singleMode.substring(1, singleMode.length());
                        snr  = range > 0.0 ? ea.getValue().getOrDefault(snrType, 0.0) : 0.0;
                        ionl = 0.0; 
                        ionp = 0.0; 
                        mp1 = 0.0; 
                        mp2 = 0.0;
                    }
                    
                    
//                    double obsP1    = ea.getValue().getOrDefault("P1", 0.0);
//                    double obsL1    = ea.getValue().getOrDefault("L1", 0.0) * GiModel.CVEL / object[NAVMAP_L1];
//                    double obsSnr1  = ea.getValue().getOrDefault("S1", 0.0);
//                    double obsP2    = ea.getValue().getOrDefault("P2", 0.0);
//                    double obsL2    = ea.getValue().getOrDefault("L2", 0.0) * GiModel.CVEL / object[NAVMAP_L2];
//                    double obsSnr2  = ea.getValue().getOrDefault("S2", 0.0);

                    
//                    if (obsP1 != 0.0) {
//                        obsP1 += -250.0;
//                    }
//                    
//                    if (obsP2 != 0.0) {
//                        obsP2 += -270.606;
//                    }
                    
//                    double snr      = obsSnr1;
//                    double range    = obsP1;

                    
                    ok = aerv(subject, object, aerv);

                    if (range != 0.0 && snr >= minSnr && aerv[1] >= minElevation) {
                        
                        double tropo = TroposphericDelay.getRangeCorrection(
                                getLatitude(), 
                                getAltitude(), 
                                aerv[1], 
                                ea.getKey());
                        range -= tropo;

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
                        deltaValues[DELTA_AZM] = aerv[0];
                        deltaValues[DELTA_ELV] = aerv[1];
                        deltaRecord.put(ea.getKey(), deltaValues);
                    }
                }
            }
        }
    }
    
    /**
     * TODO move this method to NavUtils
     * @param subject
     * @param object
     * @param aerv
     * @return 
     */
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
        delta.entrySet().forEach((tm) -> {
            double smp1 = 0.0;
            double smp2 = 0.0;
            int c = 0;
            double ionl;
            double level = 0.005;
            
            ArrayList<Double> mean1 = new ArrayList();
            ArrayList<Double> mean2 = new ArrayList();
            
            if (tm.getValue().firstEntry() != null) {
                ionl = tm.getValue().firstEntry().getValue()[DELTA_IONL];
            }
            else {
                ionl = 0.0;
            }
            
            for (Map.Entry<Long, double[]> entry : tm.getValue().entrySet()) {
                if (Math.abs(entry.getValue()[DELTA_IONL] - ionl) > level) {
                    mean1.add(smp1 / (double)c);
                    mean2.add(smp2 / (double)c);
                    smp1 = entry.getValue()[DELTA_MP1];
                    smp2 = entry.getValue()[DELTA_MP2];
                    c = 1;
                }
                else {
                    smp1 += entry.getValue()[DELTA_MP1];
                    smp2 += entry.getValue()[DELTA_MP2];
                    c++;
                }
                ionl = entry.getValue()[DELTA_IONL];
            }
            
            mean1.add(smp1 / (double)c);
            mean2.add(smp2 / (double)c);

            c = 0;
            if (tm.getValue().firstEntry() != null) {
                ionl = tm.getValue().firstEntry().getValue()[DELTA_IONL];
            }
            else {
                ionl = 0.0;
            }
            for (Map.Entry<Long, double[]> entry : tm.getValue().entrySet()) {
                if (c < mean1.size() && c < mean2.size()) {
                    entry.getValue()[DELTA_MP1] -= mean1.get(c);
                    entry.getValue()[DELTA_MP2] -= mean2.get(c);
                }
                else {
                    System.out.printf(Locale.ROOT, "error: dropDeltaOffsets");
                    break;
                }
                if (Math.abs(entry.getValue()[DELTA_IONL] - ionl) > level) {
                    c++;
                }
                ionl = entry.getValue()[DELTA_IONL];
            }
        });
    }
    
    public void saveDelta(String fileName) {
        dropDeltaOffsets();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, false))) {
            for (HashMap.Entry<Integer, TreeMap<Long, double[]>> tm : delta.entrySet()) {
                for (Map.Entry<Long, double[]> entry : tm.getValue().entrySet()) {
                    
                    String line = String.format(Locale.ROOT, 
                            "%d\t%d\t%.12e\t%.12e\t%.12e\t%.12e\t%.12e\t%.12e\t%.12e\n", 
                            tm.getKey(), 
                            entry.getKey(), 
                            entry.getValue()[DELTA_DR], 
                            entry.getValue()[DELTA_MP1], 
                            entry.getValue()[DELTA_MP2],
                            entry.getValue()[DELTA_IONP], 
                            entry.getValue()[DELTA_IONL], 
                            entry.getValue()[DELTA_AZM], 
                            entry.getValue()[DELTA_ELV]
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
        
        for (HashMap.Entry<Integer, TreeMap<Long, double[]>> tm : delta.entrySet()) {
            for (Map.Entry<Long, double[]> entry : tm.getValue().entrySet()) {
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
        for (HashMap.Entry<Integer, TreeMap<Long, double[]>> tm : this.delta.entrySet()) {
            for (Map.Entry<Long, double[]> entry : tm.getValue().entrySet()) {
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
