package com.patri.guardtracker.synchronization;

import android.os.Handler;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.patri.guardtracker.bluetooth.BleMessageListener;
import com.patri.guardtracker.bluetooth.GuardTrackerBleConnControl;
import com.patri.guardtracker.communication.GuardTrackerCommands;
import com.patri.guardtracker.custom.MyPhoneUtils;
import com.patri.guardtracker.model.GuardTracker;
import com.patri.guardtracker.model.MonitoringConfiguration;
import com.patri.guardtracker.model.Position;
import com.patri.guardtracker.model.TrackingConfiguration;
import com.patri.guardtracker.model.VigilanceConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by patri on 10/11/2016.
 */
public class SyncConfigWorkflow implements BleMessageListener {
    private final static String TAG = SyncConfigWorkflow.class.getSimpleName();

    private SyncConfigFromDevListener mFromDevListener;
//    private GuardTrackerSyncConfigToDevListener mFromDevListener;
    private SyncConfigCmdProcessedListener mCmdProcessedListener;
    private SyncConfigFinishListener mFinishListener;
    private GuardTrackerBleConnControl mBleCtrl;
    private Handler mHandler;

    //
    // Collection of commands;
    //

    final private byte [] mCompleteSyncConfigFromDevCommands[] = {
            GuardTrackerCommands.readMonitoringConfig(),
            GuardTrackerCommands.readTrackingConfig(),
            GuardTrackerCommands.readVigilanceConfig(),
            GuardTrackerCommands.readWakeSensors(),
            GuardTrackerCommands.readDevicePhoneNumber(),
            GuardTrackerCommands.readRefPos(),
            GuardTrackerCommands.readSecondaryContact(1),
            GuardTrackerCommands.readOwner()
    };
    private List<byte []> mCmdToSend;
    private List<byte []> mCompleteCmdFromDevList;
    private Iterator<byte []> mCmdIt;

    /**
     * Answer processor interface definition.
     * Em vez de toda esta proliferação de objectos, podia usar, neste caso, um switch case.
     * O campo que indica o último comando enviado é um inteiro fortalecendo a ideia de um switch case.
     */
    interface BleAnswProcessor {
        void processBleAnsw(byte[] answ);
    }


