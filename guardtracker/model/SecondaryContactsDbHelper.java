package com.patri.guardtracker.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by patri on 09/11/2016.
 */
public class SecondaryContactsDbHelper {
    public final static String TAG = SecondaryContactsDbHelper.class.getSimpleName();

    public static ArrayList<String> read(Context context, int guardTrackerId) {
        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for read
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] contactsProjection = {
                GuardTrackerContract.ContactsTable._ID,
                GuardTrackerContract.ContactsTable.COLUMN_NAME_PHONE_NUMBER
        };
        // Define columns WHERE clause.
        String contactsSelection = GuardTrackerContract.ContactsTable.COLUMN_NAME_GUARD_TRACKER + " LIKE ?";
        // Define values WHERE clause.
        String[] contactsSelArgs = { String.valueOf(guardTrackerId) };

        Cursor cursor = db.query(
                GuardTrackerContract.ContactsTable.TABLE_NAME,      // The table to query
                contactsProjection,                                 // The columns to return
                contactsSelection,                                  // The columns for the WHERE clause
                contactsSelArgs,                                    // The values for the WHERE clause
                null,                                               // don't group the rows
                null,                                               // don't filter by row groups
                null                                                // The sort order
        );
//        if (cursor.getCount() != 1) return null;

        ArrayList<String> list = new ArrayList<>(cursor.getCount());
        for (cursor.moveToFirst(); ! cursor.isAfterLast(); cursor.moveToNext()) {

            String s = cursor.getString(cursor.getColumnIndex(GuardTrackerContract.ContactsTable.COLUMN_NAME_PHONE_NUMBER));
            list.add(s);
        }

        cursor.close();
        db.close();
        dbHelper.close();
        return list;

    }

    public static void create(Context context, int guardtrackerId, String phoneNumber) {
        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for write
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(GuardTrackerContract.ContactsTable.COLUMN_NAME_GUARD_TRACKER, guardtrackerId);
        contentValues.put(GuardTrackerContract.ContactsTable.COLUMN_NAME_PHONE_NUMBER, phoneNumber);
        db.insertOrThrow(GuardTrackerContract.ContactsTable.TABLE_NAME, null, contentValues);

        db.close();
        dbHelper.close();
    }

    public static void delete(Context context, int guardTrackerId) {
        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for write
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Define columns WHERE clause Contacts.
        String whereClauseContacts = GuardTrackerContract.ContactsTable.COLUMN_NAME_GUARD_TRACKER + " LIKE ?";
        String[] whereClauseArgsContacts = { String.valueOf(guardTrackerId) };
        // Execute query
        int deletedItems = db.delete(GuardTrackerContract.ContactsTable.TABLE_NAME, whereClauseContacts, whereClauseArgsContacts);
        Log.i(TAG, "deleteDeep: Deleted " + deletedItems + " items");

        db.close();
        dbHelper.close();
    }
    public static void delete(Context context, int guardTrackerId, String phoneNumber) {
        // Read GuardTracker database helper
        GuardTrackerDbHelper dbHelper = new GuardTrackerDbHelper(context);
        // Initialize database for write
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Define columns WHERE clause Contacts.
        String whereClauseContacts =
                GuardTrackerContract.ContactsTable.COLUMN_NAME_GUARD_TRACKER + " LIKE ? AND " +
                GuardTrackerContract.ContactsTable.COLUMN_NAME_PHONE_NUMBER + " = ?";
        String[] whereClauseArgsContacts = { String.valueOf(guardTrackerId), phoneNumber };
        // Execute query
        int deletedItems = db.delete(GuardTrackerContract.ContactsTable.TABLE_NAME, whereClauseContacts, whereClauseArgsContacts);
        Log.i(TAG, "deleteDeep: Deleted " + deletedItems + " items");

        db.close();
        dbHelper.close();

    }
}
