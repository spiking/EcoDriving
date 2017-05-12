package com.example.currentplacedetailsonmap.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.example.currentplacedetailsonmap.R;
import com.example.currentplacedetailsonmap.fragments.MapFragment;
import com.example.currentplacedetailsonmap.models.LatLngSerializedObject;
import com.example.currentplacedetailsonmap.models.Session;
import com.example.currentplacedetailsonmap.services.DataService;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.HashMap;

public class DetailedStatsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private HashMap<Integer, Integer> mScores;
    private LineGraphSeries<DataPoint> mSeries;
    private DataPoint[] mValues;
    private ArrayList<LatLngSerializedObject> mRoute;
    private MapFragment mapFragment;

    private int mIndex;
    private String mDate;
    private int mScore;
    private double mDistance;
    private long mTime;
    private TextView mScoreTextView;
    private TextView mDateTextView;
    private TextView mDistanceTextView;
    private TextView mTimeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_stats);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {

            mIndex = extras.getInt("INDEX");
            Session session = DataService.getInstance().getSessions().get(mIndex);

            if (session != null) {
                mDate = session.getDate();
                mScore = session.getCurrentScore();
                mDistance = session.getTravelDistance();
                mTime = session.getTravelTime();
                mScores = session.getAllScores();
                mRoute = session.getRoute();
            } else {
                mDate = "";
                mScore = 0;
                mDistance = 0;
                mTime = 0;
                mScores = new HashMap<Integer, Integer>();
                mRoute = new ArrayList<LatLngSerializedObject>();
            }

        }

        mScoreTextView = (TextView) findViewById(R.id.stats_score);
        mScoreTextView.setText("Score: " + Integer.toString(mScore));
        mDateTextView = (TextView) findViewById(R.id.stats_date);
        mDateTextView.setText("Date: " + mDate);
        mDistanceTextView = (TextView) findViewById(R.id.stats_distance);
        mDistanceTextView.setText("Distance: " + String.format("%.0f", mDistance) + " m");
        mTimeTextView = (TextView) findViewById(R.id.stats_time);
        mTimeTextView.setText("Travel time: " + Long.toString(mTime) + " s");

        addGraphData();

        GraphView graph = (GraphView) findViewById(R.id.graph);

        if (mScores != null) {
            mSeries = new LineGraphSeries<>(mValues);
            mSeries.setThickness(15);
            mSeries.setColor(Color.parseColor("#4CAF50"));
            graph.addSeries(mSeries);
            graph.getViewport().setScalable(true);
        }

        // Add route to map view
        mHandler.postDelayed(runnable, 500);

        FragmentManager manager = getSupportFragmentManager();
        mapFragment = (MapFragment) manager.findFragmentById(R.id.map_fragment);

    }

    private Handler mHandler = new Handler();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mapFragment.drawRoute(mRoute);
        }
    };

    public void addGraphData() {

        if (mScores == null || mScores.isEmpty()) {
            return;
        }

        mScores.put(0, 0);
        mValues = new DataPoint[mScores.size() - 1];

        for (int i = 0; i < mScores.size() - 1; i++) {
            DataPoint dp = new DataPoint(i, mScores.get(i));
            mValues[i] = dp;
        }
    }
}
