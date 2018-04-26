package com.patri.guardtracker.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

import java.util.Calendar;

/**
 * Created by patri on 21/04/2016.
 */
public class MonitoringConfiguration {
    static final public int GPS_THRESHOLD_METERS_MIN = 10;
    static final public int GPS_THRESHOLD_METERS_MAX = 1000;
    static final public int GPS_FOV_MIN = 1;
    static final public int GPS_FOV_MAX = 20;
    static final public double TEMP_LOW_MIN = 0;
    static final public double TEMP_LOW_MAX = 20;
    static final public double TEMP_HIGH_MIN = 20;
    static final public double TEMP_HIGH_MAX = 100;
    public final static int SMS_CRITERIA_CYCLIC_MAX = 99;
    public final static int SMS_CRITERIA_CYCLIC_MIN = 1;
    static final public double SIM_BALANCE_WATERLINE_MAX_EURO = 10;
    static final public double SIM_BALANCE_WATERLINE_MAX_CENTIME = 99;
    static final public double SIM_BALANCE_WATERLINE_MIN_EURO = 0;
    static final public double SIM_BALANCE_WATERLINE_MIN_CENTIME = 0;
    static final private int MON_CFG_TEMP_PRECISION = 10;
    static final private int MON_CFG_BAT_BALANCE_PRECISION = 100;
    private int _id;
    private int mTimeMod;            // minutes of day
    private int mPeriodMin;          // minutes
    private SmsCriteria mSmsCriteria;// criteria
    private int mGpsThresholdMeters; // meters
    private int mGpsFov;             // number of satellites in view
    private int mGpsTimeout;         // minutes
    private double mTempHigh;        // graus celcius
    private double mTempLow;         // graus celcius
    private double mSimBalanceThreshold;// euros.centimes

    public MonitoringConfiguration(
                                      int timeMod,
                                      int periodMin,
                                      SmsCriteria smsCriteria,
                                      int gpsThresholdMeters,
                                      int gpsFov, int gpsTimeout,
                                      double tempHigh,
                                      double tempLow,
                                      double simBalanceThreshold) {
        this.mGpsFov = gpsFov;
        this.mGpsThresholdMeters = gpsThresholdMeters;
        this.mGpsTimeout = gpsTimeout;
        this.mPeriodMin = periodMin;
        this.mSmsCriteria = smsCriteria;
        this.mTimeMod = timeMod;
        this.mTempHigh = tempHigh;
        this.mTempLow = tempLow;
        this.mSimBalanceThreshold = simBalanceThreshold;
    }

    /**
     * Compares two MonitoringConfigurations. Two MonitoringConfigurations are considered equals
     * if they have the same configuration values for the same device.
     * This may happen in backup domain.
     * @param mc
     * @return
     */
    public boolean equals(final MonitoringConfiguration mc) {
        return  mc == this || (
                mc.getTimeMod() == this.getTimeMod() &&
                        mc.getPeriodMin() == this.getPeriodMin() &&
                        mc.getSmsCriteria().equals(this.getSmsCriteria()) &&
                        mc.getGpsThresholdMeters() == this.getGpsThresholdMeters() &&
                        mc.getGpsFov() == this.getGpsFov() &&
                        mc.getGpsTimeout() == this.getGpsTimeout() &&
                        mc.getTempHigh() == this.getTempHigh() &&
                        mc.getTempLow() == this.getTempLow() &&
                        mc.getSimBalanceThreshold() == this.getSimBalanceThreshold()
        );
    }


