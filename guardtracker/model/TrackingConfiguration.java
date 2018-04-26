package com.patri.guardtracker.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

/**
 * Created by patri on 21/04/2016.
 */
public class TrackingConfiguration {

    public enum SmsCriteria {
        Lazy, Normal, Stresses, Final;
        private static SmsCriteria[] values = null;

        public static SmsCriteria fromInteger(int i) {
            if (SmsCriteria.values == null) {
                SmsCriteria.values = SmsCriteria.values();
            }
            return SmsCriteria.values[i];
        }
    }

    ;
    private int _id;
    private SmsCriteria mSmsCriteria;
    private int mGpsThreshold;          // Meters units
    private int mGpsFov;                // Number of satellites in view
    private int mGpsTimeout;            // Minutes units
    private int mTimeoutTracking;       // Minutes units
    private int mTimeoutPre;            // Minutes units
    private int mTimeoutPost;           // Minutes units

    public TrackingConfiguration(SmsCriteria criteria, int threshold, int fov, int timeout, int timeTracking, int timePre, int timePost) {
        this.mSmsCriteria = criteria;
        this.mGpsFov = fov;
        this.mGpsThreshold = threshold;
        this.mGpsTimeout = timeout;
        this.mTimeoutPost = timePost;
        this.mTimeoutPre = timePre;
        this.mTimeoutTracking = timeTracking;
    }

    /**
     * Compares two TrackingConfigurations. Two TrackingConfigurations are considered equals
     * if they have the same configuration values for the same device.
     * This may happen in backup domain.
     * @param tc
     * @return
     */
    public boolean equals(final TrackingConfiguration tc) {
        return  tc == this || (
                        tc.getSmsCriteria().equals(this.getSmsCriteria()) &&
                        tc.getGpsThreshold() == this.getGpsThreshold() &&
                        tc.getGpsFov() == this.getGpsFov() &&
                        tc.getGpsTimeout() == this.getGpsTimeout() &&
                        tc.getTimeoutTracking() == this.getTimeoutTracking() &&
                        tc.getTimeoutPre() == this.getTimeoutPre() &&
                        tc.getTimeoutPost() == this.getTimeoutPost()
        );
    }

    //public TrackingConfiguration(int _id) { this._id = _id; }

    public void set_id(int id)          { _id = id; }
    public int get_id() {
        return _id;
    }

    public SmsCriteria getSmsCriteria() {
        return mSmsCriteria;
    }

    public int getGpsFov() {
        return mGpsFov;
    }

    public int getGpsThreshold() {
        return mGpsThreshold;
    }

    public int getGpsTimeout() {
        return mGpsTimeout;
    }

    public int getTimeoutPost() {
        return mTimeoutPost;
    }

    public int getTimeoutPre() {
        return mTimeoutPre;
    }

    public int getTimeoutTracking() {
        return mTimeoutTracking;
    }

    public void setSmsCriteria(SmsCriteria criteria) {
        this.mSmsCriteria = criteria;
    }

    public void setGpsFov(int fov) {
        this.mGpsFov = fov;
    }

    public void setGpsThreshold(int threshold) {
        this.mGpsThreshold = threshold;
    }

    public void setGpsTimeout(int timeout) {
        this.mGpsTimeout = timeout;
    }

    public void setTimeoutPost(int timePost) {
        this.mTimeoutPost = timePost;
    }

    public void setTimeoutPre(int timePre) {
        this.mTimeoutPre = timePre;
    }

    public void setTimeoutTracking(int timeTracking) {
        this.mTimeoutTracking = timeTracking;
    }

    public String getPrettySmsCriteria() {
        return mSmsCriteria.name();
    }

    public String getPrettyGpsFov() {
        return "" + mGpsFov + " satellites";
    }

    public String getPrettyGpsThreshold() {
        return "" + mGpsThreshold + " meters";
    }

    public String getPrettyGpsTimeout() {
        return "" + mGpsTimeout + " minutes";
    }

    public String getPrettyTimeoutTracking() {
        return "" + mTimeoutTracking + " minutes";
    }

    public String getPrettyTimeoutPre() {
        return "" + mTimeoutPre + " minutes";
    }

    public String getPrettyTimeoutPost() {
        return "" + mTimeoutPost + " minutes";
    }

