package com.patri.guardtracker;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.i18n.phonenumbers.NumberParseException;
import com.patri.guardtracker.bluetooth.BleMessageListener;
import com.patri.guardtracker.bluetooth.BleConnectionStateChangeListener;
import com.patri.guardtracker.bluetooth.GuardTrackerBleConnControl;
import com.patri.guardtracker.communication.GuardTrackerCommands;
import com.patri.guardtracker.custom.MyPhoneUtils;
import com.patri.guardtracker.model.GuardTracker;
import com.patri.guardtracker.model.MonitoringConfiguration;
import com.patri.guardtracker.model.MonitoringInfo;
import com.patri.guardtracker.model.PermissionsChecker;
import com.patri.guardtracker.model.Position;
import com.patri.guardtracker.model.SecondaryContactsDbHelper;
import com.patri.guardtracker.model.TrackSession;
import com.patri.guardtracker.model.TrackingConfiguration;
import com.patri.guardtracker.model.VigilanceConfiguration;
import com.patri.guardtracker.permissions.PermissionsActivity;
import com.patri.guardtracker.sms.SmsReceivedEarlierActivity;
import com.patri.guardtracker.synchronization.SyncConfigCmdProcessedListener;
import com.patri.guardtracker.synchronization.SyncConfigFinishListener;
import com.patri.guardtracker.synchronization.SyncConfigFromDevListener;
import com.patri.guardtracker.synchronization.SyncConfigWorkflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class GuardTrackerActivity extends AppCompatActivity implements BleConnectionStateChangeListener, DialogListener {
    public final static String TRACK_SESSION_ITEM_SELECTED = "com.patri.guardtracker.TRACK_SESSION_ITEM_SELECTION";
    public final static String CONFIG_ID = "com.patri.guardtracker.CONFIG_ID";
    public final static String BACKUP_CREATE = "com.patri.guardtracker.BACKUP_CREATE";
    public final static String MON_INFO_ITEM_SELECTED = "com.patri.guardtracker.MON_INFO_ITEM_SELECTED";
    public final static String GUARD_TRACKER_ID = "com.patri.guardtracker.GUARD_TRACKER_ID";
    public final static String GUARD_TRACKER_NAME = "com.patri.guardtracker.GUARD_TRACKER_NAME";
    public static final String RESULT_MON_CFG = "com.patri.guardtracker.RESULT_MON_CGF";
    public static final String RESULT_TRACK_CFG = "com.patri.guardtracker.RESULT_TRACK_CGF";
    public static final String RESULT_VIG_CFG = "com.patri.guardtracker.RESULT_VIG_CGF";
    private final static String TAG = GuardTrackerActivity.class.getSimpleName();

    /* Attribute for permissions checeker */
    static final String[] PERMISSIONS = new String[]{Manifest.permission.READ_SMS};
    //private PermissionsChecker mChecker;
    private static final int REQUEST_ENABLE_READ_SMS = 0;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_MON_CFG = 2;
    private static final int REQUEST_TRACK_CFG = 3;
    private static final int REQUEST_VIG_CFG = 4;

    private int mGuardTrackerId;
    private GuardTracker mGuardTracker;
//    private MonitoringConfiguration mMonCfg;
    private SyncConfigWorkflow mSyncConfigWorkflow;
    private GuardTracker mTempGuardTracker; // Used in resync App command
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    public enum BleStateEnum {
        Disconnected, Scanning, Connect, Connecting, Connected;
        private String[] valuesStr = {"Disconnected", "Scanning", "In range", "Connecting", "Connected"};
        private int[] valuesBackgroundColor = {Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.GREEN};
        private int[] valuesForegroundColor = {Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.BLACK};

        public String toString() {
            String statusStr = valuesStr[this.ordinal()];
            return statusStr;
        }

        public int toBackgroundColor() {
            return valuesBackgroundColor[this.ordinal()];
        }

        public int toForegroundColor() {
            return valuesForegroundColor[this.ordinal()];
        }
    }

    /**
     * This enumerate is used to establish the Tag to be passed to dialog. The same dialog type is called in several situations.
     */
    enum DialogFragmentTags {
        SelfDeviceEliminate, DevicesEliminate, WakeSensorsConfig, SecondaryContactAdd,
        SecondaryContactDelete, FactoryDefaultsReset, AppResync, DevicePhoneNumber, ChangeOwner,
        ChangeDevicePhoneNumber, ChangeDeviceNickname, WakeSensorsRestore, WakeSensorsSync, WakeSensorsRefresh,
        MonCfgRestore, MonCfgSync, MonCfgRefresh,
        TrackCfgRestore, TrackCfgSync, TrackCfgRefresh,
        VigCfgRestore, VigCfgSync, VigCfgRefresh
        ;
        private String[] valuesStr = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13",
            "14", "15", "16", "17", "18", "19", "20", "21", "22"};

        public String toString() {
            String dialogStr = valuesStr[this.ordinal()];
            return dialogStr;
        }
    }

    // Bluetooth attributes
    private GuardTrackerBleConnControl mBleConnControl;
    private BluetoothAdapter mBluetoothAdapter;
    private BleStateEnum mBleState;
    private Handler mHandler;
    // Stops scanning after 10 seconds.
    public static final long SCAN_PERIOD = 20000;

    // No momento da introdução dos próximos atributos fica a ideia que poder ser
    // demasiada penalização ler a base de dados só para obter o número total de entradas.
    private ArrayList<MonitoringInfo> mMonInfoList;
    private ArrayList<TrackSession> mTrackSessionList;
    private ArrayList<String> mContactsSecondaryList;

    //private TrackingConfiguration mTrackCfg;
    //private VigilanceConfiguration mVigilanceCfg;
    private TextView mIdNameView;
    private ImageButton mIdNameImgButton;
    private TextView mIdBleAddrView;
    private TextView mIdGsmAddrView;
    private ImageButton mIdGsmAddrImgButton;
    private TextView mLastMonInfoDatetimeView;
    private TextView mLastMonInfoTimeView;
    private TextView mLastMonInfoLatView;
    private TextView mLastMonInfoLngView;
    private TextView mLastMonInfoAltView;
    private TextView mLastMonInfoSatView;
    private TextView mLastMonInfoHdopView;
    private TextView mLastMonInfoFixedView;
    private TextView mLastMonInfoTempView;
    private TextView mLastMonInfoSimView;
    private TextView mLastMonInfoBatView;
    private TextView mPosRefDatetimeView;
    private TextView mPosRefLatView;
    private TextView mPosRefLngView;
    private TextView mPosRefAltView;
    private TextView mPosRefSatView;
    private TextView mPosRefHdopView;
    private TextView mPosRefFixedView;
    private TextView mBleStatusValueView;
    private EditText mBleStatusCommandsView;
    private Button   mBleStatusCommandsButton;

    private TextView mContactsOwnerView;
    private ViewGroup mContactsSecondaryViewGroup;
    private TextView mContactsSecondaryEmptyView;
    private Button mContactsOwnerRefreshButton;
    private Button mContactsOwnerRstButton;
    private Button mContactsSecondaryAddButton;
    private Button mContactsSecondaryRefreshButton;
    private Button mContactsSecondaryDelButton;

    private Button mLastMonInfoGoogleMapsButton;
    private Button mPosRefGoogleMapsButton;
    private Button mPosRefRefreshButton;
    private Button mPosRefNewRefButton;
    private Button mPosRefCancelNewRefButton;
    private Button mPosRefPendingButton;

    private TextView mSensorsConfigBleView;
    private TextView mSensorsConfigRtcView;
    private TextView mSensorsConfigAccView;
    private TextView mSensorsSyncedStateView;
    private Spinner mSensorsConfigSpinner;
    private Spinner mSensorsSyncSpinner;

    private TextView mMonCfgDatetimeView;
    private TextView mMonCfgSyncedStateView;
    private Spinner mMonCfgConfigSpinner;
    private Spinner mMonCfgSyncSpinner;
    private TextView mTrackCfgSyncedStateView;
    private Spinner mTrackCfgConfigSpinner;
    private Spinner mTrackCfgSyncSpinner;
    private TextView mVigCfgSyncedStateView;
    private Spinner mVigCfgConfigSpinner;
    private Spinner mVigCfgSyncSpinner;
    //private Button mTrackCfgDetailsButton;
    //private Button mVigilanceCfgDetailsButton;
    private TextView mMonInfoSizeEntriesView;
    private Button mMonInfoTempButton;
    private Button mMonInfoPosButton;
    private Button mMonInfoSimButton;
    private Button mMonInfoBatButton;
    private TextView mTrackSessionSizeEntriesView;
    private Button mTrackSessionAllButton;
    private Button mTrackSessionLastButton;
    private Button mTrackSessionSelectButton;

//    private ImageButton   mNicknameEditButton;
//    private ImageButton   mNicknameDoneButton;
//    private ImageButton   mNicknameCancelButton;

    // Experimentei este código com intuito de fazer desaperecer o soft keyboard quando o mNameIdView perdesse o focus.
    // Não tive o efeito desejado mas não eliminei o código porcausa do exemplo que faz desaparecer o dito teclado.
