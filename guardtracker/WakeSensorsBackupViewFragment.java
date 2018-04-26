package com.patri.guardtracker;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by patri on 01/11/2016.
 */
public class WakeSensorsBackupViewFragment extends AppCompatDialogFragment {
    public static final String WAKE_SENSORS_BITMASK_ID_KEY = "WakeSensorsBackupViewFragment.WAKE_SENSORS_BITMASK_ID_KEY";
//    public static final String WAKE_SENSORS_MESSAGE_BODY_ID_KEY = "WakeSensorsBackupViewFragment.WAKE_SENSORS_MESSAGE_BODY_ID_KEY";
    public static final String WAKE_SENSORS_TITLE_ID_KEY = "WakeSensorsBackupViewFragment.WAKE_SENSORS_TITLE_ID_KEY";
    public final static String TAG = WakeSensorsBackupViewFragment.class.getSimpleName();

    public static final int BITMASK_ACC = (1 << 0);
    public static final int BITMASK_RTC = (1 << 1);
    public static final int BITMASK_BLE = (1 << 2);


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        int bitmask = bundle.getInt(WAKE_SENSORS_BITMASK_ID_KEY);
        int dialogTitleId = R.string.dialog_wake_sensors_view_synced_title;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        ViewGroup viewContainer = (ViewGroup) inflater.inflate(R.layout.fragment_wake_sensors_view, null);
        TextView accStateView = viewContainer.findViewById(R.id.sensors_acc_state_label);
        TextView rtcStateView = viewContainer.findViewById(R.id.sensors_rtc_state_label);
        TextView bleStateView = viewContainer.findViewById(R.id.sensors_ble_state_label);

        CharSequence ena = getText(R.string.enable);
        CharSequence dis = getText(R.string.disable);
        accStateView.setText((bitmask & BITMASK_ACC) != 0 ? ena : dis);
        rtcStateView.setText((bitmask & BITMASK_RTC) != 0 ? ena : dis);
        bleStateView.setText((bitmask & BITMASK_BLE) != 0 ? ena : dis);

        int mediumLen = (int)getResources().getDimension(R.dimen.margin_medium);
        //viewContainer.setLayoutParams(new ViewGroup.LayoutParams(getContext(), Layout.Alignment.ALIGN_CENTER, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        viewContainer.setPadding(0, mediumLen, 0, mediumLen);
        builder.setCancelable(true)
                .setView(viewContainer)
                .setTitle(dialogTitleId)
                .setPositiveButton(R.string.close_label, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing.
                    }
                });


        AlertDialog mAlertDialog = builder.create();
        return mAlertDialog;
    }
}
