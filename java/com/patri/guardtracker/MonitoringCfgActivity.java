package com.patri.guardtracker;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.patri.guardtracker.bluetooth.BleConnectionStateChangeListener;
import com.patri.guardtracker.bluetooth.BleMessageListener;
import com.patri.guardtracker.bluetooth.BleStateChangeAdapter;
import com.patri.guardtracker.bluetooth.GuardTrackerBleConnControl;
import com.patri.guardtracker.communication.GuardTrackerCommands;
import com.patri.guardtracker.model.GuardTracker;
import com.patri.guardtracker.model.MonitoringConfiguration;

import java.util.Arrays;

public class MonitoringCfgActivity extends AppCompatActivity implements DialogListener {
    private static final String TAG = MonitoringCfgActivity.class.getSimpleName();
    public final static String MON_CFG_CURRENT_TIME_TO_WAKE_IN_MINUTES_ARG = "com.patri.guardtracker.MonitoringCfgActivity.MON_CFG_CURRENT_TIME_TO_WAKE_IN_MINUTES";
    public final static String MON_CFG_CURRENT_WAKEUP_PERIOD_IN_MINUTES_ARG = "com.patri.guardtracker.MonitoringCfgActivity.MON_CFG_CURRENT_WAKEUP_PERIOD_IN_MINUTES_ARG";
    public final static String MON_CFG_CURRENT_SMS_CRITERIA_ARG = "com.patri.guardtracker.MonitoringCfgActivity.MON_CFG_CURRENT_SMS_CRITERIA_ARG";
    public final static String MON_CFG_CURRENT_SMS_CRITERIA_CYCLIC_ARG = "com.patri.guardtracker.MonitoringCfgActivity.MON_CFG_CURRENT_SMS_CRITERIA_CYCLIC_ARG";
    public final static String MON_CFG_CURRENT_GPS_THRESHOLD_IN_METERS_ARG = "com.patri.guardtracker.MonitoringCfgActivity.MON_CFG_CURRENT_GPS_THRESHOLD_IN_METERS_ARG";
    public final static String MON_CFG_CURRENT_GPS_FOV_ARG = "com.patri.guardtracker.MonitoringCfgActivity.MON_CFG_CURRENT_GPS_FOV_ARG";
    public final static String MON_CFG_CURRENT_GPS_TIMEOUT_IN_MINUTES_ARG = "com.patri.guardtracker.MonitoringCfgActivity.MON_CFG_CURRENT_GPS_TIMEOUT_IN_MINUTES_ARG";
    public final static String MON_CFG_CURRENT_TEMP_BOUNDS_LOW_ARG = "com.patri.guardtracker.MonitoringCfgActivity.MON_CFG_CURRENT_TEMP_BOUNDS_LOW_ARG";
    public final static String MON_CFG_CURRENT_TEMP_BOUNDS_HIGH_ARG = "com.patri.guardtracker.MonitoringCfgActivity.MON_CFG_CURRENT_TEMP_BOUNDS_HIGH_ARG";
    public final static String MON_CFG_CURRENT_SIM_BALANCE_ARG = "com.patri.guardtracker.MonitoringCfgActivity.MON_CFG_CURRENT_SIM_BLANCE_ARG";

    /**
     * This enumerate is used to establish the Tag to be passed to dialog. The same dialog type is several situations.
     */
    private enum MonitoringCfgDialogs {
        WakeupTimeModDialog, WakeupPeriodDialog, SmsCriteriaDialog, GpsThresholdDialog, GpsFovDialog, GpsTimeoutDialog, TempBoundsDialog, SimBalanceDialog;
        private String[] valuesStr = { "0", "1", "2", "3", "4", "5", "6", "7" };
        public String toString() {
            String dialogStr = valuesStr[this.ordinal()];
            return dialogStr;
        }
    }

    private int mGuardTrackerId;
    private String mGuardTrackerName;
    private int mMonCfgId;
    private MonitoringConfiguration mMonCfg;
    private GuardTracker mGuardTracker;

    private String mOwnerPhoneNumber;
    private String mBleAddress;
    private boolean mBleConnected;
    private GuardTrackerBleConnControl mBleConnControl;
    private ArrayAdapter mArrayAdapter;
    private String [][] mElems;

