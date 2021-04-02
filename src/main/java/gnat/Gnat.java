
package gnat;

import java.io.File;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashSet;

import rinex.RinexReader;

/**
 *
 * @author Ivan
 */
public class Gnat {

    /**
     * @param args the command line arguments
     */
    private static final String ARG_CLOCK_CORRECTION = "--clockcorr=";
    private static final String ARG_FILTER = "--filter=";
    private static final String ARG_LETTERS = "--letters=";
    private static final String ARG_MEDIAN_FILTER_THRESHOLD = "--mfthreshold=";
    private static final String ARG_MAXELV = "--maxelv=";
    private static final String ARG_MINELV = "--minelv=";
    private static final String ARG_MINSNR = "--minsnr=";
    private static final String ARG_OBSNAMES1 = "--obsnames1=";
    private static final String ARG_OBSNAMES2 = "--obsnames2=";
    private static final String ARG_OUTPUT = "--output=";
    private static final String ARG_POSOFFSET = "--posoffset=";
    private static final String ARG_SINGLE = "--single=";
    private static final String ARG_STEP = "--step=";
    private static final String ARG_DIRS = "--dirs=";

    private static final void printUsage() {
        System.out.println("Usage:\njava -jar Gnat.jar --filter=SUBSTRING /my/path");
    }

    private static final void printMode(CalcObject co) {
        System.out.printf("Step Time [ms]:      %d\n", co.getStepTime());
        System.out.printf("Max Elevation [deg]: %1.1f\n", Math.toDegrees(co.getMaxElevation()));
        System.out.printf("Min Elevation [deg]: %1.1f\n", Math.toDegrees(co.getMinElevation()));
        System.out.printf("Min SNR [dB/Hz]:     %1.1f\n", co.getMinSnr());
        System.out.printf("Single Mode:         %s\n", co.getSingleMode());
        System.out.println();
    }

