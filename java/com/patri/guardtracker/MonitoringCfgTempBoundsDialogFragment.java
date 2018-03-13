package com.patri.guardtracker;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;

import com.patri.guardtracker.model.MonitoringConfiguration;

/**
 * Created by patri on 20/11/2016.
 */
public class MonitoringCfgTempBoundsDialogFragment extends DialogGenericIcTtMs2Bt {
    public final static String TAG = MonitoringCfgTempBoundsDialogFragment.class.getSimpleName();

    protected ViewGroup mViewContainer;
    private NumberPicker mLowNumberPicker;
    private NumberPicker mHighNumberPicker;

    public double getNewTempLow() {
        double currentTempLow = (double)mLowNumberPicker.getValue();
        return currentTempLow;
    }
    public double getNewTempHigh() {
        double currentTempHigh = (double)mHighNumberPicker.getValue();
        return currentTempHigh;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mViewContainer = (ViewGroup)inflater.inflate(R.layout.fragment_monitoring_cfg_temp_bounds, null);
        mAlertDialog.setView(mViewContainer);

        mLowNumberPicker = (NumberPicker) mViewContainer.findViewById(R.id.numberPickerLow);
        mHighNumberPicker = (NumberPicker) mViewContainer.findViewById(R.id.numberPickerHigh);

        Bundle bundle = getArguments();
        double currentTempLow = bundle.getDouble(MonitoringCfgActivity.MON_CFG_CURRENT_TEMP_BOUNDS_LOW_ARG);
        double currentTempHigh = bundle.getDouble(MonitoringCfgActivity.MON_CFG_CURRENT_TEMP_BOUNDS_HIGH_ARG);

        mLowNumberPicker.setMinValue((int)MonitoringConfiguration.TEMP_LOW_MIN);
        mLowNumberPicker.setMaxValue((int)MonitoringConfiguration.TEMP_LOW_MAX);
        mLowNumberPicker.setValue((int)currentTempLow);
        mHighNumberPicker.setMinValue((int)MonitoringConfiguration.TEMP_HIGH_MIN);
        mHighNumberPicker.setMaxValue((int)MonitoringConfiguration.TEMP_HIGH_MAX);
        mHighNumberPicker.setValue((int)currentTempHigh);

        return mAlertDialog;

    }
}
