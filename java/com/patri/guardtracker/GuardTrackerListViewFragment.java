package com.patri.guardtracker;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.patri.guardtracker.model.GuardTracker;
import com.patri.guardtracker.model.GuardTrackerContract;

import java.util.ArrayList;

/**
 * Created by patri on 08/06/2016.
 */
public class GuardTrackerListViewFragment extends Fragment {
    private static String TAG = GuardTrackerListViewFragment.class.getSimpleName();

    private ArrayList<GuardTracker> mGuardTrackerList;
    private Cursor mCursor; // Retirar este campo quando o cursor puder ser fechado no mesmo contexto onde foi aberto.
    private ListView mListView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     */
    // TODO: Rename and change types and number of parameters
    public static GuardTrackerListViewFragment newInstance() {
        GuardTrackerListViewFragment fragment = new GuardTrackerListViewFragment();
//        Bundle args = new Bundle();
//        args.putInt(ARG_PARAM1, guardTrackerId);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        //mGuardTrackerList = GuardTracker.read(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.i(TAG, "onCreateView()");

        View view = inflater.inflate(R.layout.fragment_guard_tracker_list_view, container, false);

        mListView = (ListView)view.findViewById(R.id.guard_tracker_list_view);

        // Coloquei aqui e removi do onCreate.
        mGuardTrackerList = GuardTracker.read(getContext());

        mCursor = GuardTracker.readCursor(this.getContext(), null, null);

//        if (mCursor.getCount() == 0) {
//            String [] noElemsStr =
//                      {"List empty: no devices paired with this App."};
//            ArrayAdapter<String> itens =
//                      new ArrayAdapter<String>(this.getContext(), R.layout.guardtracker_list_item, noElemsStr);
//            listView.setAdapter(itens);
//
        if (mCursor.getCount() == 0) {

                Context context = getContext();
                CharSequence text = "Go to Scan and Pair";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
        } else {

            View v = view.findViewById(android.R.id.empty);
            v.setVisibility(View.GONE);

//            container.removeView(view.findViewById(android.R.id.empty));

            String[] fromColumns = {
                    GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_NAME,
                    GuardTrackerContract.GuardTrackerTable.COLUMN_NAME_GSM_ID
            };
/*            int[] toViews = {R.id.device_name, R.id.device_address};
            SimpleCursorAdapter cursorAdapter =
                    new SimpleCursorAdapter(this.getContext(), R.layout.list_item_device_id, mCursor, fromColumns, toViews, 0);
*/
            // ELIMINAR O CÓDIGO ANTERIOR DEPOIS DE TESTAR O ECRÃ com o TwoLine
            int[] toViews = {android.R.id.text1, android.R.id.text2};
            SimpleCursorAdapter cursorAdapter =
                    new SimpleCursorAdapter(this.getContext(), R.layout.list_item_two_line, mCursor, fromColumns, toViews, 0);

            mListView.setAdapter(cursorAdapter);
            mListView.setOnItemClickListener(new ListView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView parent, View view, int position, long id) {
                    Log.i(TAG, "onItemClick: item checked: position = " + position + "; id = " + id);
                    mListView.setItemChecked(position, true);
                    GuardTracker guardTracker = mGuardTrackerList.get(position);
                    Intent intent = new Intent(getContext(), GuardTrackerActivity.class);
                    int guardTrackerId = guardTracker.get_id();
                    intent.putExtra(MainActivity.GUARD_TRACKER_ID, guardTrackerId);
                    startActivity(intent);
                }
            });

        }

        return view;
    }



    @Override
    public void onStop() {
        Log.i(TAG, "onStop()");
        super.onStop();
        GuardTracker.unload(mCursor);
        mCursor = null;
        mListView = null;
    }

}
