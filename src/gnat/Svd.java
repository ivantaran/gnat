/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gnat;

/**
 *
 * @author Taran
 */
public final class Svd {
    
    private static void getColumn(double[][] m, double[] v, int index) {
        for (int i = 0; i < Math.min(v.length, m.length); i++) {
            v[i] = m[i][index];
        }
    }

    private static void setColumn(double[][] m, double[] v, int index) {
        for (int i = 0; i < Math.min(v.length, m.length); i++) {
            m[i][index] = v[i];
        }
    }

    private static void getRow(double[][] m, double[] v, int index) {
        for (int i = 0; i < Math.min(v.length, m[index].length); i++) {
            v[i] = m[index][i];
        }
    }

    private static void setRow(double[][] m, double[] v, int index) {
        for (int i = 0; i < Math.min(v.length, m[index].length); i++) {
            m[index][i] = v[i];
        }
    }

    private static void getHouseholder(double[] v, double[] up, double[] b, int l) {
        double c1 = 0.0; 
        double sd = 0.0;
        double c = Math.abs(v[l]);
        double p = 0.0;

        for (int i = l + 1; i < v.length; i++) {
            c = Math.max(Math.abs(v[i]), c);
        }

        if (c > 0.0) {
            c1 = 1.0 / c;
            
            for (int i = l; i < v.length; i++) {
                sd += v[i] * c1 * v[i] * c1;
            }
            
            p = sd;
            p = c * Math.sqrt(Math.abs(p));
            if (v[l] > 0.0) {
                p = -p;
            }
            up[0] = v[l] - p;
            b[0] = 1 / (p * up[0]);
        }
    }

    private static void setHouseholder(double[] v, double up, double b, double[] c, int l) {
        double s = c[l] * up;
        int len = Math.min(c.length, v.length);
        
        for (int i = l + 1; i < len; i++) {
            s += c[i] * v[i];
        }

        s *= b;
        c[l] += s * up;

        for (int i = l + 1; i < len; i++) {
            c[i] += s * v[i];
        }
    }

    private static void mto2d(double[][] m, double[][] b, double[] d, double[] e, int h, int w, int wb) {
        double[] up = Blas.getVector(1);
        double[] bb = Blas.getVector(1);

        double[] v = Blas.getVector(w);
        double[] s = Blas.getVector(w);
        double[] ups = Blas.getVector(w);
        double[] bbs = Blas.getVector(w);
        
        Blas.zero(ups);
        Blas.zero(bbs);
        Blas.zero(v);
        Blas.zero(s);

        for (int i = 0; i < w; i++) {
            if ((i < w - 1) || (h > w)) {
                getColumn(m, v, i);
                getHouseholder(v, up, bb, i);
                
                for (int j = i; j < w; j++) {
                    getColumn(m, s, j);
                    setHouseholder(v, up[0], bb[0], s, i);
                    setColumn(m, s, j);
                }

                for (int k = 0; k < wb; k++) {
                    getColumn(b, s, k);
                    setHouseholder(v, up[0], bb[0], s, i);
                    setColumn(b, s, k);
                }
            }

            if (i < w - 2) {
                getRow(m, v, i);
                getHouseholder(v, up, bb, i + 1);
                ups[i] = up[0];
                bbs[i] = bb[0];
                for (int j = i; j < h; j++) {
                    getRow(m, s, j);
                    setHouseholder(v, up[0], bb[0], s, i + 1);
                    if (j == i) {
                        for (int k = i + 2; k < w; k++) {
                            s[k] = v[k];
                        }
                    }
                    setRow(m, s, j);
                }
            }
        }
        
        if (w > 1) {
            for (int i = 1; i < w; i++) {
                d[i] = m[i][i];
                e[i] = m[i - 1][i];
            }
        }
        
        d[0] = m[0][0];
        e[0] = 0;
        
        for (int i = w - 1; i >= 0; i--) {
            if (i < w - 1) {
                getRow(m, v, i);
            }
            for (int k = 0; k < w; k++) {
                m[i][k] = 0.0;
            }
            m[i][i] = 1.0;
            if (i < w - 2) {
                for (int k = i; k < w; k++) {
                    getColumn(m, s, k);
                    setHouseholder(v, ups[i], bbs[i], s, i + 1);
                    setColumn(m, s, k);
                }
            }
        }

    }

