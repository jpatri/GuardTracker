package com.patri.guardtracker;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TimePicker;

/**
 * Created by patri on 20/11/2016.
 */
public class CfgTimePickerDialogFragment extends DialogGenericIcTtMs2Bt {
    public final static String TAG = CfgTimePickerDialogFragment.class.getSimpleName();
    public final static String CFG_CURRENT_TIME_IN_MINUTES_ARG = "com.patri.guardtracker.CfgTimePickerDialogFragment.CFG_CURRENT_TIME_IN_MINUTES_ARG";

    protected ViewGroup mViewContainer;
    private TimePicker mTimePicker;

    public int getNewTime() {
        int currentTime = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            currentTime = mTimePicker.getHour() * 60;
            currentTime += mTimePicker.getMinute();

        }
        return currentTime;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);


        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mViewContainer = (ViewGroup)inflater.inflate(R.layout.fragment_cfg_time_picker, null);
        mAlertDialog.setView(mViewContainer);

        mTimePicker = mViewContainer.findViewById(R.id.numberPicker);

        Bundle bundle = getArguments();
        int currentTime = bundle.getInt(CfgTimePickerDialogFragment.CFG_CURRENT_TIME_IN_MINUTES_ARG);
        mTimePicker.setHour(currentTime/60);
        mTimePicker.setMinute(currentTime%60);
        mTimePicker.setIs24HourView(true);

        return mAlertDialog;

    }
}