    private void sendNextCommand() {
        if (mCmdIt.hasNext()) {
            final byte[] nextCmd = mCmdIt.next();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBleCtrl.writeBytes(nextCmd);
                }
            }, 200);
        } else {
            // Remove this instance as listener of BleMessageListener from BleConnControl
            mBleCtrl.removeMessageListener(SyncConfigWorkflow.this);
            // Signal end of sync config
            mFinishListener.onFinishSyncConfig();
            // Clear list of sent commands
            mCmdToSend.clear();
        }
    }

    final BleAnswProcessor mNotSupporttedAnswProcessor = new BleAnswProcessor() {
        @Override
        public void processBleAnsw(byte[] answ) {
            Log.w(SyncConfigWorkflow.TAG, "No support to process the answer for the command with id "+answ[2]);
            sendNextCommand();
        }
    };
    final BleAnswProcessor mDefaultAnswProcessor = new BleAnswProcessor() {
        @Override
        public void processBleAnsw(byte[] answ) {
            Log.w(SyncConfigWorkflow.TAG, "Default answer processor for command with id "+answ[1]);
            if (mCmdProcessedListener != null)
                mCmdProcessedListener.onCommandProcessed(
                    GuardTrackerCommands.CommandValues.valueOf(answ[2]),
                    GuardTrackerCommands.CmdResValues.valueOf(answ[1])
                );
            sendNextCommand();
        }
    };
    /*
     * The different implementations of processBleAnsw follow the same steps:
     * 1. Decode configurations from received message
     * 2. Create and build the right object. The two steps above may be done in the same step.
     * 3. Signal listener passing new constructed object
     * 4. Send next command except it is the last command.
     */
    final BleAnswProcessor [] answProcessors = new BleAnswProcessor[] {
            mDefaultAnswProcessor, // 0: CLEAN_E2PROM - Clean all data in e2prom (reset the equipment) answer
            mNotSupporttedAnswProcessor, // 1: CHANGE_OWNER - Modify mobile number registered in equipment answer
            mNotSupporttedAnswProcessor, // 2: REF_POSITION - Acquire new GPS coordinates and set them as equipment's reference position answer
            mNotSupporttedAnswProcessor, // 3: ALARM_INTERVAL - Interval time that sms are sent in tracking mode answer
            mNotSupporttedAnswProcessor, // 4: REF_TIME_TRACKING - ?????
            mNotSupporttedAnswProcessor, // 5: TEMPERATURE_BOUNDARIES - Temperature boundaries (low and high levels) answer
            mNotSupporttedAnswProcessor, // 6: MONITORING_GPS_BOUNDARIES - GPS boundaries to determine different position in monitoring mode answer
            mNotSupporttedAnswProcessor, // 7: TRACKING_GPS_BOUNDARIES - GPS boundaries to determine different position in tracking mode answer
            mNotSupporttedAnswProcessor, // 8: TILT_DIS - Disable accelerometer answer
            mNotSupporttedAnswProcessor, // 9: TILT_ENA - Enable accelerometer answer
            mNotSupporttedAnswProcessor, //10: SIM_BALANCE_THRESHOLD - Modify balance value threshold answer
            mNotSupporttedAnswProcessor, //11: RETURN_APP_STATE - Return internal app state answer
            mNotSupporttedAnswProcessor, //12: RTC_TIME_INTERVAL - Modify the period for monitoring answer
            mNotSupporttedAnswProcessor, //13: RETURN_INFO - Return app collected info since date answer
            mNotSupporttedAnswProcessor, //14: DELETE_LOGS - Delete logs between dates answer
            mDefaultAnswProcessor, //15: WAKE_SENSORS - Ena/Dis wake sensors: accelerometer, rtc, ble. The wake_by_manual and wake_by_jtag are always enabled answer
            mNotSupporttedAnswProcessor, //16: RST_REF_POS - Reset reference location answer
            mDefaultAnswProcessor, //17: RST_OWNER - Reset owner phonenumber answer
            mNotSupporttedAnswProcessor, //18: READ_LOGS - Read logs from device (don't deleteDeep them) answer
            mNotSupporttedAnswProcessor, //19: DEL_LOGS - Delete logs answer
            mNotSupporttedAnswProcessor, //20: SDCARD_INFO - Get SDCard info (disk capacity, disk space in use, total files in disk) answer
            mNotSupporttedAnswProcessor, //21: TEST_LONG_MSG - Test long message (????) answer
            mNotSupporttedAnswProcessor, //22: TEST_SEND - Test send message (????) answer
            new BleAnswProcessor() { //23: READ_MONITORING_CFG - Read monitoring configuration answer
                @Override
                public void processBleAnsw(byte[] answ) {
                    MonitoringConfiguration monCfg = new MonitoringConfiguration();
                    // Decode received message and build object.
                    final int monLen = 19;
                    byte [] monData = new byte [monLen];
                    if (answ[1] == GuardTrackerCommands.CmdResValues.CMD_RES_OK.value())
                        System.arraycopy(answ, 2, monData, 0, monLen);
                    monCfg.parse(monData);
                    // Signal listeners
                    if (mFromDevListener != null)
                        mFromDevListener.onMonitoringConfigReceived(monCfg);
                    // Send next command
                    sendNextCommand();
                }
            },
            new BleAnswProcessor() { //24: READ_TRACKING_CFG - Read tracking configuration answer
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
                    if (mFromDevListener != null)
                        mFromDevListener.onTrackingConfigReceived(trackCfg);
                    // Send next command
                    sendNextCommand();
                }
            },
            new BleAnswProcessor() { //25: READ_VIGILANCE_CFG - Read vigilance configuration answer
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
                    if (mFromDevListener != null)
                        mFromDevListener.onVigilanceConfigReceived(vigilanceCfg);
                    // Send next command
                    sendNextCommand();
                }
            },
            new BleAnswProcessor() { //26: READ_WAKE_SENSORS_STATE - Read wake sensors configuration answer
                @Override
                public void processBleAnsw(byte[] answ) {
                    // Decode received message and build object.
                    int wakeSensorsStatus = 0;
                    if (answ[1] == GuardTrackerCommands.CmdResValues.CMD_RES_OK.value())
                        wakeSensorsStatus = (((byte)answ[2] << 8) | ((byte)answ[3] << 0));
                    // Signal listener
                    if (mFromDevListener != null)
                        mFromDevListener.onWakeSensorsReceived(wakeSensorsStatus);
                    // Send next command
                    sendNextCommand();
                }
            },
            new BleAnswProcessor() { //27: READ_DEVICE_PHONE_NUMBER - Read phone number of guard tracker device answer
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
                    if (mFromDevListener != null)
                        mFromDevListener.onDevicePhoneNumberReceived(phoneNumber);
                    // Send next command
                    // For now, just continue with the solution that breaks workflow on a KO phone number reception.
                    // In the future, removes the if and send next command always even in a KO reception condition.
                    // Without the if, the phone number must be validated in the end of the process.
                    if (answ[1] == GuardTrackerCommands.CmdResValues.CMD_RES_OK.value())
                        sendNextCommand();
//                    if (answ[1] == GuardTrackerCommands.CmdResValues.CMD_RES_OK.value()) {
//                        // Send next command
//                        mLastCfgCmd += 1;
//                        final byte[] nextCmd = GuardTrackerCommands.readRefPos();
//                        mHandler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                mBleCtrl.writeBytes(nextCmd);
//                            }
//                        }, 200);
//                    }
                }
            },
            new BleAnswProcessor() { //28: READ_REF_POS - Read reference position answer
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
                    if (mFromDevListener != null)
                        mFromDevListener.onPositionReferenceReceived(position);
                    // Send next command
                    sendNextCommand();
                }
            },
            mDefaultAnswProcessor, //29: ADD_SECONDARY_CONTACT - Add new contact to device to use in sending alerts answer
            mDefaultAnswProcessor, //30: DELETE_SECONDARY_CONTACT - Delete contact from device answer
            new BleAnswProcessor() { //31: READ_SECONDARY_CONTACT - Read secondary contacts answer
                @Override
                public void processBleAnsw(byte[] answ) {
                    if (answ[1] == GuardTrackerCommands.CmdResValues.CMD_RES_OK.value()) {
                        // Decode received message and build object.
                        String phoneNumber = new String(answ, 4, answ[3]);
                        // Signal listener
                        if (mFromDevListener != null)
                            mFromDevListener.onSecondaryContactReceived(phoneNumber);
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
                        sendNextCommand();
                    }
                }
            },
            mDefaultAnswProcessor, //32: WRITE_MONITORING_CFG - Write monitoring configuration (one per write, parametrized) answer
            null, //33: READ_OWNER - Read owner phone number answer
            null  //34: STOP_PENDING_CMD - Cancel pending operation (any pending operation) answer

    };

    protected SyncConfigWorkflow(
            GuardTrackerBleConnControl bleCtrl,
            SyncConfigFromDevListener configFromDevlistener,
            SyncConfigCmdProcessedListener configCmdProcessedListener,
            SyncConfigFinishListener finishListener) {
        mFromDevListener = configFromDevlistener;
        mFinishListener = finishListener;
        mBleCtrl = bleCtrl;
        mBleCtrl.addMessageListener(this);
        mHandler = new Handler();

        mCmdToSend = new ArrayList<>();
        mCompleteCmdFromDevList = Arrays.asList(mCompleteSyncConfigFromDevCommands);
    }
