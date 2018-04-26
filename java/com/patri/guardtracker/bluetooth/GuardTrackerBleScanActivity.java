package com.patri.guardtracker.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.patri.guardtracker.DialogListener;
import com.patri.guardtracker.PhonePickerDialogFragment;
import com.patri.guardtracker.R;

import java.util.ArrayList;

/**
 * Created by patri on 16/10/2016.
 */
public class GuardTrackerBleScanActivity extends AppCompatActivity implements DialogListener {
    private final static String TAG = GuardTrackerBleScanActivity.class.getSimpleName();
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private String mSelfPhoneNumber;
    private boolean mNeverAskAgain;
    private Handler mHandler;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private static final int REQUEST_ENABLE_BT = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_device_scan);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle(R.string.title_devices);

        ListView listView = (ListView)findViewById(android.R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
                if (device == null) return;
                final Intent intent = new Intent(GuardTrackerBleScanActivity.this, GuardTrackerBleControlActivityFromDev.class);
                intent.putExtra(GuardTrackerBleControlActivityFromDev.EXTRAS_DEVICE_NAME, device.getName());
                intent.putExtra(GuardTrackerBleControlActivityFromDev.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                intent.putExtra(GuardTrackerBleControlActivityFromDev.EXTRAS_DEVICE_OWNER_PHONE, mSelfPhoneNumber);
                if (mScanning) {
                    stopBleScan();
                }
                startActivity(intent);

            }
        });

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1)
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        else
            mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mHandler = new Handler();

        // Get self phone number
        //PhonePickerDialogFragment phoneDialog = new PhonePickerNeverAskAgainDialogFragment();
        PhonePickerDialogFragment phoneDialog = new PhonePickerDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PhonePickerDialogFragment.ICON_ID_KEY, R.drawable.ic_pair_new_link);
        bundle.putInt(PhonePickerDialogFragment.TITLE_ID_KEY, R.string.dialog_change_owner_title);
        bundle.putInt(PhonePickerDialogFragment.MESSAGE_ID_KEY, R.string.dialog_change_owner_phone_message_body);
        bundle.putInt(PhonePickerDialogFragment.YES_BUTTON_LABEL_ID_KEY, R.string.done_button_label);
        bundle.putInt(PhonePickerDialogFragment.NO_BUTTON_LABEL_ID_KEY, R.string.cancel_button_label);
        phoneDialog.setArguments(bundle);
        phoneDialog.show(getSupportFragmentManager(), "PhonePickerDialogFragment");

    }

    private void startBleScan() {
        // Change default scan parameters
        ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder();
        // LOW_LATENCY enables the capture of device advertisements with a cycle of 20 seconds.
        scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        ScanSettings scanSettings = scanSettingsBuilder.build();
        // Stops scanning after a pre-defined scan period.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopBleScan();
                invalidateOptionsMenu();
            }
        }, SCAN_PERIOD);
//        mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
        mBluetoothAdapter.getBluetoothLeScanner().startScan(null, scanSettings, mScanCallback);
        mScanning = true;
    }
    private void stopBleScan() {
        mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
        mScanning = false;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                mLeDeviceListAdapter.clear();
                startBleScan();
                invalidateOptionsMenu();
                return true;
            case R.id.menu_stop:
                stopBleScan();
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();

        ListView listView = (ListView)findViewById(android.R.id.list);
        listView.setAdapter(mLeDeviceListAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopBleScan();
        mLeDeviceListAdapter.clear();
    }


    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = GuardTrackerBleScanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
//                view = mInflator.inflate(R.layout.list_item_device_id, null);
//                viewHolder = new ViewHolder();
//                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
//                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
//                view.setTag(viewHolder);
                // ELIMINAR O CÓDIGO ANTERIOR DEPOIS DE TESTAR O ECRÃ com o TwoLine
                view = mInflator.inflate(R.layout.list_item_two_line, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(android.R.id.text1);
                viewHolder.deviceName = (TextView) view.findViewById(android.R.id.text2);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0) {
                viewHolder.deviceName.setText(deviceName);
            } else {
                viewHolder.deviceName.setText(R.string.unknown_device);
            }
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }
    }


    // Device scan callback.
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            super.onScanResult(callbackType, result);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    View emtpy = GuardTrackerBleScanActivity.this.findViewById(android.R.id.empty);
                    emtpy.setVisibility(View.GONE);
                    BluetoothDevice device = result.getDevice();
                    mLeDeviceListAdapter.addDevice(device);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        Log.i(TAG, "onDialogNegativeClick --> Nothing done. Must be done anything");
        dialog.dismiss();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        PhonePickerDialogFragment phonePickerDialog = (PhonePickerDialogFragment)dialog;
        mSelfPhoneNumber = phonePickerDialog.getPhoneNumber();
        startBleScan();
        dialog.dismiss();
        invalidateOptionsMenu();
    }
}
