package com.patri.guardtracker;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.patri.guardtracker.model.PermissionsChecker;
import com.patri.guardtracker.permissions.PermissionsActivity;

/**
 * Created by patri on 29/09/2016.
 */
public class GuardTrackerPairingDialogFragment extends PhonePickerDialogFragment {
    public final static String TAG = GuardTrackerPairingDialogFragment.class.getSimpleName();

    /**
     * Returns a CharSequence that concatenates the specified array of CharSequence
     * objects and then applies a list of zero or more tags to the entire range.
     *
     * @param content an array of character sequences to apply a style to
     * @param tags the styled span objects to apply to the content
     *        such as android.text.style.StyleSpan
     *
     */
    private static CharSequence apply(CharSequence[] content, Object... tags) {
        SpannableStringBuilder text = new SpannableStringBuilder();
        openTags(text, tags);
        for (CharSequence item : content) {
            text.append(item);
        }
        closeTags(text, tags);
        return text;
    }

    /**
     * Iterates over an array of tags and applies them to the beginning of the specified
     * Spannable object so that future text appended to the text will have the styling
     * applied to it. Do not call this method directly.
     */
    private static void openTags(Spannable text, Object[] tags) {
        for (Object tag : tags) {
            text.setSpan(tag, 0, 0, Spannable.SPAN_MARK_MARK);
        }
    }

    /**
     * "Closes" the specified tags on a Spannable by updating the spans to be
     * endpoint-exclusive so that future text appended to the end will not take
     * on the same styling. Do not call this method directly.
     */
    private static void closeTags(Spannable text, Object[] tags) {
        int len = text.length();
        for (Object tag : tags) {
            if (len > 0) {
                text.setSpan(tag, 0, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                text.removeSpan(tag);
            }
        }
    }

    /**
     * Returns a CharSequence that applies boldface to the concatenation
     * of the specified CharSequence objects.
     */
    public static CharSequence bold(CharSequence... content) {
        return apply(content, new StyleSpan(Typeface.BOLD));
    }

    /**
     * Returns a CharSequence that applies italics to the concatenation
     * of the specified CharSequence objects.
     */
    public static CharSequence italic(CharSequence... content) {
        return apply(content, new StyleSpan(Typeface.ITALIC));
    }

    /**
     * Returns a CharSequence that applies normal to the concatenation
     * of the specified CharSequence objects.
     */
    public static CharSequence normal(CharSequence... content) {
        return apply(content, new StyleSpan(Typeface.NORMAL));
    }

    /**
     * Returns a CharSequence that applies a foreground color to the
     * concatenation of the specified CharSequence objects.
     */
    public static CharSequence color(int color, CharSequence... content) {
        return apply(content, new ForegroundColorSpan(color));
    }

//    /* Attribute fro permissions checeker */
//    static final String[] PERMISSIONS = new String[]{Manifest.permission.SEND_SMS};
//    //private PermissionsChecker mChecker;
//    private static final int REQUEST_CODE = 0;

//    private void startPermissionsActivity() {
//        PermissionsActivity.startActivityForResult(this.getActivity(), REQUEST_CODE, PERMISSIONS);
//    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

//        final String[] PERMISSIONS = new String[] {Manifest.permission.SEND_SMS};
//        PermissionsChecker checker = new PermissionsChecker(getContext());
//
//        if (checker.lacksPermissions(PERMISSIONS)) {
//            startPermissionsActivity();
//        }

        return dialog;
    }

//    /**
//     * Remove next method when it is tested in GTDeviceControlActivity.
//     * @param requestCode
//     * @param resultCode
//     * @param data
//     */
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
//            this.dismiss();
//        }
//
//    }

}
