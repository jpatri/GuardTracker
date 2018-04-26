package com.patri.guardtracker.sms;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.patri.guardtracker.model.MonitoringInfo;
import com.patri.guardtracker.model.PermissionsChecker;
import com.patri.guardtracker.permissions.PermissionsActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by patri on 22/09/2016.
 */
final public class SmsInboxUtils {
    final private static String TAG = SmsInboxUtils.class.getSimpleName();
    final public static String SMS_READED_COLUMN = "read";
    final public static String SMS_ADDRESS_COLUMN = "address";
    final public static String SMS_DATE_COLUMN = "date";
    final public static String SMS_PERSON_COLUMN = "person";
    final public static String SMS_BODY_COLUMN = "body";
    final public static String SMS_THREAD_ID_COLUMN = "thread_id";
    final public static String SMS_ID_COLUMN = "_id";

    static public Cursor readSmsCursor(Context context, long ignoreThreadId,
                                       boolean unreadOnly, String phoneNumber,
                                       ArrayList<MonitoringInfo> monInfoList) {

        // Define columns WHERE clause.
        boolean isFirst = true;
        StringBuilder sb = new StringBuilder();
        //sb.append((unreadOnly ? SMS_READED_COLUMN + " = 0" : SMS_READED_COLUMN + " = 1"));
        if (unreadOnly == true) {
            if (isFirst == false)
                sb.append(" AND ");
            isFirst = false;
            sb.append(SMS_READED_COLUMN + " = 0");
        }
        if (monInfoList != null && monInfoList.size() > 0) {
            if (isFirst == false)
                sb.append(" AND ");
            isFirst = false;
            sb.append(SMS_DATE_COLUMN).append(" NOT IN ( ");
            MonitoringInfo monInfo = monInfoList.get(0);
            sb.append(monInfo.getDate().getTime());
            for (int i = 1; i < monInfoList.size(); i++) {
                monInfo = monInfoList.get(i);
                sb.append(", ").append(monInfo.getDate().getTime());
            }
            sb.append(" )");
        }
        if (phoneNumber != null) {
            if (isFirst == false)
                sb.append(" AND ");
            isFirst = false;
            sb.append(SMS_ADDRESS_COLUMN).append(" LIKE ").append('\'').append(phoneNumber).append('\'');
        }
        if (ignoreThreadId > 0) {
            if (isFirst == false)
                sb.append(" AND ");
            // Log.v("Ignoring sms threadId = " + ignoreThreadId);
            sb.append(SMS_THREAD_ID_COLUMN)
              .append(" != ")
              .append(ignoreThreadId);
        }
        String whereClause = sb.toString();
        // Define values WHERE clause.
        String SORT_ORDER = SMS_DATE_COLUMN + " ASC";
        String[] smsProjection = {
                SMS_ID_COLUMN, SMS_THREAD_ID_COLUMN, SMS_ADDRESS_COLUMN, SMS_PERSON_COLUMN,
                SMS_DATE_COLUMN, SMS_BODY_COLUMN, SMS_READED_COLUMN
        };
        Log.i(TAG, whereClause);

        Cursor cursor = context.getContentResolver().query(
                Uri.parse("content://sms/inbox"),
                smsProjection,
                whereClause,
                null,
                SORT_ORDER);

        return cursor;
    }

    /**
     *  Adapted from: http://stackoverflow.com/questions/848728/how-can-i-read-sms-messages-from-the-inbox-programmatically-in-android
     * @return
     */
    static public List<Sms> readSmsList(Context context, long ignoreThreadId, boolean unreadOnly) {
        Cursor cursor = readSmsCursor(context, ignoreThreadId, unreadOnly, null, null);

        if (cursor != null) {
            try {
                int count = cursor.getCount();
                ArrayList<Sms> list = new ArrayList<>(count);
                for (cursor.moveToFirst(); ! cursor.isAfterLast(); cursor.moveToNext()) {
                    long messageId = cursor.getLong(0);
                    long threadId = cursor.getLong(1);
                    String address = cursor.getString(2);
                    long contactId = cursor.getLong(3);
                    long timestamp = cursor.getLong(4);
                    String body = cursor.getString(5);
                    String readedAux = cursor.getString(6);
                    boolean readed = "0".equals(readedAux);

                    Sms sms = new Sms();
                    sms.setId(messageId);
                    sms.setThreadId(threadId);
                    sms.setFrom(address);
                    sms.setFolderName("Inbox");
                    sms.setBody(body);
                    sms.setDate(timestamp);
                    sms.setReadState(readed);

                    list.add(sms);
                }

                return list;

            } finally {
                cursor.close();
            }
        }
        return null;
    }

    /**
     *  Adapted from: http://stackoverflow.com/questions/848728/how-can-i-read-sms-messages-from-the-inbox-programmatically-in-android
     * @return
     */
    static public List<Sms> readSmsAll(Context context) {
        List<Sms> lstSms = new ArrayList<Sms>();
        Uri message = Uri.parse("content://sms/inbox");
        ContentResolver cr = context.getContentResolver();

        Cursor c = cr.query(message, null, null, null, null);
        int totalSMS = c.getCount();

        if (c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {

                Sms objSms = new Sms();
                objSms.setId(c.getLong(c.getColumnIndexOrThrow(SMS_ID_COLUMN)));
                objSms.setFrom(c.getString(c.getColumnIndexOrThrow(SMS_ADDRESS_COLUMN)));
                objSms.setBody(c.getString(c.getColumnIndexOrThrow(SMS_BODY_COLUMN)));
                objSms.setReadState(c.getString(c.getColumnIndex(SMS_READED_COLUMN)));
                objSms.setDate(c.getLong(c.getColumnIndexOrThrow(SMS_DATE_COLUMN)));
                objSms.setContactId(c.getLong(c.getColumnIndexOrThrow(SMS_PERSON_COLUMN)));

//                if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
//                    objSms.setFolderName("inbox");
//                } else {
//                    objSms.setFolderName("sent");
//                }

                lstSms.add(objSms);
                c.moveToNext();
            }
        }
        // else {
        // throw new RuntimeException("You have no SMS");
        // }
        c.close();

        return lstSms;
    }

}
