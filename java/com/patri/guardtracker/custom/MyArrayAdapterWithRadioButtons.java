package com.patri.guardtracker.custom;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.ArrayRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;

import com.patri.guardtracker.R;

/**
 * Created by patri on 19/12/2016.
 */
public class MyArrayAdapterWithRadioButtons<T> extends ArrayAdapter<T> {
    public final static String TAG = MyArrayAdapterWithRadioButtons.class.getSimpleName();

    private boolean[] mItemCheckedState;
    private int mItemCheckedCount;
    private ListView mView;

    public static ArrayAdapter<CharSequence> createFromResource(Context context,
                                                                @ArrayRes int textArrayResId, @LayoutRes int textViewResId, ListView view) {
        CharSequence[] strings = context.getResources().getTextArray(textArrayResId);
        return new MyArrayAdapterWithRadioButtons<CharSequence>(context, textViewResId, strings, view);
    }

    public MyArrayAdapterWithRadioButtons(Context context, @LayoutRes int textViewResourceId, @NonNull T[] objects, ListView view) {
        super(context, textViewResourceId, android.R.id.text1, objects);

        mItemCheckedState = new boolean[this.getCount()];
        mItemCheckedCount = 0;
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
