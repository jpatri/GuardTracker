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

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService3 extends Service {
    private final static String TAG = BluetoothLeService3.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    // A reference counter to this service (... let's see if it works).
    // Only disconnect and unbind if this reference )
    private int mBindCountRef;
    private int mConnCountRef;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.patri.guardtracker.bluetooth.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.patri.guardtracker.bluetooth.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.patri.guardtracker.bluetooth.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.patri.guardtracker.bluetooth.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.patri.guardtracker.bluetooth.EXTRA_DATA";
    public final static String ACTION_GATT_CHARACTERISTIC_WRITTEN =
            "com.patri.guardtracker.bluetooth.ACTION_GATT_CHARACTERISTIC_WRITTEN";
    public final static String ACTION_GATT_DESCRIPTOR_WRITTEN =
            "com.patri.guardtracker.bluetooth.ACTION_GATT_DESCRIPTOR_WRITTEN";
    public final static String ACTION_GATT_RECONNECT =
            "com.patri.guardtracker.bluetooth.ACTION_GATT_RECONNECTED";

//    public final static UUID UUID_HEART_RATE_MEASUREMENT =
//            UUID.fromString(GTGattAttributes.HEART_RATE_MEASUREMENT);

    public boolean isConnected() {
        return mConnectionState == STATE_CONNECTED;
    }
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnCountRef += 1;
                Log.i(TAG, "BluetoothGattCallback.onConnectioStateChanged(): Connected to GATT server. mConnCountRef = " + mConnCountRef);
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (mConnCountRef > 0) mConnCountRef -= 1; // A REVER: foi para evitar decrementar mConnCountRef com 0
                Log.i(TAG, "BluetoothGattCallback.onConnectioStateChanged(): Disconnected from GATT server. mConnCountRef = " + mConnCountRef);
                if (mConnCountRef == 0) {
                    Log.i(TAG, "BluetoothGattCallback.onConnectioStateChanged(): send broadcastUpdate");
                    intentAction = ACTION_GATT_DISCONNECTED;
                    mConnectionState = STATE_DISCONNECTED;
                    broadcastUpdate(intentAction);
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

//        @Override
//        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                broadcastUpdate(ACTION_GATT_CHARACTERISTIC_WRITTEN, characteristic);
//            }
//        }
//
//        @Override
//        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                broadcastUpdate(ACTION_GATT_DESCRIPTOR_WRITTEN, descriptor.getCharacteristic());
//            }
//        }


    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

//    private void broadcastUpdate(final String action,
//                                 final BluetoothGattCharacteristic characteristic) {
//        final Intent intent = new Intent(action);
//
//        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
//        // carried out as per profile specifications:
//        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
//        Log.i(TAG, "broadcastUpdate(" + action + ")");
//        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
//            int flag = characteristic.getProperties();
//            int format = -1;
//            if ((flag & 0x01) != 0) {
//                format = BluetoothGattCharacteristic.FORMAT_UINT16;
//                Log.d(TAG, "Heart rate format UINT16.");
//            } else {
//                format = BluetoothGattCharacteristic.FORMAT_UINT8;
//                Log.d(TAG, "Heart rate format UINT8.");
//            }
//            final int heartRate = characteristic.getIntValue(format, 1);
//            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
//            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
//        } else {
//            // For all other profiles, writes the data formatted in HEX.
//            final byte[] data = characteristic.getValue();
//            if (data != null && data.length > 0) {
//                final StringBuilder stringBuilder = new StringBuilder(data.length);
//                for(byte byteChar : data)
//                    stringBuilder.append(String.format("%02X ", byteChar));
//                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
//            }
//        }
//        sendBroadcast(intent);
//    }
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        Log.i(TAG, "broadcastUpdate(" + action + ")");
        if (GTGattAttributes.GUARD_TRACKER_DATA_CHARACTERISTIC.equals(characteristic.getUuid().toString())) {
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                //String sdata = new String(data);
                //intent.putExtra(EXTRA_DATA, sdata );
                intent.putExtra(EXTRA_DATA, data);
                sendBroadcast(intent);
            }
        }
    }

    public class LocalBinder extends Binder {
        BluetoothLeService3 getService() {
            Log.i(TAG, "LocalBinder.getService()");
            return BluetoothLeService3.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        //mBindCountRef += 1;
        //Log.i(TAG, "onBind: mBindCountRef = " + mBindCountRef);
        Log.i(TAG, "onBind(): return local binder");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
//        mBindCountRef -= 1;
//        Log.i(TAG, "onUnbind: mBindCountRef = " + mBindCountRef);
//        if (mBindCountRef > 0 )
//            return false; // see super.onUnbind to understand the value false.
        Log.i(TAG, "onUnbind() --> this.close()");
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        Log.i(TAG, "initialize(): set mBluetoothAdapter and mBluetoothManager");
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        //mConnCountRef += 1;
        //Log.i(TAG, "connect: mConnCountRef = " + mConnCountRef);
        Log.i(TAG, "connect(): test if exists a valid mBluetoothGatt and use it otherwise create a new one (post delayed).");

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
//                mConnectionState = STATE_CONNECTING;
                mConnectionState = STATE_CONNECTED; // DEu-me a sensação que a ligação permanece connected e não connecting.
                String intentAction = ACTION_GATT_RECONNECT;
                broadcastUpdate(intentAction);
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }

////        if(TTTUtilities.isLollipopOrAbove()) {
//            // Little hack with reflect to use the connect gatt with defined transport in Lollipop
//            Method connectGattMethod = null;
//
//            try {
//                connectGattMethod = device.getClass().getMethod("connectGatt", Context.class, boolean.class, BluetoothGattCallback.class, int.class);
//            } catch (NoSuchMethodException e) {
//                e.printStackTrace();
//            }
//
//            try {
//                mBluetoothGatt = (BluetoothGatt) connectGattMethod.invoke(device, this, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (IllegalArgumentException e) {
//                e.printStackTrace();
//            } catch (InvocationTargetException e) {
//                e.printStackTrace();
//            }
////        } else  {
////            mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
////        }




        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Method connectGattMethod = null;

                try {
                    connectGattMethod = device.getClass().getMethod("connectGatt", Context.class, boolean.class, BluetoothGattCallback.class, int.class);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }

                try {
                    mBluetoothGatt = (BluetoothGatt) connectGattMethod.invoke(device, BluetoothLeService3.this, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                //mBluetoothGatt = device.connectGatt(BluetoothLeService.this, false, mGattCallback);
                Log.d(TAG, "Trying to create a new connection.");
                mBluetoothDeviceAddress = address;
                mConnectionState = STATE_CONNECTING;
            }
        });
        //mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
