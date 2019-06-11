
package gnat;

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
    private static final String ARG_STEP = "--step=";
    private static final String ARG_MINELV = "--minelv=";
    private static final String ARG_MINSNR = "--minsnr=";
    private static final String ARG_SINGLE = "--single=";
    
    private static final void printUsage() {
        System.out.println("Usage:\njava Gnat.jar --filter=SUBSTRING /my/path");
    }
    
    private static final void printMode(CalcObject co) {
        System.out.printf("Step Time [s]:       %1.3f\n", co.getStepTime());
        System.out.printf("Min Elevation [deg]: %1.1f\n", Math.toDegrees(co.getMinElevation()));
        System.out.printf("Min SNR [dB/Hz]:     %1.1f\n", co.getMinSnr());
        System.out.printf("Single Mode:         %s\n", co.getSingleMode());
        System.out.println();
    }
    
    public static void main(String[] args) {
        String filter = "";
        String path = "";
        String single = "";
        
        Double step = null;
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
                }
                else if (s.contains(ARG_STEP)) {
                    s = s.replaceFirst(ARG_FILTER, "");
                    step = Double.valueOf(s);
                }
                else if (s.contains(ARG_MINELV)) {
                    s = s.replaceFirst(ARG_MINELV, "");
                    minelv = Math.toRadians(Double.valueOf(s));
                }
                else if (s.contains(ARG_MINSNR)) {
                    s = s.replaceFirst(ARG_MINSNR, "");
                    minsnr = Double.valueOf(s);
                }
                else if (s.contains(ARG_SINGLE)) {
                    single = s.replaceFirst(ARG_SINGLE, "");
                }
            }
        }
        catch (NumberFormatException ex) {
            System.out.println(ex.getMessage());
            System.exit(-1);
        }
        
        RinexReader rnx = new RinexReader();
        
        rnx.openDir(path, filter);
        
        if (rnx.observeReader != null) {
//            rnx.observeReader.save();
        }
        
        if (rnx.gnd_tmp != null) {
//            rnx.gnd_tmp.save();
            
            CalcObject co = new CalcObject();
            
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
            
            co.addGlonassNavDataList(rnx.gnd_tmp.getNavDataList());

            co.setPositionXyz(rnx.observeReader.getApproxPositionXyz());
            co.addObservesMap(rnx.observeReader.getObjectMap());
            co.saveDelta("delta.txt");
            
            printMode(co);
            
            MarquardtMin mm = new MarquardtMin();
            mm.exec(co);
            co.saveDelta("delta1.txt");
            
        }
        
        
//    http://pastebin.com/mgcv1FpA
    }
}
