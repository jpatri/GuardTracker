package com.patri.guardtracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.patri.guardtracker.model.GuardTracker;
import com.patri.guardtracker.model.VigilanceConfiguration;

public class VigilanceCfgActivity extends AppCompatActivity {
    private static final String TAG = VigilanceConfiguration.class.getSimpleName();

    private VigilanceConfiguration mVigilanceCfg;
    private int mVigilanceCfgId;
    private int mGuardTrackerId;
    private String mGuardTrackerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_vigilance_cfg);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create a progress bar to display while the list loads
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progressBar.setIndeterminate(true);

        final ListView listView = (ListView)findViewById(R.id.vig_cfg_list_view);
        listView.setEmptyView(progressBar);

        // Must add the progress bar to the root of the layout
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
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

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);

        Intent intent = getIntent();
        mGuardTrackerId = intent.getIntExtra(GuardTrackerActivity.GUARD_TRACKER_ID, 0);
        mVigilanceCfgId = intent.getIntExtra(GuardTrackerActivity.CONFIG_ID, 0);
        if (mGuardTrackerId == 0 || mVigilanceCfgId == 0) {
            mGuardTrackerId = preferences.getInt(getString(R.string.saved_guard_tracker_id), 0);
            mVigilanceCfgId = preferences.getInt(getString(R.string.saved_guard_tracker_tracking_cfg_id), 0);
        }
        if (mGuardTrackerId == 0 || mVigilanceCfgId == 0) {
            // Return to last activity
            // ToDo
        }

        mVigilanceCfg = VigilanceConfiguration.read(getBaseContext(), mVigilanceCfgId);

        final String [][] elems = {
                { getString(R.string.vig_cfg_item_title_tilt_sensitivity), mVigilanceCfg.getPrettyTiltSensitivity() },
                { getString(R.string.vig_cfg_item_title_ble_advertise_period), mVigilanceCfg.getPrettyBleAdvertisePeriod() }
        };

        // A simple ArrayAdapter can only be represented by a TextView
        ArrayAdapter adapter = new ArrayAdapter(getBaseContext(), android.R.layout.simple_list_item_2, android.R.id.text1, elems) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setText(elems[position][0]);
                text2.setText(elems[position][1]);
                return view;
            }
        };

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "listView.onItemSelected: position = " + position + " id = " + id);
                listView.setItemChecked(position, true);
                // ToDo:

            }
        });


        GuardTracker guardTracker = GuardTracker.read(getBaseContext(), mGuardTrackerId);
        mGuardTrackerName = guardTracker.getName();
        setTitle(getString(R.string.title_activity_vigilance_cfg) + ": " + mGuardTrackerName);
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
