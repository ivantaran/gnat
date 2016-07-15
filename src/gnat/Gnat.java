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

        RinexReader hr = new RinexReader();
        hr.open("d:\\tmp\\5\\data\\16196\\L1\\obs\\scae1960cd.16o");
        System.out.println(hr.getErrorMessasge());
        
        RinexReader ndr = new RinexReader();
        ndr.open("d:\\tmp\\5\\data\\16196\\L1\\nav\\scae1960cd.16g");
        System.out.println(ndr.getErrorMessasge());

        CalcObject co = new CalcObject(ndr.gnd_tmp.getNavDataList().get(0));
        co.save("co.txt");
        
    }
}
