/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gnat;

import java.io.File;
import rinex.RinexReader;

/**
 *
 * @author Ivan
 */
public class Gnat {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        File flist[];
        RinexReader obs = new RinexReader();
        
//        flist = (new File("e:\\data\\rnx\\1\\obs")).listFiles();
        flist = (new File("e:\\data\\rnx\\7\\17o")).listFiles();
        for (File f : flist) {
            if (f.isFile() && f.canRead()) {
                obs.open(f.getAbsolutePath());
                System.out.println(obs.getErrorMessasge());
            }
        }
        
        if (obs.observeReader != null) {
            obs.observeReader.save();
        }
        
        RinexReader nav = new RinexReader();

//        flist = (new File("e:\\data\\rnx\\1\\nav")).listFiles();
        flist = (new File("e:\\data\\rnx\\7\\17g")).listFiles();
        for (File f : flist) {
            if (f.isFile() && f.canRead()) {
                nav.open(f.getAbsolutePath());
                System.out.println(nav.getErrorMessasge());
            }
        }
//        ndr.gnd_tmp.save();
        
        CalcObject co = new CalcObject();
        co.addGlonassNavDataList(nav.gnd_tmp.getNavDataList());
        
//        co.save("co.txt");
        co.setPositionXyz(obs.observeReader.getApproxPositionXyz());
        co.addObservesMap(obs.observeReader.getObjectMap());
        co.saveDelta("delta.txt");
        MarquardtMin mm = new MarquardtMin();
        mm.exec(co);
        co.saveDelta("delta1.txt");
//    http://pastebin.com/mgcv1FpA
    }
}
