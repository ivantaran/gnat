/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rinex;

import java.util.GregorianCalendar;

/**
 *
 * @author Taran
 */
public class GlonassNavData {
    private int number;
    private GregorianCalendar time;
    private double timeOffset;
    private double frequencyOffset;
    private double messageTime;
    private boolean suitability;
    private double[] state = new double[6];
    private double[] acceleration = new double[3];
    private int frequencyChannelNumber;
    private double age;

    /**
     * @return the number
     */
    public int getNumber() {
        return number;
    }

    /**
     * @param number the number to set
     */
    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * @return the time
     */
    public GregorianCalendar getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(GregorianCalendar time) {
        this.time = time;
    }

    /**
     * @return the timeOffset
     */
    public double getTimeOffset() {
        return timeOffset;
    }

    /**
     * @param timeOffset the timeOffset to set
     */
    public void setTimeOffset(double timeOffset) {
        this.timeOffset = timeOffset;
    }

    /**
     * @return the frequencyOffset
     */
    public double getFrequencyOffset() {
        return frequencyOffset;
    }

    /**
     * @param frequencyOffset the frequencyOffset to set
     */
    public void setFrequencyOffset(double frequencyOffset) {
        this.frequencyOffset = frequencyOffset;
    }

    /**
     * @return the messageTime
     */
    public double getMessageTime() {
        return messageTime;
    }

    /**
     * @param messageTime the messageTime to set
     */
    public void setMessageTime(double messageTime) {
        this.messageTime = messageTime;
    }

    /**
     * @return the suitability
     */
    public boolean isSuitability() {
        return suitability;
    }

    /**
     * @param suitability the suitability to set
     */
    public void setSuitability(boolean suitability) {
        this.suitability = suitability;
    }

    /**
     * @return the state
     */
    public double[] getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(double[] state) {
        this.state = state;
    }

    /**
     * @return the acceleration
     */
    public double[] getAcceleration() {
        return acceleration;
    }

    /**
     * @param acceleration the acceleration to set
     */
    public void setAcceleration(double[] acceleration) {
        this.acceleration = acceleration;
    }

    /**
     * @return the frequencyChannelNumber
     */
    public int getFrequencyChannelNumber() {
        return frequencyChannelNumber;
    }

    /**
     * @param frequencyChannelNumber the frequencyChannelNumber to set
     */
    public void setFrequencyChannelNumber(int frequencyChannelNumber) {
        this.frequencyChannelNumber = frequencyChannelNumber;
    }

    /**
     * @return the age
     */
    public double getAge() {
        return age;
    }

    /**
     * @param age the age to set
     */
    public void setAge(double age) {
        this.age = age;
    }
}
