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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.patri.guardtracker.model.GuardTracker;
import com.patri.guardtracker.model.TrackingConfiguration;

public class TrackingCfgActivity extends AppCompatActivity {
    private static final String TAG = TrackingCfgActivity.class.getSimpleName();

    private TrackingConfiguration mTrackCfg;
    private int mTrackCfgId;
    private int mGuardTrackerId;
    private String mGuardTrackerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tracking_cfg);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create a progress bar to display while the list loads
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progressBar.setIndeterminate(true);

        final ListView listView = (ListView)findViewById(R.id.track_cfg_list_view);
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
        mTrackCfgId = intent.getIntExtra(GuardTrackerActivity.CONFIG_ID, 0);
        if (mGuardTrackerId == 0 || mTrackCfgId == 0) {
            mGuardTrackerId = preferences.getInt(getString(R.string.saved_guard_tracker_id), 0);
            mTrackCfgId = preferences.getInt(getString(R.string.saved_guard_tracker_tracking_cfg_id), 0);
        }
        if (mGuardTrackerId == 0 || mTrackCfgId == 0) {
            // Return to last activity
            // ToDo
        }

        mTrackCfg = TrackingConfiguration.read(getBaseContext(), mTrackCfgId);

        final String [][] elems = {
                { getString(R.string.track_cfg_item_title_sms_criteria), mTrackCfg.getPrettySmsCriteria() },
                { getString(R.string.track_cfg_item_title_gps_threshold), mTrackCfg.getPrettyGpsThreshold() },
                { getString(R.string.track_cfg_item_title_gps_fov), mTrackCfg.getPrettyGpsFov() },
                { getString(R.string.track_cfg_item_title_gps_timeout), mTrackCfg.getPrettyGpsTimeout() },
                { getString(R.string.track_cfg_item_title_timeout_tracking), mTrackCfg.getPrettyTimeoutTracking() },
                { getString(R.string.track_cfg_item_title_timeout_pre), mTrackCfg.getPrettyTimeoutPre() },
                { getString(R.string.track_cfg_item_title_timeout_post), mTrackCfg.getPrettyTimeoutPost() }
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
        setTitle(getString(R.string.title_activity_tracking_cfg) + ": " + mGuardTrackerName);
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
