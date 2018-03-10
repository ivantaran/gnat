
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
        int flag;
        
        getObserve().clear();
        
        String line = getLine();
        flag = getFlag(line);
        
        if (flag == FLAG_OK) {
            time = getDeltaTime(line);
            count = getObjectCount(line);
        }
        else {
            if (flag < 0) {
                warning(String.format("error at line %d", getLineIndex()));
                warning(line);
                System.exit(-1);
            }
            if (flag == FLAG_HEADER || flag == FLAG_EXTEVENT) {
                warning(String.format("flag at line: %d", getLineIndex()));
                warning(line);
                int c = Integer.parseInt(line.substring(29, 32).trim());
                while (c > 0) {
                    getLine();
                    c--;
                }
            }
            else {
                warning(String.format("flag at line: %d", getLineIndex()));
                warning(line);
            }
            //TODO write all flags
        }
    }
    
    @Override
    protected void readObservations() {
        ObserveObject object;
        HeaderReader hr;
        int valuePosition;
        String lineValue, line, name, types[];
        double value, data[];
        int lli, ps; // TODO use lli and ps
        
        for (int c = 0; c < count; c++) {
            line = getLine();
            name = line.substring(0, 3).trim();
            hr = getHeaderReader();
            types = hr.getSysObsTypes().get(name.charAt(0));

            if (types == null || types.length < 1) {
                continue;
            }

            data = new double[types.length];
            
            try {
                for (int i = 0; i < data.length; i++) {
                    valuePosition = 3 + 16 * i;
                    lineValue = line.substring(valuePosition, valuePosition + 14).trim();
                    value = lineValue.isEmpty() ? 0.0 : Double.parseDouble(lineValue);

                    valuePosition += 14;
                    lineValue = line.substring(valuePosition, valuePosition + 1).trim();
                    lli = lineValue.isEmpty() ? 0 : Integer.parseInt(lineValue);

                    valuePosition += 1;
                    lineValue = line.substring(valuePosition, valuePosition + 1).trim();
                    ps = (lineValue.isEmpty()) ? 0 : Integer.parseInt(lineValue);
                    data[i] = value;
                }
            }
            catch (IndexOutOfBoundsException | NumberFormatException ex) {
                warning(String.format("error at line: %d", getLineIndex()));
                warning(line);
                warning(ex.getMessage());
            }

            if (getObjectMap().containsKey(name)) {
                object = getObjectMap().get(name);
            }
            else {
                object = new ObserveObject(name);
                getObjectMap().put(name, object);
            }

            object.putObsData(time, types, data);
        }
    }
    
    @Override
    protected void addObservations() {
        
    }    

    /**
     * @return the approxPositionXyz
     */
    @Override
    public double[] getApproxPositionXyz() {
        return getHeaderReader().getApproxPositionXyz();
    }
    
}