//    private class MyFocusChangeListener implements View.OnFocusChangeListener {
//
//        public void onFocusChange(View v, boolean hasFocus){
//
//            if(v.getId() == R.id.identification_nickname && !hasFocus) {
//
//                InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//
//            }
//        }
//    }

    // Device scan callback.
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            if (device.getAddress().equals(mGuardTracker.getBleId())) {
                stopBleScan();
//                updateBleStateAndInvalidateMenu(BleStateEnum.Connect);

                if (mBleConnControl != null)
                    mBleConnControl.connect();
                else {
                    mBleConnControl = new GuardTrackerBleConnControl(getBaseContext(), mGuardTracker.getBleId(),
                            mGuardTracker.getOwnerPhoneNumber(), GuardTrackerActivity.this, null);
                }
                updateBleStateAndInvalidateMenu(BleStateEnum.Connecting);
            }
        }
    };

    private Runnable stopBleScanRunnable = new Runnable() {
        @Override
        public void run() {
            stopBleScan();
            updateBleStateAndInvalidateMenu(BleStateEnum.Disconnected);
        }
    };

    private void startBleScan() {
        // Change default scan parameters
        ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder();
        // LOW_LATENCY enables the capture of device advertisements with a cycle of 10 seconds.
        scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        ScanSettings scanSettings = scanSettingsBuilder.build();
        // Stops scanning after a pre-defined scan period.
        mHandler.postDelayed(stopBleScanRunnable, SCAN_PERIOD);
//        mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
        mBluetoothAdapter.getBluetoothLeScanner().startScan(null, scanSettings, mScanCallback);
        updateBleStateAndInvalidateMenu(BleStateEnum.Scanning);
    }

    private void stopBleScan() {
        mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
        mHandler.removeCallbacks(stopBleScanRunnable);
    }

    @Override
    public void onAuthenticated() {
        Log.i(TAG, "onAuthenticated");
        updateBleStateAndInvalidateMenu(BleStateEnum.Connected);
        updateWakeSensorsView();
        updateMonCfgView();
        updateTrackCfgView();
        updateVigCfgView();
        refPosButtonVisibilityUpdate(false);
        mContactsOwnerRefreshButton.setVisibility(View.VISIBLE);
        mContactsOwnerRstButton.setVisibility(View.VISIBLE);
        mContactsSecondaryAddButton.setVisibility(View.VISIBLE);
        mContactsSecondaryRefreshButton.setVisibility(View.VISIBLE);
        if (mContactsSecondaryList.size() > 0)
            mContactsSecondaryDelButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBindServiceError() {
        Log.i(TAG, "onBindServiceError");
    }

    @Override
    public void onUnableToDiscoverServices() {
        Log.i(TAG, "onUnableToDiscoverServices");
    }

    @Override
    public void onConnected() {
        Log.i(TAG, "onConnected");
    }

    @Override
    public void onReconnected() {
        Log.i(TAG, "onReconnected");
    }

    @Override
    public void onDisconnected() {
        Log.i(TAG, "onDisconnected");
        updateBleStateAndInvalidateMenu(BleStateEnum.Connect);
        updateWakeSensorsView();
        updateMonCfgView();
        updateTrackCfgView();
        updateVigCfgView();
        mPosRefRefreshButton.setVisibility(View.GONE);
        mPosRefNewRefButton.setVisibility(View.GONE);
        mPosRefCancelNewRefButton.setVisibility(View.GONE);
        mPosRefPendingButton.setVisibility(View.GONE);
        mContactsOwnerRstButton.setVisibility(View.GONE);
        mContactsOwnerRefreshButton.setVisibility(View.GONE);
        mContactsSecondaryAddButton.setVisibility(View.GONE);
        mContactsSecondaryDelButton.setVisibility(View.GONE);
        mContactsSecondaryRefreshButton.setVisibility(View.GONE);
    }

    @Override
    public void onUnableToDiscoverDataService() {
        Log.i(TAG, "onUnableToDiscoverDataService");
    }

    @Override
    public void onDataChannelDiscovered() {
        Log.i(TAG, "onDataChannelDiscovered");
    }

    @Override
    public void onUnexpectedMessage(byte[] msg, int detailMsgRscId) {
        Log.i(TAG, "onUnexpectedMessage");
    }

    @Override
    public void onReady() {
        Log.i(TAG, "onReady");
    }

    private boolean isBleConnectionValid() {
        return (mBleState == BleStateEnum.Connected);
    }

    private void updateBleStateAndInvalidateMenu(BleStateEnum newState) {
        mBleState = newState;
        updateBleStatusView();
        invalidateOptionsMenu();
    }

    private void startPermissionsActivity() {
        PermissionsActivity.startActivityForResult(this, REQUEST_ENABLE_READ_SMS, PERMISSIONS);
    }

    private void startSmsReceivedEarlierActivity() {
        Intent intent = new Intent(this, SmsReceivedEarlierActivity.class);
        int guardTrackerId = mGuardTracker.get_id();
        String guardTrackerName = mGuardTracker.getName();
        intent.putExtra(GuardTrackerActivity.GUARD_TRACKER_ID, guardTrackerId);
        intent.putExtra(GuardTrackerActivity.GUARD_TRACKER_NAME, guardTrackerName);
        startActivity(intent);
    }

    /*
     * The iconRsc and titleRsc parameters are optional. The other parameters must be present.
     * Zero resource value means an invalid value.
     *
     */
    private void startGenericDialog(DialogGenericIcTtMs2Bt dialog, Bundle bundle, int iconRsc,
                                    int titleRsc, int msgRsc,
                                    int yesBtnRsc, int noBtnRsc,
                                    String tag) {
        FragmentManager manager = getSupportFragmentManager();
        if (iconRsc != 0)
            bundle.putInt(DialogGenericIcTtMs2Bt.ICON_ID_KEY, iconRsc);
        if (titleRsc != 0)
            bundle.putInt(DialogGenericIcTtMs2Bt.TITLE_ID_KEY, titleRsc);
        bundle.putInt(DialogGenericIcTtMs2Bt.MESSAGE_ID_KEY, msgRsc);
        bundle.putInt(DialogGenericIcTtMs2Bt.YES_BUTTON_LABEL_ID_KEY, yesBtnRsc);
        bundle.putInt(DialogGenericIcTtMs2Bt.NO_BUTTON_LABEL_ID_KEY, noBtnRsc);
        dialog.setArguments(bundle);
        dialog.show(manager, tag);
    }

    private void startConfirmationDialog() {
        DialogGenericIcTtMs2Bt dialogFragment = new DialogGenericIcTtMs2Bt();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogGenericIcTtMs2Bt.ICON_ID_KEY, android.R.drawable.ic_dialog_alert);
        bundle.putInt(DialogGenericIcTtMs2Bt.TITLE_ID_KEY, R.string.dialog_eliminate_device_title);
        bundle.putInt(DialogGenericIcTtMs2Bt.MESSAGE_ID_KEY, R.string.dialog_eliminate_device_message_body);
        bundle.putInt(DialogGenericIcTtMs2Bt.YES_BUTTON_LABEL_ID_KEY, android.R.string.yes);
        bundle.putInt(DialogGenericIcTtMs2Bt.NO_BUTTON_LABEL_ID_KEY, android.R.string.no);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, DialogFragmentTags.SelfDeviceEliminate.toString());
    }

    private void startNicknameDialog() {
        Bundle bundle = new Bundle();
        bundle.putString(NicknamePickerDialogFragment.NICKNAME_ORIGINAL_ID_KEY, mGuardTracker.getName());
        startGenericDialog(new NicknamePickerDialogFragment(), bundle, android.R.drawable.ic_menu_edit,
                R.string.dialog_nickname_title,
                R.string.dialog_nickname_message_body,
                R.string.done_button_label,
                R.string.cancel_button_label,
                DialogFragmentTags.ChangeDeviceNickname.toString());
    }

    private void startIdGsmAddrDialog() {
        startGenericDialog(new PhonePickerDialogFragment(), new Bundle(), android.R.drawable.ic_menu_edit,
                R.string.dialog_device_phone_number_title,
                R.string.dialog_id_gsm_addr_message_body,
                R.string.done_button_label,
                R.string.cancel_button_label,
                DialogFragmentTags.ChangeDevicePhoneNumber.toString());
    }

    private void startWakeSensorsEditDialog() {
        int bitmask = mGuardTracker.getWakeSensors();
        WakeSensorsConfigDialogFragment dialogFragment = new WakeSensorsConfigDialogFragment();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(WakeSensorsConfigDialogFragment.WAKE_SENSORS_BITMASK_ID_KEY, bitmask);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, DialogFragmentTags.WakeSensorsConfig.toString());
    }

    private void startWakeSensorsRestoreDialog() {
        startGenericDialog(new DialogGenericIcTtMs2Bt(), new Bundle(), android.R.drawable.ic_dialog_alert,
                R.string.dialog_wake_sensors_restore_title,
                R.string.dialog_wake_sensors_restore_message_body,
                android.R.string.yes,
                android.R.string.no,
                DialogFragmentTags.WakeSensorsRestore.toString());
    }

    private void startWakeSensorsViewSyncedDialog() {
        int bitmask = mGuardTracker.getNext().getWakeSensors();
        WakeSensorsBackupViewFragment dialogFragment = new WakeSensorsBackupViewFragment();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(WakeSensorsBackupViewFragment.WAKE_SENSORS_BITMASK_ID_KEY, bitmask);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, /*DialogFragmentTags.WakeSensorsConfig.toString()*/null);
    }

    private void startWakeSensorsSyncDialog() {
        startGenericDialog(new DialogGenericIcTtMs2Bt(), new Bundle(), android.R.drawable.ic_dialog_alert,
                R.string.dialog_wake_sensors_sync_title,
                R.string.dialog_wake_sensors_sync_message_body,
                android.R.string.yes,
                android.R.string.no,
                DialogFragmentTags.WakeSensorsSync.toString());
    }

    private void startWakeSensorsRefreshDialog() {
        startGenericDialog(new DialogGenericIcTtMs2Bt(), new Bundle(), android.R.drawable.ic_dialog_alert,
                R.string.dialog_wake_sensors_refresh_title,
                R.string.dialog_wake_sensors_refresh_message_body,
                android.R.string.yes,
                android.R.string.no,
                DialogFragmentTags.WakeSensorsRefresh.toString());
    }

    private void startMonCfgEditDialog() {
        Intent intent = new Intent(getBaseContext(), MonitoringCfgActivity.class);
        int monCfgId = mGuardTracker.getMonCfgId();
//        int guardTrackerId = mGuardTracker.get_id();
//        boolean createBackup = mGuardTracker.getNext() == null || mGuardTracker.getNext().getMonCfg().equals(mGuardTracker.getMonCfg());
        intent.putExtra(GuardTrackerActivity.CONFIG_ID, monCfgId);
//        intent.putExtra(GuardTrackerActivity.GUARD_TRACKER_ID, guardTrackerId);
//        intent.putExtra(GuardTrackerActivity.BACKUP_CREATE, createBackup);
        intent.putExtra(GuardTrackerActivity.GUARD_TRACKER_NAME, mGuardTracker.getName());
        startActivityForResult(intent, REQUEST_MON_CFG);
    }
    private void startMonCfgRestoreDialog() {
        startGenericDialog(new DialogGenericIcTtMs2Bt(), new Bundle(), android.R.drawable.ic_dialog_alert,
                R.string.dialog_mon_cfg_restore_title,
                R.string.dialog_mon_cfg_restore_message_body,
                android.R.string.yes,
                android.R.string.no,
                DialogFragmentTags.MonCfgRestore.toString());
    }
    private void startMonCfgViewSyncedDialog() {
        int monCfgId = mGuardTracker.getNext().getMonCfgId();
        MonitoringCfgBackupViewFragment dialogFragment = new MonitoringCfgBackupViewFragment();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(MonitoringCfgBackupViewFragment.MON_CFG_ID, monCfgId);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, /*DialogFragmentTags.WakeSensorsConfig.toString()*/null);
    }

    private void startMonCfgSyncDialog() {
        startGenericDialog(new DialogGenericIcTtMs2Bt(), new Bundle(), android.R.drawable.ic_dialog_alert,
                R.string.dialog_mon_cfg_sync_title,
                R.string.dialog_mon_cfg_sync_message_body,
                android.R.string.yes,
                android.R.string.no,
                DialogFragmentTags.MonCfgSync.toString());
    }

    private void startMonCfgRefreshDialog() {
        startGenericDialog(new DialogGenericIcTtMs2Bt(), new Bundle(), android.R.drawable.ic_dialog_alert,
                R.string.dialog_mon_cfg_refresh_title,
                R.string.dialog_mon_cfg_refresh_message_body,
                android.R.string.yes,
                android.R.string.no,
                DialogFragmentTags.MonCfgRefresh.toString());
    }

    private void startTrackCfgEditDialog() {
        Intent intent = new Intent(getBaseContext(), TrackingCfgActivity.class);
        int trackCfgId = mGuardTracker.getTrackCfgId();
//        int guardTrackerId = mGuardTracker.get_id();
//        boolean createBackup = mGuardTracker.getNext() == null || mGuardTracker.getNext().getMonCfg().equals(mGuardTracker.getMonCfg());
        intent.putExtra(GuardTrackerActivity.CONFIG_ID, trackCfgId);
//        intent.putExtra(GuardTrackerActivity.GUARD_TRACKER_ID, guardTrackerId);
//        intent.putExtra(GuardTrackerActivity.BACKUP_CREATE, createBackup);
        intent.putExtra(GuardTrackerActivity.GUARD_TRACKER_NAME, mGuardTracker.getName());
        startActivityForResult(intent, REQUEST_TRACK_CFG);
    }
    private void startTrackCfgRestoreDialog() {
        startGenericDialog(new DialogGenericIcTtMs2Bt(), new Bundle(), android.R.drawable.ic_dialog_alert,
                R.string.dialog_track_cfg_restore_title,
                R.string.dialog_track_cfg_restore_message_body,
                android.R.string.yes,
                android.R.string.no,
                DialogFragmentTags.TrackCfgRestore.toString());
    }
    private void startTrackCfgViewSyncedDialog() {
        int trackCfgId = mGuardTracker.getNext().getTrackCfgId();
        TrackingCfgBackupViewFragment dialogFragment = new TrackingCfgBackupViewFragment();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(TrackingCfgBackupViewFragment.TRACK_CFG_ID, trackCfgId);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, /*DialogFragmentTags.WakeSensorsConfig.toString()*/null);
    }

    private void startTrackCfgSyncDialog() {
        startGenericDialog(new DialogGenericIcTtMs2Bt(), new Bundle(), android.R.drawable.ic_dialog_alert,
                R.string.dialog_track_cfg_sync_title,
                R.string.dialog_track_cfg_sync_message_body,
                android.R.string.yes,
                android.R.string.no,
                DialogFragmentTags.TrackCfgSync.toString());
    }

    private void startTrackCfgRefreshDialog() {
        startGenericDialog(new DialogGenericIcTtMs2Bt(), new Bundle(), android.R.drawable.ic_dialog_alert,
                R.string.dialog_track_cfg_refresh_title,
                R.string.dialog_track_cfg_refresh_message_body,
                android.R.string.yes,
                android.R.string.no,
                DialogFragmentTags.TrackCfgRefresh.toString());
    }
    private void startVigCfgEditDialog() {
        Intent intent = new Intent(getBaseContext(), VigilanceCfgActivity.class);
        int vigCfgId = mGuardTracker.getVigCfgId();
//        int guardVigId = mGuardTracker.get_id();
//        boolean createBackup = mGuardTracker.getNext() == null || mGuardTracker.getNext().getVigCfg().equals(mGuardTracker.getVigCfg());
        intent.putExtra(GuardTrackerActivity.CONFIG_ID, vigCfgId);
//        intent.putExtra(GuardTrackerActivity.GUARD_TRACKER_ID, guardTrackerId);
//        intent.putExtra(GuardTrackerActivity.BACKUP_CREATE, createBackup);
        intent.putExtra(GuardTrackerActivity.GUARD_TRACKER_NAME, mGuardTracker.getName());
        startActivityForResult(intent, REQUEST_MON_CFG);
    }
    private void startVigCfgRestoreDialog() {
        startGenericDialog(new DialogGenericIcTtMs2Bt(), new Bundle(), android.R.drawable.ic_dialog_alert,
                R.string.dialog_vig_cfg_restore_title,
                R.string.dialog_vig_cfg_restore_message_body,
                android.R.string.yes,
                android.R.string.no,
                DialogFragmentTags.VigCfgRestore.toString());
    }
    private void startVigCfgViewSyncedDialog() {
        int vigCfgId = mGuardTracker.getNext().getVigCfgId();
        VigilanceCfgBackupViewFragment dialogFragment = new VigilanceCfgBackupViewFragment();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(VigilanceCfgBackupViewFragment.VIG_CFG_ID, vigCfgId);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, /*DialogFragmentTags.WakeSensorsConfig.toString()*/null);
    }

    private void startVigCfgSyncDialog() {
        startGenericDialog(new DialogGenericIcTtMs2Bt(), new Bundle(), android.R.drawable.ic_dialog_alert,
                R.string.dialog_vig_cfg_sync_title,
                R.string.dialog_vig_cfg_sync_message_body,
                android.R.string.yes,
                android.R.string.no,
                DialogFragmentTags.VigCfgSync.toString());
    }

    private void startVigCfgRefreshDialog() {
        startGenericDialog(new DialogGenericIcTtMs2Bt(), new Bundle(), android.R.drawable.ic_dialog_alert,
                R.string.dialog_vig_cfg_refresh_title,
                R.string.dialog_vig_cfg_refresh_message_body,
                android.R.string.yes,
                android.R.string.no,
                DialogFragmentTags.VigCfgRefresh.toString());
    }

    private void startAddSecondaryContactDialog() {
        PhonePickerDialogFragment dialogFragment = new PhonePickerDialogFragment();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogGenericIcTtMs2Bt.ICON_ID_KEY, android.R.drawable.ic_input_add);
        bundle.putInt(DialogGenericIcTtMs2Bt.TITLE_ID_KEY, R.string.dialog_add_secondary_contact_title);
        bundle.putInt(DialogGenericIcTtMs2Bt.MESSAGE_ID_KEY, R.string.dialog_add_secondary_contact_message_body);
        bundle.putInt(DialogGenericIcTtMs2Bt.YES_BUTTON_LABEL_ID_KEY, R.string.add_button_label);
        bundle.putInt(DialogGenericIcTtMs2Bt.NO_BUTTON_LABEL_ID_KEY, R.string.cancel_button_label);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, DialogFragmentTags.SecondaryContactAdd.toString());

    }

    private void startDeleteSecondaryContactDialog() {
        PhonePickerDialogFragment dialogFragment = new PhonePickerDialogFragment();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogGenericIcTtMs2Bt.ICON_ID_KEY, android.R.drawable.ic_input_delete);
        bundle.putInt(DialogGenericIcTtMs2Bt.TITLE_ID_KEY, R.string.dialog_delete_secondary_contact_title);
        bundle.putInt(DialogGenericIcTtMs2Bt.MESSAGE_ID_KEY, R.string.dialog_delete_secondary_contact_message_body);
        bundle.putInt(DialogGenericIcTtMs2Bt.YES_BUTTON_LABEL_ID_KEY, R.string.delete_button_label);
        bundle.putInt(DialogGenericIcTtMs2Bt.NO_BUTTON_LABEL_ID_KEY, R.string.cancel_button_label);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, DialogFragmentTags.SecondaryContactDelete.toString());
    }

    private void startOwnerRstDialog() {
        PhonePickerDialogFragment dialogFragment = new PhonePickerDialogFragment();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogGenericIcTtMs2Bt.ICON_ID_KEY, android.R.drawable.ic_dialog_alert);
        bundle.putInt(DialogGenericIcTtMs2Bt.TITLE_ID_KEY, R.string.dialog_change_owner_title);
        bundle.putInt(DialogGenericIcTtMs2Bt.MESSAGE_ID_KEY, R.string.dialog_change_owner_phone_message_body);
        bundle.putInt(DialogGenericIcTtMs2Bt.YES_BUTTON_LABEL_ID_KEY, R.string.done_button_label);
        bundle.putInt(DialogGenericIcTtMs2Bt.NO_BUTTON_LABEL_ID_KEY, R.string.cancel_button_label);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, DialogFragmentTags.ChangeOwner.toString());
    }

    private void startResetToFactoryDefaultsDialog() {
        DialogGenericIcTtMs2Bt dialogFragment = new DialogGenericIcTtMs2Bt();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogGenericIcTtMs2Bt.ICON_ID_KEY, android.R.drawable.ic_dialog_alert);
        bundle.putInt(DialogGenericIcTtMs2Bt.TITLE_ID_KEY, R.string.dialog_factory_defaults_title);
        bundle.putInt(DialogGenericIcTtMs2Bt.MESSAGE_ID_KEY, R.string.dialog_factory_defaults_message_body);
        bundle.putInt(DialogGenericIcTtMs2Bt.YES_BUTTON_LABEL_ID_KEY, android.R.string.yes);
        bundle.putInt(DialogGenericIcTtMs2Bt.NO_BUTTON_LABEL_ID_KEY, android.R.string.no);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, DialogFragmentTags.FactoryDefaultsReset.toString());
    }

    private void startResyncApp() {
        DialogGenericIcTtMs2Bt dialogFragment = new DialogGenericIcTtMs2Bt();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogGenericIcTtMs2Bt.ICON_ID_KEY, android.R.drawable.ic_dialog_alert);
        bundle.putInt(DialogGenericIcTtMs2Bt.TITLE_ID_KEY, R.string.dialog_resync_app_title);
        bundle.putInt(DialogGenericIcTtMs2Bt.MESSAGE_ID_KEY, R.string.dialog_resync_app_message_body);
        bundle.putInt(DialogGenericIcTtMs2Bt.YES_BUTTON_LABEL_ID_KEY, android.R.string.yes);
        bundle.putInt(DialogGenericIcTtMs2Bt.NO_BUTTON_LABEL_ID_KEY, android.R.string.no);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, DialogFragmentTags.AppResync.toString());
    }

    private void startGsmIdPhonePicker() {
        PhonePickerDialogFragment dialogFragment = new PhonePickerDialogFragment();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(PhonePickerDialogFragment.ICON_ID_KEY, R.drawable.ic_pair_new_link);
        bundle.putInt(PhonePickerDialogFragment.TITLE_ID_KEY, R.string.dialog_device_phone_number_title);
        bundle.putInt(PhonePickerDialogFragment.MESSAGE_ID_KEY, R.string.dialog_device_phone_number_message_body);
        bundle.putInt(PhonePickerDialogFragment.YES_BUTTON_LABEL_ID_KEY, R.string.done_button_label);
        bundle.putInt(PhonePickerDialogFragment.NO_BUTTON_LABEL_ID_KEY, R.string.cancel_button_label);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, DialogFragmentTags.DevicePhoneNumber.toString());
    }

    private void updateNicknameView() {
        String str = mGuardTracker.getName();
        if (str == null) {
            str = getString(R.string.nickname_empty_label);
        }
        setTitle(mGuardTracker.getName());
        mIdNameView.setText(str);
        mIdNameImgButton.setClickable(true);
    }

    private void updateIdGsmAddrView() {
        String str = mGuardTracker.getGsmId();
        if (str == null) {
            str = getString(R.string.id_gsm_addr_empty_label);
        }
        mIdGsmAddrView.setText(str);
        mIdGsmAddrImgButton.setClickable(true);
    }

    private void updateLastMonInfoView() {
        MonitoringInfo lastMonInfo = mGuardTracker.getLastMonInfo();
        if (lastMonInfo != null) {
            mLastMonInfoDatetimeView.setText(lastMonInfo.getPrettyDate());
            mLastMonInfoTimeView.setText(lastMonInfo.getPosition().getPrettyTime());
            mLastMonInfoLatView.setText(lastMonInfo.getPosition().getPrettyLatitude());
            mLastMonInfoLngView.setText(lastMonInfo.getPosition().getPrettyLongitude());
            mLastMonInfoAltView.setText(lastMonInfo.getPosition().getPrettyAltitude());
            mLastMonInfoSatView.setText(lastMonInfo.getPosition().getPrettySatellites());
            mLastMonInfoHdopView.setText(lastMonInfo.getPosition().getPrettyHdop());
            mLastMonInfoFixedView.setText(lastMonInfo.getPosition().getPrettyFixed());
            mLastMonInfoTempView.setText(lastMonInfo.getPrettyTemperature());
            mLastMonInfoSimView.setText(lastMonInfo.getPrettyBalance());
            mLastMonInfoBatView.setText(lastMonInfo.getPrettyCharge());
        }
        mLastMonInfoGoogleMapsButton.setVisibility(lastMonInfo != null ? View.VISIBLE : View.INVISIBLE);
    }

    private void updateReferencePositionView() {
        Position refPos = mGuardTracker.getPosRef();
        if (refPos != null) {
            mPosRefDatetimeView.setText(refPos.getPrettyTime());
            mPosRefLatView.setText(refPos.getPrettyLatitude());
            mPosRefLngView.setText(refPos.getPrettyLongitude());
            mPosRefAltView.setText(refPos.getPrettyAltitude());
            mPosRefSatView.setText(refPos.getPrettySatellites());
            mPosRefHdopView.setText(refPos.getPrettyHdop());
            mPosRefFixedView.setText(refPos.getPrettyFixed());
        }
        mPosRefGoogleMapsButton.setVisibility(refPos != null ? View.VISIBLE : View.INVISIBLE);
    }

    private void updateBleStatusView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBleStatusValueView.setText(mBleState.toString());
                mBleStatusValueView.setBackgroundColor(mBleState.toBackgroundColor());
                mBleStatusValueView.setTextColor(mBleState.toForegroundColor());
                mBleStatusCommandsView.setVisibility(
                        mBleStatusCommandsButton.getText().equals(getText(R.string.ble_status_view_messages_button_label)) ?
                                View.GONE :
                                View.VISIBLE
                );
            }
        });
    }
    private void updateWakeSensorsView() {
        final int wakeSensors = mGuardTracker.getWakeSensors();
        ArrayAdapter adapter = (ArrayAdapter)mSensorsConfigSpinner.getAdapter();
        if (mGuardTracker.getNext() != null &&
                mGuardTracker.getWakeSensors() != mGuardTracker.getNext().getWakeSensors()) {
            if (mSensorsConfigSpinner.getCount() == 2) {
                // Remove/disable spinner config Restore and View Synced items.
                // IN THE FUTURE, TURN DISABLE/ENABLE ITEMS
                // Not so easy as remove the view. It seems the ArrayAdapter class should be extended
                // and some methods must be override
                adapter.add(getResources().getString(R.string.restore_synced));
                adapter.add(getResources().getString(R.string.view_synced));
            }
        } else if (mSensorsConfigSpinner.getCount() > 2) {
            adapter.remove(getResources().getString(R.string.restore_synced));
            adapter.remove(getResources().getString(R.string.view_synced));
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSensorsSyncSpinner.setEnabled(isBleConnectionValid());

                ImageView accImageView = findViewById(R.id.sensors_acc_img_view);
                accImageView.setColorFilter((wakeSensors & WakeSensorsConfigDialogFragment.BITMASK_ACC) != 0 ? 0xFF0000 : 0x00FF00);
                ImageView rtcImageView = findViewById(R.id.sensors_rtc_img_view);
                rtcImageView.setColorFilter((wakeSensors & WakeSensorsConfigDialogFragment.BITMASK_RTC) != 0 ? 0xFF0000 : 0x00FF00);
                ImageView bleImageView = findViewById(R.id.sensors_ble_img_view);
                bleImageView.setColorFilter((wakeSensors & WakeSensorsConfigDialogFragment.BITMASK_BLE) != 0 ? 0xFF0000 : 0x00FF00);

                mSensorsConfigBleView.setText((wakeSensors & WakeSensorsConfigDialogFragment.BITMASK_BLE) != 0 ? getText(R.string.enable) : getText(R.string.disable));
                mSensorsConfigRtcView.setText((wakeSensors & WakeSensorsConfigDialogFragment.BITMASK_RTC) != 0 ? getText(R.string.enable) : getText(R.string.disable));
                mSensorsConfigAccView.setText((wakeSensors & WakeSensorsConfigDialogFragment.BITMASK_ACC) != 0 ? getText(R.string.enable) : getText(R.string.disable));
                mSensorsSyncedStateView.setVisibility (
                        mGuardTracker.getNext() != null && mGuardTracker.getSync() == false && mGuardTracker.getNext().getWakeSensors() != wakeSensors ?
                                View.VISIBLE :
                                View.GONE
                );

            }
        });
    }

    private void updateMonCfgView() {
        final String date;
        MonitoringConfiguration monCfg = mGuardTracker.getMonCfg();
        int periodMin = monCfg.getPeriodMin();
        int modMonCfg = monCfg.getTimeMod();
        MonitoringInfo tmpLastMonInfo = mGuardTracker.getLastMonInfo();
        if (tmpLastMonInfo != null) {
            long dateInMillis = tmpLastMonInfo.getDate().getTime();
            dateInMillis += (periodMin * 60 * 1000);
            date = String.format("%1$td/%1$tm/%1$tY %1$tR", dateInMillis); // 1$ = index og argument
        } else {
            Calendar now = Calendar.getInstance();
            if (periodMin >= 24 * 60) {
                int nowHour = now.get(Calendar.HOUR_OF_DAY);
                int nowMinute = now.get(Calendar.MINUTE);
                long nowInMillis = now.getTimeInMillis();
                now.set(Calendar.HOUR_OF_DAY, modMonCfg / 60); // Adjust to alarm time (hours)
                now.set(Calendar.MINUTE, modMonCfg % 60);      // Adjust to alarm time (minutes)
                long dateInMillis = now.getTimeInMillis();
                if (nowInMillis > dateInMillis) {
                    dateInMillis += 24 * 60 * 60 * 1000; // Add 1 day to wake alert time
                }
                date = String.format("%1$td/%1$tm/%1$tY %1$tR", dateInMillis); // 1$ = index og argument
            } else {
                int nextHour = periodMin / 60;
                int nextMinute = periodMin % 60;
                date = nextHour == 0 ?
                        String.format(getString(R.string.mon_cfg_next_wake_in_minutes), nextMinute) :
                        String.format(getString(R.string.mon_cfg_next_wake_in_hours_minutes), nextHour, nextMinute);
            }
        }
        ArrayAdapter configAdapter = (ArrayAdapter) mMonCfgConfigSpinner.getAdapter();
        if (mGuardTracker.getNext() != null &&
                mGuardTracker.getMonCfg().equals(mGuardTracker.getNext().getMonCfg()) == false) {
            if (mMonCfgConfigSpinner.getCount() == 2) {
                // Remove/disable spinner config Restore and View Synced items.
                // IN THE FUTURE, TURN DISABLE/ENABLE ITEMS
                // Not so easy as remove the view. It seems the ArrayAdapter class should be extended
                // and some methods must be override
                configAdapter.add(getResources().getString(R.string.restore_synced));
                configAdapter.add(getResources().getString(R.string.view_synced));
            }
        } else if (mMonCfgConfigSpinner.getCount() > 2) {
            configAdapter.remove(getResources().getString(R.string.restore_synced));
            configAdapter.remove(getResources().getString(R.string.view_synced));
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMonCfgSyncSpinner.setEnabled(isBleConnectionValid());
                mMonCfgDatetimeView.setText(date);
                mMonCfgSyncedStateView.setVisibility (
                        mGuardTracker.getNext() != null &&
                                mGuardTracker.getSync() == false &&
                                mGuardTracker.getMonCfg().equals(mGuardTracker.getNext().getMonCfg()) == false?
                                View.VISIBLE : View.GONE
                );

            }
        });
    }
    private void updateTrackCfgView() {
        ArrayAdapter configAdapter = (ArrayAdapter) mTrackCfgConfigSpinner.getAdapter();
        if (mGuardTracker.getNext() != null &&
                mGuardTracker.getTrackCfg().equals(mGuardTracker.getNext().getTrackCfg()) == false) {
            if (mTrackCfgConfigSpinner.getCount() == 2) {
                // Remove/disable spinner config Restore and View Synced items.
                // IN THE FUTURE, TURN DISABLE/ENABLE ITEMS
                // Not so easy as remove the view. It seems the ArrayAdapter class should be extended
                // and some methods must be override
                configAdapter.add(getResources().getString(R.string.restore_synced));
                configAdapter.add(getResources().getString(R.string.view_synced));
            }
        } else if (mTrackCfgConfigSpinner.getCount() > 2) {
            configAdapter.remove(getResources().getString(R.string.restore_synced));
            configAdapter.remove(getResources().getString(R.string.view_synced));
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTrackCfgSyncSpinner.setEnabled(isBleConnectionValid());
                mTrackCfgSyncedStateView.setVisibility (
                        mGuardTracker.getNext() != null &&
                                mGuardTracker.getSync() == false &&
                                mGuardTracker.getTrackCfg().equals(mGuardTracker.getNext().getTrackCfg()) == false?
                                View.VISIBLE : View.GONE
                );

            }
        });
    }
    private void updateVigCfgView() {
        ArrayAdapter configAdapter = (ArrayAdapter) mVigCfgConfigSpinner.getAdapter();
        if (mGuardTracker.getNext() != null &&
                mGuardTracker.getVigCfg().equals(mGuardTracker.getNext().getVigCfg()) == false) {
            if (mVigCfgConfigSpinner.getCount() == 2) {
                // Remove/disable spinner config Restore and View Synced items.
                // IN THE FUTURE, TURN DISABLE/ENABLE ITEMS
                // Not so easy as remove the view. It seems the ArrayAdapter class should be extended
                // and some methods must be override
                configAdapter.add(getResources().getString(R.string.restore_synced));
                configAdapter.add(getResources().getString(R.string.view_synced));
            }
        } else if (mVigCfgConfigSpinner.getCount() > 2) {
            configAdapter.remove(getResources().getString(R.string.restore_synced));
            configAdapter.remove(getResources().getString(R.string.view_synced));
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mVigCfgSyncSpinner.setEnabled(isBleConnectionValid());
                mVigCfgSyncedStateView.setVisibility (
                        mGuardTracker.getNext() != null &&
                                mGuardTracker.getSync() == false &&
                                mGuardTracker.getVigCfg().equals(mGuardTracker.getNext().getVigCfg()) == false?
                                View.VISIBLE : View.GONE
                );

            }
        });
    }

    private void updateSecondaryContactsView() {
        mContactsSecondaryEmptyView.setVisibility(
                mContactsSecondaryList.size() == 0 ?
                        View.VISIBLE :
                        View.GONE
        );
        mContactsSecondaryViewGroup.removeAllViews();
        for (int i = 0; i < mContactsSecondaryList.size(); i++) {
            View view = getLayoutInflater().inflate(R.layout.list_item_label_value_text, null);
            System.out.println(view);
            TextView label = view.findViewById(R.id.text1);
            label.setText("Contact " + (i + 1) + ":");
            TextView value = view.findViewById(R.id.text2);
            value.setText(mContactsSecondaryList.get(i));
            mContactsSecondaryViewGroup.addView(view);
        }
        if (mBleState == BleStateEnum.Connected)
            mContactsSecondaryDelButton.setVisibility(mContactsSecondaryList.size() > 0 ?
                    View.VISIBLE : View.GONE);
    }

    private void updateOwnerPhoneNumberView() {
        mContactsOwnerView.setText(mGuardTracker.getOwnerPhoneNumber());
    }

    private void bleStatusCommandsViewAppendMessage(String preamble, byte [] message) {
        StringBuilder sb = new StringBuilder(preamble).append(preamble.equals("Tx") ? " > " : " < ");
        for (int i = 0; i < message.length; i++) {
            sb.append(String.format("%02h ", message[i]));
        }
        sb.append('\n');
        mBleStatusCommandsView.append(sb);
    }

    private void onClickChangeDeviceNickname(String nickname) {
        // Update model
        mGuardTracker.setName(nickname);
        // Update database
        mGuardTracker.update(getBaseContext());
        // Update view
        updateNicknameView();
        Toast.makeText(getBaseContext(), R.string.dialog_nickname_successful, Toast.LENGTH_LONG).show();
    }

    private void onClickChangeDevicePhoneNumber(String phoneNumber) {
        // Update model
        mGuardTracker.setGsmId(phoneNumber);
        // Update database
        mGuardTracker.update(getBaseContext());
        // Update view
        updateIdGsmAddrView();
        Toast.makeText(getBaseContext(), R.string.dialog_toast_ok, Toast.LENGTH_LONG).show();
    }

    private void onClickPosRefRefreshPos() {
        byte[] cmd = GuardTrackerCommands.readRefPos();
        mBleConnControl.sendCommand(cmd, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msg) {
                // Dump received message
                bleStatusCommandsViewAppendMessage("Rx", msg);

                // Test if answer belongs with this command
                byte ordinal = (byte) GuardTrackerCommands.CommandValues.READ_REF_POS.ordinal();
                if (msg[0] != ordinal) {
//                    Toast.makeText(getBaseContext(),
//                            "BLE answer (" + msg[0] + ") don't belong to cmd READ_REF_POS (" + ordinal + ")",
//                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (msg[1] != GuardTrackerCommands.CmdResValues.CMD_RES_OK.value()) {
                    Toast.makeText(GuardTrackerActivity.this.getBaseContext(),
                            R.string.toast_pos_ref_unsuccessful_read,
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // Parse ble message args
                final int porRefLen = 18;
                byte [] posRefData = new byte[porRefLen];
                System.arraycopy(msg, 2, posRefData, 0, porRefLen);
                Position newRefPos = new Position(posRefData);
                if (mGuardTracker.getPosRef() != null) {
                    // Update old reference position.
                    int id = mGuardTracker.getPosRefId();
                    newRefPos.set_id(id);
//                    newRefPos.update(getBaseContext());
//                    mGuardTracker.setPosRef(newRefPos);
                } else {
                    // Create new reference position.
                    newRefPos = new Position(newRefPos.getLatitude(), newRefPos.getLongitude(), newRefPos.getTime(), newRefPos.getAltitude(),
                            newRefPos.getSatellites(), newRefPos.getHdop(), newRefPos.getFixed());
                    newRefPos.create(GuardTrackerActivity.this.getBaseContext());
//                    mGuardTracker.setPosRef(newRefPos);
//                    mGuardTracker.update(getBaseContext());
                }
                mGuardTracker.setPosRef(newRefPos); // Tirei dos scopes anteriores e coloquei aqui. Esta linha e os dois updates à base de dados a seguir.
                // Update database
                newRefPos.update(getBaseContext());
                mGuardTracker.update(getBaseContext());
                // Update view;
                updateReferencePositionView();
                Toast.makeText(getBaseContext(), R.string.refresh_pos_ref_successful, Toast.LENGTH_LONG).show();
            }

//            @Override
//            public void onMessageSent(byte[] msgSent) {
//
//            }
        });
        // Dump transmitted message
        bleStatusCommandsViewAppendMessage("Tx", cmd);
    }

    private class PendingNewRefPosMessageListener implements BleMessageListener {

        @Override
        public void onMessageReceived(byte[] msgRecv) {
            // Dump received message
            bleStatusCommandsViewAppendMessage("Rx", msgRecv);

            // Test if answer belongs with this command
            byte ordinal = (byte) GuardTrackerCommands.CommandValues.RST_REF_POS.ordinal();
            if (msgRecv[0] != ordinal) {
//                Toast.makeText(getBaseContext(),
//                        "BLE answer (" + msgRecv[0] + ") don't belong to cmd RST_REF_POS (" + ordinal + ")",
//                        Toast.LENGTH_LONG).show();
                return;
            }

            if (msgRecv[1] == GuardTrackerCommands.CmdResValues.CMD_RES_PEND.value()) {
                return;
            }
            // Remove listener
            mBleConnControl.removeMessageListener(this);
            mPendRefPosMessageListener = null;
            refPosButtonVisibilityUpdate(false);

            if (msgRecv[1] == GuardTrackerCommands.CmdResValues.CMD_RES_KO.value()) {
                Toast.makeText(getBaseContext(),
                        R.string.toast_pos_ref_unsuccessful_write,
                        Toast.LENGTH_LONG).show();
                return;
            }
            // else CMD_RES_OK
            Toast.makeText(getBaseContext(), R.string.toast_pos_ref_successful, Toast.LENGTH_LONG).show();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onClickPosRefRefreshPos();
                }
            }, 200);
        }
    }

    private PendingNewRefPosMessageListener mPendRefPosMessageListener;

    private void onClickPosRefNewReferencePos() {
        byte[] cmd = GuardTrackerCommands.resetRefPos();
        mBleConnControl.sendCommand(cmd, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msg) {
                // Dump received message
                bleStatusCommandsViewAppendMessage("Rx", msg);

                // Test if answer belongs with this command
                byte ordinal = (byte) GuardTrackerCommands.CommandValues.RST_REF_POS.ordinal();
                if (msg[0] != ordinal) {
//                    Toast.makeText(getBaseContext(),
//                            "BLE answer (" + msg[0] + ") don't belong to cmd RST_REF_POS (" + ordinal + ")",
//                            Toast.LENGTH_LONG).show();
                    return;
                }

                // Remove this object as listener from messageListener
                // If it does not exists, it doesn't throw an exception
                //mBleConnControl.removeMessageListener(this);

                if (msg[1] == GuardTrackerCommands.CmdResValues.CMD_RES_PEND.value()) {
                    mPendRefPosMessageListener = new PendingNewRefPosMessageListener();
                    mBleConnControl.addMessageListener(mPendRefPosMessageListener);
                    refPosButtonVisibilityUpdate(true);
                    Toast.makeText(getBaseContext(),
                            R.string.toast_pos_ref_pending,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                if (msg[1] == GuardTrackerCommands.CmdResValues.CMD_RES_KO.value()) {
                    Toast.makeText(getBaseContext(),
                            R.string.toast_pos_ref_unsuccessful_write,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                // else CMD_RES_OK
                Toast.makeText(getBaseContext(), R.string.toast_pos_ref_successful, Toast.LENGTH_LONG).show();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onClickPosRefRefreshPos();
                    }
                }, 200);
            }

//            @Override
//            public void onMessageSent(byte[] msgSent) {
//
//            }
        });

        // Dump transmitted message
        bleStatusCommandsViewAppendMessage("Tx", cmd);
    }

    private void refPosButtonVisibilityUpdate(boolean pending) {
        mPosRefNewRefButton.setVisibility(pending ? View.GONE : View.VISIBLE);
        mPosRefCancelNewRefButton.setVisibility(pending ? View.VISIBLE : View.GONE);
        mPosRefPendingButton.setVisibility(pending ? View.VISIBLE : View.GONE);
        mPosRefRefreshButton.setVisibility(View.VISIBLE);
    }
    private void onClickPosRefCancelNewReferencePos() {
        byte[] cmd = GuardTrackerCommands.cancelResetRefPos((byte) GuardTrackerCommands.CommandValues.RST_REF_POS.ordinal());
        mBleConnControl.sendCommand(cmd, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msg) {
                // Dump received message
                bleStatusCommandsViewAppendMessage("Rx", msg);

                // Test if answer belongs with this command
                byte stopPendingOrdinal = (byte) GuardTrackerCommands.CommandValues.STOP_PENDING_CMD.ordinal();
                byte newPosRefCmdOrdinal = (byte) GuardTrackerCommands.CommandValues.RST_REF_POS.ordinal();
                if (msg[0] != stopPendingOrdinal || msg[2] != newPosRefCmdOrdinal) {
//                    Toast.makeText(getBaseContext(),
//                            "BLE answer (" + msg[0] + ") don't belong to cmd STOP_PENDING_CMD (" + stopPendingOrdinal + ")",
//                            Toast.LENGTH_LONG).show();
                    return;
                }

                // Remove this object as listener from messageListener
                // If it does not exists, it doesn't throw an exception
                mBleConnControl.removeMessageListener(mPendRefPosMessageListener);

                Toast.makeText(getBaseContext(),
                        msg[1] == GuardTrackerCommands.CmdResValues.CMD_RES_KO.value()
                                ? R.string.toast_pos_ref_cancel_unsuccessful
                                : R.string.toast_pos_ref_cancel_successful, Toast.LENGTH_LONG).show();
                refPosButtonVisibilityUpdate(false);
            }
        });

        // Dump transmitted message
        bleStatusCommandsViewAppendMessage("Tx", cmd);
    }


    /**
     * Create backup of GuardTracker.
     * This method does not update current GuardTracker in database, only the backup.
     */
    private void createBackup() {
        // Clone GuardTracker
        // In the domain of this application, there is only one backup per GuardTracker,
        // so sync and next fields are always true and null, respectively.
        // Cloning GuardTracker with getSync and getNext returning values instead of true and null literals,
        // enables an hypothetical solution with a chain of backups.
        GuardTracker backup = new GuardTracker(
                mGuardTracker.getName(),
                mGuardTracker.getBleId(),
                mGuardTracker.getGsmId(),
                mGuardTracker.getOwnerPhoneNumber(),
                mGuardTracker.getWakeSensors(),
                mGuardTracker.getPosRef(),
                mGuardTracker.getLastMonInfo(),
                mGuardTracker.getMonCfg(),
                mGuardTracker.getTrackCfg(),
                mGuardTracker.getVigCfg(),
                mGuardTracker.getSync(), mGuardTracker.getNext()
        );
        // Create backup in database
        backup.create(getBaseContext());
        // Set backup in GuardTracker
        mGuardTracker.setNext(backup);
        // There is no need to update GuardTracker database here because it will be updated above.
    }


    ///
    /// Wake Sensors onClick button events

    private void onClickWakeSensorsSync() {
        byte[] cmd = GuardTrackerCommands.setWakeSensors((short) mGuardTracker.getWakeSensors());
        mBleConnControl.sendCommand(cmd, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msg) {
                // Dump received message
                bleStatusCommandsViewAppendMessage("Rx", msg);

                // Test if answer belongs with this command
                byte ordinal = (byte) GuardTrackerCommands.CommandValues.WAKE_SENSORS.ordinal();
                if (msg[0] != ordinal) {
                    Log.w(TAG, "Command id ("+ msg[0] + ") does not corresponds to Wake sensors ordinal id (" + ordinal + ")");
                    return;
                }

                if (msg[1] == GuardTrackerCommands.CmdResValues.CMD_RES_KO.value()) {
                    Toast.makeText(GuardTrackerActivity.this.getBaseContext(),
                            R.string.dialog_wake_sensors_message_unsuccessful_write,
                            Toast.LENGTH_LONG).show();
                    return;
                }

                GuardTracker backup = mGuardTracker.getNext();
                // Delete backup if there aren't any more differences.
                if (backup != null) {
                    if (backup.equals(mGuardTracker)) {
                        // Perform a flat delete (not a deep delete)
                        backup.delete(getBaseContext());
                        mGuardTracker.setNext(null);
                        mGuardTracker.update(getBaseContext());
                    } else {
                        // Else update backup entry
                        backup.setWakeSensors(mGuardTracker.getWakeSensors());
                        backup.update(getBaseContext());
                    }
                }
                // Update view
                updateWakeSensorsView();
                Toast.makeText(getBaseContext(), R.string.dialog_wake_sensors_message_successful, Toast.LENGTH_LONG).show();
            }
        });

        // Dump transmitted message
        bleStatusCommandsViewAppendMessage("Tx", cmd);
    }
    private void onClickWakeSensorsRestore() {

        mGuardTracker.setWakeSensors(mGuardTracker.getNext().getWakeSensors());

        // Delete backup if there aren't any more differences.
        if (mGuardTracker.getNext().equals(mGuardTracker)) {
            // Perform a flat delete (not a deep delete)
            mGuardTracker.getNext().delete(getBaseContext());
            mGuardTracker.setNext(null);
            // Not doing GuardTracker update here because it will be done always above.
        }
        // Update database
        mGuardTracker.update(getBaseContext());
        // Update view
        updateWakeSensorsView();

        Toast.makeText(getBaseContext(), R.string.dialog_wake_sensors_message_successful, Toast.LENGTH_LONG).show();

    }
