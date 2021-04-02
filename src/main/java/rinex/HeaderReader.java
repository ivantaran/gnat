
package rinex;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Ivan
 */
public class HeaderReader {

    private double rinex_version = 0.0;
    private String type = "";
    private String system = "";
    private String pgm = "";
    private String run_by = "";
    private String date = "";
    private String marker_name = "";
    private String marker_number = "";
    private String marker_type = "";
    private String observer = "";
    private String agency = "";
    private String rec_num = "";
    private String rec_type = "";
    private String rec_vers = "";
    private String ant_num = "";
    private String ant_type = "";
    private final double approx_position_xyz[] = { 0.0, 0.0, 0.0 };
    private double ant_hen[] = { 0.0, 0.0, 0.0 };
    private ArrayList<String> types_of_observ = null;
    private final HashMap<Character, String[]> sys_obs_types = new HashMap();
    private String signal_strength_unit = "";
    private double interval = 1.0;
    private long time_of_first_obs = 0;
    private String time_system = "";
    private ArrayList<String> sys_phase_shift = null;
    private ArrayList<String> glonass_slot_frq = null;
    private ArrayList<String> glonass_cod_phs_bis = null;
    private int leap_seconds = 18; // TODO !!!

    private ArrayList<String> headLines;
    private int lineIndex = 0;

    HeaderReader(ArrayList<String> headLines) {
        setHeadLines(headLines);
    }

    /**
     * @param headLines the headLines to set
     */
    public final void setHeadLines(ArrayList<String> headLines) {
        this.headLines = headLines;
        parse();
    }

    private void parse() {
        lineIndex = 0;
        while (linesReady()) {
            parseLine();
        }
    }

    private void parse_rinex_version(String line) {
        try {
            rinex_version = Double.valueOf(line.substring(0, 9));
            type = line.substring(20, 40);
            system = line.substring(40, 60);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            warning(e.getMessage());
        }
    }

    private void parse_pgm_run_by_date(String line) {
    }

    private void parse_marker_name(String line) {
    }

    private void parse_marker_number(String line) {
    }

    private void parse_marker_type(String line) {
    }

    private void parse_observer_agency(String line) {
    }

    private void parse_rec_num_type_vers(String line) {
    }

    private void parse_ant_num_type(String line) {
    }

    private void parse_approx_position_xyz(String line) {
        String lineValue;
        try {
            lineValue = line.substring(0, 14).trim();
            approx_position_xyz[0] = lineValue.isEmpty() ? 0.0 : Double.parseDouble(lineValue);
            lineValue = line.substring(14, 28).trim();
            approx_position_xyz[1] = lineValue.isEmpty() ? 0.0 : Double.parseDouble(lineValue);
            lineValue = line.substring(28, 42).trim();
            approx_position_xyz[2] = lineValue.isEmpty() ? 0.0 : Double.parseDouble(lineValue);
        } catch (IndexOutOfBoundsException | NumberFormatException ex) {
            warning(String.format("error at line: %d", lineIndex));
            warning(line);
            warning(Thread.currentThread().getStackTrace()[1].getMethodName());
            warning(ex.getMessage());
        }
    }

    private void parse_antenna_delta_hen(String line) {
    }

    private void parse_types_of_observ(int index) {
    }

    private void parse_sys_obs_types(String line) {
        int countLines = 0;

        try {
            char sys = line.charAt(0);
            if (!Character.isLetter(sys)) {
                warning(String.format("Error at line %d", lineIndex));
                warning(line);
                warning(Thread.currentThread().getStackTrace()[1].getMethodName());
                System.exit(-1);
            }
            int count = Integer.parseInt(line.substring(3, 6).trim());
            countLines = (count - 1) / 13 + 1;
            String types[] = new String[count];
            int c = 0;

            for (int j = 0; j < countLines; j++) {
                if (line.substring(RinexReader.MARKER_LINE_INDEX).contains(RinexReader.MARKER_SYS_NUM_OBS_TYPES)) {
                    for (int i = 0; i < 13; i++) {
                        if (c < count) {
                            int pos = 7 + i * 4;
                            String type = line.substring(pos, pos + 3).trim();
                            if (!type.isEmpty()) {
                                types[c] = type;
                            } else {
                                warning(String.format("Error at line %d", lineIndex));
                                warning(line);
                                System.exit(-1);
                            }
                            c++;
                        } else {
                            break;
                        }
                    }
                } else {
                    warning(String.format("Error at line %d", lineIndex));
                    warning(line);
                    warning(Thread.currentThread().getStackTrace()[1].getMethodName());
                    System.exit(-1);
                }
                if (c < count) {
                    line = getLine();
                }
            }
            getSysObsTypes().put(sys, types);
        } catch (IndexOutOfBoundsException | NumberFormatException ex) {
            warning(String.format("Error at line %d", lineIndex));
            warning(line);
            warning(ex.getMessage());
            warning(Thread.currentThread().getStackTrace()[1].getMethodName());
        }
    }

    private void parse_signal_strength_unit(String line) {
    }

    private void parse_interval(String line) {
        try {
            interval = Double.parseDouble(line.substring(0, 10).trim());
        } catch (IndexOutOfBoundsException | NumberFormatException ex) {
            warning(String.format("error at line: %d", lineIndex));
            warning(line);
            warning(ex.getMessage());
            warning(Thread.currentThread().getStackTrace()[1].getMethodName());
            System.exit(-1);
        }
    }

