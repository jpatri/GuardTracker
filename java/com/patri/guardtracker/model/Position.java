package com.patri.guardtracker.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.IllegalFormatException;
import java.util.StringTokenizer;

/**
 * Created by patri on 21/04/2016.
 */
public class Position {
    public final static String TAG = Position.class.getSimpleName();
    static private SQLiteDatabase db = null;
    static private Cursor cursor;
    static final private int GPS_BLE_MSG_SIZE = 18;
    static final private int GPS_BLE_MIN_PRECISION = 10000;
    static final private int GPS_DEGREES_PRECISION = 1000000;
    static final private int GPS_ALTITUDE_PRECISION = 10;
    static final private int GPS_HDOP_PRECISION = 100;

    private int _id;
    private int mTrackSessionId;
    private TrackSession mTrackSession;
    private double mLatitude;
    private double mLongitude;
    private Date mTime;
    private double mAltitude;
    private double mHdop;
    private int mFixed;
    private int mSat;

    private static Context context;                     // Auxiliary context for demand loading from database

    /*    protected Position(int id,
                       int trackSessionFk,
                       double latitude,
                       double longitude,
                       Date time,
                       double altitude,
                       int nSat,
                       double hDop,
                       int fixed) {
        this._id = id;
        this.mTrackSessionId = trackSessionFk;
        this.mFixed = fixed;
        this.mAltitude = altitude;
        this.mHdop = hDop;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mTime = time;
        this.mSat = nSat;
    }*/
    public Position(double latitude,
                    double longitude,
                    Date time,
                    double altitude,
                    int nSat,
                    double hDop,
                    int fixed) {
        this.mFixed = fixed;
        this.mAltitude = altitude;
        this.mHdop = hDop;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mTime = time;
        this.mSat = nSat;
    }

    /**
     * Use to construct a new Position from data string.
     * This constructor is used to build new Positions from SMS messages.
     * The position constructed is added to database. It has an ID valid after constructor return.
     *
     * @param trackSessionId Integer with session identification. This parameter may be 0 indicating
     *                       that the new Position belongs to a MonitoringInfo alert.
     * @param positionBody   String with position data to parse
     */
    public Position(TrackSession trackSession, String positionBody) {
        final String DELIMITER_TOKEN = ",";

        // Parse positionBody
        String aux;
        StringTokenizer tk = new StringTokenizer(positionBody, DELIMITER_TOKEN);
        aux = tk.nextToken();
        String ltStr = aux.substring(0, aux.length() - 1);
        char cardinal = aux.charAt(aux.length() - 1);
        int factorLt = (cardinal == 'N' ? 1 : -1);
        double lt = Double.parseDouble(ltStr);
        int ltInt = (int) (lt / 100);
        double ltMinutes = lt % 100;
        double ltDegrees = ltInt + ltMinutes / 60;
        ltDegrees = ltDegrees * factorLt;
        aux = tk.nextToken();
        String lgStr = aux.substring(0, aux.length() - 1);
        cardinal = aux.charAt(aux.length() - 1);
        int factorLg = (cardinal == 'E' ? 1 : -1);
        double lg = Double.parseDouble(lgStr);
        int lgInt = (int) (lg / 100);
        double lgMinutes = lg % 100;
        double lgDegrees = lgInt + lgMinutes / 60;
        lgDegrees = lgDegrees * factorLg;
        String timeStr = tk.nextToken();
        int hour = Character.digit(timeStr.charAt(0), 10);
        hour = hour * 10 + Character.digit(timeStr.charAt(1), 10);
        int minute = Character.digit(timeStr.charAt(2), 10);
        minute = minute * 10 + Character.digit(timeStr.charAt(3), 10);
        int second = Character.digit(timeStr.charAt(4), 10);
        second = second * 10 + Character.digit(timeStr.charAt(5), 10);
        int secondsOfDay = hour * 60 * 60 + minute * 60 + second;
        Date time = new Date(secondsOfDay * 1000);
        //String dummy = tk.nextToken();  // Not used
        String altStr = tk.nextToken();
        double alt = Double.parseDouble(altStr);
        //String velStr = tk.nextToken(); // Not used
        String satStr = tk.nextToken();
        int nSat = Integer.parseInt(satStr);
        String hDopStr = tk.nextToken();
        double hDop = Double.parseDouble(hDopStr);
        String fixedStr = tk.nextToken();
        int fixed = Integer.parseInt(fixedStr);

        // Initialize object fields.
        mTrackSession = trackSession;
        if (mTrackSession != null)
            mTrackSessionId = trackSession.get_id();
        mLatitude = ltDegrees;
        mLongitude = lgDegrees;
        mTime = time;
        mAltitude = alt;
        mSat = nSat;
        mHdop = hDop;
        mFixed = fixed;

// A inserção na base de dados é realizada fora do constructor.
//        // Add new Object into database
//        // Read GuardTracker database helper
//        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
//        // Initialize database for write
//        final SQLiteDatabase db = dbHelper.getWritableDatabase();
//        // Prepare values to write to database
//        int latRaw = (int) (mLatitude * GPS_DEGREES_PRECISION);
//        int lngRaw = (int) (mLongitude * GPS_DEGREES_PRECISION);
//        int altRaw = (int) (mAltitude * GPS_ALTITUDE_PRECISION);
//        int hDopRaw = (int) (mHdop * GPS_HDOP_PRECISION);
//        int timeRaw = (int) time.getTime();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(GuardTrackerContract.PositionTable.COLUMN_NAME_SESSION, mTrackSessionId == 0 ? null : mTrackSessionId);
//        contentValues.put(GuardTrackerContract.PositionTable.COLUMN_NAME_LT, latRaw);
//        contentValues.put(GuardTrackerContract.PositionTable.COLUMN_NAME_LG, lngRaw);
//        contentValues.put(GuardTrackerContract.PositionTable.COLUMN_NAME_TIME, timeRaw);
//        contentValues.put(GuardTrackerContract.PositionTable.COLUMN_NAME_ALTITUDE, altRaw);
//        contentValues.put(GuardTrackerContract.PositionTable.COLUMN_NAME_NSAT, mSat);
//        contentValues.put(GuardTrackerContract.PositionTable.COLUMN_NAME_HDOP, hDopRaw);
//        contentValues.put(GuardTrackerContract.PositionTable.COLUMN_NAME_FIXED, mFixed);
//        long pkId = db.insertOrThrow(GuardTrackerContract.PositionTable.TABLE_NAME, null, contentValues);
//
//        _id = (int) pkId;
//
//        db.close();

    }

