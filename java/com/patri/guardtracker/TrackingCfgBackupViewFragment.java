package com.patri.guardtracker;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.patri.guardtracker.model.MonitoringConfiguration;
import com.patri.guardtracker.model.TrackingConfiguration;

/**
 * Created by patri on 01/11/2016.
 */
public class TrackingCfgBackupViewFragment extends AppCompatDialogFragment {
    public static final String TRACK_CFG_ID = "TtrackingCfgBackupViewFragment.TRACK_CFG_ID";
    public final static String TAG = TrackingCfgBackupViewFragment.class.getSimpleName();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        int trackCfgId = bundle.getInt(TRACK_CFG_ID);
        TrackingConfiguration trackCfg = TrackingConfiguration.read(getContext(), trackCfgId);
        int dialogTitleId = R.string.dialog_track_cfg_view_synced_title;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // Set content data
        ListView listView = new ListView(getContext());
        //listView.setClickable(false); Não teve efeito

        final String [][] elem = new String[][] {
                { getString(R.string.track_cfg_item_title_sms_criteria), trackCfg.getPrettySmsCriteria() },
                { getString(R.string.track_cfg_item_title_gps_threshold), trackCfg.getPrettyGpsThreshold() },
                { getString(R.string.track_cfg_item_title_gps_fov), trackCfg.getPrettyGpsFov() },
                { getString(R.string.track_cfg_item_title_gps_timeout), trackCfg.getPrettyGpsTimeout() },
                { getString(R.string.track_cfg_item_title_timeout_tracking), trackCfg.getPrettyTimeoutTracking() },
                { getString(R.string.track_cfg_item_title_timeout_pre), trackCfg.getPrettyTimeoutPre() },
                { getString(R.string.track_cfg_item_title_timeout_post), trackCfg.getPrettyTimeoutPost() }
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
