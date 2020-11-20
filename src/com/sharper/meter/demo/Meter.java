package com.sharper.meter.demo;

import jssc.SerialPort;
import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Meter {

    private final SerialPort port;
    private final int serial;

    Meter(String name, String serial){
        this.port = new SerialPort(name);
        this.serial = Integer.parseInt(serial);
    }

    //sending a message and returning a response
    public String sendReturn(String message, int lengh) throws Exception {

        String returnedMessage = null;
        ByteBuffer outBuffer = ByteBuffer.allocate(7);
        outBuffer.putInt(serial);
        outBuffer.put(Hex.decodeHex(message));

        byte[] crcBase = new byte[5];
        outBuffer.get(0, crcBase, 0,5);

        byte[] crc16 = crc16(crcBase); //base array to get CRC16 value
        outBuffer.put(crc16);

        port.openPort(); //opening serialport
        port.purgePort(SerialPort.PURGE_TXCLEAR | SerialPort.PURGE_RXCLEAR);
        port.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE); //setting parameters
        //port.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT); //setting parameters
        port.writeBytes(outBuffer.array());
        //Thread.sleep(50);
        port.writeBytes(outBuffer.array());

        byte[] response = Arrays.copyOfRange(port.readBytes(lengh,100),5,lengh-2);
        returnedMessage = Hex.encodeHexString(response);

        port.closePort();
        return  returnedMessage;
    }

    //to work with opened port
    private String getResponse(String message, int length) throws Exception{

        String returnedMessage = null;
        ByteBuffer outBuffer = ByteBuffer.allocate(7);
        outBuffer.putInt(serial);
        outBuffer.put(Hex.decodeHex(message));

        byte[] crcBase = new byte[5];
        outBuffer.get(0, crcBase, 0,5);

        byte[] crc16 = crc16(crcBase); //base array to get CRC16 value
        outBuffer.put(crc16);

        port.purgePort(SerialPort.PURGE_TXCLEAR | SerialPort.PURGE_RXCLEAR);
        port.writeBytes(outBuffer.array());
        Thread.sleep(50);
        port.writeBytes(outBuffer.array());

        byte[] response = Arrays.copyOfRange(port.readBytes(length,100),5,length-2);
        returnedMessage = Hex.encodeHexString(response);
        return  returnedMessage;
    }

    //to get all Readings
    public Readings getReadings() throws Exception {

        port.openPort(); //opening serialport
        port.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE); //setting parameters

        int serialNum = Integer.valueOf(getResponse("2F",11),16);
        float day = Float.parseFloat(getResponse("27",23).substring(0,8))/100;
        float night = Float.parseFloat(getResponse("27", 23).substring(8,16))/100;
        float voltage = Float.parseFloat(getResponse("63", 14).substring(0,4))/10;
        float current = Float.parseFloat(getResponse("63", 14).substring(4,8))/100;
        float power = Float.parseFloat(getResponse("63", 14).substring(8))/1000;

        Readings result = new Readings(serialNum,day,night,current,power,voltage);
        port.closePort();

        return result;
    }

    //get values of meter
    public float[] getValues() throws Exception{
        String response = this.sendReturn("27", 23);
        float day = Float.parseFloat(response.substring(0,8))/100;
        float night = Float.parseFloat(response.substring(8,16))/100;

        float[] result = new float[2];
        result[0] = day;
        result[1] = night;

        return result;

    }

//    public void getUIP() throws Exception{
//        String response = this.sendReturn("63", 14);
//
//        float voltage = Float.parseFloat(response.substring(0,4))/10;
//        float current = Float.parseFloat(response.substring(4,8))/100;
//        float power = Float.parseFloat(response.substring(8))/1000;
//
//        System.out.println(
//                voltage + " " +
//                current + " " +
//                power);
//
//    }

    //modbus CRC16 calculation
    private byte[] crc16(byte[] message){
        int crc =  0xFFFF;

        for (int pos = 0; pos < message.length; pos++) {
            crc ^= (int)(0x00ff & message[pos]);  // FIX HERE -- XOR byte into least sig. byte of crc

            for (int i = 8; i != 0; i--) {    // Loop over each bit
                if ((crc & 0x0001) != 0) {      // If the LSB is set
                    crc >>= 1;                    // Shift right and XOR 0xA001
                    crc ^= 0xA001;
                }
                else                            // Else LSB is not set
                    crc >>= 1;                    // Just shift right
            }
        }

        byte[] result = new byte[2];
        result[0] = (byte)(crc + (crc << 8)&0xFF );
        result[1] = (byte)((crc >>> 8)&0xFF);
        return result;
    }

    //get meter's Serial number
    public int getSerialNum() throws Exception{
        return Integer.valueOf((this.sendReturn("2F", 11)),16);
        //return this.sendReturn("2F", 11);
    }





}
