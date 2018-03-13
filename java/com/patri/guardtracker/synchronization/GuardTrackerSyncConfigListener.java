package com.patri.guardtracker.synchronization;

import com.patri.guardtracker.model.MonitoringConfiguration;
import com.patri.guardtracker.model.Position;
import com.patri.guardtracker.model.TrackingConfiguration;
import com.patri.guardtracker.model.VigilanceConfiguration;

/**
 * Created by patri on 10/11/2016.
 */
public interface GuardTrackerSyncConfigListener {
    void onMonitoringConfigReceived(MonitoringConfiguration monCfg);
    void onTrackingConfigReceived(TrackingConfiguration trackCfg);
    void onVigilanceConfigReceived(VigilanceConfiguration vigilanceCfg);
    void onDevicePhoneNumberReceived(String devicePhoneNumber);
    void onPositionReferenceReceived(Position posRef);
    void onWakeSensorsReceived(int wakeSensorsCfgStatus);
    void onSecondaryContactReceived(String secondaryPhoneNumber);
    void onFinishSyncConfig();
}
