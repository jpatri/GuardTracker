package com.patri.guardtracker.synchronization;

import android.os.Handler;
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
public class GuardTrackerSyncConfigWorkflow2 implements BleMessageListener {
    private final static String TAG = GuardTrackerSyncConfigWorkflow2.class.getSimpleName();

    private SyncConfigFromDevListener mListener;
    private SyncConfigFinishListener mFinishListener;
    private GuardTrackerBleConnControl mBleCtrl;
    private int mLastCfgCmd;
    private Handler mHandler;

    /**
     * Answer processor interface definition.
     * Em vez de toda esta proliferação de objectos, podia usar, neste caso, um switch case.
     * O campo que indica o último comando enviado é um inteiro fortalecendo a ideia de um switch case.
     */
    interface BleAnswProcessor {
        void processBleAnsw(byte[] answ);
    }
    /*
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
                    final int monLen = 19;
                    byte [] monData = new byte [monLen];
                    if (answ[1] == GuardTrackerCommands.CmdResValues.CMD_RES_OK.value())
                        System.arraycopy(answ, 2, monData, 0, monLen);
                    monCfg.parse(monData);
                    // Signal listener
                    mListener.onMonitoringConfigReceived(monCfg);
                    // Send next command
                    mLastCfgCmd += 1;
                    final byte [] nextCmd = GuardTrackerCommands.readTrackingConfig();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mBleCtrl.writeBytes(nextCmd);
                        }
                    }, 200);
                }
            },
            new BleAnswProcessor() { // 1: Tracking configuration answer
                @Override
                public void processBleAnsw(byte[] answ) {
                    TrackingConfiguration trackCfg = new TrackingConfiguration();
                    // Decode received message and build object.
                    final int trackLen = 14;
                    byte [] trackData = new byte [trackLen];
                    if (answ[1] == GuardTrackerCommands.CmdResValues.CMD_RES_OK.value())
                        System.arraycopy(answ, 2, trackData, 0, trackLen);
                    trackCfg.parse(trackData);
                    // Signal listener
                    mListener.onTrackingConfigReceived(trackCfg);
                    // Send next command
                    mLastCfgCmd += 1;
                    final byte [] nextCmd = GuardTrackerCommands.readVigilanceConfig();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mBleCtrl.writeBytes(nextCmd);
                        }
                    }, 200);
                }
            },
            new BleAnswProcessor() { // 2: Vigilance configuration answer
                @Override
                public void processBleAnsw(byte[] answ) {
                    VigilanceConfiguration vigilanceCfg = new VigilanceConfiguration();
                    // Decode received message and build object.
                    final int vigLen = 3;
                    byte [] vigData = new byte [vigLen];
                    if (answ[1] == GuardTrackerCommands.CmdResValues.CMD_RES_OK.value())
                        System.arraycopy(answ, 2, vigData, 0, vigLen);
                    vigilanceCfg.parse(vigData);
                    // Signal listener
                    mListener.onVigilanceConfigReceived(vigilanceCfg);
                    // Send next command
                    mLastCfgCmd += 1;
                    final byte [] nextCmd = GuardTrackerCommands.readWakeSensors();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mBleCtrl.writeBytes(nextCmd);
                        }
                    }, 200);
                }
            },
            new BleAnswProcessor() { // 3: Wake sensors status
                @Override
                public void processBleAnsw(byte[] answ) {
                    // Decode received message and build object.
                    int wakeSensorsStatus = 0;
                    if (answ[1] == GuardTrackerCommands.CmdResValues.CMD_RES_OK.value())
                        wakeSensorsStatus = (((byte)answ[2] << 8) | ((byte)answ[3] << 0));
                    // Signal listener
                    mListener.onWakeSensorsReceived(wakeSensorsStatus);
                    // Send next command
                    mLastCfgCmd += 1;
                    final byte [] nextCmd = GuardTrackerCommands.readDevicePhoneNumber();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mBleCtrl.writeBytes(nextCmd);
                        }
                    }, 200);
                }
            },
            new BleAnswProcessor() { // 4: GSM phonenumber
                @Override
                public void processBleAnsw(byte[] answ) {
                    String phoneNumber = "";
                    if (answ[1] == GuardTrackerCommands.CmdResValues.CMD_RES_OK.value()) {
                        String phoneNumberRaw = new String(answ, 2, answ.length-2);
                        // Decode received message and build object.
                        try {
                            phoneNumber = MyPhoneUtils.formatE164(phoneNumberRaw);
                        } catch (NumberParseException e) {
                            Log.e(TAG, e.getErrorType().toString() + ": " + e.getMessage());
                        }
                    }
                    // Signal listener
                    mListener.onDevicePhoneNumberReceived(phoneNumber);
                    if (answ[1] == GuardTrackerCommands.CmdResValues.CMD_RES_OK.value()) {
                        // Send next command
                        mLastCfgCmd += 1;
                        final byte[] nextCmd = GuardTrackerCommands.readRefPos();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mBleCtrl.writeBytes(nextCmd);
                            }
                        }, 200);
                    }
                }
            },
            new BleAnswProcessor() { // 5: Reference position
                @Override
                public void processBleAnsw(byte[] answ) {
                    Position position = null;
                    if (answ[1] == GuardTrackerCommands.CmdResValues.CMD_RES_OK.value()) {
                        Log.i(TAG, "processBleAnsw: create new reference position.");
                        // Decode received message and build object.
                        final int posLen = 18;
                        byte [] posData = new byte [posLen];
                        System.arraycopy(answ, 2, posData, 0, posLen);
                        position = new Position(posData);
                    } else {
                        try { Thread.sleep(300); } catch(InterruptedException e) {}
                    }
                    // Signal listener
                    mListener.onPositionReferenceReceived(position);
                    // Send next command
                    mLastCfgCmd += 1;
                    final byte [] nextCmd = GuardTrackerCommands.readSecondaryContact(1/*first secondary contact*/);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mBleCtrl.writeBytes(nextCmd);
                        }
                    }, 200);
                }
            },
            new BleAnswProcessor() { // 6: Read secondary contact
                @Override
                public void processBleAnsw(byte[] answ) {
                    if (answ[1] == GuardTrackerCommands.CmdResValues.CMD_RES_OK.value()) {
                        // Decode received message and build object.
                        String phoneNumber = new String(answ, 4, answ[3]);
                        // Signal listener
                        mListener.onSecondaryContactReceived(phoneNumber);
                        // Send next command
                        int nextContact = answ[2] + 1;
                        final byte [] nextCmd = GuardTrackerCommands.readSecondaryContact(nextContact);
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mBleCtrl.writeBytes(nextCmd);
                            }
                        }, 200);
                    } else {
                        // No more contacts
                        // Signal listener
                        mFinishListener.onFinishSyncConfig();
                        // Remove this instance as listener of BleMessageListener from BleConnControl
                        mBleCtrl.removeMessageListener(GuardTrackerSyncConfigWorkflow2.this);
                    }
                }
            }
    };

    public GuardTrackerSyncConfigWorkflow2(GuardTrackerBleConnControl bleCtrl, SyncConfigFromDevListener listener, SyncConfigFinishListener finishListener) {
        mListener = listener;
        mFinishListener = finishListener;
        mBleCtrl = bleCtrl;
        mBleCtrl.addMessageListener(this);
        mHandler = new Handler();
    }

    @Override
    public void onMessageReceived(byte[] msgRecv) {
        Log.i(TAG, "onMessageReceived: msgRecv: " + GuardTrackerBleConnControl.dumpMsg(msgRecv) );
        // Allways process answer command, even if it's length is 0, because it is the answer processor that triggers next command to remote device.
        this.answProcessors[mLastCfgCmd].processBleAnsw(msgRecv);
    }
//    @Override
//    public void onMessageSent(byte [] msgSent) {
//        Log.i(TAG, "onMessageSent: msgSent: " + GuardTrackerBleConnControl.dumpMsg(msgSent));
//    }

    public void startSync() {
        byte [] cmd = GuardTrackerCommands.readMonitoringConfig();
        mLastCfgCmd = 0;
        mBleCtrl.writeBytes(cmd);
    }

    /**
     * This method is necessary to wait for external initiation of device phone number.
     */
    public void workflowContinue() {
        // Send next command
        mLastCfgCmd += 1;
        byte[] nextCmd = GuardTrackerCommands.readRefPos();
        mBleCtrl.writeBytes(nextCmd);
    }
}
