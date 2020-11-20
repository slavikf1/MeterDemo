package com.sharper.meter.demo;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Readings {

    private int serialNum;
    private Date date;
    private float day;
    private float night;
    private float current;
    private float power;
    private float voltage;

    public Readings(int serialNum, float day, float night, float current, float power, float voltage) {
        this.serialNum = serialNum;
        this.date = new Date();
        this.day = day;
        this.night = night;
        this.current = current;
        this.power = power;
        this.voltage = voltage;
    }


    public int getSerialNum() {
        return serialNum;
    }

    public String getDateString() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return formatter.format(this.date);
    }

    public float getDay() {
        return day;
    }

    public float getNight() {
        return night;
    }

    public float getCurrent() {
        return current;
    }

    public float getPower() {
        return power;
    }

    public float getVoltage() {
        return voltage;
    }

    public String toString(){
        return serialNum + " on " + getDateString() + ": " + "day: " + day + " night: "+night + "\n" + "V: "+ voltage +
                "Cur: " + current +"P: "+ power;
    }
}
