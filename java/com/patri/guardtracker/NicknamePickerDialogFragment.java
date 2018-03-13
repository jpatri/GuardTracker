package com.patri.guardtracker;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
public class NicknamePickerDialogFragment extends DialogGenericIcTtMs2Bt {
    public final static String TAG = NicknamePickerDialogFragment.class.getSimpleName();
    public static final String NICKNAME_ORIGINAL_ID_KEY = "NicknamePickerDialogFragment.NICKNAME_ORIGINAL_ID_KEY";

    protected ViewGroup mViewContainer; // protected to be accessed in GuardTrackerPAiringDialogFragment.
    private EditText mInputText;

    public String getText() {
        String text = "" + mInputText.getText();
        return text;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mViewContainer = (ViewGroup)inflater.inflate(R.layout.fragment_dialog_input_text, null);
        mInputText = (EditText)mViewContainer.findViewById(R.id.dialog_input_text);

        Bundle bundle = getArguments();
        String originalText = bundle.getString(NicknamePickerDialogFragment.NICKNAME_ORIGINAL_ID_KEY);
        mInputText.setText("" + originalText);

        mAlertDialog.setView(mViewContainer);

        return mAlertDialog;

    }
}
