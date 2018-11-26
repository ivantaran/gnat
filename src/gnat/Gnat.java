
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
    public static void main(String[] args) {
        
        RinexReader rnx = new RinexReader();
        
        rnx.openDir("/home/taran/tmp/combined");

//        rnx.openDir("/home/taran/tmp/BAMI2018/vniiftri/3/bami/PRM2");
        
        if (rnx.observeReader != null) {
            rnx.observeReader.save();
        }
        
        if (rnx.gnd_tmp != null) {
            rnx.gnd_tmp.save();
            
            CalcObject co = new CalcObject();
            co.addGlonassNavDataList(rnx.gnd_tmp.getNavDataList());

//            co.save("co.txt");
//            double pos[] = {2821840.3813,  2202230.2258,  5261500.6415};
//            co.setPositionXyz(pos);
            co.setPositionXyz(rnx.observeReader.getApproxPositionXyz());
            co.addObservesMap(rnx.observeReader.getObjectMap());
            co.saveDelta("delta.txt");
            MarquardtMin mm = new MarquardtMin();
            mm.exec(co);
            co.saveDelta("delta1.txt");
            
        }
        
        
//    http://pastebin.com/mgcv1FpA
    }
}
