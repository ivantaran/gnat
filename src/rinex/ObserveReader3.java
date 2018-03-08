
package rinex;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *
 * @author taran
 */
public class ObserveReader3 extends ObserveReader {
    
    private double time = 0;
    private long count = 0;
    
    ObserveReader3(HeaderReader headerReader, ArrayList<String> headLines, ArrayList<String> dataLines) {
        super(headerReader, headLines, dataLines);
    }
    
    /**
     * @return the flag
     */
    private int getFlag(String line) {
        int result = -1;
        
        if (line.length() > 30  && line.charAt(0) == '>') {
            result = Integer.valueOf(line.substring(31, 32));
        }
        else {
            warning(String.format("Line index %d length = %d.", getLineIndex(), line.length()));
        }
        
        return result;
    }

    private double getDeltaTime(String line) {
        double result = -1;
        
        try {
            int year = Integer.parseInt(line.substring(2, 6).trim());
            int month = Integer.parseInt(line.substring(7, 9).trim()) - 1;
            int day = Integer.parseInt(line.substring(10, 12).trim());
            int hour = Integer.parseInt(line.substring(13, 15).trim());
            int minute = Integer.parseInt(line.substring(16, 18).trim());
            double fsecond = Double.parseDouble(line.substring(18, 29).trim());
            int second = (int)fsecond;
            fsecond -= second;
            Calendar date = new GregorianCalendar(year, month, day, hour, minute, second);
            result = date.getTimeInMillis() / 1000L + fsecond - (double)getHeaderReader().getLeapSeconds();
        }
        catch (NumberFormatException | IndexOutOfBoundsException e) {
            warning(String.format("Exception at line %d", getLineIndex()));
            warning(line);
            warning(e.getMessage());
        }
        
        return result;
    }

    private int getObjectCount(String line) {
        int result = 0;
        
        try {
            result = Integer.valueOf(line.substring(32, 35).trim());
        }
        catch (NumberFormatException ex) {
            warning(String.format("Error at line index %d", getLineIndex()));
            warning(ex.getMessage());
        }
        
        return result;
    }

    @Override
    protected boolean parseHeader() {
        return true;
    }
    
    @Override
    protected void readObservesHeader() {
//        int flag;
//        
//        getObserve().clear();
//        
//        String line = getLine();
//        flag = getFlag(line);
//        
//        if (flag == FLAG_OK) {
//            time = getDeltaTime(line);
//            count = getObjectCount(line);
//            
//            for (int i = 0; i < count; i++) {
//                line = getLine();
//                newObserve(name, time);
//            }
//        }
//        else {
//            if (flag < 0) {
//                warning(String.format("error at line %d", getLineIndex()));
//                warning(line);
//                System.exit(-1);
//            }
////            System.out.printf("flag: %d\n", flag);
//            //TODO write all flags
//        }
    }
    
    @Override
    protected void readObservations() {
//        int observeLineCount = (observeTypeCount - 1)/5 + 1;
//        int indexObserve;
//        int valuePosition;
//        String lineValue;
//        double value;
//        int lli, ps; // TODO use lli and ps
//
//        String line = getLine();
//        String name = line.substring(0, 3).trim();
//        HeaderReader hr = getHeaderReader();
//        String types[] = hr.getSysObsTypes().get(name.charAt(0));
//        
//        for (ObserveSample data : getObserve()) {
//            indexObserve = 0;
//            for (int j = 0; j < observeLineCount; ++j) {
//                String line = getLine();
//                for (int i = 0; (i < 5) && (indexObserve < observeTypeCount); ++i, ++indexObserve) {
//                    valuePosition = 16 * i;
//                    if (valuePosition + 14 <= line.length()) { //TODO check +14 value
//                        lineValue = line.substring(valuePosition, valuePosition + 14).trim();
//                        value = (lineValue.isEmpty()) ? 0.0 : Double.valueOf(lineValue);
//                    }
//                    else {
//                        value = 0.0;
//                    }
//
//                    valuePosition += 14;
//                    if (valuePosition + 1 <= line.length()) {
//                        lineValue = line.substring(valuePosition, valuePosition + 1).trim();
//                        lli = (lineValue.isEmpty()) ? 0 : Integer.valueOf(lineValue);
//                    }
//                    else {
//                        lli = 0;
//                    }
//                    
//                    valuePosition += 1;
//                    if (valuePosition + 1 <= line.length()) {
//                        lineValue = line.substring(valuePosition, valuePosition + 1).trim();
//                        ps = (lineValue.isEmpty()) ? 0 : Integer.valueOf(lineValue);
//                    }
//                    else {
//                        ps = 0;
//                    }
//                    
////                    if (lli != 0) {
////                        System.out.printf(Locale.ROOT, "lli[%.3f]: %d\n", value, lli);
////                    }
//                    
//                    data.getData()[indexObserve] = value;
//                }
//            }
//        }
    }
    
}
