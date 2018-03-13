package com.patri.guardtracker;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import com.patri.guardtracker.model.MonitoringConfiguration;

/**
 * Created by patri on 20/11/2016.
 */
public class MonitoringCfgGpsFovDialogFragment extends DialogGenericIcTtMs2Bt {
    public final static String TAG = MonitoringCfgGpsFovDialogFragment.class.getSimpleName();

    protected ViewGroup mViewContainer;
    private NumberPicker mGpsFovNumberPicker;

    public int getNewGpsFov() {
        int currentGpsFov = mGpsFovNumberPicker.getValue();
        return currentGpsFov;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);


        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mViewContainer = (ViewGroup)inflater.inflate(R.layout.fragment_monitoring_cfg_gps_fov, null);
        mAlertDialog.setView(mViewContainer);

        mGpsFovNumberPicker = (NumberPicker) mViewContainer.findViewById(R.id.timePicker);

        Bundle bundle = getArguments();
        int currentGpsFov = bundle.getInt(MonitoringCfgActivity.MON_CFG_CURRENT_GPS_FOV_ARG);
        mGpsFovNumberPicker.setMinValue(MonitoringConfiguration.GPS_FOV_MIN);
        mGpsFovNumberPicker.setMaxValue(MonitoringConfiguration.GPS_FOV_MAX);
        mGpsFovNumberPicker.setValue(currentGpsFov);

        return mAlertDialog;

    }
}
