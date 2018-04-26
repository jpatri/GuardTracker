/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.patri.guardtracker.bluetooth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.patri.guardtracker.DialogListener;
import com.patri.guardtracker.GuardTrackerActivity;
import com.patri.guardtracker.MainActivity;
import com.patri.guardtracker.PhonePickerDialogFragment;
import com.patri.guardtracker.R;
import com.patri.guardtracker.model.GuardTracker;
import com.patri.guardtracker.model.MonitoringConfiguration;
import com.patri.guardtracker.model.Position;
import com.patri.guardtracker.model.TrackingConfiguration;
import com.patri.guardtracker.model.VigilanceConfiguration;
import com.patri.guardtracker.synchronization.SyncConfigFinishListener;
import com.patri.guardtracker.synchronization.SyncConfigFromDevListener;
import com.patri.guardtracker.synchronization.SyncConfigWorkflow;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class GuardTrackerBleControlActivityFromDev extends AppCompatActivity
                                            implements DialogListener,
        BleConnectionStateChangeListener,
        BleMessageListener,
        SyncConfigFromDevListener,
        SyncConfigFinishListener
{
    private final static String TAG = GuardTrackerBleControlActivityFromDev.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "GuardTrackerBleControlActivityFromDev.DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "GuardTrackerBleControlActivityFromDev.DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE_OWNER_PHONE = "GuardTrackerBleControlActivityFromDev.OWNER_PHONE";

    private TextView mConnectionState;
    private TextView mDataField;
    private TextView mSyncLogArea;
    private String mDeviceName;
    private String mDeviceAddress;
    private String mOwnerPhone;
    private boolean mConnected = false;
    private GuardTrackerBleConnControl mBleConnControl;

    private GuardTracker mGuardTracker;
    private SyncConfigWorkflow mSyncWorkflow;

//    private MonitoringConfiguration mMonCfg;
//    private TrackingConfiguration mTrackCfg;
//    private VigilanceConfiguration mVigilanceCfg;
//    private String mDevicePhoneNumber;
//    private Position mPosRef;
//    private int mWakeSensorsCfgStatus;
//    private ArrayList<String> mSecondaryContacts = new ArrayList<>();

//    private GuardTrackerSyncProcessor mSyncProcessor;
//    private GuardTrackerSyncCfgProcessor mSyncCfgProcessor;
//    private GuardTrackerSyncMonProcessor mSyncMonProcessor;

    /*
     * BleConnectionStateChangeListener implementation
     */
    @Override
    public void onBindServiceError() {
        finish();
    }
    @Override
    public void onUnableToDiscoverServices() {
        finish();
    }
    @Override
    public void onConnected() {
        mConnected = true;
        updateConnectionState(R.string.connected);
        updateLogArea("Connected");
        invalidateOptionsMenu();
    }
    @Override
    public void onReconnected() {
        mConnected = true;
        updateConnectionState(R.string.connected);
        updateLogArea("Connected");
        invalidateOptionsMenu();
    }

    @Override
    public void onDisconnected() {
        mConnected = false;
        updateConnectionState(R.string.disconnected);
        updateLogArea("Disconnected");
        invalidateOptionsMenu();
        clearUI();
    }
    @Override
    public void onUnableToDiscoverDataService() {
        finish();
    }
    @Override
    public void onDataChannelDiscovered() {
        updateLogArea("Data channel discovered");
        updateConnectionState(R.string.wait_device_ready);
    }
    @Override
    public void onUnexpectedMessage(byte[] msg, int detailMsgRscId) {
        Toast.makeText(this.getBaseContext(), detailMsgRscId, Toast.LENGTH_SHORT);
        // Any thing else to do ?? Must be verified.
    }
    @Override
    public void onReady() {
        updateConnectionState(R.string.authenticating);
    }
//    @Override
//    public void onAuthenticated() {
//        updateLogArea("Authenticated");
//        int gtId;
//        try {
//            gtId = GuardTracker.bleAddressToId(this.getBaseContext(), mDeviceAddress);
//            mSyncProcessor = new GuardTrackerSyncMonProcessor(getBaseContext(), gtId);
//            updateLogArea("Device already paired");
//            updateLogArea("Sync monitoring info");
//        } catch (IllegalArgumentException e) {
//            // First synchronization
//            mSyncProcessor = new GuardTrackerSyncCfgProcessor(getBaseContext());
//            updateLogArea("Pair with new device");
//            updateLogArea("Sync first time");
//        }
//
//        if (mSyncProcessor.hasNext()) {
//            byte[] cmd = mSyncProcessor.next();
//            mBleConnControl.writeBytes(cmd);
//            updateLogArea("--> " + toPrettyString(cmd));
//        }
//        updateConnectionState(R.string.syncing);
//
//    }
    @Override
    public void onAuthenticated() {
        updateLogArea("Authenticated");
        int gtId;
        try {
            gtId = GuardTracker.readByBleAddress(this.getBaseContext(), mDeviceAddress).get_id();
            //mSyncProcessor = new GuardTrackerSyncMonProcessor(getBaseContext(), gtId);
            updateLogArea("Device already paired");
            updateLogArea("Sync monitoring info");
            Toast.makeText(getBaseContext(), R.string.ble_authenticated_success, Toast.LENGTH_SHORT).show();
            startGuardTrackerView(gtId);

        } catch (Exception e) {
            // First synchronization
            mSyncWorkflow = new SyncConfigWorkflow(mBleConnControl, this, this);
            updateLogArea("Pair with new device");
            updateLogArea("Sync first time");
            mGuardTracker = new GuardTracker();
            mGuardTracker.setName(mDeviceName);
            mGuardTracker.setBleId(mDeviceAddress);
            mGuardTracker.setOwnerPhoneNumber(mOwnerPhone);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSyncWorkflow.startCompleteSyncFromDev();
                }
            }, 200);
            updateConnectionState(R.string.syncing);
        }
    }

    /*
     * SyncConfigFromDevListener implementation
     */
    @Override
    public void onDevicePhoneNumberReceived(String devicePhoneNumber) {
        if (devicePhoneNumber.length() == 0) {
            updateLogArea("Received device phone number with length 0");

            PhonePickerDialogFragment phoneDialog = new PhonePickerDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(PhonePickerDialogFragment.ICON_ID_KEY, R.drawable.ic_pair_new_link);
            bundle.putInt(PhonePickerDialogFragment.TITLE_ID_KEY, R.string.dialog_device_phone_number_title);
            bundle.putInt(PhonePickerDialogFragment.MESSAGE_ID_KEY, R.string.dialog_device_phone_number_message_body);
            bundle.putInt(PhonePickerDialogFragment.YES_BUTTON_LABEL_ID_KEY, R.string.done_button_label);
            bundle.putInt(PhonePickerDialogFragment.NO_BUTTON_LABEL_ID_KEY, R.string.cancel_button_label);
            phoneDialog.setArguments(bundle);
            phoneDialog.show(getSupportFragmentManager(), "PhonePickerDialogFragment");
            return;
        }
        mGuardTracker.setGsmId(devicePhoneNumber);
        updateLogArea("Received device phone number: " + devicePhoneNumber);
    }

    @Override
    public void onMonitoringConfigReceived(MonitoringConfiguration monCfg) {
        mGuardTracker.setMonCfg(monCfg);
        updateLogArea("Received monitoring configuration");
    }

    @Override
    public void onTrackingConfigReceived(TrackingConfiguration trackCfg) {
        mGuardTracker.setTrackCfg(trackCfg);
        updateLogArea("Received tracking configuration");
    }

    @Override
    public void onVigilanceConfigReceived(VigilanceConfiguration vigilanceCfg) {
        mGuardTracker.setVigilanceCfg(vigilanceCfg);
        updateLogArea("Received vigilance configuration");
    }

    @Override
    public void onPositionReferenceReceived(Position posRef) {
        if (posRef != null) {
            mGuardTracker.setPosRef(posRef);
            updateLogArea("Received reference position");
            return;
        }
        updateLogArea("Received NO reference position");
    }

    @Override
    public void onWakeSensorsReceived(int wakeSensorsCfgStatus) {
        mGuardTracker.setWakeSensors(wakeSensorsCfgStatus);
        updateLogArea("Received wake sensors cfg status: " + wakeSensorsCfgStatus);
    }

    @Override
    public void onSecondaryContactReceived(String secondaryPhoneNumber) {
        mGuardTracker.addSecondaryContact(secondaryPhoneNumber);
        updateLogArea("Received secondary phone number: " + secondaryPhoneNumber);
    }

    @Override
    public void onFinishSyncConfig() {
        updateLogArea("Finish synchronization with device");
        updateConnectionState(R.string.synchronized_);
        // Create entries in database
        mGuardTracker.create(getBaseContext());

        Toast.makeText(getBaseContext(), R.string.dialog_toast_ok, Toast.LENGTH_SHORT).show();
        startGuardTrackerView(mGuardTracker.get_id());
    }

    /*
     * BleMessageListener implementation
     */
    @Override
    public void onMessageReceived(byte[] recv) {
        updateLogArea("<-- " + toPrettyString(recv));
    }