    /**
     * This constructor is called when message is received from ble.
     * This constructor does not create entry in database.
     */
    public Position(byte[] bleMsg) {
        if (bleMsg.length < GPS_BLE_MSG_SIZE)
            throw new InvalidParameterException(
                    "Error in BLE message: not enough bytes. Should receive "
                            + GPS_BLE_MSG_SIZE + " and received " + bleMsg.length
            );

        // Latitude
        int latDecMin = 0;
        latDecMin = latDecMin << 8 | (bleMsg[0] & 0xFF);
        latDecMin = latDecMin << 8 | (bleMsg[1] & 0xFF);
        latDecMin = latDecMin << 8 | (bleMsg[2] & 0xFF);
        latDecMin = latDecMin << 8 | (bleMsg[3] & 0xFF);
        double latMin = (double) latDecMin / GPS_BLE_MIN_PRECISION;
        mLatitude = latMin / 60;
        // Longitude
        int lngDecMin = 0;
        lngDecMin = lngDecMin << 8 | (bleMsg[4] & 0xFF);
        lngDecMin = lngDecMin << 8 | (bleMsg[5] & 0xFF);
        lngDecMin = lngDecMin << 8 | (bleMsg[6] & 0xFF);
        lngDecMin = lngDecMin << 8 | (bleMsg[7] & 0xFF);
        double lngMin = (double) lngDecMin / GPS_BLE_MIN_PRECISION;
        mLongitude = lngMin / 60;
        // Time
        int secondsOfDay = 0;
        secondsOfDay = secondsOfDay << 8 | (bleMsg[8] & 0xFF);
        secondsOfDay = secondsOfDay << 8 | (bleMsg[9] & 0xFF);
        secondsOfDay = secondsOfDay << 8 | (bleMsg[10] & 0xFF);
        mTime = new Date(secondsOfDay * 1000);
        // Altitude
        int altitude = 0;
        altitude = altitude << 8 | (bleMsg[11] & 0xFF);
        altitude = altitude << 8 | (bleMsg[12] & 0xFF);
        altitude = altitude << 8 | (bleMsg[13] & 0xFF);
        mAltitude = (double) altitude / GPS_ALTITUDE_PRECISION;
        // Satellites
        int satellites = bleMsg[14];
        mSat = satellites;
        // Hdop
        int hDopDec = 0;
        hDopDec = hDopDec << 8 | (bleMsg[15] & 0xFF);
        hDopDec = hDopDec << 8 | (bleMsg[16] & 0xFF);
        mHdop = (double) hDopDec / GPS_HDOP_PRECISION;
        // Fix
        mFixed = bleMsg[17];
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int id) {
        _id = id;
    } // Used to update an already created instance

