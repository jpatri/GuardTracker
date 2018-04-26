package com.patri.guardtracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;

import com.patri.guardtracker.model.GuardTrackerContract;
import com.patri.guardtracker.model.TrackSession;

import java.util.ArrayList;

/**
 * Created by patri on 20/09/2016.
 */
public class TrackSessionsSelectionActivity extends AppCompatActivity {
    private static final String TAG = TrackSessionsSelectionActivity.class.getSimpleName();

    private int mGuardTrackerId;
    private String mGuardTrackerName;
    private ArrayList<TrackSession> mSessionList;
    private Cursor mCursor;
    private boolean [] mItemCheckedState;
    private int mItemCheckedCount;
    private Button mGoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_track_sessions_selection);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create a progress bar to display while the list loads
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progressBar.setIndeterminate(true);

        ListView listView = (ListView) findViewById(R.id.track_session_list_view);
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
        mGuardTrackerName = intent.getStringExtra(GuardTrackerActivity.GUARD_TRACKER_NAME);
        if (mGuardTrackerId == 0) {
            mGuardTrackerId = preferences.getInt(getString(R.string.saved_guard_tracker_id), 0);
            mGuardTrackerName = preferences.getString(getString(R.string.saved_guard_tracker_name), null);
        }
        if (mGuardTrackerId == 0) {
            // Return to last activity
            // ToDo
            finish();
        }

        setTitle(getString(R.string.title_activity_track_session_selection) + ": " + mGuardTrackerName);

        mCursor = TrackSession.readCursorFromGuardTrackerId(getBaseContext(), mGuardTrackerId);

        String[] fromColumns = {
                GuardTrackerContract.TrackSessionTable.COLUMN_NAME_DATE,
                GuardTrackerContract.TrackSessionTable.COLUMN_NAME_NAME
        };
        int[] toViews = {android.R.id.text1, android.R.id.text2};
        SimpleCursorAdapter cursorAdapter =
                new SimpleCursorAdapter(getBaseContext(), R.layout.list_item_radio_button_two_line, mCursor, fromColumns, toViews, 0);

        listView.setAdapter(cursorAdapter);

        mItemCheckedState = new boolean [cursorAdapter.getCount()];
        mItemCheckedCount = 0;
        listView.setOnItemClickListener(new ListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                ListView listView1 = (ListView)parent;
                boolean isChecked =  mItemCheckedState[position];
                Log.i(TAG, "onItemClick: item checked: position = " + position + "; id = " + id + "; isChecked = " + !isChecked);
                listView1.setItemChecked(position, !isChecked);
                RadioButton radioButton = (RadioButton) view.findViewById(R.id.radio_button);
                radioButton.setChecked(!isChecked);
                mItemCheckedState[position] = !isChecked;

                mItemCheckedCount += (isChecked ? -1 : +1);
                mGoButton.setEnabled(mItemCheckedCount > 0);

            }
        });

        Button cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick cancel button");
                finish();
            }
        });
        mGoButton = (Button) findViewById(R.id.go_button);
        mGoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick go button");
                // Get collection as array of ints of all selected TrackSessions.
                // ToDo
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.i(TAG, "onStart");

    }

    @Override
    protected void onStop() {
        super.onStop();

        System.out.println("MonitoringCfgActivity onStop()");

        if (mCursor != null)
            mCursor.close();
        mCursor = null;
        TrackSession.unload();
    }


    @Override
    protected void onPause() {
        super.onPause();

        System.out.println("MonitoringCfgActivity onPause()");

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        System.out.println("MonitoringCfgActivity onSaveInstanceState()");

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        System.out.println("MonitoringCfgActivity onRestart()");
    }
}