package com.patri.guardtracker;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import com.patri.guardtracker.model.MonitoringConfiguration;

import java.util.Arrays;

/**
 * Created by patri on 20/11/2016.
 */
public class MonitoringCfgGpsThresholdDialogFragment extends DialogGenericIcTtMs2Bt {
    public final static String TAG = MonitoringCfgGpsThresholdDialogFragment.class.getSimpleName();
    private final static String[] mValues = {
            "010", "020", "030", "040", "050", "060", "070", "080", "090", "100", "150", "200",
            "300", "400", "500", "600", "700", "800", "900", "1000"
    };
    private final static int[] mIntValues = {
            10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 150, 200,
            300, 400, 500, 600, 700, 800, 900, 1000
    };


    protected ViewGroup mViewContainer;
    private NumberPicker mGpsThresholdNumberPicker;

    public int getNewGpsThreshold() {
        int currentGpsThresholdIdx = mGpsThresholdNumberPicker.getValue();
        int currentGpsThreshold = mIntValues[currentGpsThresholdIdx];
        return currentGpsThreshold;
    }

    private int getIndexOfValue(int value) {
        int idx = Arrays.binarySearch(mIntValues, value);
        if (idx < 0) {
            idx = Math.abs(idx);
            idx -= 1;
            if (idx > 0) // adjust to one above index of insertion only greater than zero.
                idx -= 1;
        }
        return idx;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);


        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mViewContainer = (ViewGroup)inflater.inflate(R.layout.fragment_monitoring_cfg_gps_threshold, null);
        mAlertDialog.setView(mViewContainer);

        mGpsThresholdNumberPicker = (NumberPicker) mViewContainer.findViewById(R.id.timePicker);

        Bundle bundle = getArguments();
        int currentGpsThreshold = bundle.getInt(MonitoringCfgActivity.MON_CFG_CURRENT_GPS_THRESHOLD_IN_METERS_ARG);
        mGpsThresholdNumberPicker.setMinValue(0/*mIntValues[0]*//*MonitoringConfiguration.GPS_THRESHOLD_METERS_MIN*/);
        mGpsThresholdNumberPicker.setMaxValue(mIntValues.length - 1/*MonitoringConfiguration.GPS_THRESHOLD_METERS_MAX*/);
        mGpsThresholdNumberPicker.setSoundEffectsEnabled(true);

        mGpsThresholdNumberPicker.setDisplayedValues(mValues);

        int idx = getIndexOfValue(currentGpsThreshold);
        mGpsThresholdNumberPicker.setValue(idx);

        return mAlertDialog;

    }
}
