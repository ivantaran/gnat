/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rinex;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

/**
 *
 * @author Ivan
 */
public class ObserveReader {

    public static final int FLAG_OK = 0;
    private ArrayList<String> headLines;
    private ArrayList<String> dataLines;
    private final HashMap<String, ObserveObject> objectMap = new HashMap();
    private Calendar baseDate = new GregorianCalendar();
    private double baseFracSecond = 0;
    private int lineIndex = 0;
    private final ArrayList<ObserveSample> observe = new ArrayList();
    private int observeTypeCount = 0;
    private String[] typesObservations;
    private int leapSeconds = 0;
    private final double[] approxPositionXyz = {0.0, 0.0, 0.0};
    private String markerName = "";
    
    ObserveReader(ArrayList<String> headLines, ArrayList<String> dataLines) {
        add(headLines, dataLines);
    }
    
    public final void add(ArrayList<String> headLines, ArrayList<String> dataLines) {
        this.headLines = headLines;
        this.dataLines = dataLines;
        parse();
    }
    
    private double getDeltaTime(String line) {
        double result = -1;
        
        if (line.length() > 25) {
            try {
                int year = Integer.parseInt(line.substring(1, 3).trim()) + 
                        (baseDate.get(Calendar.YEAR) / 100) * 100;
                int month = Integer.parseInt(line.substring(4, 6).trim()) - 1;
                int day = Integer.parseInt(line.substring(7, 9).trim());
                int hour = Integer.parseInt(line.substring(10, 12).trim());
                int minute = Integer.parseInt(line.substring(13, 15).trim());
                double fsecond = Double.parseDouble(line.substring(15, 26).trim());
                int second = (int)fsecond;
                fsecond -= second;
                Calendar date = new GregorianCalendar(year, month, day, hour, minute, second);
                result = date.getTimeInMillis() / 1000L + fsecond - (double)leapSeconds;
//                result = date.getTimeInMillis() / 1000 - baseDate.getTimeInMillis() / 1000 + 
//                fsecond - baseFracSecond;
            }
            catch (NumberFormatException e) {
                warning(String.format("NumberFormatException at line %d", lineIndex));
                warning(line);
                warning(e.getMessage());
            }
        }
        else {
            warning(String.format("Line index %d length = %d.", lineIndex, line.length()));
        }
        
        return result;
    }

    /**
     * @return the markerName
     */
    public String getMarkerName() {
        return markerName;
    }

    /**
     * @param markerName the markerName to set
     */
    private void setMarkerName(String markerName) {
        if (!this.markerName.equalsIgnoreCase(markerName)) {
            this.markerName = markerName;
            if (!objectMap.isEmpty()) {
                warning(markerName);
                warning("Marker name is changed. All observes will be dropped.");
                objectMap.clear();
            }
        }
    }

    
    /**
     * @return the approxPositionXyz
     */
    public double[] getApproxPositionXyz() {
        return approxPositionXyz;
    }
    
    /**
     * @return the flag
     */
    private int getFlag(String line) {
        int result = -1;
        
        if (line.length() > 27) {
            result = Integer.valueOf(line.substring(28, 29));
        }
        else {
            warning(String.format("Line index %d length = %d.", lineIndex, line.length()));
        }
        
        return result;
    }
    
    private int getObjectCount(String line) {
        int result = 0;
        
        if (line.length() > 30) {
            result = Integer.valueOf(line.substring(29, 32).trim());
        }
        else {
            warning(String.format("Line index %d length = %d.", lineIndex, line.length()));
        }
        
        return result;
    }
    
    private void newObserve(String name, double time) {
        double data[] = new double[observeTypeCount];
        observe.add(new ObserveSample(name, time, data));
    }
    
    private void addObservations() {
        ObserveObject object;
        
        for (ObserveSample observeData : observe) {
            String name = observeData.getName();

            if (getObjectMap().containsKey(name)) {
                object = getObjectMap().get(name);
            }
            else {
                object = new ObserveObject(name);
                getObjectMap().put(name, object);
            }
            
            object.putObsData(observeData.getTime(), typesObservations, observeData.getData());
        }
        
    }
    
