/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gnat;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

/**
 *
 * @author Taran
 */
public final class Blas {
    
    public static double[][] getMatrix(int h, int w) {
        return new double[h][w];
    }
    
    public static double[] getVector(int h) {
        return new double[h];
    }
    
    public static void zero(double[][] m) {
        int i, j;
        for (i = 0; i < m.length; i++)
            for (j = 0; j < m[0].length; j++) 
                m[i][j] = 0;
    }
    
    public static void zero(double[] v) {
        int i, j;
        for (i = 0; i < v.length; i++)
            v[i] = 0;
    }
    
    public static void fill(double[] v, double value) {
        int i, j;
        for (i = 0; i < v.length; i++)
            v[i] = value;
    }
    
    public static void add(double[][] m1, double[][] m2, double[][] mr) {
        int i, j;
        for (i = 0; i < m1.length; i++)
            for (j = 0; j < m1[0].length; j++) 
                mr[i][j] = m1[i][j] + m2[i][j];
    }
    
    public static void add(double[] v1, double[] v2, double[] vr) {
        int i;
        for (i = 0; i < v1.length; i++)
            vr[i] = v1[i] + v2[i];
    }
    
    public static void sub(double[] v1, double[] v2, double[] vr) {
        for (int i = 0; i < Math.min(Math.min(v1.length, v1.length), vr.length); i++) {
            vr[i] = v1[i] - v2[i];
        }
    }
    
    public static void mul(double[][] m1, double[][] m2, double[][] mr) {
        int i, j, k;
        for (j = 0; j < m2[0].length; j++)
            for (i = 0; i < m1.length; i++) {
                mr[i][j] = 0;
                for (k = 0; k < m2.length; k++) 
                    mr[i][j] += m1[i][k] * m2[k][j];
            }
    }
    
    public static void mul(double[] v1, double[] v2, double[] vr) {
        int i;
        for (i = 0; i < v1.length; i++)
            vr[i] = v1[i] * v2[i];
    }

    public static void mul(double[] v, double c, double[] vr) {
        int i;
        for (i = 0; i < v.length; i++)
            vr[i] = c * v[i];
    }

    public static void mul(double[] column, double[][] m, double[][] mr) {
        int i, j;
        for (i = 0; i < m.length; i++)
            for (j = 0; j < m[0].length; j++) 
                mr[i][j] = m[i][j] * column[i];
    }
    
    public static void mul(double[][] m, double[] v, double[] vr) {
        int i, k;
        for (i = 0; i < m.length; i++) {
            vr[i] = 0.0;
            for (k = 0; k < m[0].length; k++) 
                vr[i] += m[i][k] * v[k];
        }
    }

    public static void mul(double[][] m, double[] v, int iv, int length, double[] vr) {
        int i, k;
        for (i = 0; i < length; i++) {
            vr[i] = 0.0;
            for (k = 0; k < length; k++) 
                vr[i] += m[i][k] * v[iv + k];
        }
    }

    public static void tmul(double[] row, double[][] m, double[][] mr) {
        int i, j;
        for (i = 0; i < m[0].length; i++)
            for (j = 0; j < m.length; j++) 
                mr[i][j] = m[i][j] * row[j];
    }

    public static void ttmul(double[] v, double[][] m, double[][] mr) {
        int i, j;
        for (i = 0; i < m.length; i++)
            for (j = 0; j < m[0].length; j++) 
                mr[j][i] = m[i][j]*v[i];
    }
    
    
    public static void sqr(double[] v, double[][] m) {
    int i, j;
    for (j = 0; j < v.length; ++j)
        for (i = 0; i < v.length; ++i) {
            m[i][j] = v[i]*v[j];
        }
    }

    public static void sqr(double[][] m, double[][] mr) {
        int i, j, k;
        double[][] tmp = getMatrix(m.length, m.length);
        
        zero(mr);

        for (j = 0; j < m[0].length; j++) {
            for (i = 0; i < m.length; i++)
                for (k = 0; k < m.length; k++) 
                    tmp[k][i] = m[k][j] * m[i][j];

            add(mr, tmp, mr);
        }
    }
    
