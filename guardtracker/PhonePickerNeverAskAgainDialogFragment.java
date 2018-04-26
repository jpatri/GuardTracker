package com.patri.guardtracker;

import android.Manifest;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;

import com.patri.guardtracker.model.PermissionsChecker;

/**
 * Created by patri on 03/10/2016.
 */
public class PhonePickerNeverAskAgainDialogFragment extends PhonePickerDialogFragment {
    public final static String TAG = PhonePickerNeverAskAgainDialogFragment.class.getSimpleName();

    private AppCompatCheckBox mCheckBox;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        mCheckBox = new AppCompatCheckBox(this.getContext());
        mCheckBox.setText("Never ask again");
        mCheckBox.setChecked(false);
        mViewContainer.addView(mCheckBox);

        return dialog;
    }

    public boolean isNeverAskAgainChecked() {
        return mCheckBox.isChecked();
    }

}
