/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rinex;

import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 *
 * @author Taran
 */
public class GlonassNavDataReader {
    private ArrayList<String> headLines;
    private ArrayList<String> dataLines;
    private int lineIndex = 0;
    protected ArrayList<GlonassNavData> navDataList = new ArrayList();
    
    GlonassNavDataReader(ArrayList<String> headLines, ArrayList<String> dataLines) {
        add(headLines, dataLines);
    }
    
    public final void add(ArrayList<String> headLines, ArrayList<String> dataLines) {
        this.headLines = headLines;
        this.dataLines = dataLines;
        parse();
    }
    
    private GregorianCalendar getTime(String line) {
        GregorianCalendar c = new GregorianCalendar();
        try {
            int year = Integer.parseInt(line.substring(0, 2).trim());
            year = (year < 80) ? year + 2000 : 1900;
            int month = Integer.parseInt(line.substring(3, 5).trim());
            int day = Integer.parseInt(line.substring(6, 8).trim());
            int hour = Integer.parseInt(line.substring(9, 11).trim());
            int minute = Integer.parseInt(line.substring(12, 14).trim());
            double fracSecond = Double.parseDouble(line.substring(14, 19).trim());
            int second = (int)fracSecond;
            fracSecond -= second;
            c.setTimeInMillis(0);
            c.set(year, month, day, hour, minute, second);
            c.setTimeInMillis(c.getTimeInMillis() + (long)(fracSecond * 1000));
//            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
//            sdf.setCalendar(c);
//            warning(sdf.format(c.getTimeInMillis()));
        }
        catch (NumberFormatException e) {
            warning(String.format("NumberFormatException at header line %d", lineIndex));
            warning(line);
            warning(e.getMessage());
        }
        return c;
    }
    
    private void parseNavData() {
        String line;
        double value;
        double[] state = new double[6];
        double[] acceleration = new double[3];
        GlonassNavData gnd = new GlonassNavData();
        
        /* line 1 */
        line = getLine();
        line = line.replace('D', 'E');
        String ss = line.substring(0, 2);
        gnd.setNumber(Integer.parseInt(line.substring(0, 2).trim()));
        gnd.setTime(getTime(line.substring(3, line.length())));
        gnd.setTimeOffset(Double.parseDouble(line.substring(22, 41).trim()));
        gnd.setFrequencyOffset(Double.parseDouble(line.substring(41, 60).trim()));
        gnd.setMessageTime(Double.parseDouble(line.substring(60, 79).trim()));
        
        /* line 2 */
        line = getLine();
        line = line.replace('D', 'E');
        state[0] = Double.parseDouble(line.substring(3, 22).trim());
        state[3] = Double.parseDouble(line.substring(22, 41).trim());
        acceleration[0] = Double.parseDouble(line.substring(41, 60).trim());
        value = Double.parseDouble(line.substring(60, 79).trim());
        gnd.setSuitability(value == 0);
        
        /* line 3 */
        line = getLine();
        line = line.replace('D', 'E');
        state[1] = Double.parseDouble(line.substring(3, 22).trim());
        state[4] = Double.parseDouble(line.substring(22, 41).trim());
        acceleration[1] = Double.parseDouble(line.substring(41, 60).trim());
        value = Double.parseDouble(line.substring(60, 79).trim());
        gnd.setFrequencyChannelNumber((int)value);
        
        /* line 4 */
        line = getLine();
        line = line.replace('D', 'E');
        state[2] = Double.parseDouble(line.substring(3, 22).trim());
        state[5] = Double.parseDouble(line.substring(22, 41).trim());
        acceleration[2] = Double.parseDouble(line.substring(41, 60).trim());
        gnd.setAge(Double.parseDouble(line.substring(60, 79).trim()));
        /*        */
        
        for (int i = 0; i < state.length; i++) {
            state[i] *= 1000;
        }
        
        for (int i = 0; i < acceleration.length; i++) {
            acceleration[i] *= 1000;
        }
        
        gnd.setState(state);
        gnd.setAcceleration(acceleration);
        
        navDataList.add(gnd);
    }
    
    private void parse() {
        lineIndex = 0;
        while (linesReady()) {
            parseNavData();
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
    
    private boolean linesReady() {
        return (lineIndex < dataLines.size());
    }

    private void warning(String message) {
        System.out.println(message);
    }

    public void save() {
        for (GlonassNavData gnd : navDataList) {
            gnd.save(String.valueOf(gnd.getNumber()) + ".gnd", true);
        }
    }

    /**
     * @return the navDataList
     */
    public ArrayList<GlonassNavData> getNavDataList() {
        return navDataList;
    }

}

