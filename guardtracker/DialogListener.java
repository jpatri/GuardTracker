package com.patri.guardtracker;

import android.support.v4.app.DialogFragment;

/**
 * Created by patri on 01/11/2016.
 */
public interface DialogListener {
    public void onDialogPositiveClick(DialogFragment dialog);
    public void onDialogNegativeClick(DialogFragment dialog);
}