    private static void gvd(double v1, double v2, double[] c, double[] s) {
        double a1 = Math.abs(v1);
        double a2 = Math.abs(v2);
        double w, q;

        if (a1 > a2) {
            w = v2 / v1;
            q = Math.sqrt(1 + w * w);
            c[0] = 1 / q;
            if (v1 < 0) c[0] = -c[0];
            s[0] = c[0] * w;
        }
        else
            if (v2 != 0) {
                w = v1 / v2;
                q = Math.sqrt(1 + w * w);
                s[0] = 1 / q;
                if (v2 < 0) s[0] = -s[0];
                c[0] = s[0] * w;
            }
            else {
                c[0] = 1;
                s[0] = 0;
            }
    }

    private static void gva(double[] v1, int i1, double[] v2, int i2 , double[] c, double[] s) {
        double a1 = Math.abs(v1[i1]);
        double a2 = Math.abs(v2[i2]);
        double w, q;

        if (a1 > a2) {
            w = v2[i2] / v1[i1];
            q = Math.sqrt(1.0 + w * w);
            c[0] = 1.0 / q;
            if (v1[i1] < 0.0) {
                c[0] = -c[0];
            }
            s[0] = c[0] * w;
            v1[i1] = a1 * q;
            v2[i2] = 0.0;
        }
        else {
            if (v2[i2] != 0.0) {
                w = v1[i1] / v2[i2];
                q = Math.sqrt(1.0 + w * w);
                s[0] = 1.0 / q;
                if (v2[i2] < 0.0) {
                    s[0] = -s[0];
                }
                c[0] = s[0] * w;
                v1[i1] = a2 * q;
                v2[i2] = 0.0;
            }
            else {
                c[0] = 1.0;
                s[0] = 0.0;
            }
        }
    }

    private static void gvt(double[] z1, int i1, double[] z2, int i2, double c, double s) {
        double w = z1[i1] * c + z2[i2] * s;
        z2[0] = -z1[i1] * s + z2[i2] * c;
        z1[0] = w;
    }

    private static void m2dtod1(double[][] m, double[] d, double[] e, int w, int k) {
        
        double[] cs = Blas.getVector(1);
        double[] sn = Blas.getVector(1);
        double[] h = Blas.getVector(1);
        
        for (int i = k - 1; k >= 0; --k) {
            if (i == k - 1) {
                gva(d, i, e, i + 1, cs, sn);
            }
            else
                gva(d, i, h, 0, cs, sn);

            if (i > 0) {
                h[0] = 0;
                gvt(e, i, h, 0, cs[0], sn[0]);
            }

            for (int j = 0; j < w; j++)
                gvt(m[j], i, m[j], k, cs[0], sn[0]);
        }
    }

    private static void m2dtod2(double[][] b, double[] d, double[] e, int wb, int k, int l) {
        
        double[] cs = Blas.getVector(1);
        double[] sn = Blas.getVector(1);
        double[] h = Blas.getVector(1);


        for (int i = l; i < k + 1; i++) {
            if (i == l)
                gva(d, i, e, i, cs, sn);
            else
                gva(d, i, h, 0, cs, sn);

            if (i < k) {
                h[0] = 0;
                gvt(e, i + 1, h, 0, cs[0], sn[0]);
            }

            for (int j = 0; j < wb; j++)
                gvt(cs, 0, sn, 0, b[i][j], b[l - 1][j]);
        }
    }

