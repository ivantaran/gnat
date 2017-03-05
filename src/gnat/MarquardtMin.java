package gnat;

import java.util.Locale;

/**
 *
 * @author Taran
 */
public class MarquardtMin {
    private final static double EPS             = 1.0e-8;
    private final static double TOLERANCE       = 1.0e-15;
    private final static double SCALE           = 0.5 * (Math.sqrt(5.0) + 1.0);
    private final static int    COUNT           = 100;
    private final static int    POSITION_SIZE   = 4;
    
//    public enum Result { 
//        MAR_OK(0), MAR_FAIL(1); 
//        
//        private final int value;
//        
//        private Result(int value) {
//            this.value = value;
//        }
//    }

//    int value, ok;
//    double scale = 0.5*(sqrt(5) + 1);
//    double **hi = (double **)mtx_create(s->n, s->n);
//    double **jcbn = (double **)mtx_create(s->n, s->len);

//    struct minmarset {
//        double *dx;
//        double *x;
//        double *data;
//        int len;
//        double xmean;
//        double xstd;
//        int n;
//        double fmin;
//        double eps;
//        double count;
//        double (*fnc)(double* args, double t, int n);
//    };

    private void jdfprod(double jcbn[][], double delta[], double jdf[][]) {
        for (int i = 0; i < POSITION_SIZE; i++) {
            jdf[i][0] = 0;
            for (int j = 0; j < delta.length; j++) {
                jdf[i][0] += jcbn[i][j] * delta[j];
            }
        }
    }

    private void jtoa(double jcbn[][], double a[][], double lam) {
        Blas.sqr(jcbn, a);
        for (int i = 0; i < POSITION_SIZE; i++) {
            a[i][i] += lam * a[i][i];
        }
    }

    
    private double xstd(double mean, int len) {
        double std = 0;
        double dq = 0;

        for (int i = 0; i < len; ++i) {
            dq   = i - mean;
            dq  *= dq;
            std += dq;
        }
        std = (len > 1) ? Math.sqrt(std / (len - 1)) : 0.0;

        return std;
    }
    
    public boolean exec(CalcObject co) {
        int i;
        double fmn, fmnl, f1;
        boolean result = true;
        boolean ok;
        int len = co.getLength();
        int k[] = new int[1];
        double lam = 1e-3;

        double jcbn[][] = new double[POSITION_SIZE][len];
        double delta[] = new double[len];
        double h[][] = new double[POSITION_SIZE][POSITION_SIZE];
        double g[][] = new double[POSITION_SIZE][1];
        double r[] = new double[POSITION_SIZE];
        double x[] = new double[POSITION_SIZE];
        double mx[][] = new double[POSITION_SIZE][1];

        fmnl = co.sse(co.getPosition());
        fmn = fmnl;
        f1 = fmnl;
        System.out.printf(Locale.ROOT, "sse: %f\n\n", fmn);
        
        for (i = 0; i < COUNT; i++) {
            co.copyJacobianAndDelta(jcbn, delta);
            jtoa(jcbn, h, lam);
            jdfprod(jcbn, delta, g);

            result = Svd.svd_solver(h, g, mx, r, POSITION_SIZE, POSITION_SIZE, 1, k, 0);
            for (int j = 0; j < POSITION_SIZE; j++) {
                x[j] = mx[j][0];
            }

            for (int j = 0; j < POSITION_SIZE; j++) {
                System.out.println(x[j]);
            }

            if (!result) {
                System.out.println("SVD failed");
                break;
            }

            Blas.add(co.getPosition(), x, x);
            f1 = co.sse(x);
            System.out.printf(Locale.ROOT, "sse[%s]: %f\n\n", i, f1);
            
            if (f1 <= fmnl) {
                lam /= SCALE;
                System.arraycopy(x, 0, co.getPosition(), 0, Math.min(x.length, co.getPosition().length));
                fmn = f1;
                ok = true;
            }
            else {
                lam *= SCALE;
                ok = false;
            }

            if (ok) {
                if (Math.abs(fmnl - fmn) < EPS * Math.abs(fmn) + TOLERANCE) {
                    break;            
                }
                fmnl = fmn;            
            }
        }
        
        if (i >= COUNT) {
            result = false;
        }
        
        System.out.println("");
        System.out.printf(Locale.ROOT, "sse[%d]: %f\n\n", i, fmnl);
        
        for (int j = 0; j < POSITION_SIZE; j++) {
            System.out.println(x[j]);
        }
        
        return result;
    }
}
