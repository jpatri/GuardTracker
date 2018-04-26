package com.patri.guardtracker.model;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by patri on 15/04/2016.
 */
public class GuardTrackerDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "GuardTracker.db";
    public static final String DATABASE_PATH = "/data/data/com.patri.guardtracker/databases/";
    public static final String DATABASE_PATHNAME = DATABASE_PATH + DATABASE_NAME;

    private static final String PRIMARY_KEY = " PRIMARY KEY";
    private static final String FOREIGN_KEY = "";
    private static final String AUTOINCREMENT = " AUTOINCREMENT";
    private static final String UNIQUE = " UNIQUE";
    private static final String DEFAULT = " DEFAULT";
    private static final String TEXT_TYPE = " TEXT";
    private static final String VARCHAR255_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String DATETIME_TYPE = " NUMERIC";
    private static final String BOOLEAN_TYPE = " NUMERIC";
    private static final String REAL_TYPE = " REAL";
    private static final String COMMA_SEP = ",";
    private static final String NOT_NULL = " NOT NULL";

    // Acabei por não utilizar para listar o menu com o nome das tabelas
    //public static final String SQL_GET_TABLES_NAMES =
    //        "SELECT name FROM sqlite_master WHERE type='table'";
    private static final String SQL_CREATE_GUARD_TRACKER =
            "CREATE TABLE " + GuardTrackerContract.GuardTrackerTable.TABLE_NAME + " (" +
                    GuardTrackerContract.GuardTrackerTable._ID + INTEGER_TYPE + PRIMARY_KEY + AUTOINCREMENT + COMMA_SEP +
                    GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_BLE_ID + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_GSM_ID + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_NAME + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_WAKE_SENSORS + INTEGER_TYPE + NOT_NULL +  COMMA_SEP +
                    GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_POS_REF + INTEGER_TYPE + FOREIGN_KEY + COMMA_SEP +
                    GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_LAST_MON_INFO + INTEGER_TYPE + FOREIGN_KEY + COMMA_SEP +
                    GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_MON_CFG + INTEGER_TYPE + FOREIGN_KEY + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_TRACK_CFG + INTEGER_TYPE + FOREIGN_KEY + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_VIGILANCE_CFG + INTEGER_TYPE + FOREIGN_KEY + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_OWNER_PHONE_NUMBER + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_SYNC + BOOLEAN_TYPE + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_NEXT + INTEGER_TYPE + FOREIGN_KEY +
            " )";
    private static final String SQL_DELETE_GUARD_TRACKER =
            "DROP TABLE IF EXISTS " + GuardTrackerContract.GuardTrackerTable.TABLE_NAME;

