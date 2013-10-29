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
        hr.open("D:/data/msr/bjfs2770.13o");
        System.out.println(hr.getErrorMessasge());
    }
}
