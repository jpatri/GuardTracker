package com.patri.guardtracker;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.patri.guardtracker.bluetooth.BleConnectionStateChangeListener;
import com.patri.guardtracker.bluetooth.BleMessageListener;
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
import com.patri.guardtracker.synchronization.GuardTrackerSyncConfigListener;
import com.patri.guardtracker.synchronization.GuardTrackerSyncConfigWorkflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class GuardTrackerActivity2 extends AppCompatActivity implements BleConnectionStateChangeListener, DialogListener {
    public final static String TRACK_SESSION_ITEM_SELECTED = "com.patri.guardtracker.TRACK_SESSION_ITEM_SELECTION";
    public final static String CONFIG_ID = "com.patri.guardtracker.CONFIG_ID";
    public final static String MON_INFO_ITEM_SELECTED = "com.patri.guardtracker.MON_INFO_ITEM_SELECTED";
    public final static String GUARD_TRACKER_ID = "com.patri.guardtracker.GUARD_TRACKER_ID";
    public final static String GUARD_TRACKER_NAME = "com.patri.guardtracker.GUARD_TRACKER_NAME";
    private final static String TAG = GuardTrackerActivity2.class.getSimpleName();

    private int mGuardTrackerId;
    private GuardTracker mGuardTracker;
    private MonitoringConfiguration mMonCfg;
    private GuardTrackerSyncConfigWorkflow mSyncConfigWorkflow;
    private GuardTracker mTempGuardTracker; // Used in resync App command


    enum BleStateEnum {
        Disconnected, Scanning, Connect, Connecting, Connected;
        private String [] valuesStr = {"Disconnected", "Scanning", "In range", "Connecting", "Connected"};
        public String toString() {
            String statusStr = valuesStr[this.ordinal()];
            return statusStr;
        }
    }

    /**
     * This enumerate is used to establish the Tag to be passed to dialog. The same dialog type is several situations.
     */
    enum DialogFragmentTags {
        SelfDeviceEliminate, DevicesEliminate, WakeSensorsConfig, SecondaryContactAdd,
        SecondaryContactDelete, FactoryDefaultsReset, AppResync, DevicePhoneNumber, ChangeOwner, ChangeDevicePhoneNumber;
        private String [] valuesStr = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
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
    private static final long SCAN_PERIOD = 10000;

    // No momento da introdução dos próximos atributos fica a ideia que poder ser
    // demasiada penalização ler a base de dados só para obter o número total de entradas.
    private ArrayList<MonitoringInfo> mMonInfoList;
    private ArrayList<TrackSession> mTrackSessionList;
    private ArrayList<String> mContactsSecondaryList;

    //private TrackingConfiguration mTrackCfg;
    //private VigilanceConfiguration mVigilanceCfg;
    private TextView mIdNameView;
    private TextView mIdBleAddrView;
    private TextView mIdGsmAddrView;
    private ImageButton mIdGsmImgButton;
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
    private TextView mSensorsBleStatusView;
    private TextView mSensorsProgBleView;
    private TextView mSensorsProgRtcView;
    private TextView mSensorsProgAccView;

    private TextView mContactsOwnerView;
    private ViewGroup mContactsSecondaryViewGroup;
    private TextView mContactsSecondaryEmptyView;
    private Button   mContactsOwnerRefreshButton;
    private Button   mContactsOwnerRstButton;
    private Button   mContactsSecondaryAddButton;
    private Button   mContactsSecondaryRefreshButton;
    private Button   mContactsSecondaryDelButton;

    private Button   mLastMonInfoGoogleMapsButton;
    private Button   mPosRefGoogleMapsButton;
    private Button   mPosRefRefreshButton;
    private Button   mPosRefNewRefButton;
    private Button   mWakeSensorsCfgButton;
    private Button   mWakeSensorsRefreshButton;

    private TextView mMonCfgDatetimeView;
    private Button   mMonCfgDetailsButton;
    private Button   mTrackCfgDetailsButton;
    private Button   mVigilanceCfgDetailsButton;
    private TextView mMonInfoSizeEntriesView;
    private Button   mMonInfoTempButton;
    private Button   mMonInfoPosButton;
    private Button   mMonInfoSimButton;
    private Button   mMonInfoBatButton;
    private TextView mTrackSessionSizeEntriesView;
    private Button   mTrackSessionAllButton;
    private Button   mTrackSessionLastButton;
    private Button   mTrackSessionSelectButton;

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

    /* Attribute fro permissions checeker */
    static final String[] PERMISSIONS = new String[]{Manifest.permission.READ_SMS};
    //private PermissionsChecker mChecker;
    private static final int REQUEST_ENABLE_READ_SMS = 0;
    private static final int REQUEST_ENABLE_BT = 1;

    // Device scan callback.
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            if (device.getAddress().equals(mGuardTracker.getBleId())) {
                stopBleScan();
                updateBleStateAndInvalidateMenu(BleStateEnum.Connect);
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
        // Stops scanning after a pre-defined scan period.
        mHandler.postDelayed(stopBleScanRunnable, SCAN_PERIOD);
        mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
        updateBleStateAndInvalidateMenu(BleStateEnum.Scanning);
    }
    private void stopBleScan() {
        mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
        mHandler.removeCallbacks(stopBleScanRunnable);
    }

    @Override
    public void onAuthenticated() {
        Log.i(TAG, "onAuthenticated");
        mWakeSensorsCfgButton.setVisibility(View.VISIBLE);
        mWakeSensorsRefreshButton.setVisibility(View.VISIBLE);
        mPosRefRefreshButton.setVisibility(View.VISIBLE);
        mPosRefNewRefButton.setVisibility(View.VISIBLE);
        mContactsOwnerRefreshButton.setVisibility(View.VISIBLE);
        mContactsOwnerRstButton.setVisibility(View.VISIBLE);
        mContactsSecondaryAddButton.setVisibility(View.VISIBLE);
        mContactsSecondaryRefreshButton.setVisibility(View.VISIBLE);
        if (mContactsSecondaryList.size() > 0)
            mContactsSecondaryDelButton.setVisibility(View.VISIBLE);
        updateBleStateAndInvalidateMenu(BleStateEnum.Connected);
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
        mWakeSensorsCfgButton.setVisibility(View.GONE);
        mWakeSensorsRefreshButton.setVisibility(View.GONE);
        mPosRefRefreshButton.setVisibility(View.GONE);
        mPosRefNewRefButton.setVisibility(View.GONE);
        mContactsOwnerRstButton.setVisibility(View.GONE);
        mContactsOwnerRefreshButton.setVisibility(View.GONE);
        mContactsSecondaryAddButton.setVisibility(View.GONE);
        mContactsSecondaryDelButton.setVisibility(View.GONE);
        mContactsSecondaryRefreshButton.setVisibility(View.GONE);
        updateBleStateAndInvalidateMenu(BleStateEnum.Connect);
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
        if (mBleState != BleStateEnum.Connected) {
            Toast.makeText(getBaseContext(),
                    "Operation in progress needs ble to be connected",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void updateBleStateAndInvalidateMenu(BleStateEnum newState) {
        mBleState = newState;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSensorsBleStatusView.setText(mBleState.toString());

            }
        });
        invalidateOptionsMenu();
    }

    private void startPermissionsActivity() {
        PermissionsActivity.startActivityForResult(this, REQUEST_ENABLE_READ_SMS, PERMISSIONS);
    }
    private void startSmsReceivedEarlierActivity() {
        Intent intent = new Intent(this, SmsReceivedEarlierActivity.class);
        int guardTrackerId = mGuardTracker.get_id();
        String guardTrackerName = mGuardTracker.getName();
        intent.putExtra(GuardTrackerActivity2.GUARD_TRACKER_ID, guardTrackerId);
        intent.putExtra(GuardTrackerActivity2.GUARD_TRACKER_NAME, guardTrackerName);
        startActivity(intent);
    }

    /*
     * The iconRsc and titleRsc parameters are optional. The other parameters must be present.
     * Zero resource value means an invalid value.
     *
     */
    private void startGenericDialog(DialogGenericIcTtMs2Bt dialog, int iconRsc,
                                    int titleRsc, int msgRsc,
                                    int yesBtnRsc, int noBtnRsc,
                                    String tag) {
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
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
    private void startIdGsmAddrDialog()  {
        startGenericDialog(new PhonePickerDialogFragment(), android.R.drawable.ic_menu_edit,
                R.string.dialog_device_phone_number_title,
                R.string.dialog_device_phone_number_message_body,
                R.string.done_button_label,
                R.string.cancel_button_label,
                DialogFragmentTags.ChangeDevicePhoneNumber.toString());
    }

    private void startWakeSensorsConfigDialog() {
        int bitmask = mGuardTracker.getWakeSensors();
        WakeSensorsConfigDialogFragment dialogFragment = new WakeSensorsConfigDialogFragment();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(WakeSensorsConfigDialogFragment.WAKE_SENSORS_BITMASK_ID_KEY, bitmask);
        bundle.putInt(WakeSensorsConfigDialogFragment.WAKE_SENSORS_MESSAGE_BODY_ID_KEY, R.string.dialog_wake_sensors_message_body);
        bundle.putInt(WakeSensorsConfigDialogFragment.WAKE_SENSORS_TITLE_ID_KEY, R.string.dialog_wake_sensors_title);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, DialogFragmentTags.WakeSensorsConfig.toString());
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

    private void updateNicknameView(boolean editable) {
//        mNicknameEditButton.setEnabled(editable);
//        mNicknameDoneButton.setEnabled( ! editable);
//        mNicknameCancelButton.setEnabled( ! editable);
//        mNicknameEditButton.setClickable( ! editable);
//        mNicknameDoneButton.setClickable(editable);
//        mNicknameCancelButton.setClickable(editable);

        mIdNameView.setTag( editable );

        if (editable) {
            Drawable drawable = getDrawable(R.drawable.ic_action_checkmark);
            mIdNameView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
            mIdNameView.setInputType(InputType.TYPE_CLASS_TEXT);
            //mIdNameView.requestFocus();

        } else {
            Drawable drawable = getDrawable(R.drawable.ic_action_edit);
            mIdNameView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
            mIdNameView.setInputType(InputType.TYPE_NULL);
            InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mIdNameView.getWindowToken(), 0);
        }

    }
    private void updateIdGsmAddrView() {
        String str = mGuardTracker.getGsmId();
        if (str == null) {
            str = getString(R.string.id_gsm_addr_empty_label);
        }
        mIdGsmAddrView.setText(str);
        mIdGsmImgButton.setClickable(true);
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
    private void updateWakeSensorsView() {
        mSensorsBleStatusView.setText(mBleState.toString());
        int wakeSensors = mGuardTracker.getWakeSensors();
        mSensorsProgBleView.setText((wakeSensors & WakeSensorsConfigDialogFragment.BITMASK_BLE) != 0 ? "BLE on": "BLE off");
        mSensorsProgRtcView.setText((wakeSensors & WakeSensorsConfigDialogFragment.BITMASK_RTC) != 0 ? "RTC on": "RTC off");
        mSensorsProgAccView.setText((wakeSensors & WakeSensorsConfigDialogFragment.BITMASK_ACC) != 0 ? "ACC on": "ACC off");
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
            TextView label = (TextView) view.findViewById(R.id.text1);
            label.setText("Contact " + ( i + 1 ) + ":");
            TextView value = (TextView) view.findViewById(R.id.text2);
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

    private void onClickChangeDevicePhoneNumber(String phoneNumber) {
        // Update model
        mGuardTracker.setGsmId(phoneNumber);
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
                if (msg.length != 18/* FACADE: SHOULD NOT BE BASE ON NUMBER OF BYTES RECEIVED */) {
                    Toast.makeText(GuardTrackerActivity2.this.getBaseContext(),
                            R.string.toast_pos_ref_unsuccessful_read,
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // Parse ble message
                Position newRefPos = new Position(msg);
                if (mGuardTracker.getPosRef() != null) {
                    // Update old reference position.
                    int id = mGuardTracker.getPosRefId();
                    newRefPos.set_id(id);
                    newRefPos.update(getBaseContext());
                    mGuardTracker.setPosRef(newRefPos);
                } else {
                    newRefPos = new Position(newRefPos.getLatitude(), newRefPos.getLongitude(), newRefPos.getTime(), newRefPos.getAltitude(),
                            newRefPos.getSatellites(), newRefPos.getHdop(), newRefPos.getFixed());
                    newRefPos.create(GuardTrackerActivity2.this.getBaseContext());
                    mGuardTracker.setPosRef(newRefPos);
                    mGuardTracker.update(getBaseContext());
                }
                // Update view;
                updateReferencePositionView();
                Toast.makeText(getBaseContext(), R.string.refresh_pos_ref_success, Toast.LENGTH_LONG).show();
            }
        });
    }
    private void onClickPosRefNewReferencePos() {
        byte [] cmd = GuardTrackerCommands.resetRefPos();
        mBleConnControl.sendCommand(cmd, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msg) {
                if (Arrays.equals(msg, "1".getBytes()) == false) {
                    Toast.makeText(GuardTrackerActivity2.this.getBaseContext(),
                            R.string.toast_pos_ref_unsuccessful_write,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                onClickPosRefRefreshPos();
                Toast.makeText(getBaseContext(), R.string.toast_pos_ref_success, Toast.LENGTH_LONG).show();
            }
        });
    }
    private void onClickWakeSensorsRefresh() {
        short bitmask = 0;
        byte [] cmd = GuardTrackerCommands.readWakeSensors();
        mBleConnControl.sendCommand(cmd, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msg) {
                if (msg.length != 2/* FACADE: SHOULD NOT BE BASE ON NUMBER OF BYTES RECEIVED */) {
                    Toast.makeText(GuardTrackerActivity2.this.getBaseContext(),
                            R.string.dialog_wake_sensors_message_unsuccessful_read,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                // Parse ble message
                short bitmask = (short)((msg[0] << 8) | (msg[1] << 0));
                mGuardTracker.setWakeSensors(bitmask & 0xFFFF);
                mGuardTracker.update(getBaseContext());
                // Update view
                updateWakeSensorsView();
                Toast.makeText(getBaseContext(), R.string.refresh_wake_sensors_message_success, Toast.LENGTH_LONG).show();
            }
        });
    }
    private void onClickWakeSensorsConfig(int bitmask) {
        byte [] cmd = GuardTrackerCommands.setWakeSensors((short)bitmask);
        mBleConnControl.sendCommand(cmd, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msg) {
                if (Arrays.equals(msg, "1".getBytes()) == false) {
                    Toast.makeText(GuardTrackerActivity2.this.getBaseContext(),
                            R.string.dialog_wake_sensors_message_unsuccessful_write,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                onClickWakeSensorsRefresh();
                Toast.makeText(getBaseContext(), R.string.dialog_wake_sensors_message_success, Toast.LENGTH_LONG).show();
            }
        });
    }
    private void onClickOwnerRefresh() {
        byte[] cmd = GuardTrackerCommands.readOwner();
        mBleConnControl.sendCommand(cmd, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msgRecv) {
                String ownerPhoneNumber = "";
                if (msgRecv.length == 0/* FACADE: SHOULD NOT BE BASE ON NUMBER OF BYTES RECEIVED */) {
                    Toast.makeText(GuardTrackerActivity2.this.getBaseContext(),
                            R.string.refresh_owner_unsuccessful,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                // Parse ble message
                String phoneNumberRaw = new String(msgRecv);
                // Decode received message and build object.
                try {
                    ownerPhoneNumber = MyPhoneUtils.formatE164(phoneNumberRaw);
                    // Update database
                    mGuardTracker.setOwnerPhoneNumber(ownerPhoneNumber);
                    mGuardTracker.update(getBaseContext());
                    // Update view
                    updateOwnerPhoneNumberView();
                    Toast.makeText(getBaseContext(), R.string.refresh_owner_success, Toast.LENGTH_LONG).show();
                } catch (NumberParseException e) {
                    Log.e(TAG, e.getErrorType().toString() + ": " + e.getMessage());
                    Toast.makeText(GuardTrackerActivity2.this.getBaseContext(),
                            R.string.refresh_owner_unsuccessful,
                            Toast.LENGTH_LONG).show();
                }
            }

        });
    }
    private void onClickRstOwner(final String phoneNumber) {
        // Send command to change Owner with a new one
        byte [] cmd = GuardTrackerCommands.changeOwner(phoneNumber);
        mBleConnControl.sendCommand(cmd, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msgRecv) {
                if (Arrays.equals(msgRecv, "1".getBytes()) == false) {
                    Toast.makeText(GuardTrackerActivity2.this.getBaseContext(),
                            R.string.dialog_change_owner_unsuccessful_write,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                // Refresh database with data read from device
                onClickOwnerRefresh();
//                // Update model
//                mGuardTracker.setOwnerPhoneNumber(phoneNumber);
//                mGuardTracker.update(getBaseContext());
//                // Update view
//                updateOwnerPhoneNumberView();
                Toast.makeText(getBaseContext(), R.string.dialog_change_owner_success, Toast.LENGTH_LONG).show();
            }

//            @Override
//            public void onMessageSent(byte[] msgSent) {
//
//            }
        });
    }
    class ReadSecondaryContactsMessageListener implements BleMessageListener {
        @Override
        public void onMessageReceived(byte[] msgRecv) {
            if (msgRecv[0] == '1') { // Continue reading secondary contacts
                // Decode received message and build object.
                String phoneNumber = new String(msgRecv, 3, msgRecv[2]);
                // Update database and model
                SecondaryContactsDbHelper.create(getBaseContext(),mGuardTracker.get_id(), phoneNumber);
                mGuardTracker.addSecondaryContact(phoneNumber);
                // Send next command
                int nextContact = msgRecv[1] + 1;
                byte[] nextCmd = GuardTrackerCommands.readSecondaryContact(nextContact);
                mBleConnControl.sendCommand(nextCmd, this);
            } else {
                // There are no more secondary contacts
                // Update view
                updateSecondaryContactsView();
                Toast.makeText(getBaseContext(), R.string.refresh_secondary_contacts_success, Toast.LENGTH_LONG).show();
            }
        }

    }
    private void onClickSecondaryContactsRefresh() {
        // Delete local database
        SecondaryContactsDbHelper.delete(getBaseContext(), mGuardTracker.get_id());
        mGuardTracker.clearSecondaryContacts();

        // Send command to read first secondary contact
        byte [] cmd = GuardTrackerCommands.readSecondaryContact(1);
        mBleConnControl.sendCommand(cmd, new ReadSecondaryContactsMessageListener());
    }
    private void onClickAddSecondaryContact(final String phoneNumber) {
        // Send command to add secondary contact
        byte [] cmd = GuardTrackerCommands.addContact(phoneNumber);
        mBleConnControl.sendCommand(cmd, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msg) {
                if (Arrays.equals(msg, "1".getBytes()) == false) {
                    Toast.makeText(GuardTrackerActivity2.this.getBaseContext(),
                            R.string.dialog_add_secondary_contact_unsuccessful_write,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                // Update model
                SecondaryContactsDbHelper.create(getBaseContext(), mGuardTracker.get_id(), phoneNumber);
                mGuardTracker.addSecondaryContact(phoneNumber);
                // Update view
                updateSecondaryContactsView();
                Toast.makeText(getBaseContext(), R.string.dialog_add_secondary_contact_success, Toast.LENGTH_LONG).show();
            }
//            @Override
//            public void onMessageSent(byte[] msgSent) {
//
//            }
        });
    }
    private void onClickDelSecondaryContact(final String phoneNumber) {
        // Send command to delete secondary contact
        byte [] cmd = GuardTrackerCommands.deleteContact(phoneNumber);
        mBleConnControl.sendCommand(cmd, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msg) {
                if (Arrays.equals(msg, "1".getBytes()) == false) {
                    Toast.makeText(GuardTrackerActivity2.this.getBaseContext(),
                            R.string.dialog_delete_secondary_contact_unsuccessful_write,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                // Update model
                SecondaryContactsDbHelper.delete(getBaseContext(), mGuardTracker.get_id(), phoneNumber);
                mGuardTracker.removeSecondaryContact(phoneNumber);
                // Update view
                updateSecondaryContactsView();
                Toast.makeText(getBaseContext(), R.string.dialog_delete_secondary_contact_success, Toast.LENGTH_LONG).show();
            }
//            @Override
//            public void onMessageSent(byte[] msgSent) {
//
//            }
        });
    }
    private void onClickResyncAppWithDevice() {
        mTempGuardTracker = new GuardTracker();
        mSyncConfigWorkflow = new GuardTrackerSyncConfigWorkflow(mBleConnControl, new GuardTrackerSyncConfigListener() {
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
                monCfg.create(GuardTrackerActivity2.this.getBaseContext());
                TrackingConfiguration trackCfg = mTempGuardTracker.getTrackCfg();
                trackCfg.create(GuardTrackerActivity2.this.getBaseContext());
                VigilanceConfiguration vigilanceCfg = mTempGuardTracker.getVigCfg();
                vigilanceCfg.create(GuardTrackerActivity2.this.getBaseContext());
                Position posRef = mTempGuardTracker.getPosRef();
                if (posRef != null)
                    posRef.create(GuardTrackerActivity2.this.getBaseContext());
                List<String> secondaryContacts = mTempGuardTracker.getSecondaryContacts();
                for(String contact: secondaryContacts)
                    SecondaryContactsDbHelper.create(GuardTrackerActivity2.this.getBaseContext(), mGuardTracker.get_id(), contact);
                // Update model
                mGuardTracker.copyConfigs(mTempGuardTracker);
                // Update GuardTracker
                mGuardTracker.update(GuardTrackerActivity2.this.getBaseContext());
                Toast.makeText(getBaseContext(), R.string.dialog_resync_app_success, Toast.LENGTH_SHORT).show();
                GuardTrackerActivity2.this.recreate();
            }
        });

        mSyncConfigWorkflow.startSync();
    }
    private void onClickResetFactoryDefaults() {
        // Send command to reset configurations to factory defaults
        byte [] cmd = GuardTrackerCommands.cleanE2prom();
        mBleConnControl.sendCommand(cmd, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msg) {
                if (Arrays.equals(msg, "1".getBytes()) == false) {
                    Toast.makeText(GuardTrackerActivity2.this.getBaseContext(),
                            R.string.dialog_factory_defaults_unsuccessful_write,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                // Update database
                int id = mGuardTrackerId;
                boolean deviceEliminated = GuardTracker.delete(getBaseContext(), id);
                Toast.makeText(getBaseContext(), deviceEliminated ?
                                R.string.dialog_factory_defaults_success :
                                R.string.dialog_factory_defaults_unsuccessful_read,
                        Toast.LENGTH_LONG).show();

                GuardTrackerActivity2.this.finish();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate(Bundle savedInstanceState [" + savedInstanceState + "]);");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guard_tracker);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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

        mIdNameView                = (TextView)findViewById(R.id.identification_nickname_value);
        mIdBleAddrView             = (TextView)findViewById(R.id.identification_ble_value);
        mIdGsmAddrView             = (TextView)findViewById(R.id.identification_gsm_value);
        mIdGsmImgButton            = (ImageButton)findViewById(R.id.identification_gsm_edit_button);
        mLastMonInfoDatetimeView   = (TextView)findViewById(R.id.last_mon_info_datetime);
        mLastMonInfoTimeView       = (TextView)findViewById(R.id.last_mon_info_time);
        mLastMonInfoLatView        = (TextView)findViewById(R.id.last_mon_info_lat);
        mLastMonInfoLngView        = (TextView)findViewById(R.id.last_mon_info_lng);
        mLastMonInfoAltView        = (TextView)findViewById(R.id.last_mon_info_alt);
        mLastMonInfoSatView        = (TextView)findViewById(R.id.last_mon_info_sat);
        mLastMonInfoHdopView       = (TextView)findViewById(R.id.last_mon_info_hdop);
        mLastMonInfoFixedView      = (TextView)findViewById(R.id.last_mon_info_fixed);
        mLastMonInfoTempView       = (TextView)findViewById(R.id.last_mon_info_temp);
        mLastMonInfoSimView        = (TextView)findViewById(R.id.last_mon_info_sim);
        mLastMonInfoBatView        = (TextView)findViewById(R.id.last_mon_info_bat);
        mLastMonInfoGoogleMapsButton=(Button)  findViewById(R.id.last_mon_info_google_maps_button);
        mPosRefDatetimeView        = (TextView)findViewById(R.id.pos_ref_datetime);
        mPosRefLatView             = (TextView)findViewById(R.id.pos_ref_lat);
        mPosRefLngView             = (TextView)findViewById(R.id.pos_ref_lng);
        mPosRefAltView             = (TextView)findViewById(R.id.pos_ref_alt);
        mPosRefSatView             = (TextView)findViewById(R.id.pos_ref_sat);
        mPosRefHdopView            = (TextView)findViewById(R.id.pos_ref_hdop);
        mPosRefFixedView           = (TextView)findViewById(R.id.pos_ref_fixed);
        mPosRefRefreshButton       = (Button)  findViewById(R.id.pos_ref_refresh_button);
        mPosRefNewRefButton        = (Button)  findViewById(R.id.pos_ref_set_new_button);
        mPosRefGoogleMapsButton    = (Button)  findViewById(R.id.pos_ref_google_maps_button);
        mSensorsBleStatusView      = (TextView)findViewById(R.id.sensors_ble_status);
        mSensorsProgBleView        = (TextView)findViewById(R.id.sensors_program_ble);
        mSensorsProgRtcView        = (TextView)findViewById(R.id.sensors_program_rtc);
        mSensorsProgAccView        = (TextView)findViewById(R.id.sensors_program_acc);
        mWakeSensorsCfgButton      = (Button)  findViewById(R.id.sensors_ble_program_button);
        mWakeSensorsRefreshButton  = (Button)  findViewById(R.id.sensors_ble_refresh_button);
        mMonCfgDatetimeView        = (TextView)findViewById(R.id.mon_cfg_next_wake_datetime);
        mMonCfgDetailsButton       = (Button)  findViewById(R.id.mon_cfg_details);
        mTrackCfgDetailsButton     = (Button)  findViewById(R.id.track_cfg_details);
        mVigilanceCfgDetailsButton = (Button)  findViewById(R.id.vigilance_cfg_details);
        mMonInfoSizeEntriesView    = (TextView)findViewById(R.id.mon_info_size_entries_view);
        mMonInfoTempButton         = (Button)  findViewById(R.id.mon_info_temp_button);
        mMonInfoPosButton          = (Button)  findViewById(R.id.mon_info_pos_button);
        mMonInfoSimButton          = (Button)  findViewById(R.id.mon_info_sim_button);
        mMonInfoBatButton          = (Button)  findViewById(R.id.mon_info_bat_button);
        mTrackSessionSizeEntriesView=(TextView)findViewById(R.id.track_session_size_entries_view);
        mTrackSessionAllButton     = (Button)  findViewById(R.id.track_session_all_button);
        mTrackSessionLastButton    = (Button)  findViewById(R.id.track_session_last_button);
        mTrackSessionSelectButton  = (Button)  findViewById(R.id.track_session_select_button);
//        mNicknameEditButton        = (ImageButton)findViewById(R.id.nickname_edit_button);
//        mNicknameDoneButton        = (ImageButton)findViewById(R.id.nickname_done_button);
//        mNicknameCancelButton      = (ImageButton)findViewById(R.id.nickname_cancel_button);
        mContactsOwnerView         = (TextView)findViewById(R.id.owner_value);
        mContactsOwnerRefreshButton= (Button)  findViewById(R.id.owner_refresh_button);
        mContactsOwnerRstButton    = (Button)  findViewById(R.id.owner_rst_button);
        mContactsSecondaryViewGroup= (ViewGroup)findViewById(R.id.contacts_container);
        mContactsSecondaryEmptyView= (TextView)findViewById(R.id.empty_contacts);
        mContactsSecondaryAddButton= (Button)  findViewById(R.id.secondary_contacts_add_button);
        mContactsSecondaryRefreshButton=(Button)  findViewById(R.id.secondary_contacts_refresh_button);
        mContactsSecondaryDelButton= (Button)  findViewById(R.id.secondary_contacts_delete_button);

        mIdNameView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (mIdNameView.getRight() - mIdNameView.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        boolean editable = (Boolean)mIdNameView.getTag();
                        updateNicknameView( ! editable );
                        if (editable) {
                            mGuardTracker.setName("" + mIdNameView.getText());
                            mGuardTracker.update(GuardTrackerActivity2.this);
                        }
                        v.playSoundEffect(android.view.SoundEffectConstants.CLICK);
                        return true;
                    }
                }
                return false;
            }
        });
        mIdNameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    updateNicknameView(false);
                    mIdNameView.setText(mGuardTracker.getName());
                    handled = true;
                }
                return handled;

            }
        });
        mIdGsmImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startIdGsmAddrDialog();
            }
        });

        mMonCfgDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), MonitoringCfgActivity.class);
                int monCfgId = mGuardTracker.getMonCfgId();
                int guardTrackerId = mGuardTracker.get_id();
                intent.putExtra(GuardTrackerActivity2.CONFIG_ID, monCfgId);
                intent.putExtra(GuardTrackerActivity2.GUARD_TRACKER_ID, guardTrackerId);
                startActivity(intent);
            }
        });
        // Tracking configuration
        mTrackCfgDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), TrackingCfgActivity.class);
                int trackCfgId = mGuardTracker.getTrackCfgId();
                int guardTrackerId = mGuardTracker.get_id();
                intent.putExtra(GuardTrackerActivity2.CONFIG_ID, trackCfgId);
                intent.putExtra(GuardTrackerActivity2.GUARD_TRACKER_ID, guardTrackerId);
                startActivity(intent);
            }
        });
        // Vigilance configuration
        mVigilanceCfgDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), VigilanceCfgActivity.class);
                int vigilanceCfgId = mGuardTracker.getVigCfgId();
                int guardTrackerId = mGuardTracker.get_id();
                intent.putExtra(GuardTrackerActivity2.CONFIG_ID, vigilanceCfgId);
                intent.putExtra(GuardTrackerActivity2.GUARD_TRACKER_ID, guardTrackerId);
                startActivity(intent);
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
                intent.putExtra(GuardTrackerActivity2.MON_INFO_ITEM_SELECTED, monInfoSelected);
                intent.putExtra(GuardTrackerActivity2.GUARD_TRACKER_ID, guardTrackerId);
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
                intent.putExtra(GuardTrackerActivity2.MON_INFO_ITEM_SELECTED, monInfoSelected);
                intent.putExtra(GuardTrackerActivity2.GUARD_TRACKER_ID, guardTrackerId);
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
                intent.putExtra(GuardTrackerActivity2.MON_INFO_ITEM_SELECTED, monInfoSelected);
                intent.putExtra(GuardTrackerActivity2.GUARD_TRACKER_ID, guardTrackerId);
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
                intent.putExtra(GuardTrackerActivity2.MON_INFO_ITEM_SELECTED, monInfoSelected);
                intent.putExtra(GuardTrackerActivity2.GUARD_TRACKER_ID, guardTrackerId);
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
                intent.putExtra(GuardTrackerActivity2.GUARD_TRACKER_ID, guardTrackerId);
                intent.putExtra(GuardTrackerActivity2.GUARD_TRACKER_NAME, guardTrackerName);
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
                        Uri gmmIntentUri = Uri.parse("geo:0,0?q="+lat+","+lng+"&z=12");
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        if (mapIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(mapIntent);
                        }
                    }
                }}
        });
        mPosRefGoogleMapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Creates an Intent that will load a map of San Francisco
                Position pos = mGuardTracker.getPosRef();
                if (pos != null) {

                    double lat = pos.getLatitude();
                    double lng = pos.getLongitude();
                    Uri gmmIntentUri = Uri.parse("geo:0,0?q="+lat+","+lng+"&z=12");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(mapIntent);
                    }
                }}
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
        mWakeSensorsRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "sensorsProgramButton.onClick");
                if (isBleConnectionValid())
                    onClickWakeSensorsRefresh();
            }
        });
        mWakeSensorsCfgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "sensorsProgramButton.onClick");
                if (isBleConnectionValid())
                    startWakeSensorsConfigDialog();
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
        mHandler = new Handler();
        // Deve ser feito aqui e não no onStart porque poderemos estar a meio de uma edição do nickname
        // quando interrompemos essa edição e quando voltarmos pretendemos manter a edição.
        mIdNameView.setTag(new Boolean(false));
        updateNicknameView(false);
