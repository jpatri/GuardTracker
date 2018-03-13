package com.patri.guardtracker.sms;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.patri.guardtracker.GuardTrackerActivity;
import com.patri.guardtracker.R;
import com.patri.guardtracker.model.GuardTracker;
import com.patri.guardtracker.model.MonitoringInfo;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by patri on 21/09/2016.
 */
public class SmsReceivedEarlierActivity extends AppCompatActivity {
    private static final String TAG = SmsReceivedEarlierActivity.class.getSimpleName();

    private int mGuardTrackerId;
    private String mGuardTrackerName;
    private GuardTracker mGuardTracker;
    private ArrayList<SmsMessage> mSessionList;
    private boolean[] mItemCheckedState;
    private int mItemCheckedCount;
    private Button mImportButton;
    private Cursor mCursor;
    private ListView mListView;

    static class ItemChecked {
        RadioButton radioButton;
        boolean isChecked;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sms_received_earlier);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create a progress bar to display while the list loads
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progressBar.setIndeterminate(true);

        mListView = (ListView) findViewById(R.id.sms_received_list_view);
        mListView.setEmptyView(progressBar);

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
//        if (mGuardTrackerId == 0) {
//            mGuardTrackerId = preferences.getInt(getString(R.string.saved_guard_tracker_id), 0);
//            mGuardTrackerName = preferences.getString(getString(R.string.saved_guard_tracker_name), null);
//        }
        if (mGuardTrackerId == 0) {
            mGuardTrackerName = "";
            mCursor = SmsInboxUtils.readSmsCursor(getBaseContext(), 0, false, null, null);
        } else {

            mGuardTracker = GuardTracker.read(getBaseContext(), mGuardTrackerId);
            String phonenumber = mGuardTracker.getGsmId();
            ArrayList<MonitoringInfo> monInfoList = MonitoringInfo.readList(getBaseContext(), mGuardTrackerId);
            mCursor = SmsInboxUtils.readSmsCursor(getBaseContext(), 0, false, phonenumber, monInfoList);

        }

        setTitle(getString(R.string.title_activity_sms_received_earlier) + ": " + mGuardTrackerName);

        String[] fromColumns = {
                SmsInboxUtils.SMS_DATE_COLUMN,
                SmsInboxUtils.SMS_ADDRESS_COLUMN,
                SmsInboxUtils.SMS_BODY_COLUMN,
                SmsInboxUtils.SMS_READED_COLUMN
        };
        int[] toViews = {R.id.date_view, R.id.from_view, R.id.body_view, R.id.read_view};
        final SimpleCursorAdapter cursorAdapter =
                new SmsCursorAdapter(getBaseContext(), R.layout.list_item_radio_button_sms_received_earlier, mCursor, fromColumns, toViews, 0);

