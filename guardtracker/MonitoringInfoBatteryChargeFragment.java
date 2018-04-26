package com.patri.guardtracker;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.patri.guardtracker.custom.MyMarkerView;
import com.patri.guardtracker.model.MonitoringInfo;

import java.util.ArrayList;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MonitoringInfoBatteryChargeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MonitoringInfoBatteryChargeFragment extends Fragment implements OnChartGestureListener, OnChartValueSelectedListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "guardTrackerId";
    private static final String TAG = MonitoringInfoBatteryChargeFragment.class.getSimpleName();

    // TODO: Rename and change types of parameters
    private int mGuardTrackerId;
    private ArrayList<String> mTimeDataSet = new ArrayList<String>();
    private ArrayList<Entry> mBatteryChargeDataSet = new ArrayList<Entry>();
    private LineChart mChart;


    public MonitoringInfoBatteryChargeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param guardTrackerId Parameter 1.
     * @return A new instance of fragment MonitoringInfoBatteryChargeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MonitoringInfoBatteryChargeFragment newInstance(int guardTrackerId) {
        MonitoringInfoBatteryChargeFragment fragment = new MonitoringInfoBatteryChargeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, guardTrackerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mGuardTrackerId = getArguments().getInt(ARG_PARAM1);
        } else
            mGuardTrackerId = 0;

        ArrayList<MonitoringInfo> monInfoList = MonitoringInfo.readList(getContext(), mGuardTrackerId);

        mTimeDataSet.clear();
        mBatteryChargeDataSet.clear();
        int i = 0;

        for (MonitoringInfo monInfo: monInfoList) {

            if (monInfo.hasBatCharge()) {
                int batteryCharge = monInfo.getBatCharge();
                mBatteryChargeDataSet.add(new Entry(batteryCharge, i));
            }
            Date dateTime = monInfo.getDate();
            String dateTimeStr = "" + android.text.format.DateFormat.format("yy-MM-dd HH:mm:ss", dateTime);
            mTimeDataSet.add(dateTimeStr);

            i+= 1;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_battery_charge_info, container, false);
        // Continue creating the graph...
        mChart = (LineChart)view.findViewById(R.id.batteryChargeChart);

        mChart.setBackgroundColor(Color.WHITE);

        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDrawGridBackground(false);
        // no description text
        mChart.setDescription("Description example");
        mChart.setNoDataTextDescription("You need to provide data for the chart.");

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        // mChart.setScaleXEnabled(true);
        // mChart.setScaleYEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        // mChart.setBackgroundColor(Color.GRAY);

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        MyMarkerView mv = new MyMarkerView(getContext(), R.layout.custom_marker_view);

        // set the marker to the chart
        mChart.setMarkerView(mv);

        // x-axis limit line
        LimitLine llXAxis = new LimitLine(10f, "Index 10");
        llXAxis.setLineWidth(4f);
        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);

        XAxis xAxis = mChart.getXAxis();
        //xAxis.setValueFormatter(new MyCustomXAxisValueFormatter());
        //xAxis.addLimitLine(llXAxis); // add x-axis limit line

        //Typeface tf = Typeface.createFromAsset(this.getActivity().getAssets(), "OpenSans-Regular.ttf");

//        LimitLine ll1 = new LimitLine(3600f/*threshold temperature high*/, "Upper Limit");
//        ll1.setLineWidth(4f);
//        ll1.enableDashedLine(10f, 10f, 0f);
//        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
//        ll1.setTextSize(10f);
//        //ll1.setTypeface(tf);

        LimitLine ll2 = new LimitLine(3100f/*threshold temperature low*/, "Lower Limit");
        ll2.setLineWidth(3f);
        ll2.enableDashedLine(10f, 10f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        ll2.setTextSize(10f);
        //ll2.setTypeface(tf);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
//        leftAxis.addLimitLine(ll1);
        leftAxis.addLimitLine(ll2);
        leftAxis.setAxisMaxValue(3700f);
        leftAxis.setAxisMinValue(2900f);
        //leftAxis.setYOffset(20f);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(true);

        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);

        mChart.getAxisRight().setEnabled(false);

        //mChart.getViewPortHandler().setMaximumScaleY(2f);
        //mChart.getViewPortHandler().setMaximumScaleX(2f);

        // add data
        setData(mBatteryChargeDataSet, mTimeDataSet);

