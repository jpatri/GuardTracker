package com.patri.guardtracker.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by patri on 21/04/2016.
 */
public class GuardTracker {
    public final static String TAG = GuardTracker.class.getSimpleName();

    // Database index fields
    static private SQLiteDatabase db = null;
    //static private Cursor cursor = null;
    private int _id;                                    // Primary key in GuardTracker table
    private int mTrackSessionOnFlyId;                   // Foreign key to track session on the fly
    private int mPosRefId;                              // Foreign key to reference position
    private int mMonCfgId;                              // Foreign key to monitoring configuration
    private int mTrackCfgId;                            // Foreign key to track session configuration
    private int mVigilanceCfgId;                        // Foreign key to vigilance configuration
    private int mLastMonInfoId;                         // Foreign key to last monitoring information
    private int mNextId;                                // Foreign key to backup of this instance

    // Data type fields
    private String mName;                               // Name of device
    private String mBleId;                              // BLE address
    private String mGsmId;                              // GSM phone number (remote device)
    private int mWakeSensorsStatus;                     // Sensors mask
    private Position mPosRef;                           // Reference position
    private MonitoringInfo mLastMonInfo;                // Last monitoring information
    private ArrayList<TrackSession> mTrackSessions;     // List of track sessions
    private TrackSession mTrackSessionOnFly;            // Track session on the fly
    private ArrayList<MonitoringInfo> mStatistics;      // List of monitoring information
    private InternalStatus mInternalStatus;             // Internal status
    private MonitoringConfiguration mMonCfg;            // Monitoring configuration
    private TrackingConfiguration mTrackCfg;            // Tracking configuration
    private VigilanceConfiguration mVigilanceCfg;       // Vigilance configuration
    private String mOwnerPhoneNumber;                   // Owner's mobile phone number
    private List<String> mContactsSecondaryList = new ArrayList<>();    // List with secondary contacts
    private GuardTracker mNext;                         // Backup of this instance (last synched values are stored in this object)
    private boolean mSync;                              // Is synched with remote paired device

    private static Context context;                     // Auxiliary context for demand loading from database
    private boolean mBleIsConnected;

