package com.patri.guardtracker.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 * Created by patri on 21/04/2016.
 */
public class MonitoringInfo {
    public final static String TAG = MonitoringInfo.class.getSimpleName();
    public enum MonInfoItem {TEMPERATURE, SIM_BALANCE, BATTERY, POSITION}
    static private SQLiteDatabase db = null;
    private int _id;
    private GuardTracker mGuardTracker;
    private int mGuardTrackerId;
    private Date mDate;
    private Position mPosition;
    private int mPositionId;
    private int mBatCharge;
    private double mTemperature;
    private double mSimBalance;
    // A monitoring info instance may not have all fields filled.
    // Instead of having particular values to identify a non present field, it is preferable
    // to have a field for each data field setting if it is present or not.
    private boolean mHasPosition;
    private boolean mHasSimBalance;
    private boolean mHasTemperature;
    private boolean mHasBatCharge;
    static final private int MON_INFO_TEMPERATURE_PRECISION = 10;
    static final private int MON_INFO_BAT_BALANCE_PRECISION = 100;

    private static Context context;                     // Auxiliary context for demand loading from database

/*    static final public int TEMPERATURE_RAW_NULL_VALUE = Integer.MIN_VALUE;
    static final public int SIM_BALANCE_RAW_NULL_VALUE = Integer.MIN_VALUE;
    static final public int POSITION_RAW_NULL_VALUE    = 0; // There are no entries with 0 value (in all tables, the first entry has id equal to 1)
    static final public Position POSITION_NULL_VALUE   = null;
    static final public double TEMPERATURE_NULL_VALUE  = Double.MIN_VALUE;
    static final public double SIM_BALANCE_NULL_VALUE  = Double.MIN_VALUE;
    static final public int BAT_CHARGE_NULL_VALUE      = Integer.MIN_VALUE;
*/

    public MonitoringInfo(
                          GuardTracker guardTracker,
                          long date,
                          boolean isPosValid,
                          Position position,
                          boolean isTemperatureValid,
                          double temperature,
                          boolean isSimBalanceValid,
                          double simBalance,
                          boolean isBatChargeValid,
                          int batCharge)
    {
        this.mHasPosition = isPosValid;
        this.mHasBatCharge = isBatChargeValid;
        this.mHasSimBalance = isSimBalanceValid;
        this.mHasTemperature = isTemperatureValid;
        this.mDate = new Date(date);
        this.mGuardTracker = guardTracker;
        this.mBatCharge = batCharge;
        this.mPosition = position;
        this.mSimBalance = simBalance;
        this.mTemperature = temperature;
    }

