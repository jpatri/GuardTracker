package com.patri.guardtracker;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.NumberPicker;

import com.patri.guardtracker.model.MonitoringConfiguration;
import com.patri.guardtracker.model.TrackingConfiguration;

/**
 * Created by patri on 20/11/2016.
 */
public class TrackingCfgSmsCriteriaDialogFragment extends DialogGenericIcTtMs2Bt {
    public final static String TAG = TrackingCfgSmsCriteriaDialogFragment.class.getSimpleName();
    public final static String CFG_CURRENT_SMS_CRITERIA_ARG = "com.patri.guardtracker.TrackingCfgSmsCriteriaDialogFragment.CFG_CURRENT_SMS_CRITERIA_ARG";

    protected ViewGroup mViewContainer;
    private ListView mCriteriaListView;
    //private NumberPicker mCyclicNumberPicker;
    private int mLastPosition = -1; // Invalid position
    //private CheckedTextView mLastCheckedTextView = null;
    ArrayAdapter<CharSequence> mArrayAdapter;

    public TrackingConfiguration.SmsCriteria getNewSmsCriteria() {
        // Better to use mLastPosition. The next statment allways return -1.
        // int currentPosition = mCriteriaListView.getSelectedItemPosition();
        TrackingConfiguration.SmsCriteria criteria = TrackingConfiguration.SmsCriteria.fromInteger(mLastPosition);
        return criteria;
    }
    //ublic int getNewSmsCriteriaCyclicValue() {
    //    int cyclicValue = mCyclicNumberPicker.getValue();
    //    return cyclicValue;
    //}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mViewContainer = (ViewGroup)inflater.inflate(R.layout.fragment_cfg_sms_criteria, null);
        mAlertDialog.setView(mViewContainer);

        mCriteriaListView = mViewContainer.findViewById(R.id.sms_criteria_list_view);
        //mCyclicNumberPicker = mViewContainer.findViewById(R.id.timePicker);

        Bundle bundle = getArguments();
        int currentSmsCriteria = bundle.getInt(CFG_CURRENT_SMS_CRITERIA_ARG);

        //int currentCyclic = bundle.getInt(TrackingCfgActivity.MON_CFG_CURRENT_SMS_CRITERIA_CYCLIC_ARG, 1);
        //mCyclicNumberPicker.setMinValue(MonitoringConfiguration.SMS_CRITERIA_CYCLIC_MIN);
        //mCyclicNumberPicker.setMaxValue(MonitoringConfiguration.SMS_CRITERIA_CYCLIC_MAX);
        //mCyclicNumberPicker.setValue(currentCyclic);

        // Create an ArrayAdapter using the string array and a default spinner layout
//        ArrayAdapter<CharSequence> mArrayAdapter = new MyArrayAdapterWithRadioButtons(getContext(), R.array.monitoring_sms_criteria_array, R.layout.list_item_radio_button_one_line, mCriteriaListView);
//        ArrayAdapter<CharSequence> mArrayAdapter = MyArrayAdapterWithRadioButtons.createFromResource(getContext(), R.array.monitoring_sms_criteria_array, R.layout.list_item_radio_button_one_line2, mCriteriaListView);
//        ArrayAdapter<CharSequence> mArrayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.monitoring_sms_criteria_array, R.layout.list_item_radio_button_one_line/*android.R.layout.simple_list_item_1*/);
        CharSequence[] strings = getContext().getResources().getTextArray(R.array.tracking_sms_criteria_array);
        mArrayAdapter = new CustomArrayAdapter(getContext(), R.layout.list_item_radio_button_one_line, android.R.id.text1, strings);
        // Specify the layout to use when the list of choices appears
        mArrayAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        // Apply the adapter to the spinner
        mCriteriaListView.setAdapter(mArrayAdapter);
        // Position last selected spinner item
        mLastPosition = currentSmsCriteria;
        //ViewGroup viewGroup = (ViewGroup)inflater.inflate(R.layout.list_item_radio_button_one_line, null);
        //mLastCheckedTextView = (CheckedTextView)mArrayAdapter.getView(mLastPosition, null, viewGroup);
        //mLastCheckedTextView.setChecked(true);
//        mCriteriaListView.setItemChecked(mLastPosition, true);
        mCriteriaListView.setSelection(mLastPosition);

        mCriteriaListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemClick: id " + id + "; position " + position + "; v = " + view);
                ViewGroup rootListItem = (ViewGroup)getViewByPosition(mLastPosition, mCriteriaListView);
                CheckedTextView oldChecked = rootListItem.findViewById(android.R.id.text1);
                CheckedTextView newChecked = view.findViewById(android.R.id.text1);
                oldChecked.setChecked(false);
                newChecked.setChecked(true);
                mLastPosition = position;
                mCriteriaListView.setSelection(position);
                //mCyclicNumberPicker.setVisibility(TrackingConfiguration.SmsCriteria.Cyclic.ordinal() == position ? View.VISIBLE : View.GONE);
            }
        });
        mCriteriaListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // An item was selected. You can retrieve the selected item using
                String itemSelected = (String) parent.getItemAtPosition(position);
                Log.i(TAG, "Enable item selected: id " + id + "; position " + position + "; itemName = " + itemSelected);
                // ToDo: testar se o item seleccionado é o cyclic e se for tornar visível o NumberPicker.)
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

        return mAlertDialog;

    }

    class CustomArrayAdapter extends ArrayAdapter<CharSequence> {
        public CustomArrayAdapter(Context context, int resource, int textViewResourceId, CharSequence[] objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup viewGroup = null;
            viewGroup = (ViewGroup) super.getView(position, convertView, parent);
            CheckedTextView checkedTextView = viewGroup.findViewById(android.R.id.text1);
            if (mLastPosition == position) {
                checkedTextView.setChecked(true);
            }

            return viewGroup;
        }
    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

}