// Ver a definição de MyFocusChangeListener
//        mIdNameView.setOnFocusChangeListener(new MyFocusChangeListener());

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
            final String[] PERMISSIONS = new String[] {Manifest.permission.READ_SMS};
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
     * Remove next method when it is tested in GTDeviceControlActivity.
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
            if (resultCode == Activity.RESULT_CANCELED)
                Toast.makeText(getBaseContext(), "No bluetooth connection can be made without bluetooth enabled", Toast.LENGTH_LONG);
            else if (resultCode == Activity.RESULT_OK) {
                startBleScan(); // Já altera o estado de mBleSate e invalida o menu
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        Log.i(TAG, "onStart()");

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
            Position lastPosMonInfo    = Position.read(getBaseContext(), lastMonInfo.getPositionId());
            assert lastPosMonInfo != null;
            lastMonInfo.setPosition(lastPosMonInfo);
            mGuardTracker.setLastMonInfo(lastMonInfo);
        }
        mMonInfoList = MonitoringInfo.readList(getBaseContext(), mGuardTracker.get_id());
        mTrackSessionList = TrackSession.readList(getBaseContext(), mGuardTracker.get_id());
        mMonCfg = MonitoringConfiguration.read(getBaseContext(), mGuardTracker.getMonCfgId());
        mGuardTracker.setMonCfg(mMonCfg);
        mContactsSecondaryList = SecondaryContactsDbHelper.read(getBaseContext(), mGuardTracker.get_id());
        mGuardTracker.setSecondaryContacts(mContactsSecondaryList);

        // Set views state
        // Identification
        setTitle(mGuardTracker.getName());
        mIdNameView.setText(mGuardTracker.getName());
        mIdBleAddrView.setText(mGuardTracker.getBleId());
        mIdGsmAddrView.setText(mGuardTracker.getGsmId());


        // Configurations
        // Monitoring configuration
        String date;
        int periodMin = mMonCfg.getPeriodMin();
        int modMonCfg = mMonCfg.getTimeMod();
        MonitoringInfo tmpLastMonInfo = mGuardTracker.getLastMonInfo();
        if (tmpLastMonInfo != null) {
            long dateInMillis = tmpLastMonInfo.getDate().getTime();
            dateInMillis += (periodMin * 60 * 1000);
            date = String.format("%1$td/%1$tm/%1$tY %1$tR", dateInMillis); // 1$ = index og argument
        } else {
            Calendar now = Calendar.getInstance();
            if (periodMin >= 24*60) {
                int nowHour = now.get(Calendar.HOUR_OF_DAY);
                int nowMinute = now.get(Calendar.MINUTE);
                long nowInMillis = now.getTimeInMillis();
                now.set(Calendar.HOUR_OF_DAY, modMonCfg/60); // Adjust to alarm time (hours)
                now.set(Calendar.MINUTE, modMonCfg%60);      // Adjust to alarm time (minutes)
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
        mMonCfgDatetimeView.setText(date);

        // Current status
        // Last monitoring information
        updateLastMonInfoView();
        // Reference position
        updateReferencePositionView();
        // Wake sensors
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


//        SharedPreferences preferences = this.getPreferences(MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putInt(getString(R.string.saved_guard_tracker_id), guardTrackerId);
//        editor.putInt(getString(R.string.saved_guard_tracker_pos_ref_id), posRefId);
//        editor.putInt(getString(R.string.saved_guard_tracker_mon_cfg_id), monCfgId);
//        editor.putInt(getString(R.string.saved_guard_tracker_track_session_cfg_id), trackSessionCfgId);
//        editor.putInt(getString(R.string.saved_guard_tracker_vigilance_cfg_id), vigilanceCfgId);
//        editor.commit();
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
            } finally {
                dialog.dismiss();
            }
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
            PhonePickerDialogFragment dialogFragment = (PhonePickerDialogFragment)dialog;
            String devicePhoneNumber = dialogFragment.getPhoneNumber();
            dialog.dismiss();
            mTempGuardTracker.setGsmId(devicePhoneNumber);
            mSyncConfigWorkflow.workflowContinue();
            return;
        }
        if (DialogFragmentTags.SelfDeviceEliminate.toString().equals(dialogTag)/*dialog instanceof DialogGenericIcTtMs2Bt*/) {
            int id = mGuardTrackerId;
            boolean deviceEliminated = GuardTracker.delete(getBaseContext(), id);
            Toast.makeText(getBaseContext(), deviceEliminated ?
                            R.string.dialog_eliminate_devices_message_success :
                            R.string.dialog_eliminate_devices_message_unsuccessful,
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (DialogFragmentTags.SecondaryContactAdd.toString().equals(dialogTag)) {
            PhonePickerDialogFragment phonePickerDialogFragment = (PhonePickerDialogFragment)dialog;
            String phoneNumber = phonePickerDialogFragment.getPhoneNumber();
            onClickAddSecondaryContact(phoneNumber);
            dialog.dismiss();
            return;
        }
        if (DialogFragmentTags.SecondaryContactDelete.toString().equals(dialogTag)) {
            PhonePickerDialogFragment phonePickerDialogFragment = (PhonePickerDialogFragment)dialog;
            String phoneNumber = phonePickerDialogFragment.getPhoneNumber();
            onClickDelSecondaryContact(phoneNumber);
            dialog.dismiss();
            return;
        }
        if (DialogFragmentTags.ChangeOwner.toString().equals(dialogTag)) {
            PhonePickerDialogFragment phonePickerDialogFragment = (PhonePickerDialogFragment)dialog;
            String phoneNumber = phonePickerDialogFragment.getPhoneNumber();
            onClickRstOwner(phoneNumber);
            dialog.dismiss();
            return;
        }
        if (DialogFragmentTags.ChangeDevicePhoneNumber.toString().equals(dialogTag)) {
            PhonePickerDialogFragment phonePickerDialogFragment = (PhonePickerDialogFragment)dialog;
            String phoneNumber = phonePickerDialogFragment.getPhoneNumber();
            onClickChangeDevicePhoneNumber(phoneNumber);
            dialog.dismiss();
            return;
        }
    }
}
