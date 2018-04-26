package com.patri.guardtracker.communication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by patri on 25/06/2016.
 * Class representing the commands that can be sent to GuardTracker.
 * Class where the commands are build.
 * This class is a collection to retain commands that can not be sent to GuardTacker devices because they are out of range.
 */
public class GuardTrackerCommands extends ArrayList<byte []> {
    public enum CommandValues {
        CLEAN_E2PROM(0),               // 0 - Clean all data in e2prom (reset the equipment)
        CHANGE_OWNER(1),               // 1 - Modify mobile number registered in equipment
        REF_POSITION(2),               // 2 - Acquire new GPS coordinates and set them as equipment's reference position
        ALARM_INTERVAL(3),             // 3 - Interval time that sms are sent in tracking mode
        REF_TIME_TRACKING(4),          // 4 - ?????
        TEMPERATURE_BOUNDARIES(5),     // 5 - Temperature boundaries (low and high levels)
        MONITORING_GPS_BOUNDARIES(6),  // 6 - GPS boundaries to determine different position in monitoring mode
        TRACKING_GPS_BOUNDARIES(7),    // 7 - GPS boundaries to determine different position in tracking mode
        TILT_DIS(8),                   // 8 - Disable accelerometer
        TILT_ENA(9),                   // 9 - Enable accelerometer
        SIM_BALANCE_THRESHOLD(10),      // 10- Modify balance value threshold
        RETURN_APP_STATE(11),           // 11- Return internal app state
        RTC_TIME_INTERVAL(12),          // 12- Modify the period for monitoring
        RETURN_INFO(13),                // 13- Return app collected info since date
        DELETE_LOGS(14),                // 14- Delete logs between dates
        WAKE_SENSORS(15),               // 15- Ena/Dis wake sensors: accelerometer, rtc, ble. The wake_by_manual and wake_by_jtag are allways enabled
        RST_REF_POS(16),                // 16- Reset reference location
        RST_OWNER(17),                  // 17- Reset owner phonenumber
        READ_LOGS(18),                  // 18- Read logs from device (don't deleteDeep them)
        DEL_LOGS(19),                   // 19- Delete logs
        SDCARD_INFO(20),                // 20- Get SDCard info (disk capacity, disk space in use, total files in disk)
        TEST_LONG_MSG(21),              // 21- Test long message (????)
        TEST_SEND(22),                  // 22- Test send message (????)
        READ_MONITORING_CFG(23),        // 23- Read monitoring configuration
        READ_TRACKING_CFG(24),          // 24- Read tracking configuration
        READ_VIGILANCE_CFG(25),         // 25- Read vigilance configuration
        READ_WAKE_SENSORS_STATE(26),    // 26- Read wake sensors configuration
        READ_DEVICE_PHONE_NUMBER(27),   // 27- Read phone number of guard tracker device
        READ_REF_POS(28),               // 28- Read reference position
        ADD_SECONDARY_CONTACT(29),      // 29- Add new contact to device to use in sending alerts
        DELETE_SECONDARY_CONTACT(30),   // 30- Delete contact from device
        READ_SECONDARY_CONTACT(31),     // 31- Read secondary contacts
        WRITE_MONITORING_CFG(32),       // 32- Write monitoring configuration (one per write, parametrized)
        READ_OWNER(33),                 // 33- Read owner phone number
        STOP_PENDING_CMD(34),           // 34- Cancel pending operation (any pending operation)
        WRITE_TRACKING_CFG(35),         // 35- Write tracking configuration (one per write, parametrized)
        WRITE_VIGILANCE_CFG(36);        // 36- Write vigilance configuration (one per write, parametrized)

        private int r;
        CommandValues(int cmdValue) { r = cmdValue; }
        public int value() { return r; }
        private static Map<Integer, CommandValues> map = new HashMap<>();
        static {
            for (CommandValues cv : CommandValues.values()) {
                map.put(cv.r, cv);
            }
        }
        public static CommandValues valueOf(int v) { return map.get(v); }
    }
    public enum CmdResValues {
        CMD_RES_OK('1'),
        CMD_RES_KO('0'),
        CMD_RES_PEND('2');

