package com.patri.guardtracker.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by patri on 21/04/2016.
 */
public class TrackSession {
    public final static String TAG = TrackSession.class.getSimpleName();
    public enum TrackSessionViewItem {ALL, LAST, SELECT};
    static private SQLiteDatabase db = null;

    private int _id;
    private int mGuardTrackerId;
    private GuardTracker mGuardTracker;
    private Date mDate;
    private String mName;
    private ArrayList<Position> mPositionList;

    public TrackSession(GuardTracker guardTracker,
                        long date,
                        String name)
    {
        this.mDate           = new Date(date);
        this.mName           = name;
        this.mGuardTracker = guardTracker;
        this.mPositionList = new ArrayList<>();
    }

    public Date     getDate()             { return mDate; }
    public String   getName()             { return mName; }
    public int      getGuardTrackerId()   { return mGuardTrackerId; }
    public int      get_id()              { return _id; }

    public void     add(Position position){ mPositionList.add(position); }
    public void     addRange(ArrayList<Position> list) { mPositionList.addAll(list); }
    public void     setSession(ArrayList<Position> session) { mPositionList = session; };

    public String   getPrettyDate()       { return String.format("%1$td/%1$tm/%1$tY %1$tR", mDate); }
    public String   getPrettyName()       { return mName; }

    public void create(Context context)
    {
        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for write
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Prepare values to write to database
        ContentValues contentValues = new ContentValues();
        contentValues.put(GuardTrackerContract.TrackSessionTable.COLUMN_NAME_GUARD_TRACKER, mGuardTrackerId);
        contentValues.put(GuardTrackerContract.TrackSessionTable.COLUMN_NAME_DATE, mDate.getTime());
        contentValues.put(GuardTrackerContract.TrackSessionTable.COLUMN_NAME_NAME, mName);
        long pkId = db.insertOrThrow(GuardTrackerContract.TrackSessionTable.TABLE_NAME, null, contentValues);
        this._id = (int)pkId;

        db.close();
    }

    static public Cursor readCursorFromGuardTrackerId(Context context, int guardTrackerId) {
        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for read
        db = dbHelper.getReadableDatabase();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] trackSessionProjection = {
                GuardTrackerContract.TrackSessionTable._ID,
                GuardTrackerContract.TrackSessionTable.COLUMN_NAME_DATE,
                GuardTrackerContract.TrackSessionTable.COLUMN_NAME_NAME
        };
        // Define columns WHERE clause.
        String trackSessionSelection = GuardTrackerContract.TrackSessionTable.COLUMN_NAME_GUARD_TRACKER + " LIKE ?";
        // Define values WHERE clause.
        String[] trackSessionSelArgs = { String.valueOf(guardTrackerId) };

