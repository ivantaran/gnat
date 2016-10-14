package gnat;

/**
 *
 * @author Taran
 */
public class MarquardtMin {
    private final static double EPS     = 1.0e-8;
    private final static int    COUNT   = 100;
    
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
//    double lam = 1e-3;//1e-3;
//    double scale = 0.5*(sqrt(5) + 1);
//    double *x = malloc(s->n*sizeof(double));
//    double *r = malloc(s->n*sizeof(double));
//    double **g = (double **)mtx_create(s->n, 1);
//    double **mx = (double **)mtx_create(s->n, 1);
//    double **h = (double **)mtx_create(s->n, s->n);
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

//    private double fncsse(double *args, struct minmarset* s) {
//        int i;
//        double value;
//        double result = 0;
//        double t;
//
//        for (i = 0; i < s->len; ++i) {
//            t = ((double)(i) - s->xmean)/s->xstd;
//            value = s->data[i] - s->fnc(args, t, s->n);
//            result += value*value;
//        }
//        return result;
//    }
    
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
        double eps = EPS;
        double xmean = (double)(len - 1) * 0.5;

        double xstd = xstd(xmean, len);

        fmnl = co.sse();
        fmn = fmnl;
        System.out.println(fmn);
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