//    @Override
//    public void onMessageSent(byte [] sent) {
//        updateLogArea("--> " + toPrettyString(sent));
//    }

    private void startGuardTrackerView(int guardTrackerId) {
        // Goto GuardTracker view
        Intent intent = new Intent(GuardTrackerBleControlActivityFromDev.this, GuardTrackerActivity.class);
        intent.putExtra(MainActivity.GUARD_TRACKER_ID, guardTrackerId);
        startActivity(intent);
    }

    private void clearUI() {
        //mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        mOwnerPhone = intent.getStringExtra(EXTRAS_DEVICE_OWNER_PHONE);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);
        mSyncLogArea = (EditText) findViewById(R.id.syncEditText);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);

        //Toolbar toolbar = new Toolbar(getBaseContext());
        //setActionBar(toolbar);
        getSupportActionBar().setTitle(mDeviceName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mBleConnControl = new GuardTrackerBleConnControl(this.getBaseContext(), mDeviceAddress, mOwnerPhone, this, this);
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop()");
        super.onStop();
        if (mBleConnControl != null) {
            mBleConnControl.close(); // Retirei do onPause porque a criação está a ocorrer no onCreate. Na realidade, depois de consultar a fig com o ciclo de vida da activity, verifiquei que a criação devia estar no onResume.
            mBleConnControl = null;
        }
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause()");
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBleConnControl.connect();
                return true;
            case R.id.menu_disconnect:
                mBleConnControl.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateLogArea(final String log) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSyncLogArea.append(log + '\n');
            }
        });
    }
    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    // Este displayData a ser executado pela uiThread foi inserido por mim.
