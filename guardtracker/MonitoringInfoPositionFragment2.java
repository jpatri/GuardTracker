package com.patri.guardtracker;


import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.patri.guardtracker.model.GuardTrackerContract;
import com.patri.guardtracker.model.MonitoringInfo;
import com.patri.guardtracker.model.Position;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MonitoringInfoPositionFragment2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MonitoringInfoPositionFragment2 extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "guardTrackerId";

    // TODO: Rename and change types of parameters
    private int guardTrackerId;
    private ArrayList<Position> positions;
    private Cursor cursor;

    public MonitoringInfoPositionFragment2() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     *
     * @param guardTrackerId@return A new instance of fragment MonitoringInfoPositionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MonitoringInfoPositionFragment2 newInstance(int guardTrackerId) {
        MonitoringInfoPositionFragment2 fragment = new MonitoringInfoPositionFragment2();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, guardTrackerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            guardTrackerId = getArguments().getInt(ARG_PARAM1);
        }

        ArrayList<MonitoringInfo> monInfoList = MonitoringInfo.readList(getContext(), guardTrackerId);
        int i = 0;
        int [] positionFks = new int [monInfoList.size()];
        for (MonitoringInfo monInfo: monInfoList) {
            positionFks[i] = monInfo.getPositionId();
            i += 1;
        }
        cursor = Position.readCursor(getContext(), positionFks);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_position_info3, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        View view = getView();
        ListView positionListView = (ListView)view.findViewById(R.id.position_listView);
        String[] fromProjection = {
                GuardTrackerContract.PositionTable.COLUMN_NAME_LT,
                GuardTrackerContract.PositionTable.COLUMN_NAME_LG,
                GuardTrackerContract.PositionTable.COLUMN_NAME_TIME,
                GuardTrackerContract.PositionTable.COLUMN_NAME_ALTITUDE,
                GuardTrackerContract.PositionTable.COLUMN_NAME_NSAT,
                GuardTrackerContract.PositionTable.COLUMN_NAME_HDOP,
                GuardTrackerContract.PositionTable.COLUMN_NAME_FIXED
        };
        int [] toViews = {
                R.id.lt_position_list_item_textView,
                R.id.lg_position_list_item_textView,
                R.id.alt_position_list_item_textView,
                R.id.time_position_list_item_textView,
                R.id.nsat_position_list_item_textView,
                R.id.hdop_position_list_item_textView,
                R.id.fixed_position_list_item_textView
        };

        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(view.getContext(), R.layout.list_item_position_info, cursor, fromProjection, toViews, 0);
        positionListView.setAdapter(cursorAdapter);

//        positionCursor.moveToFirst();
//        positions = new ArrayList<>();
//
//        // Extract values from query result
//        nTuples = positionCursor.getCount();
//        for (int i = 0; i < nTuples; i++) {
//            int lt = positionCursor.getInt(positionCursor.getColumnIndex(GuardTrackerContract.PositionTable.COLUMN_NAME_LT));
//            int lg = positionCursor.getInt(positionCursor.getColumnIndex(GuardTrackerContract.PositionTable.COLUMN_NAME_LG));
//            int alt = positionCursor.getInt(positionCursor.getColumnIndex(GuardTrackerContract.PositionTable.COLUMN_NAME_ALTITUDE));
//            int time = positionCursor.getInt(positionCursor.getColumnIndex(GuardTrackerContract.PositionTable.COLUMN_NAME_TIME1));
//            int nSat = positionCursor.getInt(positionCursor.getColumnIndex(GuardTrackerContract.PositionTable.COLUMN_NAME_NSAT));
//            int hdop = positionCursor.getInt(positionCursor.getColumnIndex(GuardTrackerContract.PositionTable.COLUMN_NAME_HDOP));
//            int fixed = positionCursor.getInt(positionCursor.getColumnIndex(GuardTrackerContract.PositionTable.COLUMN_NAME_FIXED));
//            Position position = new Position();
//            positions.add(position);
//        }


    }

    @Override
    public void onStop() {
        super.onStop();
        Position.unload();
    }
}