    public int getTrackSessionId() {
        return mTrackSessionId;
    }
    public void setTrackSessionId(int trackSessionId) { mTrackSessionId = trackSessionId; }
    public TrackSession getTrackSession() {
        // On demand strategy: load track session from database only when of it's first use.
        if (mTrackSession == null && mTrackSessionId != 0)
            mTrackSession = TrackSession.read(Position.context, mTrackSessionId);
        return mTrackSession;
    }
    public void setTrackSession(TrackSession trackSession) { mTrackSession = trackSession; mTrackSessionId = trackSession.get_id(); }

    public double getAltitude() {
        return mAltitude;
    }

    public int getFixed() {
        return mFixed;
    }

    public double getHdop() {
        return mHdop;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public Date getTime() {
        return mTime;
    }

    public int getSatellites() {
        return mSat;
    }

    public String getPrettyAltitude() {
        return String.format("%.1f m", mAltitude);
    }

    public String getPrettyHdop() {
        return String.format("%.2f", mHdop);
    }

    public String getPrettyFixed() {
        return String.format("%d", mFixed);
    }

    public String getPrettySatellites() {
        return String.format("%d", mSat);
    }

    public String getPrettyTime() {
        return String.format("%tT", mTime);
    }

    public String getPrettyLatitude() {
        return String.format("%.5f%c", Math.abs(mLatitude), mLatitude < 0 ? 'S' : 'N');
    }

    public String getPrettyLongitude() {
        return String.format("%.5f%c", Math.abs(mLongitude), mLongitude < 0 ? 'W' : 'E');
    }

    public void create(Context context) {
        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for write
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Prepare values to write to database
        int latRaw = (int) (mLatitude * GPS_DEGREES_PRECISION);
        int lngRaw = (int) (mLongitude * GPS_DEGREES_PRECISION);
        int altRaw = (int) (mAltitude * GPS_ALTITUDE_PRECISION);
        int hDopRaw = (int) (mHdop * GPS_HDOP_PRECISION);
        int timeRaw = (int) mTime.getTime();
        ContentValues contentValues = new ContentValues();
        contentValues.put(GuardTrackerContract.PositionTable.COLUMN_NAME_SESSION, mTrackSessionId == 0 ? null : mTrackSessionId);
        contentValues.put(GuardTrackerContract.PositionTable.COLUMN_NAME_LT, latRaw);
        contentValues.put(GuardTrackerContract.PositionTable.COLUMN_NAME_LG, lngRaw);
        contentValues.put(GuardTrackerContract.PositionTable.COLUMN_NAME_TIME, timeRaw);
        contentValues.put(GuardTrackerContract.PositionTable.COLUMN_NAME_ALTITUDE, altRaw);
        contentValues.put(GuardTrackerContract.PositionTable.COLUMN_NAME_NSAT, mSat);
        contentValues.put(GuardTrackerContract.PositionTable.COLUMN_NAME_HDOP, hDopRaw);
        contentValues.put(GuardTrackerContract.PositionTable.COLUMN_NAME_FIXED, mFixed);
        long pkId = db.insertOrThrow(GuardTrackerContract.PositionTable.TABLE_NAME, null, contentValues);
        this._id = (int)pkId;

        db.close();
        dbHelper.close();
    }

    static private Cursor readRawCursor(Context context, String positionSelection, String[] positionSelArgs) {
        // Cache context for demand loading from database
        Position.context = context;

        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for read
        db = dbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] positionProjection = {
                GuardTrackerContract.PositionTable._ID,
                GuardTrackerContract.PositionTable.COLUMN_NAME_SESSION,
                GuardTrackerContract.PositionTable.COLUMN_NAME_LT,
                GuardTrackerContract.PositionTable.COLUMN_NAME_LG,
                GuardTrackerContract.PositionTable.COLUMN_NAME_TIME,
                GuardTrackerContract.PositionTable.COLUMN_NAME_ALTITUDE,
                GuardTrackerContract.PositionTable.COLUMN_NAME_NSAT,
                GuardTrackerContract.PositionTable.COLUMN_NAME_HDOP,
                GuardTrackerContract.PositionTable.COLUMN_NAME_FIXED
        };

        cursor = db.query(
                GuardTrackerContract.PositionTable.TABLE_NAME,  // The table to query
                positionProjection,                             // The columns to return
                positionSelection,                              // The columns for the WHERE clause
                positionSelArgs,                                // The values for the WHERE clause
                null,                                               // don't group the rows
                null,                                               // don't filter by row groups
                null                                                // The sort order
        );
        if (cursor.getCount() == 0) throw new IllegalStateException("No entries PositionTable");

        // O cursor dependerá dos db ou dbHelper? Se não depender, deverei fechar db e dbHelper.
        return cursor;
    }
    /**
     * Read Cursor based on array of id's.
     *
     * @param context
     * @param id
     * @return
     */
    static public Cursor readCursor(Context context, int[] id) {
        // Build WHERE clause
        StringBuilder positionSelBuilder = new StringBuilder();
        ArrayList<String> positionSelArgsList = new ArrayList<String>();
        positionSelBuilder.append(GuardTrackerContract.PositionTable._ID);
        positionSelBuilder.append(" LIKE ?");
        positionSelArgsList.add("" + id[0]);
        for (int i = 1; i < id.length; i++) {
            // Define columns WHERE clause.
            positionSelBuilder.append(" OR ");
            positionSelBuilder.append(GuardTrackerContract.PositionTable._ID);
            positionSelBuilder.append(" LIKE ?");
            positionSelArgsList.add("" + id[i]);
        }
        // Define columns WHERE clause.
        String positionSelection = positionSelBuilder.toString();
        // Define values WHERE clause.
        String[] positionSelArgs = new String[positionSelArgsList.size()];
        positionSelArgs = positionSelArgsList.toArray(positionSelArgs);
        Cursor cursor = readRawCursor(context, positionSelection, positionSelArgs);
        return cursor;
    }