//    private void displayData(final String data) {
//        if (data != null) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mDataField.setText(data);
//                }
//            });
//        }
//    }

    private String toPrettyString(byte [] data) {
        final StringBuilder stringBuilder = new StringBuilder();
        for(byte byteChar : data)
            stringBuilder.append(String.format("%02X ", byteChar));


        String text = stringBuilder.toString();

        return text;
    }
    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
        }
    }
    private void displayData(byte [] data) {

        final StringBuilder stringBuilder = new StringBuilder();
        final StringBuilder asciiBuilder = new StringBuilder();
        for(byte byteChar : data) {
            stringBuilder.append(String.format("%02X ", byteChar));
            char charChar = Character.isJavaIdentifierPart(byteChar) ? (char)byteChar : '.';
            asciiBuilder.append(String.format("%c", charChar));
        }

        stringBuilder.append("   [").append(asciiBuilder).append(']');
        String text = stringBuilder.toString();
        mDataField.setText(text);
    }


    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        dialog.dismiss();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        String phoneNumber = ((PhonePickerDialogFragment)dialog).getPhoneNumber();
        dialog.dismiss();
        mGuardTracker.setGsmId(phoneNumber);
        updateLogArea("Device phone number: " + phoneNumber);
        mSyncWorkflow.workflowContinue();
    }
}