//    private void onClickWakeSensorsViewSynced() {
//
//    }
    private void onClickWakeSensorsRefresh() {
        byte[] cmd = GuardTrackerCommands.readWakeSensors();
        mBleConnControl.sendCommand(cmd, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msg) {
                // Dump received message
                bleStatusCommandsViewAppendMessage("Rx", msg);

                // Test if answer belongs with this command
                byte ordinal = (byte) GuardTrackerCommands.CommandValues.READ_WAKE_SENSORS_STATE.ordinal();
                if (msg[0] != ordinal) {
                    Log.w(TAG, "Command id ("+ msg[0] + ") does not corresponds to Wake sensors ordinal id (" + ordinal + ")");
                    return;
                }

                if (msg[1] == GuardTrackerCommands.CmdResValues.CMD_RES_KO.value()) {
                    Toast.makeText(GuardTrackerActivity.this.getBaseContext(),
                            R.string.dialog_wake_sensors_message_unsuccessful_read,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                // Parse ble message args
                short bitmask = (short) ((msg[2] << 8) | (msg[3] << 0));
                mGuardTracker.setWakeSensors(bitmask & 0xFFFF);

                GuardTracker backup = mGuardTracker.getNext();
                if (backup != null) {
                    // Delete backup if there aren't any more differences.
                    if (backup.equals(mGuardTracker)) {
                        // Perform a flat delete (not a deep delete)
                        backup.delete(getBaseContext());
                        mGuardTracker.setNext(null);
                        // Not doing GuardTracker update here because it will be done always above.
                    }
                } else {
                    // Else update backup entry
                    backup.setWakeSensors(mGuardTracker.getWakeSensors());
                    backup.update(getBaseContext());
                }
                // Update database
                mGuardTracker.update(getBaseContext());
                // Update view
                updateWakeSensorsView();
                Toast.makeText(getBaseContext(), R.string.dialog_wake_sensors_message_successful, Toast.LENGTH_LONG).show();
            }
        });

        // Dump transmitted message
        bleStatusCommandsViewAppendMessage("Tx", cmd);
    }

    private void onClickWakeSensorsConfig(int bitmask) {
        // Do not do anything if the new wake sensors value has the same value that current wake sensors value.
        if (bitmask == mGuardTracker.getWakeSensors()) {
            Toast.makeText(getBaseContext(), R.string.nothing_to_do, Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a backup only if there are no backup created
        if (mGuardTracker.getNext() == null) {
            createBackup();
        }

        mGuardTracker.setWakeSensors(bitmask & 0xFFFF);
        // New wake sensors value may be equals to original value.
        if (mGuardTracker.getWakeSensors() == mGuardTracker.getNext().getWakeSensors()) {
            // Delete backup if there aren't any more differences.
            if (mGuardTracker.getNext().equals(mGuardTracker)) {
                // Perform a flat delete (not a deep delete)
                mGuardTracker.getNext().delete(getBaseContext());
                mGuardTracker.setNext(null);
                // Not doing GuardTracker update here because it will be done always above.
            }
        }
        // Update database
        mGuardTracker.update(getBaseContext());
        // Update view
        updateWakeSensorsView();
        Toast.makeText(getBaseContext(), R.string.dialog_wake_sensors_message_successful, Toast.LENGTH_LONG).show();

    }


    ///
    /// Mon configuration onClick button events

    private void onClickMonCfgSync() {
        SyncConfigWorkflow monCfgSync = new SyncConfigWorkflow(mBleConnControl, new SyncConfigCmdProcessedListener() {
            @Override
            public void onCommandProcessed(GuardTrackerCommands.CommandValues cmdValue, GuardTrackerCommands.CmdResValues res) {
                // Dump received message
                bleStatusCommandsViewAppendMessage("Rx", new byte[] { (byte)cmdValue.value(), (byte)res.value() });

                Log.i(GuardTrackerActivity.TAG, "Cmd id: " + cmdValue.toString() + ", Res: " + res.toString());
            }
        }, new SyncConfigFinishListener() {
            @Override
            public void onFinishSyncConfig() {
                MonitoringConfiguration monCfg = mGuardTracker.getMonCfg();
                GuardTracker backup = mGuardTracker.getNext();
                // Two different entries in Monitoring Configuration table may have different states.
                // In that case, one of the entries must be deleted.
                // But, both mGuardTracker and respective backup may refer to the same entry in Monitoring Configuration table.
                // In that case, the entry must not be deleted.
                if (backup != null) {
                    //if (mGuardTracker.getMonCfgId() != backup.getMonCfgId()) {
                    if (monCfg.equals(backup.getMonCfg()) == false) {
                        // Delete mon cfg backup from database. Backup and current config values are equals.
                        backup.getMonCfg().delete(getBaseContext());
                        backup.setMonCfg(monCfg);
                        // Delete backup if there aren't any more differences.
                        if (backup.equals(mGuardTracker)) {
                            // Perform a flat delete (not a deep delete)
                            backup.delete(getBaseContext());
                            mGuardTracker.setNext(null);
                            // Not doing GuardTracker update here because it will be done always above.
                        } else
                            backup.update(getBaseContext());

                        // Update database
                        mGuardTracker.update(getBaseContext());
                        // Update view
                        updateMonCfgView();
                    }
                }
                Toast.makeText(getBaseContext(), R.string.dialog_dev_message_successful, Toast.LENGTH_LONG).show();
            }
        });
        MonitoringConfiguration monCfg = mGuardTracker.getMonCfg();
        monCfgSync.addCommand(GuardTrackerCommands.writeMonitoringConfig(GuardTrackerCommands.MonCfgItems.MON_CFG_TIME, monCfg.getTimeMod()))
                .addCommand(GuardTrackerCommands.writeMonitoringConfig(GuardTrackerCommands.MonCfgItems.MON_CFG_PERIOD, monCfg.getPeriodMin()))
                .addCommand(GuardTrackerCommands.writeMonitoringConfig(GuardTrackerCommands.MonCfgItems.MON_CFG_SMS_CRITERIA, monCfg.getSmsCriteria().ordinal()))
                .addCommand(GuardTrackerCommands.writeMonitoringConfig(GuardTrackerCommands.MonCfgItems.MON_CFG_GPS_THRESHOLD, monCfg.getGpsThresholdMeters(),
                        MonitoringConfiguration.getRawGpsThresholdLat(monCfg.getGpsThresholdMeters()),
                        MonitoringConfiguration.getRawGpsThresholdLon(monCfg.getGpsThresholdMeters())))
                .addCommand(GuardTrackerCommands.writeMonitoringConfig(GuardTrackerCommands.MonCfgItems.MON_CFG_GPS_FOV, monCfg.getGpsFov()))
                .addCommand(GuardTrackerCommands.writeMonitoringConfig(GuardTrackerCommands.MonCfgItems.MON_CFG_GPS_TIMEOUT, monCfg.getGpsTimeout()))
                .addCommand(GuardTrackerCommands.writeMonitoringConfig(GuardTrackerCommands.MonCfgItems.MON_CFG_TEMP,
                        MonitoringConfiguration.getRawTemp(monCfg.getTempHigh()),
                        MonitoringConfiguration.getRawTemp(monCfg.getTempLow())))
                .addCommand(GuardTrackerCommands.writeMonitoringConfig(GuardTrackerCommands.MonCfgItems.MON_CFG_SIM_BALANCE,
                        MonitoringConfiguration.getRawSimBalance(monCfg.getSimBalanceThreshold())))
                .startSync();


    }
    private void onClickMonCfgRestore() {

        // Delete monitoring config backup database entry.
        GuardTracker backup = mGuardTracker.getNext();
        MonitoringConfiguration monCfgBackup = backup.getMonCfg();
        mGuardTracker.getMonCfg().delete(getBaseContext());
        mGuardTracker.setMonCfg(monCfgBackup);

        // Delete backup if there aren't any more differences.
        if (backup.equals(mGuardTracker)) {
            // Perform a flat delete (not a deep delete)
            backup.delete(getBaseContext());
            mGuardTracker.setNext(null);
            // Not doing GuardTracker update here because it will be done always above.
        }
        // Update database
        mGuardTracker.update(getBaseContext());
        // Update view
        updateMonCfgView();
        Toast.makeText(getBaseContext(), R.string.dialog_db_message_successful, Toast.LENGTH_LONG).show();

    }
//    private void onClickMonCfgViewSynced() {
//
//    }
    private void onClickMonCfgRefresh() {
        byte[] cmd = GuardTrackerCommands.readMonitoringConfig();
        mBleConnControl.sendCommand(cmd, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msg) {
                // Dump received message
                bleStatusCommandsViewAppendMessage("Rx", msg);

                // Test if answer belongs with this command
                byte ordinal = (byte) GuardTrackerCommands.CommandValues.READ_MONITORING_CFG.ordinal();
                if (msg[0] != ordinal) {
                    Log.w(TAG, "Command id ("+ msg[0] + ") does not corresponds to monitoring configuration ordinal id (" + ordinal + ")");
                    return;
                }

                if (msg[1] == GuardTrackerCommands.CmdResValues.CMD_RES_KO.value()) {
                    Toast.makeText(GuardTrackerActivity.this.getBaseContext(),
                            R.string.dialog_db_message_unsuccessful,
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // Parse ble message into temporally monCfg object.
                MonitoringConfiguration devMonCfg = new MonitoringConfiguration();
                int cfgLen = 19;
                byte [] cfgData = new byte[cfgLen];
                System.arraycopy(msg, 2, cfgData, 0, cfgLen);
                // Decode received message and build object.
                devMonCfg.parse(cfgData);

                GuardTracker backup = mGuardTracker.getNext();
                if (backup == null) {
                    devMonCfg.set_id(mGuardTracker.getMonCfg().get_id());
                    mGuardTracker.setMonCfg(devMonCfg);
                } else {
                    MonitoringConfiguration backupMonCfg = backup.getMonCfg();
                    MonitoringConfiguration monCfg = mGuardTracker.getMonCfg();
                    devMonCfg.set_id(backupMonCfg.get_id());
                    backup.setMonCfg(devMonCfg);
                    mGuardTracker.setMonCfg(devMonCfg);
                    // Different actions based on same monitoring configuration or not.
                    if (backupMonCfg.equals(monCfg) == false) {
                        monCfg.delete(getBaseContext());
                        // Delete backup if there aren't any more differences.
                        if (backup.equals(mGuardTracker)) {
                            // Perform a flat delete (not a deep delete)
                            backup.delete(getBaseContext());
                            mGuardTracker.setNext(null);
                            // Not doing GuardTracker update here because it will be done always above.
                        } else
                            backup.update(getBaseContext());
                    }
                }
                mGuardTracker.getMonCfg().update(getBaseContext());
                // Update database
                mGuardTracker.update(getBaseContext());
                // Update view
                updateMonCfgView();
                Toast.makeText(getBaseContext(), R.string.dialog_db_message_successful, Toast.LENGTH_LONG).show();
            }
        });

        // Dump transmitted message
        bleStatusCommandsViewAppendMessage("Tx", cmd);
    }

    private void onClickMonCfgEdit(int newMonId) {
        MonitoringConfiguration newMonCfg = MonitoringConfiguration.read(getBaseContext(), newMonId);

        // Create backup if not backed up.
        if (mGuardTracker.getNext() == null) {
            // Create backup of GuardTracker
            createBackup();
        }
        GuardTracker backup = mGuardTracker.getNext();
        MonitoringConfiguration backupMonCfg = backup.getMonCfg();
        MonitoringConfiguration monCfg = mGuardTracker.getMonCfg();
        // Create new backup of monitoring configuration in both no backup and same backup scenarios.
        if (backupMonCfg.equals(monCfg)) {
            // mGuardTracker and backup has same monCfg.
            // It includes the no backup scenario because in that scenario, mGuardTracker has a backup
            // created above with same monitoring configuration
            mGuardTracker.setMonCfg(newMonCfg);
        } else {
            // mGuardTracker and backup has different monCfg.
            // Delete actual monitoring configuration entry
            monCfg.delete(getBaseContext());
            if (newMonCfg.equals(backupMonCfg)) {
                // new configurations match last synced values
                newMonCfg.delete(getBaseContext());
                // set actual monitoring configuration to last synced values.
                mGuardTracker.setMonCfg(backupMonCfg);
                // Delete backup if there aren't any more differences.
                if (mGuardTracker.equals(backup)) {
//                    deleteBackup();
                    // Perform a flat delete (not a deep delete)
                    backup.delete(getBaseContext());
                    mGuardTracker.setNext(null);
                    // Not doing GuardTracker update here because it will be done always above.
                }
            } else {
                // new configurations does not match synced values, so substitutes with new one.
                mGuardTracker.setMonCfg(newMonCfg);
            }
        }
        // Update database
        mGuardTracker.update(getBaseContext());
        // Update view
        updateMonCfgView();
        Toast.makeText(getBaseContext(), R.string.dialog_db_message_successful, Toast.LENGTH_LONG).show();
    }

    ///
    /// Track configuration onClick button events

    private void onClickTrackCfgSync() {
        SyncConfigWorkflow trackCfgSync = new SyncConfigWorkflow(mBleConnControl, new SyncConfigCmdProcessedListener() {
            @Override
            public void onCommandProcessed(GuardTrackerCommands.CommandValues cmdValue, GuardTrackerCommands.CmdResValues res) {
                // Dump received message
                bleStatusCommandsViewAppendMessage("Rx", new byte[] { (byte)cmdValue.value(), (byte)res.value() });

                Log.i(GuardTrackerActivity.TAG, "Cmd id: " + cmdValue.toString() + ", Res: " + res.toString());
            }
        }, new SyncConfigFinishListener() {
            @Override
            public void onFinishSyncConfig() {
                TrackingConfiguration trackCfg = mGuardTracker.getTrackCfg();
                GuardTracker backup = mGuardTracker.getNext();
                // Two different entries in Monitoring Configuration table may have different states.
                // In that case, one of the entries must be deleted.
                // But, both mGuardTracker and respective backup may refer to the same entry in Monitoring Configuration table.
                // In that case, the entry must not be deleted.
                if (backup != null) {
                    //if (mGuardTracker.getTrackCfgId() != backup.getTrackCfgId()) {
                    if (trackCfg.equals(backup.getTrackCfg()) == false) {
                        // Delete Track cfg backup from database. Backup and current config values are equals.
                        backup.getTrackCfg().delete(getBaseContext());
                        backup.setTrackCfg(trackCfg);
                        // Delete backup if there aren't any more differences.
                        if (backup.equals(mGuardTracker)) {
                            // Perform a flat delete (not a deep delete)
                            backup.delete(getBaseContext());
                            mGuardTracker.setNext(null);
                            // Not doing GuardTracker update here because it will be done always above.
                        } else
                            backup.update(getBaseContext());

                        // Update database
                        mGuardTracker.update(getBaseContext());
                        // Update view
                        updateTrackCfgView();
                    }
                }
                Toast.makeText(getBaseContext(), R.string.dialog_dev_message_successful, Toast.LENGTH_LONG).show();
            }
        });
        TrackingConfiguration trackCfg = mGuardTracker.getTrackCfg();
        trackCfgSync.addCommand(GuardTrackerCommands.writeTrackingConfig(GuardTrackerCommands.TrackCfgItems.TRACK_CFG_SMS_CRITERIA, trackCfg.getSmsCriteria().ordinal()))
                .addCommand(GuardTrackerCommands.writeTrackingConfig(GuardTrackerCommands.TrackCfgItems.TRACK_CFG_GPS_THRESHOLD, trackCfg.getGpsThreshold(),
                        MonitoringConfiguration.getRawGpsThresholdLat(trackCfg.getGpsThreshold()), // getRawGPS... is a static method
                        MonitoringConfiguration.getRawGpsThresholdLon(trackCfg.getGpsThreshold())))
                .addCommand(GuardTrackerCommands.writeTrackingConfig(GuardTrackerCommands.TrackCfgItems.TRACK_CFG_GPS_FOV, trackCfg.getGpsFov()))
                .addCommand(GuardTrackerCommands.writeTrackingConfig(GuardTrackerCommands.TrackCfgItems.TRACK_CFG_GPS_TIMEOUT, trackCfg.getGpsTimeout()))
                .addCommand(GuardTrackerCommands.writeTrackingConfig(GuardTrackerCommands.TrackCfgItems.TRACK_CFG_TIMEOUT_POST, trackCfg.getTimeoutPost()))
                .addCommand(GuardTrackerCommands.writeTrackingConfig(GuardTrackerCommands.TrackCfgItems.TRACK_CFG_TIMEOUT_PRE, trackCfg.getTimeoutPre()))
                .addCommand(GuardTrackerCommands.writeTrackingConfig(GuardTrackerCommands.TrackCfgItems.TRACK_CFG_TIMEOUT_TRACKING, trackCfg.getTimeoutTracking()))
                .startSync();


    }
    private void onClickTrackCfgRestore() {

        // Delete monitoring config backup database entry.
        GuardTracker backup = mGuardTracker.getNext();
        TrackingConfiguration trackCfgBackup = backup.getTrackCfg();
        mGuardTracker.getTrackCfg().delete(getBaseContext());
        mGuardTracker.setTrackCfg(trackCfgBackup);

        // Delete backup if there aren't any more differences.
        if (backup.equals(mGuardTracker)) {
            // Perform a flat delete (not a deep delete)
            backup.delete(getBaseContext());
            mGuardTracker.setNext(null);
            // Not doing GuardTracker update here because it will be done always above.
        }
        // Update database
        mGuardTracker.update(getBaseContext());
        // Update view
        updateTrackCfgView();
        Toast.makeText(getBaseContext(), R.string.dialog_db_message_successful, Toast.LENGTH_LONG).show();

    }
    //    private void onClickMonCfgViewSynced() {
//
//    }
    private void onClickTrackCfgRefresh() {
        byte[] cmd = GuardTrackerCommands.readTrackingConfig();
        mBleConnControl.sendCommand(cmd, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msg) {
                // Dump received message
                bleStatusCommandsViewAppendMessage("Rx", msg);

                // Test if answer belongs with this command
                byte ordinal = (byte) GuardTrackerCommands.CommandValues.READ_TRACKING_CFG.ordinal();
                if (msg[0] != ordinal) {
                    Log.w(TAG, "Command id ("+ msg[0] + ") does not corresponds to monitoring configuration ordinal id (" + ordinal + ")");
                    return;
                }

                if (msg[1] == GuardTrackerCommands.CmdResValues.CMD_RES_KO.value()) {
                    Toast.makeText(GuardTrackerActivity.this.getBaseContext(),
                            R.string.dialog_db_message_unsuccessful,
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // Parse ble message into temporally trackCfg object.
                TrackingConfiguration devTrackCfg = new TrackingConfiguration();
                int cfgLen = 19;
                byte [] cfgData = new byte[cfgLen];
                System.arraycopy(msg, 2, cfgData, 0, cfgLen);
                // Decode received message and build object.
                devTrackCfg.parse(cfgData);

                GuardTracker backup = mGuardTracker.getNext();
                if (backup == null) {
                    devTrackCfg.set_id(mGuardTracker.getTrackCfg().get_id());
                    mGuardTracker.setTrackCfg(devTrackCfg);
                } else {
                    TrackingConfiguration backupTrackCfg = backup.getTrackCfg();
                    TrackingConfiguration trackCfg = mGuardTracker.getTrackCfg();
                    devTrackCfg.set_id(backupTrackCfg.get_id());
                    backup.setTrackCfg(devTrackCfg);
                    mGuardTracker.setTrackCfg(devTrackCfg);
                    // Different actions based on same tracking configuration or not.
                    if (backupTrackCfg.equals(trackCfg) == false) {
                        trackCfg.delete(getBaseContext());
                        // Delete backup if there aren't any more differences.
                        if (backup.equals(mGuardTracker)) {
                            // Perform a flat delete (not a deep delete)
                            backup.delete(getBaseContext());
                            mGuardTracker.setNext(null);
                            // Not doing GuardTracker update here because it will be done always above.
                        } else
                            backup.update(getBaseContext());
                    }
                }
                mGuardTracker.getTrackCfg().update(getBaseContext());
                // Update database
                mGuardTracker.update(getBaseContext());
                // Update view
                updateTrackCfgView();
                Toast.makeText(getBaseContext(), R.string.dialog_db_message_successful, Toast.LENGTH_LONG).show();
            }
        });

        // Dump transmitted message
        bleStatusCommandsViewAppendMessage("Tx", cmd);
    }

    private void onClickTrackCfgEdit(int newTrackId) {
        TrackingConfiguration newTrackCfg = TrackingConfiguration.read(getBaseContext(), newTrackId);

        // Create backup if not backed up.
        if (mGuardTracker.getNext() == null) {
            // Create backup of GuardTracker
            createBackup();
        }
        GuardTracker backup = mGuardTracker.getNext();
        TrackingConfiguration backupTrackCfg = backup.getTrackCfg();
        TrackingConfiguration trackCfg = mGuardTracker.getTrackCfg();
        // Create new backup of tracking configuration in both no backup and same backup scenarios.
        if (backupTrackCfg.equals(trackCfg)) {
            // mGuardTracker and backup has same trackCfg.
            // It includes the no backup scenario because in that scenario, mGuardTracker has a backup
            // created above with same monitoring configuration
            mGuardTracker.setTrackCfg(newTrackCfg);
        } else {
            // mGuardTracker and backup has different trackCfg.
            // Delete actual tracking configuration entry
            trackCfg.delete(getBaseContext());
            if (newTrackCfg.equals(backupTrackCfg)) {
                // new configurations match last synced values
                newTrackCfg.delete(getBaseContext());
                // set actual tracking configuration to last synced values.
                mGuardTracker.setTrackCfg(backupTrackCfg);
                // Delete backup if there aren't any more differences.
                if (mGuardTracker.equals(backup)) {
//                    deleteBackup();
                    // Perform a flat delete (not a deep delete)
                    backup.delete(getBaseContext());
                    mGuardTracker.setNext(null);
                    // Not doing GuardTracker update here because it will be done always above.
                }
            } else {
                // new configurations does not match synced values, so substitutes with new one.
                mGuardTracker.setTrackCfg(newTrackCfg);
            }
        }
        // Update database
        mGuardTracker.update(getBaseContext());
        // Update view
        updateTrackCfgView();
        Toast.makeText(getBaseContext(), R.string.dialog_db_message_successful, Toast.LENGTH_LONG).show();
    }


    ///
    /// Vig configuration onClick button events
    private void onClickVigCfgSync() {
        SyncConfigWorkflow vigCfgSync = new SyncConfigWorkflow(mBleConnControl, new SyncConfigCmdProcessedListener() {
            @Override
            public void onCommandProcessed(GuardTrackerCommands.CommandValues cmdValue, GuardTrackerCommands.CmdResValues res) {
                // Dump received message
                bleStatusCommandsViewAppendMessage("Rx", new byte[] { (byte)cmdValue.value(), (byte)res.value() });

                Log.i(GuardTrackerActivity.TAG, "Cmd id: " + cmdValue.toString() + ", Res: " + res.toString());
            }
        }, new SyncConfigFinishListener() {
            @Override
            public void onFinishSyncConfig() {
                VigilanceConfiguration vigCfg = mGuardTracker.getVigCfg();
                GuardTracker backup = mGuardTracker.getNext();
                // Two different entries in Vigilance Configuration table may have different states.
                // In that case, one of the entries must be deleted.
                // But, both mGuardTracker and respective backup may refer to the same entry in Vigilance Configuration table.
                // In that case, the entry must not be deleted.
                if (backup != null) {
                    //if (mGuardTracker.getVigCfgId() != backup.getVigCfgId()) {
                    if (vigCfg.equals(backup.getVigCfg()) == false) {
                        // Delete vigilance cfg backup from database. Backup and current config values are equals.
                        backup.getVigCfg().delete(getBaseContext());
                        backup.setVigilanceCfg(vigCfg);
                        // Delete backup if there aren't any more differences.
                        if (backup.equals(mGuardTracker)) {
                            // Perform a flat delete (not a deep delete)
                            backup.delete(getBaseContext());
                            mGuardTracker.setNext(null);
                            // Not doing GuardTracker update here because it will be done always above.
                        } else
                            backup.update(getBaseContext());

                        // Update database
                        mGuardTracker.update(getBaseContext());
                        // Update view
                        updateVigCfgView();
                    }
                }
                Toast.makeText(getBaseContext(), R.string.dialog_dev_message_successful, Toast.LENGTH_LONG).show();
            }
        });
        VigilanceConfiguration vigCfg = mGuardTracker.getVigCfg();
        vigCfgSync.addCommand(GuardTrackerCommands.writeVigilanceConfig(GuardTrackerCommands.VigCfgItems.VIG_CFG_TILT_LEVEL, vigCfg.getTiltLevel()))
                .addCommand(GuardTrackerCommands.writeVigilanceConfig(GuardTrackerCommands.VigCfgItems.VIG_CFG_BLE_ADVERTISEMENT_PERIOD, vigCfg.getBleAdvertisePeriod()))
                .startSync();
    }
    private void onClickVigCfgRestore() {

        // Delete vigilance config backup database entry.
        GuardTracker backup = mGuardTracker.getNext();
        VigilanceConfiguration vigCfgBackup = backup.getVigCfg();
        mGuardTracker.getVigCfg().delete(getBaseContext());
        mGuardTracker.setVigilanceCfg(vigCfgBackup);

        // Delete backup if there aren't any more differences.
        if (backup.equals(mGuardTracker)) {
            // Perform a flat delete (not a deep delete)
            backup.delete(getBaseContext());
            mGuardTracker.setNext(null);
            // Not doing GuardTracker update here because it will be done always above.
        }
        // Update database
        mGuardTracker.update(getBaseContext());
        // Update view
        updateVigCfgView();
        Toast.makeText(getBaseContext(), R.string.dialog_db_message_successful, Toast.LENGTH_LONG).show();

    }
    //    private void onClickVigCfgViewSynced() {
//
//    }
    private void onClickVigCfgRefresh() {
        byte[] cmd = GuardTrackerCommands.readVigilanceConfig();
        mBleConnControl.sendCommand(cmd, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msg) {
                // Dump received message
                bleStatusCommandsViewAppendMessage("Rx", msg);

                // Test if answer belongs with this command
                byte ordinal = (byte) GuardTrackerCommands.CommandValues.READ_VIGILANCE_CFG.ordinal();
                if (msg[0] != ordinal) {
                    Log.w(TAG, "Command id ("+ msg[0] + ") does not corresponds to vigilance configuration ordinal id (" + ordinal + ")");
                    return;
                }

                if (msg[1] == GuardTrackerCommands.CmdResValues.CMD_RES_KO.value()) {
                    Toast.makeText(GuardTrackerActivity.this.getBaseContext(),
                            R.string.dialog_db_message_unsuccessful,
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // Parse ble message into temporally vigCfg object.
                VigilanceConfiguration devVigCfg = new VigilanceConfiguration();
                int cfgLen = 19;
                byte [] cfgData = new byte[cfgLen];
                System.arraycopy(msg, 2, cfgData, 0, cfgLen);
                // Decode received message and build object.
                devVigCfg.parse(cfgData);

                GuardTracker backup = mGuardTracker.getNext();
                if (backup == null) {
                    devVigCfg.set_id(mGuardTracker.getMonCfg().get_id());
                    mGuardTracker.setVigilanceCfg(devVigCfg);
                } else {
                    VigilanceConfiguration backupVigCfg = backup.getVigCfg();
                    VigilanceConfiguration vigCfg = mGuardTracker.getVigCfg();
                    devVigCfg.set_id(backupVigCfg.get_id());
                    backup.setVigilanceCfg(devVigCfg);
                    mGuardTracker.setVigilanceCfg(devVigCfg);
                    // Different actions based on same vigilance configuration or not.
                    if (backupVigCfg.equals(vigCfg) == false) {
                        vigCfg.delete(getBaseContext());
                        // Delete backup if there aren't any more differences.
                        if (backup.equals(mGuardTracker)) {
                            // Perform a flat delete (not a deep delete)
                            backup.delete(getBaseContext());
                            mGuardTracker.setNext(null);
                            // Not doing GuardTracker update here because it will be done always above.
                        } else
                            backup.update(getBaseContext());
                    }
                }
                mGuardTracker.getVigCfg().update(getBaseContext());
                // Update database
                mGuardTracker.update(getBaseContext());
                // Update view
                updateVigCfgView();
                Toast.makeText(getBaseContext(), R.string.dialog_db_message_successful, Toast.LENGTH_LONG).show();
            }
        });

        // Dump transmitted message
        bleStatusCommandsViewAppendMessage("Tx", cmd);
    }

    private void onClickVigCfgEdit(int newVigId) {
        VigilanceConfiguration newVigCfg = VigilanceConfiguration.read(getBaseContext(), newVigId);

        // Create backup if not backed up.
        if (mGuardTracker.getNext() == null) {
            // Create backup of GuardTracker
            createBackup();
        }
        GuardTracker backup = mGuardTracker.getNext();
        VigilanceConfiguration backupVigCfg = backup.getVigCfg();
        VigilanceConfiguration vigCfg = mGuardTracker.getVigCfg();
        // Create new backup of vigilance configuration in both no backup and same backup scenarios.
        if (backupVigCfg.equals(vigCfg)) {
            // mGuardTracker and backup has same vigCfg.
            // It includes the no backup scenario because in that scenario, mGuardTracker has a backup
            // created above with same vigilance configuration
            mGuardTracker.setVigilanceCfg(newVigCfg);
        } else {
            // mGuardTracker and backup has different vigCfg.
            // Delete actual vigilance configuration entry
            vigCfg.delete(getBaseContext());
            if (newVigCfg.equals(backupVigCfg)) {
                // new configurations match last synced values
                newVigCfg.delete(getBaseContext());
                // set actual vigilance configuration to last synced values.
                mGuardTracker.setVigilanceCfg(backupVigCfg);
                // Delete backup if there aren't any more differences.
                if (mGuardTracker.equals(backup)) {
//                    deleteBackup();
                    // Perform a flat delete (not a deep delete)
                    backup.delete(getBaseContext());
                    mGuardTracker.setNext(null);
                    // Not doing GuardTracker update here because it will be done always above.
                }
            } else {
                // new configurations does not match synced values, so substitutes with new one.
                mGuardTracker.setVigilanceCfg(newVigCfg);
            }
        }
        // Update database
        mGuardTracker.update(getBaseContext());
        // Update view
        updateVigCfgView();
        Toast.makeText(getBaseContext(), R.string.dialog_db_message_successful, Toast.LENGTH_LONG).show();
    }

    private void onClickOwnerRefresh() {
        byte[] cmd = GuardTrackerCommands.readOwner();
        mBleConnControl.sendCommand(cmd, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msgRecv) {
                // Dump received message
                bleStatusCommandsViewAppendMessage("Rx", msgRecv);

                // Test if answer belongs with this command
                byte ordinal = (byte) GuardTrackerCommands.CommandValues.READ_OWNER.ordinal();
                if (msgRecv[0] != ordinal) {
//                    Toast.makeText(getBaseContext(),
//                            "BLE answer (" + msgRecv[0] + ") don't belong to cmd READ_OWNER (" + ordinal + ")",
//                            Toast.LENGTH_LONG).show();
                    return;
                }

                String ownerPhoneNumber = "";
                if (msgRecv[1] == GuardTrackerCommands.CmdResValues.CMD_RES_KO.value()) {
                    Toast.makeText(GuardTrackerActivity.this.getBaseContext(),
                            R.string.refresh_owner_unsuccessful,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                // Parse ble message
                String phoneNumberRaw = new String(msgRecv, 3, msgRecv[2]); // Não TESTEIi
                //String phoneNumberRaw = String(msgRecv);
                // Decode received message and build object.
                try {
                    ownerPhoneNumber = MyPhoneUtils.formatE164(phoneNumberRaw);
                    // Update database
                    mGuardTracker.setOwnerPhoneNumber(ownerPhoneNumber);
                    mGuardTracker.update(getBaseContext());
                    // Update view
                    updateOwnerPhoneNumberView();
                    Toast.makeText(getBaseContext(), R.string.refresh_owner_successful, Toast.LENGTH_LONG).show();
                } catch (NumberParseException e) {
                    Log.e(TAG, e.getErrorType().toString() + ": " + e.getMessage());
                    Toast.makeText(GuardTrackerActivity.this.getBaseContext(),
                            R.string.refresh_owner_unsuccessful,
                            Toast.LENGTH_LONG).show();
                }
            }

//            @Override
//            public void onMessageSent(byte[] msgSent) {
//
//            }
        });

        // Dump transmitted message
        bleStatusCommandsViewAppendMessage("Tx", cmd);
    }

    private void onClickRstOwner(final String phoneNumber) {
        // Send command to change Owner with a new one
        byte[] cmd = GuardTrackerCommands.changeOwner(phoneNumber);
        mBleConnControl.sendCommand(cmd, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msgRecv) {
                // Dump received message
                bleStatusCommandsViewAppendMessage("Rx", msgRecv);

                // Test if answer belongs with this command
                byte ordinal = (byte) GuardTrackerCommands.CommandValues.RST_OWNER.ordinal();
                if (msgRecv[0] != ordinal) {
//                    Toast.makeText(getBaseContext(),
//                            "BLE answer (" + msgRecv[0] + ") don't belong to cmd RST_OWNER (" + ordinal + ")",
//                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (msgRecv[1] == GuardTrackerCommands.CmdResValues.CMD_RES_KO.value()) {
                    Toast.makeText(GuardTrackerActivity.this.getBaseContext(),
                            R.string.dialog_change_owner_unsuccessful_write,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(getBaseContext(), R.string.dialog_change_owner_successful, Toast.LENGTH_LONG).show();
                // Refresh database with data read from device
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onClickOwnerRefresh();
                    }
                }, 200);
//                // Update model
//                mGuardTracker.setOwnerPhoneNumber(phoneNumber);
//                mGuardTracker.update(getBaseContext());
//                // Update view
//                updateOwnerPhoneNumberView();
            }

//            @Override
//            public void onMessageSent(byte[] msgSent) {
//
//            }
        });

        // Dump transmitted message
        bleStatusCommandsViewAppendMessage("Tx", cmd);
    }

    class ReadSecondaryContactsMessageListener implements BleMessageListener {
        @Override
        public void onMessageReceived(byte[] msgRecv) {
            // Dump received message
            bleStatusCommandsViewAppendMessage("Rx", msgRecv);

            // Test if answer belongs with this command
            byte ordinal = (byte) GuardTrackerCommands.CommandValues.READ_SECONDARY_CONTACT.ordinal();
            if (msgRecv[0] != ordinal) {
//                Toast.makeText(getBaseContext(),
//                        "BLE answer (" + msgRecv[0] + ") don't belong to cmd READ_SECONDARY_CONTACT (" + ordinal + ")",
//                        Toast.LENGTH_LONG).show();
                return;
            }

            byte value = (byte) GuardTrackerCommands.CmdResValues.CMD_RES_OK.value();
            if (msgRecv[1] == value) { // Continue reading secondary contacts
                // Decode received message and build object.
                String phoneNumber = new String(msgRecv, 4, msgRecv[3]);
                // Update database and model
                SecondaryContactsDbHelper.create(getBaseContext(), mGuardTracker.get_id(), phoneNumber);
                mGuardTracker.addSecondaryContact(phoneNumber);
                // Send next command
                final int nextContact = msgRecv[2] + 1;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        byte[] nextCmd = GuardTrackerCommands.readSecondaryContact(nextContact);
                        mBleConnControl.sendCommand(nextCmd, ReadSecondaryContactsMessageListener.this);
                    }
                }, 200);
            } else {
                // There are no more secondary contacts
                // Update view
                updateSecondaryContactsView();
                Toast.makeText(getBaseContext(), R.string.refresh_secondary_contacts_successful, Toast.LENGTH_LONG).show();
            }
        }

