/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gnat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        
        RinexReader rnx = new RinexReader();
        
        try {
            Files.walk(Paths.get("c:\\work\\data\\3\\"))
                    .filter(path -> 
                            Files.isRegularFile(path) && Files.isReadable(path)
                    )
                    .forEach(path -> {
                        rnx.open(path.toAbsolutePath().toString());
                        System.out.println(rnx.getErrorMessasge());
                    });
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        
        if (rnx.observeReader != null) {
            rnx.observeReader.save();
        }
        
        rnx.gnd_tmp.save();
        
        CalcObject co = new CalcObject();
        co.addGlonassNavDataList(rnx.gnd_tmp.getNavDataList());
        
//        co.save("co.txt");
        co.setPositionXyz(rnx.observeReader.getApproxPositionXyz());
        co.addObservesMap(rnx.observeReader.getObjectMap());
        co.saveDelta("delta.txt");
        MarquardtMin mm = new MarquardtMin();
        mm.exec(co);
        co.saveDelta("delta1.txt");
//    http://pastebin.com/mgcv1FpA
    }
}
