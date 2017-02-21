/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gnat;

import java.io.File;
import rinex.GlonassNavData;
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
        RinexReader hr = new RinexReader();
        
        flist = (new File("e:\\data\\rnx\\obs")).listFiles();
        for (File f : flist) {
            if (f.isFile() && f.canRead()) {
                hr.open(f.getAbsolutePath());
                System.out.println(hr.getErrorMessasge());
            }
        }
        
        if (hr.observeReader != null) {
//            hr.observeReader.save();
        }
        
        RinexReader ndr = new RinexReader();

        flist = (new File("e:\\data\\rnx\\nav")).listFiles();
        for (File f : flist) {
            if (f.isFile() && f.canRead()) {
                ndr.open(f.getAbsolutePath());
                System.out.println(ndr.getErrorMessasge());
            }
        }
    
//        ndr.gnd_tmp.save();
        
        CalcObject co = new CalcObject();
        
        for (GlonassNavData nd : ndr.gnd_tmp.getNavDataList()) {
            co.add(nd);
        }
        
//        co.save("co.txt");
        co.setObserves(hr.observeReader.getObjectList());
        co.saveDelta("delta.txt");
        MarquardtMin mm = new MarquardtMin();
        mm.exec(co);
    }
}