    //    public MonitoringConfiguration(int _id) {
//        this(_id,0,0,false,0,0,0);
//    }
    public void set_id(int id)          { _id = id; }
    public int get_id()                 { return _id; }
    public int getGpsFov()              { return mGpsFov; }
    public int getGpsThresholdMeters()  { return mGpsThresholdMeters; }
    public int getGpsTimeout()          { return mGpsTimeout; }
    public int getPeriodMin()           { return mPeriodMin; }
    public SmsCriteria getSmsCriteria() { return mSmsCriteria; }
    public int getTimeMod()             { return mTimeMod; }
    public double getTempHigh()            { return mTempHigh; }
    public double getTempLow()             { return mTempLow; }
    public double getSimBalanceThreshold() { return mSimBalanceThreshold; }
    public void setGpsFov(int gpsFov)                         { this.mGpsFov = gpsFov; }
    public void setGpsThresholdMeters(int gpsThresholdMeters) { this.mGpsThresholdMeters = gpsThresholdMeters; }
    public void setGpsTimeout(int gpsTimeout)                 { this.mGpsTimeout = gpsTimeout; }
    public void setPeriodMin(int periodMin)                   { this.mPeriodMin = periodMin; }
    public void setSmsCriteria(SmsCriteria smsCriteria)       { this.mSmsCriteria = smsCriteria; }
    public void setTimeMod(int timeMod)                       { this.mTimeMod = timeMod; }
    public void setTempHigh(double tempHigh)                  { this.mTempHigh = tempHigh; }
    public void setTempLow(double tempLow)                    { this.mTempLow = tempLow; }
    public void setSimBalanceThreshold(double simBalanceThreshold) { this.mSimBalanceThreshold = simBalanceThreshold; }

