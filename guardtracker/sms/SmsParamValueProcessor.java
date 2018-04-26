package com.patri.guardtracker.sms;

import android.content.Context;

/**
 * Created by patri on 26/09/2016.
 */
public interface SmsParamValueProcessor {
    public <T> T process(String param);
}
