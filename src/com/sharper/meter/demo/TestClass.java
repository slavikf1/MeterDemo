package com.sharper.meter.demo;

public class TestClass {

    public static void main(String[] args) {
        String comPort = "ttyUSB0";
        //String comPort = "COM3";
        String serial = "39037291";

        Readings readings = null;



        try {
            Meter meter = new Meter(comPort, serial);
            System.out.println("Trying readings: ");
            readings = meter.getReadings();
        }

        catch (Exception e){
            e.printStackTrace();
        }

        System.out.println(readings.toString());

    }
}