//        Log.d(TAG, "Trying to create a new connection.");
//        mBluetoothDeviceAddress = address;
//        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
/*        mConnCountRef -= 1;
        if (mConnCountRef > 0) {
            Log.i(TAG, "disconnect: mConnCountRef = " + mConnCountRef);
            return;
        }
        Log.i(TAG, "disconnect: mConnCountRef = 0: --> mBluetoothGatt.disconnect();");
*/        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        Log.i(TAG, "disconnect(): --> mBluetoothGatt.disconnect()");
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        //Log.i(TAG, "close: mBindCountRef = " + mBindCountRef + ": --> mBluetoothGatt.close(); mBluetoothGatt = null;");
        Log.i(TAG, "close(): --> mBluetoothGatt.close(); mBluetoothGatt = null;");
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to GuardTracker Measurement.
        // Apenas incluir o código se não conseguir comunicar com sucesso, nomeadamente, receber indicação de característica alterada.
//        List<BluetoothGattDescriptor> gattDescriptors = characteristic.getDescriptors();
//        for (BluetoothGattDescriptor gattDescriptor: gattDescriptors) {
//            Log.i(TAG, "Descriptor characteristic uuid: " + gattDescriptor.getUuid());
//        }
        String charUuid = characteristic.getUuid().toString();
        if (GTGattAttributes.GUARD_TRACKER_DATA_CHARACTERISTIC.equals(charUuid)) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(GTGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    /**
     * Retrieves a supported GATT service for the corresponding UUID service. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public BluetoothGattService getService(UUID uuid) {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getService(uuid);
    }

    /**
     * Discovers asynchronously a list of services supported by remote device (includes their characteristics and descriptors).
     */
    public boolean discoverServices() {
        if (mBluetoothGatt == null) return false;

        return mBluetoothGatt.discoverServices();
    }


    /**
     * Write characteristic value to remote BLE device.
     */
    public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothGatt == null) return false;

        return mBluetoothGatt.writeCharacteristic(characteristic);
    }
}
