/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rinex;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

/**
 *
 * @author Taran
 */
public class GlonassNavDataReader {
    private ArrayList<String> headLines;
    private ArrayList<String> dataLines;
    private HashMap<String, ObserveObject> objectList = new HashMap();
    private Calendar baseDate = new GregorianCalendar();
    private double baseFracSecond = 0;
    private int lineIndex = 0;
    private ArrayList<ObserveSample> observe = new ArrayList();
    private int observeTypeCount = 0;
    private String[] typesObservations;
    
    GlonassNavDataReader(ArrayList<String> headLines, ArrayList<String> dataLines) {
        this.headLines = headLines;
        this.dataLines = dataLines;
//        parse();
    }
    
}
