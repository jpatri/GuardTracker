package com.patri.guardtracker.bluetooth;

import android.util.Log;

/**
 * Created by patri on 30/10/2016.
 */
public abstract class BleStateChangeAdapter implements BleConnectionStateChangeListener {
    private final String TAG = BleStateChangeAdapter.class.getSimpleName();

    @Override
    public void onBindServiceError() {
        Log.i(TAG, "OnBindServiceError");
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

    @Override
    public void onAuthenticated() {
        Log.i(TAG, "onAuthenticated");
    }

//    @Override
//    public void onMessageReceived(byte[] msg) {
//
//    }
}