    public GuardTracker (String name, String ble, String gsm, String owner, int wakeMask, Position pos, MonitoringInfo last,
                         MonitoringConfiguration monCfg, TrackingConfiguration trackCfg, VigilanceConfiguration vigCfg,
                         boolean sync, GuardTracker next) {
        mName = name;
        mBleId = ble;
        mGsmId = gsm;
        mOwnerPhoneNumber = owner;
        mWakeSensorsStatus = wakeMask;
        mPosRef = pos;
        mLastMonInfo = last;
        mMonCfg = monCfg;
        mTrackCfg = trackCfg;
        mVigilanceCfg = vigCfg;
        mSync = sync;
        mNext = next;
        // All other field are zero.
    }
//    public GuardTracker (String name, String ble, String gsm, String owner, int wakeMask, Position pos,
//                         MonitoringConfiguration monCfg, TrackingConfiguration trackCfg, VigilanceConfiguration vigCfg) {
//        this(name, ble, gsm, owner, wakeMask, pos, null, monCfg, trackCfg, vigCfg);
//    }
//    public GuardTracker (String name, String ble, String gsm, String owner, int wakeMask,
//                         MonitoringConfiguration monCfg, TrackingConfiguration trackCfg, VigilanceConfiguration vigCfg) {
//        this(name, ble, gsm, owner, wakeMask, null, null, monCfg, trackCfg, vigCfg);
//    }
//    protected GuardTracker(int id, String name, String  bleId, String gsmId, String ownerPhoneNumber, int wakeSensorsStatus, int posRefFk, int lastMonInfoFk, int monCfgFk, int trackCfgFk, int vigilanceCfgFk) {
//        _id = id;
//        mName = name;
//        mBleId = bleId;
//        mGsmId = gsmId;
//        mWakeSensorsStatus = wakeSensorsStatus;
//        mLastMonInfoId = lastMonInfoFk;
//        mPosRefId = posRefFk;
//        mMonCfgId = monCfgFk;
//        mTrackCfgId = trackCfgFk;
//        mVigilanceCfgId = vigilanceCfgFk;
//        mOwnerPhoneNumber = ownerPhoneNumber;
//    }
//    protected GuardTracker(int id, String name, String  bleId, String gsmId, String ownerPhoneNumber, int wakeSensorsStatus, int posRefFk, int lastMonInfoFk, int monCfgFk, int trackCfgFk, int vigilanceCfgFk, boolean sync, int backupFk) {
//        this(id, name, bleId, gsmId, ownerPhoneNumber, wakeSensorsStatus, posRefFk, lastMonInfoFk, monCfgFk, trackCfgFk, vigilanceCfgFk);
//    }
//    protected GuardTracker(int id, String name, String  bleId, String gsmId, String ownerPhoneNumber, int wakeSensorsStatus, int posRefFk, int lastMonInfoFk, int monCfgFk, int trackCfgFk, int vigilanceCfgFk, int currId) {
//        this(id, name, bleId, gsmId, ownerPhoneNumber, wakeSensorsStatus, posRefFk, lastMonInfoFk, monCfgFk, trackCfgFk, vigilanceCfgFk);
//        this.mCurrId = currId;
//    }
//    protected GuardTracker(int id, String name, String  bleId, String gsmId, String ownerPhoneNumber, int wakeSensorsStatus, int monCfgFk, int trackCfgFk, int vigilanceCfgFk) {
//        this(id, name, bleId, gsmId, ownerPhoneNumber, wakeSensorsStatus, 0,0, monCfgFk, trackCfgFk, vigilanceCfgFk);
//    }
//    protected GuardTracker(int id, String name, String  bleId, String gsmId, String ownerPhoneNumber, int wakeSensorsStatus, int monCfgFk, int trackCfgFk, int vigilanceCfgFk, boolean sync, int backupId) {
//        this(id, name, bleId, gsmId, ownerPhoneNumber, wakeSensorsStatus, 0,0, monCfgFk, trackCfgFk, vigilanceCfgFk);
//        this.mSync = sync;
//        this.mBackupId = backupId;
//    }
//    protected GuardTracker(int id, String name, String  bleId, String gsmId, String ownerPhoneNumber, int posRefId, int lastMonInfoId) {
//        _id = id;
//        mName = name;
//        mBleId = bleId;
//        mGsmId = gsmId;
//        mPosRefId = posRefId;
//        mLastMonInfoId = lastMonInfoId;
//        mBleIsConnected = false;
//        mOwnerPhoneNumber = ownerPhoneNumber;
//    }

    /* Getters */
    public int get_id()                         { return _id; }
    public String getName()                     { return mName; }
    public String getBleId()                    { return mBleId; }
    public String getGsmId()                    { return mGsmId; }
    public String getOwnerPhoneNumber()         { return mOwnerPhoneNumber; }
    public int getWakeSensors()                 { return mWakeSensorsStatus; }
    public InternalStatus getInternalStatus()   { return mInternalStatus; }
    public Position getPosRef()                 {
        if (mPosRef == null && mPosRefId != 0)
            mPosRef = Position.read(context, mPosRefId);
        return mPosRef;
    }
    public TrackSession getTrackSessionOnFly()  {
        if (mTrackSessionOnFly == null && mTrackSessionOnFlyId != 0)
            mTrackSessionOnFly = TrackSession.read(context, mTrackSessionOnFlyId);
        return mTrackSessionOnFly;
    }
    public MonitoringConfiguration getMonCfg()  {
        if (mMonCfg == null && mMonCfgId != 0)
            mMonCfg = MonitoringConfiguration.read(context, mMonCfgId);
        return mMonCfg;
    }
    public TrackingConfiguration getTrackCfg()  {
        if (mTrackCfg == null && mTrackCfgId != 0)
            mTrackCfg = TrackingConfiguration.read(context, mTrackCfgId);
        return mTrackCfg;
    }
    public VigilanceConfiguration getVigCfg()   {
        if (mVigilanceCfg == null && mVigilanceCfgId != 0)
            mVigilanceCfg = VigilanceConfiguration.read(context, mVigilanceCfgId);
        return mVigilanceCfg;
    }
    public MonitoringInfo getLastMonInfo()      {
        if (mLastMonInfo == null && mLastMonInfoId != 0)
            mLastMonInfo = MonitoringInfo.read(context, mLastMonInfoId);
        return mLastMonInfo;
    }