    static private Position buildPosition(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.PositionTable._ID));
        int trackSessionId =
                cursor.isNull(cursor.getColumnIndex(GuardTrackerContract.PositionTable.COLUMN_NAME_SESSION)) ?
                        0 :
                        cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.PositionTable.COLUMN_NAME_SESSION));
        int ltRaw = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.PositionTable.COLUMN_NAME_LT));
        int lgRaw = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.PositionTable.COLUMN_NAME_LG));
        int altRaw = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.PositionTable.COLUMN_NAME_ALTITUDE));
        int timeRaw = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.PositionTable.COLUMN_NAME_TIME));
        int nSat = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.PositionTable.COLUMN_NAME_NSAT));
        int hdopRaw = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.PositionTable.COLUMN_NAME_HDOP));
        int fixed = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.PositionTable.COLUMN_NAME_FIXED));

        double latitude = (double) ltRaw / GPS_DEGREES_PRECISION;
        double longitude = (double) lgRaw / GPS_DEGREES_PRECISION;

        Date time = new Date(timeRaw);

        double alt = (double) altRaw / GPS_ALTITUDE_PRECISION;

        double hdop = (double) hdopRaw / GPS_HDOP_PRECISION;

        Position position = new Position(latitude, longitude, time, alt, nSat, hdop, fixed);
        position.set_id(id);
        // On demand strategy: loads track session from database only when of it's first use.
        if (trackSessionId != 0)
            position.setTrackSessionId(trackSessionId);
        return position;
    }

    /**
     * Read list of positions based on array of id's.
     *
     * @param context
     * @param id
     * @return
     */
    static public ArrayList<Position> readList(Context context, int[] id) {
        ArrayList<Position> list = new ArrayList<>();
        Cursor cursor = readCursor(context, id);

        for (cursor.moveToFirst(); cursor.isAfterLast() != true; cursor.moveToNext()) {
            Position position = buildPosition(cursor);
            list.add(position);
        }

        cursor.close();
        db.close();

        return list;
    }

    /**
     * Read position based on id
     *
     * @param context
     * @param id
     * @return
     */
    static public Position read(Context context, int id) {
        int[] ids = {id};
        ArrayList<Position> list = readList(context, ids);
        Position position = list.get(0);
        return position;
    }

    /**
     * Read list of positions belonging to a track session.
     *
     * @param context
     * @param trackSessionId
     * @return
     */
    static public ArrayList<Position> readFromTrackSession(Context context, int trackSessionId) {
        ArrayList<Position> list = new ArrayList<>();

        // Define columns WHERE clause.
        String positionSelection = GuardTrackerContract.PositionTable.COLUMN_NAME_SESSION + " LIKE ?";
        // Define values WHERE clause.
        String[] positionSelArgs = {String.valueOf(trackSessionId)};
        Cursor cursor = readRawCursor(context, positionSelection, positionSelArgs);

        for (cursor.moveToFirst(); cursor.isAfterLast(); cursor.moveToNext()) {
            Position position = buildPosition(cursor);
            list.add(position);
        }

        cursor.close();
        db.close();

        return list;
    }

    static public void unload() {
        if (cursor != null) cursor.close();
        if (db != null) db.close();
    }

    /**
     * Called when a ble message is received with a position data.
     *
     * @param context
     */
    public void update(Context context) {
        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for write
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Prepare values to write to database
        int latRaw = (int) (mLatitude * GPS_DEGREES_PRECISION);
        int lngRaw = (int) (mLongitude * GPS_DEGREES_PRECISION);
        int altRaw = (int) (mAltitude * GPS_ALTITUDE_PRECISION);
        int hDopRaw = (int) (mHdop * GPS_HDOP_PRECISION);
        int timeRaw = (int) mTime.getTime();
        ContentValues contentValues = new ContentValues();
        contentValues.put(GuardTrackerContract.PositionTable.COLUMN_NAME_SESSION, mTrackSessionId == 0 ? null : mTrackSessionId);
        contentValues.put(GuardTrackerContract.PositionTable.COLUMN_NAME_LT, latRaw);
        contentValues.put(GuardTrackerContract.PositionTable.COLUMN_NAME_LG, lngRaw);
        contentValues.put(GuardTrackerContract.PositionTable.COLUMN_NAME_TIME, timeRaw);
        contentValues.put(GuardTrackerContract.PositionTable.COLUMN_NAME_ALTITUDE, altRaw);
        contentValues.put(GuardTrackerContract.PositionTable.COLUMN_NAME_NSAT, mSat);
        contentValues.put(GuardTrackerContract.PositionTable.COLUMN_NAME_HDOP, hDopRaw);
        contentValues.put(GuardTrackerContract.PositionTable.COLUMN_NAME_FIXED, mFixed);
        // Define columns WHERE clause.
        String whereClause = GuardTrackerContract.PositionTable._ID + " LIKE ?";
        // Define values WHERE clause.
        String[] whereClauseArgs = {String.valueOf(_id)};

        int rowsUpdated = db.update(GuardTrackerContract.PositionTable.TABLE_NAME, contentValues, whereClause, whereClauseArgs);
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
        String whereClause = GuardTrackerContract.PositionTable._ID + " LIKE ?";
        // Define values WHERE clause.
        String[] whereClauseArgs = {String.valueOf(id)};

        // Execute query
        int deletedItems = db.delete(GuardTrackerContract.PositionTable.TABLE_NAME, whereClause, whereClauseArgs);

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
        String whereClause = GuardTrackerContract.PositionTable._ID + " LIKE ?";
        // Define values WHERE clause.
        String[] whereClauseArgs = new String[ids.length];
        int i = 0;
        for (int id : ids) {
            whereClauseArgs[i++] = String.valueOf(id);
        }

        // Execute query
        int deletedItems = db.delete(GuardTrackerContract.PositionTable.TABLE_NAME, whereClause, whereClauseArgs);

        db.close();
        dbHelper.close();
        return deletedItems;
    }
    public boolean delete(Context context) {
        return delete(context, this._id);
    }

    public static int deleteByTrackSession(Context context, int trackSessionId) {
        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for write
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Define columns WHERE clause.
        String whereClause = GuardTrackerContract.PositionTable.COLUMN_NAME_SESSION + " LIKE ?";
        // Define values WHERE clause.
        String[] whereClauseArgs = {String.valueOf(trackSessionId)};

        // Execute query
        int deletedItems = db.delete(GuardTrackerContract.PositionTable.TABLE_NAME, whereClause, whereClauseArgs);

        db.close();
        dbHelper.close();
        return deletedItems;

    }



}