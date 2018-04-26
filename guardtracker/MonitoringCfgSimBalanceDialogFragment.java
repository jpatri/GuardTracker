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
public class MonitoringCfgSimBalanceDialogFragment extends DialogGenericIcTtMs2Bt {
    public final static String TAG = MonitoringCfgSimBalanceDialogFragment.class.getSimpleName();
    private final static int CENTIME_STEP_SIZE = 10;

    protected ViewGroup mViewContainer;
    private NumberPicker mEurosNumberPicker;
    private NumberPicker mCentimesNumberPicker;

    public double getNewSimBalance() {
        double currentSimBalance = mEurosNumberPicker.getValue();
        currentSimBalance += (double)mCentimesNumberPicker.getValue() * CENTIME_STEP_SIZE / 100;
        return currentSimBalance;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);


        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mViewContainer = (ViewGroup)inflater.inflate(R.layout.fragment_monitoring_cfg_sim_balance, null);
        mAlertDialog.setView(mViewContainer);

        mEurosNumberPicker = (NumberPicker) mViewContainer.findViewById(R.id.numberPickerEuros);
        mCentimesNumberPicker = (NumberPicker) mViewContainer.findViewById(R.id.numberPickerCentimes);

        Bundle bundle = getArguments();
        double currentSimBalanceWaterLine = bundle.getDouble(MonitoringCfgActivity.MON_CFG_CURRENT_SIM_BALANCE_ARG);
        int currentEurosValue = (int)currentSimBalanceWaterLine;
        int currentCentimesValue = (int)(currentSimBalanceWaterLine * 100) % 100;

        mEurosNumberPicker.setMinValue((int)MonitoringConfiguration.SIM_BALANCE_WATERLINE_MIN_EURO);
        mEurosNumberPicker.setMaxValue((int)MonitoringConfiguration.SIM_BALANCE_WATERLINE_MAX_EURO);
        mEurosNumberPicker.setValue(currentEurosValue);
        mCentimesNumberPicker.setMinValue((int)MonitoringConfiguration.SIM_BALANCE_WATERLINE_MIN_CENTIME/CENTIME_STEP_SIZE);
        mCentimesNumberPicker.setMaxValue((int)MonitoringConfiguration.SIM_BALANCE_WATERLINE_MAX_CENTIME/CENTIME_STEP_SIZE);

        String[] centimeValues = new String[mCentimesNumberPicker.getMaxValue() - mCentimesNumberPicker.getMinValue() + 1];
        for (int i = 0; i < centimeValues.length; i++) {
            String number = Integer.toString(i * CENTIME_STEP_SIZE);
            centimeValues[i] = number.length() < 2 ? "0" + number : number;
        }
        mCentimesNumberPicker.setDisplayedValues(centimeValues);

        mCentimesNumberPicker.setValue(currentCentimesValue/CENTIME_STEP_SIZE);

        return mAlertDialog;

    }
}