    public int getMonCfgId()                    { return mMonCfgId; }
    public int getTrackCfgId()                  { return mTrackCfgId; }
    public int getVigCfgId()                    { return mVigilanceCfgId; }
    public int getPosRefId()                    { return mPosRefId; }
    public int getLastMonInfoId()               { return mLastMonInfoId; }

    public boolean isBleConnected()             { return mBleIsConnected; }
    public boolean isEnabled()                  { return (mWakeSensorsStatus & 0x3) != 0; }

    public List<String> getSecondaryContacts()  { return mContactsSecondaryList; }
    public boolean getSync()                    { return mSync; }
    public int getNextId()                      { return mNextId; }
    public GuardTracker getNext()             {
        if (mNext == null && mNextId != 0)
            mNext = GuardTracker.read(context, mNextId);
        return mNext;
    }

    public String getPrettyBle()                { return mBleId; }
    public String getPrettyGsm()                { return mGsmId; }

    /* Setters */
    public void setMonCfgId(int monCfgId)                               { this.mMonCfgId = monCfgId; }
    public void setMonCfg(MonitoringConfiguration monCfg)               { this.mMonCfg = monCfg; this.mMonCfgId = monCfg.get_id(); }
    public void setTrackCfgId(int trackCfgId)                           { this.mTrackCfgId = trackCfgId; }
    public void setTrackCfg(TrackingConfiguration trackCfg)             { this.mTrackCfg = trackCfg; this.mTrackCfgId = trackCfg.get_id(); }
    public void setVigilanceCfgId(int vigilanceCfgId)                   { this.mVigilanceCfgId = vigilanceCfgId; }
    public void setVigilanceCfg(VigilanceConfiguration vigilanceCfg)    { this.mVigilanceCfg = vigilanceCfg; this.mVigilanceCfgId = vigilanceCfg.get_id(); }
    public void setName(String name)                                    { this.mName = name; }
    public void setBleId(String bleAddress)                             { this.mBleId = bleAddress; }
    public void setGsmId(String gsmPhoneNumber)                         { this.mGsmId = gsmPhoneNumber; }
    public void setPosRefId(int posRefId)                               { this.mPosRefId = posRefId; }
    public void setPosRef(Position posRef)                              { this.mPosRef = posRef; this.mPosRefId = posRef.get_id(); }
    public void setLastMonInfo(MonitoringInfo lastMonInfo)              { this.mLastMonInfo = lastMonInfo; this.mLastMonInfoId = lastMonInfo.get_id(); }
    public void setTrackSessionOnFly(TrackSession trackSessionOnFly)    { this.mTrackSessionOnFly = trackSessionOnFly; }
    public void setInternalStatus(InternalStatus internalStatus)        { this.mInternalStatus = internalStatus; }
    public void setBleConnected()                                       { this.mBleIsConnected = true; }
    public void setBleDisconnected()                                    { this.mBleIsConnected = false; }
    public void setWakeSensors(int bitmask)                             { this.mWakeSensorsStatus = bitmask; }
    public void setOwnerPhoneNumber(String ownerPhoneNumber)            { this.mOwnerPhoneNumber = ownerPhoneNumber; }
    public void setSecondaryContacts(List<String> contacts)             { this.mContactsSecondaryList = contacts; }
    public void clearSecondaryContacts()                                { this.mContactsSecondaryList.clear(); }
    public void addSecondaryContact(String contact)                     { this.mContactsSecondaryList.add(contact); }
    public void removeSecondaryContact(String contact)                  { this.mContactsSecondaryList.remove(contact); }
    public void setNextId(int nextId)                                   { this.mNextId = nextId; }
    public void setNext(GuardTracker next)                              { this.mNext = next; }

    // Database operations (CRUD)
    // Auxiliary method to cleanup database resources
    static public void unload(Cursor cursor) {
        if (cursor != null) cursor.close();
        if (db != null) db.close();
        db = null;
    }

