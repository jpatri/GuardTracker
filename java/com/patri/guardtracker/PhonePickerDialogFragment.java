package com.patri.guardtracker;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.i18n.phonenumbers.NumberParseException;
import com.patri.guardtracker.custom.MyPhoneUtils;

import java.util.Locale;

/**
 * Created by patri on 29/09/2016.
 */
public class PhonePickerDialogFragment extends DialogGenericIcTtMs2Bt {
    public final static String TAG = PhonePickerDialogFragment.class.getSimpleName();

    protected ViewGroup mViewContainer; // protected to be accessed in GuardTrackerPAiringDialogFragment.
    private TextView mAlertText;
    private EditText mInputText;
    private String mPhoneNumberE164; // phone number in national format XX XXX XX XX

    public String getPhoneNumber() {
        String number = mPhoneNumberE164;
        return number;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mViewContainer = (ViewGroup)inflater.inflate(R.layout.fragment_generic_dialog_input_phone, null);
        mInputText = (EditText)mViewContainer.findViewById(R.id.dialog_input_phone);
        mAlertText = (TextView)mViewContainer.findViewById(R.id.dialog_invalid_phone_format);
        mAlertText.setVisibility(View.INVISIBLE);

        mInputText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertText.setVisibility(View.INVISIBLE);
            }
        });

        mAlertDialog.setView(mViewContainer);

        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //String countryCode = PhonePickerDialogFragment.this.getContext().getResources().getConfiguration().locale.getCountry();
                        String countryCode = Locale.getDefault().getCountry();
                        //TelephonyManager manager = (TelephonyManager) PhonePickerDialogFragment.this.getContext().getSystemService(Context.TELEPHONY_SERVICE);
                        //String countryCode = manager.getNetworkCountryIso();
                        Log.i(TAG, "onClick ok button");
                        String phoneNumber = mInputText.getText().toString();

                        try {
                            String phone = MyPhoneUtils.formatE164(phoneNumber);
                            mPhoneNumberE164 = phone;
                        } catch (NumberParseException e) {
                            mAlertText.setVisibility(View.VISIBLE);
                            mPhoneNumberE164 = null;
                            return;
                        }
                        if (mListener != null)
                            mListener.onDialogPositiveClick(PhonePickerDialogFragment.this);
                    }
                });
            }
        });

        return mAlertDialog;

//        builder.setCancelable(true)
//                .setView(mViewContainer)
//                .setIcon(mIconId)
//                .setTitle(R.string.dialog_phone_title)
//                .setMessage(mMessageId)
//                .setPositiveButton(R.string.ok_button_label, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        // Do nothing.
//                    }
//                })
//                .setNegativeButton(R.string.cancel_button_label, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        mListener.onDialogNegativeClick(PhonePickerDialogFragment.this);
//                    }
//                });
//
//        mAlertDialog = builder.create();
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
//        return mAlertDialog;
    }
}
