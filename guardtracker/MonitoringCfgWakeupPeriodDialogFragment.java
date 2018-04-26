package com.patri.guardtracker;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.Spinner;

/**
 * Created by patri on 20/11/2016.
 */
public class MonitoringCfgWakeupPeriodDialogFragment extends DialogGenericIcTtMs2Bt {
    public final static String TAG = MonitoringCfgWakeupPeriodDialogFragment.class.getSimpleName();

    /*
     * Array of value limits for each domain period selection.
     * First for minutes; second for hours and last, third, for days domain.
     * It must have the same order that string-array in string.xml resources.
     */
    private static final int [] limits = { 60, 24, 30 };
    private static final int [] scales = { 1, 60, 60*24 };

    protected ViewGroup mViewContainer;
    private Spinner mDomainSpinner;
    private NumberPicker mPeriodNumberPicker;
    //protected Fragment mWakeupPeriodFragment;

    public int getNewWakeupPeriodInMinutes() {
        int currentPosition = mDomainSpinner.getSelectedItemPosition();

        int valueInMinutes = mPeriodNumberPicker.getValue() * scales[currentPosition];
        return valueInMinutes;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);


        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mViewContainer = (ViewGroup)inflater.inflate(R.layout.fragment_monitoring_cfg_wakeup_period, null);
        mAlertDialog.setView(mViewContainer);

        mDomainSpinner = (Spinner) mViewContainer.findViewById(R.id.domain_choice_spinner);
        mPeriodNumberPicker = (NumberPicker) mViewContainer.findViewById(R.id.numberPicker);

        Bundle bundle = getArguments();
        int currentWakeupPeriod = bundle.getInt(MonitoringCfgActivity.MON_CFG_CURRENT_WAKEUP_PERIOD_IN_MINUTES_ARG);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> enableAdapter = ArrayAdapter.createFromResource(getContext(), R.array.period_type_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        enableAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mDomainSpinner.setAdapter(enableAdapter);
        // Position last selected spinner item
        int spinnerPosition = (currentWakeupPeriod < 60) ? 0 : (currentWakeupPeriod < 24*60 ? 1 : 2);
        mDomainSpinner.setSelection(spinnerPosition);
        mDomainSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // An item was selected. You can retrieve the selected item using
                String itemSelected = (String) parent.getItemAtPosition(position);
                Log.i(TAG, "Enable item selected: id " + id + "; position " + position + "; itemName = " + itemSelected);
                mPeriodNumberPicker.setMaxValue(limits[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

        int number = currentWakeupPeriod / scales[spinnerPosition];
        mPeriodNumberPicker.setMinValue(1);
        mPeriodNumberPicker.setMaxValue(limits[spinnerPosition]);
        mPeriodNumberPicker.setValue(number);

//        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(final DialogInterface dialog) {
//                Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
//                b.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        //String countryCode = PhonePickerDialogFragment.this.getContext().getResources().getConfiguration().locale.getCountry();
//                        String countryCode = Locale.getDefault().getCountry();
//                        //TelephonyManager manager = (TelephonyManager) PhonePickerDialogFragment.this.getContext().getSystemService(Context.TELEPHONY_SERVICE);
//                        //String countryCode = manager.getNetworkCountryIso();
//                        Log.i(TAG, "onClick ok button");
//                        String phoneNumber = mInputText.getText().toString();
//
//                        try {
//                            String phone = MyPhoneUtils.formatE164(phoneNumber);
//                            mPhoneNumberE164 = phone;
//                        } catch (NumberParseException e) {
//                            mAlertText.setVisibility(View.VISIBLE);
//                            mPhoneNumberE164 = null;
//                            return;
//                        }
//                        if (mListener != null)
//                            mListener.onDialogPositiveClick(PhonePickerDialogFragment.this);
//                    }
//                });
//            }
//        });

        return mAlertDialog;

    }
}