    public void create(Context context) {
        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for write
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(GuardTrackerContract.TrackCfgTable.COLUMN_NAME_GPS_THRESHOLD_METERS, mGpsThreshold);
        contentValues.put(GuardTrackerContract.TrackCfgTable.COLUMN_NAME_GPS_TIME_CRITERIA, mSmsCriteria.toString());
        contentValues.put(GuardTrackerContract.TrackCfgTable.COLUMN_NAME_GPS_TIMEOUT, mGpsTimeout);
        contentValues.put(GuardTrackerContract.TrackCfgTable.COLUMN_NAME_GPS_FOV, mGpsFov);
        contentValues.put(GuardTrackerContract.TrackCfgTable.COLUMN_NAME_TIME_TRACKING, mTimeoutTracking);
        contentValues.put(GuardTrackerContract.TrackCfgTable.COLUMN_NAME_TIME_PRE, mTimeoutPre);
        contentValues.put(GuardTrackerContract.TrackCfgTable.COLUMN_NAME_TIME_POST, mTimeoutPost);
        long pkId = db.insertOrThrow(GuardTrackerContract.TrackCfgTable.TABLE_NAME, null, contentValues);
        this._id = (int)pkId;

        db.close();
        dbHelper.close();
    }

    public static TrackingConfiguration read(Context context, int id) {
        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for read
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] trackingCfgProjection = {
                GuardTrackerContract.TrackCfgTable.COLUMN_NAME_GPS_TIME_CRITERIA,
                GuardTrackerContract.TrackCfgTable.COLUMN_NAME_GPS_THRESHOLD_METERS,
                GuardTrackerContract.TrackCfgTable.COLUMN_NAME_GPS_FOV,
                GuardTrackerContract.TrackCfgTable.COLUMN_NAME_GPS_TIMEOUT,
                GuardTrackerContract.TrackCfgTable.COLUMN_NAME_TIME_TRACKING,
                GuardTrackerContract.TrackCfgTable.COLUMN_NAME_TIME_PRE,
                GuardTrackerContract.TrackCfgTable.COLUMN_NAME_TIME_POST
        };
        // Define columns WHERE clause.
        String trackingCfgSelection = GuardTrackerContract.TrackCfgTable._ID + " LIKE ?";
        // Define values WHERE clause.
        String[] trackingCfgSelArgs = {String.valueOf(id)};

        Cursor cursor = db.query(
                GuardTrackerContract.TrackCfgTable.TABLE_NAME,  // The table to query
                trackingCfgProjection,                             // The columns to return
                trackingCfgSelection,                              // The columns for the WHERE clause
                trackingCfgSelArgs,                                // The values for the WHERE clause
                null,                                         // don't group the rows
                null,                                         // don't filter by row groups
                null                                          // The sort order
        );
        if (cursor.getCount() != 1) throw new IllegalArgumentException();

