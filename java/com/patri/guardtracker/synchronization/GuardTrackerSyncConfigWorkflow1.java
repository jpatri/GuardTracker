package com.patri.guardtracker.synchronization;

import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.patri.guardtracker.bluetooth.BleMessageListener;
import com.patri.guardtracker.bluetooth.GuardTrackerBleConnControl;
import com.patri.guardtracker.communication.GuardTrackerCommands;
import com.patri.guardtracker.custom.MyPhoneUtils;
import com.patri.guardtracker.model.MonitoringConfiguration;
import com.patri.guardtracker.model.Position;
import com.patri.guardtracker.model.TrackingConfiguration;
import com.patri.guardtracker.model.VigilanceConfiguration;

/**
 * Created by patri on 10/11/2016.
 */
public class GuardTrackerSyncConfigWorkflow1 implements BleMessageListener {
    private final static String TAG = GuardTrackerSyncConfigWorkflow1.class.getSimpleName();
    private static final int NO_MORE_COMMANDS            = -1;
    public static final long MONITORING_BIT_FIELD        = (1 << 0);
    public static final long TRACKING_BIT_FIELD          = (1 << 1);
    public static final long VIGILANCE_BIT_FIELD         = (1 << 2);
    public static final long WAKE_SENSORS_BIT_FIELD      = (1 << 3);
    public static final long GSM_PHONE_NUMBER_BIT_FIELD  = (1 << 4);
    public static final long REF_POSITION_BIT_FIELD      = (1 << 5);
    public static final long SECONDARY_CONTACT_BIT_FIELD = (1 << 6);
    public static final long OWNER_BIT_FIELD             = (1 << 7);
    public static final long ALL_BIT_FIELD = (
            MONITORING_BIT_FIELD | TRACKING_BIT_FIELD | VIGILANCE_BIT_FIELD |
            WAKE_SENSORS_BIT_FIELD | GSM_PHONE_NUMBER_BIT_FIELD | REF_POSITION_BIT_FIELD |
            SECONDARY_CONTACT_BIT_FIELD | OWNER_BIT_FIELD);

    private GuardTrackerSyncConfigListener mListener;   // listener to be notified during workflow process of messages
    private GuardTrackerBleConnControl mBleCtrl;        // controller to communicate with remote device
    private int mLastCfgCmd;                            // identifies the last command sent to remote device
    private long mBitmask;                              // bit field with configurations to be synchronized
    private long mWorkflowBitmask;                      // bit field used to detect end of workflow

    /**
     * Answer processor interface definition.
     * Em vez de toda esta proliferação de objectos, podia usar, neste caso, um switch case.
     * O campo que indica o último comando enviado é um inteiro fortalecendo a ideia de um switch case.
     */
    interface BleAnswProcessor {
        void processBleAnsw(byte[] answ);
    }

    /**
     *
     * @return -1 if no cmd to send
     */
    private int getFirstCfgCmd() {
        int firstCfgCmd = 0;
        long bitField = 1 << firstCfgCmd;
        while(mWorkflowBitmask != 0 && (mBitmask & bitField) == 0) {
            mWorkflowBitmask &= ~bitField;
            firstCfgCmd += 1;
            bitField = 1 << firstCfgCmd;
        }
        if (mWorkflowBitmask == 0)
            firstCfgCmd = NO_MORE_COMMANDS;
        mLastCfgCmd = firstCfgCmd;
        return firstCfgCmd;
    }