    public String getPrettyStartTime()      {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, mTimeMod/60);
        calendar.set(Calendar.MINUTE, mTimeMod%60);
        String prettyDate = String.format("%1$tR", calendar.getTimeInMillis());
        return prettyDate;
    }
    public String getPrettyPeriod()         {
        // ToDo: see Duration/Period class to measures the difference between two dates.
        int minutes = mPeriodMin % 60;
        int hours   = mPeriodMin / 60 % 24;
        int days    = mPeriodMin / 60 / 24;
        StringBuilder stringBuilder = new StringBuilder();
        if (days > 0)
            stringBuilder.append(String.format("%d days   ", days));
        if (hours > 0)
            stringBuilder.append(String.format("%d hours   ", hours));
        if (minutes > 0)
            stringBuilder.append(String.format("%d minutes", minutes));
        String prettyDate = stringBuilder.toString();
        return prettyDate;
    }
    public String getPrettySmsCriteria()    { return mSmsCriteria.name(); }
    public String getPrettyGpsFov()         { return "" + mGpsFov + " satellites"; }
    public String getPrettyGpsThreshold()   { return "" + mGpsThresholdMeters + " meters"; }
    public String getPrettyGpsTimeout()     { return "" + mGpsTimeout + " minutes"; }
    public String getPrettyTempHigh()       {
        String temp = String.format("Max: %.1fºC", mTempHigh);
        return temp;
    }
    public String getPrettyTempLow()        {
        String temp = String.format("Min: %.1fºC", mTempLow);
        return temp;
    }
    public String getPrettySimBalance()     { return "€" + mSimBalanceThreshold; }



    /*
    Create part.
     */

    public enum SmsCriteria {
        Allways, AlertOnly, Never, Cyclic;
        private static SmsCriteria[] values = null;
        public static SmsCriteria fromInteger(int i) {
            if (SmsCriteria.values == null) {
                SmsCriteria.values = SmsCriteria.values();
            }
            return SmsCriteria.values[i];
        }
    }

    public void create(Context context) {
        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for write
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        int tempLowRaw  = (int)(mTempLow  * MON_CFG_TEMP_PRECISION);
        int tempHighRaw = (int)(mTempHigh * MON_CFG_TEMP_PRECISION);
        int simThresholdRaw = (int)(mSimBalanceThreshold * MON_CFG_BAT_BALANCE_PRECISION);
        ContentValues contentValues = new ContentValues();
        contentValues.put(GuardTrackerContract.MonCfgTable.COLUMN_NAME_TIME, mTimeMod);
        contentValues.put(GuardTrackerContract.MonCfgTable.COLUMN_NAME_PERIOD, mPeriodMin);
        contentValues.put(GuardTrackerContract.MonCfgTable.COLUMN_NAME_SMS_CRITERIA, mSmsCriteria.toString());
        contentValues.put(GuardTrackerContract.MonCfgTable.COLUMN_NAME_GPS_THRESHOLD_METERS, mGpsThresholdMeters);
        contentValues.put(GuardTrackerContract.MonCfgTable.COLUMN_NAME_GPS_FOV, mGpsFov);
        contentValues.put(GuardTrackerContract.MonCfgTable.COLUMN_NAME_GPS_TIMEOUT, mGpsTimeout);
        contentValues.put(GuardTrackerContract.MonCfgTable.COLUMN_NAME_TEMP_HIGH, tempHighRaw);
        contentValues.put(GuardTrackerContract.MonCfgTable.COLUMN_NAME_TEMP_LOW, tempLowRaw);
        contentValues.put(GuardTrackerContract.MonCfgTable.COLUMN_NAME_SIM_BALANCE_THRESHOLD, simThresholdRaw);
        long pkId = db.insertOrThrow(GuardTrackerContract.MonCfgTable.TABLE_NAME, null, contentValues);
        this._id = (int)pkId;

        db.close();
        dbHelper.close();
    }

    public static MonitoringConfiguration read(Context context, int id) {
        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for read
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] monCfgProjection = {
                GuardTrackerContract.MonCfgTable.COLUMN_NAME_TIME,
                GuardTrackerContract.MonCfgTable.COLUMN_NAME_PERIOD,
                GuardTrackerContract.MonCfgTable.COLUMN_NAME_SMS_CRITERIA,
                GuardTrackerContract.MonCfgTable.COLUMN_NAME_GPS_THRESHOLD_METERS,
                GuardTrackerContract.MonCfgTable.COLUMN_NAME_GPS_FOV,
                GuardTrackerContract.MonCfgTable.COLUMN_NAME_GPS_TIMEOUT,
                GuardTrackerContract.MonCfgTable.COLUMN_NAME_TEMP_HIGH,
                GuardTrackerContract.MonCfgTable.COLUMN_NAME_TEMP_LOW,
                GuardTrackerContract.MonCfgTable.COLUMN_NAME_SIM_BALANCE_THRESHOLD
        };
        // Define columns WHERE clause.
        String monCfgSelection = GuardTrackerContract.MonCfgTable._ID + " LIKE ?";
        // Define values WHERE clause.
        String[] monCfgSelArgs = { String.valueOf(id) };

        Cursor cursor = db.query(
                GuardTrackerContract.MonCfgTable.TABLE_NAME,  // The table to query
                monCfgProjection,                             // The columns to return
                monCfgSelection,                              // The columns for the WHERE clause
                monCfgSelArgs,                                // The values for the WHERE clause
                null,                                         // don't group the rows
                null,                                         // don't filter by row groups
                null                                          // The sort order
        );
        if (cursor.getCount() != 1) throw new IllegalArgumentException();

        cursor.moveToFirst();
        // Extract values from query result
        int time = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.MonCfgTable.COLUMN_NAME_TIME));
        int period = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.MonCfgTable.COLUMN_NAME_PERIOD));
        SmsCriteria smsCriteria = SmsCriteria.fromInteger(cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.MonCfgTable.COLUMN_NAME_SMS_CRITERIA)));
        int gpsThresholdMeters = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.MonCfgTable.COLUMN_NAME_GPS_THRESHOLD_METERS));
        int gpsFov = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.MonCfgTable.COLUMN_NAME_GPS_FOV));
        int gpsTimeout = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.MonCfgTable.COLUMN_NAME_GPS_TIMEOUT));
        int tempRawHigh = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.MonCfgTable.COLUMN_NAME_TEMP_HIGH));
        int tempRawLow = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.MonCfgTable.COLUMN_NAME_TEMP_LOW));
        int simBalanceThresholdRaw = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.MonCfgTable.COLUMN_NAME_SIM_BALANCE_THRESHOLD));
        double tempHigh = (double)tempRawHigh / MON_CFG_TEMP_PRECISION;
        double tempLow  = (double)tempRawLow  / MON_CFG_TEMP_PRECISION;
        double simBalanceThreshold = (double)simBalanceThresholdRaw / MON_CFG_BAT_BALANCE_PRECISION;

        MonitoringConfiguration monCfg = new MonitoringConfiguration(
                time, period,
                smsCriteria,
                gpsThresholdMeters, gpsFov, gpsTimeout,
                tempHigh, tempLow,
                simBalanceThreshold
        );
        monCfg.set_id(id);

        cursor.close();
        db.close();
        dbHelper.close();

        return monCfg;
    }

    public void update(Context context) {
        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for write
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int tempRawLow  = (int)(mTempLow  * MON_CFG_TEMP_PRECISION);
        int tempRawHigh = (int)(mTempHigh * MON_CFG_TEMP_PRECISION);
        int simThresholdRaw = (int)(mSimBalanceThreshold * MON_CFG_BAT_BALANCE_PRECISION);
        ContentValues contentValues = new ContentValues();
        contentValues.put(GuardTrackerContract.MonCfgTable.COLUMN_NAME_TIME, mTimeMod);
        contentValues.put(GuardTrackerContract.MonCfgTable.COLUMN_NAME_PERIOD, mPeriodMin);
        contentValues.put(GuardTrackerContract.MonCfgTable.COLUMN_NAME_SMS_CRITERIA, mSmsCriteria.toString());
        contentValues.put(GuardTrackerContract.MonCfgTable.COLUMN_NAME_GPS_THRESHOLD_METERS, mGpsThresholdMeters);
        contentValues.put(GuardTrackerContract.MonCfgTable.COLUMN_NAME_GPS_FOV, mGpsFov);
        contentValues.put(GuardTrackerContract.MonCfgTable.COLUMN_NAME_GPS_TIMEOUT, mGpsTimeout);
        contentValues.put(GuardTrackerContract.MonCfgTable.COLUMN_NAME_TEMP_HIGH, tempRawHigh);
        contentValues.put(GuardTrackerContract.MonCfgTable.COLUMN_NAME_TEMP_LOW, tempRawLow);
        contentValues.put(GuardTrackerContract.MonCfgTable.COLUMN_NAME_SIM_BALANCE_THRESHOLD, simThresholdRaw);
        // Define columns WHERE clause.
        String whereClause = GuardTrackerContract.MonCfgTable._ID + " LIKE ?";
        // Define values WHERE clause.
        String[] whereClauseArgs = { String.valueOf(_id) };

        int rowsUpdated = db.update(GuardTrackerContract.MonCfgTable.TABLE_NAME, contentValues, whereClause, whereClauseArgs);
        assert rowsUpdated == 1;

        db.close();
        dbHelper.close();
    }


    public static boolean delete(Context context, int id) {
        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for write
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Define columns WHERE clause.
        String whereClause = GuardTrackerContract.MonCfgTable._ID + " LIKE ?";
        // Define values WHERE clause.
        String [] whereClauseArgs = { String.valueOf(id) };

        // Execute query
        int deletedItems = db.delete(GuardTrackerContract.MonCfgTable.TABLE_NAME, whereClause, whereClauseArgs);

        db.close();
        dbHelper.close();
        return deletedItems == 1;
    }
    public static int delete(Context context, int [] ids) {
        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for write
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Define columns WHERE clause.
        String whereClause = GuardTrackerContract.MonCfgTable._ID + " LIKE ?";
        // Define values WHERE clause.
        String [] whereClauseArgs = new String[ids.length];
        int i = 0;
        for (int id: ids) {
            whereClauseArgs[i++] = String.valueOf(id);
        }

        // Execute query
        int deletedItems = db.delete(GuardTrackerContract.MonCfgTable.TABLE_NAME, whereClause, whereClauseArgs);

        db.close();
        dbHelper.close();
        return deletedItems;
    }
    public boolean delete(Context context) {
        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for write
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Define columns WHERE clause.
        String whereClause = GuardTrackerContract.MonCfgTable._ID + " LIKE ?";
        // Define values WHERE clause.
        String [] whereClauseArgs = { String.valueOf(_id) };

        // Execute query
        int deletedItems = db.delete(GuardTrackerContract.MonCfgTable.TABLE_NAME, whereClause, whereClauseArgs);

        db.close();
        dbHelper.close();
        return deletedItems == 1;
    }