//    private static final String SQL_CREATE_GUARD_TRACKER_BACKUP =
//            "CREATE TABLE " + GuardTrackerContract.GuardTrackerBackupTable.TABLE_NAME + " (" +
//                    GuardTrackerContract.GuardTrackerBackupTable._ID + INTEGER_TYPE + PRIMARY_KEY + AUTOINCREMENT + COMMA_SEP +
//                    GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_BLE_ID + TEXT_TYPE + UNIQUE + NOT_NULL + COMMA_SEP +
//                    GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_GSM_ID + TEXT_TYPE + UNIQUE + NOT_NULL + COMMA_SEP +
//                    GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_NAME + TEXT_TYPE + NOT_NULL + COMMA_SEP +
//                    GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_WAKE_SENSORS + INTEGER_TYPE + NOT_NULL +  COMMA_SEP +
//                    GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_POS_REF + INTEGER_TYPE + FOREIGN_KEY + COMMA_SEP +
//                    GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_LAST_MON_INFO + INTEGER_TYPE + FOREIGN_KEY + COMMA_SEP +
//                    GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_MON_CFG + INTEGER_TYPE + FOREIGN_KEY + NOT_NULL + COMMA_SEP +
//                    GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_TRACK_CFG + INTEGER_TYPE + FOREIGN_KEY + NOT_NULL + COMMA_SEP +
//                    GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_VIGILANCE_CFG + INTEGER_TYPE + FOREIGN_KEY + NOT_NULL + COMMA_SEP +
//                    GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_OWNER_PHONE_NUMBER + TEXT_TYPE + NOT_NULL + COMMA_SEP +
//                    GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_CURR_ID + INTEGER_TYPE + FOREIGN_KEY + NOT_NULL +
//                    " )";
//    private static final String SQL_DELETE_GUARD_TRACKER_BACKUP =
//            "DROP TABLE IF EXISTS " + GuardTrackerContract.GuardTrackerBackupTable.TABLE_NAME;

    private static final String SQL_CREATE_CONTACTS =
            "CREATE TABLE " + GuardTrackerContract.ContactsTable.TABLE_NAME + " (" +
                    GuardTrackerContract.ContactsTable._ID + INTEGER_TYPE + PRIMARY_KEY + AUTOINCREMENT + COMMA_SEP +
                    GuardTrackerContract.ContactsTable.COLUMN_NAME_GUARD_TRACKER + INTEGER_TYPE + FOREIGN_KEY + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.ContactsTable.COLUMN_NAME_PHONE_NUMBER + TEXT_TYPE + NOT_NULL +
                    " )";
    private static final String SQL_DELETE_CONTACTS =
            "DROP TABLE IF EXISTS " + GuardTrackerContract.ContactsTable.TABLE_NAME;

    private static final String SQL_CREATE_MON_CFG =
            "CREATE TABLE " + GuardTrackerContract.MonCfgTable.TABLE_NAME + " (" +
                    GuardTrackerContract.MonCfgTable._ID + INTEGER_TYPE + PRIMARY_KEY + AUTOINCREMENT + COMMA_SEP +
                    GuardTrackerContract.MonCfgTable.COLUMN_NAME_TIME + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.MonCfgTable.COLUMN_NAME_PERIOD + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.MonCfgTable.COLUMN_NAME_SMS_CRITERIA + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.MonCfgTable.COLUMN_NAME_GPS_THRESHOLD_METERS + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.MonCfgTable.COLUMN_NAME_GPS_FOV + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.MonCfgTable.COLUMN_NAME_GPS_TIMEOUT + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.MonCfgTable.COLUMN_NAME_TEMP_HIGH + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.MonCfgTable.COLUMN_NAME_TEMP_LOW + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.MonCfgTable.COLUMN_NAME_SIM_BALANCE_THRESHOLD + INTEGER_TYPE + NOT_NULL +
                    " )";
    private static final String SQL_DELETE_MON_CFG =
            "DROP TABLE IF EXISTS " + GuardTrackerContract.MonCfgTable.TABLE_NAME;
//    private static final String SQL_CREATE_MON_CFG_BACKUP =
//            "CREATE TABLE " + GuardTrackerContract.MonCfgTable.TABLE_NAME + " (" +
//                    GuardTrackerContract.MonCfgBackupTable._ID + INTEGER_TYPE + PRIMARY_KEY + AUTOINCREMENT + COMMA_SEP +
//                    GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_TIME + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
//                    GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_PERIOD + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
//                    GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_SMS_CRITERIA + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
//                    GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_GPS_THRESHOLD_METERS + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
//                    GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_GPS_FOV + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
//                    GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_GPS_TIMEOUT + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
//                    GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_TEMP_HIGH + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
//                    GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_TEMP_LOW + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
//                    GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_SIM_BALANCE_THRESHOLD + INTEGER_TYPE + NOT_NULL +
//                    GuardTrackerContract.MonCfgBackupTable.COLUMN_NAME_CURR_ID + INTEGER_TYPE + FOREIGN_KEY + NOT_NULL +
//                    " )";
//    private static final String SQL_DELETE_MON_CFG_BACKUP =
//            "DROP TABLE IF EXISTS " + GuardTrackerContract.MonCfgBackupTable.TABLE_NAME;

    private static final String SQL_CREATE_TRACK_CFG =
            "CREATE TABLE " + GuardTrackerContract.TrackCfgTable.TABLE_NAME + " (" +
                    GuardTrackerContract.TrackCfgTable._ID + INTEGER_TYPE + PRIMARY_KEY + AUTOINCREMENT + COMMA_SEP +
                    GuardTrackerContract.TrackCfgTable.COLUMN_NAME_GPS_THRESHOLD_METERS + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.TrackCfgTable.COLUMN_NAME_GPS_TIME_CRITERIA + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.TrackCfgTable.COLUMN_NAME_GPS_TIMEOUT + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.TrackCfgTable.COLUMN_NAME_GPS_FOV + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.TrackCfgTable.COLUMN_NAME_TIME_TRACKING + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.TrackCfgTable.COLUMN_NAME_TIME_PRE + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.TrackCfgTable.COLUMN_NAME_TIME_POST + INTEGER_TYPE + NOT_NULL +
                    " )";
    private static final String SQL_DELETE_TRACK_CFG =
            "DROP TABLE IF EXISTS " + GuardTrackerContract.TrackCfgTable.TABLE_NAME;