    public static void abs(double[] v, double[] vr) {
        int i;
        for (i = 0; i < v.length; i++)
            vr[i] = Math.abs(v[i]);
    }
    
    public static void trp(double[][] m, double[][] mr) {
        int i, j;
        for (j = 0; j < m[0].length; j++)
            for (i = 0; i < m.length; i++) 
                mr[j][i] = m[i][j];
    }
    
    public static int maxIndex(double[] v) {
        int i;
        int m = 0;

        for (i = 1; i < v.length; ++i) {
            if (v[m] < v[i]) {
                m = i;
            }
        }

        return m;
    }

    public static void copy(double[] v, double[] vr) {
        vr = v.clone();
    }

    public static boolean hol(double[][] m, double[][] mr) {
        int l, j, k;
        double tmp;

        zero(mr);

        for (k = 0; k < m[0].length; k++)
            for (j = k; j < m.length; j++) {
                tmp = 0.0;
                for (l = 0; l < k; l++) 
                    tmp += mr[k][l] * mr[j][l];
                
                mr[j][k] = m[j][k] - tmp;
                
                if (mr[j][k] <= 0 && j == k) {
                    return false;
                }
                
                if (j == k) 
                    mr[j][k] = Math.sqrt(mr[j][k]);
                else 
                    mr[j][k] = mr[j][k] / mr[k][k];
            }
        
        return true;
    }
    
    public static boolean inchol(double[][] m, double[][] mr) {
        int l, j, k;
        double tmp;

        zero(mr);

        for (k = 0; k < m[0].length; k++)
            for (j = k; j < m.length; j++) {
                tmp = 0.0;
                for (l = 0; l < k; l++) tmp += mr[k][l]*mr[j][l];
                mr[j][k] = m[j][k] - tmp;
                if (mr[j][k] <= 0 && j == k) {
    //                return false;
                    mr[j][k] = 1;
                    System.out.println("incomplete cholesky");
                }
                if (j == k) mr[j][k] = Math.sqrt(mr[j][k]);
                else mr[j][k] = mr[j][k] / mr[k][k];
            }
        return true;
    }

    public static boolean hinv(double[][] m, double[][] mr) {
        int i, j, k, l;
        int h = m.length;
        double[][] mh = getMatrix(h, h);

        if (!inchol(m, mh)) {
              return false;
        }

        for (i = 0; i < h; i++) {
            for (l = i; l < h; l++) {
                if (l == i) 
                    mr[l][h - 1] = 1 / mh[l][l];
                else {
                    mr[l][h - 1] = 0;
                    for (k = i; k < l; k++) 
                        mr[l][h - 1] -= mh[l][k] * mr[k][h - 1];
                    mr[l][h - 1] = mr[l][h - 1] / mh[l][l];
                }
            }
            for (l = h - 1; l >= i; l--) {
                if (i == h - 1) 
                    mr[l][i] = mr[l][h - 1] / mh[l][l];
                else {
                    mr[l][i] = mr[l][h - 1];
                    for (k = h - 1; k >= l + 1; k--) 
                        mr[l][i] -= mh[k][l] * mr[k][i];
                    mr[l][i] = mr[l][i] / mh[l][l];
                }
            }
        }

        for (j = 0; j < h; j++)
            for (i = 0; i < j; i++) 
                mr[i][j] = mr[j][i];

        return true;
    }
    
    public static void save(double[] v, String fileName) {
        String line;
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, false));
            
            for (int i = 0; i < v.length; i++) {
                line = String.format(Locale.ROOT, "%12.12f\n", v[i]);
                bw.write(line, 0, line.length());
            }
            
            bw.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }
    
    public static void save(double[][] m, String fileName) {
        String line;
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, false));
            
            for (double[] m1 : m) {
                for (int j = 0; j < m[0].length; j++) {
                    line = String.format(Locale.ROOT, "%12.18g ", m1[j]);
                    bw.write(line);
                }
                bw.write("\n");
            }
            
            bw.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

}