//    /**
//     * Create backup from this instance and insert it in GuardTrackerBackup table.
//     * @param context
//     */
//    public void createBackup(Context context) {
//        // Read MonitoringConfiguration database helper
//        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
//        // Initialize database for write
//        final SQLiteDatabase db = dbHelper.getWritableDatabase();
//        int tempLowRaw  = (int)(mTempLow  * MON_CFG_TEMP_PRECISION);
//        int tempHighRaw = (int)(mTempHigh * MON_CFG_TEMP_PRECISION);
//        int simThresholdRaw = (int)(mSimBalanceThreshold * MON_CFG_BAT_BALANCE_PRECISION);
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_TIME, mTimeMod);
//        contentValues.put(GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_PERIOD, mPeriodMin);
//        contentValues.put(GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_SMS_CRITERIA, mSmsCriteria.toString());
//        contentValues.put(GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_GPS_THRESHOLD_METERS, mGpsThresholdMeters);
//        contentValues.put(GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_GPS_FOV, mGpsFov);
//        contentValues.put(GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_GPS_TIMEOUT, mGpsTimeout);
//        contentValues.put(GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_TEMP_HIGH, tempHighRaw);
//        contentValues.put(GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_TEMP_LOW, tempLowRaw);
//        contentValues.put(GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_SIM_BALANCE_THRESHOLD, simThresholdRaw);
//        contentValues.put(GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_CURR_ID, _id);
//        long pkId = db.insertOrThrow(GuardTrackerContract.MonCfgBackupTable.TABLE_NAME, null, contentValues);
//
//        mNextId = (int)pkId;
//        mBackup = new MonitoringConfiguration(_id, mTimeMod, mPeriodMin, mSmsCriteria,
//                mGpsThresholdMeters, mGpsFov, mGpsTimeout,
//                tempHighRaw, tempLowRaw, simThresholdRaw);
//        mBackup.mCurrId = _id;
//
//    }
//
//    /**
//     * Delete backup of this instance from GuardTableBackup table.
//     * @param context
//     * @return true if backup was sucessfully deleted.
//     */
//    public boolean deleteBackup(Context context) {
//        // Read GuardTracker database helper
//        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
//        // Initialize database for write
//        final SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//        // Define columns WHERE clause.
//        String whereClause = GuardTrackerContract.MonCfgBackupTable._ID + " LIKE ?";
//        String[] whereClauseArgs = { String.valueOf(mBackup._id) };
//
//        // Execute query
//        int deletedItems = db.deleteDeep(GuardTrackerContract.MonCfgBackupTable.TABLE_NAME, whereClause, whereClauseArgs);
//
//        db.close();
//        dbHelper.close();
//        this.mBackup = null;
//        this.mNextId = 0;
//
//        return deletedItems == 1;
//    }
//
//    public boolean updateBackup(Context context) {
//        // Read GuardTracker database helper
//        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
//        // Initialize database for write
//        final SQLiteDatabase db = dbHelper.getWritableDatabase();
//        int tempLowRaw  = (int)(mTempLow  * MON_CFG_TEMP_PRECISION);
//        int tempHighRaw = (int)(mTempHigh * MON_CFG_TEMP_PRECISION);
//        int simThresholdRaw = (int)(mSimBalanceThreshold * MON_CFG_BAT_BALANCE_PRECISION);
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_TIME, mTimeMod);
//        contentValues.put(GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_PERIOD, mPeriodMin);
//        contentValues.put(GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_SMS_CRITERIA, mSmsCriteria.toString());
//        contentValues.put(GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_GPS_THRESHOLD_METERS, mGpsThresholdMeters);
//        contentValues.put(GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_GPS_FOV, mGpsFov);
//        contentValues.put(GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_GPS_TIMEOUT, mGpsTimeout);
//        contentValues.put(GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_TEMP_HIGH, tempHighRaw);
//        contentValues.put(GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_TEMP_LOW, tempLowRaw);
//        contentValues.put(GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_SIM_BALANCE_THRESHOLD, simThresholdRaw);
//        contentValues.put(GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_CURR_ID, _id);
//
//        // Define columns WHERE clause.
//        String whereClause = GuardTrackerContract.MonCfgBackupTable._ID + " LIKE ?";
//        String[] whereClauseArgs = { String.valueOf(mBackup._id) };
//
//        // Execute query
//        int updatedItems = db.update(GuardTrackerContract.MonCfgBackupTable.TABLE_NAME, contentValues, whereClause, whereClauseArgs);
//
//        db.close();
//        dbHelper.close();
//
//        return updatedItems == 1;
//    }
//
//    public void readBackup(Context context) {
//        // Read GuardTracker database helper
//        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
//        // Initialize database for read
//        SQLiteDatabase db = dbHelper.getReadableDatabase();
//        // Define a projection that specifies which columns from the database
//        // you will actually use after this query.
//        String[] backupProjection = {
//                GuardTrackerContract.MonCfgBackupTable._ID,
//                GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_TIME,
//                GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_PERIOD,
//                GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_SMS_CRITERIA,
//                GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_GPS_THRESHOLD_METERS,
//                GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_GPS_FOV,
//                GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_GPS_TIMEOUT,
//                GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_TEMP_HIGH,
//                GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_TEMP_LOW,
//                GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_SIM_BALANCE_THRESHOLD,
//                GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_CURR_ID
//        };
//        // Define columns WHERE clause.
//        String backupSelection = GuardTrackerContract.MonCfgBackupTable._ID + " LIKE ?";
//        // Define values WHERE clause.
//        String[] backupSelArgs = {String.valueOf(mNextId)};
//
//        Cursor cursor = db.query(
//                GuardTrackerContract.MonCfgBackupTable.TABLE_NAME,  // The table to query
//                backupProjection,                             // The columns to return
//                backupSelection,                              // The columns for the WHERE clause
//                backupSelArgs,                                // The values for the WHERE clause
//                null,                                               // don't group the rows
//                null,                                               // don't filter by row groups
//                null                                                // The sort order
//        );
//        if (cursor.getCount() != 1) throw new IllegalArgumentException("readBackup");
//
//        cursor.moveToFirst();
//        // Extract values from query result
//        int id = cursor.getInt((cursor.getColumnIndex(GuardTrackerContract.MonCfgBackupTable._ID)));
//        int time = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_TIME));
//        int period = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_PERIOD));
//        SmsCriteria smsCriteria = SmsCriteria.fromInteger(cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_SMS_CRITERIA)));
//        int gpsThresholdMeters = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_GPS_THRESHOLD_METERS));
//        int gpsFov = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_GPS_FOV));
//        int gpsTimeout = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_GPS_TIMEOUT));
//        int tempRawHigh = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_TEMP_HIGH));
//        int tempRawLow = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_TEMP_LOW));
//        int simBalanceThresholdRaw = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_SIM_BALANCE_THRESHOLD));
//        double tempHigh = (double)tempRawHigh / MON_CFG_TEMP_PRECISION;
//        double tempLow  = (double)tempRawLow  / MON_CFG_TEMP_PRECISION;
//        double simBalanceThreshold = (double)simBalanceThresholdRaw / MON_CFG_BAT_BALANCE_PRECISION;
//        int currId = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_CURR_ID));
//
//        mBackup = new MonitoringConfiguration(id,
//                time, period,
//                smsCriteria,
//                gpsThresholdMeters, gpsFov, gpsTimeout,
//                tempHigh, tempLow,
//                simBalanceThreshold
//        );
//        mBackup.setCurrId(currId);
//
//        cursor.close();
//        db.close();
//        dbHelper.close();
//    }

    // Third refactoring iteration. New code belows.

    /**
     * Create en empty object.
     * ToDo: Preferível no futuro alterar o construtor para incluir como parâmetro o array de bytes
     * que inicia o estado do objecto (incluir no construtor o código do método parser.
     */
    public MonitoringConfiguration() {

    }
    public void parse(byte [] data) {
        int time         = (((int)data[0] & 0xFF) << 8) + ((int)data[1] & 0xFF);
        int period       = (((int)data[2] & 0xFF) << 8) + ((int)data[3] & 0xFF);
        int _smsCriteria = (int)data[4] & 0xFF;
        SmsCriteria smsCriteria  = SmsCriteria.fromInteger(_smsCriteria);
        float ltDegrees  = ((((int)data[5] & 0xFF) << 24) +
                (((int)data[6] & 0xFF) << 16) +
                (((int)data[7] & 0xFF) << 8) +
                (((int)data[8] & 0xFF) << 0));
        ltDegrees = ltDegrees / 10000 / 60;
        float lgDegrees  = ((((int)data[9] & 0xFF) << 24) +
                (((int)data[10] & 0xFF) << 16) +
                (((int)data[11] & 0xFF) << 8) +
                (((int)data[12] & 0xFF) << 0));
        lgDegrees = lgDegrees / 10000 / 60;
        Location zeroLocation = new Location("Zero location");
        zeroLocation.setLatitude(0);
        zeroLocation.setLongitude(0);
        Location monThreshold = new Location("Mon threshold");
        monThreshold.setLatitude(ltDegrees);
        monThreshold.setLongitude(lgDegrees);
        float distance = zeroLocation.distanceTo(monThreshold);
        int gpsThresholdMeters = (int)distance;
        int gpsFov       = ((int)data[13] & 0xFF);
        int gpsTimeout   = ((int)data[14] & 0xFF);
        int tempRawHigh     = ((int)data[15] & 0xFF);
        int tempRawLow      = ((int)data[16] & 0xFF);
        int simThresholdRaw = (((int)data[17] & 0xFF) << 8) + ((int)data[18] & 0xFF);
        double tempHigh = (tempRawHigh >> 1) + (0.5)*(tempRawHigh & 1);
        double tempLow  = (tempRawLow  >> 1) + (0.5)*(tempRawLow  & 1);
        double simThreshold = (double)simThresholdRaw / 100; // ESTA CONSTANTE MÁGICA TEM DE SER SUBTITUÍDA POR UM SÍMBOLO.

        setTimeMod(time);
        setPeriodMin(period);
        setSmsCriteria(smsCriteria);
        setGpsThresholdMeters(gpsThresholdMeters);
        setGpsFov(gpsFov);
        setGpsTimeout(gpsTimeout);
        setTempHigh(tempHigh);
        setTempLow(tempLow);
        setSimBalanceThreshold(simThreshold);
    }


