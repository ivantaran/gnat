
package gnat;

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
    private static final String ARG_FILTER = "--filter=";
    private static final String ARG_LETTERS = "--letters=";
    private static final String ARG_MINELV = "--minelv=";
    private static final String ARG_MINSNR = "--minsnr=";
    private static final String ARG_OBSNAMES1 = "--obsnames1=";
    private static final String ARG_OBSNAMES2 = "--obsnames2=";
    private static final String ARG_POSOFFSET = "--posoffset=";
    private static final String ARG_SINGLE = "--single=";
    private static final String ARG_STEP = "--step=";

    private static final void printUsage() {
        System.out.println("Usage:\njava Gnat.jar --filter=SUBSTRING /my/path");
    }

    private static final void printMode(CalcObject co) {
        System.out.printf("Step Time [ms]:      %d\n", co.getStepTime());
        System.out.printf("Min Elevation [deg]: %1.1f\n", Math.toDegrees(co.getMinElevation()));
        System.out.printf("Min SNR [dB/Hz]:     %1.1f\n", co.getMinSnr());
        System.out.printf("Single Mode:         %s\n", co.getSingleMode());
        System.out.println();
    }

    public static void main(String[] args) {
        String filter = "";
        String letters = "";
        String obsnames1 = "";
        String obsnames2 = "";
        String path = "";
        String posoffset = "";
        String single = "";

        Long step = null;
        Double minelv = null;
        Double minsnr = null;

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
                } else if (s.contains(ARG_STEP)) {
                    s = s.replaceFirst(ARG_FILTER, "");
                    step = Long.valueOf(s);
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
                } else if (s.contains(ARG_POSOFFSET)) {
                    posoffset = s.replaceFirst(ARG_POSOFFSET, "");
                }
            }
        } catch (NumberFormatException ex) {
            System.out.println(ex.getMessage());
            System.exit(-1);
        }

        RinexReader rnx = new RinexReader();

        rnx.openDir(path, filter);

        if (rnx.observeReader != null) {
            rnx.observeReader.save();
        }

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

            if (minelv != null) {
                co.setMinElevation(minelv);
            }

            if (minsnr != null) {
                co.setMinSnr(minsnr);
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

            MarquardtMin mm = new MarquardtMin();
            mm.exec(co);
            co.saveDelta("delta1.txt");

        }

    }
}
