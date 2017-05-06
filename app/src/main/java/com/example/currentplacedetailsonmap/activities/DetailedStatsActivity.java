package com.example.currentplacedetailsonmap.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.currentplacedetailsonmap.R;
import com.example.currentplacedetailsonmap.fragments.MapFragment;
import com.example.currentplacedetailsonmap.models.LatLngSerializedObject;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_stats);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            String date = extras.getString("DATE");
            int score = extras.getInt("SCORE");
            Intent intent = getIntent();
            mScores = (HashMap<Integer, Integer>) intent.getSerializableExtra("ALL_SCORES");
            mRoute = (ArrayList<LatLngSerializedObject>) intent.getSerializableExtra("ROUTE");
        }

        addGraphData();

        GraphView graph = (GraphView) findViewById(R.id.graph);
        mSeries = new LineGraphSeries<>(mValues);
        mSeries.setThickness(15);
        mSeries.setColor(Color.parseColor("#4CAF50"));
        graph.addSeries(mSeries);
        //GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
        //gridLabel.setHorizontalAxisTitle("Counter");
        //gridLabel.setVerticalAxisTitle("Score");
        //graph.getViewport().setScalable(true);
        //graph.getViewport().setMaxX(mScores.size());
        //graph.getViewport().setXAxisBoundsManual(true);

        // Add route to map view
        mHandler.postDelayed(runnable, 1000);

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
