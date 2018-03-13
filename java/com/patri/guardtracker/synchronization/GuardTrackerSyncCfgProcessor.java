package com.patri.guardtracker.synchronization;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.patri.guardtracker.communication.GuardTrackerCommands;
import com.patri.guardtracker.custom.MyPhoneUtils;
import com.patri.guardtracker.model.MonitoringConfiguration;
import com.patri.guardtracker.model.Position;
import com.patri.guardtracker.model.TrackingConfiguration;
import com.patri.guardtracker.model.VigilanceConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by patri on 24/08/2016.
 */
public class GuardTrackerSyncCfgProcessor extends GuardTrackerSyncProcessor {
    private final static String TAG = GuardTrackerSyncCfgProcessor.class.getSimpleName();

    private MonitoringConfiguration mMonCfg;
    private TrackingConfiguration mTrackingCfg;
    private VigilanceConfiguration mVigilanceCfg;
    private int mWakeSensorsStatus;
    private String mGsmPhonenumber;
    private Position mRefPos;
    private List<String> mSecondaryContacts = new ArrayList<>();


    public GuardTrackerSyncCfgProcessor(Context context) {
        super(context);
    }

    final public void setMonCfg(int time,
                                int period,
                                MonitoringConfiguration.SmsCriteria smsCriteria,
                                int gpsThreshold,
                                int gpsFov,
                                int gpsTimeout,
                                double tempHigh,
                                double tempLow,
                                double simThreshold) {
        mMonCfg = new MonitoringConfiguration(
                time,
                period,
                smsCriteria,
                gpsThreshold,
                gpsFov,
                gpsTimeout,
                tempHigh, tempLow,
                simThreshold);
        mMonCfg.create(context);
    }
    final public void setTrackingCfg(int gpsThreshold,
                                     TrackingConfiguration.SmsCriteria smsCriteria,
                                     int gpsTimeout,
                                     int gpsFov,
                                     int trackTimeout,
                                     int preTimeout,
                                     int postTimeout) {
        mTrackingCfg = new TrackingConfiguration(
                smsCriteria,
                gpsThreshold,
                gpsFov,
                gpsTimeout,
                trackTimeout,
                preTimeout,
                postTimeout);
        mTrackingCfg.create(context);
    }
    final public void setVigilanceCfg(int tiltLevelCriteria, int bleAdvertisementPeriod) {
        mVigilanceCfg = new VigilanceConfiguration(
                tiltLevelCriteria,
                bleAdvertisementPeriod);
        mVigilanceCfg.create(context);
    }
    final public void setWakeSensorsStatus(int wakeSensorsStatus) { mWakeSensorsStatus = wakeSensorsStatus; }
    final public void setGsmPhonenumber(String gsmPhonenumber)    { mGsmPhonenumber = gsmPhonenumber; }
    final public void setSecondaryContact(String contact)         {
        mSecondaryContacts.add(contact);

    }
    public MonitoringConfiguration  getMonCfg()                   { return mMonCfg; }
    public TrackingConfiguration    getTrackingCfg()              { return mTrackingCfg; }
    public VigilanceConfiguration   getVigilanceCfg()             { return mVigilanceCfg; }
    public int                      getWakeSensorsStatus()        { return mWakeSensorsStatus; }
    public String                   getGsmPhonenumber()           { return mGsmPhonenumber; }
    public Position                 getReferencePosition()        { return mRefPos; }
    public List<String>             getSecondaryContacts()        { return mSecondaryContacts; }

