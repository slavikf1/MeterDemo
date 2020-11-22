package com.sharper.meter.demo;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TestClass {

    public static void main(String[] args) {
        String comPort = "ttyUSB0";
        //String comPort = "COM3";
        String serial = "39037291";
        SimpleDateFormat format = new SimpleDateFormat("mm-ss-SS");
        Readings readings = null;



        try {
            Meter meter = new Meter(comPort, serial);
            Date lastDate = new Date();
            for (int i = 0; i < 3; i++){
                System.out.println("\nTrying readings: ");
                readings = meter.getReadings();

                System.out.println("tame taken: " + (readings.getDate().getTime() - lastDate.getTime()) + "\n");
                lastDate = readings.getDate();
                System.out.println(readings);
            }
            }

        catch (Exception e){
            e.printStackTrace();
        }

    }
}
