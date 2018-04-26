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

import com.patri.guardtracker.model.TrackingConfiguration;

public class TrackingCfgActivity extends AppCompatActivity implements DialogListener {
    private static final String TAG = TrackingCfgActivity.class.getSimpleName();
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
    private enum TrackingCfgDialogs {
        SmsCriteriaDialog, GpsThresholdDialog, GpsFovDialog, GpsTimeoutDialog, TimeoutTrackingDialog, TimeoutPreDialog, TimeoutPostDialog, TrackCfgCancelDialog;
        private String[] valuesStr = { "0", "1", "2", "3", "4", "5", "6", "7" };
        public String toString() {
            String dialogStr = valuesStr[this.ordinal()];
            return dialogStr;
        }
    }

    private TrackingConfiguration mTrackCfg;
    private TrackingConfiguration mTrackCfgBackup;
    private int mTrackCfgId;
    private Button mDoneButton;
    private Button mCancelButton;

    ArrayAdapter mArrayAdapter;
    String [][] mElems;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tracking_cfg);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create a progress bar to display while the list loads
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progressBar.setIndeterminate(true);

        mDoneButton = findViewById(R.id.track_cfg_done_button);
        mCancelButton = findViewById(R.id.track_cfg_cancel_button);
        final ListView listView = findViewById(R.id.track_cfg_list_view);
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
        mTrackCfgId = intent.getIntExtra(GuardTrackerActivity.CONFIG_ID, 0);
        String guardTrackerName = intent.getStringExtra(GuardTrackerActivity.GUARD_TRACKER_NAME);
