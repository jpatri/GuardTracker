package com.patri.guardtracker.model;

import android.provider.BaseColumns;

/**
 * Created by patri on 15/04/2016.
 */
public class GuardTrackerContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public GuardTrackerContract() {}

    /* Inner class that defines the GuardTracker table contents */
    public static abstract class GuardTrackerTable implements BaseColumns {
        public static final String TABLE_NAME = "GuardTracker";
//        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_BLE_ID = "bleId";
        public static final String COLUMN_NAME_GSM_ID = "gsmId";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_WAKE_SENSORS = "wakeSensors";
        public static final String COLUMN_NAME_POS_REF = "posRef";
        public static final String COLUMN_NAME_LAST_MON_INFO = "lastMonInfo";
        public static final String COLUMN_NAME_MON_CFG = "monCfg";
        public static final String COLUMN_NAME_TRACK_CFG = "trackCfg";
        public static final String COLUMN_NAME_VIGILANCE_CFG = "vigilanceCfg";
        public static final String COLUMN_NAME_OWNER_PHONE_NUMBER = "ownerPhoneNumber";
        public static final String COLUMN_NAME_SYNC = "sync";
        public static final String COLUMN_NAME_NEXT = "next";
    }
//    /* Backup of GuardTracker table entries */
//    public static abstract class GuardTrackerBackupTable implements BaseColumns {
//        public static final String TABLE_NAME = "GuardTrackerBackup";
//        //        public static final String COLUMN_NAME_ID = "id";
//        public static final String COLUMN_NAME_BLE_ID = "bleId";
//        public static final String COLUMN_NAME_GSM_ID = "gsmId";
//        public static final String COLUMN_NAME_NAME = "name";
//        public static final String COLUMN_NAME_WAKE_SENSORS = "wakeSensors";
//        public static final String COLUMN_NAME_POS_REF = "posRef";
//        public static final String COLUMN_NAME_LAST_MON_INFO = "lastMonInfo";
//        public static final String COLUMN_NAME_MON_CFG = "monCfg";
//        public static final String COLUMN_NAME_TRACK_CFG = "trackCfg";
//        public static final String COLUMN_NAME_VIGILANCE_CFG = "vigilanceCfg";
//        public static final String COLUMN_NAME_OWNER_PHONE_NUMBER = "ownerPhoneNumber";
//        public static final String COLUMN_NAME_CURR_ID = "currId";
//    }

    /* Inner class that defines the GuardTracker table contents */
    public static abstract class ContactsTable implements BaseColumns {
        public static final String TABLE_NAME = "Contacts";
        //        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_GUARD_TRACKER = "guardTrackerId";
        public static final String COLUMN_NAME_PHONE_NUMBER = "phoneNumber";
    }

    /* Inner class that defines the MonitoringConfiguration table contents */
    public static abstract class MonCfgTable implements BaseColumns {
        public static final String TABLE_NAME = "MonitoringConfiguration";
//        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_PERIOD = "period";
        public static final String COLUMN_NAME_SMS_CRITERIA = "smsCriteria";
        public static final String COLUMN_NAME_GPS_THRESHOLD_METERS = "gpsThresholdMeters";
        public static final String COLUMN_NAME_GPS_FOV = "gpsFov";
        public static final String COLUMN_NAME_GPS_TIMEOUT = "gpsTimeout";
        public static final String COLUMN_NAME_TEMP_HIGH = "tempHigh";
        public static final String COLUMN_NAME_TEMP_LOW = "tempLow";
        public static final String COLUMN_NAME_SIM_BALANCE_THRESHOLD = "simBalanceThreshold";
    }

