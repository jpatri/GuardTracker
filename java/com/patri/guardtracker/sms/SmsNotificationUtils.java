package com.patri.guardtracker.sms;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.patri.guardtracker.GuardTrackerActivity;
import com.patri.guardtracker.MainActivity;
import com.patri.guardtracker.R;
import com.patri.guardtracker.model.GuardTracker;

/**
 * Created by patri on 28/09/2016.
 */
public class SmsNotificationUtils {
    final static int mNotificationId = 1;
    final static String GROUP_KEY_SMS_RECEIVED = "group_key_messages";
    final static String NOTIFICATION_TIMESTAMP = "com.patri.guardtracker.sms.NOTIFICATION_TIMESTAMP";
    final static String NOTIFICATION_PHONENUMBER = "com.patri.guardtracker.sms.NOTIFICATION_PHONENUMBER";
    final static String NOTIFICATION_SMS_BODY = "com.patri.guardtracker.sms.NOTIFICATION_SMS_BODY";
    final static String NOTIFICATION_ALERT_LEVEL = "com.patri.guardtracker.sms.NOTIFICATION_ALERT_LEVEL";

    static int mNumberOfNotifications = 0;

    static public void notifyReceivedSms(Context context, long timestamp, String phonenumber, String smsBody, int alertLevel, int guardTrackerId, String guardTrackerName) {

        incNumberOfNotifications();

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_beeswax)
                        .setContentTitle(context.getString(R.string.notification_title))
                        .setContentText("Alert from " + phonenumber)
                        .setGroup(GROUP_KEY_SMS_RECEIVED)
                        .setAutoCancel(true);

//        // For expanded layout
//        NotificationCompat.InboxStyle inboxStyle =
//                new NotificationCompat.InboxStyle();
//        String[] events = new String[6];
//        // Sets a title for the Inbox in expanded layout
//        inboxStyle.setBigContentTitle("Event tracker details:");

        // Creates an explicit intent for an Activity in your app
        Intent intent = new Intent(context, SmsReceivedEarlierActivity.class);
        intent.putExtra(GuardTrackerActivity.GUARD_TRACKER_ID, guardTrackerId);
        intent.putExtra(GuardTrackerActivity.GUARD_TRACKER_NAME, guardTrackerName);
        //intent.putExtra(NOTIFICATION_TIMESTAMP, timestamp);
        //intent.putExtra(NOTIFICATION_PHONENUMBER, phonenumber);
        //intent.putExtra(NOTIFICATION_SMS_BODY, smsBody);
        //intent.putExtra(NOTIFICATION_ALERT_LEVEL, alertLevel);


        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(GuardTrackerActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        //mBuilder.setContentText("Actual text").setNumber()
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mNotificationId allows you to update the notification later on.
        notificationManager.notify(mNotificationId, mBuilder.build());

//        // Summary group
//        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(),
//                R.drawable.ic_message_new/*ic_large_icon*/);
//        Notification summaryNotification = new NotificationCompat.Builder(context)
//                .setContentTitle("" + mNumberOfNotifications + " new messages")
//                .setSmallIcon(R.drawable.ic_beeswax/*ic_small_icon*/)
//                .setLargeIcon(largeIcon)
//                .setStyle(new NotificationCompat.InboxStyle()
//                        .addLine("Alex Faaborg   Check this out")
//                        .addLine("Jeff Chang   Launch Party")
//                        .setBigContentTitle("InboxStyle: " + mNumberOfNotifications + " new messages")
//                        .setSummaryText("johndoe@gmail.com"))
//                .setGroup(GROUP_KEY_SMS_RECEIVED)
//                .setGroupSummary(true)
//                .build();
//
//        notificationManager.notify(mNotificationId, summaryNotification);

// For updating notifications
//        notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//// Sets an ID for the notification, so it can be updated
//        int notifyID = 1;
//        mNotifyBuilder = new NotificationCompat.Builder(this)
//                .setContentTitle("New Message")
//                .setContentText("You've received new messages.")
//                .setSmallIcon(R.drawable.ic_notify_status)
//        numMessages = 0;
//// Start of a loop that processes data and then notifies the user
//        ...
//        mNotifyBuilder.setContentText(currentText)
//                .setNumber(++numMessages);
//        // Because the ID remains unchanged, the existing notification is
//        // updated.
//        notificationManager.notify(
//                notifyID,
//                mNotifyBuilder.build());
    }

    static public void incNumberOfNotifications() {
        mNumberOfNotifications += 1;
    }
    static public void decNumberOfNotifications() {
        mNumberOfNotifications -= 1;
    }

}
