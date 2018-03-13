package com.patri.guardtracker.custom;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;

import com.patri.guardtracker.DialogListener;
import com.patri.guardtracker.MonitoringCfgActivity;

/**
 * Created by patri on 17/12/2016.
 */
public class MyTimePickerDialogFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {
    private static String TAG = MyTimePickerDialogFragment.class.getSimpleName();

    private int mNewHour;
    private int mNewMinute;
    private DialogListener mListener;

    public void setListener(DialogListener listener) {
        mListener = listener;
    }

    public int getNewHour() {
        return mNewHour;
    }

    public int getNewMinute() {
        return mNewMinute;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker

        Bundle bundle = getArguments();
        int minutes = bundle.getInt(MonitoringCfgActivity.MON_CFG_CURRENT_TIME_TO_WAKE_IN_MINUTES_ARG);
        int hour = minutes / 60;
        int minute = minutes % 60;

        // Create a new instance of TimePickerDialog and return it
        TimePickerDialog dialog = new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
        dialog.setIcon(android.R.drawable.ic_menu_edit);

        return dialog;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
        Log.i(TAG, "onTimeSet");
        mNewHour = hourOfDay;
        mNewMinute = minute;
        if (mListener != null)
            mListener.onDialogPositiveClick(this);
    }
}
