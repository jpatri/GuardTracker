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
public class MonitoringCfgGpsTimeoutDialogFragment extends DialogGenericIcTtMs2Bt {
    public final static String TAG = MonitoringCfgGpsTimeoutDialogFragment.class.getSimpleName();

    protected ViewGroup mViewContainer;
    private TimePicker mGpsTimeoutTimePicker;

    public int getNewGpsTimeout() {
        int currentGpsTimeout = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            currentGpsTimeout = mGpsTimeoutTimePicker.getHour() * 60;
            currentGpsTimeout += mGpsTimeoutTimePicker.getMinute();

        }
        return currentGpsTimeout;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);


        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mViewContainer = (ViewGroup)inflater.inflate(R.layout.fragment_cfg_time_picker, null);
        mAlertDialog.setView(mViewContainer);

        mGpsTimeoutTimePicker = (TimePicker) mViewContainer.findViewById(R.id.numberPicker);

        Bundle bundle = getArguments();
        int currentGpsTimeout = bundle.getInt(MonitoringCfgActivity.MON_CFG_CURRENT_GPS_TIMEOUT_IN_MINUTES_ARG);
        mGpsTimeoutTimePicker.setHour(currentGpsTimeout/60);
        mGpsTimeoutTimePicker.setMinute(currentGpsTimeout%60);
        mGpsTimeoutTimePicker.setIs24HourView(true);

        return mAlertDialog;

    }
}