//        @Override
//        public void onMessageSent(byte[] msgSent) {
//
//        }
    }

    private void onClickSecondaryContactsRefresh() {
        // Delete local database A ELIMINAçÃO DA BASE DE DADOS Só DEVIA OCORRER DEPOIS DE RECEBER UMA RESPOSTA COM SUCESSO. PENSAR NISTO. POR OUTRO LADO, mesmo não eliminando, PODERÀ SEMPRE FICAR INCONSISTENTE (o device com a base de dados).
        SecondaryContactsDbHelper.delete(getBaseContext(), mGuardTracker.get_id());
        mGuardTracker.clearSecondaryContacts();

        // Send command to read first secondary contact
        byte[] cmd = GuardTrackerCommands.readSecondaryContact(1);
        mBleConnControl.sendCommand(cmd, new ReadSecondaryContactsMessageListener());

        // Dump transmitted message
        bleStatusCommandsViewAppendMessage("Tx", cmd);
    }

    private void onClickAddSecondaryContact(final String phoneNumber) {
        // Send command to add secondary contact
        byte[] cmd = GuardTrackerCommands.addContact(phoneNumber);
        mBleConnControl.sendCommand(cmd, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msg) {
                // Dump received message
                bleStatusCommandsViewAppendMessage("Rx", msg);

                // Test if answer belongs with this command
                byte ordinal = (byte) GuardTrackerCommands.CommandValues.ADD_SECONDARY_CONTACT.ordinal();
                if (msg[0] != ordinal) {
//                    Toast.makeText(getBaseContext(),
//                            "BLE answer (" + msg[0] + ") don't belong to cmd ADD_SECONDARY_CONTACT (" + ordinal + ")",
//                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (msg[1] == GuardTrackerCommands.CmdResValues.CMD_RES_KO.value()) {
                    Toast.makeText(GuardTrackerActivity.this.getBaseContext(),
                            R.string.dialog_add_secondary_contact_unsuccessful_write,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                // DEVIA SEGUIR O MODELO ANTERIOR E COLOCAR UM COMANDO PARA REFRESCAR O CONTACTO COMO ORDINAL cmd[1].
                // Update model
                SecondaryContactsDbHelper.create(getBaseContext(), mGuardTracker.get_id(), phoneNumber);
                mGuardTracker.addSecondaryContact(phoneNumber);
                // Update view
                updateSecondaryContactsView();
                Toast.makeText(getBaseContext(), R.string.dialog_add_secondary_contact_successful, Toast.LENGTH_LONG).show();
            }

//            @Override
//            public void onMessageSent(byte[] msgSent) {
//
//            }
        });

        // Dump transmitted message
        bleStatusCommandsViewAppendMessage("Tx", cmd);
    }

    private void onClickDelSecondaryContact(final String phoneNumber) {
        // Send command to deleteDeep secondary contact
        byte[] cmd = GuardTrackerCommands.deleteContact(phoneNumber);
        mBleConnControl.sendCommand(cmd, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msg) {
                // Dump received message
                bleStatusCommandsViewAppendMessage("Rx", msg);

                // Test if answer belongs with this command
                byte ordinal = (byte) GuardTrackerCommands.CommandValues.DELETE_SECONDARY_CONTACT.ordinal();
                if (msg[0] != ordinal) {
//                    Toast.makeText(getBaseContext(),
//                            "BLE answer (" + msg[0] + ") don't belong to cmd DELETE_SECONDARY_CONTACT (" + ordinal + ")",
//                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (msg[1] == GuardTrackerCommands.CmdResValues.CMD_RES_KO.value()) {
                    Toast.makeText(GuardTrackerActivity.this.getBaseContext(),
                            R.string.dialog_delete_secondary_contact_unsuccessful_write,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                // DEVIA SEGUIR O MODELO ANTERIOR E COLOCAR UM COMANDO PARA REFRESCAR O CONTACTO COMO ORDINAL cmd[1].
                // Update model
                SecondaryContactsDbHelper.delete(getBaseContext(), mGuardTracker.get_id(), phoneNumber);
                mGuardTracker.removeSecondaryContact(phoneNumber);
                // Update view
                updateSecondaryContactsView();
                Toast.makeText(getBaseContext(), R.string.dialog_delete_secondary_contact_successful, Toast.LENGTH_LONG).show();
            }

//            @Override
//            public void onMessageSent(byte[] msgSent) {
//
//            }
        });

        // Dump transmitted message
        bleStatusCommandsViewAppendMessage("Tx", cmd);
    }

    private void onClickResyncAppWithDevice() {
        mTempGuardTracker = new GuardTracker();
        mSyncConfigWorkflow = new SyncConfigWorkflow(mBleConnControl, new SyncConfigFromDevListener() {
            @Override
            public void onMonitoringConfigReceived(MonitoringConfiguration monCfg) {
                mTempGuardTracker.setMonCfg(monCfg);
            }

            @Override
            public void onTrackingConfigReceived(TrackingConfiguration trackCfg) {
                mTempGuardTracker.setTrackCfg(trackCfg);
            }

            @Override
            public void onVigilanceConfigReceived(VigilanceConfiguration vigilanceCfg) {
                mTempGuardTracker.setVigilanceCfg(vigilanceCfg);
            }

            @Override
            public void onDevicePhoneNumberReceived(String devicePhoneNumber) {
                if (devicePhoneNumber.length() == 0) {
                    startGsmIdPhonePicker();
                    return;
                }
                mTempGuardTracker.setGsmId(devicePhoneNumber);
            }

            @Override
            public void onPositionReferenceReceived(Position posRef) {
                if (posRef != null)
                    mTempGuardTracker.setPosRef(posRef);
            }

            @Override
            public void onWakeSensorsReceived(int wakeSensorsCfgStatus) {
                mTempGuardTracker.setWakeSensors(wakeSensorsCfgStatus);
            }

            @Override
            public void onSecondaryContactReceived(String secondaryPhoneNumber) {
                mTempGuardTracker.addSecondaryContact(secondaryPhoneNumber);
            }

        }, new SyncConfigFinishListener() {
            @Override
            public void onFinishSyncConfig() {
                // Delete local configurations before update
                int id;
                id = mGuardTracker.getMonCfgId();
                MonitoringConfiguration.delete(getBaseContext(), id);
                id = mGuardTracker.getTrackCfgId();
                TrackingConfiguration.delete(getBaseContext(), id);
                id = mGuardTracker.getVigCfgId();
                VigilanceConfiguration.delete(getBaseContext(), id);
                id = mGuardTracker.get_id();
                SecondaryContactsDbHelper.delete(getBaseContext(), id);
                id = mGuardTracker.getPosRefId();
                Position.delete(getBaseContext(), id);

                // Create new configuration entries in database
                MonitoringConfiguration monCfg = mTempGuardTracker.getMonCfg();
                monCfg.create(GuardTrackerActivity.this.getBaseContext());
                TrackingConfiguration trackCfg = mTempGuardTracker.getTrackCfg();
                trackCfg.create(GuardTrackerActivity.this.getBaseContext());
                VigilanceConfiguration vigilanceCfg = mTempGuardTracker.getVigCfg();
                vigilanceCfg.create(GuardTrackerActivity.this.getBaseContext());
                Position posRef = mTempGuardTracker.getPosRef();
                if (posRef != null)
                    posRef.create(GuardTrackerActivity.this.getBaseContext());
                List<String> secondaryContacts = mTempGuardTracker.getSecondaryContacts();
                for (String contact : secondaryContacts)
                    SecondaryContactsDbHelper.create(GuardTrackerActivity.this.getBaseContext(), mGuardTracker.get_id(), contact);
                // Update model
                mGuardTracker.copyConfigs(mTempGuardTracker);
                // Update GuardTracker
                mGuardTracker.update(GuardTrackerActivity.this.getBaseContext());
                Toast.makeText(getBaseContext(), R.string.dialog_resync_app_successful, Toast.LENGTH_SHORT).show();
                GuardTrackerActivity.this.recreate();
            }
        });

        mSyncConfigWorkflow.startCompleteSyncFromDev();
    }

    private void onClickResetFactoryDefaults() {
        // Send command to reset configurations to factory defaults
        byte[] cmd = GuardTrackerCommands.cleanE2prom();
        mBleConnControl.sendCommand(cmd, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msg) {
                // Dump received message
                bleStatusCommandsViewAppendMessage("Rx", msg);

                // Test if answer belongs with this command
                byte ordinal = (byte) GuardTrackerCommands.CommandValues.CLEAN_E2PROM.ordinal();
                if (msg[0] != ordinal) {
//                    Toast.makeText(getBaseContext(),
//                            "BLE answer (" + msg[0] + ") don't belong to cmd CLEAN_E2PROM (" + ordinal + ")",
//                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (msg[1] == (byte) GuardTrackerCommands.CmdResValues.CMD_RES_KO.value()) {
                    Toast.makeText(GuardTrackerActivity.this.getBaseContext(),
                            R.string.dialog_factory_defaults_unsuccessful_write,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                // Update database
                int id = mGuardTrackerId;
                boolean deviceEliminated = GuardTracker.deleteDeep(getBaseContext(), id);
                Toast.makeText(getBaseContext(), deviceEliminated ?
                                R.string.dialog_factory_defaults_successful :
                                R.string.dialog_factory_defaults_unsuccessful_read,
                        Toast.LENGTH_LONG).show();

                GuardTrackerActivity.this.finish();
            }

//            @Override
//            public void onMessageSent(byte[] msgSent) {
//
//            }
        });

        // Dump transmitted message
        bleStatusCommandsViewAppendMessage("Tx", cmd);
    }

    private class MySpinnerAdapter<T> extends ArrayAdapter<T> {

        public MySpinnerAdapter(Context context, int layoutRes, T [] array) {
            super(context, layoutRes, array);
        }
        public MySpinnerAdapter(Context context, int layoutRes, List<T> list) {
            super(context, layoutRes, list);
        }

        @Override
        public boolean isEnabled(int position){
            // Disable the first item from Spinner
            // First item will be use for hint
            return position != 0;
        }
        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            View view = super.getDropDownView(position, convertView, parent);
            if (position == 0) {
                TextView tv = (TextView) view;
                tv.setTextColor(Color.LTGRAY);
                tv.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
            }
            return view;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate(Bundle savedInstanceState [" + savedInstanceState + "]);");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guard_tracker);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        // Get GuardTracker _ID
        Intent intent = getIntent();
        mGuardTrackerId = intent.getIntExtra(MainActivity.GUARD_TRACKER_ID, 0);
        if (mGuardTrackerId == 0) {
            if (savedInstanceState != null) {
                mGuardTrackerId = savedInstanceState.getInt(MainActivity.GUARD_TRACKER_ID);
            } else {
                mGuardTrackerId = preferences.getInt(getString(R.string.saved_guard_tracker_id), 0);
                if (mGuardTrackerId == 0) {

                    // Return to previous screen. ToDo...
                    return;
                }
            }
        }

//        enableSpinnerPosition = preferences.getInt(getString(R.string.saved_guard_tracker_enable_spinner_position), 0);
//        monInfoSpinnerPosition = preferences.getInt(getString(R.string.saved_guard_tracker_mon_info_spinner_position), 0);
//        trackSessionSpinnerPosition = preferences.getInt(getString(R.string.saved_guard_tracker_track_session_spinner_position), 0);
//        cfgSpinnerPosition = preferences.getInt(getString(R.string.saved_guard_tracker_cfg_spinner_position), 0);

        mIdNameView =               findViewById(R.id.identification_nickname_value);
        mIdNameImgButton =          findViewById(R.id.identification_nickname_edit_button);
        mIdBleAddrView =            findViewById(R.id.identification_ble_value);
        mIdGsmAddrView =            findViewById(R.id.identification_gsm_value);
        mIdGsmAddrImgButton =       findViewById(R.id.identification_gsm_edit_button);
        mLastMonInfoDatetimeView =  findViewById(R.id.last_mon_info_datetime);
        mLastMonInfoTimeView =      findViewById(R.id.last_mon_info_time);
        mLastMonInfoLatView =       findViewById(R.id.last_mon_info_lat);
        mLastMonInfoLngView =       findViewById(R.id.last_mon_info_lng);
        mLastMonInfoAltView =       findViewById(R.id.last_mon_info_alt);
        mLastMonInfoSatView =       findViewById(R.id.last_mon_info_sat);
        mLastMonInfoHdopView =      findViewById(R.id.last_mon_info_hdop);
        mLastMonInfoFixedView =     findViewById(R.id.last_mon_info_fixed);
        mLastMonInfoTempView =      findViewById(R.id.last_mon_info_temp);
        mLastMonInfoSimView =       findViewById(R.id.last_mon_info_sim);
        mLastMonInfoBatView =       findViewById(R.id.last_mon_info_bat);
        mLastMonInfoGoogleMapsButton = findViewById(R.id.last_mon_info_google_maps_button);
        mPosRefDatetimeView =       findViewById(R.id.pos_ref_datetime);
        mPosRefLatView =            findViewById(R.id.pos_ref_lat);
        mPosRefLngView =            findViewById(R.id.pos_ref_lng);
        mPosRefAltView =            findViewById(R.id.pos_ref_alt);
        mPosRefSatView =            findViewById(R.id.pos_ref_sat);
        mPosRefHdopView =           findViewById(R.id.pos_ref_hdop);
        mPosRefFixedView =          findViewById(R.id.pos_ref_fixed);
        mPosRefRefreshButton =      findViewById(R.id.pos_ref_refresh_button);
        mPosRefNewRefButton =       findViewById(R.id.pos_ref_set_new_button);
        mPosRefCancelNewRefButton = findViewById(R.id.pos_ref_cancel_button);
        mPosRefPendingButton =      findViewById(R.id.pos_ref_pending_button);
        mPosRefGoogleMapsButton =   findViewById(R.id.pos_ref_google_maps_button);
        mBleStatusValueView =       findViewById(R.id.ble_status_value);
        mBleStatusCommandsView =    findViewById(R.id.ble_status_commands_editText);
        mBleStatusCommandsButton =  findViewById(R.id.ble_status_commands_button);
        mSensorsConfigBleView =     findViewById(R.id.sensors_ble_state_label);
        mSensorsConfigRtcView =     findViewById(R.id.sensors_rtc_state_label);
        mSensorsConfigAccView =     findViewById(R.id.sensors_acc_state_label);
        mSensorsSyncedStateView =   findViewById(R.id.sensors_synced_state_view);
        mSensorsConfigSpinner =     findViewById(R.id.sensors_config_spinner);
        mSensorsSyncSpinner =       findViewById(R.id.sensors_sync_spinner);
        mMonCfgDatetimeView =       findViewById(R.id.mon_cfg_next_wakeup_view);
        mMonCfgSyncedStateView =    findViewById(R.id.mon_cfg_synced_state_view);
        mMonCfgConfigSpinner =      findViewById(R.id.mon_cfg_config_spinner);
        mMonCfgSyncSpinner =        findViewById(R.id.mon_cfg_sync_spinner);
        mTrackCfgSyncedStateView =  findViewById(R.id.track_cfg_synced_state_view);
        mTrackCfgConfigSpinner =    findViewById(R.id.track_cfg_config_spinner);
        mTrackCfgSyncSpinner =      findViewById(R.id.track_cfg_sync_spinner);
        mVigCfgSyncedStateView =    findViewById(R.id.vig_cfg_synced_state_view);
        mVigCfgConfigSpinner =      findViewById(R.id.vig_cfg_config_spinner);
        mVigCfgSyncSpinner =        findViewById(R.id.vig_cfg_sync_spinner);
//        mTrackCfgDetailsButton =    findViewById(R.id.track_cfg_details);
//        mVigilanceCfgDetailsButton = findViewById(R.id.vigilance_cfg_details);
        mMonInfoSizeEntriesView =   findViewById(R.id.mon_info_size_entries_view);
        mMonInfoTempButton =        findViewById(R.id.mon_info_temp_button);
        mMonInfoPosButton =         findViewById(R.id.mon_info_pos_button);
        mMonInfoSimButton =         findViewById(R.id.mon_info_sim_button);
        mMonInfoBatButton =         findViewById(R.id.mon_info_bat_button);
        mTrackSessionSizeEntriesView = findViewById(R.id.track_session_size_entries_view);
        mTrackSessionAllButton =    findViewById(R.id.track_session_all_button);
        mTrackSessionLastButton =   findViewById(R.id.track_session_last_button);
        mTrackSessionSelectButton = findViewById(R.id.track_session_select_button);
//        mNicknameEditButton        = (ImageButton)findViewById(R.id.nickname_edit_button);
//        mNicknameDoneButton        = (ImageButton)findViewById(R.id.nickname_done_button);
//        mNicknameCancelButton      = (ImageButton)findViewById(R.id.nickname_cancel_button);
        mContactsOwnerView =        findViewById(R.id.owner_value);
        mContactsOwnerRefreshButton = findViewById(R.id.owner_refresh_button);
        mContactsOwnerRstButton =   findViewById(R.id.owner_rst_button);
        mContactsSecondaryViewGroup = findViewById(R.id.contacts_container);
        mContactsSecondaryEmptyView = findViewById(R.id.empty_contacts);
        mContactsSecondaryAddButton = findViewById(R.id.secondary_contacts_add_button);
        mContactsSecondaryRefreshButton = findViewById(R.id.secondary_contacts_refresh_button);
        mContactsSecondaryDelButton = findViewById(R.id.secondary_contacts_delete_button);

//        mIdNameView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                final int DRAWABLE_LEFT = 0;
//                final int DRAWABLE_TOP = 1;
//                final int DRAWABLE_RIGHT = 2;
//                final int DRAWABLE_BOTTOM = 3;
//
//                if(event.getAction() == MotionEvent.ACTION_UP) {
//                    if(event.getRawX() >= (mIdNameView.getRight() - mIdNameView.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
//                        // your action here
//                        boolean editable = (Boolean)mIdNameView.getTag();
//                        updateNicknameView( ! editable );
//                        if (editable) {
//                            mGuardTracker.setName("" + mIdNameView.getText());
//                            mGuardTracker.update(GuardTrackerActivity.this);
//                        }
//                        v.playSoundEffect(android.view.SoundEffectConstants.CLICK);
//                        return true;
//                    }
//                }
//                return false;
//            }
//        });
//        mIdNameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                boolean handled = false;
//                if (actionId == EditorInfo.IME_ACTION_DONE) {
//                    updateNicknameView(false);
//                    mIdNameView.setText(mGuardTracker.getName());
//                    handled = true;
//                }
//                return handled;
//
//            }
//        });
        mIdNameImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNicknameDialog();
                v.setClickable(false);
            }
        });
        mIdGsmAddrImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startIdGsmAddrDialog();
                v.setClickable(false);
            }
        });

        ArrayAdapter<String> adapterConfigSpinner;
        ArrayAdapter<String> adapterSyncSpinner;
        List<String> config;
        String [] sync;

        // Get String array from resources
        config = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.config_spinner_array)));
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapterConfigSpinner = new MySpinnerAdapter<>(this,
                android.R.layout.simple_spinner_item, config);
        // Specify the layout to use when the list of choices appears
        adapterConfigSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mMonCfgConfigSpinner.setAdapter(adapterConfigSpinner);
        mMonCfgConfigSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mMonCfgConfigSpinner.setSelection(0);
                switch(position) {
                    case 1:
                        startMonCfgEditDialog();
                        break;
                    case 2:
                        startMonCfgRestoreDialog();
                        break;
                    case 3:
                        startMonCfgViewSyncedDialog();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        // Get String array from resources
        sync = getResources().getStringArray(R.array.sync_spinner_array);
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapterSyncSpinner = new MySpinnerAdapter<>(this,
                android.R.layout.simple_spinner_item, sync);
        // Specify the layout to use when the list of choices appears
        adapterSyncSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mMonCfgSyncSpinner.setAdapter(adapterSyncSpinner);
        mMonCfgSyncSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mMonCfgSyncSpinner.setSelection(0);
                switch(position) {
                    case 1:
                        Log.i(TAG, "sensorsSyncButton.onClick");
                        startMonCfgSyncDialog();
                        break;
                    case 2:
                        Log.i(TAG, "sensorsRefreshButton.onClick");
                        startMonCfgRefreshDialog();
                        break;
                    default:
                        Log.i(TAG, "Sync spinner default item selected");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Tracking configuration
        // Apply the adapter to the spinner
        mTrackCfgConfigSpinner.setAdapter(adapterConfigSpinner);
        mTrackCfgConfigSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mTrackCfgConfigSpinner.setSelection(0);
                switch(position) {
                    case 1:
                        startTrackCfgEditDialog();
                        break;
                    case 2:
                        startTrackCfgRestoreDialog();
                        break;
                    case 3:
                        startTrackCfgViewSyncedDialog();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        // Apply the adapter to the spinner
        mTrackCfgSyncSpinner.setAdapter(adapterSyncSpinner);
        mTrackCfgSyncSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mTrackCfgSyncSpinner.setSelection(0);
                switch(position) {
                    case 1:
                        Log.i(TAG, "trackSyncButton.onClick");
                        startTrackCfgSyncDialog();
                        break;
                    case 2:
                        Log.i(TAG, "trackRefreshButton.onClick");
                        startTrackCfgRefreshDialog();
                        break;
                    default:
                        Log.i(TAG, "Sync spinner default item selected");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        // Tracking configuration
        // Apply the adapter to the spinner
        mVigCfgConfigSpinner.setAdapter(adapterConfigSpinner);
        mVigCfgConfigSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mVigCfgConfigSpinner.setSelection(0);
                switch(position) {
                    case 1:
                        startVigCfgEditDialog();
                        break;
                    case 2:
                        startVigCfgRestoreDialog();
                        break;
                    case 3:
                        startVigCfgViewSyncedDialog();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        // Apply the adapter to the spinner
        mVigCfgSyncSpinner.setAdapter(adapterSyncSpinner);
        mVigCfgSyncSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mVigCfgSyncSpinner.setSelection(0);
                switch(position) {
                    case 1:
                        Log.i(TAG, "vigSyncButton.onClick");
                        startVigCfgSyncDialog();
                        break;
                    case 2:
                        Log.i(TAG, "vigRefreshButton.onClick");
                        startVigCfgRefreshDialog();
                        break;
                    default:
                        Log.i(TAG, "Sync spinner default item selected");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        // History
        mMonInfoTempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i(TAG, "temperatureButton.onClick");
                Intent intent = new Intent(getBaseContext(), MonitoringInfoActivity.class);
                int monInfoSelected = MonitoringInfo.MonInfoItem.TEMPERATURE.ordinal();
                int guardTrackerId = mGuardTracker.get_id();
                intent.putExtra(GuardTrackerActivity.MON_INFO_ITEM_SELECTED, monInfoSelected);
                intent.putExtra(GuardTrackerActivity.GUARD_TRACKER_ID, guardTrackerId);
                startActivity(intent);

            }
        });
        mMonInfoPosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i(TAG, "positionButton.onClick");
                Intent intent = new Intent(getBaseContext(), MonitoringInfoActivity.class);
                int monInfoSelected = MonitoringInfo.MonInfoItem.POSITION.ordinal();
                int guardTrackerId = mGuardTracker.get_id();
                intent.putExtra(GuardTrackerActivity.MON_INFO_ITEM_SELECTED, monInfoSelected);
                intent.putExtra(GuardTrackerActivity.GUARD_TRACKER_ID, guardTrackerId);
                startActivity(intent);

            }
        });
        mMonInfoSimButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i(TAG, "simBalanceButton.onClick");
                Intent intent = new Intent(getBaseContext(), MonitoringInfoActivity.class);
                int monInfoSelected = MonitoringInfo.MonInfoItem.SIM_BALANCE.ordinal();
                int guardTrackerId = mGuardTracker.get_id();
                intent.putExtra(GuardTrackerActivity.MON_INFO_ITEM_SELECTED, monInfoSelected);
                intent.putExtra(GuardTrackerActivity.GUARD_TRACKER_ID, guardTrackerId);
                startActivity(intent);

            }
        });
        mMonInfoBatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i(TAG, "batteryButton.onClick");
                Intent intent = new Intent(getBaseContext(), MonitoringInfoActivity.class);
                int monInfoSelected = MonitoringInfo.MonInfoItem.BATTERY.ordinal();
                int guardTrackerId = mGuardTracker.get_id();
                intent.putExtra(GuardTrackerActivity.MON_INFO_ITEM_SELECTED, monInfoSelected);
                intent.putExtra(GuardTrackerActivity.GUARD_TRACKER_ID, guardTrackerId);
                startActivity(intent);

            }
        });
        // Track session
        mTrackSessionAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i(TAG, "allButton.onClick");
                // ToDo:
