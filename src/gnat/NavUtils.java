
package gnat;

/**
 *
 * @author Taran
 */
public class NavUtils {
//    public static final double EARTH_OMG = 7.2921151467e-5;
//    WGS-84
    public static final double EARTH_A = 6378137.0;
    public static final double EARTH_F = 1.0 / 298.257223563;
    public static final double EARTH_B = EARTH_A * (1.0 - EARTH_F);
    public static final double E1SQR = ((EARTH_A * EARTH_A - EARTH_B * EARTH_B) / (EARTH_A * EARTH_A));
    public static final double E2SQR = ((EARTH_A * EARTH_A - EARTH_B * EARTH_B) / (EARTH_B * EARTH_B));
    
    public static void ecefToLla(double xyz[], double lla[]) {
        double p, t, st, ct, n;

        p = Math.sqrt(xyz[0] * xyz[0] + xyz[1] * xyz[1]);
        t = Math.atan2(xyz[2] * EARTH_A, p * EARTH_B);
        st = Math.sin(t);
        ct = Math.cos(t);
        
        lla[0] = Math.atan2(xyz[2] + E2SQR * EARTH_B * st * st * st, p - E1SQR * EARTH_A * ct * ct * ct);
        lla[0] = lla[0] % (0.5 * Math.PI);
        lla[1] = Math.atan2(xyz[1] , xyz[0]);

        n = EARTH_A / Math.sqrt(1.0 - E1SQR * Math.sin(lla[0]) * Math.sin(lla[0]));
        lla[2] = p / Math.cos(lla[0]) - n;
    }
}
