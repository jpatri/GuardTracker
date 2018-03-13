package com.patri.guardtracker.sms;

import android.content.Context;

import com.patri.guardtracker.model.GuardTracker;
import com.patri.guardtracker.model.MonitoringInfo;
import com.patri.guardtracker.model.Position;

import java.util.Scanner;

/**
 * Created by patri on 26/09/2016.
 */
public class SmsBodyProcessor {
    static SmsGpsProcessor  gpsProcessor    = new SmsGpsProcessor();
    static SmsTempProcessor tempProcessor   = new SmsTempProcessor();
    static SmsSimProcessor  simProcessor    = new SmsSimProcessor();
    static SmsBatProcessor  batProcessor    = new SmsBatProcessor();
    static final SmsParamValueProcessor [][] paramsProcessors = new SmsParamValueProcessor[][]{
            // Alert type
            {},     // 0
            {},     // 1
            {},     // 2
            {},     // 3
            {},     // 4
            {},     // 5
            {},     // 6
            {},     // 7: tracking
            {},     // 8: post tracking
            {       // 9: monitoring info
                    gpsProcessor,  // 0
                    tempProcessor, // 1
                    simProcessor,  // 2
                    batProcessor   // 3
            }
    };
    static SmsTrackingProcessor  trackingProcessor    = new SmsTrackingProcessor();
    static SmsPostTrackingProcessor postTrackingProcessor   = new SmsPostTrackingProcessor();
    static SmsMonInfoProcessor  monInfoProcessor    = new SmsMonInfoProcessor();
    static final SmsAlertProcessor [] alertsProcessors = new SmsAlertProcessor[] {
            null,   // 0
            null,   // 1
            null,   // 2
            null,   // 3
            null,   // 4
            null,   // 5
            null,   // 6
            trackingProcessor,      // 7: Tracking alert
            postTrackingProcessor,  // 8: Post-tracking alert
            monInfoProcessor        // 9: Monitoring information alert
    };

    static class SmsGpsProcessor implements SmsParamValueProcessor {
        @Override
        public Position process(String param) {
            // ToDo
            return null;
        }
    }
    static class SmsTempProcessor implements SmsParamValueProcessor {
        @Override
        public Double process(String param) {
            // ToDo
            return null;
        }
    }
    static class SmsSimProcessor implements SmsParamValueProcessor {
        @Override
        public Integer process(String param) {
            // ToDo
            return null;
        }
    }
    static class SmsBatProcessor implements SmsParamValueProcessor {
        @Override
        public Integer process(String param) {
            // ToDo
            return null;
        }
    }

    static class SmsMonInfoProcessor implements SmsAlertProcessor {
        @Override
        public void process(Context context, int guardTrackerId, long date, String alertBody, GuardTracker guardTracker) {
            if (guardTracker == null)
                guardTracker = GuardTracker.read(context, guardTrackerId);
            MonitoringInfo monInfo = new MonitoringInfo(guardTracker, date, alertBody);
            // Update GuardTracker with this monitoring info if it is the latest MonitoringInfo received.
            boolean isLatest = true;
            int lastMonInfoId = guardTracker.getLastMonInfoId();
            if (lastMonInfoId != 0) {
                MonitoringInfo lastMonInfo = MonitoringInfo.read(context, lastMonInfoId);
                if (lastMonInfo.getDate().getTime() > monInfo.getDate().getTime())
                    isLatest = false;
            }
            if (isLatest == true) {
                guardTracker.setLastMonInfo(monInfo);
                guardTracker.update(context);
            }
        }
    }
    static class SmsTrackingProcessor implements SmsAlertProcessor {
        @Override
        public void process(Context context, int guardTrackerId, long date, String alertBody, GuardTracker guardTracer) {
            // ToDo: update database with tracking data
        }
    }
    static class SmsPreTrackingProcessor implements SmsAlertProcessor {
        @Override
        public void process(Context context, int guardTrackerId, long date, String alertBody, GuardTracker guardTracer) {
            // ToDo: update database with PreTracking data

        }
    }
    static class SmsPostTrackingProcessor implements SmsAlertProcessor {
        @Override
        public void process(Context context, int guardTrackerId, long date, String alertBody, GuardTracker guardTracer) {
            // ToDo: update database with PostTracking data

        }
    }

    static public void process(Context context, int guardTrackerId, long date, String body, GuardTracker guardTracer) {
        int alertBodyIdx = body.indexOf(' ') + 1;
        int msgType = Integer.parseInt(body.substring(0, alertBodyIdx - 1));
        alertsProcessors[msgType].process(context, guardTrackerId, date, body.substring(alertBodyIdx), guardTracer);

//        Scanner scanner = new Scanner(body);
//        int msgType = Integer.parseInt(scanner.next());
//        while (scanner.hasNext()) { // Process values of parameters
//
//            String tk = scanner.next();
//            // Filter type of parameter
//            int type = tk.charAt(0) - '0';
//            paramsProcessors[msgType][type].process(tk.substring(1));
//        }
    }
}
