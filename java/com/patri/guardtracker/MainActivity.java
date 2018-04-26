package com.patri.guardtracker;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.patri.guardtracker.bluetooth.GuardTrackerBleScanActivity;
import com.patri.guardtracker.bluetoothTest.DeviceScanActivity;
import com.patri.guardtracker.model.GuardTracker;
import com.patri.guardtracker.model.GuardTrackerDbHelper;
import com.patri.guardtracker.model.PermissionsChecker;
import com.patri.guardtracker.permissions.PermissionsActivity;
import com.patri.guardtracker.sms.SmsNotificationUtils;
import com.patri.guardtracker.sms.SmsReceivedEarlierActivity;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DialogListener {
    public final static String TAG = MainActivity.class.getSimpleName();
    public final static String GUARD_TRACKER_ID = "com.patri.guardtracker.GUARD_TRACKER_SELECTION";

    //private BluetoothAdapter mBluetoothAdapter;
    private boolean mTrackerListIsEmpty;
    private boolean mViewAsList;
    private List<GuardTracker> mGuardTrackerList;

    /* Attribute for permissions checker */
    //static final String[] PERMISSIONS = new String[]{Manifest.permission.SEND_SMS};
    //private PermissionsChecker mChecker;
    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 0;
    private static final int REQUEST_CODE_SEND_SMS = 1;

    private void startPermissionsActivity(int requestCode, String[] permissions) {
        PermissionsActivity.startActivityForResult(this, requestCode, permissions);
    }

    private void startPhonePickerForPairingDialog() {
        PhonePickerDialogFragment pairingDialogFragment = new PhonePickerDialogFragment();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(PhonePickerDialogFragment.ICON_ID_KEY, R.drawable.ic_pair_new_link);
        bundle.putInt(PhonePickerDialogFragment.TITLE_ID_KEY, R.string.dialog_pair_by_sms_title);
        bundle.putInt(PhonePickerDialogFragment.MESSAGE_ID_KEY, R.string.dialog_pair_by_sms_message_body);
        bundle.putInt(PhonePickerDialogFragment.YES_BUTTON_LABEL_ID_KEY, R.string.done_button_label);
        bundle.putInt(PhonePickerDialogFragment.NO_BUTTON_LABEL_ID_KEY, R.string.cancel_button_label);
        pairingDialogFragment.setArguments(bundle);
        pairingDialogFragment.show(manager, "PhonePickerDialogFragment");
    }
    private void startBleScanActivity() {
        Intent intent = new Intent(this.getBaseContext(), GuardTrackerBleScanActivity.class);
        startActivity(intent);
    }

    private void commonCreateAndStart() {
        // Add map fragment with GuardTracker devices
        mGuardTrackerList = GuardTracker.readMapped(getBaseContext());
        // Falta TESTAR esta condição.
        mTrackerListIsEmpty = mGuardTrackerList.isEmpty();
        mViewAsList = mTrackerListIsEmpty == true;
//        Fragment fragment = mViewAsList ?
//                GuardTrackerListViewFragment.newInstance() :
//                GuardTrackerListMapFragment.newInstance();
        Fragment fragment = GuardTrackerListViewFragment.newInstance();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_fragment_container, fragment).commit();
        //transaction.addToBackStack(null); Must be comment to work
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);

        // Only to be executed while in debug.
        //GuardTrackerDbHelper.onDeleteDb();

        commonCreateAndStart();
//        // Add map fragment with GuardTracker devices
//        mGuardTrackerList = GuardTracker.readMapped(getBaseContext());
//        // Falta TESTAR esta condição.
//        mTrackerListIsEmpty = mGuardTrackerList.isEmpty();
//        mViewAsList = mTrackerListIsEmpty == true;
////        Fragment fragment = mViewAsList ?
////                GuardTrackerListViewFragment.newInstance() :
////                GuardTrackerListMapFragment.newInstance();
//        Fragment fragment = GuardTrackerListViewFragment.newInstance();
//
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.add(R.id.main_fragment_container, fragment).commit();
//        //transaction.addToBackStack(null); Must be comment to work