    /**
     * Answer processor interface definition.
     */
    interface BleAnswProcessor {
        void processBleAnsw(byte[] answ);
    }
    final BleAnswProcessor [] answProcessors = new BleAnswProcessor[] {
            new BleAnswProcessor() { // 0: Monitoring configuration answer
                @Override
                public void processBleAnsw(byte[] answ) { // Process monitoring configuration
                    int time         = (((int)answ[0] & 0xFF) << 8) + ((int)answ[1] & 0xFF);
                    int period       = (((int)answ[2] & 0xFF) << 8) + ((int)answ[3] & 0xFF);
                    int _smsCriteria = (int)answ[4] & 0xFF;
                    MonitoringConfiguration.SmsCriteria smsCriteria  = MonitoringConfiguration.SmsCriteria.fromInteger(_smsCriteria);
                    float ltDegrees  = ((((int)answ[5] & 0xFF) << 24) +
                            (((int)answ[6] & 0xFF) << 16) +
                            (((int)answ[7] & 0xFF) << 8) +
                            (((int)answ[8] & 0xFF) << 0));
                    ltDegrees = ltDegrees / 10000 / 60;
                    float lgDegrees  = ((((int)answ[9] & 0xFF) << 24) +
                            (((int)answ[10] & 0xFF) << 16) +
                            (((int)answ[11] & 0xFF) << 8) +
                            (((int)answ[12] & 0xFF) << 0));
                    lgDegrees = lgDegrees / 10000 / 60;
                    Location zeroLocation = new Location("Zero location");
                    zeroLocation.setLatitude(0);
                    zeroLocation.setLongitude(0);
                    Location monThreshold = new Location("Mon threshold");
                    monThreshold.setLatitude(ltDegrees);
                    monThreshold.setLongitude(lgDegrees);
                    float distance = zeroLocation.distanceTo(monThreshold);
                    int gpsThresholdMeters = (int)distance;
                    int gpsFov       = ((int)answ[13] & 0xFF);
                    int gpsTimeout   = ((int)answ[14] & 0xFF);
                    int tempRawHigh     = ((int)answ[15] & 0xFF);
                    int tempRawLow      = ((int)answ[16] & 0xFF);
                    int simThresholdRaw = (((int)answ[17] & 0xFF) << 8) + ((int)answ[18] & 0xFF);
                    double tempHigh = (tempRawHigh >> 1) + (0.5)*(tempRawHigh & 1);
                    double tempLow  = (tempRawLow  >> 1) + (0.5)*(tempRawLow  & 1);
                    double simThreshold = (double)simThresholdRaw / 100;
                    GuardTrackerSyncCfgProcessor.this.setMonCfg(
                            time, period,
                            smsCriteria,
                            gpsThresholdMeters, gpsFov, gpsTimeout,
                            tempHigh, tempLow,
                            simThreshold
                    );
                }
            },
            new BleAnswProcessor() { // 1: Tracking configuration answer
                @Override
                public void processBleAnsw(byte[] answ) {
                    float ltDegrees         = ((((int)answ[0] & 0xFF) << 24) +
                            (((int)answ[1] & 0xFF) << 16) +
                            (((int)answ[2] & 0xFF) << 8) +
                            (((int)answ[3] & 0xFF) << 0));
                    ltDegrees = ltDegrees / 10000 / 60;
                    float lgDegrees         = ((((int)answ[4] & 0xFF) << 24) +
                            (((int)answ[5] & 0xFF) << 16) +
                            (((int)answ[6] & 0xFF) << 8) +
                            (((int)answ[7] & 0xFF) << 0));
                    lgDegrees = lgDegrees / 10000 / 60;
                    Location zeroLocation = new Location("Zero location");
                    zeroLocation.setLatitude(0);
                    zeroLocation.setLongitude(0);
                    Location monThreshold = new Location("Mon threshold");
                    monThreshold.setLatitude(ltDegrees);
                    monThreshold.setLongitude(lgDegrees);
                    float distance = zeroLocation.distanceTo(monThreshold);
                    int gpsThresholdMeters = (int)distance;
                    int _smsCriteria = (int)answ[8] & 0xFF;
                    TrackingConfiguration.SmsCriteria smsCriteria  = TrackingConfiguration.SmsCriteria.fromInteger(_smsCriteria);
                    int gpsTimeoutMinutes   = ((int)answ[9] & 0xFF);
                    int gpsFov = ((int)answ[10] & 0xFF);
                    int trackTimeoutMinutes = ((int)answ[11] & 0xFF);
                    int preTimeoutMinutes   = ((int)answ[12] & 0xFF);
                    int postTimeoutMinutes  = ((int)answ[13] & 0xFF);
                    GuardTrackerSyncCfgProcessor.this.setTrackingCfg(
                            gpsThresholdMeters,
                            smsCriteria,
                            gpsTimeoutMinutes,
                            gpsFov,
                            trackTimeoutMinutes, preTimeoutMinutes, postTimeoutMinutes
                    );
                }
            },
            new BleAnswProcessor() { // 2: Advertisement configuration answer
                @Override
                public void processBleAnsw(byte[] answ) {
                    int tiltLevelCriteria       = ((int)answ[0] & 0xFF);
                    int bleAdvertisementPeriod  = ((int)answ[1] & 0xFF << 8) + ((int)answ[2] & 0xFF);

                    GuardTrackerSyncCfgProcessor.this.setVigilanceCfg(
                            tiltLevelCriteria, bleAdvertisementPeriod
                    );
                }
            },
            new BleAnswProcessor() { // 3: Wake sensors status
                @Override
                public void processBleAnsw(byte[] answ) {
                    int wakeSensorsStatus = (((byte)answ[0] << 8) | ((byte)answ[1] << 0));
                    GuardTrackerSyncCfgProcessor.this.setWakeSensorsStatus(
                            wakeSensorsStatus
                    );
                }
            },
            new BleAnswProcessor() { // 4: GSM phonenumber
                @Override
                public void processBleAnsw(byte[] answ) {
                    String phoneNumberRaw = new String(answ);
                    try {
                        String phoneNumber = MyPhoneUtils.formatE164(phoneNumberRaw);
                        GuardTrackerSyncCfgProcessor.this.setGsmPhonenumber(phoneNumber);
                    } catch(NumberParseException e) {
                        Log.e(TAG, e.getErrorType().toString() + ": " + e.getMessage());
                    }
                }
            },
            new BleAnswProcessor() { // 5: Reference position
                @Override
                public void processBleAnsw(byte[] answ) {
                    mRefPos = new Position(answ);
                }
            },
            new BleAnswProcessor() { // 6: Read secondary contact
                @Override
                public void processBleAnsw(byte[] answ) {
                    if (answ[0] == '1') {
                        String phoneNumber = new String(answ, 3, answ[2]);
                        mSecondaryContacts.add(phoneNumber);
                        // BIG FACADE: 4 comandos repetidos implicando saber quantos slots existem com contactos secundários.
                    }
                }
            },
            new BleAnswProcessor() { // 7: Read secondary contact
                @Override
                public void processBleAnsw(byte[] answ) {
                    if (answ[0] == '1') {
                        String phoneNumber = new String(answ, 3, answ[2]);
                        mSecondaryContacts.add(phoneNumber);
                        // BIG FACADE: 4 comandos repetidos implicando saber quantos slots existem com contactos secundários.
                    }
                }
            },
            new BleAnswProcessor() { // 8: Read secondary contact
                @Override
                public void processBleAnsw(byte[] answ) {
                    if (answ[0] == '1') {
                        String phoneNumber = new String(answ, 3, answ[2]);
                        mSecondaryContacts.add(phoneNumber);
                        // BIG FACADE: 4 comandos repetidos implicando saber quantos slots existem com contactos secundários.
                    }
                }
            },
            new BleAnswProcessor() { // 9: Read secondary contact
                @Override
                public void processBleAnsw(byte[] answ) {
                    if (answ[0] == '1') {
                        String phoneNumber = new String(answ, 3, answ[2]);
                        mSecondaryContacts.add(phoneNumber);
                        // BIG FACADE: 4 comandos repetidos implicando saber quantos slots existem com contactos secundários.
                    }
                }
            }

    };
    @Override
    public void processAnsw(byte [] answ) {
        // This answer belongs to the last command sent to GuardTracker device.
        if (answ.length > 0) {
            int cmdIdx = getIt() - 1;
            this.answProcessors[cmdIdx].processBleAnsw(answ);
        }
    }