    /**
     *  Create GuardTrackerTable entry based on self object.
     *  Also create entries in  MoritoringConfigTable, TrackingConfigTable, VigilanceConfigTable
     *  and, if the object has a valid reference position, also create an entry in PositionTable.
     *  This operation does not have impact on MonitoringInfoTable, TrackSessionTable
     *  and Backup tables.
     */
    public void create(Context context) {
        if (mMonCfgId == 0 && mMonCfg != null) {
            mMonCfg.create(context);
            mMonCfgId = mMonCfg.get_id();
        }
        if (mTrackCfgId == 0 && mTrackCfg != null) {
            mTrackCfg.create(context);
            mTrackCfgId = mTrackCfg.get_id();
        }
        if (mVigilanceCfgId == 0 && mVigilanceCfg != null) {
            mVigilanceCfg.create(context);
            mVigilanceCfgId = mVigilanceCfg.get_id();
        }
        if (mPosRefId == 0 && mPosRef != null) {
            mPosRef.create(context);
            mPosRefId = mPosRef.get_id();
        }
        // Do not make much sense want to create monInfo and backup entries when creating a new GuardTracker:
        // - A new GuardTracker do not has an id and this value is mandatory to create monInfo entries.
        // - A new GuardTracker do not has a backup.
        // Well, but in the meantime if there are any valid reference in lastMonInfo or in next,
        // then it is not an error to create an entry in database.
        if (mLastMonInfoId == 0 && mLastMonInfo != null) {
            mLastMonInfo.create(context);
            mLastMonInfoId = mLastMonInfo.get_id();
        }
        if (mNextId == 0 && mNext != null) {
            mNext.create(context);
            mNextId = mNext.get_id();
        }

        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for write
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_NAME, mName);
        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_BLE_ID, mBleId);
        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_GSM_ID, mGsmId);
        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_WAKE_SENSORS, mWakeSensorsStatus);
        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_MON_CFG, mMonCfgId);
        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_TRACK_CFG, mTrackCfgId);
        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_VIGILANCE_CFG, mVigilanceCfgId);
        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_OWNER_PHONE_NUMBER, mOwnerPhoneNumber);
        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_POS_REF, mPosRefId == 0 ? null : mPosRefId);
        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_LAST_MON_INFO, mLastMonInfoId == 0 ? null : mLastMonInfoId);
        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_SYNC, mSync);
        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_NEXT, mNextId == 0 ? null : mNextId);
        long pkId = db.insertOrThrow(GuardTrackerContract.GuardTrackerTable.TABLE_NAME, null, contentValues);
        _id = (int)pkId;

        db.close();
        dbHelper.close();

        List<String> contacts = getSecondaryContacts();
        for(String contact: contacts) {
            SecondaryContactsDbHelper.create(context, _id, contact);
        }

    }