//        final ActionBar actionBar = getSupportActionBar();
//        if (actionBar == null)
//            return;
//
//        // Specify that tabs should be displayed in the action bar.
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_menu, menu);
        if (mViewAsList == true) {
            menu.findItem(R.id.action_view_as_map).setVisible(true);

            menu.findItem(R.id.action_view_as_list).setVisible(false);
            menu.findItem(R.id.action_view_as_map).setEnabled(mTrackerListIsEmpty == false);
        } else {
            menu.findItem(R.id.action_view_as_map).setVisible(false);
            menu.findItem(R.id.action_view_as_list).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_view_as_list) {
            // Remove map fragment and replace with list view fragment
            Fragment fragment = GuardTrackerListViewFragment.newInstance();
            getSupportFragmentManager().beginTransaction().
                    replace(R.id.main_fragment_container, fragment).commit();

            mViewAsList = true;
            invalidateOptionsMenu();
            return true;
        }
        if (id == R.id.action_view_as_map) {
            // Remove map fragment and replace with list view fragment
            Fragment fragment = GuardTrackerListMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().
                    replace(R.id.main_fragment_container, fragment).commit();

            mViewAsList = false;
            invalidateOptionsMenu();
            return true;
        }
        if (id == R.id.action_scan_ble) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                final String[] PERMISSIONS = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};
                PermissionsChecker checker = new PermissionsChecker(getBaseContext());

                if (checker.lacksPermissions(PERMISSIONS)) {
                    startPermissionsActivity(REQUEST_CODE_ACCESS_COARSE_LOCATION, PERMISSIONS);
                    return true;
                }
            }
            startBleScanActivity();
            return true;

        }
        if (id == R.id.action_pair_new_device) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                final String[] PERMISSIONS = new String[]{Manifest.permission.SEND_SMS};
                PermissionsChecker checker = new PermissionsChecker(getBaseContext());

                if (checker.lacksPermissions(PERMISSIONS)) {
                    startPermissionsActivity(REQUEST_CODE_SEND_SMS, PERMISSIONS);
                    return true;
                }
            }
            startPhonePickerForPairingDialog();
            return true;
        }
        if (id == R.id.action_eliminate_devices) {
            Intent intent = new Intent(this, com.patri.guardtracker.DevicesEliminateActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_back_office) {
            Intent intent = new Intent(this, com.patri.guardtracker.backOffice.AndroidDatabaseManager.class);
            startActivity(intent);
            return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_ble_test) {
            Intent intent = new Intent(this, DeviceScanActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_sms_received) {
            Intent intent = new Intent(this, SmsReceivedEarlierActivity.class);
            startActivity(intent);
            return true;
//            GuardTracker guardTracker = mGuardTrackerList.get(0);
//            if (guardTracker != null) {
//                int guardTrackerId = guardTracker.get_id();
//                String guardTrackerName = guardTracker.getName();
//                intent.putExtra(GuardTrackerActivity.GUARD_TRACKER_ID, guardTrackerId);
//                intent.putExtra(GuardTrackerActivity.GUARD_TRACKER_NAME, guardTrackerName);
//                startActivity(intent);
//            }
//            return true;
        }
        if (id == R.id.test_sms_notification) {
            SmsNotificationUtils.notifyReceivedSms(getBaseContext()
                    , Calendar.getInstance().getTimeInMillis()
                    , "962974845"
                    , "SMS message body text. Just a simple experience"
                    , 0
                    , 0
                    , "Tracker 1");

            return true;
//            GuardTracker guardTracker = mGuardTrackerList.get(0);
//            if (guardTracker != null) {
//                int guardTrackerId = guardTracker.get_id();
//                String guardTrackerName = guardTracker.getName();
//                intent.putExtra(GuardTrackerActivity.GUARD_TRACKER_ID, guardTrackerId);
//                intent.putExtra(GuardTrackerActivity.GUARD_TRACKER_NAME, guardTrackerName);
//                startActivity(intent);
//            }
//            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop");
        super.onStop();
        //GuardTracker.unload();

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        Log.i(TAG, "onDialogNegativeClick --> Nothing done (only dismiss dialog). Must be done anything");
        dialog.dismiss();

    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        try {
            PhonePickerDialogFragment phonePicker = (PhonePickerDialogFragment)dialog;
            SmsManager smsManager = SmsManager.getDefault();
            final String pairSmsMessage = "123ajt";
            int id = smsManager.getSubscriptionId();
            smsManager.sendTextMessage(phonePicker.getPhoneNumber(), null, pairSmsMessage, null, null);
            Toast.makeText(getBaseContext(), R.string.toast_pair_by_sms_sent, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), R.string.toast_pair_by_sms_not_sent, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            dialog.dismiss();

        }
    }

    /**
     * Remove next method when it is tested in BleControlActivity. Needs to be revisited.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SEND_SMS) {
            if (resultCode == PermissionsActivity.PERMISSIONS_DENIED)
                Toast.makeText(getBaseContext(), "The link with device can not be done without permission to send sms", Toast.LENGTH_LONG);
            else {
                startPhonePickerForPairingDialog();
            }
            return;
        }
        if (requestCode == REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            if (resultCode == PermissionsActivity.PERMISSIONS_DENIED)
                Toast.makeText(getBaseContext(), "The ACCESS_COARSE_LOCATOIN must be granted so scan with BLE", Toast.LENGTH_LONG);
            else {
                startBleScanActivity();
            }
            return;

        }

    }

    @Override
    protected void onRestart() {
        Log.i(TAG, "onRestart()");
        super.onRestart();

        Fragment fragment = GuardTrackerListViewFragment.newInstance();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, fragment).commit();

    }
}
