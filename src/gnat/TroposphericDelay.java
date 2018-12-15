package gnat;

/**
 *
 * @author taran
 */
public final class TroposphericDelay {
    
    private static final double K1 = 77.604;
    private static final double K2 = 382000;
    private static final double RD = 287.054;
    private static final double GM = 9.784;
    private static final double G = 9.80665;
    private static final double DMIN_NORTH = 28.0;
    private static final double DMIN_SOUTH = 211.0;
    private static final double OEM6K1RDDGM = 1.0e-6 * K1 * RD / GM;
    private static final double OEM6K2RD = 1.0e-6 * K1 * RD / GM;
    
    private static final double[] P0 = {
        1013.25, 
        1017.25, 
        1015.75, 
        1011.75, 
        1013.00
    };
    private static final double[] T0 = {
        299.65,  
        294.15,  
        283.15,  
        272.15,  
        263.65
    };
    private static final double[] E0 = {  
        26.31,   
        21.79,   
        11.66,    
        6.78,    
        4.11
    };
    private static final double[] BETA0 = {
        6.30e-3, 
        6.05e-3, 
        5.58e-3, 
        5.39e-3, 
        4.53e-3
    };
    private static final double[] LAMBDA0 = {
        2.77, 
        3.15, 
        2.57, 
        1.81, 
        1.55
    };
    private static final double[] DP = {
        0.00, 
        -3.75, 
        -2.25, 
        -1.75, 
        -0.50
    };
    private static final double[] DT = {
        0.00, 
        7.00, 
        11.00, 
        15.00, 
        14.50
    };
    private static final double[] DE = {
        0.00, 
        8.85, 
        7.24, 
        5.36, 
        3.39
    };
    private static final double[] DBETA = {
        0.00, 
        0.25e-3, 
        0.32e-3, 
        0.81e-3, 
        0.62e-3
    };
    private static final double[] DLAMBDA = {
        0.00, 
        0.33, 
        0.46, 
        0.74, 
        0.30
    };
    
    private static final double ANGLE_STEP = Math.toRadians(15.0);
    
    private static double m_scale = 0.0;
    private static int m_index = 5;
    
    private static final void assignIndexAndScale(double elevation) {
        m_scale = (elevation % ANGLE_STEP) / ANGLE_STEP;
        m_index = (elevation == ANGLE_STEP) ? 0 : (int)(elevation / ANGLE_STEP);
    }
    
    private static final double getValue(double[] array) {
        double r;

        if (m_index == 0) {
            r = array[0];
        } else if (m_index < array.length) {
            r = array[m_index - 1] + (array[m_index] - array[m_index - 1]) * m_scale;
        }
        else {
            r = array[array.length - 1];
        }

        return r;
    }
    
    /**
     * 
     * @param altitude in meters
     * @param elevation in radians [0..pi/2]
     * @param doy Day Of Year
     * 
     * @return Tropospherical range correction, m
     * 
     */
    public static final double getRangeCorrection(double latitude, double altitude, double elevation, double doy) {
        double r;
        
        assignIndexAndScale(elevation);
        double dmin = (latitude >= 0.0) ? DMIN_NORTH : DMIN_SOUTH;
        double ddcos = Math.cos(2.0 * Math.PI * (doy - dmin) / 365.25);
        
        double p = getValue(P0) - ddcos * getValue(DP);
        double t = getValue(T0) - ddcos * getValue(DT);
        double e = getValue(E0) - ddcos * getValue(DE);
        double beta = getValue(BETA0) - ddcos * getValue(DBETA);
        double lambda = getValue(LAMBDA0) - ddcos * getValue(DLAMBDA);
        
        double t0_dry = OEM6K1RDDGM * p;
        double t0_wet = OEM6K2RD * e / t / ((lambda + 1.0) * GM - beta * RD);
        
        double ombadt = 1.0 - beta * altitude / t;
        double t_dry = Math.pow(ombadt, G / RD / beta);
        double t_wet = Math.pow(ombadt, (lambda + 1.0) * G / RD / beta - 1.0);
        t_dry *= t0_dry;
        t_wet *= t0_wet;
        
        double sine = Math.sin(elevation);
        double m_e = 1.001 / Math.sqrt(0.002001 + sine * sine);

        r = (t_dry + t_wet) * m_e;
        
        return r;
    }
}
