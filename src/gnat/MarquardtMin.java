package gnat;

/**
 *
 * @author Taran
 */
public class MarquardtMin {
    private final static double EPS             = 1.0e-8;
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

//    int i, j, k;
//    int result = MAR_OK;
//    int value, ok;
//    double t = 1e-15;
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
        double fmn, fmnl, f1, f2;
        boolean result = true;
        int count = COUNT;
        int len = co.getLength();
        int k[] = new int[1];
        double eps = EPS;
        double xmean = (double)(len - 1) * 0.5;
        double lam = 1e-3;

        double xstd = xstd(xmean, len);
        double jcbn[][] = new double[POSITION_SIZE][len];
        double delta[] = new double[len];
        double h[][] = new double[POSITION_SIZE][POSITION_SIZE];
        double g[][] = new double[POSITION_SIZE][1];
        double r[] = new double[POSITION_SIZE];
        double x[] = new double[POSITION_SIZE];
        double mx[][] = new double[POSITION_SIZE][1];

        fmnl = co.sse();
        fmn = fmnl;
        System.out.println(fmn);
        
        co.copyJacobianAndDelta(jcbn, delta);
        jtoa(jcbn, h, lam);

        h[0][0] = 4;
        h[0][1] = 3;
        h[0][2] = 2;
        h[0][3] = 1;
        h[1][0] = 3;
        h[1][1] = 4;
        h[1][2] = 3;
        h[1][3] = 2;
        h[2][0] = 2;
        h[2][1] = 3;
        h[2][2] = 4;
        h[2][3] = 3;
        h[3][0] = 1;
        h[3][1] = 2;
        h[3][2] = 3;
        h[3][3] = 4;

        jdfprod(jcbn, delta, g);
//        Blas.save(jcbn, "j.txt");
        g[0][0] = 1;
        g[1][0] = 2;
        g[2][0] = 3;
        g[3][0] = 4;
        
        result = Svd.svd_solver(h, g, mx, r, POSITION_SIZE, POSITION_SIZE, 1, k, 0);
        for (int j = 0; j < POSITION_SIZE; j++) {
            x[j] = mx[j][0];
        }
        
        if (!result) {
            System.out.println("SVD failed");
//            for (int j = 0; j < POSITION_SIZE; j++) {
//                System.out.println(x[j]);
//            }
            return result;
            //TODO break;
        }
//        Blas.sub(x, x, r);
//        for (i = 0; i < s->count; ++i) {
//
//            jacobian(jcbn, s);
//            jtoa(jcbn, h, lam, s);
//            jdfprod(jcbn, g, s);
//
//            value = mtx_svd_solver(h, g, mx, r, s->n, s->n, 1, &k, 0);
//            for (j = 0; j < s->n; ++j)
//                x[j] = mx[j][0];
//
//            if (!value) {
//                result = MAR_SVD_ERR;
//                break;
//            }
//
//            vcr_dlt(s->x, x, x, s->n);
//
//            f1 = fncsse(x, s);
//
//            if (f1 <= fmnl) {
//                lam /= scale;
//                memcpy(s->x, x, s->n*sizeof(double));
//                fmn = f1;
//                ok = TRUE;
//            }
//            else {
//                lam *= scale;
//                ok = FALSE;
//            }
//
//            if (ok) {
//                if (fabs(fmnl - fmn) < s->eps*fabs(fmn) + t) 
//                    break;            
//                fmnl = fmn;
//            }
//        }
//    
//        if (i == s->count) result = MAR_FAILED;
//        s->fmin = fmn;
//            printf("i = % d; lam %g;\n", i, lam);
//        mtx_save(jcbn, s->n, s->len, "jcbn.txt");
//
//        mtx_free(h, s->n);
//        mtx_free(g, 1);
//        mtx_free(mx, 1);
//        mtx_free(jcbn, s->n);
//        free(x);
//        free(r);
        return result;
    }
}
