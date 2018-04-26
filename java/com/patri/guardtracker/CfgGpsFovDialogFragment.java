package com.patri.guardtracker;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.patri.guardtracker.model.MonitoringConfiguration;

/**
 * Created by patri on 20/11/2016.
 */
public class CfgGpsFovDialogFragment extends DialogGenericIcTtMs2Bt {
    public final static String TAG = CfgGpsFovDialogFragment.class.getSimpleName();
    public final static String CFG_CURRENT_GPS_FOV_ARG = "com.patri.guardtracker.CfgGpsFovDialogFragment.CFG_CURRENT_GPS_FOV_ARG";

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
        mViewContainer = (ViewGroup)inflater.inflate(R.layout.fragment_cfg_number_picker, null);
        mAlertDialog.setView(mViewContainer);

        mGpsFovNumberPicker = mViewContainer.findViewById(R.id.numberPicker);
        TextView textView = mViewContainer.findViewById(R.id.number_picker_units);

        Bundle bundle = getArguments();
        int currentGpsFov = bundle.getInt(CFG_CURRENT_GPS_FOV_ARG);
        mGpsFovNumberPicker.setMinValue(MonitoringConfiguration.GPS_FOV_MIN);
        mGpsFovNumberPicker.setMaxValue(MonitoringConfiguration.GPS_FOV_MAX);
        mGpsFovNumberPicker.setValue(currentGpsFov);
        textView.setText(R.string.satellites_label_all_small);

        return mAlertDialog;

    }
}
