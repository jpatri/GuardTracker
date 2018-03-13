package com.patri.guardtracker;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

/**
 * Created by patri on 05/11/2016.
 */
public class DialogGenericIcTtMs2Bt extends AppCompatDialogFragment {
    public static final String ICON_ID_KEY = "DialogIcTtMs2BtFragment.ICON_ID_KEY";
    public static final String TITLE_ID_KEY = "DialogIcTtMs2BtFragment.TITLE_ID_KEY";
    public static final String MESSAGE_ID_KEY = "DialogIcTtMs2BtFragment.MESSAGE_ID_KEY";
    public static final String YES_BUTTON_LABEL_ID_KEY = "DialogIcTtMs2BtFragment.YES_BUTTON_LABEL_ID_KEY";
    public static final String NO_BUTTON_LABEL_ID_KEY = "DialogIcTtMs2BtFragment.NO_BUTTON_LABEL_ID_KEY";

    public final static String TAG = DialogGenericIcTtMs2Bt.class.getSimpleName();

    AlertDialog mAlertDialog;

    int mIconId;
    int mTitleId;
    int mMessageId;
    int mYesButtonLabelId;
    int mNoButtonLabelId;
    // Use this instance of the interface to deliver action events
    DialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (DialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement DialogListener");
        }
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        mIconId = bundle.getInt(ICON_ID_KEY);
        mTitleId = bundle.getInt(TITLE_ID_KEY);
        mMessageId = bundle.getInt(MESSAGE_ID_KEY);
        mYesButtonLabelId = bundle.getInt(YES_BUTTON_LABEL_ID_KEY);
        mNoButtonLabelId = bundle.getInt(NO_BUTTON_LABEL_ID_KEY);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setCancelable(true)
                .setIcon(mIconId)
                .setTitle(mTitleId)
                .setMessage(mMessageId)
                .setPositiveButton(mYesButtonLabelId, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogPositiveClick(DialogGenericIcTtMs2Bt.this);
                    }
                })
                .setNegativeButton(mNoButtonLabelId, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogNegativeClick(DialogGenericIcTtMs2Bt.this);
                    }
                });

        mAlertDialog = builder.create();
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