        Cursor cursor = db.query(
                GuardTrackerContract.TrackSessionTable.TABLE_NAME,  // The table to query
                trackSessionProjection,                             // The columns to return
                trackSessionSelection,                              // The columns for the WHERE clause
                trackSessionSelArgs,                                // The values for the WHERE clause
                null,                                               // don't group the rows
                null,                                               // don't filter by row groups
                null                                                // The sort order
        );
        return cursor; // May be an empty cursor.
    }
    static private Cursor readCursorFromTrackSessionId(Context context, int trackSessionId) {
        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for read
        db = dbHelper.getReadableDatabase();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] trackSessionProjection = {
                GuardTrackerContract.TrackSessionTable._ID,
                GuardTrackerContract.TrackSessionTable.COLUMN_NAME_GUARD_TRACKER,
                GuardTrackerContract.TrackSessionTable.COLUMN_NAME_DATE,
                GuardTrackerContract.TrackSessionTable.COLUMN_NAME_NAME
        };
        // Define columns WHERE clause.
        String trackSessionSelection = GuardTrackerContract.TrackSessionTable.COLUMN_NAME_GUARD_TRACKER + " LIKE ?";
        // Define values WHERE clause.
        String[] trackSessionSelArgs = { String.valueOf(trackSessionId) };

        Cursor cursor = db.query(
                GuardTrackerContract.TrackSessionTable.TABLE_NAME,  // The table to query
                trackSessionProjection,                             // The columns to return
                trackSessionSelection,                              // The columns for the WHERE clause
                trackSessionSelArgs,                                // The values for the WHERE clause
                null,                                               // don't group the rows
                null,                                               // don't filter by row groups
                null                                                // The sort order
        );
        if (cursor.getCount() == 0) throw new IllegalStateException("No entries in TrackSessionTable");
        return cursor;
    }
    static private TrackSession buildTrackSession(Cursor cursor, int guardTrackerId) {
        int   id            = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.TrackSessionTable._ID));
        long  dateTimeRaw   = cursor.getLong(cursor.getColumnIndex(GuardTrackerContract.TrackSessionTable.COLUMN_NAME_DATE));
        String nameRaw      = cursor.getString(cursor.getColumnIndex(GuardTrackerContract.TrackSessionTable.COLUMN_NAME_NAME));

        TrackSession trackSession = new TrackSession(null, dateTimeRaw, nameRaw);
        trackSession._id = id;
        trackSession.mGuardTrackerId = guardTrackerId;
        return trackSession;
    }
    static private TrackSession buildTrackSession(Cursor cursor) {
        int guardTrackerId = cursor.getInt(cursor.getColumnIndex(GuardTrackerContract.TrackSessionTable.COLUMN_NAME_GUARD_TRACKER));

        TrackSession trackSession = buildTrackSession(cursor, guardTrackerId);
        return trackSession;
    }
    static public TrackSession read(Context context, int trackSessionId) {
        Cursor cursor = readCursorFromTrackSessionId(context, trackSessionId);

        cursor.moveToFirst();

        TrackSession trackSession = buildTrackSession(cursor);
        cursor.close();
        unload();
        return trackSession;
    }

    static public ArrayList<TrackSession> readList(Context context, int guardTrackerId) {
        ArrayList<TrackSession> list = new ArrayList<>();

        Cursor cursor = readCursorFromGuardTrackerId(context, guardTrackerId);

        for (cursor.moveToFirst(); cursor.isAfterLast() != true; cursor.moveToNext()) {
            TrackSession trackSession = buildTrackSession(cursor, guardTrackerId);
            list.add(trackSession);
        }

        cursor.close();
        unload();
        return list;
    }

    static public void unload() {
        if (db != null) db.close();
        db = null;
    }

    public void update(
            Context context,
            int guardTrackerId,
            long date,
            String name)
    {
        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for write
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(GuardTrackerContract.TrackSessionTable.COLUMN_NAME_GUARD_TRACKER, guardTrackerId);
        contentValues.put(GuardTrackerContract.TrackSessionTable.COLUMN_NAME_DATE, date);
        contentValues.put(GuardTrackerContract.TrackSessionTable.COLUMN_NAME_NAME, name);
        // Define columns WHERE clause.
        String whereClause = GuardTrackerContract.TrackSessionTable._ID + " LIKE ?";
        // Define values WHERE clause.
        String[] whereClauseArgs = { String.valueOf(_id) };

        int rowsUpdated = db.update(GuardTrackerContract.TrackSessionTable.TABLE_NAME, contentValues, whereClause, whereClauseArgs);
        assert rowsUpdated == 1;

        db.close();
        dbHelper.close();
    }


    public static boolean delete(Context context, int id) {
        int posDeleted = Position.deleteByTrackSession(context, id);
        Log.i(TAG, "Delete " + posDeleted + " positions given the TrackSession " + id);
        assert posDeleted > 0;

        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for write
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Define columns WHERE clause.
        String whereClause = GuardTrackerContract.TrackSessionTable._ID + " LIKE ?";
        // Define values WHERE clause.
        String [] whereClauseArgs = { String.valueOf(id) };

        // Execute query
        int deletedItems = db.delete(GuardTrackerContract.TrackSessionTable.TABLE_NAME, whereClause, whereClauseArgs);

        db.close();
        dbHelper.close();
        return deletedItems == 1;

    }
    public static int delete(Context context, int [] ids) {
        int deletedItems = 0;
        for (int id: ids) {
            deletedItems += delete(context, id) ? 1 : 0;
        };
        return deletedItems;
    }

    public static int deleteByGuardTracker(Context context, int guardTrackerId) {
        ArrayList<TrackSession> trackSessionArrayList = TrackSession.readList(context, guardTrackerId);

        if (trackSessionArrayList.size() == 0)
            return 0;

        for (TrackSession session :
                trackSessionArrayList) {
            int sessionId = session.get_id();
            Position.deleteByTrackSession(context, sessionId);
        }

        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for write
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Define columns WHERE clause.
        String whereClause = GuardTrackerContract.TrackSessionTable.COLUMN_NAME_GUARD_TRACKER + " LIKE ?";
        // Define values WHERE clause.
        String [] whereClauseArgs = { String.valueOf(guardTrackerId) };

        // Execute query
        int deletedItems = db.delete(GuardTrackerContract.TrackSessionTable.TABLE_NAME, whereClause, whereClauseArgs);

        db.close();
        dbHelper.close();
        return deletedItems;

    }

}
