package com.patri.guardtracker.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by patri on 21/04/2016.
 */
public class VigilanceConfiguration {
    private int _id;
    private int mTiltLevel;
    private int mBleAdvertisePeriod;

    public VigilanceConfiguration(int tiltLevel, int bleAdvertisePeriod) {
        this.mTiltLevel = tiltLevel;
        this.mBleAdvertisePeriod = bleAdvertisePeriod;
    }

    public int get_id()                         { return _id; }
    public int getmTiltLevel()                  { return mTiltLevel; }
    public int getBleAdvertisePeriod()          { return mBleAdvertisePeriod; }
    public void setTiltLevel(int mTiltLevel)   { this.mTiltLevel = mTiltLevel; }
    public void setBleAdvertisePeriod(int bleAdvertisePeriod) {
        this.mBleAdvertisePeriod = bleAdvertisePeriod;
    }

    public String getPrettyTiltSensitivity()    { return "" + mTiltLevel + " ???"; }
    public String getPrettyBleAdvertisePeriod() {
        StringBuilder stringBuilder = new StringBuilder();
        int hours = mBleAdvertisePeriod/60/60;
        if (hours > 0) {
            stringBuilder.append(hours).append(" hours   ");
            stringBuilder.append(hours > 1 ? "s   " : "   ");
        }
        int minutes = (mBleAdvertisePeriod/60) % 60;
        if (minutes > 0) {
            stringBuilder.append(minutes).append(" minute");
            stringBuilder.append(minutes > 1 ? "s   " : "   ");
        }
        int seconds = mBleAdvertisePeriod % 60;
        if (seconds > 0) {
            stringBuilder.append(seconds).append(" second");
            stringBuilder.append(seconds > 1 ? "s   " : "   ");
        }
        return stringBuilder.toString();
    }

    public void create(Context context) {
        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for write
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(GuardTrackerContract.VigilanceCfgTable.COLUMN_NAME_TILT_LEVEL_CRITERIA, mTiltLevel);
        contentValues.put(GuardTrackerContract.VigilanceCfgTable.COLUMN_NAME_BLE_ADVERTISEMENT_PERIOD, mBleAdvertisePeriod);
        long pkId = db.insertOrThrow(GuardTrackerContract.VigilanceCfgTable.TABLE_NAME, null, contentValues);
        this._id = (int)pkId;

        db.close();
        dbHelper.close();
    }

    public static VigilanceConfiguration read(Context context, int id) {
        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for read
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] vigilanceCfgProjection = {
                GuardTrackerContract.VigilanceCfgTable.COLUMN_NAME_TILT_LEVEL_CRITERIA,
                GuardTrackerContract.VigilanceCfgTable.COLUMN_NAME_BLE_ADVERTISEMENT_PERIOD
        };
        // Define columns WHERE clause.
        String vigilanceCfgSelection = GuardTrackerContract.VigilanceCfgTable._ID + " LIKE ?";
        // Define values WHERE clause.
        String[] vigilanceCfgSelArgs = { String.valueOf(id) };

        final Cursor cursor = db.query(
                GuardTrackerContract.VigilanceCfgTable.TABLE_NAME,  // The table to query
                vigilanceCfgProjection,                             // The columns to return
                vigilanceCfgSelection,                              // The columns for the WHERE clause
                vigilanceCfgSelArgs,                                // The values for the WHERE clause
                null,                                         // don't group the rows
                null,                                         // don't filter by row groups
                null                                          // The sort order
        );
        if (cursor.getCount() != 1) throw new IllegalArgumentException();

