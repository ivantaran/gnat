/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gnat;

import java.util.Arrays;

/**
 *
 * @author Taran
 */
public class GiModel {
    public static final double C20  = -1082.63e-6;
    public static final double AE   =  6378136.0;
    public static final double WE   =  7.292115e-5;
    public static final double MUE  =  398600.44e9;
    public static final double CVEL =  299792458.0;
        
    private void gimodel(GlonassSet s) {
        double r;
        double a, b, c;
        
        double[] data = s.getInitial();
        
        r = Math.sqrt(
                data[0] * data[0] +
                data[1] * data[1] +
                data[2] * data[2]
                );
        
        if (r < Double.MIN_VALUE) {
            r = Double.MIN_VALUE;
        }
        
        a = -MUE / (r * r * r);
        b = 1.5 * C20 * MUE * AE * AE / (r * r * r * r * r);
        c = 5.0 * data[2] * data[2] / (r * r);

        s.getDerivatives()[0] = data[3];
        s.getDerivatives()[1] = data[4];
        s.getDerivatives()[2] = data[5];
        
        s.getDerivatives()[3] = 
                a * data[0] + 
                b * data[0] * (1 - c) + 
                WE * WE * data[0] + 
                2 * WE * data[4] + 
                data[6];

        s.getDerivatives()[4] = 
                a * data[1] + 
                b * data[1] * (1 - c) + 
                WE * WE * data[1] - 
                2 * WE * data[3] + 
                data[7];

        s.getDerivatives()[5] = 
                a * data[2] + 
                b * data[2] * (3 - c) + 
                data[8];
    }
    
    public void step(GlonassSet s) {
        GlonassSet k1;
        GlonassSet k2;
        GlonassSet k3;
        GlonassSet k4;
        
        k1 = s.clone();
        
        gimodel(k1);
        k2 = k1.clone();
        for (int i = 0; i < GlonassSet.VectorLength; i++) {
            k1.getDerivatives()[i] *= k1.getStepTime();
            k2.getInitial()[i] = 0.5*k1.getDerivatives()[i] + s.getInitial()[i];
        }

        gimodel(k2);
        k3 = k2.clone();
        for (int i = 0; i < GlonassSet.VectorLength; ++i) {
            k2.getDerivatives()[i] *= k2.getStepTime();
            k3.getInitial()[i] = 0.5*k2.getDerivatives()[i] + s.getInitial()[i];
        }

        gimodel(k3);
        k4 = k3.clone();
        for (int i = 0; i < GlonassSet.VectorLength; ++i) {
            k3.getDerivatives()[i] *= k3.getStepTime();
            k4.getInitial()[i] = k3.getDerivatives()[i] + s.getInitial()[i];
        }

        gimodel(k4);
        for (int i = 0; i < GlonassSet.VectorLength; ++i) {
            k4.getDerivatives()[i] *= k4.getStepTime();
            s.getCurrent()[i] = s.getInitial()[i] + 
                (k1.getDerivatives()[i] + 2.0 * k2.getDerivatives()[i] + 
                 2.0 * k3.getDerivatives()[i] + k4.getDerivatives()[i]) / 6.0;
        }
    }
    
}