    @Override
    public int getCmdsSize() {
        int size = bleCfgCommands.length;
        return size;
    }

    @Override
    public byte[] getCmd(int i) {
        if (i < 0 || i >= getCmdsSize()) throw new NoSuchElementException();
        return bleCfgCommands[i];
    }

    /**
     * BLE commands in the configuration scope.
     */
    static byte [][] bleCfgCommands = new byte [][] {
            {(byte)GuardTrackerCommands.CommandValues.READ_MONITORING_CFG.ordinal()},        // Read monitoring configuration
            {(byte)GuardTrackerCommands.CommandValues.READ_TRACKING_CFG.ordinal()},          // Read tracking configuration
            {(byte)GuardTrackerCommands.CommandValues.READ_VIGILANCE_CFG.ordinal()},         // Read vigilance configuration
            {(byte)GuardTrackerCommands.CommandValues.READ_WAKE_SENSORS_STATE.ordinal()},    // Read wake sensors state
            {(byte)GuardTrackerCommands.CommandValues.READ_DEVICE_PHONE_NUMBER.ordinal()},   // Read device phone number
            {(byte)GuardTrackerCommands.CommandValues.READ_REF_POS.ordinal()},               // Read device reference position
            {(byte)GuardTrackerCommands.CommandValues.READ_SECONDARY_CONTACT.ordinal(), 1},  // Read device first secondary contact
            {(byte)GuardTrackerCommands.CommandValues.READ_SECONDARY_CONTACT.ordinal(), 2},  // Read device second secondary contact
            {(byte)GuardTrackerCommands.CommandValues.READ_SECONDARY_CONTACT.ordinal(), 3},  // Read device third secondary contact
            {(byte)GuardTrackerCommands.CommandValues.READ_SECONDARY_CONTACT.ordinal(), 4}   // Read device fourth secondary contact
    };
    // BIG FACADE THESE four READ_SECONDARY_CONTACT commands.

}