//                Intent intent = new Intent(getBaseContext(), MonitoringCfgActivity.class);
//                int monCfgId = mGuardTracker.getMonCfgId();
//                int guardTrackerId = mGuardTracker.get_id();
//                intent.putExtra(GuardTrackerActivity.CONFIG_ID, monCfgId);
//                intent.putExtra(GuardTrackerActivity.GUARD_TRACKER_ID, guardTrackerId);
//                startActivity(intent);

            }
        });
        mTrackSessionLastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i(TAG, "lastButton.onClick");
                // ToDo:
//                Intent intent = new Intent(getBaseContext(), MonitoringCfgActivity.class);
//                int monCfgId = mGuardTracker.getMonCfgId();
//                int guardTrackerId = mGuardTracker.get_id();
//                intent.putExtra(GuardTrackerActivity.CONFIG_ID, monCfgId);
//                intent.putExtra(GuardTrackerActivity.GUARD_TRACKER_ID, guardTrackerId);
//                startActivity(intent);

            }
        });
        mTrackSessionSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i(TAG, "selectButton.onClick");
                Intent intent = new Intent(getBaseContext(), TrackSessionsSelectionActivity.class);
                int guardTrackerId = mGuardTracker.get_id();
                String guardTrackerName = mGuardTracker.getName();
                intent.putExtra(GuardTrackerActivity.GUARD_TRACKER_ID, guardTrackerId);
                intent.putExtra(GuardTrackerActivity.GUARD_TRACKER_NAME, guardTrackerName);
                startActivity(intent);

            }
        });
        mLastMonInfoGoogleMapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Creates an Intent that will load a map of San Francisco
                MonitoringInfo lastMonInfo = mGuardTracker.getLastMonInfo();
                if (lastMonInfo != null) {

                    Position pos = lastMonInfo.getPosition();
                    if (pos != null) {

                        double lat = pos.getLatitude();
                        double lng = pos.getLongitude();
                        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + lat + "," + lng + "&z=12");
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        if (mapIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(mapIntent);
                        }
                    }
                }
            }
        });
        mPosRefGoogleMapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Creates an Intent that will load a map of San Francisco
                Position pos = mGuardTracker.getPosRef();
                if (pos != null) {

                    double lat = pos.getLatitude();
                    double lng = pos.getLongitude();
                    Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + lat + "," + lng + "&z=12");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(mapIntent);
                    }
                }
            }
        });

        // The next onClicks are ble connection depedents...

        mPosRefRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "PosRefRefreshButton.onClick");
                if (isBleConnectionValid())
                    onClickPosRefRefreshPos();
            }
        });
        mPosRefNewRefButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "posRefSetNewButton.onClick");
                if (isBleConnectionValid())
                    onClickPosRefNewReferencePos();
            }
        });
        mPosRefCancelNewRefButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "posRefCancelNewRefButton.onClick");
                if (isBleConnectionValid())
                    onClickPosRefCancelNewReferencePos();
            }
        });

        // Get String array from resources
        config = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.config_spinner_array)));
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapterConfigSpinner = new MySpinnerAdapter<>(this,
                android.R.layout.simple_spinner_item, config);
        // Specify the layout to use when the list of choices appears
        adapterConfigSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mSensorsConfigSpinner.setAdapter(adapterConfigSpinner);
        mSensorsConfigSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSensorsConfigSpinner.setSelection(0);
                switch(position) {
                    case 1:
                        startWakeSensorsEditDialog();
                        break;
                    case 2:
                        startWakeSensorsRestoreDialog();
                        break;
                    case 3:
                        startWakeSensorsViewSyncedDialog();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        // Get String array from resources
        sync = getResources().getStringArray(R.array.sync_spinner_array);
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapterSyncSpinner = new MySpinnerAdapter<>(this,
                android.R.layout.simple_spinner_item, sync);
        // Specify the layout to use when the list of choices appears
        adapterSyncSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mSensorsSyncSpinner.setAdapter(adapterSyncSpinner);
        mSensorsSyncSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSensorsSyncSpinner.setSelection(0);
                switch(position) {
                    case 1:
                        Log.i(TAG, "sensorsSyncButton.onClick");
                        startWakeSensorsSyncDialog();
                        break;
                    case 2:
                        Log.i(TAG, "sensorsRefreshButton.onClick");
                        startWakeSensorsRefreshDialog();
                        break;
                    default:
                        Log.i(TAG, "Sync spinner default item selected");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
//        mNicknameEditButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i(TAG, "nicknameEditButton.onClick");
//                mNicknameDoneButton.setVisibility(View.VISIBLE);
//                mNicknameCancelButton.setVisibility(View.VISIBLE);
//                mNicknameEditButton.setVisibility(View.GONE);
//                mIdNameView.setInputType(InputType.TYPE_CLASS_TEXT);
//                // ToDo
//            }
//        });
//        mNicknameEditButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i(TAG, "nicknameEditButton.onClick");
//                updateNicknameView(true);
//                // ToDo
//            }
//        });
//        mNicknameDoneButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i(TAG, "nicknameDoneButton.onClick");
//                updateNicknameView(false);
//                // ToDo
//            }
//        });
//        mNicknameCancelButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i(TAG, "nicknameCancelButton.onClick");
//                updateNicknameView(false);
//                // ToDo
//            }
//        });

        mContactsOwnerRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "ownerRefreshButton.onClick");
                if (isBleConnectionValid())
                    onClickOwnerRefresh();
            }
        });
        mContactsOwnerRstButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "ownerRstButton.onClick");
                if (isBleConnectionValid())
                    startOwnerRstDialog();
            }
        });

        mContactsSecondaryRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "secondaryContactRefreshButton.onClick");
                if (isBleConnectionValid())
                    onClickSecondaryContactsRefresh();
            }
        });
        mContactsSecondaryAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "secondaryContactAddButton.onClick");
                if (isBleConnectionValid())
                    startAddSecondaryContactDialog();
            }
        });
        mContactsSecondaryDelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "secondaryContactDelButton.onClick");
                if (isBleConnectionValid())
                    startDeleteSecondaryContactDialog();
            }
        });

        mBleState = BleStateEnum.Disconnected;


        mBleStatusCommandsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBleStatusCommandsButton.getText().equals(getText(R.string.ble_status_view_messages_button_label))) {
                    mBleStatusCommandsView.setVisibility(View.VISIBLE);
                    mBleStatusCommandsButton.setText(R.string.ble_status_hide_messages_button_label);
                } else {
                    mBleStatusCommandsView.setVisibility(View.GONE);
                    mBleStatusCommandsButton.setText(R.string.ble_status_view_messages_button_label);
                }

            }
        });
        mHandler = new Handler();

        // Deve ser feito aqui e não no onStart porque poderemos estar a meio de uma edição do nickname
        // quando interrompemos essa edição e quando voltarmos pretendemos manter a edição.
