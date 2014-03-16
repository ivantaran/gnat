/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rinex;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
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
    
    public String getTimeLine() {
        String value;
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
        sdf.setCalendar(time);
        value = sdf.format(time.getTimeInMillis());
        return value;
}
    
    public void save(String fileName, boolean append) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, append));

            bw.write(String.format("number                 %24d\n", number));
            bw.write(String.format("time                   %24s\n", getTimeLine()));
            bw.write(String.format("timeOffset             %24f\n", timeOffset));
            bw.write(String.format("frequencyOffset        %24f\n", frequencyOffset));
            bw.write(String.format("messageTime            %24f\n", messageTime));
            bw.write(String.format("suitability            %24b\n", suitability));
            bw.write(String.format("frequencyChannelNumber %24d\n", frequencyChannelNumber));
            bw.write(String.format("age                    %24f\n", age));
            
            for (int i = 0; i < state.length; i++) {
                bw.write(String.format("state[%d]               %24f\n", i, state[i]));
            }
            for (int i = 0; i < acceleration.length; i++) {
                bw.write(String.format("acceleration[%d]        %24f\n", i, acceleration[i]));
            }
            bw.write("\n");
            bw.close();
            
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }
}