    public static void main(String[] args) {
        String clockcorr = "";
        String filter = "";
        String letters = "";
        String dirs = "";
        String obsnames1 = "";
        String obsnames2 = "";
        String output = "";
        String path = "";
        String posoffset = "";
        String single = "";

        Long step = null;
        Double maxelv = null;
        Double minelv = null;
        Double minsnr = null;
        Double mfthreshold = null;

        if (args.length < 1) {
            printUsage();
            System.exit(0);
        }

        path = args[args.length - 1];

        if (path.isEmpty()) {
            printUsage();
            System.exit(0);
        }

        try {
            for (String s : args) {
                if (s.contains(ARG_FILTER)) {
                    filter = s.replaceFirst(ARG_FILTER, "");
                } else if (s.contains(ARG_LETTERS)) {
                    letters = s.replaceFirst(ARG_LETTERS, "");
                } else if (s.contains(ARG_DIRS)) {
                    dirs = s.replaceFirst(ARG_DIRS, "");
                } else if (s.contains(ARG_STEP)) {
                    s = s.replaceFirst(ARG_STEP, "");
                    step = Long.valueOf(s);
                } else if (s.contains(ARG_MAXELV)) {
                    s = s.replaceFirst(ARG_MAXELV, "");
                    maxelv = Math.toRadians(Double.valueOf(s));
                } else if (s.contains(ARG_MINELV)) {
                    s = s.replaceFirst(ARG_MINELV, "");
                    minelv = Math.toRadians(Double.valueOf(s));
                } else if (s.contains(ARG_MINSNR)) {
                    s = s.replaceFirst(ARG_MINSNR, "");
                    minsnr = Double.valueOf(s);
                } else if (s.contains(ARG_SINGLE)) {
                    single = s.replaceFirst(ARG_SINGLE, "");
                } else if (s.contains(ARG_OBSNAMES1)) {
                    obsnames1 = s.replaceFirst(ARG_OBSNAMES1, "");
                } else if (s.contains(ARG_OBSNAMES2)) {
                    obsnames2 = s.replaceFirst(ARG_OBSNAMES2, "");
                } else if (s.contains(ARG_CLOCK_CORRECTION)) {
                    clockcorr = s.replaceFirst(ARG_CLOCK_CORRECTION, "");
                } else if (s.contains(ARG_OUTPUT)) {
                    output = s.replaceFirst(ARG_OUTPUT, "");
                } else if (s.contains(ARG_MEDIAN_FILTER_THRESHOLD)) {
                    s = s.replaceFirst(ARG_MEDIAN_FILTER_THRESHOLD, "");
                    mfthreshold = Double.valueOf(s);
                } else if (s.contains(ARG_POSOFFSET)) {
                    posoffset = s.replaceFirst(ARG_POSOFFSET, "");
                }
            }
        } catch (NumberFormatException ex) {
            System.out.println(ex.getMessage());
            System.exit(-1);
        }

        RinexReader rnx = new RinexReader();

        File f = new File(path);
        if (f.exists() && !f.isDirectory()) {
            rnx.open(path, filter);
        } else {
            rnx.openDir(path, filter);
        }

        if (!dirs.isEmpty()) {
            String[] list = dirs.split("[ ,;]");
            for (String dir : list) {
                rnx.openDir(dir, filter);
            }
        }

        // if (rnx.observeReader != null) {
        // rnx.observeReader.save();
        // }

        if (rnx.gnd_tmp != null) {
            // rnx.gnd_tmp.save();

            CalcObject co = new CalcObject();

            if (!letters.isEmpty()) {
                try {
                    String[] list = letters.split("[ ,;]");
                    HashSet<Integer> lettersSet = new HashSet();
                    for (int i = 0; i < list.length; i++) {
                        lettersSet.add(Integer.parseInt(list[i]));
                    }
                    co.setLetters(lettersSet);
                } catch (NumberFormatException ex) {
                    System.out.println(letters);
                    System.out.println(ex.getMessage());
                }
            }

            if (step != null) {
                co.setStepTime(step);
            }

            if (maxelv != null) {
                co.setMaxElevation(maxelv);
            }

            if (minelv != null) {
                co.setMinElevation(minelv);
            }

            if (minsnr != null) {
                co.setMinSnr(minsnr);
            }

            if (mfthreshold != null) {
                co.setMedianFilterThreshold(mfthreshold);
            }

            if (!single.isEmpty()) {
                co.setSingleMode(single);
            }

            if (!obsnames1.isEmpty()) {
                String[] list = obsnames1.split("[ ,;]");
                co.setObservationsNames1(list);
            }

            if (!obsnames2.isEmpty()) {
                String[] list = obsnames2.split("[ ,;]");
                co.setObservationsNames2(list);
            }

            if (!clockcorr.isEmpty()) {
                String[] list = clockcorr.split("[,;]");
                double rate = Double.valueOf(list[0]);
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
                GregorianCalendar gc = new GregorianCalendar();
                try {
                    gc.setTime(sdf.parse(list[1]));
                    co.setClockCorrection(rate * 0.001, gc.getTimeInMillis());
                } catch (ParseException e) {
                    System.out.println(e.toString());
                }
            }

            if (!posoffset.isEmpty()) {
                try {
                    String[] list = posoffset.split("[ ,;]");
                    double po[] = new double[list.length];
                    for (int i = 0; i < po.length; i++) {
                        po[i] = Double.parseDouble(list[i]);
                    }
                    co.setPositionOffset(po);
                } catch (NumberFormatException ex) {
                    System.out.println(posoffset);
                    System.out.println(ex.getMessage());
                }
            }

            co.addGlonassNavDataList(rnx.gnd_tmp.getNavDataList());

            double p[] = rnx.observeReader.getApproxPositionXyz();
            co.setPosition(p);

            co.addObservesMap(rnx.observeReader.getObjectMap());
            co.saveDelta("delta.txt");

            printMode(co);
            // System.exit(0);

            MarquardtMin mm = new MarquardtMin();
            mm.exec(co);
            if (!output.isEmpty()) {
                co.saveDelta(output);
            } else {
                co.saveDelta("output.txt");
            }
        }

    }
}