    private void parse_time_of_first_obs(String line) {
    }

    private void parse_sys_phase_shift(int index) {
    }

    private void parse_glonass_slot_frq(int index) {
    }

    private void parse_glonass_cod_phs_bis(int index) {
    }

    private void parse_leap_seconds(String line) {
        try {
            leap_seconds = Integer.parseInt(line.substring(0, 6).trim());
        } catch (IndexOutOfBoundsException | NumberFormatException ex) {
            warning(String.format("error at line: %d", lineIndex));
            warning(line);
            warning(ex.getMessage());
            warning(Thread.currentThread().getStackTrace()[1].getMethodName());
            System.exit(-1);
        }
    }

    private void parseLine() {
        String line = getLine();

        if (line.length() <= RinexReader.MARKER_LINE_INDEX) {
            return;
        } else if (line.substring(RinexReader.MARKER_LINE_INDEX).contains(RinexReader.MARKER_RINEX_VERSION)) {
            parse_rinex_version(line);
        } else if (line.substring(RinexReader.MARKER_LINE_INDEX).contains(RinexReader.MARKER_PGM_RUN_BY_DATE)) {
            parse_pgm_run_by_date(line);
        } else if (line.substring(RinexReader.MARKER_LINE_INDEX).contains(RinexReader.MARKER_NAME)) {
            parse_marker_name(line);
        } else if (line.substring(RinexReader.MARKER_LINE_INDEX).contains(RinexReader.MARKER_NUMBER)) {
            parse_marker_number(line);
        } else if (line.substring(RinexReader.MARKER_LINE_INDEX).contains(RinexReader.MARKER_TYPE)) {
            parse_marker_type(line);
        } else if (line.substring(RinexReader.MARKER_LINE_INDEX).contains(RinexReader.MARKER_OBSERVER_AGENCY)) {
            parse_observer_agency(line);
        } else if (line.substring(RinexReader.MARKER_LINE_INDEX).contains(RinexReader.MARKER_REC_NUM_TYPE_VERS)) {
            parse_rec_num_type_vers(line);
        } else if (line.substring(RinexReader.MARKER_LINE_INDEX).contains(RinexReader.MARKER_ANT_NUM_TYPE)) {
            parse_ant_num_type(line);
        } else if (line.substring(RinexReader.MARKER_LINE_INDEX).contains(RinexReader.MARKER_APPROX_POSITION_XYZ)) {
            parse_approx_position_xyz(line);
        } else if (line.substring(RinexReader.MARKER_LINE_INDEX).contains(RinexReader.MARKER_ANTENNA_DELTA_HEN)) {
            parse_antenna_delta_hen(line);
        } else if (line.substring(RinexReader.MARKER_LINE_INDEX).contains(RinexReader.MARKER_TYPES_OF_OBSERV)) {
            parse_types_of_observ(lineIndex);
        } else if (line.substring(RinexReader.MARKER_LINE_INDEX).contains(RinexReader.MARKER_SYS_NUM_OBS_TYPES)) {
            parse_sys_obs_types(line);
        } else if (line.substring(RinexReader.MARKER_LINE_INDEX).contains(RinexReader.MARKER_SIGNAL_STRENGTH_UNIT)) {
            parse_signal_strength_unit(line);
        } else if (line.substring(RinexReader.MARKER_LINE_INDEX).contains(RinexReader.MARKER_INTERVAL)) {
            parse_interval(line);
        } else if (line.substring(RinexReader.MARKER_LINE_INDEX).contains(RinexReader.MARKER_TIME_OF_FIRST_OBS)) {
            parse_time_of_first_obs(line);
        } else if (line.substring(RinexReader.MARKER_LINE_INDEX).contains(RinexReader.MARKER_SYS_PHASE_SHIFT)) {
            parse_sys_phase_shift(lineIndex);
        } else if (line.substring(RinexReader.MARKER_LINE_INDEX).contains(RinexReader.MARKER_GLONASS_SLOT_FRQ)) {
            parse_glonass_slot_frq(lineIndex);
        } else if (line.substring(RinexReader.MARKER_LINE_INDEX).contains(RinexReader.MARKER_GLONASS_COD_PHS_BIS)) {
            parse_glonass_cod_phs_bis(lineIndex);
        } else if (line.substring(RinexReader.MARKER_LINE_INDEX).contains(RinexReader.MARKER_LEAP_SECONDS)) {
            parse_leap_seconds(line);
        }
    }

    private String getLine() {
        String line = "";
        if (lineIndex < headLines.size()) {
            line = headLines.get(lineIndex);
            lineIndex++;
        }
        return line;
    }

    private boolean linesReady() {
        return (lineIndex < headLines.size());
    }

    private void warning(String message) {
        System.out.println(message);
    }

    /**
     * @return the rinex_version
     */
    public double getRinexVersion() {
        return rinex_version;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @return the system
     */
    public String getSystem() {
        return system;
    }

    /**
     * @return the leap_seconds
     */
    public int getLeapSeconds() {
        return leap_seconds;
    }

    /**
     * @return the sys_obs_types
     */
    public HashMap<Character, String[]> getSysObsTypes() {
        return sys_obs_types;
    }

    /**
     * @return the approx_position_xyz
     */
    public double[] getApproxPositionXyz() {
        return approx_position_xyz;
    }

}
