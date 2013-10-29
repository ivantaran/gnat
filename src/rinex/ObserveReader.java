/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rinex;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Ivan
 */
public class ObserveReader {
    private ArrayList<String> headLines;
    private ArrayList<String> dataLines;
    private HashMap<String, ObserveObject> objectList = new HashMap();
    private Calendar baseDate = new GregorianCalendar();
    private double baseFracSecond = 0;
    private int lineIndex = 0;
    private ArrayList<ObserveData> observe = new ArrayList();
    private int observeTypeCount = 0;
    
    ObserveReader(ArrayList<String> headLines, ArrayList<String> dataLines) {
        this.headLines = headLines;
        this.dataLines = dataLines;
        parse();
    }
    
    private double getDeltaTime(String line) {
        double result = -1;
        
        if (line.length() > 25) {
            try {
                int year = Integer.parseInt(line.substring(1, 3).trim()) + 
                        (baseDate.get(Calendar.YEAR)/100)*100;
                int month = Integer.parseInt(line.substring(4, 6).trim());
                int day = Integer.parseInt(line.substring(7, 9).trim());
                int hour = Integer.parseInt(line.substring(10, 12).trim());
                int minute = Integer.parseInt(line.substring(13, 15).trim());
                double fsecond = Double.parseDouble(line.substring(15, 26).trim());
                int second = (int)fsecond;
                fsecond -= second;
                Calendar date = new GregorianCalendar(year, month, day, hour, minute, second);
                result = date.getTimeInMillis()/1000 - baseDate.getTimeInMillis()/1000 + 
                fsecond - baseFracSecond;
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
        Double [] data = new Double[observeTypeCount + 1];
        data[0] = time;
        observe.add(new ObserveData(name, data));
    }
    
    private void addObserves() {
        ObserveObject object;
        
        for (Iterator<ObserveData> it = observe.iterator(); it.hasNext();) {
            ObserveData observeData = it.next();
            String name = observeData.getName();
            Double [] data = observeData.getData();

            if (objectList.containsKey(name)) {
                object = objectList.get(name);
            }
            else {
                object = new ObserveObject();
                objectList.put(name, object);
            }
            
            object.getData().add(data);
        }
        
    }
    
    private void getObservesHeader() {
        double time = 0;
        int flag = 0;
        int count = 0;
        int objectLineCount = 0;
        int indexObject = 0;
        
        observe.clear();
        
        String line = getLine();
        time = getDeltaTime(line);
        flag = getFlag(line);
        count = getObjectCount(line);
        objectLineCount = (count - 1)/12 + 1;

        for (int j = 0; j < objectLineCount; ++j) {
            for (int i = 0; (i < 12) && (indexObject < count); ++i, ++indexObject) {
                int objectNamePosition = 32 + i*3;
                if (objectNamePosition + 1 < line.length()) {
                    String name = line.substring(objectNamePosition, objectNamePosition + 3);
                    newObserve(name, time);
                    warning(name);
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
    
    private void getObserves() {
        int observeLineCount = (observeTypeCount - 1)/5 + 1;
        int indexObserve;
        double value;
        
        for (Iterator<ObserveData> it = observe.iterator(); it.hasNext();) {
            ObserveData data = it.next();
            indexObserve = 0;
            for (int j = 0; j < observeLineCount; ++j) {
                String line = getLine();
                for (int i = 0; (i < 5) && (indexObserve < observeTypeCount); ++i, ++indexObserve) {
                    int valuePosition = 16*i;
                    if (valuePosition + 12 < line.length()) {
                        String lineValue = line.substring(valuePosition, valuePosition + 14).trim();
                        value = (lineValue.isEmpty()) ? 0 : Double.valueOf(lineValue);
                    }
                    else {
                        value = 0;
                    }
                    data.getData()[indexObserve + 1] = value;
                }
            }
        }
    }
    
    private boolean parseHeader() {
        boolean result = true;
        int index;
        String line;

        index = RinexReader.getMarkerIndex(RinexReader.MarkerTypesOfObserv, headLines);
        if (index > -1) {
            line = headLines.get(index);
            try {
                observeTypeCount = Integer.parseInt(line.substring(0, 7).trim());
                result = (observeTypeCount > 0);
            } catch (NumberFormatException e) {
                warning(String.format("NumberFormatException at header line %d", index));
                warning(line);
                warning(e.getMessage());
                result = false;
            }
        }
        else {
            warning("MarkerTypesOfObserv not found");
            result = false;
        }
        
        if (result) {
            index = RinexReader.getMarkerIndex(RinexReader.MarkerTimeOfFirstObs, headLines);
            if (index > -1) {
                line = headLines.get(index);
                try {
                    int year = Integer.parseInt(line.substring(0, 6).trim());
                    int month = Integer.parseInt(line.substring(6, 12).trim());
                    int day = Integer.parseInt(line.substring(12, 18).trim());
                    int hour = Integer.parseInt(line.substring(18, 24).trim());
                    int minute = Integer.parseInt(line.substring(24, 30).trim());
                    baseFracSecond = Double.parseDouble(line.substring(30, 43).trim());
                    int second = (int)baseFracSecond;
                    baseFracSecond -= second;
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
                warning("MarkerTimeOfFirstObs not found");
                result = false;
            }
        }
        
        return result;
    }
    
    private void parse() {
        boolean result = parseHeader();
        if (result) {
            while (linesReady()) {
                getObservesHeader();
                getObserves();
                addObserves();
            }
            save();
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
        for (Map.Entry<String, ObserveObject> entry : objectList.entrySet()) {
            String string = entry.getKey();
            ObserveObject observeObject = entry.getValue();
            observeObject.save(string + ".txt");
        }
    }
}
