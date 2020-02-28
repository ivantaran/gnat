
package rinex;

import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 *
 * @author taran
 */
public class GlonassNavDataReader3 extends GlonassNavDataReader {

    GlonassNavDataReader3(ArrayList<String> dataLines) {
        super(dataLines);
    }

    @Override
    protected void parseNavData() {
        String line;
        double value;
        double[] state = new double[6];
        double[] acceleration = new double[3];
        GlonassNavData gnd = new GlonassNavData();

        /* line 1 */
        line = getLine();
        char navDataMarker = line.charAt(0);
        if (navDataMarker != 'R') {
            switch (navDataMarker) {
                case 'G':
                    for (int i = 0; i < 7; i++) {
                        getLine();
                    }
                    break;
                default:
                    warning(String.format("Unknown nav data: %c", navDataMarker));
                    break;
            }
            return;
        }
        gnd.setNumber(Integer.parseInt(line.substring(1, 3).trim()));
        line = line.replace('D', 'E');
        gnd.setTime(getTime(line.substring(4)));
        // TODO time offset with minus?
        gnd.setTimeOffset(Double.parseDouble(line.substring(23, 42).trim()));
        gnd.setFrequencyOffset(Double.parseDouble(line.substring(42, 61).trim()));
        gnd.setMessageTime(Double.parseDouble(line.substring(61, 80).trim()));

        /* line 2 */
        line = getLine();
        line = line.replace('D', 'E');
        state[0] = Double.parseDouble(line.substring(4, 23).trim());
        state[3] = Double.parseDouble(line.substring(23, 42).trim());
        acceleration[0] = Double.parseDouble(line.substring(42, 61).trim());
        value = Double.parseDouble(line.substring(61, 80).trim());
        gnd.setSuitability(value == 0);

        /* line 3 */
        line = getLine();
        line = line.replace('D', 'E');
        state[1] = Double.parseDouble(line.substring(4, 23).trim());
        state[4] = Double.parseDouble(line.substring(23, 42).trim());
        acceleration[1] = Double.parseDouble(line.substring(42, 61).trim());
        value = Double.parseDouble(line.substring(61, 80).trim());
        gnd.setFrequencyChannelNumber((int) value);

        /* line 4 */
        line = getLine();
        line = line.replace('D', 'E');
        state[2] = Double.parseDouble(line.substring(4, 23).trim());
        state[5] = Double.parseDouble(line.substring(23, 42).trim());
        acceleration[2] = Double.parseDouble(line.substring(42, 61).trim());
        gnd.setAge(Double.parseDouble(line.substring(61, 80).trim()));
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

    private GregorianCalendar getTime(String line) {
        GregorianCalendar c = new GregorianCalendar();
        try {
            int year = Integer.parseInt(line.substring(0, 4).trim());
            int month = Integer.parseInt(line.substring(5, 7).trim()) - 1;
            int day = Integer.parseInt(line.substring(8, 10).trim());
            int hour = Integer.parseInt(line.substring(11, 13).trim());
            int minute = Integer.parseInt(line.substring(14, 16).trim());
            int second = Integer.parseInt(line.substring(17, 19).trim());
            c.setTimeInMillis(0);
            c.set(year, month, day, hour, minute, second);
            // SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
            // sdf.setCalendar(c);
            // warning(sdf.format(c.getTimeInMillis()));
        } catch (NumberFormatException e) {
            warning(String.format("NumberFormatException at header line %d", getLineIndex()));
            warning(line);
            warning(e.getMessage());
        }
        return c;
    }

}
