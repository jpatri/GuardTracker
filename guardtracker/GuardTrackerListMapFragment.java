package com.patri.guardtracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.patri.guardtracker.model.GuardTracker;
import com.patri.guardtracker.model.MonitoringInfo;
import com.patri.guardtracker.model.Position;

import java.util.ArrayList;

public class GuardTrackerListMapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowLongClickListener {
    private static String TAG = GuardTrackerListMapFragment.class.getSimpleName();

    private GoogleMap                   mMap;
    private ArrayList<GuardTracker>     mGuardTrackerList;

    public GuardTrackerListMapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     */
    // TODO: Rename and change types and number of parameters
    public static GuardTrackerListMapFragment newInstance() {
        GuardTrackerListMapFragment fragment = new GuardTrackerListMapFragment();
//        Bundle args = new Bundle();
//        args.putInt(ARG_PARAM1, guardTrackerId);
//        fragment.setArguments(args);
        return fragment;
    }

    /** Demonstrates customizing the info window and/or its contents. */
    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        // These a both viewgroups containing an ImageView with id "badge" and two TextViews with id
        // "title" and "snippet".
        // private final View mWindow;

        private final View mContents;

        CustomInfoWindowAdapter() {
            //mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
            mContents = getLayoutInflater(null).inflate(R.layout.custom_guard_tracker_resumed_contents, null);
        }

        @Override
        public View getInfoWindow(Marker marker) { return null; }

        @Override
        public View getInfoContents(Marker marker) {
            render(marker, mContents);
            return mContents;
        }

        private void render(Marker marker, View view) {

            String title = marker.getTitle();
            int id = Integer.parseInt(title);
            GuardTracker guardTracker = mGuardTrackerList.get(id);
            Position posRef = guardTracker.getPosRef();
            MonitoringInfo lastMonInfo = guardTracker.getLastMonInfo();

            TextView titleUi = ((TextView) view.findViewById(R.id.title));
            if (title != null) {
                // Spannable string allows us to edit the formatting of the text.
                String name = guardTracker.getName();
                String stateEn = getString(guardTracker.isEnabled() ? R.string.activated : R.string.deactivated);
                title = name + ' ' + stateEn;
                SpannableString titleText = new SpannableString(title);
                titleText.setSpan(new ForegroundColorSpan(guardTracker.isEnabled() ? Color.GREEN : Color.RED), name.length() + 1, titleText.length(), 0);
                titleUi.setText(titleText);
            } else {
                titleUi.setText("");
            }

            TextView snippetGsmUi = ((TextView) view.findViewById(R.id.snippetGsmId));
            snippetGsmUi.setText(guardTracker.getGsmId());
            TextView snippetBleUi = ((TextView) view.findViewById(R.id.snippetBleId));
            snippetBleUi.setText(guardTracker.getBleId());

//            TextView snippetPosRefLatitudeUi = ((TextView) view.findViewById(R.id.snippetPosRefLat));
//            snippetPosRefLatitudeUi.setText(String.format("lt: %.4f", posRef.getLatitude()));
//            TextView snippetPosRefLongitudeUi = ((TextView) view.findViewById(R.id.snippetPosRefLng));
//            snippetPosRefLongitudeUi.setText(String.format("lg: %.4f", posRef.getLongitude()));

            TextView snippetLastDateUi = ((TextView) view.findViewById(R.id.snippetLastMonDate));
            snippetLastDateUi.setText(lastMonInfo.getPrettyDate());
            TextView snippetLastPosLatitudeUi = ((TextView) view.findViewById(R.id.snippetLastMonPosLat));
            int lastPosId = lastMonInfo.getPositionId();
            Position lastPos = Position.read(getContext(), lastPosId);
            snippetLastPosLatitudeUi.setText("lt: " + lastPos.getPrettyLatitude());
            TextView snippetLastPosLongitudeUi = ((TextView) view.findViewById(R.id.snippetLastMonPosLng));
            snippetLastPosLongitudeUi.setText("lg: " + lastPos.getPrettyLongitude());
            TextView snippetLastTemperatureUi = ((TextView) view.findViewById(R.id.snippetLastMonTemperature));
            snippetLastTemperatureUi.setText(lastMonInfo.getPrettyTemperature());
            TextView snippetLastBalanceUi = ((TextView) view.findViewById(R.id.snippetLastMonBalance));
            snippetLastBalanceUi.setText(lastMonInfo.getPrettyBalance());
            TextView snippetLastBatteryUi = ((TextView) view.findViewById(R.id.snippetLastMonBattery));
            snippetLastBatteryUi.setText(lastMonInfo.getPrettyCharge());
        }
    }