    private void readObservesHeader() {
        double time;
        int flag;
        int count;
        int objectLineCount;
        int indexObject = 0;
        
        observe.clear();
        
        String line = getLine();
        flag = getFlag(line);
        
        if (flag == FLAG_OK) {
            time = getDeltaTime(line);
            count = getObjectCount(line);
            objectLineCount = (count - 1) / 12 + 1;
            
            for (int j = 0; j < objectLineCount; ++j) {
                for (int i = 0; (i < 12) && (indexObject < count); ++i, ++indexObject) {
                    int objectNamePosition = 32 + i * 3;
                    if (objectNamePosition + 1 < line.length()) {
                        String name = line.substring(objectNamePosition, objectNamePosition + 3);
                        newObserve(name, time);
    //                    warning(name);
                    }
                    else {
                        warning(String.format("error at line %d", lineIndex));
                        warning(line);
                        System.exit(-1);
                    }
                }
                if (j < objectLineCount - 1) {
                    line = getLine();
                }
            }
        }
        else {
//            System.out.printf("flag: %d\n", flag);
            //TODO write all flags
        }
    }
    
    private void readObservations() {
        int observeLineCount = (observeTypeCount - 1)/5 + 1;
        int indexObserve;
        int valuePosition;
        String lineValue;
        double value;
        int lli, ps; // TODO use lli and ps
        
        for (ObserveSample data : observe) {
            indexObserve = 0;
            for (int j = 0; j < observeLineCount; ++j) {
                String line = getLine();
                for (int i = 0; (i < 5) && (indexObserve < observeTypeCount); ++i, ++indexObserve) {
                    valuePosition = 16 * i;
                    if (valuePosition + 14 <= line.length()) { //TODO check +14 value
                        lineValue = line.substring(valuePosition, valuePosition + 14).trim();
                        value = (lineValue.isEmpty()) ? 0.0 : Double.valueOf(lineValue);
                    }
                    else {
                        value = 0.0;
                    }

                    valuePosition += 14;
                    if (valuePosition + 1 <= line.length()) {
                        lineValue = line.substring(valuePosition, valuePosition + 1).trim();
                        lli = (lineValue.isEmpty()) ? 0 : Integer.valueOf(lineValue);
                    }
                    else {
                        lli = 0;
                    }
                    
                    valuePosition += 1;
                    if (valuePosition + 1 <= line.length()) {
                        lineValue = line.substring(valuePosition, valuePosition + 1).trim();
                        ps = (lineValue.isEmpty()) ? 0 : Integer.valueOf(lineValue);
                    }
                    else {
                        ps = 0;
                    }
                    
//                    if (lli != 0) {
//                        System.out.printf(Locale.ROOT, "lli[%.3f]: %d\n", value, lli);
//                    }
                    
                    data.getData()[indexObserve] = value;
                }
            }
        }
    }
    
    private void parseTypesOfObserv(int index, int count) {// TODO make return result
        int typesLineCount = (count - 1) / 9 + 1;
        int indexType = 0;
        String line;
        
        typesObservations = new String[count];
        for (int j = 0; j < typesLineCount; ++j) {
            line = headLines.get(index + j);
            for (int i = 0; (i < 9) && (indexType < count); ++i, ++indexType) {
                int valuePosition = 6 + 6 * i + 4;
                String lineValue = line.substring(valuePosition, valuePosition + 2);
                typesObservations[indexType] = lineValue;
            }
        }
    }
    