        cursor.moveToFirst();
        // Extract values from query result
        int tiltLevel = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.VigilanceCfgTable.COLUMN_NAME_TILT_LEVEL_CRITERIA));
        int bleAdvertisePeriod = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.VigilanceCfgTable.COLUMN_NAME_BLE_ADVERTISEMENT_PERIOD));

        VigilanceConfiguration vigilanceCfg = new VigilanceConfiguration(tiltLevel, bleAdvertisePeriod);
        vigilanceCfg._id = id;

        cursor.close();
        db.close();
        dbHelper.close();

        return vigilanceCfg;
    }

    public void update(Context context)
    {
        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for write
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(GuardTrackerContract.VigilanceCfgTable.COLUMN_NAME_TILT_LEVEL_CRITERIA, mTiltLevel);
        contentValues.put(GuardTrackerContract.VigilanceCfgTable.COLUMN_NAME_BLE_ADVERTISEMENT_PERIOD, mBleAdvertisePeriod);
        // Define columns WHERE clause.
        String whereClause = GuardTrackerContract.VigilanceCfgTable._ID + " LIKE ?";
        // Define values WHERE clause.
        String[] whereClauseArgs = { String.valueOf(_id) };

        int rowsUpdated = db.update(GuardTrackerContract.VigilanceCfgTable.TABLE_NAME, contentValues, whereClause, whereClauseArgs);
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
        String whereClause = GuardTrackerContract.VigilanceCfgTable._ID + " LIKE ?";
        // Define values WHERE clause.
        String [] whereClauseArgs = { String.valueOf(id) };

        // Execute query
        int deletedItems = db.delete(GuardTrackerContract.VigilanceCfgTable.TABLE_NAME, whereClause, whereClauseArgs);

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
        String whereClause = GuardTrackerContract.VigilanceCfgTable._ID + " LIKE ?";
        // Define values WHERE clause.
        String [] whereClauseArgs = new String[ids.length];
        int i = 0;
        for (int id: ids) {
            whereClauseArgs[i++] = String.valueOf(id);
        };

        // Execute query
        int deletedItems = db.delete(GuardTrackerContract.VigilanceCfgTable.TABLE_NAME, whereClause, whereClauseArgs);

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
        String whereClause = GuardTrackerContract.VigilanceCfgTable._ID + " LIKE ?";
        // Define values WHERE clause.
        String [] whereClauseArgs = { String.valueOf(_id) };

        // Execute query
        int deletedItems = db.delete(GuardTrackerContract.VigilanceCfgTable.TABLE_NAME, whereClause, whereClauseArgs);

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
//        contentValues.put(GuardTrackerContract.VigilanceCfgBackupTable.COLUMN_NAME_TILT_LEVEL_CRITERIA, mTiltLevel);
//        contentValues.put(GuardTrackerContract.VigilanceCfgBackupTable.COLUMN_NAME_BLE_ADVERTISEMENT_PERIOD, mBleAdvertisePeriod);
//        contentValues.put(GuardTrackerContract.VigilanceCfgBackupTable.COLUMN_NAME_CURR_ID, _id);
//        long pkId = db.insertOrThrow(GuardTrackerContract.VigilanceCfgBackupTable.TABLE_NAME, null, contentValues);
//
//        mBackupId = (int)pkId;
//        mBackup = new VigilanceConfiguration(_id, mTiltLevel, mBleAdvertisePeriod);
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
//        String whereClause = GuardTrackerContract.VigilanceCfgBackupTable._ID + " LIKE ?";
//        String[] whereClauseArgs = { String.valueOf(mBackup._id) };
//
//        // Execute query
//        int deletedItems = db.delete(GuardTrackerContract.VigilanceCfgBackupTable.TABLE_NAME, whereClause, whereClauseArgs);
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
//        contentValues.put(GuardTrackerContract.VigilanceCfgBackupTable.COLUMN_NAME_TILT_LEVEL_CRITERIA, mTiltLevel);
//        contentValues.put(GuardTrackerContract.VigilanceCfgBackupTable.COLUMN_NAME_BLE_ADVERTISEMENT_PERIOD, mBleAdvertisePeriod);
//        contentValues.put(GuardTrackerContract.VigilanceCfgBackupTable.COLUMN_NAME_CURR_ID, _id);
//
//        // Define columns WHERE clause.
//        String whereClause = GuardTrackerContract.VigilanceCfgBackupTable._ID + " LIKE ?";
//        String[] whereClauseArgs = { String.valueOf(mBackup._id) };
//
//        // Execute query
//        int updatedItems = db.update(GuardTrackerContract.VigilanceCfgBackupTable.TABLE_NAME, contentValues, whereClause, whereClauseArgs);
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
//                GuardTrackerContract.VigilanceCfgBackupTable._ID,
//                GuardTrackerContract.VigilanceCfgBackupTable.COLUMN_NAME_TILT_LEVEL_CRITERIA,
//                GuardTrackerContract.VigilanceCfgBackupTable.COLUMN_NAME_BLE_ADVERTISEMENT_PERIOD,
//                GuardTrackerContract.VigilanceCfgBackupTable.COLUMN_NAME_CURR_ID
//        };
//        // Define columns WHERE clause.
//        String backupSelection = GuardTrackerContract.VigilanceCfgBackupTable._ID + " LIKE ?";
//        // Define values WHERE clause.
//        String[] backupSelArgs = {String.valueOf(mBackupId)};
//
//        Cursor cursor = db.query(
//                GuardTrackerContract.VigilanceCfgBackupTable.TABLE_NAME,  // The table to query
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
//        int id = cursor.getInt((cursor.getColumnIndex(GuardTrackerContract.VigilanceCfgBackupTable._ID)));
//        int tiltLevel = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.VigilanceCfgBackupTable.COLUMN_NAME_TILT_LEVEL_CRITERIA));
//        int bleAdvertisePeriod = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.VigilanceCfgBackupTable.COLUMN_NAME_BLE_ADVERTISEMENT_PERIOD));
//        int currId = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.VigilanceCfgBackupTable.COLUMN_NAME_CURR_ID));
//
//        mBackup = new VigilanceConfiguration(id, tiltLevel, bleAdvertisePeriod);
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
    public VigilanceConfiguration() {

    }

    public void parse(byte[] data) {
        int tiltLevelCriteria = ((int) data[0] & 0xFF);
        int bleAdvertisementPeriod = ((int) data[1] & 0xFF << 8) + ((int) data[2] & 0xFF);

        setTiltLevel(tiltLevelCriteria);
        setBleAdvertisePeriod(bleAdvertisementPeriod);
    }
}
