package com.patri.guardtracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.patri.guardtracker.model.VigilanceConfiguration;

public class VigilanceCfgActivity extends AppCompatActivity implements DialogListener {
    private static final String TAG = VigilanceConfiguration.class.getSimpleName();
//    public final static String MON_CFG_CURRENT_TIME_TO_WAKE_IN_MINUTES_ARG = "com.patri.guardtracker.MonitoringCfgActivity.MON_CFG_CURRENT_TIME_TO_WAKE_IN_MINUTES";
//    public final static String MON_CFG_CURRENT_WAKEUP_PERIOD_IN_MINUTES_ARG = "com.patri.guardtracker.MonitoringCfgActivity.MON_CFG_CURRENT_WAKEUP_PERIOD_IN_MINUTES_ARG";
//    public final static String MON_CFG_CURRENT_SMS_CRITERIA_ARG = "com.patri.guardtracker.MonitoringCfgActivity.MON_CFG_CURRENT_SMS_CRITERIA_ARG";
//    public final static String MON_CFG_CURRENT_SMS_CRITERIA_CYCLIC_ARG = "com.patri.guardtracker.MonitoringCfgActivity.MON_CFG_CURRENT_SMS_CRITERIA_CYCLIC_ARG";
//    public final static String MON_CFG_CURRENT_GPS_THRESHOLD_IN_METERS_ARG = "com.patri.guardtracker.MonitoringCfgActivity.MON_CFG_CURRENT_GPS_THRESHOLD_IN_METERS_ARG";
//    public final static String MON_CFG_CURRENT_GPS_FOV_ARG = "com.patri.guardtracker.MonitoringCfgActivity.MON_CFG_CURRENT_GPS_FOV_ARG";
//    public final static String MON_CFG_CURRENT_GPS_TIMEOUT_IN_MINUTES_ARG = "com.patri.guardtracker.MonitoringCfgActivity.MON_CFG_CURRENT_GPS_TIMEOUT_IN_MINUTES_ARG";
//    public final static String MON_CFG_CURRENT_TEMP_BOUNDS_LOW_ARG = "com.patri.guardtracker.MonitoringCfgActivity.MON_CFG_CURRENT_TEMP_BOUNDS_LOW_ARG";
//    public final static String MON_CFG_CURRENT_TEMP_BOUNDS_HIGH_ARG = "com.patri.guardtracker.MonitoringCfgActivity.MON_CFG_CURRENT_TEMP_BOUNDS_HIGH_ARG";
//    public final static String MON_CFG_CURRENT_SIM_BALANCE_ARG = "com.patri.guardtracker.MonitoringCfgActivity.MON_CFG_CURRENT_SIM_BLANCE_ARG";

    /**
     * This enumerate is used to establish the Tag to be passed to dialog. The same dialog type is several situations.
     */
    private enum VigilanceCfgDialogs {
        TiltSensitivityDialog, BleAdvertPeriodDialog, VigCfgCancelDialog;
        private String[] valuesStr = { "0", "1", "2" };
        public String toString() {
            String dialogStr = valuesStr[this.ordinal()];
            return dialogStr;
        }
    }

    private VigilanceConfiguration mVigilanceCfg;
    private VigilanceConfiguration mVigilanceCfgBackup;
    private int mVigilanceCfgId;
    private Button mDoneButton;
    private Button mCancelButton;

    ArrayAdapter mArrayAdapter;
    String [][] mElems;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_vigilance_cfg);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create a progress bar to display while the list loads
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progressBar.setIndeterminate(true);

        mDoneButton = findViewById(R.id.vig_cfg_done_button);
        mCancelButton = findViewById(R.id.vig_cfg_cancel_button);
        final ListView listView = findViewById(R.id.vig_cfg_list_view);
        listView.setEmptyView(progressBar);

        // Must add the progress bar to the root of the layout
        ViewGroup root = findViewById(android.R.id.content);
        root.addView(progressBar);


//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        Log.i(TAG, "onCreate(Bundle savedInstanceState [" + savedInstanceState + "])");

//        SharedPreferences preferences = getPreferences(MODE_PRIVATE);

        Intent intent = getIntent();
