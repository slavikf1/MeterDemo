package com.sharper.meter.demo;

import com.fazecast.jSerialComm.SerialPort;
import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Meter {

    private final SerialPort port;
    private final int serial;

    Meter(String name, String serial){
        this.port = SerialPort.getCommPort(name);
        this.serial = Integer.parseInt(serial);
    }

        //Senging string 1-Byte command and getting a response. Works with already initialized port.
    private String getResponse(String message, int length) throws Exception{

        String returnedMessage = null;
        ByteBuffer outBuffer = ByteBuffer.allocate(7);
        outBuffer.putInt(serial);
        outBuffer.put(Hex.decodeHex(message));
        outBuffer.position(0);

        byte[] crcBase = new byte[5];
        outBuffer.get(crcBase,0,5);

        byte[] crc16 = crc16(crcBase); //base array to get CRC16 value
        outBuffer.put(crc16);

        //initSerialPort();
        port.writeBytes(outBuffer.array(), 7);
        byte[] buffer = new byte[length];
        Thread.sleep(100);
        port.readBytes(buffer, length);
        byte[] response = Arrays.copyOfRange(buffer, 5, length-2);
        returnedMessage = Hex.encodeHexString(response);
        //port.closePort();
        return  returnedMessage;
    }

    //to get all Readings - see Readings class.
    public Readings getReadings() throws Exception {

        initSerialPort();
        int serialNum = Integer.valueOf(getResponse("2F",11),16);
        String readings = getResponse("27",23);
        float day = Float.parseFloat(readings.substring(0,8))/100;
        float night = Float.parseFloat(readings.substring(8,16))/100;
        String instant = getResponse("63",14);
        float voltage = Float.parseFloat(instant.substring(0,4))/10;
        float current = Float.parseFloat(instant.substring(4,8))/100;
        float power = Float.parseFloat(instant.substring(8, 14))/1000;
        Readings result = new Readings(serialNum,day,night,current,power,voltage);
        port.closePort();
        return result;
    }

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
        initSerialPort();
        port.closePort();
        return Integer.valueOf((this.getResponse("2F", 11)),16);
    }

    private void initSerialPort() throws Exception {

        port.openPort();
        port.setParity(SerialPort.NO_PARITY);
        port.setNumStopBits(SerialPort.ONE_STOP_BIT);
        port.setNumDataBits(8);
        port.setBaudRate(9600);

        //Preparing a wake up call - 2F - request Meter's number to never recieve;
            ByteBuffer wakeupBuffer = ByteBuffer.allocate(7);
            wakeupBuffer.putInt(serial);
            wakeupBuffer.put(Hex.decodeHex("2F"));
            wakeupBuffer.position(0);


            byte[] crcBase = new byte[5];
            wakeupBuffer.get(crcBase,0,5);

            byte[] crc16 = crc16(crcBase); //base array to get CRC16 value
            wakeupBuffer.put(crc16);

            //Waking up
            port.writeBytes(wakeupBuffer.array(), 7);
            Thread.sleep(200); //It takes long to wake up after 1st command sent;
            //flushing buffer
            port.closePort();
            port.openPort();
    }

}