//        mIdNameView.setTag(new Boolean(false));
//        updateNicknameView(false);
// Ver a definição de MyFocusChangeListener
//        mIdNameView.setOnFocusChangeListener(new MyFocusChangeListener());

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.guard_tracker_menu, menu);

        MenuItem stop = menu.findItem(R.id.menu_stop);
        MenuItem scan = menu.findItem(R.id.menu_scan);
        MenuItem refresh = menu.findItem(R.id.menu_refresh);
        MenuItem connect = menu.findItem(R.id.menu_connect);
        MenuItem disconnect = menu.findItem(R.id.menu_disconnect);

        MenuItem resync = menu.findItem(R.id.action_resynnc_app);
        MenuItem factoryDefaults = menu.findItem(R.id.action_factory_defaults);

        stop.setVisible(mBleState == BleStateEnum.Scanning || mBleState == BleStateEnum.Connecting);
        scan.setVisible(mBleState == BleStateEnum.Disconnected);
        connect.setVisible(mBleState == BleStateEnum.Connect);
        disconnect.setVisible(mBleState == BleStateEnum.Connected);
        if (mBleState == BleStateEnum.Scanning || mBleState == BleStateEnum.Connecting)
            refresh.setActionView(R.layout.actionbar_indeterminate_progress);
        else
            refresh.setActionView(null);
        resync.setEnabled(mBleState == BleStateEnum.Connected);
        factoryDefaults.setEnabled(mBleState == BleStateEnum.Connected);

