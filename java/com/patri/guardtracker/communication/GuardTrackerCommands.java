package com.patri.guardtracker.communication;

import java.util.ArrayList;

/**
 * Created by patri on 25/06/2016.
 * Class representing the commands that can be sent to GuardTracker.
 * Class where the commands are build.
 * This class is a collection to retain commands that can not be sent to GuardTacker devices because they are out of range.
 */
public class GuardTrackerCommands extends ArrayList<byte []> {
    public enum CommandValues {
        CLEAN_E2PROM,               // 0 - Clean all data in e2prom (reset the equipment)
        CHANGE_OWNER,               // 1 - Modify mobile number registered in equipment
        REF_POSITION,               // 2 - Acquire new GPS coordinates and set them as equipment's reference position
        ALARM_INTERVAL,             // 3 - Interval time that sms are sent in tracking mode
        REF_TIME_TRACKING,          // 4 - ?????
        TEMPERATURE_BOUNDARIES,     // 5 - Temperature boundaries (low and high levels)
        MONITORING_GPS_BOUNDARIES,  // 6 - GPS boundaries to determine different position in monitoring mode
        TRACKING_GPS_BOUNDARIES,    // 7 - GPS boundaries to determine different position in tracking mode
        TILT_DIS,                   // 8 - Disable accelerometer
        TILT_ENA,                   // 9 - Enable accelerometer
        SIM_BALANCE_THRESHOLD,      // 10- Modify balance value threshold
        RETURN_APP_STATE,           // 11- Return internal app state
        RTC_TIME_INTERVAL,          // 12- Modify the period for monitoring
        RETURN_INFO,                // 13- Return app collected info since date
        DELETE_LOGS,                // 14- Delete logs between dates
        WAKE_SENSORS,               // 15- Ena/Dis wake sensors: accelerometer, rtc, ble. The wake_by_manual and wake_by_jtag are allways enabled
        RST_REF_POS,                // 16- Reset reference location
        RST_OWNER,                  // 17- Reset owner phonenumber
        READ_LOGS,                  // 18- Read logs from device (don't delete them)
        DEL_LOGS,                   // 19- Delete logs
        SDCARD_INFO,                // 20- Get SDCard info (disk capacity, disk space in use, total files in disk)
        TEST_LONG_MSG,              // 21- Test long message (????)
        TEST_SEND,                  // 22- Test send message (????)
        READ_MONITORING_CFG,        // 23- Read monitoring configuration
        READ_TRACKING_CFG,          // 24- Read tracking configuration
        READ_VIGILANCE_CFG,         // 25- Read vigilance configuration
        READ_WAKE_SENSORS_STATE,    // 26- Read wake sensors configuration
        READ_DEVICE_PHONE_NUMBER,   // 27- Read phone number of guard tracker device
        READ_REF_POS,               // 28- Read reference position
        ADD_SECONDARY_CONTACT,      // 29- Add new contact to device to use in sending alerts
        DELETE_SECONDARY_CONTACT,   // 30- Delete contact from device
        READ_SECONDARY_CONTACT,     // 31- Read secondary contacts
        WRITE_MONITORING_CFG,       // 32- Write monitoring configuration (one per write, parametrized)
        READ_OWNER,                 // 33- Read owner phone number
        STOP_PENDING_CMD            // 34- Cancel pending operation (any pending operation)

    }
    public enum CmdResValues {
        CMD_RES_OK('1'),
        CMD_RES_KO('0'),
        CMD_RES_PEND('2');

        private int r;
        CmdResValues(int v) { r = v; }
        public int value() { return r; }
    }

//    private byte [][] cmds = {
//            {(byte)CommandValues.CLEAN_E2PROM.ordinal()},
//            {(byte)CommandValues.CHANGE_OWNER.ordinal()},
//            {(byte)CommandValues.REF_POSITION.ordinal()},
//            {(byte)CommandValues.ALARM_INTERVAL.ordinal()/*+args*/},
//            {(byte)CommandValues.REF_TIME_TRACKING.ordinal()},
//            {(byte)CommandValues.CLEAN_E2PROM.ordinal()},
//            {(byte)CommandValues.CLEAN_E2PROM.ordinal()},
//            {(byte)CommandValues.CLEAN_E2PROM.ordinal()},
//            {(byte)CommandValues.CLEAN_E2PROM.ordinal()},
//            {(byte)CommandValues.CLEAN_E2PROM.ordinal()},
//            {(byte)CommandValues.CLEAN_E2PROM.ordinal()},
//            {(byte)CommandValues.CLEAN_E2PROM.ordinal()},
//            {(byte)CommandValues.CLEAN_E2PROM.ordinal()},
//            {(byte)CommandValues.CLEAN_E2PROM.ordinal()},
//            {(byte)CommandValues.CLEAN_E2PROM.ordinal()},
//            {(byte)CommandValues.CLEAN_E2PROM.ordinal()},
//            {(byte)CommandValues.CLEAN_E2PROM.ordinal()},
//            {(byte)CommandValues.CLEAN_E2PROM.ordinal()},
//            {(byte)CommandValues.CLEAN_E2PROM.ordinal()},
//            {(byte)CommandValues.CLEAN_E2PROM.ordinal()},
//            {(byte)CommandValues.CLEAN_E2PROM.ordinal()},
//            {(byte)CommandValues.CLEAN_E2PROM.ordinal()},
//            {(byte)CommandValues.CLEAN_E2PROM.ordinal()},
//            {(byte)CommandValues.CLEAN_E2PROM.ordinal()},
//            {(byte)CommandValues.CLEAN_E2PROM.ordinal()},
//            {(byte)CommandValues.CLEAN_E2PROM.ordinal()},
//
//    };

