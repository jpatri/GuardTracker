package com.patri.guardtracker;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;

/**
 * Created by patri on 01/11/2016.
 */
public class WakeSensorsConfigDialogFragment extends AppCompatDialogFragment {
    public static final String WAKE_SENSORS_BITMASK_ID_KEY = "WakeSensorsConfigDialogFragment.WAKE_SENSORS_BITMASK_ID_KEY";
    public static final String WAKE_SENSORS_MESSAGE_BODY_ID_KEY = "WakeSensorsConfigDialogFragment.WAKE_SENSORS_MESSAGE_BODY_ID_KEY";
    public static final String WAKE_SENSORS_TITLE_ID_KEY = "WakeSensorsConfigDialogFragment.WAKE_SENSORS_TITLE_ID_KEY";
    public final static String TAG = WakeSensorsConfigDialogFragment.class.getSimpleName();

    public static final int BITMASK_ACC = (1 << 0);
    public static final int BITMASK_RTC = (1 << 1);
    public static final int BITMASK_BLE = (1 << 2);


    private ViewGroup mViewContainer; // protected to be accessed in GuardTrackerPAiringDialogFragment.
    private AlertDialog mAlertDialog;
    private Switch mAccSwitch;
    private Switch mRtcSwitch;
    private Switch mBleSwitch;

    private int mBitmask;

    // Use this instance of the interface to deliver action events
    DialogListener mListener;

    public int getWakeSensorsBitmask() {
        return mBitmask;
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (DialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement DialogListener");
        }
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        mBitmask = bundle.getInt(WAKE_SENSORS_BITMASK_ID_KEY);
        int mMessageBodyId = R.string.dialog_wake_sensors_config_message_body;
        int mDialogTitleId = R.string.dialog_wake_sensors_config_title;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mViewContainer = (ViewGroup)inflater.inflate(R.layout.fragment_wake_sensors_dialog, null);
        mAccSwitch = mViewContainer.findViewById(R.id.dialog_accelerometer_switch);
        mRtcSwitch = mViewContainer.findViewById(R.id.dialog_rtc_switch);
        mBleSwitch = mViewContainer.findViewById(R.id.dialog_ble_switch);

        mAccSwitch.setChecked((mBitmask & BITMASK_ACC) != 0);
        mRtcSwitch.setChecked((mBitmask & BITMASK_RTC) != 0);
        mBleSwitch.setChecked((mBitmask & BITMASK_BLE) != 0);
        
        builder.setCancelable(true)
                .setView(mViewContainer)
                .setTitle(mDialogTitleId)
                .setMessage(mMessageBodyId)
                .setPositiveButton(R.string.ok_button_label, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing.
                    }
                })
                .setNegativeButton(R.string.cancel_button_label, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogNegativeClick(WakeSensorsConfigDialogFragment.this);
                    }
                });

        mAlertDialog = builder.create();
        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int bitmask = 0;
                        if (mAccSwitch.isChecked())
                            bitmask |= BITMASK_ACC;
                        if (mRtcSwitch.isChecked())
                            bitmask |= BITMASK_RTC;
                        if (mBleSwitch.isChecked())
                            bitmask |= BITMASK_BLE;

                        mBitmask = bitmask;
                        if (mListener != null)
                            mListener.onDialogPositiveClick(WakeSensorsConfigDialogFragment.this);
                    }
                });
            }
        });
        return mAlertDialog;
    }
}