//    public SyncConfigWorkflow(
//            GuardTrackerBleConnControl bleCtrl,
//            GuardTrackerSyncConfigToDevListener configFromDevlistener,
//            SyncConfigFinishListener finishListener) {
//        this(bleCtrl, null, configFromDevlistener, finishListener);
//    }
    public SyncConfigWorkflow(
            GuardTrackerBleConnControl bleCtrl,
            SyncConfigFromDevListener configFromDevlistener,
            SyncConfigFinishListener finishListener) {
        this(bleCtrl, configFromDevlistener, null, finishListener);
    }
    public SyncConfigWorkflow(
            GuardTrackerBleConnControl bleCtrl,
            SyncConfigCmdProcessedListener configCmdProcessedListener,
            SyncConfigFinishListener finishListener) {
        this(bleCtrl, null, configCmdProcessedListener, finishListener);
    }

    @Override
    public void onMessageReceived(byte[] msgRecv) {
        Log.i(TAG, "onMessageReceived: msgRecv: " + GuardTrackerBleConnControl.dumpMsg(msgRecv) );
        // Always process answer command, even if it's length is 0, because it is the answer processor that triggers next command to remote device.
        this.answProcessors[msgRecv[0]].processBleAnsw(msgRecv);
    }
//    @Override
//    public void onMessageSent(byte [] msgSent) {
//        Log.i(TAG, "onMessageSent: msgSent: " + GuardTrackerBleConnControl.dumpMsg(msgSent));
//    }

    public SyncConfigWorkflow addCommand(byte [] cmd) {
        mCmdToSend.add(cmd); return this;
    }
    public void startSync() {
        mCmdIt = mCmdToSend.iterator();
        sendNextCommand();
    }
    public void startCompleteSyncFromDev() {
        mCmdIt = mCompleteCmdFromDevList.iterator();
        sendNextCommand();
    }
//    public void startCompleteSyncToDev(GuardTracker guardTracker) {
//        // ToDo - sync all values of guardTracker object to device
//        // Ou talvez não. Para este e para os seguintes métodos.
//        // Acabo sempre por chegar à conclusão que a necessidade de converter o objecto em
//        // array de bytes pode ser feito por quem pretende enviar o comando e usar os métodos add e startSync genéricos,
//        // e também porque, geralmente, a alteração de uma configuração no despositivo é realizada com um comando isolado,
//        // ou seja, raramente se pretende enviar uma sequência de configurações completas.
//    }
//    public void startCompleteMonCfgSyncToDev(MonitoringConfiguration monCfg) {
//        // ToDo - sync all values of monCfg object to device
//    }
//    public void startCompleteTrackCfgSyncToDev(TrackingConfiguration trackCfg) {
//        // ToDo - sync all values of trackCfg object to device
//    }
//    public void startCompleteVigCfgSyncToDev(VigilanceConfiguration vigCfg) {
//        // ToDo - sync all values of vigCfg object to device
//    }

    /**
     * This method is necessary to wait for external initiation of device phone number.
     */
    public void workflowContinue() {
        // Send next command
        if (mCmdIt.hasNext()) {
            byte[] nextCmd = mCmdIt.next();
            mBleCtrl.writeBytes(nextCmd);
        } else
            mFinishListener.onFinishSyncConfig();
    }
}
