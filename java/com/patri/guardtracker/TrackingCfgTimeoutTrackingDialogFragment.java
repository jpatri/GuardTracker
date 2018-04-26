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
public class TrackingCfgTimeoutTrackingDialogFragment extends DialogGenericIcTtMs2Bt {
    public final static String TAG = TrackingCfgTimeoutTrackingDialogFragment.class.getSimpleName();

    protected ViewGroup mViewContainer;
    private TimePicker mTimeToWakeTimePicker;

    public int getNewTimeToWakeMinutes() {
        int currentMinutes = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            currentMinutes = mTimeToWakeTimePicker.getHour() * 60;
            currentMinutes += mTimeToWakeTimePicker.getMinute();
        }
        return currentMinutes;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);


        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mViewContainer = (ViewGroup)inflater.inflate(R.layout.fragment_cfg_time_picker, null);
        mAlertDialog.setView(mViewContainer);

        mTimeToWakeTimePicker = (TimePicker) mViewContainer.findViewById(R.id.numberPicker);

        Bundle bundle = getArguments();
        int currentTimeToWakeTimeout = bundle.getInt(MonitoringCfgActivity.MON_CFG_CURRENT_TIME_TO_WAKE_IN_MINUTES_ARG);
        mTimeToWakeTimePicker.setHour(currentTimeToWakeTimeout/60);
        mTimeToWakeTimePicker.setMinute(currentTimeToWakeTimeout%60);
        mTimeToWakeTimePicker.setIs24HourView(true);

        return mAlertDialog;

    }
}