//        MenuItem bleMenuItem = menu.findItem(R.id.ble_status_icon);
//        Drawable drawable = mBleConnected ? getDrawable(R.drawable.ic_action_bluetooth) : null;
//
//        bleMenuItem.setIcon(drawable);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_factory_defaults) {
            startResetToFactoryDefaultsDialog();
            return true;
        }
        if (id == R.id.action_resynnc_app) {
            startResyncApp();
            return true;
        }
        if (id == R.id.action_eliminate_device) {
            startConfirmationDialog();
            return true;
        }
        if (id == R.id.action_sms_received) {
            final String[] PERMISSIONS = new String[]{Manifest.permission.READ_SMS};
            PermissionsChecker checker = new PermissionsChecker(getBaseContext());

            if (checker.lacksPermissions(PERMISSIONS)) {
                startPermissionsActivity();
            } else {
                startSmsReceivedEarlierActivity();
            }
            return true;
        }
        if (id == R.id.menu_stop) {
            if (mBleState == BleStateEnum.Scanning) {
                stopBleScan();
                updateBleStateAndInvalidateMenu(BleStateEnum.Disconnected);
            } else if (mBleState == BleStateEnum.Connecting) {
                mBleConnControl.close();
                mBleConnControl = null;
                updateBleStateAndInvalidateMenu(BleStateEnum.Disconnected);
                // ToDo: desactivar os componentes bluetooth.
            }
            return true;
        }
        if (id == R.id.menu_scan) {
            // Use this check to determine whether BLE is supported on the device.  Then you can
            // selectively disable BLE-related features.
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
                return true;
            }
            // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
            // BluetoothAdapter through BluetoothManager.
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
            // Checks if Bluetooth is supported on the device.
            if (mBluetoothAdapter == null) {
                Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
                return true;
            }
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                return true;
            }
            startBleScan(); // Já altera o estado de mBleSate e invalida o menu

            return true;
        }
        if (id == R.id.menu_connect) {
            if (mBleConnControl != null)
                mBleConnControl.connect();
            else {
                mBleConnControl = new GuardTrackerBleConnControl(getBaseContext(), mGuardTracker.getBleId(),
                        mGuardTracker.getOwnerPhoneNumber(), this, null);
            }
            updateBleStateAndInvalidateMenu(BleStateEnum.Connecting);
            return true;
        }
        if (id == R.id.menu_disconnect) {
            mBleConnControl.disconnect();
            updateBleStateAndInvalidateMenu(BleStateEnum.Connect);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Remove next method when it is tested in BleControlActivity. Needs to be revisited.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_READ_SMS) {
            if (resultCode == PermissionsActivity.PERMISSIONS_DENIED)
                Toast.makeText(getBaseContext(), "The list cannot be done without permission", Toast.LENGTH_LONG);
            else
                startSmsReceivedEarlierActivity();

            return;
        }
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK)
                startBleScan();
            else
                Toast.makeText(getBaseContext(), "No bluetooth connection can be made without bluetooth enabled", Toast.LENGTH_LONG);
            return;
        }
        if (requestCode == REQUEST_MON_CFG) {
            if (resultCode == Activity.RESULT_OK) {
                // Create GuardTracker backup if returned id is different of current monCfgId.
                int new_id = data.getIntExtra(RESULT_MON_CFG, 0);
                if (new_id != mGuardTracker.getMonCfgId())
                    onClickMonCfgEdit(new_id);
            } else
                Toast.makeText(getBaseContext(), "Monitoring Configuration Activity return error", Toast.LENGTH_LONG);

            return;
        }
        if (requestCode == REQUEST_TRACK_CFG) {
            if (resultCode == Activity.RESULT_OK) {
                // Create GuardTracker backup if returned id is different of current trackCfgId.
                int new_id = data.getIntExtra(RESULT_TRACK_CFG, 0);
                if (new_id != mGuardTracker.getTrackCfgId())
                    onClickTrackCfgEdit(new_id);
            } else
                Toast.makeText(getBaseContext(), "Tracking Configuration Activity return error", Toast.LENGTH_LONG);

            return;
        }
        if (requestCode == REQUEST_VIG_CFG) {
            if (resultCode == Activity.RESULT_OK) {
                // Create GuardTracker backup if returned id is different of current vigCfgId.
                int new_id = data.getIntExtra(RESULT_VIG_CFG, 0);
                if (new_id != mGuardTracker.getVigCfgId())
                    onClickVigCfgEdit(new_id);
            } else
                Toast.makeText(getBaseContext(), "Vigilance Configuration Activity return error", Toast.LENGTH_LONG);

            return;
        }
    }


    @Override
    protected void onStart() {
        Log.i(TAG, "onStart()");
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();

        // Transferi o código que se segue do onCreate para aqui porque no entretanto podem ter sido
        // estado deste Guardtracker
        // Build model
        mGuardTracker = GuardTracker.read(getBaseContext(), mGuardTrackerId);

        if (mGuardTracker.getPosRefId() != 0) {
            Position refPos = Position.read(getBaseContext(), mGuardTracker.getPosRefId());
            mGuardTracker.setPosRef(refPos);
        }
        if (mGuardTracker.getLastMonInfoId() != 0) {
            MonitoringInfo lastMonInfo = MonitoringInfo.read(getBaseContext(), mGuardTracker.getLastMonInfoId());
            Position lastPosMonInfo = Position.read(getBaseContext(), lastMonInfo.getPositionId());
            assert lastPosMonInfo != null;
            lastMonInfo.setPosition(lastPosMonInfo);
            mGuardTracker.setLastMonInfo(lastMonInfo);
        }
        mMonInfoList = MonitoringInfo.readList(getBaseContext(), mGuardTracker.get_id());
        mTrackSessionList = TrackSession.readList(getBaseContext(), mGuardTracker.get_id());
        MonitoringConfiguration monCfg = MonitoringConfiguration.read(getBaseContext(), mGuardTracker.getMonCfgId());
        mGuardTracker.setMonCfg(monCfg);
        // Tracking and Vigilance configuration needs to be initiated already because on updateView
        // this objects are used to be compared
        TrackingConfiguration trackCfg = TrackingConfiguration.read(getBaseContext(), mGuardTracker.getTrackCfgId());
        mGuardTracker.setTrackCfg(trackCfg);
        VigilanceConfiguration vigCfg = VigilanceConfiguration.read(getBaseContext(), mGuardTracker.getVigCfgId());
        mGuardTracker.setVigilanceCfg(vigCfg);
        mContactsSecondaryList = SecondaryContactsDbHelper.read(getBaseContext(), mGuardTracker.get_id());
        mGuardTracker.setSecondaryContacts(mContactsSecondaryList);

        // Set views state
        // Identification
        setTitle(mGuardTracker.getName());
        mIdNameView.setText(mGuardTracker.getName());
        mIdBleAddrView.setText(mGuardTracker.getBleId());
        mIdGsmAddrView.setText(mGuardTracker.getGsmId());


        // Configurations
        updateMonCfgView();
        updateTrackCfgView();
        updateVigCfgView();
        // Current status
        // Ble status
        updateBleStatusView();
        // Last monitoring information
        updateLastMonInfoView();
        // Reference position
        updateReferencePositionView();
        // Wake sensors
        // Turn the buttons disabled in config spinner if there is no wakeup sensors backup.
        updateWakeSensorsView();

        // Monitoring information
        int monInfoSize = mMonInfoList.size();
        mMonInfoSizeEntriesView.setText(String.format("Number of entries: %d", monInfoSize));
        boolean clickable = monInfoSize != 0;
        mMonInfoTempButton.setEnabled(clickable);
        mMonInfoBatButton.setEnabled(clickable);
        mMonInfoSimButton.setEnabled(clickable);
        mMonInfoPosButton.setEnabled(clickable);
        // Track session
        int trackSessionSize = mTrackSessionList.size();
        clickable = trackSessionSize != 0;
        mTrackSessionSizeEntriesView.setText(String.format("Number of entries: %d", trackSessionSize));
        mTrackSessionAllButton.setEnabled(clickable);
        mTrackSessionLastButton.setEnabled(clickable);
        mTrackSessionSelectButton.setEnabled(clickable);

        // Contacts
        // Owner
        mContactsOwnerView.setText(mGuardTracker.getOwnerPhoneNumber());
        updateSecondaryContactsView();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "GuardTracker Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.patri.guardtracker/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop()");

        if (mBleState == BleStateEnum.Scanning)
            stopBleScan();

        if (mBleState == BleStateEnum.Connected && mBleConnControl != null) {
            Log.i(TAG, "onStop: mBleConnCtrl.close(); mBleConnControl = null");
            mBleConnControl.close();
        }
        mBleConnControl = null;

        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "GuardTracker Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.patri.guardtracker/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);


