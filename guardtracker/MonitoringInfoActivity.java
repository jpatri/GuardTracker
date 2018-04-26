package com.patri.guardtracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.patri.guardtracker.model.GuardTracker;
import com.patri.guardtracker.model.MonitoringConfiguration;

public class MonitoringInfoActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private int mGuardTrackerId;
    private int mMonInfoSelected;
    private MonitoringConfiguration mMonCfg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring_info);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get GuardTracker _ID and monitoring menu item selected
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        Intent intent = getIntent();
        mGuardTrackerId = intent.getIntExtra(GuardTrackerActivity.GUARD_TRACKER_ID, 0);
        mMonInfoSelected = intent.getIntExtra(GuardTrackerActivity.MON_INFO_ITEM_SELECTED, -1);
        if (mGuardTrackerId == 0 || mMonInfoSelected == -1) {
            mGuardTrackerId = preferences.getInt(getString(R.string.saved_guard_tracker_id), 0);
            mMonInfoSelected = preferences.getInt(getString(R.string.saved_guard_tracker_mon_info_spinner_position), -1);
        }
        if (mGuardTrackerId == 0 || mMonInfoSelected == -1) {
            // Return to last activity
            // ToDo
        }

        GuardTracker guardTracker = GuardTracker.read(getBaseContext(), mGuardTrackerId);
        mMonCfg = MonitoringConfiguration.read(getBaseContext(), guardTracker.getMonCfgId());

        // Create the adapter that will return a fragment for each of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(mMonInfoSelected); // The tab id in TabLayout start with value nº 1.

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        String name = guardTracker.getName();
        setTitle(getString(R.string.title_activity_monitoring_info) + ": " + name);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_monitoring_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    /**
//     * A placeholder fragment containing a simple view.
//     */
//    public static class PlaceholderFragment extends Fragment {
//        /**
//         * The fragment argument representing the section number for this
//         * fragment.
//         */
//        private static final String ARG_SECTION_NUMBER = "section_number";
//
//        public PlaceholderFragment() {
//        }
//
//        /**
//         * Returns a new instance of this fragment for the given section
//         * number.
//         */
//        public static PlaceholderFragment newInstance(int sectionNumber) {
//            PlaceholderFragment fragment = new PlaceholderFragment();
//            Bundle args = new Bundle();
//            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//            fragment.setArguments(args);
//            return fragment;
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_monitoring_info, container, false);
//            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
//            return rootView;
//        }
//    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private ArrayAdapter<CharSequence> arrayAdapter;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            arrayAdapter = ArrayAdapter.createFromResource(MonitoringInfoActivity.this, R.array.mon_info_array, android.R.layout.simple_spinner_item);

        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            System.out.println("" + this.getClass().getSimpleName() + ".getItem: position = " + position);

            Fragment fragment;
            switch(position) {
                case 0: fragment = MonitoringInfoTemperatureFragment.newInstance(mGuardTrackerId, mMonCfg.getTempLow(), mMonCfg.getTempHigh()); break;
                case 1: fragment = MonitoringInfoSimBalanceFragment.newInstance(mGuardTrackerId, mMonCfg.getSimBalanceThreshold()); break;
                case 2: fragment = MonitoringInfoBatteryChargeFragment.newInstance(mGuardTrackerId); break;
                case 3: fragment = MonitoringInfoPositionFragment.newInstance(mGuardTrackerId); break;
                default: fragment = null;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return arrayAdapter.getCount();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position < arrayAdapter.getCount())
                return arrayAdapter.getItem(position);
            return null;
        }
    }
}
