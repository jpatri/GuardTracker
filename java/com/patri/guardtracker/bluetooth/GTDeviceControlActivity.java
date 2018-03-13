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

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.patri.guardtracker.DialogListener;
import com.patri.guardtracker.GuardTrackerActivity;
import com.patri.guardtracker.MainActivity;
import com.patri.guardtracker.PhonePickerDialogFragment;
import com.patri.guardtracker.R;
import com.patri.guardtracker.model.GuardTracker;
import com.patri.guardtracker.synchronization.GuardTrackerSyncCfgProcessor;
import com.patri.guardtracker.synchronization.GuardTrackerSyncMonProcessor;
import com.patri.guardtracker.synchronization.GuardTrackerSyncProcessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class GTDeviceControlActivity extends AppCompatActivity implements DialogListener {
    private final static String TAG = GTDeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "GTDeviceControlActivity.DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "GTDeviceControlActivity.DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE_OWNER_PHONE = "GTDeviceControlActivity.LOCAL_PHONE";
    public static final int PICK_PHONE_NUMBER_REQUEST = 1;

    private TextView mConnectionState;
    private TextView mDataField;
    private TextView mSyncLogArea;
    private String mDeviceName;
    private String mDeviceAddress;
    private String mOwnerPhone;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    /* Data channel characteristic */
    private BluetoothGattCharacteristic mDataCharacteristic;

    /* Internal states */
    private enum InternalState {
        WaitReady, Authenticating, SyncingMon, SyncingCfg, Command
    }
    private InternalState mState;
    private GuardTracker mGuardTracker;

    private class BleReceiveMessage {
        /* This buffer contains only the payload data of message. It does not contains the length. */
        private byte [] values;
        private int recvCntCur = 0;
        private int recvCntAll = -1;

        public void addBytes(byte[] bytes) {
            if (bytes.length == 0) return;
            if (recvedAll())
                throw new IllegalStateException("This BleRecvMsg already received all bytes.");

            int srcIdx = 0;
            if (recvCntAll == -1) {
                byte cntAll = bytes[0];
                //if (cntAll == 0) return;
                recvCntAll = cntAll;
                recvCntCur = 0;
                values = new byte[recvCntAll];
                srcIdx = 1;
            }
            System.arraycopy(bytes, srcIdx, values, recvCntCur, bytes.length - srcIdx);
            recvCntCur += bytes.length - srcIdx;
        }

        public boolean recvedAll() { return recvCntAll == recvCntCur; }
        public int recvLeft() {
            if (recvCntAll == -1)
                throw new IllegalStateException("This BleRecvMsg does not know it's dimension yet.");
            return recvCntAll - recvCntCur;
        }
        public byte[] getBytes() { return values; }
    }

    private BleReceiveMessage mBleRxMsg;
    private GuardTrackerSyncProcessor mSyncProcessor;
    private GuardTrackerSyncCfgProcessor mSyncCfgProcessor;
    private GuardTrackerSyncMonProcessor mSyncMonProcessor;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

            // Automatically connects to the device upon successful start-up initialization.
            Log.i(TAG, "Connect to " + mDeviceAddress);
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;

        }
    };

    private String getOwnerPhonenumber() {
//        TelephonyManager tMgr = (TelephonyManager)GTDeviceControlActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
//        String phoneNumber = tMgr.getLine1Number();
//        if (phoneNumber == null) {
//            // Activity for result to get local phonenumber from user.
//        }
        return mOwnerPhone;
    }

    private static byte[] appMsgToBleMsg(byte[] s) {
        byte [] bleMsg = new byte[s.length+1];
        byte len = (byte) s.length;
        bleMsg[0] = len;
        System.arraycopy(s, 0, bleMsg, 1, len);
        return bleMsg;
    }

    static private void delay(int milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.i(TAG, "gattUpdateBroadcastReceiver with action " + action);
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                updateLogArea("Connected");
                invalidateOptionsMenu();
                if (!mBluetoothLeService.discoverServices()) {
                    Log.e(TAG, "Unable to discover BLE services");
                    finish();
                }
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                updateLogArea("Disconnected");
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Search for data service to authenticate with BLE device
                BluetoothGattService dataService  = mBluetoothLeService.getService(UUID.fromString(GTGattAttributes.GUARD_TRACKER_DATA_CHANNEL_SERVICE));
                if (dataService == null) {
                    Log.e(TAG, "The connected BLE doesn't implement the Data Channel Service");
                    finish();
                }

                updateLogArea("Services discovered");

                // Initialize some stufs
                mDataCharacteristic = dataService.getCharacteristic(UUID.fromString(GTGattAttributes.GUARD_TRACKER_DATA_CHARACTERISTIC));
                mNotifyCharacteristic = mDataCharacteristic;
                mBluetoothLeService.setCharacteristicNotification(mDataCharacteristic, true);
                mBleRxMsg = new BleReceiveMessage();

                // Wait for device to be ready
                mState = InternalState.WaitReady;
                updateConnectionState(R.string.wait_device_ready);

                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                byte [] answ = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                displayData(answ);
                try {
                    mBleRxMsg.addBytes(answ);
                } catch (IllegalStateException e) {
                    Toast.makeText(getBaseContext(), "Receive to mutch characters from BLE. Continue receiving.", Toast.LENGTH_LONG).show();
                    mBleRxMsg = new BleReceiveMessage();
                }
                if (mBleRxMsg.recvedAll() == true) {

                    byte[] recv = mBleRxMsg.getBytes();

                    switch (mState) {
                        case WaitReady:
                            if (Arrays.equals(recv, "1".getBytes()) == false) {
                                Toast.makeText(GTDeviceControlActivity.this.getBaseContext(), R.string.unknown_owner, Toast.LENGTH_SHORT);
                                Log.e(TAG, "The remote device does ot seem to be a GuardTracker device. Expecting a ready to receive (1) and get a 0.");
                                break;
                                //finish();
                            }
                            // Authenticate with BLE device: write local phonenumber to remote device
                            String phoneNumber = getOwnerPhonenumber();
                            byte[] message = appMsgToBleMsg(phoneNumber.getBytes());
                            mDataCharacteristic.setValue(message);
                            mBluetoothLeService.writeCharacteristic(mDataCharacteristic);
                            mState = InternalState.Authenticating;
                            updateConnectionState(R.string.authenticating);
                            break;
                        case Authenticating:
                            if (Arrays.equals(recv, "1".getBytes()) == false) {
                                Toast.makeText(GTDeviceControlActivity.this.getBaseContext(), R.string.unknown_owner, Toast.LENGTH_SHORT);
                                Log.e(TAG, "The remote device is paired with another phonenumber");
                                finish();
                            }
                            updateLogArea("Authenticated");
                            int gtId;
                            try {
                                gtId = GuardTracker.readByBleAddress(GTDeviceControlActivity.this.getBaseContext(), mDeviceAddress).get_id();
                                mState = InternalState.SyncingMon;
                                mSyncProcessor = new GuardTrackerSyncMonProcessor(getBaseContext(), gtId);
                                updateLogArea("Device already paired");
                                updateLogArea("Sync monitoring info");
                            } catch (IllegalArgumentException e) {
                                // First synchronization
                                mState = InternalState.SyncingCfg;
                                mSyncProcessor = new GuardTrackerSyncCfgProcessor(getBaseContext());
                                updateLogArea("Pair with new device");
                                updateLogArea("Sync first time");
                            }

                            if (mSyncProcessor.hasNext()) {
                                byte[] cmd = mSyncProcessor.next();
                                byte[] message1 = appMsgToBleMsg(cmd);
                                mDataCharacteristic.setValue(message1);
                                mBluetoothLeService.writeCharacteristic(mDataCharacteristic);
                                updateLogArea("--> " + toPrettyString(cmd));
                            }

                            updateConnectionState(R.string.syncing);
                            break;
                        case SyncingCfg:
                            mSyncProcessor.processAnsw(recv);
                            updateLogArea("<-- " + toPrettyString(recv));

                            if (mSyncProcessor.hasNext()) {

                                delay(100);

                                byte[] cmd = mSyncProcessor.next();
                                byte[] message1 = appMsgToBleMsg(cmd);
                                mDataCharacteristic.setValue(message1);
                                mBluetoothLeService.writeCharacteristic(mDataCharacteristic);
                                updateLogArea("--> " + toPrettyString(cmd));

                            } else {
                                mSyncCfgProcessor = (GuardTrackerSyncCfgProcessor) mSyncProcessor;
                                if (mSyncCfgProcessor.getGsmPhonenumber() == null) {
                                    PhonePickerDialogFragment phoneDialog = new PhonePickerDialogFragment();
                                    Bundle bundle = new Bundle();
                                    bundle.putInt(PhonePickerDialogFragment.ICON_ID_KEY, R.drawable.ic_pair_new_link);
                                    bundle.putInt(PhonePickerDialogFragment.MESSAGE_ID_KEY, R.string.dialog_device_phone_number_message_body);
                                    phoneDialog.setArguments(bundle);
                                    phoneDialog.show(getSupportFragmentManager(), "PhonePickerDialogFragment");
                                    return;
                                }
                                createGuardTrackerAndGoToSelfView();
                            }
                            break;
                        case SyncingMon:
                            mSyncProcessor.processAnsw(recv);
                            updateLogArea("<-- " + toPrettyString(recv));

                            if (mSyncProcessor.hasNext()) {

                                delay(100);

                                byte[] cmd = mSyncProcessor.next();
                                byte[] message1 = appMsgToBleMsg(cmd);
                                mDataCharacteristic.setValue(message1);
                                mBluetoothLeService.writeCharacteristic(mDataCharacteristic);
                                updateLogArea("--> " + toPrettyString(cmd));

                            } else {
                                // Goto GuardTracker view
                                int pk = GuardTracker.readByBleAddress(getBaseContext(), mDeviceAddress).get_id();
                                goToGuardTrackerView(pk);
                            }
                            break;
                        case Command:
                            // ToDo
                            break;
                    }
                    mBleRxMsg = new BleReceiveMessage();
                }

            } else if (BluetoothLeService.ACTION_GATT_CHARACTERISTIC_WRITTEN.equals(action)) {
                Log.i(TAG, "BLE characteristic written: success written");
                // ToDo
            } else if (BluetoothLeService.ACTION_GATT_DESCRIPTOR_WRITTEN.equals(action)) {
                Log.i(TAG, "BLE descriptor written: success written");
                // ToDo
            }
        }
    };

    private void goToGuardTrackerView(int guardTrackerId) {
        // Goto GuardTracker view
        Intent intent = new Intent(GTDeviceControlActivity.this, GuardTrackerActivity.class);
        intent.putExtra(MainActivity.GUARD_TRACKER_ID, guardTrackerId);
        startActivity(intent);
    }
    private void createGuardTrackerAndGoToSelfView() {
        GuardTracker guardTracker = new GuardTracker(
                mDeviceName,
                mDeviceAddress,
                mSyncCfgProcessor.getGsmPhonenumber(),
                mOwnerPhone,
                mSyncCfgProcessor.getWakeSensorsStatus(),
                null,
                null,
                mSyncCfgProcessor.getMonCfg(),
                mSyncCfgProcessor.getTrackingCfg(),
                mSyncCfgProcessor.getVigilanceCfg(),
                true, null
        );
        guardTracker.create(getBaseContext());
        goToGuardTrackerView(guardTracker.get_id());
    }

    private void clearUI() {
        //mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBluetoothLeService.disconnect(); // COLOQUEI ESTA LINHA DE CÓDIGO. SE ESTIVER CORRECTA ESTA INSERÇÃO ENTÃO DEVEREI MOVER a linha de código seguinte para o momento em que é notificado o _disconnect (caso exista esta notificação).
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
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
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
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


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        Log.i(TAG, "onDialogNegativeClick --> Nothing done. Must be done anything");
        dialog.dismiss();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        Log.i(TAG, "onDialogPositiveClick --> GuardTrackerActivity");
        String phonenumber = ((PhonePickerDialogFragment)dialog).getPhoneNumber();
        dialog.dismiss();
        Toast.makeText(getBaseContext(), R.string.dialog_toast_ok, Toast.LENGTH_SHORT).show();
        mSyncCfgProcessor.setGsmPhonenumber(phonenumber);
        createGuardTrackerAndGoToSelfView();

    }
}