    /**
     * Use to construct a new MonitoringInfo from data string.
     * This constructor is used to build new MonitoringInfos from SMS messages.
     * The MonitoringInfo constructed is added to database. It has an ID valid after constructor return.
     * @param guardTracker Integer with guardTracker identification. This parameter must not be 0.
     * @param date timestamp in miliseconds of received alert
     * @param alertBody String with monitoring information data to parse
     */
    public MonitoringInfo(GuardTracker guardTracker, long date, String alertBody) {

        // Parse alertBody
        Scanner scanner = new Scanner(alertBody);

        mHasPosition = false;
        mHasTemperature = false;
        mHasSimBalance = false;
        mHasBatCharge = false;
        while (scanner.hasNext()) {
            String tk = scanner.next();
            int paramType = tk.charAt(0) - '0';
            String paramValue = tk.substring(1);
            switch (paramType) {
                case 0: // Param type 0: GPS position data
                    Position position = new Position(null, paramValue);
                    mPositionId = position.get_id();
                    mPosition = position;
                    mHasPosition = true;
                    break;
                case 1: // Param type 1: Temperature data
                    double temp = Double.parseDouble(paramValue);
                    mTemperature = temp;
                    mHasTemperature = true;
                    break;
                case 2: // Param type 2: Sim balance in centimes
                    int sim = Integer.parseInt(paramValue);
                    mSimBalance = (double)sim / MON_INFO_BAT_BALANCE_PRECISION;
                    mHasSimBalance = true;
                    break;
                case 3: // Param type 3: Battery charge, ADC integer value
                    int bat = Integer.parseInt(paramValue);
                    mBatCharge = bat;
                    mHasBatCharge = true;
                    break;
            }
        }

        // Initialize object fields (the last fields)
        mGuardTracker = guardTracker;
        mDate = new Date(date);

//        // Add new Object into database
//        // Read GuardTracker database helper
//        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
//        // Initialize database for write
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//        // Prepare values to write to database
//        int temperatureRaw = (int)(mTemperature * MON_INFO_TEMPERATURE_PRECISION);
//        int simBalanceRaw  = (int)(mSimBalance  * MON_INFO_BAT_BALANCE_PRECISION);
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(GuardTrackerContract.MonInfoTable.COLUMN_NAME_GUARD_TRACKER, mGuardTracker);
//        contentValues.put(GuardTrackerContract.MonInfoTable.COLUMN_NAME_DATE, mDate.getTime());
//        contentValues.put(GuardTrackerContract.MonInfoTable.COLUMN_NAME_POSITION, hasPosition ? mPositionId : null);
//        contentValues.put(GuardTrackerContract.MonInfoTable.COLUMN_NAME_TEMPERATURE, hasTemperature ? temperatureRaw : null);
//        contentValues.put(GuardTrackerContract.MonInfoTable.COLUMN_NAME_BALANCE, hasSimBalance ? simBalanceRaw : null);
//        contentValues.put(GuardTrackerContract.MonInfoTable.COLUMN_NAME_BATTERY_CHARGE, hasBatCharge ? mBatCharge: null);
//        long pkId = db.insertOrThrow(GuardTrackerContract.MonInfoTable.TABLE_NAME, null, contentValues);
//        _id = (int)pkId;
//
//        db.close();
//        dbHelper.close();

    }


    public boolean  hasTemperature()      { return mHasTemperature; }
    public boolean  hasSimBalance()       { return mHasSimBalance; }
    public boolean  hasPosition()         { return mHasPosition; }
    public boolean  hasBatCharge()        { return mHasBatCharge; }
    public int      getBatCharge()        { return mBatCharge; }
    public Date     getDate()             { return mDate; }
    public int      getGuardTrackerId()   { return mGuardTrackerId; }
    public int      get_id()              { return _id; }
    public int      getPositionId()       { return mPositionId; }
    public double   getSimBalance()       { return mSimBalance; }
    public double   getTemperature()      { return mTemperature; }
    public Position getPosition()         {
        // On demand strategy: load track session from database only when of it's first use.
        if (mPosition == null && mPositionId != 0)
            mPosition = Position.read(MonitoringInfo.context, mPositionId);
        return mPosition;
    }

    public String   getPrettyDate()       { return String.format("%1$td/%1$tm/%1$tY %1$tR", mDate); }
    public String   getPrettyTemperature(){
        return mHasTemperature ? "<empty>" : String.format("%.1f ºC", mTemperature);
    }
    public String   getPrettyBalance()    {
        return mHasSimBalance ? "<empty>" : String.format("€%.2f", mSimBalance);
    }
    public String   getPrettyCharge()     {
        return mHasBatCharge ? "<empty>" : String.format("%d ADC", mBatCharge);
    }
    public void setPosition(Position position) { this.mPosition = position; this.mPositionId = position.get_id(); this.mHasPosition = true; }

    public void create(Context context) {
        if (mPositionId == 0 && mPosition != null) {
            mPosition.create(context);
            mPositionId = mPosition.get_id();
        }

        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for write
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Prepare values to write to database
        int temperatureRaw = 0;
        if (mHasTemperature)
            temperatureRaw = (int)(mTemperature * MON_INFO_TEMPERATURE_PRECISION);
        int simBalanceRaw = 0;
        if (mHasSimBalance)
            simBalanceRaw = (int)(mSimBalance * MON_INFO_BAT_BALANCE_PRECISION);
        ContentValues contentValues = new ContentValues();
        contentValues.put(GuardTrackerContract.MonInfoTable.COLUMN_NAME_GUARD_TRACKER, mGuardTrackerId);
        contentValues.put(GuardTrackerContract.MonInfoTable.COLUMN_NAME_DATE, mDate.getTime());
        contentValues.put(GuardTrackerContract.MonInfoTable.COLUMN_NAME_POSITION, mPositionId);
        contentValues.put(GuardTrackerContract.MonInfoTable.COLUMN_NAME_TEMPERATURE, temperatureRaw);
        contentValues.put(GuardTrackerContract.MonInfoTable.COLUMN_NAME_BALANCE, simBalanceRaw);
        contentValues.put(GuardTrackerContract.MonInfoTable.COLUMN_NAME_BATTERY_CHARGE, mBatCharge);
        long pkId = db.insertOrThrow(GuardTrackerContract.MonInfoTable.TABLE_NAME, null, contentValues);
        this._id = (int)pkId;

        db.close();
        dbHelper.close();
    }

