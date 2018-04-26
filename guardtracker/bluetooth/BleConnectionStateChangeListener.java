package com.patri.guardtracker.bluetooth;

/**
 * Created by patri on 17/10/2016.
 */
public interface BleConnectionStateChangeListener {
    public void onBindServiceError();
    public void onUnableToDiscoverServices();
    public void onConnected();
    public void onReconnected();
    public void onDisconnected();
    public void onUnableToDiscoverDataService();
    public void onDataChannelDiscovered();
    public void onUnexpectedMessage(byte[] msg, int detailMsgRscId);
    public void onReady();
    public void onAuthenticated();
}
