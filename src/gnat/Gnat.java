/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
        // TODO code application logic here
        RinexReader hr = new RinexReader();
//        hr.open("D:/data/msr/MCCK2740.13G");
        hr.open("D:/data/msr/d044-002.obs");
        System.out.println(hr.getErrorMessasge());
        RinexReader ndr = new RinexReader();
        ndr.open("D:/data/msr/MCCK3070.13G");
        System.out.println(ndr.getErrorMessasge());
        
        CalcObject co = new CalcObject(ndr.gnd_tmp.getNavDataList().get(1));
        co.save("co.txt");
    }
}
