package com.patri.guardtracker.bluetooth;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.patri.guardtracker.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by patri on 16/10/2016.
 */
public class GuardTrackerBleConnControl {
    private static final String TAG = GuardTrackerBleConnControl.class.getSimpleName();

    private final String mDeviceAddress;
    private final String mLocalPhone;
    private BluetoothLeService mBluetoothLeService;
    /* Data channel characteristic */
    private BluetoothGattCharacteristic mDataCharacteristic;
    /* Internal states */
    private enum InternalState {
        WaitReady, Authenticating, Command
    }
    private InternalState mState;
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection;
    private BleMessageReceive mBleRxMsg;
    private BleConnectionStateChangeListener mConnListener;
    private List<BleMessageListener> mMessageListeners;
    private BleMessageListener mOneTimeShotListener;

    // To be used by close and unbind methods.
    private Context mContext;

    public GuardTrackerBleConnControl(Context context, String deviceAddr, String localPhone,
                                      BleConnectionStateChangeListener connListener,
                                      BleMessageListener msgListener) {
        mContext = context;
        mDeviceAddress = deviceAddr;
        mLocalPhone = localPhone;
        mConnListener = connListener;
        mMessageListeners = new ArrayList<>();
        if (msgListener != null)
            mMessageListeners.add(msgListener);
        mServiceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {
                mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
                if (!mBluetoothLeService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                    mConnListener.onBindServiceError();
                    return;
                }

                // Automatically connects to the device upon successful start-up initialization.
                boolean result = mBluetoothLeService.connect(mDeviceAddress);
                Log.i(TAG, "onServiceConnected: Connect to " + mDeviceAddress + "; result = " + result);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.i(TAG, "onServiceDisconnected: mBluetoothLeService = null");
                mBluetoothLeService = null;
            }
        };
        Intent gattServiceIntent = new Intent(context, BluetoothLeService.class);
        context.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        context.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.i(TAG, "GuardTrackerBleConnControl: THIS MESSAGE SHOULD NEVER APPEAR. connect to " + mDeviceAddress + "; result = " + result);
        }

    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_RECONNECTED);
        return intentFilter;
    }

    private class BleMessageReceive {
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
                byte cntAll;
                do {
                    cntAll = bytes[srcIdx];
                    srcIdx += 1;
                    // No caso de ser 0, será preferível avançar à procura de uma nova dimensão ou será preferível descartar o resto da mensagem?
                } while (cntAll == 0 && srcIdx < bytes.length);
                //if (cntAll == 0) return;
                recvCntAll = cntAll;
                recvCntCur = 0;
                values = new byte[recvCntAll];
            }
            int lenToCpy = recvCntAll - recvCntCur;
            int curLenToCpy = bytes.length - srcIdx;
            int realLenToCpy = curLenToCpy < lenToCpy ? curLenToCpy : lenToCpy; // PODEM FICAR BYTES POR PROCESSAR (caso curLenToCpy > lenToCpy
            System.arraycopy(bytes, srcIdx, values, recvCntCur, realLenToCpy);
            recvCntCur += realLenToCpy;
        }

        public boolean recvedAll() { return recvCntAll == recvCntCur; }
        public int recvLeft() {
            if (recvCntAll == -1)
                throw new IllegalStateException("This BleRecvMsg does not know it's dimension yet.");
            return recvCntAll - recvCntCur;
        }
        public byte[] getBytes() { return values; }
    }