//    static public GuardTracker create(
//            Context context,
//            String name,
//            String bleAddress,
//            String gsmPhoneNumber,
//            String ownerPhoneNumber,
//            int wakeSensorsStatus,
//            int monCfgFk,
//            int trackCfgFk,
//            int vigilanceCfgFk,
//            boolean sync,
//            int backupFk)
//    {
//        // Read GuardTracker database helper
//        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
//        // Initialize database for write
//        final SQLiteDatabase db = dbHelper.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_NAME, name);
//        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_BLE_ID, bleAddress);
//        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_GSM_ID, gsmPhoneNumber);
//        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_WAKE_SENSORS, wakeSensorsStatus);
//        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_MON_CFG, monCfgFk);
//        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_TRACK_CFG, trackCfgFk);
//        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_VIGILANCE_CFG, vigilanceCfgFk);
//        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_OWNER_PHONE_NUMBER, ownerPhoneNumber);
//        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_SYNC, sync);
//        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_LAST_ID, backupFk);
//        long pkId = db.insertOrThrow(GuardTrackerContract.GuardTrackerTable.TABLE_NAME, null, contentValues);
//        final GuardTracker guardTracker = new GuardTracker((int)pkId,
//                name, bleAddress, gsmPhoneNumber, ownerPhoneNumber,
//                wakeSensorsStatus,
//                monCfgFk, trackCfgFk, vigilanceCfgFk, sync, backupFk
//        );
//
//        db.close();
//        dbHelper.close();
//        return guardTracker;
//    }

    /**
     * Read all columns for guard tracker devices. This function must be used when the client iterates trough the return value.
     *
     * @param context
     * @return a list of GuardTracker objects
     */
    static public Cursor readCursor(Context context, String selection, String[] selectionArgs) {
        // Cache context for demand loading from database
        GuardTracker.context = context;

        if (db == null) {
            GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
            db = dbHelper.getReadableDatabase();
        }

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                GuardTrackerContract.GuardTrackerTable._ID,
                GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_NAME,
                GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_BLE_ID,
                GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_GSM_ID,
                GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_WAKE_SENSORS,
                GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_POS_REF,
                GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_LAST_MON_INFO,
                GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_MON_CFG,
                GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_TRACK_CFG,
                GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_VIGILANCE_CFG,
                GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_OWNER_PHONE_NUMBER,
                GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_SYNC,
                GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_NEXT
        };

        Cursor cursor = db.query(
                GuardTrackerContract.GuardTrackerTable.TABLE_NAME,  // The table to query
                projection,                                         // The columns to return
                selection,                                          // The columns for the WHERE clause
                selectionArgs,                                      // The values for the WHERE clause
                null,                                               // don't group the rows
                null,                                               // don't filter by row groups
                null                                                // The sort order
        );

        return cursor;
    }

    /**
     * Auxiliary method to create a list of GuardTrackers from the data present in a cursor.
     * @param cursor
     * @return
     */
    static private ArrayList<GuardTracker> extractGuardTrackersFromCursor(Cursor cursor) {
        ArrayList<GuardTracker> list = new ArrayList<>();
        for (cursor.moveToFirst(); ! cursor.isAfterLast(); cursor.moveToNext()) {

            int id = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.GuardTrackerTable._ID));
            String name = cursor.getString(cursor.getColumnIndex(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_NAME));
            String bleAddr = cursor.getString(cursor.getColumnIndex(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_BLE_ID));
            String phoneNumber = cursor.getString(cursor.getColumnIndex(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_GSM_ID));
            String ownerPhoneNumber = cursor.getString(cursor.getColumnIndex(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_OWNER_PHONE_NUMBER));
            int wakeSensors = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_WAKE_SENSORS));
            int posRefFk = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_POS_REF));
            int lastMonInfoFk = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_LAST_MON_INFO));
            int monCfgFk = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_MON_CFG));
            int trackCfgFk = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_TRACK_CFG));
            int vigilanceCfgFk = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_VIGILANCE_CFG));
            boolean sync = cursor.getInt(cursor.getColumnIndex((GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_SYNC))) != 0;
            int nextFk = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_NEXT));
            GuardTracker gt = new GuardTracker(name, bleAddr, phoneNumber, ownerPhoneNumber, wakeSensors,
                    null, null, null, null, null, sync, null);
            // On demand load from database. Only keeps foreign keys. Load data objects from database only when necessary.
            gt._id = id;
            gt.mPosRefId = posRefFk;
            gt.mLastMonInfoId = lastMonInfoFk;
            gt.mMonCfgId = monCfgFk;
            gt.mTrackCfgId = trackCfgFk;
            gt.mVigilanceCfgId = vigilanceCfgFk;
            gt.mNextId = nextFk;
            list.add(gt);
        }
        return list;
    }

    /**
     * Read all guard tracker devices that has reference position setted.
     *
     * @param context
     * @return a list of GuardTracker objects
     */
    static public ArrayList<GuardTracker> readMapped(Context context) {

        // Selection/Where clause
        // Define columns WHERE clause.
        String selection = GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_POS_REF + " IS NOT NULL";
        // Execute query
        Cursor cursor = readCursor(context, selection, null);
        ArrayList<GuardTracker> list = extractGuardTrackersFromCursor(cursor);

        unload(cursor);
        return list;
    }

    /**
     * Read all guard tracker devices. (read all table)
     * @param context
     * @return
     */
    static public ArrayList<GuardTracker> read(Context context) {

        Cursor cursor = readCursor(context, null, null);
        ArrayList<GuardTracker> list = extractGuardTrackersFromCursor(cursor);

        unload(cursor);
        return list;
    }

    /**
     * Read full information for a particular guard tracker device.
     *
     * @param context
     * @return a list of GuardTracker objects
     */
    static public GuardTracker read(Context context, int guardTrackerId) {
        // Define columns WHERE clause.
        String selection = GuardTrackerContract.GuardTrackerTable._ID + " LIKE ?";
        // Define values WHERE clause.
        String[] selectionArgs = { String.valueOf(guardTrackerId) };

        Cursor cursor = readCursor(context, selection, selectionArgs);

        if (cursor.getCount() != 1) throw new IllegalArgumentException();

        ArrayList<GuardTracker> list = extractGuardTrackersFromCursor(cursor); // V

        unload(cursor);
        return list.get(0);
    }

    /**
     * Retrieve GuardTracker Id given a BLE address.
     * The implementation of this operation can not find a table entry unsynchronized and without
     * backup (the case when it is created a virtual GuardTtracker with no association with a remote device
     * This implementation takes into account that backup entries are in the same table and
     * in that case ble and gsm address are not unique. So, this query finds the newest GuardTracker
     * description entry (not the backup entry).     *
     * @param context
     * @param bleAddress
     * @return
     */
    static public GuardTracker readByBleAddress(Context context, String bleAddress) {
        // Selection/Where clause
        // Define columns WHERE clause.
        String selection = GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_BLE_ID + " LIKE ?";
        // Define values WHERE clause.
        String[] selectionArgs = { bleAddress };
        // Execute query
        Cursor cursor = readCursor(context, selection, selectionArgs);
        ArrayList<GuardTracker> list = extractGuardTrackersFromCursor(cursor);

        GuardTracker gt = list.get(0);

        // Extract values from query result
        int guardTrackerId = gt.get_id();
        boolean sync = gt.getSync();
        int next = gt.getNextId();
        System.out.println("First entry - id:" + guardTrackerId + " - sync:" + sync + " - next:" + next);

        unload(cursor);
        return gt;
    }

    /**
     * Retrieve new GuardTracked from database base on gsmPhoneNumber.
     * The phone number should correspond to a SIM in a device. The new GuardTracker only has
     * id and name fields filled.
     * The implementation of this operation can not find a table entry unsynchronized and without
     * backup (the case when it is created a virtual GuardTtracker with no association with a remote device
     * This implementation takes into account that backup entries are in the same table and
     * in that case ble and gsm address are not unique. So, this query finds the newest GuardTracker
     * description entry (not the backup entry).     *
     * @param context
     * @param gsmPhoneNumber phone number to look for in database
     * @return new GuardTracker
     */
    static public GuardTracker readByPhoneNumber(Context context, String gsmPhoneNumber) {
        // Define columns WHERE clause.
        String selection = GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_GSM_ID + " LIKE ?";
        // Define values WHERE clause.
        String[] selectionArgs = { gsmPhoneNumber };

        // Execute query
        Cursor cursor = readCursor(context, selection, selectionArgs);
        ArrayList<GuardTracker> list = extractGuardTrackersFromCursor(cursor);

        GuardTracker gt = list.get(0);

        // Extract values from query result
        int guardTrackerId = gt.get_id();
        boolean sync = gt.getSync();
        int next = gt.getNextId();
        System.out.println("First entry - id:" + guardTrackerId + " - sync:" + sync + " - next:" + next);

        unload(cursor);
        return gt;
    }

    /**
     * Update the state of this object only in GuardTracker table. Configuration tables are not updated.
     * @param context
     */
    public void update(Context context) {
        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for write
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_NAME, mName);
        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_BLE_ID, mBleId);
        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_GSM_ID, mGsmId);
        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_WAKE_SENSORS, mWakeSensorsStatus);
        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_POS_REF, mPosRefId == 0 ? null : mPosRefId);
        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_LAST_MON_INFO, mLastMonInfoId == 0 ? null : mLastMonInfoId);
        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_MON_CFG, mMonCfgId);
        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_TRACK_CFG, mTrackCfgId);
        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_VIGILANCE_CFG, mVigilanceCfgId);
        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_OWNER_PHONE_NUMBER, mOwnerPhoneNumber);
        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_SYNC, mSync);
        contentValues.put(GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_NEXT, mNextId);
        // Define columns WHERE clause.
        String whereClause = GuardTrackerContract.GuardTrackerTable._ID + " LIKE ?";
        // Define values WHERE clause.
        String[] whereClauseArgs = { String.valueOf(_id) };

        int rowsUpdated = db.update(GuardTrackerContract.GuardTrackerTable.TABLE_NAME, contentValues, whereClause, whereClauseArgs);
        assert rowsUpdated == 1;

        db.close();
        dbHelper.close();
    }

    /**
     * Class method. Delete GuardTracker with id from database.
     * It is a deep delete, it means it is deleted from GuardTracker table,
     * configurations tables, position table, monitoring information, track sessions,
     * contacts table and backup tables.
     * @param context
     * @param id the GuardTracker to be deleted from database
     * @return true if GuardTracker with id was successfully deleted, false otherwise.
     */
    public static boolean delete(Context context, int id) {
        GuardTracker guardTracker = GuardTracker.read(context, id);
        return guardTracker.delete(context);
    }

    /**
     * Delete GuardTracker with id's from database. It is performed a deep delete including
     * configuration tables, position, track session, monitoring information, contacts and
     * backup tables.
     * @param context
     * @param ids the array of id's to be deleted.
     * @return the number of GuardTrackers deleted from database.
     */
    public static int delete(Context context, int [] ids) {
        int deletedItems = 0;
        for (int id : ids) {
            deletedItems += GuardTracker.delete(context, id) ? 1 : 0;
        }
        return deletedItems;
    }

    /**
     * Delete this instance from database. It is a deep delete including configuration and all other
     * tables where this GuardTracker may appear.
     * @param contex
     * @return
     */
    public boolean delete(Context context) {
        if (mMonCfgId != 0)
            mMonCfg.delete(context);
        if (mTrackCfgId != 0)
            mTrackCfg.delete(context);
        if (mVigilanceCfgId != 0)
            mVigilanceCfg.delete(context);
        if (mPosRefId != 0)
            mPosRef.delete(context);
        if (mNextId != 0)
            mNext.delete(context);
        mMonCfgId = mTrackCfgId = mVigilanceCfgId = mPosRefId = mNextId = 0;
        mMonCfg = null; mTrackCfg = null; mVigilanceCfg = null; mPosRef = null; mNext = null;

        MonitoringInfo.deleteByGuardTracker(context, _id);
        TrackSession.deleteByGuardTracker(context, _id);
        SecondaryContactsDbHelper.delete(context, _id);

        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for write
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Define columns WHERE clause.
        String whereClause = GuardTrackerContract.GuardTrackerTable._ID + " LIKE ?";
        String[] whereClauseArgs = { String.valueOf(_id) };

        // Execute query
        int deletedItems = db.delete(GuardTrackerContract.GuardTrackerTable.TABLE_NAME, whereClause, whereClauseArgs);

        db.close();
        dbHelper.close();
        return deletedItems == 1;
    }

