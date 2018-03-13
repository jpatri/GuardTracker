package com.patri.guardtracker.synchronization;

import android.content.Context;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by patri on 24/08/2016.
 */
public class GuardTrackerSyncMonProcessor extends GuardTrackerSyncProcessor {
    private static byte [][] cmds = {

    };
    private int mGuardTrackerId;
    // Array  with sequence of commands

    public GuardTrackerSyncMonProcessor(Context context, int guardTrackerId) {
        super(context);
        mGuardTrackerId = guardTrackerId;
    }


    @Override
    public void processAnsw(byte[] answ) {

    }

    @Override
    public byte[] getCmd(int i) {
        if (i < 0 || i >= cmds.length) throw new NoSuchElementException();
        return cmds[i];
    }

    @Override
    public int getCmdsSize() {
        return cmds.length;
    }
}