    private boolean parseHeader() {
        boolean result = true;
        int index;
        String line;

        index = RinexReader.getMarkerIndex(RinexReader.MARKER_TYPES_OF_OBSERV, headLines);
        if (index > -1) {
            line = headLines.get(index);
            try {
                observeTypeCount = Integer.parseInt(line.substring(0, 6).trim());
                result = (observeTypeCount > 0);
                if (result) {
                    parseTypesOfObserv(index, observeTypeCount);
                }
            } catch (NumberFormatException e) {
                warning(String.format("NumberFormatException at header line %d", index));
                warning(line);
                warning(e.getMessage());
                result = false;
            }
        }
        else {
            warning("MARKER_TYPES_OF_OBSERV not found");
            result = false;
        }
        
        if (result) {
            index = RinexReader.getMarkerIndex(RinexReader.MARKER_TIME_OF_FIRST_OBS, headLines);
            if (index > -1) {
                line = headLines.get(index);
                try {
                    int year  = Integer.parseInt(line.substring( 0,  6).trim());
                    int month = Integer.parseInt(line.substring( 6, 12).trim());
                    int day   = Integer.parseInt(line.substring(12, 18).trim());
                    int hour  = Integer.parseInt(line.substring(18, 24).trim());
                    //check rinex manual
                    int minute = Integer.parseInt(line.substring(24, 30).trim());
                    baseFracSecond = Double.parseDouble(line.substring(30, 43).trim());
                    int second = (int)baseFracSecond;
                    baseFracSecond -= second;
                    //check time millis
                    baseDate = new GregorianCalendar(year, month, day, hour, minute, second);
                }
                catch (NumberFormatException e) {
                    warning(String.format("NumberFormatException at header line %d", index));
                    warning(line);
                    warning(e.getMessage());
                    result = false;
                }
            }
            else {
                warning("MARKER_TIME_OF_FIRST_OBS not found");
                result = false;
            }
            
            index = RinexReader.getMarkerIndex(RinexReader.MARKER_LEAP_SECONDS, headLines);
            if (index > -1) {
                line = headLines.get(index);
                try {
                    leapSeconds = Integer.parseInt(line.substring(0, 6).trim());
                }
                catch (NumberFormatException e) {
                    warning(String.format("NumberFormatException at header line %d", index));
                    warning(line);
                    warning(e.getMessage());
                    result = false;
                }
            }
            else {
                leapSeconds = 0;
            } 
        }
        
        if (result) {
            index = RinexReader.getMarkerIndex(RinexReader.MARKER_NAME, headLines);
            if (index > -1) {
                line = headLines.get(index);
                setMarkerName(line.substring(0, 60).trim());
            }
            else {
                warning("MARKER_NAME not found");
                result = false;
            }
        }
        
        if (result) {
            index = RinexReader.getMarkerIndex(RinexReader.MARKER_APPROX_POSITION_XYZ, headLines);
            if (index > -1) {
                line = headLines.get(index);
                try {
                    approxPositionXyz[0] = Double.parseDouble(line.substring( 0, 14));
                    approxPositionXyz[1] = Double.parseDouble(line.substring(14, 28));
                    approxPositionXyz[2] = Double.parseDouble(line.substring(28, 42));
                } catch (NumberFormatException e) {
                    warning(String.format("NumberFormatException at header line %d", index));
                    warning(line);
                    warning(e.getMessage());
                    result = false;
                }
            }
            else {
                warning("MARKER_APPROX_POSITION_XYZ not found");
                result = false;
            }
        }
        
        return result;
    }
    
    private void parse() {
        lineIndex = 0;
        boolean result = parseHeader();
        if (result) {
            while (linesReady()) {
                readObservesHeader();
                readObservations();
                addObservations();
            }
        }
        else {
            warning("Bad header");
        }
    }
    
    private String getLine() {
        String line = "";
        if (lineIndex < dataLines.size()) {
            line = dataLines.get(lineIndex);
            lineIndex++;
        }
        return line;
    }
    
    private void warning(String message) {
        System.out.println(message);
    }
    
    private boolean linesReady() {
        return (lineIndex < dataLines.size());
    }
    
    public void save() {
        getObjectMap().entrySet().forEach((entry) -> {
            String string = entry.getKey();
            ObserveObject observeObject = entry.getValue();
            observeObject.save(string + ".txt", true);
        });
    }

    /**
     * @return the objectMap
     */
    public HashMap<String, ObserveObject> getObjectMap() {
        return objectMap;
    }
}