    public static byte[] readLogs() {
        final byte[] cmd = {
                2,                                          // payload len
                (byte) CommandValues.READ_LOGS.ordinal(),   // cmd id
                0                                           // all logs
        };
        return cmd;
    }
    public static byte[] deleteLogs() {
        final byte[] cmd = {
                1,                                          // payload len
                (byte) CommandValues.DELETE_LOGS.ordinal()  // cmd id
        };
        return cmd;
    }
    public static byte[] setWakeSensors(short sensorsBitmask) {
        final byte [] cmd = {
                3,                                              // payload len
                (byte) CommandValues.WAKE_SENSORS.ordinal(),    // cmd id
                (byte) (sensorsBitmask >> 8),                   // Hight sensors: no sensors
                (byte) (sensorsBitmask >> 0)                    // Low sensors. Bitmsk: ble | rtc | acc
        };
        return cmd;
    }
    public static byte[] resetRefPos() {
        final byte[] cmd = {
                1,                                              // payload len
                (byte) CommandValues.RST_REF_POS.ordinal()      // cmd id
        };
        return cmd;
    }
    public static byte[] cancelResetRefPos(byte pendingCmd) {
        final byte[] cmd = {
                2,                                              // payload len
                (byte) CommandValues.STOP_PENDING_CMD.ordinal(),// cmd id
                pendingCmd                                      // pending cmd id
        };
        return cmd;
    }
    public static byte[] appState() {
        final byte[] cmd = {
                1,                                              // payload len
                (byte) CommandValues.RETURN_APP_STATE.ordinal() // cmd id
        };
        return cmd;
    }
    public static byte[] changeOwner(String newOwner) {
        int numberOfDigits = newOwner.length();
        int payloadLen = 1 + 1 + numberOfDigits;
        byte[] cmd = new byte[1 + payloadLen];
        cmd[0] = (byte) payloadLen;                             // payload len
        cmd[1] = (byte) CommandValues.RST_OWNER.ordinal();      // cmd id
        cmd[2] = (byte) numberOfDigits;                         // number of digits
        byte [] phoneBytes = newOwner.getBytes();
        System.arraycopy(phoneBytes, 0, cmd, 3, newOwner.length());
        return cmd;
    }
    public static byte[] readOwner() {
        final byte [] cmd = { 1, (byte)CommandValues.READ_OWNER.ordinal() };
        return cmd;
    }
    public static byte[] readRefPos() {
        final byte [] cmd = { 1, (byte)CommandValues.READ_REF_POS.ordinal() };
        return cmd;
    }
    public static byte[] readWakeSensors() {
        final byte [] cmd = { 1, (byte)CommandValues.READ_WAKE_SENSORS_STATE.ordinal() };
        return cmd;
    }
    public static byte[] cleanE2prom() {
        final byte [] cmd = { 1, (byte)CommandValues.CLEAN_E2PROM.ordinal() };
        return cmd;
    }
    public static byte[] addContact(String contact) {
        int numberOfDigits = contact.length();
        int payloadLen = 1 + 1 + numberOfDigits; // cmd_id + num_of_symbols + phone_number
        byte [] cmd = new byte[1 + payloadLen];
        cmd[0] = (byte) payloadLen;
        cmd[1] = (byte) CommandValues.ADD_SECONDARY_CONTACT.ordinal();
        cmd[2] = (byte) contact.length();
        byte [] phoneBytes = contact.getBytes();
        System.arraycopy(phoneBytes, 0, cmd, 3, contact.length());
        return cmd;
    }
    public static byte[] deleteContact(String contact) {
        int numberOfDigits = contact.length();
        int payloadLen = 1 + 1 + numberOfDigits; // cmd_id + num_of_symbols + phone_number
        byte [] cmd = new byte[1 + payloadLen];
        cmd[0] = (byte) payloadLen;
        cmd[1] = (byte) CommandValues.DELETE_SECONDARY_CONTACT.ordinal();
        cmd[2] = (byte) contact.length();
        byte [] phoneBytes = contact.getBytes();
        System.arraycopy(phoneBytes, 0, cmd, 3, contact.length());
        return cmd;
    }
    public static byte[] readSecondaryContact(int ordinal) {
        final byte [] cmd = { 2, (byte)CommandValues.READ_SECONDARY_CONTACT.ordinal(), (byte)ordinal };
        return cmd;
    }
    public static byte[] readMonitoringConfig() {
        final byte [] cmd = { 1, (byte)CommandValues.READ_MONITORING_CFG.ordinal() };
        return cmd;
    }
    public static byte[] readTrackingConfig() {
        final byte [] cmd = { 1, (byte)CommandValues.READ_TRACKING_CFG.ordinal() };
        return cmd;
    }
    public static byte[] readVigilanceConfig() {
        final byte [] cmd = { 1, (byte)CommandValues.READ_VIGILANCE_CFG.ordinal() };
        return cmd;
    }
    public static byte[] readDevicePhoneNumber() {
        final byte [] cmd = { 1, (byte)CommandValues.READ_DEVICE_PHONE_NUMBER.ordinal() };
        return cmd;
    }
    public static byte[] writeMonitoringConfig(int item, Object ... args) {
        byte [] cmd = null;
        int payloadLen = 0;
        switch(item) {
            case 0: // Time to wakeup (minute of day)
            case 1: // Period (in minutes)
                payloadLen = 5;
                cmd = new byte[payloadLen];
                cmd[3] = (byte)(((Integer)args[0]).intValue() >> 8 & 0xFF);
                cmd[4] = (byte)(((Integer)args[0]).intValue() >> 0 & 0xFF);
                break;
            case 2: // Sms criteria (this command is inconsequential)
                payloadLen = 4;
                cmd = new byte[payloadLen];
                cmd[3] = (byte)(((Integer)args[0]).intValue() >> 0 & 0xFF);
                break;
            case 3: // Gps threshold
                payloadLen = 11;
                cmd = new byte[payloadLen];
                cmd[3] = (byte)(((Integer)args[0]).intValue() >> 24 & 0xFF);
                cmd[4] = (byte)(((Integer)args[0]).intValue() >> 16 & 0xFF);
                cmd[5] = (byte)(((Integer)args[0]).intValue() >> 8 & 0xFF);
                cmd[6] = (byte)(((Integer)args[0]).intValue() >> 0 & 0xFF);
                cmd[7] = (byte)(((Integer)args[1]).intValue() >> 24 & 0xFF);
                cmd[8] = (byte)(((Integer)args[1]).intValue() >> 16 & 0xFF);
                cmd[9] = (byte)(((Integer)args[1]).intValue() >> 8 & 0xFF);
                cmd[10] = (byte)(((Integer)args[1]).intValue() >> 0 & 0xFF);
                break;
            case 4: // Gps fov this command is inconsequential)
                payloadLen = 4;
                cmd = new byte[payloadLen];
                cmd[3] = (byte)(((Integer)args[0]).intValue() >> 0 & 0xFF);
                break;
            case 5: // Gps timeout
                payloadLen = 4;
                cmd = new byte[payloadLen];
                cmd[3] = (byte)(((Integer)args[0]).intValue() >> 0 & 0xFF);
                break;
            case 6: // Temp high + temp low
                payloadLen = 5;
                cmd = new byte[payloadLen];
                cmd[3] = (byte)(((Integer)args[0]).intValue() >> 0 & 0xFF);
                cmd[4] = (byte)(((Integer)args[1]).intValue() >> 0 & 0xFF);
                break;
            case 7: // Sim balance
                payloadLen = 5;
                cmd = new byte[payloadLen];
                cmd[3] = (byte)(((Integer)args[0]).intValue() >> 8 & 0xFF);
                cmd[4] = (byte)(((Integer)args[0]).intValue() >> 0 & 0xFF);
                break;
            default:;
        }
        cmd[0] = (byte) payloadLen;
        cmd[1] = (byte) CommandValues.WRITE_MONITORING_CFG.ordinal();
        cmd[2] = (byte) item;

        return cmd;
    }
    // Others...

}
