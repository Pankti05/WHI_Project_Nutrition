package com.ron004.calx;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Trend extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private GraphView bargraph;
    private GraphView linegraph;
    final ArrayList<String> getdates = new ArrayList<String>();
    final ArrayList<Integer> getcals = new ArrayList<Integer>();

    private OnFragmentInteractionListener mListener;

    public Trend() {
        // Required empty public constructor
    }

    public static Trend newInstance(String param1, String param2) {
        Trend fragment = new Trend();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.trend_frag, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        bargraph = view.findViewById(R.id.bargraph);
        linegraph = view.findViewById(R.id.linegraph);
        DatabaseHelper db = new DatabaseHelper(getActivity());

        Cursor c = db.readTrend();
        while(!c.isAfterLast()) {
            getdates.add(c.getString(0));
            getcals.add(c.getInt(1));
            c.moveToNext();
        }

        List<String> lastdates = getdates.subList(Math.max(getdates.size() - 7, 0), getdates.size());
        List<Integer> lastcals = getcals.subList(Math.max(getcals.size() - 7, 0), getcals.size());

        String[] finaldates = new String[lastdates.size()];
        finaldates = lastdates.toArray(finaldates);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        DataPoint[] dataPoints = new DataPoint[lastcals.size()];
        for(int i=0; i<lastcals.size(); i++) {
            try {
                Date d = sdf.parse(lastdates.get(i));
                DataPoint dp = new DataPoint(d, lastcals.get(i));
                dataPoints[i] = dp;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        BarGraphSeries<DataPoint> barseries = new BarGraphSeries<>(dataPoints);
        barseries.setColor(Color.parseColor("#00BAE8"));

        barseries.setSpacing(50);
        barseries.setDrawValuesOnTop(true);
        barseries.setValuesOnTopColor(Color.RED);
        barseries.setValuesOnTopSize(50);

        barseries.setValueDependentColor(new ValueDependentColor<DataPoint>() {
            @Override
            public int get(DataPoint data) {
                return Color.rgb((int) data.getX()*255/4, (int) Math.abs(data.getY()*255/6), 100);
            }
        });

        bargraph.addSeries(barseries);
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(bargraph);
        staticLabelsFormatter.setHorizontalLabels(finaldates);
        bargraph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);

        bargraph.getGridLabelRenderer().setPadding(20);
        bargraph.getViewport().setXAxisBoundsManual(true);
        bargraph.getViewport().setMinX(barseries.getLowestValueX());
        bargraph.getViewport().setMaxX(barseries.getHighestValueX());

        bargraph.getGridLabelRenderer().setNumHorizontalLabels(7);
        bargraph.getGridLabelRenderer().setHighlightZeroLines(false);
        bargraph.getGridLabelRenderer().setVerticalLabelsAlign(Paint.Align.LEFT);
        bargraph.getGridLabelRenderer().setLabelVerticalWidth(100);
        bargraph.getGridLabelRenderer().setTextSize(40);
        bargraph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        bargraph.getGridLabelRenderer().setHorizontalLabelsAngle(120);
        bargraph.getGridLabelRenderer().setHumanRounding(false);
        bargraph.getGridLabelRenderer().reloadStyles();



        LineGraphSeries<DataPoint> lineseries = new LineGraphSeries<>(dataPoints);
        lineseries.setColor(Color.parseColor("#00BAE8"));
        lineseries.setThickness(8);

        linegraph.addSeries(lineseries);
        StaticLabelsFormatter staticLabelsFormatter1 = new StaticLabelsFormatter(linegraph);
        staticLabelsFormatter1.setHorizontalLabels(finaldates);
        linegraph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter1);

        linegraph.getViewport().setXAxisBoundsManual(true);
        linegraph.getViewport().setMinX(barseries.getLowestValueX());
        linegraph.getViewport().setMaxX(barseries.getHighestValueX());

        linegraph.getGridLabelRenderer().setPadding(20);
        linegraph.getGridLabelRenderer().setNumHorizontalLabels(7);
        linegraph.getGridLabelRenderer().setHighlightZeroLines(false);
        linegraph.getGridLabelRenderer().setVerticalLabelsAlign(Paint.Align.LEFT);
        linegraph.getGridLabelRenderer().setLabelVerticalWidth(100);
        linegraph.getGridLabelRenderer().setTextSize(40);
        linegraph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        linegraph.getGridLabelRenderer().setHorizontalLabelsAngle(120);
        linegraph.getGridLabelRenderer().setHumanRounding(false);
        linegraph.getGridLabelRenderer().reloadStyles();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