//    /**
//     * Create backup from this instance and insert it in GuardTrackerBackup table.
//     * @param context
//     */
//    public void createBackup(Context context) {
//        // Read GuardTracker database helper
//        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
//        // Initialize database for write
//        final SQLiteDatabase db = dbHelper.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_NAME, mName);
//        contentValues.put(GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_BLE_ID, mBleId);
//        contentValues.put(GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_GSM_ID, mGsmId);
//        contentValues.put(GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_WAKE_SENSORS, mWakeSensorsStatus);
//        contentValues.put(GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_MON_CFG, mMonCfgId);
//        contentValues.put(GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_TRACK_CFG, mTrackCfgId);
//        contentValues.put(GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_VIGILANCE_CFG, mVigilanceCfgId);
//        contentValues.put(GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_OWNER_PHONE_NUMBER, mOwnerPhoneNumber);
//        contentValues.put(GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_POS_REF, mPosRef == null ? null: mPosRefId);
//        contentValues.put(GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_CURR_ID, _id);
//        long pkId = db.insertOrThrow(GuardTrackerContract.GuardTrackerBackupTable.TABLE_NAME, null, contentValues);
//
//        mBackupId = (int)pkId;
//        mNext = new GuardTracker(_id, mName, mBleId, mGsmId, mOwnerPhoneNumber, mWakeSensorsStatus, mPosRefId, mLastMonInfoId, mMonCfgId, mTrackCfgId, mVigilanceCfgId);
//        mNext.mCurrId = _id;
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
//        String whereClause = GuardTrackerContract.GuardTrackerBackupTable._ID + " LIKE ?";
//        String[] whereClauseArgs = { String.valueOf(mNext._id) };
//
//        // Execute query
//        int deletedItems = db.delete(GuardTrackerContract.GuardTrackerBackupTable.TABLE_NAME, whereClause, whereClauseArgs);
//
//        db.close();
//        dbHelper.close();
//        this.mNext = null;
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
//        contentValues.put(GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_NAME, mName);
//        contentValues.put(GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_BLE_ID, mBleId);
//        contentValues.put(GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_GSM_ID, mGsmId);
//        contentValues.put(GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_WAKE_SENSORS, mWakeSensorsStatus);
//        contentValues.put(GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_MON_CFG, mMonCfgId);
//        contentValues.put(GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_TRACK_CFG, mTrackCfgId);
//        contentValues.put(GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_VIGILANCE_CFG, mVigilanceCfgId);
//        contentValues.put(GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_OWNER_PHONE_NUMBER, mOwnerPhoneNumber);
//        contentValues.put(GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_POS_REF, mPosRef == null ? null: mPosRefId);
//        contentValues.put(GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_CURR_ID, _id);
//
//        // Define columns WHERE clause.
//        String whereClause = GuardTrackerContract.GuardTrackerBackupTable._ID + " LIKE ?";
//        String[] whereClauseArgs = { String.valueOf(mNext._id) };
//
//        // Execute query
//        int updatedItems = db.update(GuardTrackerContract.GuardTrackerBackupTable.TABLE_NAME, contentValues, whereClause, whereClauseArgs);
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
//                GuardTrackerContract.GuardTrackerBackupTable._ID,
//                GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_NAME,
//                GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_BLE_ID,
//                GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_GSM_ID,
//                GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_WAKE_SENSORS,
//                GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_MON_CFG,
//                GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_TRACK_CFG,
//                GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_VIGILANCE_CFG,
//                GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_OWNER_PHONE_NUMBER,
//                GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_POS_REF,
//                GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_CURR_ID
//        };
//        // Define columns WHERE clause.
//        String backupSelection = GuardTrackerContract.GuardTrackerBackupTable._ID + " LIKE ?";
//        // Define values WHERE clause.
//        String[] backupSelArgs = {String.valueOf(mBackupId)};
//
//        Cursor cursor = db.query(
//                GuardTrackerContract.GuardTrackerBackupTable.TABLE_NAME,  // The table to query
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
//        int backupId            = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.GuardTrackerBackupTable._ID));
//        String backupName       = cursor.getString(cursor.getColumnIndex(GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_NAME));
//        String backupBle        = cursor.getString(cursor.getColumnIndex(GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_BLE_ID));
//        String backupGsm        = cursor.getString(cursor.getColumnIndex(GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_GSM_ID));
//        int backupWakeSensors   = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_WAKE_SENSORS));
//        int backupMonCfg        = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_MON_CFG));
//        int backupTrackCfg      = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_TRACK_CFG));
//        int backupVigCfg        = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_VIGILANCE_CFG));
//        String backupOwnerPhone = cursor.getString(cursor.getColumnIndex(GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_OWNER_PHONE_NUMBER));
//        int backupPosRef        = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_POS_REF));
//        int backupBackupId      = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.GuardTrackerBackupTable.COLUMN_NAME_CURR_ID));
//        mNext = new GuardTracker(backupId, backupName, backupBle, backupGsm, backupOwnerPhone, backupWakeSensors, backupPosRef, 0,
//                backupMonCfg, backupTrackCfg, backupVigCfg);
//        mNext.setCurrId(backupBackupId);
//
//        cursor.close();
//        db.close();
//        dbHelper.close();
//    }

    /**
     * Create en empty object.
     */
    public GuardTracker() {

    }

    public void copyConfigs(GuardTracker from) {
        MonitoringConfiguration monCfg = from.getMonCfg();
        setMonCfg(monCfg);
        TrackingConfiguration trackCfg = from.getTrackCfg();
        setTrackCfg(trackCfg);
        VigilanceConfiguration vigilanceCfg = from.getVigCfg();
        setVigilanceCfg(vigilanceCfg);
        Position posRef = from.getPosRef();
        if (posRef != null)
            setPosRef(posRef);
        else {
            mPosRef = null;
            mPosRefId = 0;
        }
        String devicePhoneNumber = from.getGsmId();
        if (devicePhoneNumber != null)
            setGsmId(devicePhoneNumber);
        else {
            mGsmId = null;
        }
        int wakeSensors = from.getWakeSensors();
        setWakeSensors(wakeSensors);
        List<String> secondaryContacts = from.getSecondaryContacts();
        setSecondaryContacts(secondaryContacts);
    }

}