//    public int getRawTimeOfDayToWake() {
//        return mTimeMod;
//    }
//    public int getRawPeriod() {
//        return mPeriodMin;
//    }
//    public int getRawSmsCriteria() {
//        return mSmsCriteria.ordinal();
//    }

    //    Retired from (18/12/2016): http://www.movable-type.co.uk/scripts/latlong.html
    //    Formula:	φ2 = asin( sin φ1 ⋅ cos δ + cos φ1 ⋅ sin δ ⋅ cos θ )
    //    λ2 = λ1 + atan2( sin θ ⋅ sin δ ⋅ cos φ1, cos δ − sin φ1 ⋅ sin φ2 )
    //    where	φ is latitude, λ is longitude, θ is the bearing (clockwise from north), δ is the angular distance d/R; d being the distance travelled, R the earth’s radius
    //    JavaScript:  (all angles in radians)
    //    var φ2 = Math.asin( Math.sin(φ1)*Math.cos(d/R) +
    //            Math.cos(φ1)*Math.sin(d/R)*Math.cos(brng) );
    //    var λ2 = λ1 + Math.atan2(Math.sin(brng)*Math.sin(d/R)*Math.cos(φ1),
    //            Math.cos(d/R)-Math.sin(φ1)*Math.sin(φ2));
    //    The longitude can be normalised to −180…+180 using (lon+540)%360-180
    //    Excel:       (all angles in radians)
    //    lat2: =ASIN(SIN(lat1)*COS(d/R) + COS(lat1)*SIN(d/R)*COS(brng))
    //    lon2: =lon1 + ATAN2(COS(d/R)-SIN(lat1)*SIN(lat2), SIN(brng)*SIN(d/R)*COS(lat1))
    //            * Remember that Excel reverses the arguments to ATAN2 – see notes below
    //    For final bearing, simply take the initial bearing from the end point to the start point and reverse it with (brng+180)%360.
    public static int getRawGpsThresholdLat(int distanceMeters) {
        final long earthRadiusMeters = 6371000;
        double zeroLat = 0;
        float zeroBearing = 0;
        double zeroLatRad = Math.toRadians(zeroLat);
        double latRad = Math.asin(
                Math.sin(zeroLatRad) * Math.cos((double)distanceMeters/earthRadiusMeters) +
                Math.cos(zeroLatRad) * Math.sin((double)distanceMeters/earthRadiusMeters) *
                Math.cos(zeroBearing));
        double latDegrees = Math.toDegrees(latRad);
        int latMinutes = (int)(latDegrees * 60 * 10000);

        return latMinutes;
    }
    public static int getRawGpsThresholdLon(int distanceMeters) {
        final long earthRadiusMeters = 6371000;
        double zeroLat = 0;
        double zeroLon = 0;
        float zeroBearing = 0;
        double zeroLatRad = Math.toRadians(zeroLat);
        double zeroLonRad = Math.toRadians(zeroLon);
        double latRad = Math.asin(
                Math.sin(zeroLatRad) * Math.cos((double)distanceMeters/earthRadiusMeters) +
                Math.cos(zeroLatRad) * Math.sin((double)distanceMeters/earthRadiusMeters) *
                Math.cos(zeroBearing));
        double lonRad = zeroLonRad + Math.atan2(
                Math.sin(zeroBearing) * Math.sin((double)distanceMeters/earthRadiusMeters) *
                Math.cos(zeroLatRad),
                Math.cos((double)distanceMeters/earthRadiusMeters) - Math.sin(zeroLatRad) *
                Math.sin(latRad));
        double lonDegrees = Math.toDegrees(lonRad);
        int lonMinutes = (int)(lonDegrees * 60 * 10000);

        return lonMinutes;
    }

//    public int getRawGpsFov() {
//        return mGpsFov;
//    }
//    public int getRawGpsTimeout() {
//        return mGpsTimeout;
//    }
    public static int getRawTemp(double temp) {
        int tempRaw = (int)temp << 1 + ((int)(temp * 10) % 10 != 0 ? 1 : 0);
        return tempRaw;
    }
    public static int getRawSimBalance(double simBalance) {
        int simBalanceRaw = (int)(simBalance * 100);
        return simBalanceRaw;
    }

}
