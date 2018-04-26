package com.patri.guardtracker;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

/**
 * Created by patri on 20/11/2016.
 */
public class CfgPercentPickerDialogFragment extends DialogGenericIcTtMs2Bt {
    public final static String TAG = CfgPercentPickerDialogFragment.class.getSimpleName();
    public final static String CFG_CURRENT_VAL_ARG = "com.patri.guardtracker.CfgPercentPickerDialogFragment.CFG_CURRENT_VAL_ARG";

    protected ViewGroup mViewContainer;
    private NumberPicker mNumberPicker;

    public int getNewPercent() {
        int current = mNumberPicker.getValue();
        return current;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);


        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mViewContainer = (ViewGroup)inflater.inflate(R.layout.fragment_cfg_number_picker, null);
        mAlertDialog.setView(mViewContainer);

        mNumberPicker = mViewContainer.findViewById(R.id.numberPicker);
        TextView textView = mViewContainer.findViewById(R.id.number_picker_units);

        Bundle bundle = getArguments();
        int current = bundle.getInt(CFG_CURRENT_VAL_ARG);
        mNumberPicker.setMinValue(0);
        mNumberPicker.setMaxValue(100);
        mNumberPicker.setValue(current);
        textView.setText("%");

        return mAlertDialog;

    }
}
