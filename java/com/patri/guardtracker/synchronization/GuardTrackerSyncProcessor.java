package com.patri.guardtracker.synchronization;

import android.content.Context;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by patri on 24/08/2016.
 */
public abstract class GuardTrackerSyncProcessor implements Iterator<byte []> {

    protected Context context;

    public GuardTrackerSyncProcessor(Context context) {
        this.context = context;
    }

    /**
     * Process message received from BLE device.
     * This message corresponds to the answer to command cmdId.
     * @param answ message received from BLE device
     */
    public abstract void    processAnsw(byte [] answ);
    public abstract int     getCmdsSize();
    public abstract byte[]  getCmd(int i);

    private int it = 0;

    /**
     * This method is useful for identifying the command in processAnsw implementation.
     * @return
     */
    protected final int getIt() { return it; }

    @Override
    public boolean hasNext() {
        return ! (getCmdsSize() == it);
    }

    @Override
    public byte[] next() {
        if (hasNext() == false) throw new NoSuchElementException();
        byte[] cmd = getCmd(it);
        it = it + 1;
        return cmd;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }


}