//    private static final String SQL_CREATE_TRACK_CFG_BACKUP =
//            "CREATE TABLE " + GuardTrackerContract.TrackCfgBackupTable.TABLE_NAME + " (" +
//                    GuardTrackerContract.TrackCfgBackupTable._ID + INTEGER_TYPE + PRIMARY_KEY + AUTOINCREMENT + COMMA_SEP +
//                    GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_GPS_THRESHOLD_METERS + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
//                    GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_GPS_TIME_CRITERIA + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
//                    GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_GPS_TIMEOUT + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
//                    GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_GPS_FOV + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
//                    GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_TIME_TRACKING + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
//                    GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_TIME_PRE + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
//                    GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_TIME_POST + INTEGER_TYPE + NOT_NULL +
//                    GuardTrackerContract.TrackCfgBackupTable.COLUMN_NAME_CURR_ID + INTEGER_TYPE + FOREIGN_KEY + NOT_NULL +
//                    " )";
//    private static final String SQL_DELETE_TRACK_CFG_BACKUP =
//            "DROP TABLE IF EXISTS " + GuardTrackerContract.TrackCfgBackupTable.TABLE_NAME;

    private static final String SQL_CREATE_VIGILANCE_CFG =
            "CREATE TABLE " + GuardTrackerContract.VigilanceCfgTable.TABLE_NAME + " (" +
                    GuardTrackerContract.VigilanceCfgTable._ID + INTEGER_TYPE + PRIMARY_KEY + AUTOINCREMENT + COMMA_SEP +
                    GuardTrackerContract.VigilanceCfgTable.COLUMN_NAME_TILT_LEVEL_CRITERIA + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.VigilanceCfgTable.COLUMN_NAME_BLE_ADVERTISEMENT_PERIOD + INTEGER_TYPE + NOT_NULL +
                    " )";
    private static final String SQL_DELETE_VIGILANCE_CFG =
            "DROP TABLE IF EXISTS " + GuardTrackerContract.VigilanceCfgTable.TABLE_NAME;