//    static private void delay(int milis) {
//        try {
//            Thread.sleep(milis);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
    private static byte[] appMsgToBleMsg(byte[] s) {
//        byte [] bleMsg = new byte[s.length+1];
//        byte len = (byte) s.length;
//        bleMsg[0] = len;
//        System.arraycopy(s, 0, bleMsg, 1, len);
//        return bleMsg;
        return s;
    }


    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_RECONNECTED: reconnected to a GATT server. New Intent filter introduced by Patri
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
                if (!mBluetoothLeService.discoverServices()) {
                    Log.e(TAG, "Unable to discover BLE services");
                    mConnListener.onUnableToDiscoverServices();
                    return;
                }
                mConnListener.onConnected();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnListener.onDisconnected();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Search for data service to authenticate with BLE device
                BluetoothGattService dataService  = mBluetoothLeService.getService(UUID.fromString(GTGattAttributes.GUARD_TRACKER_DATA_CHANNEL_SERVICE));
                if (dataService == null) {
                    Log.e(TAG, "The connected BLE doesn't implement the Data Channel Service");
                    mConnListener.onUnableToDiscoverDataService();
                    return;
                }
                mDataCharacteristic = dataService.getCharacteristic(UUID.fromString(GTGattAttributes.GUARD_TRACKER_DATA_CHARACTERISTIC));
                mBluetoothLeService.setCharacteristicNotification(mDataCharacteristic, true);
                mBleRxMsg = new BleMessageReceive();

                // Wait for device to be ready
                mState = InternalState.WaitReady;
                mConnListener.onDataChannelDiscovered();

            } else if (BluetoothLeService.ACTION_GATT_RECONNECTED.equals(action)) {
                BluetoothGattService dataService  = mBluetoothLeService.getService(UUID.fromString(GTGattAttributes.GUARD_TRACKER_DATA_CHANNEL_SERVICE));
                if (dataService == null) {
                    Log.e(TAG, "The connected BLE doesn't implement the Data Channel Service");
                    mConnListener.onUnableToDiscoverDataService();
                    return;
                }
                mDataCharacteristic = dataService.getCharacteristic(UUID.fromString(GTGattAttributes.GUARD_TRACKER_DATA_CHARACTERISTIC));
                //mBluetoothLeService.setCharacteristicNotification(mDataCharacteristic, true);
                mBleRxMsg = new BleMessageReceive();

                // Wait for device to be ready
                mState = InternalState.Command;
                mConnListener.onReconnected();

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                byte [] answ = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                try {
                    mBleRxMsg.addBytes(answ);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Receive to much characters from BLE. Continue receiving.");
                    mBleRxMsg = new BleMessageReceive();
                }
                if (mBleRxMsg.recvedAll() == true) {

                    byte[] recv = mBleRxMsg.getBytes();
                    mBleRxMsg = new BleMessageReceive();

                    switch (mState) {
                        case WaitReady:
                            if (Arrays.equals(recv, "1".getBytes()) == false) {
                                // Este código é executado uma vez (misteriosamente) porque, creio,
                                // o método mBluetoothLeService.setCharacteristicNotification provaca uma notificação.
                                String dumped = dumpMsg(recv);
                                Log.e(TAG, "GattUpdateReceiver: in WaitReady state and expect to receive ('1') and get:" + dumped);
                                Log.i(TAG, "GattUpdateReceiver: the last erroneous reception will not have impact in connection establishment");
                                //mConnListener.onUnexpectedMessage(recv, R.string.unknown_device);
                                break;
                            }
                            // Authenticate with BLE device: write local phonenumber to remote device
                            String phoneNumber = mLocalPhone;
                            int phoneNumberLength = mLocalPhone.length();
                            byte[] message = new byte[phoneNumberLength+1];
                            System.arraycopy(phoneNumber.getBytes(), 0, message, 1, phoneNumberLength);
                            message[0] = (byte) phoneNumberLength;
                            writeBytes(message);
                            mState = InternalState.Authenticating;
                            mConnListener.onReady();
                            break;
                        case Authenticating:
                            if (Arrays.equals(recv, "1".getBytes()) == false) {
                                Log.e(TAG, "The remote device is paired with another phonenumber");
                                mConnListener.onUnexpectedMessage(recv, R.string.unknown_owner);
                                break;
                            }
                            mState = InternalState.Command;
                            mConnListener.onAuthenticated();
                            break;

                        case Command:
                            for (BleMessageListener listener: mMessageListeners) {
                                listener.onMessageReceived(recv);
                            }
//                            if (mMessageListener != null)
//                                mMessageListener.onMessageReceived(recv);
                            if (mOneTimeShotListener != null) {
                                BleMessageListener cacheTmpListener = mOneTimeShotListener;
                                // It must be done before call onMessageReceived.
                                // The method onMessageReceived may send another ble command and consequently set another mTempListener.
                                mOneTimeShotListener = null;
                                cacheTmpListener.onMessageReceived(recv);
                                break;
                            }
                            break;
                    }
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

    public void writeBytes(byte[] message) {
        byte[] message1 = appMsgToBleMsg(message);
        mDataCharacteristic.setValue(message1);
        mBluetoothLeService.writeCharacteristic(mDataCharacteristic);
        Log.i(TAG, "Ble send bytes (body message); " + dumpMsg(message));
    }

    public void connect() {
        Log.i(TAG, "connect() --> mBluetoothLeService.connect(" + mDeviceAddress + ")");
        mBluetoothLeService.connect(mDeviceAddress);
    }
    public boolean isConnected() {
        if (mBluetoothLeService == null)
            return false;
        return mBluetoothLeService.isConnected();
    }
    public void disconnect() {
        Log.i(TAG, "disconnect() --> mBluetoothLeService.disconnect()");
        mBluetoothLeService.disconnect();
    }
    public void close() {
//        Log.i(TAG, "close(): --> mBluetoothService.disconnect();");
//        mBluetoothLeService.disconnect(); // COLOQUEI ESTA LINHA DE CÓDIGO. SE ESTIVER CORRECTA ESTA INSERÇÃO ENTÃO DEVEREI MOVER a linha de código seguinte para o momento em que é notificado o _disconnect (caso exista esta notificação).
        // Sem grandes certezas, voltei a colocar a chamada ao disconnect (parece que no original - pelos comentários anteriores - existia a chamada ao disconnect).
        disconnect();
        Log.i(TAG, "close(): --> mContext.unregisterReceiver(mGattUpdateReceiver)");
        mContext.unregisterReceiver(mGattUpdateReceiver);
        unbind();
    }
    private void unbind() {
        Log.i(TAG, "unbind(): --> mContext.unbindService(mServiceConnection); mBluetoothLeService = null");
        mContext.unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }
    public void addMessageListener(BleMessageListener listener) {
        mMessageListeners.add(listener);
    }
    public void removeMessageListener(BleMessageListener listener) {
        mMessageListeners.remove(listener);
    }

//    public void sendCommand(byte[] cmd, BleConnectionStateChangeListener listener) {
//        mTempListener = listener;
//        writeBytes(cmd);
//    }
    public void sendCommand(byte[] cmd, BleMessageListener listener) {
        mOneTimeShotListener = listener;
        writeBytes(cmd);
    }
    public void sendLongRunningCommand(byte[] cmd, BleMessageListener listener) {
        // must add listener to collection of listeners.
        // The problem to resolve is to select the place where to put the remove of the listener.
        // Must be done only after OK or KO result is received from device.
    }

    /**
     * Dump a binary message. This message is originated from or to ble device.
     * Return a string with the dumpped message.
     */
    static public String dumpMsg(byte [] data) {
        StringBuilder sbHex = new StringBuilder(data.length * 3);
        StringBuilder sbAscii = new StringBuilder(data.length);
        int i = 0;
        while ( i < data.length ) {
            int digit = data[i] & 0xFF;
            sbHex.append(String.format("%02x ", digit));
            sbAscii.append((char)(Character.isLetterOrDigit(digit) ? digit : '.'));
            i += 1;
        }
        String dumped = sbHex.toString() + "ascii: " + sbAscii.toString();
        return dumped;
    }



}
