/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gnat;

import java.util.ArrayList;
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

        RinexReader hr = new RinexReader();
        hr.open("d:\\tmp\\5\\data\\16196\\L1\\obs\\scae1960cd.16o");
        System.out.println(hr.getErrorMessasge());
        
        RinexReader ndr = new RinexReader();
        ndr.open("d:\\tmp\\5\\data\\16196\\L1\\nav\\scae1960cd.16g");
        System.out.println(ndr.getErrorMessasge());

        CalcObject co = new CalcObject();
        
        for (GlonassNavData nd : ndr.gnd_tmp.getNavDataList()) {
            co.add(nd);
        }
        
        co.save("co.txt");
        
    }
}