//        if (mGuardTrackerId == 0 || mTrackCfgId == 0) {
//            mGuardTrackerId = preferences.getInt(getString(R.string.saved_guard_tracker_id), 0);
//            mTrackCfgId = preferences.getInt(getString(R.string.saved_guard_tracker_tracking_cfg_id), 0);
//        }
//        if (mGuardTrackerId == 0 || mTrackCfgId == 0) {
//            // Return to last activity
//            // ToDo
//        }

        mTrackCfg = TrackingConfiguration.read(getBaseContext(), mTrackCfgId);
        mTrackCfgBackup = new TrackingConfiguration(
                mTrackCfg.getSmsCriteria(),
                mTrackCfg.getGpsThreshold(),
                mTrackCfg.getGpsFov(),
                mTrackCfg.getGpsTimeout(),
                mTrackCfg.getTimeoutTracking(),
                mTrackCfg.getTimeoutPre(),
                mTrackCfg.getTimeoutPost()
        );

        mElems = new String[][] {
                { getString(R.string.track_cfg_item_title_sms_criteria), mTrackCfg.getPrettySmsCriteria() },
                { getString(R.string.track_cfg_item_title_gps_threshold), mTrackCfg.getPrettyGpsThreshold() },
                { getString(R.string.track_cfg_item_title_gps_fov), mTrackCfg.getPrettyGpsFov() },
                { getString(R.string.track_cfg_item_title_gps_timeout), mTrackCfg.getPrettyGpsTimeout() },
                { getString(R.string.track_cfg_item_title_timeout_tracking), mTrackCfg.getPrettyTimeoutTracking() },
                { getString(R.string.track_cfg_item_title_timeout_pre), mTrackCfg.getPrettyTimeoutPre() },
                { getString(R.string.track_cfg_item_title_timeout_post), mTrackCfg.getPrettyTimeoutPost() }
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
                switch(position) {
                    case 0:
                        startSmsCriteriaDialog();
                        break;
                    case 1:
                        startGpsThresholdDialog();
                        break;
                    case 2:
                        startGpsFovDialog();
                        break;
                    case 3:
                        startGpsTimeoutDialog();
                        break;
                    case 4:
                        startTimeoutTrackingDialog();
                        break;
                    case 5:
                        startTimeoutPreDialog();
                        break;
                    case 6:
                        startTimeoutPostDialog();
                        break;
                    default:;
                }
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTrackCfg.equals(mTrackCfgBackup) == false)
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

        setTitle(getString(R.string.title_activity_tracking_cfg) + ": " + guardTrackerName);
        mHandler = new Handler();
    }

    private void updateTrackCfgView() {
        mElems[0][1] = mTrackCfg.getPrettySmsCriteria();
        mElems[1][1] = mTrackCfg.getPrettyGpsThreshold();
        mElems[2][1] = mTrackCfg.getPrettyGpsFov();
        mElems[3][1] = mTrackCfg.getPrettyGpsTimeout();
        mElems[4][1] = mTrackCfg.getPrettyTimeoutTracking();
        mElems[5][1] = mTrackCfg.getPrettyTimeoutPre();
        mElems[6][1] = mTrackCfg.getPrettyTimeoutPost();

//        ListAdapter listAdapter = mArrayAdapter;
//        String [] listItem = (String[])listAdapter.getItem(position);
//        listItem[1] = mMonCfg.getPrettyStartTime();
        mArrayAdapter.notifyDataSetChanged();
    }
    private void onClickSmsCriteriaConfig(TrackingConfiguration.SmsCriteria newSmsCriteria) {
        mTrackCfg.setSmsCriteria(newSmsCriteria);
        updateTrackCfgView();
    }
    private void onClickGpsThresholdConfig(int gpsThresholdMeters) {
        mTrackCfg.setGpsThreshold(gpsThresholdMeters);
        updateTrackCfgView();
    }
    private void onClickGpsFovConfig(int gpsFov) {
        mTrackCfg.setGpsFov(gpsFov);
        updateTrackCfgView();
    }
    private void onClickGpsTimeoutConfig(int gpsTimeoutMinutes) {
        mTrackCfg.setGpsTimeout(gpsTimeoutMinutes);
        updateTrackCfgView();
    }
    private void onClickTimeoutTrackingConfig(int timeoutTrackingMinutes) {
        mTrackCfg.setTimeoutTracking(timeoutTrackingMinutes);
        updateTrackCfgView();
    }
    private void onClickTimeoutPreConfig(int timeoutPreMinutes) {
        mTrackCfg.setTimeoutPre(timeoutPreMinutes);
        updateTrackCfgView();
    }
    private void onClickTimeoutPostConfig(int timeoutPostMinutes) {
        mTrackCfg.setTimeoutPost(timeoutPostMinutes);
        updateTrackCfgView();
    }
    private void onClickCancelConfig() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    private void onClickDoneConfig() {
        // May have to create a new entry in Monitoring Configuration table
        if (mTrackCfg.equals(mTrackCfgBackup) == false)
//            if (mToCreateBackup)
            mTrackCfg.create(getBaseContext());
//            else
//                mMonCfg.update(getBaseContext());
        Intent intent = new Intent();
        intent.putExtra(GuardTrackerActivity.RESULT_MON_CFG, mTrackCfg.get_id());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void startSmsCriteriaDialog() {
        DialogGenericIcTtMs2Bt dialogFragment = new TrackingCfgSmsCriteriaDialogFragment();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogGenericIcTtMs2Bt.ICON_ID_KEY, android.R.drawable.ic_menu_edit);
        bundle.putInt(DialogGenericIcTtMs2Bt.TITLE_ID_KEY, R.string.dialog_cfg_sms_criteria_title);
        bundle.putInt(DialogGenericIcTtMs2Bt.MESSAGE_ID_KEY, R.string.dialog_track_cfg_sms_criteria_message_body);
        bundle.putInt(DialogGenericIcTtMs2Bt.YES_BUTTON_LABEL_ID_KEY, R.string.done_button_label);
        bundle.putInt(DialogGenericIcTtMs2Bt.NO_BUTTON_LABEL_ID_KEY, R.string.cancel_button_label);
        bundle.putInt(TrackingCfgSmsCriteriaDialogFragment.CFG_CURRENT_SMS_CRITERIA_ARG, mTrackCfg.getSmsCriteria().ordinal());
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, TrackingCfgDialogs.SmsCriteriaDialog.toString());
    }
    private void startGpsThresholdDialog() {
        DialogGenericIcTtMs2Bt dialogFragment = new CfgGpsThresholdDialogFragment();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogGenericIcTtMs2Bt.ICON_ID_KEY, android.R.drawable.ic_menu_edit);
        bundle.putInt(DialogGenericIcTtMs2Bt.TITLE_ID_KEY, R.string.dialog_cfg_gps_threshold_title);
        bundle.putInt(DialogGenericIcTtMs2Bt.MESSAGE_ID_KEY, R.string.dialog_cfg_gps_threshold_message_body);
        bundle.putInt(DialogGenericIcTtMs2Bt.YES_BUTTON_LABEL_ID_KEY, R.string.done_button_label);
        bundle.putInt(DialogGenericIcTtMs2Bt.NO_BUTTON_LABEL_ID_KEY, R.string.cancel_button_label);
        bundle.putInt(CfgGpsThresholdDialogFragment.CFG_CURRENT_GPS_THRESHOLD_IN_METERS_ARG, mTrackCfg.getGpsThreshold());
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, TrackingCfgDialogs.GpsThresholdDialog.toString());
    }
    private void startGpsFovDialog() {
        DialogGenericIcTtMs2Bt dialogFragment = new CfgGpsFovDialogFragment();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogGenericIcTtMs2Bt.ICON_ID_KEY, android.R.drawable.ic_menu_edit);
        bundle.putInt(DialogGenericIcTtMs2Bt.TITLE_ID_KEY, R.string.dialog_cfg_gps_fov_title);
        bundle.putInt(DialogGenericIcTtMs2Bt.MESSAGE_ID_KEY, R.string.dialog_cfg_gps_fov_message_body);
        bundle.putInt(DialogGenericIcTtMs2Bt.YES_BUTTON_LABEL_ID_KEY, R.string.done_button_label);
        bundle.putInt(DialogGenericIcTtMs2Bt.NO_BUTTON_LABEL_ID_KEY, R.string.cancel_button_label);
        bundle.putInt(CfgGpsFovDialogFragment.CFG_CURRENT_GPS_FOV_ARG, mTrackCfg.getGpsFov());
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, TrackingCfgDialogs.GpsFovDialog.toString());
    }
    private void startGpsTimeoutDialog() {
        DialogGenericIcTtMs2Bt dialogFragment = new CfgTimePickerDialogFragment();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogGenericIcTtMs2Bt.ICON_ID_KEY, android.R.drawable.ic_menu_edit);
        bundle.putInt(DialogGenericIcTtMs2Bt.TITLE_ID_KEY, R.string.dialog_cfg_gps_timeout_title);
        bundle.putInt(DialogGenericIcTtMs2Bt.MESSAGE_ID_KEY, R.string.dialog_cfg_gps_timeout_message_body);
        bundle.putInt(DialogGenericIcTtMs2Bt.YES_BUTTON_LABEL_ID_KEY, R.string.done_button_label);
        bundle.putInt(DialogGenericIcTtMs2Bt.NO_BUTTON_LABEL_ID_KEY, R.string.cancel_button_label);
        bundle.putInt(CfgTimePickerDialogFragment.CFG_CURRENT_TIME_IN_MINUTES_ARG, mTrackCfg.getGpsTimeout());
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, TrackingCfgDialogs.GpsTimeoutDialog.toString());
    }
    private void startTimeoutTrackingDialog() {
        DialogGenericIcTtMs2Bt dialogFragment = new CfgTimePickerDialogFragment();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogGenericIcTtMs2Bt.ICON_ID_KEY, android.R.drawable.ic_menu_edit);
        bundle.putInt(DialogGenericIcTtMs2Bt.TITLE_ID_KEY, R.string.dialog_cfg_timeout_tracking_title);
        bundle.putInt(DialogGenericIcTtMs2Bt.MESSAGE_ID_KEY, R.string.dialog_cfg_timeout_tracking_message_body);
        bundle.putInt(DialogGenericIcTtMs2Bt.YES_BUTTON_LABEL_ID_KEY, R.string.done_button_label);
        bundle.putInt(DialogGenericIcTtMs2Bt.NO_BUTTON_LABEL_ID_KEY, R.string.cancel_button_label);
        bundle.putInt(CfgTimePickerDialogFragment.CFG_CURRENT_TIME_IN_MINUTES_ARG, mTrackCfg.getTimeoutTracking());
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, TrackingCfgDialogs.TimeoutTrackingDialog.toString());
    }
    private void startTimeoutPreDialog() {
        DialogGenericIcTtMs2Bt dialogFragment = new CfgTimePickerDialogFragment();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogGenericIcTtMs2Bt.ICON_ID_KEY, android.R.drawable.ic_menu_edit);
        bundle.putInt(DialogGenericIcTtMs2Bt.TITLE_ID_KEY, R.string.dialog_cfg_timeout_pre_title);
        bundle.putInt(DialogGenericIcTtMs2Bt.MESSAGE_ID_KEY, R.string.dialog_cfg_timeout_pre_message_body);
        bundle.putInt(DialogGenericIcTtMs2Bt.YES_BUTTON_LABEL_ID_KEY, R.string.done_button_label);
        bundle.putInt(DialogGenericIcTtMs2Bt.NO_BUTTON_LABEL_ID_KEY, R.string.cancel_button_label);
        bundle.putInt(CfgTimePickerDialogFragment.CFG_CURRENT_TIME_IN_MINUTES_ARG, mTrackCfg.getTimeoutPre());
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, TrackingCfgDialogs.TimeoutPreDialog.toString());
    }
    private void startTimeoutPostDialog() {
        DialogGenericIcTtMs2Bt dialogFragment = new CfgTimePickerDialogFragment();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogGenericIcTtMs2Bt.ICON_ID_KEY, android.R.drawable.ic_menu_edit);
        bundle.putInt(DialogGenericIcTtMs2Bt.TITLE_ID_KEY, R.string.dialog_cfg_timeout_post_title);
        bundle.putInt(DialogGenericIcTtMs2Bt.MESSAGE_ID_KEY, R.string.dialog_cfg_timeout_post_message_body);
        bundle.putInt(DialogGenericIcTtMs2Bt.YES_BUTTON_LABEL_ID_KEY, R.string.done_button_label);
        bundle.putInt(DialogGenericIcTtMs2Bt.NO_BUTTON_LABEL_ID_KEY, R.string.cancel_button_label);
        bundle.putInt(CfgTimePickerDialogFragment.CFG_CURRENT_TIME_IN_MINUTES_ARG, mTrackCfg.getTimeoutPost());
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, TrackingCfgDialogs.TimeoutPostDialog.toString());
    }
    private void startCancelConfigDialog() {
        DialogGenericIcTtMs2Bt dialogFragment = new DialogGenericIcTtMs2Bt();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogGenericIcTtMs2Bt.ICON_ID_KEY, android.R.drawable.ic_dialog_alert);
        bundle.putInt(DialogGenericIcTtMs2Bt.TITLE_ID_KEY, R.string.dialog_track_cfg_cancel_title);
        bundle.putInt(DialogGenericIcTtMs2Bt.MESSAGE_ID_KEY, R.string.dialog_track_cfg_cancel_message_body);
        bundle.putInt(DialogGenericIcTtMs2Bt.YES_BUTTON_LABEL_ID_KEY, android.R.string.yes);
        bundle.putInt(DialogGenericIcTtMs2Bt.NO_BUTTON_LABEL_ID_KEY, android.R.string.no);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, TrackingCfgActivity.TrackingCfgDialogs.TrackCfgCancelDialog.toString());
    }
    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        dialog.dismiss();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        String dialogTag = dialog.getTag();
        if (TrackingCfgActivity.TrackingCfgDialogs.SmsCriteriaDialog.toString().equals(dialogTag)) {
            TrackingConfiguration.SmsCriteria smsCriteria = ((TrackingCfgSmsCriteriaDialogFragment)dialog).getNewSmsCriteria();
            onClickSmsCriteriaConfig(smsCriteria);
            dialog.dismiss();
            return;
        }
        if (TrackingCfgActivity.TrackingCfgDialogs.GpsThresholdDialog.toString().equals(dialogTag)) {
            int gpsThresholdMeters = ((CfgGpsThresholdDialogFragment)dialog).getNewGpsThreshold();
            onClickGpsThresholdConfig(gpsThresholdMeters);
            dialog.dismiss();
            return;
        }
        if (TrackingCfgActivity.TrackingCfgDialogs.GpsFovDialog.toString().equals(dialogTag)) {
            int gpsFov = ((CfgGpsFovDialogFragment)dialog).getNewGpsFov();
            onClickGpsFovConfig(gpsFov);
            dialog.dismiss();
            return;
        }
        if (TrackingCfgActivity.TrackingCfgDialogs.GpsTimeoutDialog.toString().equals(dialogTag)) {
            int gpsTimeoutMinutes = ((CfgTimePickerDialogFragment)dialog).getNewTime();
            onClickGpsTimeoutConfig(gpsTimeoutMinutes);
            dialog.dismiss();
            return;
        }
        if (TrackingCfgActivity.TrackingCfgDialogs.TimeoutTrackingDialog.toString().equals(dialogTag)) {
            int timeoutMinutes = ((CfgTimePickerDialogFragment)dialog).getNewTime();
            onClickTimeoutTrackingConfig(timeoutMinutes);
            dialog.dismiss();
            return;
        }
        if (TrackingCfgActivity.TrackingCfgDialogs.TimeoutPreDialog.toString().equals(dialogTag)) {
            int timeoutMinutes = ((CfgTimePickerDialogFragment)dialog).getNewTime();
            onClickTimeoutPreConfig(timeoutMinutes);
            dialog.dismiss();
            return;
        }
        if (TrackingCfgActivity.TrackingCfgDialogs.TimeoutPostDialog.toString().equals(dialogTag)) {
            int timeoutMinutes = ((CfgTimePickerDialogFragment)dialog).getNewTime();
            onClickTimeoutPostConfig(timeoutMinutes);
            dialog.dismiss();
            return;
        }
        if (TrackingCfgActivity.TrackingCfgDialogs.TrackCfgCancelDialog.toString().equals(dialogTag)) {
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

        System.out.println("TrackingCfgActivity onStop()");
    }


    @Override
    protected void onPause() {
        super.onPause();

        System.out.println("TrackingCfgActivity onPause()");

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        System.out.println("TrackingCfgActivity onSaveInstanceState()");

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        System.out.println("TrackingCfgActivity onRestart()");
    }
}
