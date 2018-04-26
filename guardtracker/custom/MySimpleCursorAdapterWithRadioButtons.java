package com.patri.guardtracker.custom;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioButton;

import com.patri.guardtracker.R;

/**
 * Created by patri on 19/12/2016.
 */
public class MySimpleCursorAdapterWithRadioButtons extends SimpleCursorAdapter {
    public final static String TAG = MySimpleCursorAdapterWithRadioButtons.class.getSimpleName();
    private boolean[] mItemCheckedState;
    private int mItemCheckedCount;
    private Cursor mCursor;
    private ListView mView;

    public MySimpleCursorAdapterWithRadioButtons(Context context, int layout, Cursor c, String[] from, int[] to, int flags, ListView view) {
        super(context, layout, c, from, to, flags);

        mItemCheckedState = new boolean[c.getCount()];
        mItemCheckedCount = 0;
        mCursor = c;
        mView = view;

        view.setOnItemClickListener(new ListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView parent, View view, final int position, long id) {
                ListView listView1 = (ListView) parent;
                boolean isChecked = mItemCheckedState[position];
                isChecked = !isChecked;
                Log.i(TAG, "onItemClick: item checked: position = " + position + "; id = " + id + "; isChecked = " + isChecked);
                RadioButton radioButton = (RadioButton) view.findViewById(R.id.radio_button);
                radioButton.setChecked(isChecked);
                mItemCheckedState[position] = isChecked;
                mItemCheckedCount += (isChecked ? +1 : -1);
                listView1.setItemChecked(position, isChecked);

            }
        });

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewGroup viewGroup = null;
        viewGroup = (ViewGroup) super.getView(position, convertView, parent);
        RadioButton radioButton = (RadioButton)viewGroup.findViewById(R.id.radio_button);
        radioButton.setChecked(mItemCheckedState[position]);

        return viewGroup;
    }
}
