package com.patri.guardtracker;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.patri.guardtracker.model.MonitoringConfiguration;

/**
 * Created by patri on 01/11/2016.
 */
public class MonitoringCfgBackupViewFragment extends AppCompatDialogFragment {
    public static final String MON_CFG_ID = "MonitoringCfgBackupViewFragment.MON_CFG_ID";
    public final static String TAG = MonitoringCfgBackupViewFragment.class.getSimpleName();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        int monCfgId = bundle.getInt(MON_CFG_ID);
        MonitoringConfiguration monCfg = MonitoringConfiguration.read(getContext(), monCfgId);
        int dialogTitleId = R.string.dialog_mon_cfg_view_synced_title;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // Set content data
        ListView listView = new ListView(getContext());
        //listView.setClickable(false); Não teve efeito

        final String [][] elem = new String[][] {
                { getString(R.string.mon_cfg_item_title_hod), monCfg.getPrettyStartTime() },
                { getString(R.string.mon_cfg_item_title_period), monCfg.getPrettyPeriod() },
                { getString(R.string.mon_cfg_item_title_sms_criteria), monCfg.getPrettySmsCriteria() },
                { getString(R.string.mon_cfg_item_title_gps_threshold), monCfg.getPrettyGpsThreshold() },
                { getString(R.string.mon_cfg_item_title_gps_fov), monCfg.getPrettyGpsFov() },
                { getString(R.string.mon_cfg_item_title_gps_timeout), monCfg.getPrettyGpsTimeout() },
                { getString(R.string.mon_cfg_item_title_temp), monCfg.getPrettyTempLow() + "    " + monCfg.getPrettyTempHigh() },
                { getString(R.string.mon_cfg_item_title_sim), monCfg.getPrettySimBalance() }
        };

        // A simple ArrayAdapter can only be represented by a TextView
        ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_2, android.R.id.text1, elem) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);

                text1.setText(elem[position][0]);
                text2.setText(elem[position][1]);
                return view;
            }
        };

        listView.setAdapter(arrayAdapter);

        int mediumLen = (int)getResources().getDimension(R.dimen.margin_medium);
        //viewContainer.setLayoutParams(new ViewGroup.LayoutParams(getContext(), Layout.Alignment.ALIGN_CENTER, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        listView.setPadding(0, mediumLen, 0, mediumLen);
        builder.setCancelable(true)
                .setView(listView)
                .setTitle(dialogTitleId)
                .setPositiveButton(R.string.close_label, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing.
                    }
                });


        AlertDialog mAlertDialog = builder.create();
        return mAlertDialog;
    }
}