    private static void m2dtod3(double[][] m, double[][] b, double[] d, double[] e, int w, int wb, int k, int l) {
        double f, g, t;
        double[] cs = Blas.getVector(1);
        double[] sn = Blas.getVector(1);
        double[] h = Blas.getVector(1);

        f = ((d[k - 1] - d[k]) * (d[k - 1] + d[k]) +
             (e[k - 1] - e[k]) * (e[k - 1] + e[k])) / (2 * e[k] * d[k - 1]);

        if (Math.abs(f) > 1e10) 
            g = Math.abs(f);
        else
            g = Math.sqrt(1 + f * f);

        if (f >= 0)
            t = f + g;
        else
            t = f - g;
        f = ((d[l] - d[k]) * (d[l] + d[k]) + e[k] * (d[k - 1] / t - e[k])) / d[l];

        for (int i = l; i < k; i++) {
            if (i == l)
                gvd(f, e[i + 1], cs, sn);
            else
                gva(e, i, h, 0, cs, sn);
            gvt(d, i, e, i + 1, cs[0], sn[0]);
            h[0] = 0;
            gvt(h, 0, d, i + 1, cs[0], sn[0]);

            for (int j = 0; j < w; j++)
                gvt(m[j], i, m[j], i + 1, cs[0], sn[0]);

            gva(d, i, h, 0, cs, sn);
            gvt(e, i + 1, d, i + 1, cs[0], sn[0]);

            if (i < k - 1) {
                h[0] = 0;
                gvt(h, 0, e, i + 2, cs[0], sn[0]);
            }

            for (int j = 0; j < wb; j++)
                gvt(b[i], j, b[i + 1], j, cs[0], sn[0]);
        }
    }

    private static boolean mtx_2dtod(double[][] m, double[][] b, double[] d, double[] e, int h, int w, int wb) {
        int l = 0;
        double bmx;
        int niter = 0;
        int niterm = 10 * w;
        boolean elzero = false;
        boolean ok = true;

        bmx = d[0];

        if (w > 1) {
            for (int i = 1; i < w; i++) {
                bmx = Math.max(Math.abs(d[i]) + Math.abs(e[i]), bmx);
            }
        }

        for (int k = w - 1; k >= 0; k--) {
            niter = 0;
            if (k != 0) {
                do {
//                    if ((bmx + d[k]) - bmx == 0.0) { // TODO check double == 0
                        m2dtod1(m, d, e, w, k);
//                    }

                    for (int ll = k; ll >= 0; --ll) {
                        l = ll;
                        if (l == 0) {
                            elzero = false;
                            break;
                        }
                        else { 
                            if ((bmx - e[l]) - bmx == 0.0) { // TODO check double == 0
                                elzero = true;
                                break;
                            }
                            else {
                                if ((bmx + d[l - 1]) - bmx == 0.0) {// TODO check double == 0
                                    elzero = false;
                                }
                            }
                        }
                    }

                    if ((l > 0) && !elzero) {
                        m2dtod2(b, d, e, wb, k, l);
                    }
                    
                    if (l != k) {
                        m2dtod3(m, b, d, e, w, wb, k, l);
                        niter++;
                    }
                    else {
                        break;
                    }
                } while (niter <= niterm);
            }

            ok = (niter <= niterm);

            if (d[k] < 0.0) {
                d[k] = -d[k];
                for (int j = 0; j < w; j++) {
                    m[j][k] = -m[j][k];
                }
            }
        }

        return ok;
    }

    private static void mdsort(double[][] m, double[][] b, double[] d, int h, int w, int wb) {
        int k, index;
        double t;

        if (w < 2) return;

        index = 1;
        do {
            if (d[index] > d[index - 1]) {
                for (int i = 1; i < w; i++) {
                    t = d[i - 1];
                    k = i - 1;

                    for (int j = i; j < w; j++)
                        if (t < d[j]) {
                            t = d[j];
                            k = j;
                        }

                    if (k != i - 1) {
                        d[k] = d[i - 1];
                        d[i - 1] = t;

                        for (int j = 0; j < w; j++) {
                            t = m[j][k];
                            m[j][k] = m[j][i - 1];
                            m[j][i - 1] = t;
                        }

                        for (int j = 0; j < wb; j++) {
                            t = b[k][j];
                            b[k][j] = b[i - 1][j];
                            b[i - 1][j] = t;
                        }
                    }
                }
                index = 1;
            }
            else 
                index++;
        }
        while (index < w);
    }