//    private class LoadGuardTracker extends AsyncTask<Void, Void, Void> {
//        @Override
//        protected Void doInBackground(Void... params) {
//            mGuardTrackerList = GuardTracker.loadResumed(GuardTrackerListMapFragment.this.getContext());
//            return null;
//        }
//    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        new LoadGuardTracker().execute();

        mGuardTrackerList = GuardTracker.readMapped(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_guard_tracker_list_map, container, false);

        FragmentManager mng = getFragmentManager();
        SupportMapFragment map = (SupportMapFragment) mng.findFragmentById(R.id.map_guard_tracker_list);

        Log.i(TAG, "onCreateView");
        // Check if fragment is currently shown, otherwise initiate it.
        if (map == null) {
            // Make new fragment to show this selection.
            map = SupportMapFragment.newInstance();

            // Execute a transaction, replacing any existing fragment
            // with this one inside the frame.
            Log.i(TAG, "Creates new SupportMapFragment");
            mng.beginTransaction().replace(R.id.map_guard_tracker_list, map).
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }

        map.getMapAsync(this);
        return view;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, "onMapReady");

        mMap = googleMap;

        // Setting an info window adapter allows us to change the both the contents and look of the
        // info window.
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        // Set listeners for marker events.  See the bottom of this class for their behavior.
        mMap.setOnInfoWindowLongClickListener(this);

        // Pan to see all markers in view.
        // Cannot zoom to bounds until the map has a size.
        final View mapView = getFragmentManager().findFragmentById(R.id.map_guard_tracker_list).getView();
        if (mapView.getViewTreeObserver().isAlive()) {
            mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @SuppressWarnings("deprecation") // We use the new method when supported
                @SuppressLint("NewApi") // We check which build version we are using.
                @Override
                public void onGlobalLayout() {
                    LatLngBounds.Builder posBoundsBuilder = LatLngBounds.builder();
                    int i = 0;

                    for (GuardTracker guardTracker: mGuardTrackerList) {

                        Position position = Position.read(getContext(), guardTracker.getPosRefId());
                        MonitoringInfo lastMonInfo = MonitoringInfo.read(getContext(), guardTracker.getLastMonInfoId());

                        guardTracker.setPosRef(position);
                        guardTracker.setLastMonInfo(lastMonInfo);

                        double latitude = position.getLatitude();
                        double longitude = position.getLongitude();

                        LatLng pos = new LatLng(latitude, longitude);
                        posBoundsBuilder.include(pos);
                        mMap.addMarker(new MarkerOptions().position(pos).title(""+i).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_hive)));

                        i += 1;
                    }

                    LatLngBounds bounds = posBoundsBuilder.build();

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
                }
            });
        }
    }

    @Override
    public void onInfoWindowLongClick(Marker marker) {
        String title = marker.getTitle();
        int id = Integer.parseInt(title);
        GuardTracker guardTracker = mGuardTrackerList.get(id);
        Intent intent = new Intent(getContext(), GuardTrackerActivity.class);
        int guardTrackerId = guardTracker.get_id();
        intent.putExtra(MainActivity.GUARD_TRACKER_ID, guardTrackerId);
        startActivity(intent);
    }
}