//    private static final String SQL_CREATE_VIGILANCE_CFG_BACKUP =
//            "CREATE TABLE " + GuardTrackerContract.VigilanceCfgBackupTable.TABLE_NAME + " (" +
//                    GuardTrackerContract.VigilanceCfgBackupTable._ID + INTEGER_TYPE + PRIMARY_KEY + AUTOINCREMENT + COMMA_SEP +
//                    GuardTrackerContract.VigilanceCfgBackupTable.COLUMN_NAME_TILT_LEVEL_CRITERIA + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
//                    GuardTrackerContract.VigilanceCfgBackupTable.COLUMN_NAME_BLE_ADVERTISEMENT_PERIOD + INTEGER_TYPE + NOT_NULL +
//                    GuardTrackerContract.VigilanceCfgBackupTable.COLUMN_NAME_CURR_ID + INTEGER_TYPE + FOREIGN_KEY + NOT_NULL +
//                    " )";
//    private static final String SQL_DELETE_VIGILANCE_CFG_BACKUP =
//            "DROP TABLE IF EXISTS " + GuardTrackerContract.VigilanceCfgBackupTable.TABLE_NAME;

    private static final String SQL_CREATE_MON_INFO =
            "CREATE TABLE " + GuardTrackerContract.MonInfoTable.TABLE_NAME + " (" +
                    GuardTrackerContract.MonInfoTable._ID + INTEGER_TYPE + PRIMARY_KEY + AUTOINCREMENT + COMMA_SEP +
                    GuardTrackerContract.MonInfoTable.COLUMN_NAME_GUARD_TRACKER + INTEGER_TYPE + FOREIGN_KEY + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.MonInfoTable.COLUMN_NAME_DATE + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.MonInfoTable.COLUMN_NAME_POSITION + INTEGER_TYPE + FOREIGN_KEY + COMMA_SEP +
                    GuardTrackerContract.MonInfoTable.COLUMN_NAME_BATTERY_CHARGE + INTEGER_TYPE + COMMA_SEP +
                    GuardTrackerContract.MonInfoTable.COLUMN_NAME_TEMPERATURE + INTEGER_TYPE + COMMA_SEP +
                    GuardTrackerContract.MonInfoTable.COLUMN_NAME_BALANCE + INTEGER_TYPE +
                    " )";
    private static final String SQL_DELETE_MON_INFO =
            "DROP TABLE IF EXISTS " + GuardTrackerContract.MonInfoTable.TABLE_NAME;

    private static final String SQL_CREATE_TRACK_SESSION =
            "CREATE TABLE " + GuardTrackerContract.TrackSessionTable.TABLE_NAME + " (" +
                    GuardTrackerContract.TrackSessionTable._ID + INTEGER_TYPE + PRIMARY_KEY + AUTOINCREMENT + COMMA_SEP +
                    GuardTrackerContract.TrackSessionTable.COLUMN_NAME_GUARD_TRACKER + INTEGER_TYPE + FOREIGN_KEY + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.TrackSessionTable.COLUMN_NAME_DATE + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.TrackSessionTable.COLUMN_NAME_NAME + TEXT_TYPE + NOT_NULL +
                    " )";
    private static final String SQL_DELETE_TRACK_SESSION =
            "DROP TABLE IF EXISTS " + GuardTrackerContract.TrackSessionTable.TABLE_NAME;

    private static final String SQL_CREATE_POSITION =
            "CREATE TABLE " + GuardTrackerContract.PositionTable.TABLE_NAME + " (" +
                    GuardTrackerContract.PositionTable._ID + INTEGER_TYPE + PRIMARY_KEY + AUTOINCREMENT + COMMA_SEP +
                    GuardTrackerContract.PositionTable.COLUMN_NAME_SESSION + INTEGER_TYPE + FOREIGN_KEY + COMMA_SEP +
                    GuardTrackerContract.PositionTable.COLUMN_NAME_LT + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.PositionTable.COLUMN_NAME_LG + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.PositionTable.COLUMN_NAME_ALTITUDE + INTEGER_TYPE  + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.PositionTable.COLUMN_NAME_TIME + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.PositionTable.COLUMN_NAME_NSAT + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.PositionTable.COLUMN_NAME_HDOP + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                    GuardTrackerContract.PositionTable.COLUMN_NAME_FIXED + INTEGER_TYPE + NOT_NULL +
                    " )";
    private static final String SQL_DELETE_POSITION =
            "DROP TABLE IF EXISTS " + GuardTrackerContract.PositionTable.TABLE_NAME;

    public GuardTrackerDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_GUARD_TRACKER);
        db.execSQL(SQL_CREATE_CONTACTS);
        db.execSQL(SQL_CREATE_MON_INFO);
        db.execSQL(SQL_CREATE_MON_CFG);
        db.execSQL(SQL_CREATE_TRACK_CFG);
        db.execSQL(SQL_CREATE_VIGILANCE_CFG);
        db.execSQL(SQL_CREATE_TRACK_SESSION);
        db.execSQL(SQL_CREATE_POSITION);
    }

    public void onDelete(SQLiteDatabase db) {
        db.execSQL(SQL_DELETE_GUARD_TRACKER);
        db.execSQL(SQL_DELETE_CONTACTS);
        db.execSQL(SQL_DELETE_MON_INFO);
        db.execSQL(SQL_DELETE_MON_CFG);
        db.execSQL(SQL_DELETE_TRACK_CFG);
        db.execSQL(SQL_DELETE_VIGILANCE_CFG);
        db.execSQL(SQL_DELETE_TRACK_SESSION);
        db.execSQL(SQL_DELETE_POSITION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        onDelete(db);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    static public void onDeleteDb() {
        boolean res = SQLiteDatabase.deleteDatabase(new File(DATABASE_PATHNAME));
        assert res == true;
    }

//    public ArrayList<Cursor> getData(String rawQuery) {
//        SQLiteDatabase db = getReadableDatabase();
//        Cursor cursor = db.rawQuery(rawQuery, null);
//        ArrayList<Cursor> list = new ArrayList();
//        list.add(cursor);
//        return list;
//    }
    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "mesage" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);


        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);


            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {


                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){

            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }


    }

}