    // Bluetooth attributes
    private BluetoothAdapter mBluetoothAdapter;
    private GuardTrackerActivity.BleStateEnum mBleState;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_monitoring_cfg);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create a progress bar to display while the list loads
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progressBar.setIndeterminate(true);

        final ListView listView = (ListView)findViewById(R.id.mon_cfg_list_view);
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
        mMonCfgId = intent.getIntExtra(GuardTrackerActivity.CONFIG_ID, 0);
        if (mGuardTrackerId == 0 || mMonCfgId == 0) {
            mGuardTrackerId = preferences.getInt(getString(R.string.saved_guard_tracker_id), 0);
            mMonCfgId = preferences.getInt(getString(R.string.saved_guard_tracker_mon_cfg_id), 0);
        }
        if (mGuardTrackerId == 0 || mMonCfgId == 0) {
            // Return to last activity
            // ToDo
        }

        mMonCfg = MonitoringConfiguration.read(getBaseContext(), mMonCfgId);

        mElems = new String[][] {
                { getString(R.string.mon_cfg_item_title_hod), mMonCfg.getPrettyStartTime() },
                { getString(R.string.mon_cfg_item_title_period), mMonCfg.getPrettyPeriod() },
                { getString(R.string.mon_cfg_item_title_sms_criteria), mMonCfg.getPrettySmsCriteria() },
                { getString(R.string.mon_cfg_item_title_gps_threshold), mMonCfg.getPrettyGpsThreshold() },
                { getString(R.string.mon_cfg_item_title_gps_fov), mMonCfg.getPrettyGpsFov() },
                { getString(R.string.mon_cfg_item_title_gps_timeout), mMonCfg.getPrettyGpsTimeout() },
                { getString(R.string.mon_cfg_item_title_temp), mMonCfg.getPrettyTempLow() + "    " + mMonCfg.getPrettyTempHigh() },
                { getString(R.string.mon_cfg_item_title_sim), mMonCfg.getPrettySimBalance() }
        };

        // A simple ArrayAdapter can only be represented by a TextView
        mArrayAdapter = new ArrayAdapter(getBaseContext(), android.R.layout.simple_list_item_2, android.R.id.text1, mElems) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setText(mElems[position][0]);
                text2.setText(mElems[position][1]);
                return view;
            }
        };

        listView.setAdapter(mArrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Log.i(TAG, "listView.onItemSelected: position = " + position + " id = " + id);
                if (mBleConnControl.isConnected() == false)
                    return;

                listView.setItemChecked(position, true);

                switch(position) {
                    case 0:
                        startWakeupTimeModCfgDialog();
                        break;
                    case 1:
                        startWakeupPeriodCfgDialog();
                        break;
                    case 2:
                        startSmsCriteriaCfgDialog();
                        break;
                    case 3:
                        startGpsThresholdDialog();
                        break;
                    case 4:
                        startGpsFovDialog();
                        break;
                    case 5:
                        startGpsTimeoutDialog();
                        break;
                    case 6:
                        startTempBoundsDialog();
                        break;
                    case 7:
                        startSimBalanceDialog();
                        break;
                    default:;
                }

            }
        });


        mGuardTracker = GuardTracker.read(getBaseContext(), mGuardTrackerId);
        mGuardTrackerName = mGuardTracker.getName();
        setTitle(getString(R.string.title_activity_monitoring_cfg) + ": " + mGuardTrackerName);

        mBleAddress = mGuardTracker.getBleId();
        mOwnerPhoneNumber = mGuardTracker.getOwnerPhoneNumber();
        mBleConnControl = new GuardTrackerBleConnControl(getBaseContext(), mBleAddress, mOwnerPhoneNumber, new BleStateChangeAdapter() {
            @Override
            public void onAuthenticated() {
                Log.i(TAG, "onAuthenticated");
                super.onAuthenticated();
            }

            @Override
            public void onDisconnected() {
                Log.i(TAG, "onDisconnected");
                super.onDisconnected();
            }
        }, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msgRecv) {
                Log.i(TAG, "OnMessageReceived: " + GuardTrackerBleConnControl.dumpMsg(msgRecv));
            }
        });

        mHandler = new Handler();
        //mBleConnected = mBleConnControl.isConnected();
    }

    private void updateMonCfgView() {
        mElems[0][1] = mMonCfg.getPrettyStartTime();
        mElems[1][1] = mMonCfg.getPrettyPeriod();
        mElems[2][1] = mMonCfg.getPrettySmsCriteria();
        mElems[3][1] = mMonCfg.getPrettyGpsThreshold();
        mElems[4][1] = mMonCfg.getPrettyGpsFov();
        mElems[5][1] = mMonCfg.getPrettyGpsTimeout();
        mElems[6][1] = mMonCfg.getPrettyTempLow() + "    " + mMonCfg.getPrettyTempHigh();
        mElems[7][1] = mMonCfg.getPrettySimBalance();

//        ListAdapter listAdapter = mArrayAdapter;
//        String [] listItem = (String[])listAdapter.getItem(position);
//        listItem[1] = mMonCfg.getPrettyStartTime();
        mArrayAdapter.notifyDataSetChanged();
    }

    private void onMonCfgRefresh() {
        short bitmask = 0;
        byte [] cmd = GuardTrackerCommands.readMonitoringConfig();
        mBleConnControl.sendCommand(cmd, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msg) {
                // Test if answer belongs with this command
                byte ordinal = (byte) GuardTrackerCommands.CommandValues.READ_MONITORING_CFG.ordinal();
                if (msg[0] != ordinal) {
                    Toast.makeText(getBaseContext(),
                            "BLE answer (" + msg[0] + ") don't belong to cmd READ_MONITORING_CFG (" + ordinal + ")",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // Test if answer result is KO
                if (msg[1] != GuardTrackerCommands.CmdResValues.CMD_RES_OK.value()) {
                    Toast.makeText(MonitoringCfgActivity.this.getBaseContext(),
                            R.string.dialog_resync_monitoring_cfg_unsuccessful_read,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                // Parse ble message into temporally monCfg object.
                MonitoringConfiguration updatedMonCfg = new MonitoringConfiguration();

                int cfgLen = 19;
                byte [] cfgData = new byte[cfgLen];
                System.arraycopy(msg, 2, cfgData, 0, cfgLen);
                // Decode received message and build object.
                updatedMonCfg.parse(cfgData);
                updatedMonCfg.set_id(mMonCfgId);
                // Swap temporally monCfg to this monCfg
                mMonCfg = updatedMonCfg;
                mGuardTracker.setMonCfg(mMonCfg);
                mMonCfg.update(getBaseContext());
                // Update view
                updateMonCfgView();
                Toast.makeText(getBaseContext(), R.string.dialog_resync_monitoring_cfg_success_database, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void onClickWakeupTimeModConfig(int timeMod) {
        byte[] cmd = GuardTrackerCommands.writeMonitoringConfig(MonitoringCfgDialogs.WakeupTimeModDialog.ordinal(), timeMod);
        mBleConnControl.sendCommand(cmd, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msg) {
                // Test if answer belongs with this command
                byte ordinal = (byte) GuardTrackerCommands.CommandValues.WRITE_MONITORING_CFG.ordinal();
                if (msg[0] != ordinal) {
                    Toast.makeText(getBaseContext(),
                            "BLE answer (" + msg[0] + ") don't belong to cmd WRITE_MONITORING_CFG (" + ordinal + ")",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // Test if answer result is KO
                if (msg[1] != GuardTrackerCommands.CmdResValues.CMD_RES_OK.value()) {
                    Toast.makeText(MonitoringCfgActivity.this.getBaseContext(),
                            R.string.dialog_mon_cfg_wakeup_period_unsuccessful_write,
                            Toast.LENGTH_LONG).show();
                    return;
//                    mMonCfg.setTimeMod(timeMod);
//                    mMonCfg.update(getBaseContext());
//                    ListAdapter listAdapter = mArrayAdapter;
//                    String[] listItem = (String[]) listAdapter.getItem(0);
//                    listItem[1] = mMonCfg.getPrettyStartTime();
//                    mArrayAdapter.notifyDataSetChanged();
                }
                Toast.makeText(getBaseContext(), R.string.dialog_resync_monitoring_cfg_success_device, Toast.LENGTH_LONG).show();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onMonCfgRefresh();
                    }
                }, 200);
            }
        });
    }

    private void onClickWakeupPeriodConfig(int newPeriodInMinutes) {
        byte [] cmd = GuardTrackerCommands.writeMonitoringConfig(MonitoringCfgDialogs.WakeupPeriodDialog.ordinal(), newPeriodInMinutes);
        mBleConnControl.sendCommand(cmd, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msg) {
                // Test if answer belongs with this command
                byte ordinal = (byte) GuardTrackerCommands.CommandValues.WRITE_MONITORING_CFG.ordinal();
                if (msg[0] != ordinal) {
                    Toast.makeText(getBaseContext(),
                            "BLE answer (" + msg[0] + ") don't belong to cmd WRITE_MONITORING_CFG (" + ordinal + ")",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // Test if answer result is KO
                if (msg[1] != GuardTrackerCommands.CmdResValues.CMD_RES_OK.value()) {
                    Toast.makeText(MonitoringCfgActivity.this.getBaseContext(),
                            R.string.dialog_mon_cfg_wakeup_period_unsuccessful_write,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(getBaseContext(), R.string.dialog_mon_cfg_wakeup_period_success_write, Toast.LENGTH_LONG).show();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onMonCfgRefresh();
                    }
                }, 200);
            }
        });
    }

    private void onClickSmsCriteriaConfig(MonitoringConfiguration.SmsCriteria newSmsCriteria) {
        byte [] cmd = GuardTrackerCommands.writeMonitoringConfig(MonitoringCfgDialogs.SmsCriteriaDialog.ordinal(), newSmsCriteria.ordinal());
        mBleConnControl.sendCommand(cmd, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msg) {
                // Test if answer belongs with this command
                byte ordinal = (byte) GuardTrackerCommands.CommandValues.WRITE_MONITORING_CFG.ordinal();
                if (msg[0] != ordinal) {
                    Toast.makeText(getBaseContext(),
                            "BLE answer (" + msg[0] + ") don't belong to cmd WRITE_MONITORING_CFG (" + ordinal + ")",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // Test if answer result is KO
                if (msg[1] != GuardTrackerCommands.CmdResValues.CMD_RES_OK.value()) {
                    Toast.makeText(MonitoringCfgActivity.this.getBaseContext(),
                            R.string.dialog_mon_cfg_sms_criteria_unsuccessful_write,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(getBaseContext(), R.string.dialog_mon_cfg_sms_criteria_success_write, Toast.LENGTH_LONG).show();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onMonCfgRefresh();
                    }
                }, 200);
            }
        });
    }
    private void onClickGpsThresholdConfig(int gpsThresholdMeters) {
        int latMinutes = MonitoringConfiguration.getRawGpsThresholdLat(gpsThresholdMeters);
        int lonMinutes = MonitoringConfiguration.getRawGpsThresholdLon(gpsThresholdMeters);
        byte [] cmd = GuardTrackerCommands.writeMonitoringConfig(MonitoringCfgDialogs.GpsThresholdDialog.ordinal(), latMinutes, lonMinutes);
        mBleConnControl.sendCommand(cmd, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msg) {
                // Test if answer belongs with this command
                byte ordinal = (byte) GuardTrackerCommands.CommandValues.WRITE_MONITORING_CFG.ordinal();
                if (msg[0] != ordinal) {
                    Toast.makeText(getBaseContext(),
                            "BLE answer (" + msg[0] + ") don't belong to cmd WRITE_MONITORING_CFG (" + ordinal + ")",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // Test if answer result is KO
                if (msg[1] != GuardTrackerCommands.CmdResValues.CMD_RES_OK.value()) {
                    Toast.makeText(MonitoringCfgActivity.this.getBaseContext(),
                            R.string.dialog_mon_cfg_gps_threshold_unsuccessful_write,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(getBaseContext(), R.string.dialog_mon_cfg_gps_threshold_success_write, Toast.LENGTH_LONG).show();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onMonCfgRefresh();
                    }
                }, 200);
            }
        });
    }
    private void onClickGpsFovConfig(int gpsFov) {
        byte [] cmd = GuardTrackerCommands.writeMonitoringConfig(MonitoringCfgDialogs.GpsFovDialog.ordinal(), gpsFov);
        mBleConnControl.sendCommand(cmd, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msg) {
                // Test if answer belongs with this command
                byte ordinal = (byte) GuardTrackerCommands.CommandValues.WRITE_MONITORING_CFG.ordinal();
                if (msg[0] != ordinal) {
                    Toast.makeText(getBaseContext(),
                            "BLE answer (" + msg[0] + ") don't belong to cmd WRITE_MONITORING_CFG (" + ordinal + ")",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // Test if answer result is KO
                if (msg[1] != GuardTrackerCommands.CmdResValues.CMD_RES_OK.value()) {
                    Toast.makeText(MonitoringCfgActivity.this.getBaseContext(),
                            R.string.dialog_mon_cfg_gps_fov_unsuccessful_write,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(getBaseContext(), R.string.dialog_mon_cfg_gps_fov_success_write, Toast.LENGTH_LONG).show();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onMonCfgRefresh();
                    }
                }, 200);
            }
        });
    }
    private void onClickGpsTimeoutConfig(int gpsTimeoutMinutes) {
        byte [] cmd = GuardTrackerCommands.writeMonitoringConfig(MonitoringCfgDialogs.GpsTimeoutDialog.ordinal(), gpsTimeoutMinutes);
        mBleConnControl.sendCommand(cmd, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msg) {
                // Test if answer belongs with this command
                byte ordinal = (byte) GuardTrackerCommands.CommandValues.WRITE_MONITORING_CFG.ordinal();
                if (msg[0] != ordinal) {
                    Toast.makeText(getBaseContext(),
                            "BLE answer (" + msg[0] + ") don't belong to cmd WRITE_MONITORING_CFG (" + ordinal + ")",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // Test if answer result is KO
                if (msg[1] != GuardTrackerCommands.CmdResValues.CMD_RES_OK.value()) {
                    Toast.makeText(MonitoringCfgActivity.this.getBaseContext(),
                            R.string.dialog_mon_cfg_gps_timeout_unsuccessful_write,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(getBaseContext(), R.string.dialog_mon_cfg_gps_timeout_success_write, Toast.LENGTH_LONG).show();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onMonCfgRefresh();
                    }
                }, 200);
            }
        });
    }
    private void onClickTempBoundsConfig(double tempLow, double tempHigh) {
        int tempHighRaw = MonitoringConfiguration.getRawTemp(tempHigh);
        int tempLowRaw  = MonitoringConfiguration.getRawTemp(tempLow);
        byte [] cmd = GuardTrackerCommands.writeMonitoringConfig(MonitoringCfgDialogs.TempBoundsDialog.ordinal(), tempHighRaw, tempLowRaw);
        mBleConnControl.sendCommand(cmd, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msg) {
                // Test if answer belongs with this command
                byte ordinal = (byte) GuardTrackerCommands.CommandValues.WRITE_MONITORING_CFG.ordinal();
                if (msg[0] != ordinal) {
                    Toast.makeText(getBaseContext(),
                            "BLE answer (" + msg[0] + ") don't belong to cmd WRITE_MONITORING_CFG (" + ordinal + ")",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // Test if answer result is KO
                if (msg[1] != GuardTrackerCommands.CmdResValues.CMD_RES_OK.value()) {
                    Toast.makeText(MonitoringCfgActivity.this.getBaseContext(),
                            R.string.dialog_mon_cfg_temp_bounds_unsuccessful_write,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(getBaseContext(), R.string.dialog_mon_cfg_temp_bounds_success_write, Toast.LENGTH_LONG).show();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onMonCfgRefresh();
                    }
                }, 200);
            }
        });
    }
    private void onClickSimBalanceConfig(double simBalanceThreshold) {
        int simBalance = MonitoringConfiguration.getRawSimBalance(simBalanceThreshold);
        byte [] cmd = GuardTrackerCommands.writeMonitoringConfig(MonitoringCfgDialogs.SimBalanceDialog.ordinal(), simBalance);
        mBleConnControl.sendCommand(cmd, new BleMessageListener() {
            @Override
            public void onMessageReceived(byte[] msg) {
                // Test if answer belongs with this command
                byte ordinal = (byte) GuardTrackerCommands.CommandValues.WRITE_MONITORING_CFG.ordinal();
                if (msg[0] != ordinal) {
                    Toast.makeText(getBaseContext(),
                            "BLE answer (" + msg[0] + ") don't belong to cmd WRITE_MONITORING_CFG (" + ordinal + ")",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // Test if answer result is KO
                if (msg[1] != GuardTrackerCommands.CmdResValues.CMD_RES_OK.value()) {
                    Toast.makeText(MonitoringCfgActivity.this.getBaseContext(),
                            R.string.dialog_mon_cfg_sim_balance_unsuccessful_write,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(getBaseContext(), R.string.dialog_mon_cfg_sim_balance_success_write, Toast.LENGTH_LONG).show();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onMonCfgRefresh();
                    }
                }, 200);
            }
        });
    }

    private void startWakeupTimeModCfgDialog() {
        DialogGenericIcTtMs2Bt newFragment = new MonitoringCfgTimeToWakeDialogFragment();
        int timeToWake = mMonCfg.getTimeMod();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogGenericIcTtMs2Bt.ICON_ID_KEY, android.R.drawable.ic_menu_edit);
        bundle.putInt(DialogGenericIcTtMs2Bt.TITLE_ID_KEY, R.string.dialog_mon_cfg_time_of_day_title);
        bundle.putInt(DialogGenericIcTtMs2Bt.MESSAGE_ID_KEY, R.string.dialog_mon_cfg_time_of_day_message_body);
        bundle.putInt(DialogGenericIcTtMs2Bt.YES_BUTTON_LABEL_ID_KEY, R.string.done_button_label);
        bundle.putInt(DialogGenericIcTtMs2Bt.NO_BUTTON_LABEL_ID_KEY, R.string.cancel_button_label);
        bundle.putInt(MonitoringCfgActivity.MON_CFG_CURRENT_TIME_TO_WAKE_IN_MINUTES_ARG, timeToWake);
        newFragment.setArguments(bundle);
        newFragment.show(getSupportFragmentManager(), MonitoringCfgDialogs.WakeupTimeModDialog.toString());
    }
    private void startWakeupPeriodCfgDialog() {
        DialogGenericIcTtMs2Bt dialogFragment = new MonitoringCfgWakeupPeriodDialogFragment();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogGenericIcTtMs2Bt.ICON_ID_KEY, android.R.drawable.ic_menu_edit);
        bundle.putInt(DialogGenericIcTtMs2Bt.TITLE_ID_KEY, R.string.dialog_mon_cfg_wakeup_period_title);
        bundle.putInt(DialogGenericIcTtMs2Bt.MESSAGE_ID_KEY, R.string.dialog_mon_cfg_wakeup_period_message_body);
        bundle.putInt(DialogGenericIcTtMs2Bt.YES_BUTTON_LABEL_ID_KEY, R.string.done_button_label);
        bundle.putInt(DialogGenericIcTtMs2Bt.NO_BUTTON_LABEL_ID_KEY, R.string.cancel_button_label);
        bundle.putInt(MonitoringCfgActivity.MON_CFG_CURRENT_WAKEUP_PERIOD_IN_MINUTES_ARG, mMonCfg.getPeriodMin());
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, MonitoringCfgDialogs.WakeupPeriodDialog.toString());
    }
    private void startSmsCriteriaCfgDialog() {
        DialogGenericIcTtMs2Bt dialogFragment = new MonitoringCfgSmsCriteriaDialogFragment();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogGenericIcTtMs2Bt.ICON_ID_KEY, android.R.drawable.ic_menu_edit);
        bundle.putInt(DialogGenericIcTtMs2Bt.TITLE_ID_KEY, R.string.dialog_mon_cfg_sms_criteria_title);
        bundle.putInt(DialogGenericIcTtMs2Bt.MESSAGE_ID_KEY, R.string.dialog_mon_cfg_sms_criteria_message_body);
        bundle.putInt(DialogGenericIcTtMs2Bt.YES_BUTTON_LABEL_ID_KEY, R.string.done_button_label);
        bundle.putInt(DialogGenericIcTtMs2Bt.NO_BUTTON_LABEL_ID_KEY, R.string.cancel_button_label);
        bundle.putInt(MonitoringCfgActivity.MON_CFG_CURRENT_SMS_CRITERIA_ARG, mMonCfg.getSmsCriteria().ordinal());
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, MonitoringCfgDialogs.SmsCriteriaDialog.toString());
    }
    private void startGpsThresholdDialog() {
        DialogGenericIcTtMs2Bt dialogFragment = new MonitoringCfgGpsThresholdDialogFragment();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogGenericIcTtMs2Bt.ICON_ID_KEY, android.R.drawable.ic_menu_edit);
        bundle.putInt(DialogGenericIcTtMs2Bt.TITLE_ID_KEY, R.string.dialog_mon_cfg_gps_threshold_title);
        bundle.putInt(DialogGenericIcTtMs2Bt.MESSAGE_ID_KEY, R.string.dialog_mon_cfg_gps_threshold_message_body);
        bundle.putInt(DialogGenericIcTtMs2Bt.YES_BUTTON_LABEL_ID_KEY, R.string.done_button_label);
        bundle.putInt(DialogGenericIcTtMs2Bt.NO_BUTTON_LABEL_ID_KEY, R.string.cancel_button_label);
        bundle.putInt(MonitoringCfgActivity.MON_CFG_CURRENT_GPS_THRESHOLD_IN_METERS_ARG, mMonCfg.getGpsThresholdMeters());
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, MonitoringCfgDialogs.GpsThresholdDialog.toString());
    }
    private void startGpsFovDialog() {
        DialogGenericIcTtMs2Bt dialogFragment = new MonitoringCfgGpsFovDialogFragment();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogGenericIcTtMs2Bt.ICON_ID_KEY, android.R.drawable.ic_menu_edit);
        bundle.putInt(DialogGenericIcTtMs2Bt.TITLE_ID_KEY, R.string.dialog_mon_cfg_gps_fov_title);
        bundle.putInt(DialogGenericIcTtMs2Bt.MESSAGE_ID_KEY, R.string.dialog_mon_cfg_gps_fov_message_body);
        bundle.putInt(DialogGenericIcTtMs2Bt.YES_BUTTON_LABEL_ID_KEY, R.string.done_button_label);
        bundle.putInt(DialogGenericIcTtMs2Bt.NO_BUTTON_LABEL_ID_KEY, R.string.cancel_button_label);
        bundle.putInt(MonitoringCfgActivity.MON_CFG_CURRENT_GPS_FOV_ARG, mMonCfg.getGpsFov());
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, MonitoringCfgDialogs.GpsFovDialog.toString());
    }
    private void startGpsTimeoutDialog() {
        DialogGenericIcTtMs2Bt dialogFragment = new MonitoringCfgGpsTimeoutDialogFragment();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogGenericIcTtMs2Bt.ICON_ID_KEY, android.R.drawable.ic_menu_edit);
        bundle.putInt(DialogGenericIcTtMs2Bt.TITLE_ID_KEY, R.string.dialog_mon_cfg_gps_timeout_title);
        bundle.putInt(DialogGenericIcTtMs2Bt.MESSAGE_ID_KEY, R.string.dialog_mon_cfg_gps_timeout_message_body);
        bundle.putInt(DialogGenericIcTtMs2Bt.YES_BUTTON_LABEL_ID_KEY, R.string.done_button_label);
        bundle.putInt(DialogGenericIcTtMs2Bt.NO_BUTTON_LABEL_ID_KEY, R.string.cancel_button_label);
        bundle.putInt(MonitoringCfgActivity.MON_CFG_CURRENT_GPS_TIMEOUT_IN_MINUTES_ARG, mMonCfg.getGpsTimeout());
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, MonitoringCfgDialogs.GpsTimeoutDialog.toString());
    }
    private void startTempBoundsDialog() {
        DialogGenericIcTtMs2Bt dialogFragment = new MonitoringCfgTempBoundsDialogFragment();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogGenericIcTtMs2Bt.ICON_ID_KEY, android.R.drawable.ic_menu_edit);
        bundle.putInt(DialogGenericIcTtMs2Bt.TITLE_ID_KEY, R.string.dialog_mon_cfg_temp_bounds_title);
        bundle.putInt(DialogGenericIcTtMs2Bt.MESSAGE_ID_KEY, R.string.dialog_mon_cfg_temp_bounds_message_body);
        bundle.putInt(DialogGenericIcTtMs2Bt.YES_BUTTON_LABEL_ID_KEY, R.string.done_button_label);
        bundle.putInt(DialogGenericIcTtMs2Bt.NO_BUTTON_LABEL_ID_KEY, R.string.cancel_button_label);
        bundle.putDouble(MonitoringCfgActivity.MON_CFG_CURRENT_TEMP_BOUNDS_LOW_ARG, mMonCfg.getTempLow());
        bundle.putDouble(MonitoringCfgActivity.MON_CFG_CURRENT_TEMP_BOUNDS_HIGH_ARG, mMonCfg.getTempHigh());
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, MonitoringCfgDialogs.TempBoundsDialog.toString());
    }
    private void startSimBalanceDialog() {
        DialogGenericIcTtMs2Bt dialogFragment = new MonitoringCfgSimBalanceDialogFragment();
        FragmentManager manager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogGenericIcTtMs2Bt.ICON_ID_KEY, android.R.drawable.ic_menu_edit);
        bundle.putInt(DialogGenericIcTtMs2Bt.TITLE_ID_KEY, R.string.dialog_mon_cfg_sim_balance_title);
        bundle.putInt(DialogGenericIcTtMs2Bt.MESSAGE_ID_KEY, R.string.dialog_mon_cfg_sim_balance_message_body);
        bundle.putInt(DialogGenericIcTtMs2Bt.YES_BUTTON_LABEL_ID_KEY, R.string.done_button_label);
        bundle.putInt(DialogGenericIcTtMs2Bt.NO_BUTTON_LABEL_ID_KEY, R.string.cancel_button_label);
        bundle.putDouble(MonitoringCfgActivity.MON_CFG_CURRENT_SIM_BALANCE_ARG, mMonCfg.getSimBalanceThreshold());
        dialogFragment.setArguments(bundle);
        dialogFragment.show(manager, MonitoringCfgDialogs.SimBalanceDialog.toString());
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        dialog.dismiss();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        String dialogTag = dialog.getTag();
        if (MonitoringCfgDialogs.WakeupTimeModDialog.toString().equals(dialogTag)) {
            // New Minute of day config
            MonitoringCfgTimeToWakeDialogFragment timeToWake = (MonitoringCfgTimeToWakeDialogFragment) dialog;
            int timeMod = timeToWake.getNewTimeToWakeMinutes();
            onClickWakeupTimeModConfig(timeMod);
            dialog.dismiss();
            return;
        }
        if (MonitoringCfgDialogs.WakeupPeriodDialog.toString().equals(dialogTag)) {
            int period = ((MonitoringCfgWakeupPeriodDialogFragment)dialog).getNewWakeupPeriodInMinutes();
            onClickWakeupPeriodConfig(period);
            dialog.dismiss();
            return;
        }
        if (MonitoringCfgDialogs.SmsCriteriaDialog.toString().equals(dialogTag)) {
            MonitoringConfiguration.SmsCriteria smsCriteria = ((MonitoringCfgSmsCriteriaDialogFragment)dialog).getNewSmsCriteria();
            onClickSmsCriteriaConfig(smsCriteria);
            dialog.dismiss();
            return;
        }
        if (MonitoringCfgDialogs.GpsThresholdDialog.toString().equals(dialogTag)) {
            int gpsThresholdMeters = ((MonitoringCfgGpsThresholdDialogFragment)dialog).getNewGpsThreshold();
            onClickGpsThresholdConfig(gpsThresholdMeters);
            dialog.dismiss();
            return;
        }
        if (MonitoringCfgDialogs.GpsFovDialog.toString().equals(dialogTag)) {
            int gpsFov = ((MonitoringCfgGpsFovDialogFragment)dialog).getNewGpsFov();
            onClickGpsFovConfig(gpsFov);
            dialog.dismiss();
            return;
        }
        if (MonitoringCfgDialogs.GpsTimeoutDialog.toString().equals(dialogTag)) {
            int gpsTimeoutMinutes = ((MonitoringCfgGpsTimeoutDialogFragment)dialog).getNewGpsTimeout();
            onClickGpsTimeoutConfig(gpsTimeoutMinutes);
            dialog.dismiss();
            return;
        }
        if (MonitoringCfgDialogs.TempBoundsDialog.toString().equals(dialogTag)) {
            double tempLow = ((MonitoringCfgTempBoundsDialogFragment)dialog).getNewTempLow();
            double tempHigh = ((MonitoringCfgTempBoundsDialogFragment)dialog).getNewTempHigh();
            onClickTempBoundsConfig(tempLow, tempHigh);
            dialog.dismiss();
            return;
        }
        if (MonitoringCfgDialogs.SimBalanceDialog.toString().equals(dialogTag)) {
            double simBalanceWaterLine = ((MonitoringCfgSimBalanceDialogFragment)dialog).getNewSimBalance();
            onClickSimBalanceConfig(simBalanceWaterLine);
            dialog.dismiss();
            return;
        }

    }

    private void updateBleStateAndInvalidateMenu(GuardTrackerActivity.BleStateEnum newState) {
        mBleState = newState;
        invalidateOptionsMenu();
    }

    // Device scan callback.
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            if (device.getAddress().equals(mGuardTracker.getBleId())) {
                stopBleScan();
                updateBleStateAndInvalidateMenu(GuardTrackerActivity.BleStateEnum.Connect);
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        Log.i(TAG, "onStart");

    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop()");
        super.onStop();

        if (mBleConnControl != null) {
            mBleConnControl.close();
            mBleConnControl = null;
        }

    }


    @Override
    protected void onPause() {
        Log.i(TAG, "onPause()");
        super.onPause();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState()");

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestart() {
        Log.i(TAG, "onRestart()");
        super.onRestart();

    }

    private Runnable stopBleScanRunnable = new Runnable() {
        @Override
        public void run() {
            stopBleScan();
            invalidateOptionsMenu();
        }
    };
    private void startBleScan() {
        // Change default scan parameters
        ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder();
        // LOW_LATENCY enables the capture of device advertisements with a cycle of 20 seconds.
        scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        ScanSettings scanSettings = scanSettingsBuilder.build();
        // Stops scanning after a pre-defined scan period.
        mHandler.postDelayed(stopBleScanRunnable, GuardTrackerActivity.SCAN_PERIOD);
//        mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
        mBluetoothAdapter.getBluetoothLeScanner().startScan(null, scanSettings, mScanCallback);

        updateBleStateAndInvalidateMenu(GuardTrackerActivity.BleStateEnum.Scanning);
    }
    private void stopBleScan() {
        mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
        mHandler.removeCallbacks(stopBleScanRunnable);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.guard_tracker_menu, menu);

        MenuItem stop = menu.findItem(R.id.menu_stop);
        MenuItem scan = menu.findItem(R.id.menu_scan);
        MenuItem refresh = menu.findItem(R.id.menu_refresh);
        MenuItem connect = menu.findItem(R.id.menu_connect);
        MenuItem disconnect = menu.findItem(R.id.menu_disconnect);

        MenuItem resync = menu.findItem(R.id.action_resynnc_app);
        MenuItem factoryDefaults = menu.findItem(R.id.action_factory_defaults);

        stop.setVisible(mBleState == GuardTrackerActivity.BleStateEnum.Scanning || mBleState == GuardTrackerActivity.BleStateEnum.Connecting);
        scan.setVisible(mBleState == GuardTrackerActivity.BleStateEnum.Disconnected);
        connect.setVisible(mBleState == GuardTrackerActivity.BleStateEnum.Connect);
        disconnect.setVisible(mBleState == GuardTrackerActivity.BleStateEnum.Connected);
        if (mBleState == GuardTrackerActivity.BleStateEnum.Scanning || mBleState == GuardTrackerActivity.BleStateEnum.Connecting)
            refresh.setActionView(R.layout.actionbar_indeterminate_progress);
        else
            refresh.setActionView(null);
        resync.setEnabled(mBleState == GuardTrackerActivity.BleStateEnum.Connected);
        factoryDefaults.setEnabled(mBleState == GuardTrackerActivity.BleStateEnum.Connected);

//        MenuItem bleMenuItem = menu.findItem(R.id.ble_status_icon);
//        Drawable drawable = mBleConnected ? getDrawable(R.drawable.ic_action_bluetooth) : null;
//
//        bleMenuItem.setIcon(drawable);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_stop) {
            if (mBleState == GuardTrackerActivity.BleStateEnum.Scanning) {
                stopBleScan();
                updateBleStateAndInvalidateMenu(GuardTrackerActivity.BleStateEnum.Disconnected);
            } else if (mBleState == GuardTrackerActivity.BleStateEnum.Connecting) {
                mBleConnControl.close();
                mBleConnControl = null;
                updateBleStateAndInvalidateMenu(GuardTrackerActivity.BleStateEnum.Disconnected);
                // ToDo: desactivar os componentes bluetooth.
            }
            return true;
        }
        if (id == R.id.menu_scan) {
            // Use this check to determine whether BLE is supported on the device.  Then you can
            // selectively disable BLE-related features.
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
                return true;
            }
            // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
            // BluetoothAdapter through BluetoothManager.
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
            // Checks if Bluetooth is supported on the device.
            if (mBluetoothAdapter == null) {
                Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
                return true;
            }
            if (!mBluetoothAdapter.isEnabled()) {
                Toast.makeText(this, "Enable BLUETOOTH you son of a ....", Toast.LENGTH_LONG);
                //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                //startActivityForResult(enableBtIntent, GuardTrackerActivity.REQUEST_ENABLE_BT);
                return true;
            }
            startBleScan(); // Já altera o estado de mBleSate e invalida o menu

            return true;
        }
        if (id == R.id.menu_connect) {
            if (mBleConnControl != null)
                mBleConnControl.connect();
            else {
                mBleConnControl = new GuardTrackerBleConnControl(this.getBaseContext(), mGuardTracker.getBleId(),
                        mGuardTracker.getOwnerPhoneNumber(), new BleConnectionStateChangeAdapter(), null);
            }
            updateBleStateAndInvalidateMenu(GuardTrackerActivity.BleStateEnum.Connecting);
            return true;
        }
        if (id == R.id.menu_disconnect) {
            mBleConnControl.disconnect();
            updateBleStateAndInvalidateMenu(GuardTrackerActivity.BleStateEnum.Connect);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class BleConnectionStateChangeAdapter implements BleConnectionStateChangeListener {
        @Override
        public void onAuthenticated() {

        }

        @Override
        public void onBindServiceError() {

        }

        @Override
        public void onUnableToDiscoverServices() {

        }

        @Override
        public void onConnected() {

        }

        @Override
        public void onReconnected() {

        }

        @Override
        public void onDisconnected() {

        }

        @Override
        public void onUnableToDiscoverDataService() {

        }

        @Override
        public void onDataChannelDiscovered() {

        }

        @Override
        public void onUnexpectedMessage(byte[] msg, int detailMsgRscId) {

        }

        @Override
        public void onReady() {

        }
    }

}