        private int r;
        CmdResValues(int v) { r = v; }
        public int value() { return r; }
        private static Map<Integer, CmdResValues> map = new HashMap<>();
        static {
            for (CmdResValues crv : CmdResValues.values()) {
                map.put(crv.r, crv);
            }
        }
        public static CmdResValues valueOf(int v) { return map.get(v); }
    }
    // The order of the items in the next enumerators are compromised with same order in Guardtracker code solution.
    public enum MonCfgItems {
        MON_CFG_TIME, MON_CFG_PERIOD, MON_CFG_SMS_CRITERIA, MON_CFG_GPS_THRESHOLD,
        MON_CFG_GPS_FOV, MON_CFG_GPS_TIMEOUT, MON_CFG_TEMP, MON_CFG_SIM_BALANCE
    }
    public enum TrackCfgItems {
        TRACK_CFG_SMS_CRITERIA, TRACK_CFG_GPS_THRESHOLD, TRACK_CFG_GPS_FOV, TRACK_CFG_GPS_TIMEOUT,
        TRACK_CFG_TIMEOUT_TRACKING, TRACK_CFG_TIMEOUT_PRE, TRACK_CFG_TIMEOUT_POST
    }
    public enum VigCfgItems {
        VIG_CFG_TILT_LEVEL, VIG_CFG_BLE_ADVERTISEMENT_PERIOD
    }

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
    public static byte[] writeMonitoringConfig(MonCfgItems item, Object ... args) {
        byte [] cmd = null;
        int payloadLen = 0;
        switch(item) {
            case MON_CFG_TIME: // Time to wakeup (minute of day)
            case MON_CFG_PERIOD: // Period (in minutes)
                payloadLen = 5;
                cmd = new byte[payloadLen];
                cmd[3] = (byte)(((Integer)args[0]).intValue() >> 8 & 0xFF);
                cmd[4] = (byte)(((Integer)args[0]).intValue() >> 0 & 0xFF);
                break;
            case MON_CFG_SMS_CRITERIA: // Sms criteria (this command is inconsequential)
                payloadLen = 4;
                cmd = new byte[payloadLen];
                cmd[3] = (byte)(((Integer)args[0]).intValue() >> 0 & 0xFF);
                break;
            case MON_CFG_GPS_THRESHOLD: // Gps threshold
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
            case MON_CFG_GPS_FOV: // Gps fov this command is inconsequential)
                payloadLen = 4;
                cmd = new byte[payloadLen];
                cmd[3] = (byte)(((Integer)args[0]).intValue() >> 0 & 0xFF);
                break;
            case MON_CFG_GPS_TIMEOUT: // Gps timeout
                payloadLen = 4;
                cmd = new byte[payloadLen];
                cmd[3] = (byte)(((Integer)args[0]).intValue() >> 0 & 0xFF);
                break;
            case MON_CFG_TEMP: // Temp high + temp low
                payloadLen = 5;
                cmd = new byte[payloadLen];
                cmd[3] = (byte)(((Integer)args[0]).intValue() >> 0 & 0xFF);
                cmd[4] = (byte)(((Integer)args[1]).intValue() >> 0 & 0xFF);
                break;
            case MON_CFG_SIM_BALANCE: // Sim balance
                payloadLen = 5;
                cmd = new byte[payloadLen];
                cmd[3] = (byte)(((Integer)args[0]).intValue() >> 8 & 0xFF);
                cmd[4] = (byte)(((Integer)args[0]).intValue() >> 0 & 0xFF);
                break;
            default:;
        }
        cmd[0] = (byte) payloadLen;
        cmd[1] = (byte) CommandValues.WRITE_MONITORING_CFG.ordinal();
        cmd[2] = (byte) item.ordinal();

        return cmd;
    }
    /* NOT TESTED - ENTIRE METHOD */
    public static byte[] writeTrackingConfig(TrackCfgItems item, Object ... args) {
        byte [] cmd = null;
        int payloadLen = 0;
        switch(item) {
            case TRACK_CFG_GPS_THRESHOLD: // Gps threshold
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
            case TRACK_CFG_SMS_CRITERIA: // Sms criteria (this command is inconsequential)
            case TRACK_CFG_GPS_FOV: // Gps fov this command is inconsequential)
            case TRACK_CFG_TIMEOUT_POST: // Timeout post
            case TRACK_CFG_TIMEOUT_PRE: // Timeout pre
            case TRACK_CFG_GPS_TIMEOUT: // Gps timeout
            case TRACK_CFG_TIMEOUT_TRACKING: // Timeout tracking
                payloadLen = 4;
                cmd = new byte[payloadLen];
                cmd[3] = (byte)(((Integer)args[0]).intValue() >> 0 & 0xFF);
                break;
            default:;
        }
        cmd[0] = (byte) payloadLen;
        cmd[1] = (byte) CommandValues.WRITE_TRACKING_CFG.ordinal();
        cmd[2] = (byte) item.ordinal();

        return cmd;
    }
    /* NOT TESTED - ENTIRE METHOD */
    public static byte[] writeVigilanceConfig(VigCfgItems item, Object ... args) {
        byte [] cmd = null;
        int payloadLen = 0;
        switch(item) {
            case VIG_CFG_TILT_LEVEL: // Tilt level
                payloadLen = 4;
                cmd = new byte[payloadLen];
                cmd[3] = (byte)(((Integer)args[0]).intValue() >> 0 & 0xFF);
                break;
            case VIG_CFG_BLE_ADVERTISEMENT_PERIOD: // Ble advertisement period
                payloadLen = 5;
                cmd = new byte[payloadLen];
                cmd[3] = (byte)(((Integer)args[0]).intValue() >> 8 & 0xFF);
                cmd[4] = (byte)(((Integer)args[0]).intValue() >> 0 & 0xFF);
                break;
            default:;
        }
        cmd[0] = (byte) payloadLen;
        cmd[1] = (byte) CommandValues.WRITE_VIGILANCE_CFG.ordinal();
        cmd[2] = (byte) item.ordinal();

        return cmd;
    }

}
