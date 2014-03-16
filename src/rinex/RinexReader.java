/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rinex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Ivan
 */
public class RinexReader {
    public static final int RinexVersion = 0;
    public static final int MinimumLineLength = 61;
    public static final int MarkerLineIndex = 60;

    public static final String MarkerRinexVersion = "RINEX VERSION / TYPE";
    public static final String MarkerTypesOfObserv = "# / TYPES OF OBSERV";
    public static final String MarkerTimeOfFirstObs =  "TIME OF FIRST OBS";
    public static final String MarkerEndOfHeader = "END OF HEADER";
    
    public GlonassNavDataReader gnd_tmp;
    private double [] version_list;
    private char [] type_list;
    private char [] system_list;
    private char type = ' ';
    private String errorMessasge = "";
    private String warningMessasge = "";

    private static enum ErrorCodes {Success, Line1Length, MarkerVersionType, Version, Type, System, Unknown};
    private ArrayList<String> headLines = new ArrayList();
    private ArrayList<String> dataLines = new ArrayList();
    private int lineIndex;
    private ErrorCodes errorCode;
    
    public RinexReader() {
        version_list = new double[4];
        version_list[0] = 2.00;
        version_list[1] = 2.01;
        version_list[2] = 2.10;
        version_list[3] = 2.11;
        
        type_list = new char[2];
        type_list[0] = 'O';
        type_list[1] = 'G';
        
        system_list = new char[6];
        system_list[0] = ' ';
        system_list[1] = 'G';
        system_list[2] = 'R';
        system_list[3] = 'E';
        system_list[4] = 'S';
        system_list[5] = 'M';
    }
    
    public static int getMarkerIndex(String marker, ArrayList<String> list) {
        boolean result = false;
        int index = -1;
        Iterator<String> it = list.iterator();
        
        while (it.hasNext() && !result) {
            String line = it.next();
            int pos = line.indexOf(marker, RinexReader.MarkerLineIndex);
            result = (pos > -1);
            index++;
        }
        
        if (!result) {
            index = -1;
        }
        
        return index;
    }
    
    /**
     * @return the Error Code
     */
    public ErrorCodes getErrorCode() {
        return errorCode;
    }
    
    private boolean checkMarkerRinexVersion(String line) {
        boolean result = (line.indexOf(MarkerRinexVersion, lineIndex) > -1);

        if (!result) {
            setError(ErrorCodes.MarkerVersionType);
        }
        
        return result;
    }
    
    private boolean checkRinexVersion(String line) {
        boolean result = false;
        
        double v = Double.valueOf(line.substring(0, 9).trim());
        for (int i = 0; i < version_list.length; ++i) {
            result |= (v == version_list[i]);
        }
        
        if (!result) {
            warningMessasge = String.format("Rinex Version: %1.2f", v);
            printWarning();
            setError(ErrorCodes.Version);
        }
        
        return result;
    }
    
    private boolean checkType(String line) {
        boolean result = false;
        
        type = line.charAt(20);
        for (int i = 0; i < type_list.length; ++i) {
            result |= (type == type_list[i]);
        }
        
        
        if (!result) {
            setError(ErrorCodes.Type);
            warningMessasge = String.format("Rinex Type: %c", type);
            printWarning();
        }
        
        return result;
    }
    
    private boolean checkSystem(String line) {
        boolean result = false;
        
        char c = line.charAt(40);
        for (int i = 0; i < system_list.length; ++i) {
            result |= (c == system_list[i]);
        }
        
        if (!result) {
            warningMessasge = String.format("Satellite System: %c", c);
            printWarning();
            setError(ErrorCodes.System);
        }
        
        return result;
    }
    
    private boolean checkLine1(String line) {
        
        boolean result = (line.length() > MinimumLineLength);
        
        if (result) {
            result = checkMarkerRinexVersion(line);
            if (result) {
                result = checkRinexVersion(line);
                if (result) {
                    result = checkType(line);
                    if (result) {
                        result = checkSystem(line);
                    }
                }
            }
        } else {
            setError(ErrorCodes.Line1Length);
        }
        
        return result;
    }
    
    private void setError(ErrorCodes errorCode) {
        this.errorCode = errorCode;
        setErrorMessasge(errorCode);
    }
    
    public boolean open(String fileName) {
        boolean result = true;
        boolean eoh = false;
        
        lineIndex = 0;
        headLines.clear();
        dataLines.clear();
        
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            if (br.ready()) {
                String line = br.readLine();
                lineIndex++;
                result = checkLine1(line);
                
                if (result) {
                    headLines.add(line);
                }
                
                while (br.ready() && result && !eoh) {
                    line = br.readLine();
                    lineIndex++;
                    result = (line.length() > MinimumLineLength);
                    
                    if (result) {
                        eoh = (line.indexOf(MarkerEndOfHeader, MarkerLineIndex) > -1);
                        if (!eoh) {
                            headLines.add(line);
                        }
                    }
                }
                while (br.ready()) {
                    line = br.readLine();
                    lineIndex++;
                    dataLines.add(line);
                }
                
            } else {
                result = false;
            }
            br.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            result = false;
        }
        
        if (!result && getErrorCode() == ErrorCodes.Success) {
            setErrorMessasge(ErrorCodes.Unknown);
        }
        
        if (result) {
            setError(ErrorCodes.Success);
        }
        
        switchReader();
        
        return result;
    }
    
    void switchReader() {
        switch (type) {
            case 'O':
                ObserveReader or = new ObserveReader(headLines, dataLines);
                break;
            case 'G':
                GlonassNavDataReader gnd = new GlonassNavDataReader(headLines, dataLines);
                gnd_tmp = gnd;
                break;
        }
    }
    
    /**
     * @return the Error Messasge
     */
    public String getErrorMessasge() {
        return errorMessasge;
    }

    private void setErrorMessasge(ErrorCodes type) {
        switch (type) {
            case Success:
                errorMessasge = "Success\n";
                break;
            case Line1Length:
                errorMessasge = "Incorrect length of line 1\n";
                break;
            case MarkerVersionType:
                errorMessasge = "Incorrect version/type marker\n";
                break;
            case Version: 
                errorMessasge = "Unknown Rinex Version\n";
                break;
            case Type: 
                errorMessasge = "Unknown Rinex Type\n";
                break;
            case System: 
                errorMessasge = "Unknown Satellite System\n";
                break;
            default:
                errorMessasge = "Unknown Error\n";
                break;
        }
        errorMessasge = String.format("[line %d]: ", lineIndex) + errorMessasge;
    }
    
    private void printWarning() {
        System.out.println(warningMessasge);
    }
    
}