    public static void svd_analyse(double[][] m, double[][] b, double[] d, double[][] x, double[] r, int h, int w, int wb, int[] kn, double frac) {
        int kk;
        double eps = 1e-15;
        double sinmax = 0;
        double sinmin, s1;

        frac = Math.abs(frac);
        if (frac < eps) frac = eps;

        for (int i = 0; i < w; i++)
            sinmax = Math.max(sinmax, d[i]);

        sinmin = sinmax*frac;

        kk = w;
        for (int i = 0; i < w; i++) 
            if (d[i] <= sinmin) {
                kk = i;
                break;
            }

        for (int i = 0; i < h; i++)
            if (i < kk) {
                s1 = 1 / d[i];

                for (int j = 0; j < wb; j++)
                    b[i][j] *= s1;
            }
            else
                for (int j = 0; j < wb; j++) {
                    if (i == kk)
                        r[j] = b[i][j] * b[i][j];
                    else
                        r[j] += b[i][j] * b[i][j];

                    if (i < w) b[i][j] = 0;
                }

        for (int i = 0; i < w; i++)
            for (int j = 0; j < wb; j++) {
                x[i][j] = 0;
                for (int k = 0; k < w; k++)
                    x[i][j] += m[i][k]*b[k][j];
            }
        kn[0] = kk;
    }

    public static void svm(double[][] m, double[][] b, double[] d, double lam, double[] x1, double[] x2, int h, int w, int[] kn, double frac) {
        int i, k, kk;
        double eps = 1e-15;
        double sinmax = 0;
        double sinmin, g, den1, den2;
        double lam2 = lam * lam;
        double lamp = lam * 0.1;
        double lamp2 = lamp * lamp;

        double[] p1 = Blas.getVector(w);
        double[] p2 = Blas.getVector(w);

        frac = Math.abs(frac);
        if (frac < eps) frac = eps;

        for (i = 0; i < w; i++)
            sinmax = Math.max(sinmax, d[i]);

        sinmin = sinmax * frac;

        kk = w;
        for (i = 0; i < w; i++) 
            if (d[i] <= sinmin) {
                kk = i;
                break;
            }

        for (i = 0; i < h; i++) {
            g = b[i][0];
            if (i < kk) {
                den1 = 1 / (d[i] * d[i] + lam2);
                den2 = 1 / (d[i] * d[i] + lamp2);
                p1[i] = g * d[i] * den1;
                p2[i] = g * d[i] * den2;
            }
            else {
                if (i < w) {
                    p1[i] = 0;
                    p2[i] = 0;
                }
            }
        }

        for (i = 0; i < w; i++) {
            x1[i] = 0;
            x2[i] = 0;

            for (k = 0; k < w; k++) {
                x1[i] += m[i][k] * p1[k];
                x2[i] += m[i][k] * p2[k];
            }
        }

        kn[0] = kk;
    }

    public static boolean mtx_mar(double[][] m, double[][] b, double lam, double[] x1, double[] x2, int h, int w, int[] kn, double frac) {
        boolean result;

        double[] d = Blas.getVector(w);
        double[] e = Blas.getVector(w);

        mto2d(m, b, d, e, h, w, 1);

        result = mtx_2dtod(m, b, d, e, h, w, 1);

        mdsort(m, b, d, h, w, 1);

    //    vcr_print(d, w);

        svm(m, b, d, lam, x1, x2, h, w, kn, frac);

        return result;
    }

    public static boolean svd_solver(double[][] m, double[][] b, double[][] x, double[] r, int h, int w, int wb, int[] kn, double frac) {
        boolean result;

        double[] d = Blas.getVector(w);
        double[] e = Blas.getVector(w);

//        Blas.save(m, "a.txt");
//        Blas.save(b, "b.txt");
        mto2d(m, b, d, e, h, w, wb);
//        Blas.save(d, "d.txt");
//        Blas.save(e, "e.txt");
//        Blas.save(m, "h.txt");
//        Blas.save(b, "q.txt");

        result = mtx_2dtod(m, b, d, e, h, w, wb);

//        mtx_print(m, h, w);
//        mtx_print(b, h, wb);
//        vcr_print(d, w);
//        vcr_print(e, w);

        mdsort(m, b, d, h, w, wb);

    //    mtx_print(m, h, w);
    //    mtx_print(b, h, wb);
    //    vcr_print(d, w);
    //    vcr_print(e, w);

        svd_analyse(m, b, d, x, r, h, w, wb, kn, frac);

        return result;
    }
    
}
