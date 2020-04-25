/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rinex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Ivan
 */
public class RinexReader {
    public static final int RINEX_VERSION = 0;
    public static final int MINIMUM_LINE_LENGTH = 61;
    public static final int MARKER_LINE_INDEX = 60;

    public static final String MARKER_RINEX_VERSION = "RINEX VERSION / TYPE";
    public static final String MARKER_PGM_RUN_BY_DATE = "PGM / RUN BY / DATE";
    public static final String MARKER_NAME = "MARKER NAME";
    public static final String MARKER_NUMBER = "MARKER NUMBER";
    public static final String MARKER_TYPE = "MARKER TYPE";
    public static final String MARKER_OBSERVER_AGENCY = "OBSERVER / AGENCY";
    public static final String MARKER_REC_NUM_TYPE_VERS = "REC # / TYPE / VERS";
    public static final String MARKER_ANT_NUM_TYPE = "ANT # / TYPE";
    public static final String MARKER_APPROX_POSITION_XYZ = "APPROX POSITION XYZ";
    public static final String MARKER_ANTENNA_DELTA_HEN = "ANTENNA: DELTA H/E/N";
    public static final String MARKER_TYPES_OF_OBSERV = "# / TYPES OF OBSERV";
    public static final String MARKER_SYS_NUM_OBS_TYPES = "SYS / # / OBS TYPES";
    public static final String MARKER_SIGNAL_STRENGTH_UNIT = "SIGNAL STRENGTH UNIT";
    public static final String MARKER_INTERVAL = "INTERVAL";
    public static final String MARKER_TIME_OF_FIRST_OBS = "TIME OF FIRST OBS";
    public static final String MARKER_SYS_PHASE_SHIFT = "SYS / PHASE SHIFT";
    public static final String MARKER_GLONASS_SLOT_FRQ = "GLONASS SLOT / FRQ #";
    public static final String MARKER_GLONASS_COD_PHS_BIS = "GLONASS COD/PHS/BIS";
    public static final String MARKER_COMMENT = "COMMENT";
    public static final String MARKER_LEAP_SECONDS = "LEAP SECONDS";
    public static final String MARKER_END_OF_HEADER = "END OF HEADER";

    public GlonassNavDataReader gnd_tmp;
    public ObserveReader observeReader;
    private final double[] version_list = { 2.00, 2.01, 2.10, 2.11, 3.02, 3.03, 3.04 };
    private final char[] type_list;
    private final char[] system_list;
    private char type = ' ';
    private String errorMessasge = "";
    private String warningMessasge = "";
    private HeaderReader headerReader;

    private static enum ErrorCodes {
        Success, Line1Length, MarkerVersionType, Version, Type, System, Unknown
    };

    private final ArrayList<String> headLines = new ArrayList();
    private final ArrayList<String> dataLines = new ArrayList();
    private int lineIndex;
    private ErrorCodes errorCode;

    public RinexReader() {

        type_list = new char[3];
        type_list[0] = 'O';
        type_list[1] = 'G';
        type_list[2] = 'N';

        system_list = new char[6];
        system_list[0] = ' ';
        system_list[1] = 'G';
        system_list[2] = 'R';
        system_list[3] = 'E';
        system_list[4] = 'S';
        system_list[5] = 'M';

        observeReader = null;
        gnd_tmp = null;
        headerReader = null;
    }

    public static int getMarkerIndex(String marker, ArrayList<String> list) {
        boolean result = false;
        int index = -1;
        Iterator<String> it = list.iterator();

        while (it.hasNext() && !result) {
            String line = it.next();
            int pos = line.indexOf(marker, RinexReader.MARKER_LINE_INDEX);
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
        boolean result = (line.indexOf(MARKER_RINEX_VERSION, lineIndex) > -1);

        if (!result) {
            setError(ErrorCodes.MarkerVersionType);
        }

        return result;
    }

    private boolean checkRinexVersion(String line) {
        boolean result = false;

        double v = Double.valueOf(line.substring(0, 9).trim());
        for (int i = 0; i < version_list.length; ++i) {
            if (v == version_list[i]) {
                result = true;
                break;
            }
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

        boolean result = (line.length() > MINIMUM_LINE_LENGTH);

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

    public boolean open(String fileName, String filter) {
        boolean result = false;
        boolean eoh = false;

        lineIndex = 0;
        headLines.clear();
        dataLines.clear();

        if (!filter.isEmpty() && !fileName.contains(filter)) {
            setError(ErrorCodes.Success);
            return result;
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            if (br.ready()) {
                String line = br.readLine();
                lineIndex++;
                result = checkLine1(line);

                if (result) {
                    headLines.add(line);
                } else {
                    br.close();
                    return result;
                }

                while (br.ready() && result && !eoh) {
                    line = br.readLine();
                    lineIndex++;
                    result = (line.length() > MINIMUM_LINE_LENGTH);

                    if (result) {
                        eoh = (line.indexOf(MARKER_END_OF_HEADER, MARKER_LINE_INDEX) > -1);
                        if (!eoh) {
                            headLines.add(line);
                        }
                    }
                }
                while (br.ready()) {
                    line = br.readLine();
                    lineIndex++;
                    // TODO MarkerComment to dataLines
                    dataLines.add(line);
                    // if (line.indexOf(MARKER_COMMENT, MARKER_LINE_INDEX) < 0) {
                    // dataLines.add(line);
                    // }
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

        if (headerReader == null) {
            headerReader = new HeaderReader(headLines);
        } else {
            headerReader.setHeadLines(headLines);
        }
        switchReader(headerReader);

        return result;
    }

    public void openDir(String dir, String filter) {
        try {
            Files.walk(Paths.get(dir)).filter(path -> Files.isRegularFile(path) && Files.isReadable(path))
                    .forEach(path -> {
                        if (open(path.toAbsolutePath().toString(), filter)) {
                            System.out.print(path + "\r");
                        } else if (getErrorCode() != ErrorCodes.Success) {
                            System.out.println(path + "\r");
                            System.out.println(getErrorMessasge());
                        }
                    });
            System.out.println();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    void switchReader(HeaderReader hr) {
        switch (hr.getType().charAt(0)) {
            case 'O':
                if (observeReader == null) {
                    switch ((int) hr.getRinexVersion()) {
                        case 3:
                            observeReader = new ObserveReader3(hr, headLines, dataLines);
                            break;
                        default:
                            observeReader = new ObserveReader(hr, headLines, dataLines);
                            break;
                    }
                } else {
                    observeReader.add(headLines, dataLines);
                }
                break;
            case 'G':
                if (gnd_tmp == null) {
                    gnd_tmp = new GlonassNavDataReader(dataLines);
                } else {
                    gnd_tmp.add(dataLines);
                }
                break;
            case 'N':
                if (gnd_tmp == null) {
                    gnd_tmp = new GlonassNavDataReader3(dataLines);
                } else {
                    gnd_tmp.add(dataLines);
                }
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
