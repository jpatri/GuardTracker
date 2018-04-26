package com.patri.guardtracker.sms;

import android.content.Context;

import com.patri.guardtracker.model.GuardTracker;

import java.util.Scanner;

/**
 * Created by patri on 26/09/2016.
 */
public interface SmsAlertProcessor {
    public void process(Context context, int guardTrackerId, long date, String alertBody, GuardTracker guardTracer);
}