//    public static abstract class MonCfgBackupTable implements BaseColumns {
//        public static final String TABLE_NAME = "MonitoringConfigurationBackup";
//        //        public static final String COLUMN_NAME_ID = "id";
//        public static final String COLUMN_NAME_TIME = "time";
//        public static final String COLUMN_NAME_PERIOD = "period";
//        public static final String COLUMN_NAME_SMS_CRITERIA = "smsCriteria";
//        public static final String COLUMN_NAME_GPS_THRESHOLD_METERS = "gpsThresholdMeters";
//        public static final String COLUMN_NAME_GPS_FOV = "gpsFov";
//        public static final String COLUMN_NAME_GPS_TIMEOUT = "gpsTimeout";
//        public static final String COLUMN_NAME_TEMP_HIGH = "tempHigh";
//        public static final String COLUMN_NAME_TEMP_LOW = "tempLow";
//        public static final String COLUMN_NAME_SIM_BALANCE_THRESHOLD = "simBalanceThreshold";
//        public static final String COLUMN_NAME_CURR_ID = "currId";
//    }

    /* Inner class that defines the TrackingConfiguration table contents */
    public static abstract class TrackCfgTable implements BaseColumns {
        public static final String TABLE_NAME = "TrackingConfiguration";
        //        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_GPS_THRESHOLD_METERS = "gpsThresholdMeters";
        public static final String COLUMN_NAME_GPS_TIME_CRITERIA = "gpsTimeCriteria";
        public static final String COLUMN_NAME_GPS_TIMEOUT = "gpsTimeout";
        public static final String COLUMN_NAME_GPS_FOV = "gpsFov";
        public static final String COLUMN_NAME_TIME_TRACKING = "timeTracking";
        public static final String COLUMN_NAME_TIME_PRE = "timePre";
        public static final String COLUMN_NAME_TIME_POST = "timePost";
    }

//    public static abstract class TrackCfgBackupTable implements BaseColumns {
//        public static final String TABLE_NAME = "TrackingConfigurationBackup";
//        //        public static final String COLUMN_NAME_ID = "id";
//        public static final String COLUMN_NAME_GPS_THRESHOLD_METERS = "gpsThresholdMeters";
//        public static final String COLUMN_NAME_GPS_TIME_CRITERIA = "gpsTimeCriteria";
//        public static final String COLUMN_NAME_GPS_TIMEOUT = "gpsTimeout";
//        public static final String COLUMN_NAME_GPS_FOV = "gpsFov";
//        public static final String COLUMN_NAME_TIME_TRACKING = "timeTracking";
//        public static final String COLUMN_NAME_TIME_PRE = "timePre";
//        public static final String COLUMN_NAME_TIME_POST = "timePost";
//        public static final String COLUMN_NAME_CURR_ID = "currId";
//    }

    /* Inner class that defines the VigilanceConfiguration table contents */
    public static abstract class VigilanceCfgTable implements BaseColumns {
        public static final String TABLE_NAME = "VigilanceConfiguration";
        //        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_TILT_LEVEL_CRITERIA = "tiltLevelCriteria";
        public static final String COLUMN_NAME_BLE_ADVERTISEMENT_PERIOD = "bleAdvertisementPeriod";
    }

//    public static abstract class VigilanceCfgBackupTable implements BaseColumns {
//        public static final String TABLE_NAME = "VigilanceConfigurationBackup";
//        //        public static final String COLUMN_NAME_ID = "id";
//        public static final String COLUMN_NAME_TILT_LEVEL_CRITERIA = "tiltLevelCriteria";
//        public static final String COLUMN_NAME_BLE_ADVERTISEMENT_PERIOD = "bleAdvertisementPeriod";
//        public static final String COLUMN_NAME_CURR_ID = "currId";
//    }

    /* Inner class that defines the MonitoringInfo table contents */
    public static abstract class MonInfoTable implements BaseColumns {
        public static final String TABLE_NAME = "MonitoringInfo";
        //        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_GUARD_TRACKER = "guardTrackerId";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_POSITION = "position";
        public static final String COLUMN_NAME_BATTERY_CHARGE = "batteryCharge";
        public static final String COLUMN_NAME_TEMPERATURE = "temperature";
        public static final String COLUMN_NAME_BALANCE = "balance";
    }

    /* Inner class that defines the VigilanceConfiguration table contents */
    public static abstract class TrackSessionTable implements BaseColumns {
        public static final String TABLE_NAME = "TrackSession";
        //        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_GUARD_TRACKER = "guardTrackerId";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_NAME = "name";
    }

    /* Inner class that defines the Position table contents */
    public static abstract class PositionTable implements BaseColumns {
        public static final String TABLE_NAME = "Position";
        //        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_SESSION = "session";
        public static final String COLUMN_NAME_LT = "lt";
        public static final String COLUMN_NAME_LG = "lg";
        public static final String COLUMN_NAME_ALTITUDE = "altitude";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_NSAT = "nsat";
        public static final String COLUMN_NAME_HDOP = "hdop";
        public static final String COLUMN_NAME_FIXED = "fixed";
    }

}