//        SharedPreferences preferences = this.getPreferences(MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putInt(getString(R.string.saved_guard_tracker_id), guardTrackerId);
//        editor.putInt(getString(R.string.saved_guard_tracker_pos_ref_id), posRefId);
//        editor.putInt(getString(R.string.saved_guard_tracker_mon_cfg_id), monCfgId);
//        editor.putInt(getString(R.string.saved_guard_tracker_track_session_cfg_id), trackSessionCfgId);
//        editor.putInt(getString(R.string.saved_guard_tracker_vigilance_cfg_id), vigilanceCfgId);
//        editor.commit();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause()");
        super.onPause();

        SharedPreferences preferences = this.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(getString(R.string.saved_guard_tracker_id), mGuardTracker.get_id());
        editor.putInt(getString(R.string.saved_guard_tracker_pos_ref_id), mGuardTracker.getPosRefId());
        editor.putInt(getString(R.string.saved_guard_tracker_mon_cfg_id), mGuardTracker.getMonCfgId());
        editor.putInt(getString(R.string.saved_guard_tracker_track_session_cfg_id), mGuardTracker.getTrackCfgId());
        editor.putInt(getString(R.string.saved_guard_tracker_vigilance_cfg_id), mGuardTracker.getVigCfgId());

//        editor.putInt(getString(R.string.saved_guard_tracker_enable_spinner_position), enableSpinnerPosition);
//        editor.putInt(getString(R.string.saved_guard_tracker_mon_info_spinner_position), monInfoSpinnerPosition);
//        editor.putInt(getString(R.string.saved_guard_tracker_track_session_spinner_position), trackSessionSpinnerPosition);
//        editor.putInt(getString(R.string.saved_guard_tracker_cfg_spinner_position), cfgSpinnerPosition);

        editor.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        Log.i(TAG, "onSaveInstanceState()");

        // Save GuardTracker _ID
        outState.putInt(MainActivity.GUARD_TRACKER_ID, mGuardTracker.get_id());

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestart() {
        Log.i(TAG, "onRestart()");
        super.onRestart();

//        if (mGuardTracker == null) {
//
//            SharedPreferences preferences = this.getPreferences(MODE_PRIVATE);
//            guardTrackerId = preferences.getInt(getString(R.string.saved_guard_tracker_id), -1);
//            posRefId = preferences.getInt(getString(R.string.saved_guard_tracker_pos_ref_id), -1);
//            monCfgId = preferences.getInt(getString(R.string.saved_guard_tracker_mon_cfg_id), -1);
//            trackSessionCfgId = preferences.getInt(getString(R.string.saved_guard_tracker_track_session_cfg_id), -1);
//            vigilanceCfgId = preferences.getInt(getString(R.string.saved_guard_tracker_vigilance_cfg_id), -1);
//
//            EditText nameEditText = (EditText) findViewById(R.id.nickname_editText);
//            assert nameEditText != null;
//            String name = nameEditText.getText().toString();
//            if (name == null)
//                name = nameEditText.getHint().toString();
//
//            EditText gsmEditText = (EditText) findViewById(R.id.phone_number_editText);
//            assert gsmEditText != null;
//            String gsm = gsmEditText.getText().toString();
//            if (gsm == null)
//                gsm = gsmEditText.getHint().toString();
//
//            EditText bleEditText = (EditText) findViewById(R.id.phone_number_editText);
//            assert bleEditText != null;
//            String ble = bleEditText.getText().toString();
//            if (ble == null)
//                ble = bleEditText.getHint().toString();
//
//            Spinner enableSpinner = (Spinner) findViewById(R.id.enable_spinner);
//            assert enableSpinner != null;
//            int enable = enableSpinner.getSelectedItemPosition();
//
//            mGuardTracker = new GuardTracker(guardTrackerId, name, ble, gsm, enable == 1, posRefId, monCfgId, trackSessionCfgId, vigilanceCfgId);
//        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        Log.i(TAG, "onDialogNegativeClick --> Nothing to be done. The user cancel the current operation)");
        // Turn clickable again Nickname and GSM id
        mIdNameImgButton.setClickable(true);
        mIdGsmAddrImgButton.setClickable(true);
        dialog.dismiss();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        String dialogTag = dialog.getTag();
        if (DialogFragmentTags.WakeSensorsConfig.toString().equals(dialogTag)/*dialog instanceof WakeSensorsConfigDialogFragment*/) {
            try {
                WakeSensorsConfigDialogFragment wakeSensorsDialog = (WakeSensorsConfigDialogFragment) dialog;
                int bitmask = wakeSensorsDialog.getWakeSensorsBitmask();
                onClickWakeSensorsConfig(bitmask);
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), R.string.dialog_wake_sensors_message_unsuccessful_write, Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            dialog.dismiss();
            return;
        }
        if (DialogFragmentTags.WakeSensorsRestore.toString().equals(dialogTag)) {
            onClickWakeSensorsRestore();
            dialog.dismiss();
            return;
        }
        if (DialogFragmentTags.WakeSensorsRefresh.toString().equals(dialogTag)) {
            onClickWakeSensorsRefresh();
            dialog.dismiss();
            return;
        }
        if (DialogFragmentTags.WakeSensorsSync.toString().equals(dialogTag)) {
            onClickWakeSensorsSync();
            dialog.dismiss();
            return;
        }
        if (DialogFragmentTags.MonCfgRestore.toString().equals(dialogTag)) {
            onClickMonCfgRestore();
            dialog.dismiss();
            return;
        }
        if (DialogFragmentTags.MonCfgRefresh.toString().equals(dialogTag)) {
            onClickMonCfgRefresh();
            dialog.dismiss();
            return;
        }
        if (DialogFragmentTags.MonCfgSync.toString().equals(dialogTag)) {
            onClickMonCfgSync();
            dialog.dismiss();
            return;
        }
        if (DialogFragmentTags.TrackCfgRestore.toString().equals(dialogTag)) {
            onClickTrackCfgRestore();
            dialog.dismiss();
            return;
        }
        if (DialogFragmentTags.TrackCfgRefresh.toString().equals(dialogTag)) {
            onClickTrackCfgRefresh();
            dialog.dismiss();
            return;
        }
        if (DialogFragmentTags.TrackCfgSync.toString().equals(dialogTag)) {
            onClickTrackCfgSync();
            dialog.dismiss();
            return;
        }
        if (DialogFragmentTags.VigCfgRestore.toString().equals(dialogTag)) {
            onClickVigCfgRestore();
            dialog.dismiss();
            return;
        }
        if (DialogFragmentTags.VigCfgRefresh.toString().equals(dialogTag)) {
            onClickVigCfgRefresh();
            dialog.dismiss();
            return;
        }
        if (DialogFragmentTags.VigCfgSync.toString().equals(dialogTag)) {
            onClickVigCfgSync();
            dialog.dismiss();
            return;
        }
        if (DialogFragmentTags.FactoryDefaultsReset.toString().equals(dialogTag)) {
            onClickResetFactoryDefaults();
            dialog.dismiss();
            return;
        }
        if (DialogFragmentTags.AppResync.toString().equals(dialogTag)) {
            onClickResyncAppWithDevice();
            dialog.dismiss();
            return;
        }
        if (DialogFragmentTags.DevicePhoneNumber.toString().equals(dialogTag)) {
            PhonePickerDialogFragment dialogFragment = (PhonePickerDialogFragment) dialog;
            String devicePhoneNumber = dialogFragment.getPhoneNumber();
            mTempGuardTracker.setGsmId(devicePhoneNumber);
            mSyncConfigWorkflow.workflowContinue();
            dialog.dismiss();
            return;
        }
        if (DialogFragmentTags.SelfDeviceEliminate.toString().equals(dialogTag)/*dialog instanceof DialogGenericIcTtMs2Bt*/) {
            int id = mGuardTrackerId;
            boolean deviceEliminated = GuardTracker.deleteDeep(getBaseContext(), id);
            Toast.makeText(getBaseContext(), deviceEliminated ?
                            R.string.dialog_eliminate_devices_message_successful :
                            R.string.dialog_eliminate_devices_message_unsuccessful,
                    Toast.LENGTH_LONG).show();
            dialog.dismiss();
            finish();
        }
        if (DialogFragmentTags.SecondaryContactAdd.toString().equals(dialogTag)) {
            PhonePickerDialogFragment phonePickerDialogFragment = (PhonePickerDialogFragment) dialog;
            String phoneNumber = phonePickerDialogFragment.getPhoneNumber();
            onClickAddSecondaryContact(phoneNumber);
            dialog.dismiss();
            return;
        }
        if (DialogFragmentTags.SecondaryContactDelete.toString().equals(dialogTag)) {
            PhonePickerDialogFragment phonePickerDialogFragment = (PhonePickerDialogFragment) dialog;
            String phoneNumber = phonePickerDialogFragment.getPhoneNumber();
            onClickDelSecondaryContact(phoneNumber);
            dialog.dismiss();
            return;
        }
        if (DialogFragmentTags.ChangeOwner.toString().equals(dialogTag)) {
            PhonePickerDialogFragment phonePickerDialogFragment = (PhonePickerDialogFragment) dialog;
            String phoneNumber = phonePickerDialogFragment.getPhoneNumber();
            onClickRstOwner(phoneNumber);
            dialog.dismiss();
            return;
        }
        if (DialogFragmentTags.ChangeDevicePhoneNumber.toString().equals(dialogTag)) {
            PhonePickerDialogFragment phonePickerDialogFragment = (PhonePickerDialogFragment) dialog;
            String phoneNumber = phonePickerDialogFragment.getPhoneNumber();
            onClickChangeDevicePhoneNumber(phoneNumber);
            dialog.dismiss();
            return;
        }
        else if (DialogFragmentTags.ChangeDeviceNickname.toString().equals(dialogTag)) {
            NicknamePickerDialogFragment nicknamePickerDialogFragment = (NicknamePickerDialogFragment) dialog;
            String nickname = nicknamePickerDialogFragment.getText();
            onClickChangeDeviceNickname(nickname);
            dialog.dismiss();
            return;
        }
    }
}