        cursor.moveToFirst();
        // Extract values from query result
        int criteriaInt = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.TrackCfgTable.COLUMN_NAME_GPS_TIME_CRITERIA));
        SmsCriteria criteria = SmsCriteria.fromInteger(criteriaInt);
        int gpsThresholdMeters = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.TrackCfgTable.COLUMN_NAME_GPS_THRESHOLD_METERS));
        int gpsFov = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.TrackCfgTable.COLUMN_NAME_GPS_FOV));
        int gpsTimeout = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.TrackCfgTable.COLUMN_NAME_GPS_TIMEOUT));
        int timeTracking = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.TrackCfgTable.COLUMN_NAME_TIME_TRACKING));
        int timePre = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.TrackCfgTable.COLUMN_NAME_TIME_PRE));
        int timePost = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.TrackCfgTable.COLUMN_NAME_TIME_POST));

        TrackingConfiguration trackingCfg = new TrackingConfiguration(criteria, gpsThresholdMeters, gpsFov, gpsTimeout, timeTracking, timePre, timePost);
        trackingCfg._id = id;

        cursor.close();
        db.close();
        dbHelper.close();

        return trackingCfg;
    }

    public void update(Context context) {
        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for write
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(GuardTrackerContract.TrackCfgTable.COLUMN_NAME_GPS_TIME_CRITERIA, mSmsCriteria.toString());
        contentValues.put(GuardTrackerContract.TrackCfgTable.COLUMN_NAME_GPS_THRESHOLD_METERS, mGpsThreshold);
        contentValues.put(GuardTrackerContract.TrackCfgTable.COLUMN_NAME_GPS_FOV, mGpsFov);
        contentValues.put(GuardTrackerContract.TrackCfgTable.COLUMN_NAME_GPS_TIMEOUT, mGpsTimeout);
        contentValues.put(GuardTrackerContract.TrackCfgTable.COLUMN_NAME_TIME_TRACKING, mTimeoutTracking);
        contentValues.put(GuardTrackerContract.TrackCfgTable.COLUMN_NAME_TIME_PRE, mTimeoutPre);
        contentValues.put(GuardTrackerContract.TrackCfgTable.COLUMN_NAME_TIME_POST, mTimeoutPost);
        // Define columns WHERE clause.
        String whereClause = GuardTrackerContract.TrackCfgTable._ID + " LIKE ?";
        // Define values WHERE clause.
        String[] whereClauseArgs = {String.valueOf(_id)};

        int rowsUpdated = db.update(GuardTrackerContract.TrackCfgTable.TABLE_NAME, contentValues, whereClause, whereClauseArgs);
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
        String whereClause = GuardTrackerContract.TrackCfgTable._ID + " LIKE ?";
        // Define values WHERE clause.
        String[] whereClauseArgs = {String.valueOf(id)};

        // Execute query
        int deletedItems = db.delete(GuardTrackerContract.TrackCfgTable.TABLE_NAME, whereClause, whereClauseArgs);

        db.close();
        dbHelper.close();
        return deletedItems == 1;

    }

    public static int delete(Context context, int[] ids) {
        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for write
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Define columns WHERE clause.
        String whereClause = GuardTrackerContract.TrackCfgTable._ID + " LIKE ?";
        // Define values WHERE clause.
        String[] whereClauseArgs = new String[ids.length];
        int i = 0;
        for (int id : ids) {
            whereClauseArgs[i++] = String.valueOf(id);
        }

        // Execute query
        int deletedItems = db.delete(GuardTrackerContract.TrackCfgTable.TABLE_NAME, whereClause, whereClauseArgs);

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
        String whereClause = GuardTrackerContract.TrackCfgTable._ID + " LIKE ?";
        // Define values WHERE clause.
        String [] whereClauseArgs = { String.valueOf(_id) };

        // Execute query
        int deletedItems = db.delete(GuardTrackerContract.TrackCfgTable.TABLE_NAME, whereClause, whereClauseArgs);

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
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_GPS_THRESHOLD_METERS, mGpsThreshold);
//        contentValues.put(GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_GPS_TIME_CRITERIA, mSmsCriteria.toString());
//        contentValues.put(GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_GPS_TIMEOUT, mGpsTimeout);
//        contentValues.put(GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_GPS_FOV, mGpsFov);
//        contentValues.put(GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_TIME_TRACKING, mTimeoutTracking);
//        contentValues.put(GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_TIME_PRE, mTimeoutPre);
//        contentValues.put(GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_TIME_POST, mTimeoutPost);
//        contentValues.put(GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_CURR_ID, _id);
//        long pkId = db.insertOrThrow(GuardTrackerContract.TrackCfgBackupTable.TABLE_NAME, null, contentValues);
//
//        mBackupId = (int)pkId;
//        mBackup = new TrackingConfiguration(_id, mSmsCriteria, mGpsThreshold, mGpsFov, mGpsTimeout,
//                mTimeoutTracking, mTimeoutPre, mTimeoutPost);
//        mBackup.mCurrId = _id;
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
//        String whereClause = GuardTrackerContract.TrackCfgBackupTable._ID + " LIKE ?";
//        String[] whereClauseArgs = { String.valueOf(mBackup._id) };
//
//        // Execute query
//        int deletedItems = db.deleteDeep(GuardTrackerContract.TrackCfgBackupTable.TABLE_NAME, whereClause, whereClauseArgs);
//
//        db.close();
//        dbHelper.close();
//        this.mBackup = null;
//        this.mBackupId = 0;
//
//        return deletedItems == 1;
//    }
//
//    public boolean updateBackup(Context context) {
//        // Read GuardTracker database helper
//        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
//        // Initialize database for write
//        final SQLiteDatabase db = dbHelper.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_GPS_THRESHOLD_METERS, mGpsThreshold);
//        contentValues.put(GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_GPS_TIME_CRITERIA, mSmsCriteria.toString());
//        contentValues.put(GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_GPS_TIMEOUT, mGpsTimeout);
//        contentValues.put(GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_GPS_FOV, mGpsFov);
//        contentValues.put(GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_TIME_TRACKING, mTimeoutTracking);
//        contentValues.put(GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_TIME_PRE, mTimeoutPre);
//        contentValues.put(GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_TIME_POST, mTimeoutPost);
//        contentValues.put(GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_CURR_ID, _id);
//
//        // Define columns WHERE clause.
//        String whereClause = GuardTrackerContract.TrackCfgBackupTable._ID + " LIKE ?";
//        String[] whereClauseArgs = { String.valueOf(mBackup._id) };
//
//        // Execute query
//        int updatedItems = db.update(GuardTrackerContract.TrackCfgBackupTable.TABLE_NAME, contentValues, whereClause, whereClauseArgs);
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
//                GuardTrackerContract.TrackCfgBackupTable._ID,
//                GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_GPS_TIME_CRITERIA,
//                GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_GPS_THRESHOLD_METERS,
//                GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_GPS_FOV,
//                GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_GPS_TIMEOUT,
//                GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_TIME_TRACKING,
//                GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_TIME_PRE,
//                GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_TIME_POST,
//                GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_CURR_ID
//        };
//        // Define columns WHERE clause.
//        String backupSelection = GuardTrackerContract.TrackCfgBackupTable._ID + " LIKE ?";
//        // Define values WHERE clause.
//        String[] backupSelArgs = {String.valueOf(mBackupId)};
//
//        Cursor cursor = db.query(
//                GuardTrackerContract.TrackCfgBackupTable.TABLE_NAME,  // The table to query
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
//        int id = cursor.getInt((cursor.getColumnIndex(GuardTrackerContract.TrackCfgBackupTable._ID)));
//        int criteriaInt = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_GPS_TIME_CRITERIA));
//        SmsCriteria criteria = SmsCriteria.fromInteger(criteriaInt);
//        int gpsThresholdMeters = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_GPS_THRESHOLD_METERS));
//        int gpsFov = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_GPS_FOV));
//        int gpsTimeout = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_GPS_TIMEOUT));
//        int timeTracking = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_TIME_TRACKING));
//        int timePre = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_TIME_PRE));
//        int timePost = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_TIME_POST));
//        int currId = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_CURR_ID));
//
//        mBackup = new TrackingConfiguration(id, criteria, gpsThresholdMeters, gpsFov, gpsTimeout,
//                timeTracking, timePre, timePost
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
     * que inicia o estado do objecto.
     */
    public TrackingConfiguration() {

    }

    public void parse(byte[] data) {
        float ltDegrees = ((((int)data[0] & 0xFF) << 24) +
                (((int)data[1] & 0xFF) << 16) +
                (((int)data[2] & 0xFF) << 8) +
                (((int)data[3] & 0xFF) << 0));
        ltDegrees = ltDegrees / 10000 / 60;
        float lgDegrees = ((((int)data[4] & 0xFF) << 24) +
                (((int)data[5] & 0xFF) << 16) +
                (((int)data[6] & 0xFF) << 8) +
                (((int)data[7] & 0xFF) << 0));
        lgDegrees = lgDegrees / 10000 / 60; // SUBSTITUIR POR SÍMBOLOS
        Location zeroLocation = new Location("Zero location");
        zeroLocation.setLatitude(0);
        zeroLocation.setLongitude(0);
        Location monThreshold = new Location("Mon threshold");
        monThreshold.setLatitude(ltDegrees);
        monThreshold.setLongitude(lgDegrees);
        float distance = zeroLocation.distanceTo(monThreshold);
        int gpsThresholdMeters = (int)distance;
        int _smsCriteria = (int)data[8] & 0xFF;
        SmsCriteria smsCriteria  = SmsCriteria.fromInteger(_smsCriteria);
        int gpsTimeoutMinutes   = ((int)data[9] & 0xFF);
        int gpsFov = ((int)data[10] & 0xFF);
        int trackTimeoutMinutes = ((int)data[11] & 0xFF);
        int preTimeoutMinutes   = ((int)data[12] & 0xFF);
        int postTimeoutMinutes  = ((int)data[13] & 0xFF);

        setGpsThreshold(gpsThresholdMeters);
        setSmsCriteria(smsCriteria);
        setGpsTimeout(gpsTimeoutMinutes);
        setGpsFov(gpsFov);
        setTimeoutTracking(trackTimeoutMinutes);
        setTimeoutPre(preTimeoutMinutes);
        setTimeoutPost(postTimeoutMinutes);
    }

}