    static private Cursor readCursor(Context context, int guardTrackerId) {
        // Cache context for demand loading from database
        MonitoringInfo.context = context;

        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for read
        db = dbHelper.getReadableDatabase();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] monInfoProjection = {
                GuardTrackerContract.MonInfoTable._ID,
                GuardTrackerContract.MonInfoTable.COLUMN_NAME_DATE,
                GuardTrackerContract.MonInfoTable.COLUMN_NAME_POSITION,
                GuardTrackerContract.MonInfoTable.COLUMN_NAME_TEMPERATURE,
                GuardTrackerContract.MonInfoTable.COLUMN_NAME_BALANCE,
                GuardTrackerContract.MonInfoTable.COLUMN_NAME_BATTERY_CHARGE
        };
        // Define columns WHERE clause.
        String monInfoSelection = GuardTrackerContract.MonInfoTable.COLUMN_NAME_GUARD_TRACKER + " LIKE ?";
        // Define values WHERE clause.
        String[] monInfoSelArgs = { String.valueOf(guardTrackerId) };

        Cursor cursor = db.query(
                GuardTrackerContract.MonInfoTable.TABLE_NAME,  // The table to query
                monInfoProjection,                             // The columns to return
                monInfoSelection,                              // The columns for the WHERE clause
                monInfoSelArgs,                                // The values for the WHERE clause
                null,                                               // don't group the rows
                null,                                               // don't filter by row groups
                null                                                // The sort order
        );
        return cursor;
    }
    static private MonitoringInfo buildMonitoringInfo(Cursor cursor, int guardTrackerId) {

        int   id            = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.MonInfoTable._ID));
        long  dateTimeRaw   = cursor.getLong(cursor.getColumnIndex(GuardTrackerContract.MonInfoTable.COLUMN_NAME_DATE));
        boolean isPosPresent = cursor.isNull(cursor.getColumnIndex(GuardTrackerContract.MonInfoTable.COLUMN_NAME_POSITION));
        boolean isTempPresent = cursor.isNull(cursor.getColumnIndex(GuardTrackerContract.MonInfoTable.COLUMN_NAME_TEMPERATURE));
        boolean isBalancePresent = cursor.isNull(cursor.getColumnIndex(GuardTrackerContract.MonInfoTable.COLUMN_NAME_BALANCE));
        boolean isBatChargePresent = cursor.isNull(cursor.getColumnIndex(GuardTrackerContract.MonInfoTable.COLUMN_NAME_BATTERY_CHARGE));
        int positionFk = 0, temperatureRaw, balanceRaw, batteryCharge = 0;
        double temperature = 0, balance = 0;
        if (isPosPresent)
            positionFk = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.MonInfoTable.COLUMN_NAME_POSITION));
        if (isTempPresent) {
            temperatureRaw = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.MonInfoTable.COLUMN_NAME_TEMPERATURE));
            temperature = ((double)temperatureRaw) / MON_INFO_TEMPERATURE_PRECISION;
        }
        if (isBalancePresent) {
            balanceRaw = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.MonInfoTable.COLUMN_NAME_BALANCE));
            balance = ((double)balanceRaw) / MON_INFO_BAT_BALANCE_PRECISION;
        }
        if (isBatChargePresent)
            batteryCharge = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.MonInfoTable.COLUMN_NAME_BATTERY_CHARGE));


        MonitoringInfo monInfo = new MonitoringInfo(null, dateTimeRaw,
                isPosPresent, null,
                isTempPresent, temperature,
                isBalancePresent, balance,
                isBatChargePresent, batteryCharge);
        monInfo._id = id;
        monInfo.mPositionId = positionFk;
        monInfo.mGuardTrackerId = guardTrackerId;

        return monInfo;
    }
    static private MonitoringInfo buildMonitoringInfo(Context context, Cursor cursor) {
        int   guardTrackerId = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.MonInfoTable.COLUMN_NAME_GUARD_TRACKER));

        MonitoringInfo monInfo = buildMonitoringInfo(cursor, guardTrackerId);
        return monInfo;
    }
    static public MonitoringInfo read(Context context, int monInfoId) {
        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for read
        db = dbHelper.getReadableDatabase();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] monInfoProjection = {
                GuardTrackerContract.MonInfoTable._ID,
                GuardTrackerContract.MonInfoTable.COLUMN_NAME_GUARD_TRACKER,
                GuardTrackerContract.MonInfoTable.COLUMN_NAME_DATE,
                GuardTrackerContract.MonInfoTable.COLUMN_NAME_POSITION,
                GuardTrackerContract.MonInfoTable.COLUMN_NAME_TEMPERATURE,
                GuardTrackerContract.MonInfoTable.COLUMN_NAME_BALANCE,
                GuardTrackerContract.MonInfoTable.COLUMN_NAME_BATTERY_CHARGE
        };
        // Define columns WHERE clause.
        String monInfoSelection = GuardTrackerContract.MonInfoTable._ID + " LIKE ?";
        // Define values WHERE clause.
        String[] monInfoSelArgs = { String.valueOf(monInfoId) };

        Cursor cursor = db.query(
                GuardTrackerContract.MonInfoTable.TABLE_NAME,  // The table to query
                monInfoProjection,                             // The columns to return
                monInfoSelection,                              // The columns for the WHERE clause
                monInfoSelArgs,                                // The values for the WHERE clause
                null,                                               // don't group the rows
                null,                                               // don't filter by row groups
                null                                                // The sort order
        );
        if (cursor.getCount() == 0) throw new IllegalStateException("No entries MonInfoTable");

        cursor.moveToFirst();

        MonitoringInfo monInfo = buildMonitoringInfo(context, cursor);
        cursor.close();
        db.close();
        return monInfo;
    }
    static public MonitoringInfo readByDate(Context context, long milliseconds) {
        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for read
        db = dbHelper.getReadableDatabase();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] monInfoProjection = {
                GuardTrackerContract.MonInfoTable._ID,
                GuardTrackerContract.MonInfoTable.COLUMN_NAME_GUARD_TRACKER,
                GuardTrackerContract.MonInfoTable.COLUMN_NAME_DATE,
                GuardTrackerContract.MonInfoTable.COLUMN_NAME_POSITION,
                GuardTrackerContract.MonInfoTable.COLUMN_NAME_TEMPERATURE,
                GuardTrackerContract.MonInfoTable.COLUMN_NAME_BALANCE,
                GuardTrackerContract.MonInfoTable.COLUMN_NAME_BATTERY_CHARGE
        };
        // Define columns WHERE clause.
        String monInfoSelection = GuardTrackerContract.MonInfoTable.COLUMN_NAME_DATE + " LIKE ?";
        // Define values WHERE clause.
        String[] monInfoSelArgs = { String.valueOf(milliseconds) };

        Cursor cursor = db.query(
                GuardTrackerContract.MonInfoTable.TABLE_NAME,  // The table to query
                monInfoProjection,                             // The columns to return
                monInfoSelection,                              // The columns for the WHERE clause
                monInfoSelArgs,                                // The values for the WHERE clause
                null,                                               // don't group the rows
                null,                                               // don't filter by row groups
                null                                                // The sort order
        );

        if (cursor.getCount() == 0) {
            cursor.close();
            db.close();
            return null;
        }

        cursor.moveToFirst();

        MonitoringInfo monInfo = buildMonitoringInfo(context, cursor);
        cursor.close();
        db.close();
        return monInfo;
    }
    static public ArrayList<MonitoringInfo> readList(Context context, int guardTrackerId) {
        ArrayList<MonitoringInfo> list = new ArrayList<>();

        Cursor cursor = readCursor(context, guardTrackerId);

        for (cursor.moveToFirst(); cursor.isAfterLast() != true; cursor.moveToNext()) {
            MonitoringInfo monInfo = buildMonitoringInfo(cursor, guardTrackerId);
            list.add(monInfo);
        }

        cursor.close();
        unload();
        return list;
    }

    public void update(Context context) {

        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for write
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Prepare values to write to database
        int temperatureRaw = 0;
        if (mHasTemperature)
            temperatureRaw = (int)(mTemperature * MON_INFO_TEMPERATURE_PRECISION);
        int simBalanceRaw = 0;
        if (mHasSimBalance)
            simBalanceRaw = (int)(mSimBalance * MON_INFO_BAT_BALANCE_PRECISION);
        ContentValues contentValues = new ContentValues();
        contentValues.put(GuardTrackerContract.MonInfoTable.COLUMN_NAME_GUARD_TRACKER, mGuardTrackerId);
        contentValues.put(GuardTrackerContract.MonInfoTable.COLUMN_NAME_DATE, mDate.getTime());
        contentValues.put(GuardTrackerContract.MonInfoTable.COLUMN_NAME_POSITION, mPositionId);
        contentValues.put(GuardTrackerContract.MonInfoTable.COLUMN_NAME_TEMPERATURE, temperatureRaw);
        contentValues.put(GuardTrackerContract.MonInfoTable.COLUMN_NAME_BALANCE, simBalanceRaw);
        contentValues.put(GuardTrackerContract.MonInfoTable.COLUMN_NAME_BATTERY_CHARGE, mBatCharge);
        // Define columns WHERE clause.
        String whereClause = GuardTrackerContract.MonInfoTable._ID + " LIKE ?";
        // Define values WHERE clause.
        String[] whereClauseArgs = { String.valueOf(_id) };

        int rowsUpdated = db.update(GuardTrackerContract.MonInfoTable.TABLE_NAME, contentValues, whereClause, whereClauseArgs);
        assert rowsUpdated == 1;

        db.close();
        dbHelper.close();
    }

    public static boolean delete(Context context, int id) {
        MonitoringInfo monInfo = MonitoringInfo.read(context, id);
        int posId = monInfo.getPositionId();
        boolean posDeleted = Position.delete(context, posId);

        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for write
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Define columns WHERE clause.
        String whereClause = GuardTrackerContract.MonInfoTable._ID + " LIKE ?";
        // Define values WHERE clause.
        String [] whereClauseArgs = { String.valueOf(id) };

        // Execute query
        int deletedItems = db.delete(GuardTrackerContract.MonInfoTable.TABLE_NAME, whereClause, whereClauseArgs);

        db.close();
        dbHelper.close();
        return deletedItems == 1 && posDeleted;

    }
    public static int delete(Context context, int [] ids) {

        int deletedItems = 0;
        for (int id: ids) {
            deletedItems += delete(context, id) ? 1 : 0;
        };
        return deletedItems;
    }

    public static int deleteByGuardTracker(Context context, int guardTrackerId) {
        ArrayList<MonitoringInfo> monInfoList = MonitoringInfo.readList(context, guardTrackerId);

        if (monInfoList.size() == 0)
            return 0;

        // Build positions id
        int [] posIds = new int [monInfoList.size()];
        int i = 0;
        for (MonitoringInfo monInfo :
                monInfoList) {
            int posId = monInfo.getPositionId();
            posIds[i] = posId;
            i += 1;
        }
        // Delete postions id
        Position.delete(context, posIds);

        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for write
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Define columns WHERE clause.
        String whereClause = GuardTrackerContract.MonInfoTable.COLUMN_NAME_GUARD_TRACKER + " LIKE ?";
        // Define values WHERE clause.
        String [] whereClauseArgs = { String.valueOf(guardTrackerId) };

        // Execute query
        int deletedItems = db.delete(GuardTrackerContract.MonInfoTable.TABLE_NAME, whereClause, whereClauseArgs);

        db.close();
        dbHelper.close();
        return deletedItems;

    }
    public boolean delete(Context context) {
        return delete(context, this._id);
    }

    static public void unload() {
        if (db != null) db.close();
        db = null;
    }


}