    /**
     *
     * @return -1 when there are no more commands to send
     */
    private int getNextCfgCmd() {
        if (mLastCfgCmd == NO_MORE_COMMANDS) return NO_MORE_COMMANDS;

        int nextCfgCmd = mLastCfgCmd + 1;
        long bitField = 1 << nextCfgCmd;
        mWorkflowBitmask &= ~(1 << mLastCfgCmd);
        while ( mWorkflowBitmask != 0 && (mBitmask & bitField) == 0) {
            mWorkflowBitmask &= ~bitField;
            nextCfgCmd += 1;
            bitField = 1 << nextCfgCmd;
        }
        if (mWorkflowBitmask == 0)
            nextCfgCmd = NO_MORE_COMMANDS;
        mLastCfgCmd = nextCfgCmd;
        return nextCfgCmd;
    }
    private void sendNextCommand() {
        int cfgCmd = getNextCfgCmd();
        if (cfgCmd == NO_MORE_COMMANDS)
            // No more commands
            // Signal listener
            mListener.onFinishSyncConfig();
        byte [] nextCmd = getCommand(cfgCmd);
        mBleCtrl.writeBytes(nextCmd);
    }
    private byte [] getCommand(int cfgCmd) {
        final byte [][] commands = new byte [][] {
                GuardTrackerCommands.readMonitoringConfig(),
                GuardTrackerCommands.readTrackingConfig(),
                GuardTrackerCommands.readVigilanceConfig(),
                GuardTrackerCommands.readWakeSensors(),
                GuardTrackerCommands.readDevicePhoneNumber(),
                GuardTrackerCommands.readRefPos(),
                GuardTrackerCommands.readSecondaryContact(1), // First contact
                GuardTrackerCommands.readOwner()
        };
        return commands[cfgCmd];
    }
    /**
     * The different implementations of processBleAnsw follow the same steps:
     * 1. Decode configurations from received message
     * 2. Create and build the right object. The two steps above may be done in the same step.
     * 3. Signal listener passing new constructed object
     * 4. Send next command except it the last command.
     */
    final BleAnswProcessor [] answProcessors = new BleAnswProcessor[] {
            new BleAnswProcessor() { // 0: Monitoring configuration answer
                @Override
                public void processBleAnsw(byte[] answ) { // Process monitoring configuration
                    MonitoringConfiguration monCfg = new MonitoringConfiguration();
                    // Decode received message and build object.
                    monCfg.parse(answ);
                    // Signal listener
                    mListener.onMonitoringConfigReceived(monCfg);
                    // Send next command
                    sendNextCommand();
                }
            },
            new BleAnswProcessor() { // 1: Tracking configuration answer
                @Override
                public void processBleAnsw(byte[] answ) {
                    TrackingConfiguration trackCfg = new TrackingConfiguration();
                    // Decode received message and build object.
                    trackCfg.parse(answ);
                    // Signal listener
                    mListener.onTrackingConfigReceived(trackCfg);
                    // Send next command
                    sendNextCommand();
                }
            },
            new BleAnswProcessor() { // 2: Vigilance configuration answer
                @Override
                public void processBleAnsw(byte[] answ) {
                    VigilanceConfiguration vigilanceCfg = new VigilanceConfiguration();
                    // Decode received message and build object.
                    vigilanceCfg.parse(answ);
                    // Signal listener
                    mListener.onVigilanceConfigReceived(vigilanceCfg);
                    // Send next command
                    sendNextCommand();
                }
            },
            new BleAnswProcessor() { // 3: Wake sensors status
                @Override
                public void processBleAnsw(byte[] answ) {
                    // Decode received message and build object.
                    int wakeSensorsStatus = (((byte)answ[0] << 8) | ((byte)answ[1] << 0));
                    // Signal listener
                    mListener.onWakeSensorsReceived(wakeSensorsStatus);
                    // Send next command
                    sendNextCommand();
                }
            },
            new BleAnswProcessor() { // 4: GSM phonenumber
                @Override
                public void processBleAnsw(byte[] answ) {
                    String phoneNumber = "";
                    if (answ.length > 0) {
                        String phoneNumberRaw = new String(answ);
                        // Decode received message and build object.
                        try {
                            phoneNumber = MyPhoneUtils.formatE164(phoneNumberRaw);
                        } catch (NumberParseException e) {
                            Log.e(TAG, e.getErrorType().toString() + ": " + e.getMessage());
                        }
                    }
                    // Signal listener
                    mListener.onDevicePhoneNumberReceived(phoneNumber);
                    if (answ.length > 0) {
                        // Send next command
                        sendNextCommand();
                    }
                }
            },
            new BleAnswProcessor() { // 5: Reference position
                @Override
                public void processBleAnsw(byte[] answ) {
                    Position position = null;
                    // BIG FACADE: This e2prom has ff filled in position ref. Replace the condition commented when this problem is resolved in trackApp.c
                    // The solution has to include a field in e2prom telling if ref pos is valid.
                    if (answ.length > 0 && (answ[0] != 0 || answ[1] != 0 || answ[2] != 0 || answ[3] != 0)) {
//                    if (answ.length > 0) {
                        // Decode received message and build object.
                        position = new Position(answ);
                    }
                    // Signal listener
                    mListener.onPositionReferenceReceived(position);
                    // Send next command
                    sendNextCommand();
                }
            },
            new BleAnswProcessor() { // 6: Read secondary contact
                @Override
                public void processBleAnsw(byte[] answ) {
                    if (answ[0] == '1') { // Continue reading secondary contacts
                        // Decode received message and build object.
                        String phoneNumber = new String(answ, 3, answ[2]);
                        // Signal listener
                        mListener.onSecondaryContactReceived(phoneNumber);
                        // Send next command
                        int nextContact = answ[1] + 1;
                        byte [] nextCmd = GuardTrackerCommands.readSecondaryContact(nextContact);
                        mBleCtrl.writeBytes(nextCmd);
                    } else
                        // There are no more secondary contacts, so send next command if exists
                        sendNextCommand();
                }
            },
            new BleAnswProcessor() { // 7: Read owner contact
                @Override
                public void processBleAnsw(byte[] answ) {
                    String phoneNumber = "";
                    if (answ.length > 0) {
                        String phoneNumberRaw = new String(answ);
                        // Decode received message and build object.
                        try {
                            phoneNumber = MyPhoneUtils.formatE164(phoneNumberRaw);
                        } catch (NumberParseException e) {
                            Log.e(TAG, e.getErrorType().toString() + ": " + e.getMessage());
                            phoneNumber = "";
                        }
                    }
                    // Signal listener
                    mListener.onDevicePhoneNumberReceived(phoneNumber);
                    // Send next command
                    sendNextCommand();
                }
            }

    };

    /**
     *
     * @param bleCtrl ble controller to be used to communicate with remote device
     * @param listener listener to receive notifications during workflow synchronization
     * @param bitmask bitmask with configurations to be synchronized
     */
    public GuardTrackerSyncConfigWorkflow1(GuardTrackerBleConnControl bleCtrl, GuardTrackerSyncConfigListener listener, long bitmask) {
        mListener = listener;
        mBleCtrl = bleCtrl;
        mBleCtrl.addMessageListener(this);
        mBitmask = bitmask;
    }

    @Override
    public void onMessageReceived(byte[] msgRecv) {
        Log.i(TAG, "onMessageReceived: msgRecv: " + GuardTrackerBleConnControl.dumpMsg(msgRecv) );
        // Allways process answer command, even if it's length is 0, because it is the answer processor that triggers next command to remote device.
        this.answProcessors[mLastCfgCmd].processBleAnsw(msgRecv);
    }

    public void startSync() {
        mWorkflowBitmask = mBitmask;
        mLastCfgCmd = 0;
        int cfgCmd = getFirstCfgCmd();
        byte [] cmd = getCommand(cfgCmd);
        mBleCtrl.writeBytes(cmd);
    }

    /**
     * This method is necessary to wait for external initiation of device phone number.
     */
    public void workflowContinue() {
        // Send next command
        sendNextCommand();
    }
}
