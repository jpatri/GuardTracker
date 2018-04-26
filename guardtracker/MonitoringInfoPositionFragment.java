package com.patri.guardtracker;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.patri.guardtracker.model.MonitoringInfo;
import com.patri.guardtracker.model.Position;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MonitoringInfoPositionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MonitoringInfoPositionFragment extends Fragment implements OnMapReadyCallback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "guardTrackerId";
    private static final String TAG = MonitoringInfoPositionFragment.class.getSimpleName();

    // TODO: Rename and change types of parameters
    private int guardTrackerId;
    private ArrayList<Position> positionList = new ArrayList<>();
    private GoogleMap mMap;

    public MonitoringInfoPositionFragment() {
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
    public static MonitoringInfoPositionFragment newInstance(int guardTrackerId) {
        MonitoringInfoPositionFragment fragment = new MonitoringInfoPositionFragment();
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
        } else
            guardTrackerId = 0;

        ArrayList<MonitoringInfo> monInfoList = MonitoringInfo.readList(getContext(), guardTrackerId);
        int [] positionsIds = new int[monInfoList.size()];
        for (int i = 0; i < monInfoList.size(); i++) {
            MonitoringInfo monInfo = monInfoList.get(i);
            positionsIds[i] = monInfo.getPositionId();
        }
        positionList = Position.readList(getContext(), positionsIds);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_position_info, container, false);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        // Cannot get Fragment inside Fragment using FragmentManager. Use getChildFragmentManager instead of getFragmentManager as usually.
        // See: https://developer.android.com/about/versions/android-4.2.html#NestedFragments
        // See: http://stackoverflow.com/questions/14083950/duplicate-id-tag-null-or-parent-id-with-another-fragment-for-com-google-androi
        FragmentManager mng = this.getChildFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) mng.findFragmentById(R.id.map);

        // Check if fragment is currently shown, otherwise initiate it.
        if (mapFragment == null) {
            // Make new fragment to show this selection.
            mapFragment = SupportMapFragment.newInstance();

            // Execute a transaction, replacing any existing fragment
            // with this one inside the frame.
            FragmentTransaction ft = mng.beginTransaction();
            ft.replace(R.id.map, mapFragment);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.commit();
        }

        mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");

        mMap = googleMap;

        LatLngBounds.Builder posBoundsBuilder = LatLngBounds.builder();
        int i = 0;
        for (Position position: positionList) {
//            if (position != MonitoringInfo.POSITION_NULL_VALUE) {
                double latitude = position.getLatitude();
                double longitude = position.getLongitude();
                double hdop = position.getHdop();

                LatLng pos = new LatLng(latitude, longitude);
                posBoundsBuilder.include(pos);
                mMap.addMarker(new MarkerOptions().position(pos).title(String.format("%d", i)).snippet(
                        String.format("%.4f %.4f %.2f", latitude, longitude, hdop)));
 //           }

            i += 1;
        }

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(posBoundsBuilder.build(), 300);
        mMap.moveCamera(cameraUpdate);

    }

    @Override
    public void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

//        if (positionCursor.getCount() == 0) {
//            // No positions entries in Position table. ToDo
//            return;
//        }
//
//        View view = getView();
//        ListView positionListView = (ListView)view.findViewById(R.id.position_listView);
//        String[] fromProjection = {
//                GuardTrackerContract.PositionTable.COLUMN_NAME_LT,
//                GuardTrackerContract.PositionTable.COLUMN_NAME_LG,
//                GuardTrackerContract.PositionTable.COLUMN_NAME_TIME1,
//                GuardTrackerContract.PositionTable.COLUMN_NAME_ALTITUDE,
//                GuardTrackerContract.PositionTable.COLUMN_NAME_NSAT,
//                GuardTrackerContract.PositionTable.COLUMN_NAME_HDOP,
//                GuardTrackerContract.PositionTable.COLUMN_NAME_FIXED
//        };
//        int [] toViews = {
//                R.id.lt_position_list_item_textView,
//                R.id.lg_position_list_item_textView,
//                R.id.alt_position_list_item_textView,
//                R.id.time_position_list_item_textView,
//                R.id.nsat_position_list_item_textView,
//                R.id.hdop_position_list_item_textView,
//                R.id.fixed_position_list_item_textView
//        };
//
//        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(view.getContext(), R.layout.list_item_position_info, positionCursor, fromProjection, toViews, 0);
//        positionListView.setAdapter(cursorAdapter);
//
////        positionCursor.moveToFirst();
////        positions = new ArrayList<>();
////
////        // Extract values from query result
////        nTuples = positionCursor.getCount();
////        for (int i = 0; i < nTuples; i++) {
////            int lt = positionCursor.getInt(positionCursor.getColumnIndex(GuardTrackerContract.PositionTable.COLUMN_NAME_LT));
////            int lg = positionCursor.getInt(positionCursor.getColumnIndex(GuardTrackerContract.PositionTable.COLUMN_NAME_LG));
////            int alt = positionCursor.getInt(positionCursor.getColumnIndex(GuardTrackerContract.PositionTable.COLUMN_NAME_ALTITUDE));
////            int time = positionCursor.getInt(positionCursor.getColumnIndex(GuardTrackerContract.PositionTable.COLUMN_NAME_TIME1));
////            int nSat = positionCursor.getInt(positionCursor.getColumnIndex(GuardTrackerContract.PositionTable.COLUMN_NAME_NSAT));
////            int hdop = positionCursor.getInt(positionCursor.getColumnIndex(GuardTrackerContract.PositionTable.COLUMN_NAME_HDOP));
////            int fixed = positionCursor.getInt(positionCursor.getColumnIndex(GuardTrackerContract.PositionTable.COLUMN_NAME_FIXED));
////            Position position = new Position();
////            positions.add(position);
////        }


    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