//        mGuardTrackerId = intent.getIntExtra(GuardTrackerActivity.GUARD_TRACKER_ID, 0);
        mVigilanceCfgId = intent.getIntExtra(GuardTrackerActivity.CONFIG_ID, 0);
        String guardTrackerName = intent.getStringExtra(GuardTrackerActivity.GUARD_TRACKER_NAME);
//        if (mGuardTrackerId == 0 || mVigilanceCfgId == 0) {
//            mGuardTrackerId = preferences.getInt(getString(R.string.saved_guard_tracker_id), 0);
//            mVigilanceCfgId = preferences.getInt(getString(R.string.saved_guard_tracker_tracking_cfg_id), 0);
//        }
//        if (mGuardTrackerId == 0 || mVigilanceCfgId == 0) {
//            // Return to last activity
//            // ToDo
//        }

        mVigilanceCfg = VigilanceConfiguration.read(getBaseContext(), mVigilanceCfgId);
        mVigilanceCfgBackup = new VigilanceConfiguration(
                mVigilanceCfg.getTiltLevel(),
                mVigilanceCfg.getBleAdvertisePeriod()
        );

        mElems = new String [][] {
                { getString(R.string.vig_cfg_item_title_tilt_sensitivity), mVigilanceCfg.getPrettyTiltSensitivity() },
                { getString(R.string.vig_cfg_item_title_ble_advertise_period), mVigilanceCfg.getPrettyBleAdvertisePeriod() }
        };

        // A simple ArrayAdapter can only be represented by a TextView
        mArrayAdapter = new ArrayAdapter(getBaseContext(), android.R.layout.simple_list_item_2, android.R.id.text1, mElems) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setText(mElems[position][0]);
                text2.setText(mElems[position][1]);
                return view;
            }
        };

        listView.setAdapter(mArrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "listView.onItemSelected: position = " + position + " id = " + id);
                listView.setItemChecked(position, true);
                // Start views for configure individual elements
                // Start views for configure individual elements
                switch(position) {
                    case 0:
                        startTiltSensitivityDialog();
                        break;
                    case 1:
                        startBleAdvertPeriodDialog();
                        break;
                    case 2:
                    default:;
                }
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVigilanceCfg.equals(mVigilanceCfgBackup) == false)
                    startCancelConfigDialog();
                else
                    onClickCancelConfig();
            }
        });

        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickDoneConfig();
            }
        });

        setTitle(getString(R.string.title_activity_vigilance_cfg) + ": " + guardTrackerName);
        mHandler = new Handler();
    }

    private void updateVigCfgView() {
        mElems[0][1] = mVigilanceCfg.getPrettyTiltSensitivity();
        mElems[1][1] = mVigilanceCfg.getPrettyBleAdvertisePeriod();

//        ListAdapter listAdapter = mArrayAdapter;
//        String [] listItem = (String[])listAdapter.getItem(position);
//        listItem[1] = mMonCfg.getPrettyStartTime();
        mArrayAdapter.notifyDataSetChanged();
    }

    private void onClickTitltSensitivityConfig(int sensitivity) {
        mVigilanceCfg.setTiltLevel(sensitivity);
        updateVigCfgView();
    }
    private void onClickBleAdvertPeriodConfig(int bleAdvertPeriodSeconds) {
        mVigilanceCfg.setBleAdvertisePeriod(bleAdvertPeriodSeconds);
        updateVigCfgView();
    }

    private void onClickCancelConfig() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    private void onClickDoneConfig() {
        // May have to create a new entry in Monitoring Configuration table
        if (mVigilanceCfg.equals(mVigilanceCfgBackup) == false)
//            if (mToCreateBackup)
            mVigilanceCfg.create(getBaseContext());
//            else
//                mMonCfg.update(getBaseContext());
        Intent intent = new Intent();
        intent.putExtra(GuardTrackerActivity.RESULT_MON_CFG, mVigilanceCfg.get_id());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void startTiltSensitivityDialog() {
        DialogGenericIcTtMs2Bt dialogFragment = new CfgPercentPickerDialogFragment();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogGenericIcTtMs2Bt.ICON_ID_KEY, android.R.drawable.ic_menu_edit);
        bundle.putInt(DialogGenericIcTtMs2Bt.TITLE_ID_KEY, R.string.dialog_vig_cfg_tilt_sensitivity_title);
        bundle.putInt(DialogGenericIcTtMs2Bt.MESSAGE_ID_KEY, R.string.dialog_vig_cfg_tilt_sensitivity_message_body);
        bundle.putInt(DialogGenericIcTtMs2Bt.YES_BUTTON_LABEL_ID_KEY, R.string.done_button_label);
        bundle.putInt(DialogGenericIcTtMs2Bt.NO_BUTTON_LABEL_ID_KEY, R.string.cancel_button_label);
        bundle.putInt(CfgPercentPickerDialogFragment.CFG_CURRENT_VAL_ARG, mVigilanceCfg.getTiltLevel());
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, VigilanceCfgDialogs.TiltSensitivityDialog.toString());
    }
    private void startBleAdvertPeriodDialog() {
        DialogGenericIcTtMs2Bt dialogFragment = new CfgTimePickerDialogFragment();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogGenericIcTtMs2Bt.ICON_ID_KEY, android.R.drawable.ic_menu_edit);
        bundle.putInt(DialogGenericIcTtMs2Bt.TITLE_ID_KEY, R.string.dialog_vig_cfg_ble_advert_period_title);
        bundle.putInt(DialogGenericIcTtMs2Bt.MESSAGE_ID_KEY, R.string.dialog_vig_cfg_ble_advert_period_message_body);
        bundle.putInt(DialogGenericIcTtMs2Bt.YES_BUTTON_LABEL_ID_KEY, R.string.done_button_label);
        bundle.putInt(DialogGenericIcTtMs2Bt.NO_BUTTON_LABEL_ID_KEY, R.string.cancel_button_label);
        bundle.putInt(CfgPercentPickerDialogFragment.CFG_CURRENT_VAL_ARG, mVigilanceCfg.getBleAdvertisePeriod());
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, VigilanceCfgDialogs.BleAdvertPeriodDialog.toString());
    }

    private void startCancelConfigDialog() {
        DialogGenericIcTtMs2Bt dialogFragment = new DialogGenericIcTtMs2Bt();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogGenericIcTtMs2Bt.ICON_ID_KEY, android.R.drawable.ic_dialog_alert);
        bundle.putInt(DialogGenericIcTtMs2Bt.TITLE_ID_KEY, R.string.dialog_vig_cfg_cancel_title);
        bundle.putInt(DialogGenericIcTtMs2Bt.MESSAGE_ID_KEY, R.string.dialog_vig_cfg_cancel_message_body);
        bundle.putInt(DialogGenericIcTtMs2Bt.YES_BUTTON_LABEL_ID_KEY, android.R.string.yes);
        bundle.putInt(DialogGenericIcTtMs2Bt.NO_BUTTON_LABEL_ID_KEY, android.R.string.no);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, VigilanceCfgActivity.VigilanceCfgDialogs.VigCfgCancelDialog.toString());
    }
    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        dialog.dismiss();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        String dialogTag = dialog.getTag();
        if (VigilanceCfgDialogs.TiltSensitivityDialog.toString().equals(dialogTag)) {
            int newSensitivity = ((CfgPercentPickerDialogFragment)dialog).getNewPercent();
            onClickTitltSensitivityConfig(newSensitivity);
            dialog.dismiss();
            return;
        }
        if (VigilanceCfgDialogs.BleAdvertPeriodDialog.toString().equals(dialogTag)) {
            int bleAdvertPeriod = ((CfgTimePickerDialogFragment)dialog).getNewTime();
            onClickBleAdvertPeriodConfig(bleAdvertPeriod);
            dialog.dismiss();
            return;
        }
        if (VigilanceCfgActivity.VigilanceCfgDialogs.VigCfgCancelDialog.toString().equals(dialogTag)) {
            dialog.dismiss();
            onClickCancelConfig();
            return;
        }

    }
    @Override
    protected void onStart() {
        super.onStart();

        Log.i(TAG, "onStart");
    }


    @Override
    protected void onStop() {
        super.onStop();

        Log.i(TAG, "onStop()");
/*
//        SharedPreferences preferences = this.getPreferences(MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putInt(getString(R.string.saved_guard_tracker_id), guardTrackerId);
//        editor.putInt(getString(R.string.saved_guard_tracker_pos_ref_id), posRefId);
//        editor.putInt(getString(R.string.saved_guard_tracker_mon_cfg_id), monCfgId);
//        editor.putInt(getString(R.string.saved_guard_tracker_track_session_cfg_id), trackSessionCfgId);
//        editor.putInt(getString(R.string.saved_guard_tracker_vigilance_cfg_id), vigilanceCfgId);
//        editor.commit();
*/
    }


    @Override
    protected void onPause() {
        super.onPause();

        Log.i(TAG, "onPause()");

//        SharedPreferences preferences = this.getPreferences(MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putInt(getString(R.string.saved_guard_tracker_id), guardTrackerId);
//        editor.putInt(getString(R.string.saved_guard_tracker_pos_ref_id), posRefId);
//        editor.putInt(getString(R.string.saved_guard_tracker_mon_cfg_id), monCfgId);
//        editor.putInt(getString(R.string.saved_guard_tracker_track_session_cfg_id), trackSessionCfgId);
//        editor.putInt(getString(R.string.saved_guard_tracker_vigilance_cfg_id), vigilanceCfgId);
//
//        editor.putInt(getString(R.string.saved_guard_tracker_enable_spinner_position), enableSpinnerPosition);
//        editor.putInt(getString(R.string.saved_guard_tracker_mon_info_spinner_position), monInfoSpinnerPosition);
//        editor.putInt(getString(R.string.saved_guard_tracker_track_session_spinner_position), trackSessionSpinnerPosition);
//        editor.putInt(getString(R.string.saved_guard_tracker_cfg_spinner_position), cfgSpinnerPosition);
//
//        editor.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        Log.i(TAG, "onSaveInstanceState()");

        // Save GuardTracker _ID
//        outState.putInt(MainActivity.ITEM_SELECTED, guardTracker.get_id());

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        Log.i(TAG, "onRestart()");
/*
//        if (guardTracker == null) {
//
//            SharedPreferences preferences = this.getPreferences(MODE_PRIVATE);
//            guardTrackerId = preferences.getInt(getString(R.string.saved_guard_tracker_id), -1);
//            posRefId = preferences.getInt(getString(R.string.saved_guard_tracker_pos_ref_id), -1);
//            monCfgId = preferences.getInt(getString(R.string.saved_guard_tracker_mon_cfg_id), -1);
//            trackSessionCfgId = preferences.getInt(getString(R.string.saved_guard_tracker_track_session_cfg_id), -1);
//            vigilanceCfgId = preferences.getInt(getString(R.string.saved_guard_tracker_vigilance_cfg_id), -1);
//
//            EditText nameEditText = (EditText) findViewById(R.id.nickname_editText);
//            assert nameEditText != null;
//            String name = nameEditText.getText().toString();
//            if (name == null)
//                name = nameEditText.getHint().toString();
//
//            EditText gsmEditText = (EditText) findViewById(R.id.phone_number_editText);
//            assert gsmEditText != null;
//            String gsm = gsmEditText.getText().toString();
//            if (gsm == null)
//                gsm = gsmEditText.getHint().toString();
//
//            EditText bleEditText = (EditText) findViewById(R.id.phone_number_editText);
//            assert bleEditText != null;
//            String ble = bleEditText.getText().toString();
//            if (ble == null)
//                ble = bleEditText.getHint().toString();
//
//            Spinner enableSpinner = (Spinner) findViewById(R.id.enable_spinner);
//            assert enableSpinner != null;
//            int enable = enableSpinner.getSelectedItemPosition();
//
//            guardTracker = new GuardTracker(guardTrackerId, name, ble, gsm, enable == 1, posRefId, monCfgId, trackSessionCfgId, vigilanceCfgId);
//        }
*/
    }
}
