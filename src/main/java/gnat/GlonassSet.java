/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gnat;

/**
 *
 * @author Taran
 */
public class GlonassSet implements Cloneable{

    public static final int InitialLength = 12;
    public static final int VectorLength = 6;

    private double[] initial = new double[InitialLength];
    private double[] current = new double[VectorLength];
    private double[] derivatives = new double[VectorLength];
    private double stepTime = 1.0;
//        private double *msr_r;
//        private double len;

    /**
     * @return the initial
     */
    public double[] getInitial() {
        return initial;
    }

    /**
     * @param initial the initial to set
     */
    public void setInitial(double[] initial) {
        this.initial = initial;
    }

    /**
     * @return the current
     */
    public double[] getCurrent() {
        return current;
    }

    /**
     * @param current the current to set
     */
    public void setCurrent(double[] current) {
        this.current = current;
    }

    /**
     * @return the derivatives
     */
    public double[] getDerivatives() {
        return derivatives;
    }

    /**
     * @param derivatives the derivatives to set
     */
    public void setDerivatives(double[] derivatives) {
        this.derivatives = derivatives;
    }

    /**
     * @return the stepTime
     */
    public double getStepTime() {
        return stepTime;
    }

    /**
     * @param stepTime the stepTime to set
     */
    public void setStepTime(double stepTime) {
        this.stepTime = stepTime;
    }

    /**
     * @param stepTime the stepTime to set
     */
    public void setStepTimeInMillis(long stepTime) {
        this.stepTime = (double)stepTime * 0.001;
    }

    @Override
    public GlonassSet clone() {
        GlonassSet s;

        try {
            s = (GlonassSet)super.clone();
            s.setInitial(initial.clone());
            s.setCurrent(current.clone());
            s.setDerivatives(derivatives.clone());
        } catch (CloneNotSupportedException e) {
            System.out.println(e.getMessage());
            s = null;
        }

        return s;
    }

};
    
