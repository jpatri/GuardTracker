package com.patri.guardtracker.synchronization;

import android.app.Activity;

import com.patri.guardtracker.communication.GuardTrackerCommands;

/**
 * Created by João Pedro Patriarca on 26/03/2018.
 */

public interface SyncConfigCmdProcessedListener {
    public void onCommandProcessed(GuardTrackerCommands.CommandValues cmdValue, GuardTrackerCommands.CmdResValues res);
}
