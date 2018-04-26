package com.patri.guardtracker;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;
import com.patri.guardtracker.model.GuardTracker;
import com.patri.guardtracker.model.GuardTrackerContract;
import java.util.ArrayList;

/**
 * Created by patri on 05/11/2016.
 */
public class DevicesEliminateActivity extends AppCompatActivity implements DialogListener {
    private static final String TAG = DevicesEliminateActivity.class.getSimpleName();

    private ArrayList<GuardTracker> mGuardTrackerList;

    private GuardTracker mGuardTracker;
    private ArrayList<SmsMessage> mSessionList;
    private boolean[] mItemCheckedState;
    private int mItemCheckedCount;
    private Button mImportButton;
    private Cursor mCursor;
    private ListView mListView;

    private void startConfirmationDialog() {
        DialogGenericIcTtMs2Bt confirmationDialogFragment = new DialogGenericIcTtMs2Bt();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogGenericIcTtMs2Bt.ICON_ID_KEY, android.R.drawable.ic_dialog_alert);
        bundle.putInt(DialogGenericIcTtMs2Bt.TITLE_ID_KEY, R.string.dialog_eliminate_devices_title);
        bundle.putInt(DialogGenericIcTtMs2Bt.MESSAGE_ID_KEY, R.string.dialog_eliminate_devices_message_body);
        bundle.putInt(DialogGenericIcTtMs2Bt.YES_BUTTON_LABEL_ID_KEY, android.R.string.yes);
        bundle.putInt(DialogGenericIcTtMs2Bt.NO_BUTTON_LABEL_ID_KEY, android.R.string.no);
        confirmationDialogFragment.setArguments(bundle);
        confirmationDialogFragment.show(manager, GuardTrackerActivity.DialogFragmentTags.DevicesEliminate.toString());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_devices_eliminate);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create a progress bar to display while the list loads
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progressBar.setIndeterminate(true);

        mListView = (ListView) findViewById(R.id.devices_eliminate_list_view);
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

        setTitle(R.string.title_activity_devices_eliminate);

        mCursor = GuardTracker.readCursor(this.getBaseContext(), null, null);
        if (mCursor.getCount() == 0) {
            Toast.makeText(getBaseContext(), R.string.list_empty_body_message, Toast.LENGTH_LONG).show();
        } else {
            View v = findViewById(android.R.id.empty);
            v.setVisibility(View.GONE);

            String[] fromColumns = {
                    GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_NAME,
                    GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_GSM_ID
            };
            int[] toViews = {android.R.id.text1, android.R.id.text2};
            SimpleCursorAdapter cursorAdapter =
                    new DevicesEliminateCursorAdapter(getBaseContext(), R.layout.list_item_radio_button_two_line, mCursor, fromColumns, toViews, 0);

            mItemCheckedState = new boolean[cursorAdapter.getCount()];
            mItemCheckedCount = 0;
            mListView.setAdapter(cursorAdapter);

            mListView.setOnItemClickListener(new ListView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView parent, View view, final int position, long id) {
                    ListView listView1 = (ListView) parent;
                    boolean isChecked = mItemCheckedState[position];
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
        }

        Button cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick cancel button");
                DevicesEliminateActivity.this.
                finish();
            }
        });
        mImportButton = (Button) findViewById(R.id.import_button);
        mImportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick go button");
                startConfirmationDialog();
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


    class DevicesEliminateCursorAdapter extends SimpleCursorAdapter {
        public DevicesEliminateCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup viewGroup = null;
            viewGroup = (ViewGroup) super.getView(position, convertView, parent);
            RadioButton radioButton = (RadioButton)viewGroup.findViewById(R.id.radio_button);
            radioButton.setChecked(mItemCheckedState[position]);

            return viewGroup;
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // Nothing to be done
        dialog.dismiss();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {

        int devicesEliminated = 0;
        // Get collection as array of ints of all selected TrackSessions.
        for (int position = 0, cnt = 0; position < mItemCheckedState.length; position++) {
            if (mItemCheckedState[position]) {
                mCursor.moveToPosition(position);

                int id = mCursor.getInt(mCursor.getColumnIndex(GuardTrackerContract.GuardTrackerTable._ID));
                devicesEliminated += GuardTracker.deleteDeep(getBaseContext(), id) ? 1 : 0;

                cnt += 1;
                if (cnt == mItemCheckedCount)
                    break;
            }
        }

        Toast.makeText(getBaseContext(), devicesEliminated == mItemCheckedCount ?
                R.string.dialog_eliminate_devices_message_successful :
                R.string.dialog_eliminate_devices_message_unsuccessful,
                Toast.LENGTH_LONG).show();

        dialog.dismiss();
        recreate();
    }
}