        mItemCheckedState = new boolean [cursorAdapter.getCount()];
        mItemCheckedCount = 0;
        // Modify the view representation of a date in sms list view item.
        // An alternate implementation could be to define a custom CursorAdapter extending SimpleCursor Adapter.
        cursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                final int dateColumnIndex = cursor.getColumnIndex(SmsInboxUtils.SMS_DATE_COLUMN);
                if (columnIndex == dateColumnIndex) {
                    long milliseconds = cursor.getLong(columnIndex);
                    Date date = new Date(milliseconds);
                    String sDate = String.format("%1$td/%1$tm/%1$tY %1$tR", date);
                    ((TextView)view).setText(sDate);
                    return true;
                }
                final int readedColumnIndex = cursor.getColumnIndex(SmsInboxUtils.SMS_READED_COLUMN);
                if (columnIndex == readedColumnIndex) {
                    int read = cursor.getInt(columnIndex);
                    ImageView imgView = (ImageView)view;
                    int drawableResId = read == 1 ? R.drawable.ic_message_read : R.drawable.ic_message;
                    imgView.setImageDrawable(getDrawable(drawableResId));
                    return true;
                }
                ((TextView)view).setText(cursor.getString(columnIndex));
                return true;
            }

        });
        mListView.setAdapter(cursorAdapter);

        mListView.setOnItemClickListener(new ListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView parent, View view, final int position, long id) {
                ListView listView1 = (ListView)parent;
                boolean isChecked =  mItemCheckedState[position];
                isChecked = !isChecked;
                Log.i(TAG, "onItemClick: item checked: position = " + position + "; id = " + id + "; isChecked = " + isChecked);
                RadioButton radioButton = (RadioButton) view.findViewById(R.id.radio_button);
                radioButton.setChecked(isChecked);
                mItemCheckedState[position] = isChecked;
                mItemCheckedCount += (isChecked ? +1 : -1);
                mImportButton.setEnabled(mItemCheckedCount > 0);
                listView1.setItemChecked(position, isChecked);

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
        mImportButton = (Button) findViewById(R.id.import_button);
        mImportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick go button");
                // Get collection as array of ints of all selected TrackSessions.

                for (int position = 0, cnt = 0; position < mItemCheckedState.length; position++) {
                    if (mItemCheckedState[position]) {
                        mCursor.moveToPosition(position);
                        long miliseconds = mCursor.getLong(mCursor.getColumnIndex(SmsInboxUtils.SMS_DATE_COLUMN));
                        String from = mCursor.getString(mCursor.getColumnIndex(SmsInboxUtils.SMS_ADDRESS_COLUMN));
                        String body = mCursor.getString(mCursor.getColumnIndex(SmsInboxUtils.SMS_BODY_COLUMN));
                        String readed = mCursor.getString(mCursor.getColumnIndex(SmsInboxUtils.SMS_READED_COLUMN));
                        long threadId = mCursor.getLong(mCursor.getColumnIndex(SmsInboxUtils.SMS_THREAD_ID_COLUMN));
                        long contactId = mCursor.getLong(mCursor.getColumnIndex(SmsInboxUtils.SMS_PERSON_COLUMN));

                        SmsBodyProcessor.process(getBaseContext(), mGuardTrackerId, miliseconds, body, mGuardTracker);

                        cnt += 1;
                        if (cnt == mItemCheckedCount)
                            break;
                    }
                }

                finish();
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

        Log.i(TAG, "onStop()");
        if (mCursor != null)
            mCursor.close();
        mCursor = null;
    }


    @Override
    protected void onPause() {
        super.onPause();

        Log.i(TAG, "onPause()");

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        Log.i(TAG, "onSaveInstanceState()");

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        Log.i(TAG, "onRestart()");
    }


    class SmsCursorAdapter extends SimpleCursorAdapter {
        public SmsCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup viewGroup = null;
            viewGroup = (ViewGroup) super.getView(position, convertView, parent);
            RadioButton radioButton = (RadioButton)viewGroup.findViewById(R.id.radio_button);
            radioButton.setChecked(mItemCheckedState[position]);

            // Não é necessário descriminar. Parte do trabalho que estava a fazer substituia o trabalho realizado pelo SimpleCursorAdapter, ou seja,
            // o trabalho associado à actualização dos restantes componentes para além do RadioButton.
//            if (convertView == null) {
//                viewGroup = (ViewGroup) super.getView(position, convertView, parent);
//            } else {
//                viewGroup = (ViewGroup)convertView;
//
//                mCursor.moveToPosition(position);
//                long miliseconds = mCursor.getLong(mCursor.getColumnIndex(SmsInboxUtils.SMS_DATE_COLUMN));
//                String from = mCursor.getString(mCursor.getColumnIndex(SmsInboxUtils.SMS_ADDRESS_COLUMN));
//                String body = mCursor.getString(mCursor.getColumnIndex(SmsInboxUtils.SMS_BODY_COLUMN));
//                String readed = mCursor.getString(mCursor.getColumnIndex(SmsInboxUtils.SMS_READED_COLUMN));
//
//                TextView dateView = (TextView)viewGroup.findViewById(R.id.date_view);
//                TextView fromView = (TextView)viewGroup.findViewById(R.id.from_view);
//                TextView bodyView = (TextView)viewGroup.findViewById(R.id.body_view);
//                TextView readedView = (TextView)viewGroup.findViewById(R.id.readed_view);
//                RadioButton radioButton = (RadioButton)viewGroup.findViewById(R.id.radio_button);
//                dateView.setText(String.format("%1$td/%1$tm/%1$tY %1$tR", miliseconds));
//                fromView.setText(from);
//                bodyView.setText(body);
//                readedView.setText(readed);
//                radioButton.setChecked(mItemCheckedState[position]);
//            }
            return viewGroup;
        }
    }
}