//        mChart.setVisibleXRange(20);
//        mChart.setVisibleYRange(20f, AxisDependency.LEFT);
//        mChart.centerViewTo(20, 50, AxisDependency.LEFT);

        mChart.animateX(2500, Easing.EasingOption.EaseInOutQuart);
//        mChart.invalidate();

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(Legend.LegendForm.LINE);

        // // dont forget to refresh the drawing
        mChart.invalidate();

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /*
     * OnChartGestureListener implementation
     */
    @Override
    public void onChartDoubleTapped(MotionEvent motionEvent) {

    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture chartGesture) {
        Log.i("Gesture", "START, x: " + me.getX() + ", y: " + me.getY());

    }

    @Override
    public void onChartGestureEnd(MotionEvent motionEvent, ChartTouchListener.ChartGesture chartGesture) {
        Log.i("Gesture", "END, lastGesture: " + chartGesture);

        // un-highlight values after the gesture is finished and no single-tap
        if(chartGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
            mChart.highlightValues(null); // or highlightTouch(null) for callback to onNothingSelected(...)
    }

    @Override
    public void onChartLongPressed(MotionEvent motionEvent) { Log.i("LongPress", "Chart longpressed."); }

    @Override
    public void onChartSingleTapped(MotionEvent motionEvent) { Log.i("SingleTap", "Chart single-tapped."); }

    @Override
    public void onChartFling(MotionEvent motionEvent, MotionEvent motionEvent1, float velocityX, float velocityY) {
        Log.i("Fling", "Chart flinged. VeloX: " + velocityX + ", VeloY: " + velocityY);
    }

    @Override
    public void onChartScale(MotionEvent motionEvent, float scaleX, float scaleY) {
        Log.i("Scale / Zoom", "ScaleX: " + scaleX + ", ScaleY: " + scaleY);
    }

    @Override
    public void onChartTranslate(MotionEvent motionEvent, float dX, float dY) {
        Log.i("Translate / Move", "dX: " + dX + ", dY: " + dY);
    }

    /*
     * OnChartValueSelectedListener implementation
     */

    @Override
    public void onNothingSelected() { Log.i("Nothing selected", "Nothing selected."); }

    @Override
    public void onValueSelected(Entry entry, int i, Highlight highlight) {
        Log.i("Entry selected", entry.toString());
        Log.i("LOWHIGH", "low: " + mChart.getLowestVisibleXIndex() + ", high: " + mChart.getHighestVisibleXIndex());
        Log.i("MIN MAX", "xmin: " + mChart.getXChartMin() + ", xmax: " + mChart.getXChartMax() + ", ymin: " + mChart.getYChartMin() + ", ymax: " + mChart.getYChartMax());
    }


    private void setData(ArrayList<Entry> batteryChargeDataSet, ArrayList<String> timeDataSet) {
//    private void setData(int count, float range) {

//        ArrayList<String> xVals = new ArrayList<String>();
//        for (int i = 0; i < count; i++) {
//            xVals.add((i) + "");
//        }

//        ArrayList<Entry> yVals = new ArrayList<Entry>();

//        for (int i = 0; i < count; i++) {

//            float mult = (range + 1);
//            float val = (float) (Math.random() * mult) + 3;// + (float)
        // ((mult *
        // 0.1) / 10);
//            yVals.add(new Entry(val, i));
//        }

        LineDataSet set1;


//        if (mChart.getData() != null &&
//                mChart.getData().getDataSetCount() > 0) {
//            set1 = (LineDataSet)mChart.getData().getDataSetByIndex(0);
//            set1.setYVals(yVals);
//            mChart.getData()..setXVals(xVals);
//            mChart.notifyDataSetChanged();
//        } else {

        // create a dataset and give it a type
        set1 = new LineDataSet(batteryChargeDataSet, getString(R.string.temperature_line_legend_temperature_info));

        // set1.setFillAlpha(110);
        // set1.setFillColor(Color.RED);

        // set the line to be drawn like this "- - - - - -"
        set1.enableDashedLine(10f, 5f, 0f);
        set1.enableDashedHighlightLine(10f, 5f, 0f);
        set1.setColor(Color.BLACK);
        set1.setCircleColor(Color.BLACK);
        set1.setLineWidth(1f);
        set1.setCircleRadius(3f);
        set1.setDrawCircleHole(false);
        set1.setValueTextSize(9f);
        set1.setDrawFilled(false);
        set1.setDrawCubic(true);
//        }

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(timeDataSet, dataSets);


        // set data
        mChart.setData(data);
    }

}
