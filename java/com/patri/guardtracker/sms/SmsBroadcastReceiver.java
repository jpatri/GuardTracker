package com.patri.guardtracker.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.util.Log;

import com.patri.guardtracker.model.GuardTracker;

import java.util.Locale;

/**
 * Created by patri on 21/09/2016.
 * // Copied from http://stackoverflow.com/questions/7089313/android-listen-for-incoming-sms-messages
 */
public class SmsBroadcastReceiver extends BroadcastReceiver {
    public final static String TAG = SmsBroadcastReceiver.class.getSimpleName();

    private SharedPreferences preferences;

    static private void processSmsMessage(Context context, long timestamp, String phonenumber, String sms, int guardTrackerId, String guardTrackerName) {
        // ToDo
        Log.i(TAG, "SMS msg: " + sms);
        SmsNotificationUtils.notifyReceivedSms(context, timestamp, phonenumber, sms, 0, guardTrackerId, guardTrackerName);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub

        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Bundle bundle = intent.getExtras();           //---get the SMS message passed in---

            //---retrieve the SMS message received---
            if (bundle != null) try {
                Object[] pdus = (Object[]) bundle.get("pdus");
                SmsMessage[] msgs = new SmsMessage[pdus.length];
                for (int i = 0; i < msgs.length; i++) {
                    if (Build.VERSION.SDK_INT > 23)
                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i], "3gpp");
                    else
                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);

                    String msgFrom = msgs[i].getOriginatingAddress();
                    String msgFromE164 = PhoneNumberUtils.formatNumberToE164(msgFrom, Locale.getDefault().getCountry());
                    String msgFromNational = PhoneNumberUtils.formatNumber(msgFrom, Locale.getDefault().getCountry());
                    String msgFromStripped = PhoneNumberUtils.stripSeparators(msgFrom);
                    String msgFromNetPortion = PhoneNumberUtils.extractNetworkPortion(msgFrom);
                    String msgFromFormatted = PhoneNumberUtils.formatNumber(msgFrom, Locale.getDefault().getCountry());
                    Log.i(TAG, "msgFromStripped = " + msgFromStripped +
                            "; msgFromNetPortion = " + msgFromNetPortion +
                            "; msgFromFormatted = " + msgFromFormatted);

                    GuardTracker guardTracker = GuardTracker.readByPhoneNumber(context, msgFrom);
                    int guardTrackerId = guardTracker.get_id();
                    String guardTrackerName = guardTracker.getName();
                    // Test if return a valid guardTrackerId (> 0)
                    if (guardTrackerId > 0) {
                        Log.i(TAG, "Found a match phonenumber: " + msgFrom);
                        long timestamp = msgs[i].getTimestampMillis();
                        String msgBody = msgs[i].getMessageBody();
                        processSmsMessage(context, timestamp, msgFrom, msgBody, guardTrackerId, guardTrackerName);
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
        }
    }
}