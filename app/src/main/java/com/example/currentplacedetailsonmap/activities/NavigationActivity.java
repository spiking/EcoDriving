package com.example.currentplacedetailsonmap.activities;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.currentplacedetailsonmap.R;
import com.example.currentplacedetailsonmap.models.Session;
import com.example.currentplacedetailsonmap.services.DataService;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class NavigationActivity extends AppCompatActivity implements SensorEventListener {

    // Side menu and toolbar customization.
    private Toolbar mToolbar;

    // Sensors
    private SensorManager mSensorManager;
    private Sensor mLinearAccelerometer;
    private float[] mLinearAccelerationValues;
    private Timer mTimer;
    private float mAccelerationValue;

    //TextView
    private TextView mAccelerationValueTextView;
    private TextView mAccelerationFeedbackTextView;
    private int mCounter = 0;

    // Values
    private int mBadCount;
    private int mOkCount;
    private int mGoodCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLinearAccelerationValues = new float[3];
        mAccelerationValue = 0;

        initializeSensors();

        mAccelerationValueTextView = (TextView) findViewById(R.id.navigation_acceleration_value);
        mAccelerationFeedbackTextView = (TextView) findViewById(R.id.navigation_feedback);

        mTimer = new Timer();
        startUITimer();

        resetCounts();

        mBadCount = 0;
        mOkCount = 0;
        mGoodCount = 0;

    }

    public void resetCounts() {
        mBadCount = mOkCount = mGoodCount = 0;
    }

    public void startUITimer() {
        mTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                updateUI();
            }
        }, 0, 500);
    }

    // This method is called directly by the timer, thus it runs on a sub thread
    // Call The runOnUIThread via runnable to be able to update UI elements

    public void updateUI() {
        this.runOnUiThread(RunnableUpdateUI);
    }

    public Runnable RunnableUpdateUI = new Runnable() {
        @Override
        public void run() {

            Log.v("Counter", "RUNNING! " + mCounter++);

            if (mAccelerationValue > 2.5) {
                mAccelerationValueTextView.setTextColor(Color.WHITE);
                mAccelerationFeedbackTextView.setTextColor(Color.WHITE);
                findViewById(R.id.navigation_layout).setBackgroundColor(Color.RED);
                mAccelerationFeedbackTextView.setText("BAD!");
                mAccelerationValueTextView.setText(Float.toString(mAccelerationValue));
                mBadCount++;
            } else if (mAccelerationValue > 1.5) {
                mAccelerationValueTextView.setTextColor(Color.DKGRAY);
                mAccelerationFeedbackTextView.setTextColor(Color.DKGRAY);
                findViewById(R.id.navigation_layout).setBackgroundColor(Color.LTGRAY);
                mAccelerationFeedbackTextView.setText("OKEY!");
                mAccelerationValueTextView.setText(Float.toString(mAccelerationValue));
                mOkCount++;
            } else {
                mAccelerationValueTextView.setTextColor(Color.DKGRAY);
                mAccelerationFeedbackTextView.setTextColor(Color.DKGRAY);
                findViewById(R.id.navigation_layout).setBackgroundColor(Color.GREEN);
                mAccelerationFeedbackTextView.setText("GREAT!");
                mAccelerationValueTextView.setText(Float.toString(mAccelerationValue));
                mGoodCount++;
            }
        }
    };

    public void initializeSensors() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mLinearAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorManager.registerListener(this, mLinearAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stopSession(View view) {
        Log.v("STOP", "Stop session!");
        saveSession();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mLinearAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this); // Stop receiving updates
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // Basically a HighPass-filter
        // Acceleration = gravity + linear acceleration <=> linear acceleration = acceleration - gravity

        if (event.sensor == mLinearAccelerometer) {
            mLinearAccelerationValues[0] = event.values[0]; // x-value
            mLinearAccelerationValues[1] = event.values[1]; // y-value
            mLinearAccelerationValues[2] = event.values[2]; // x-value

            mAccelerationValue = Math.abs(event.values[0] + event.values[1] + event.values[2]);

            Log.v("Acceleration total: ", Float.toString(mAccelerationValue));

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Chill
    }


    public void saveSession() {

        Location startLocation = new Location("VBG");
        startLocation.setLatitude(58.36014);
        startLocation.setLongitude(12.344412);

        Location endLocation = new Location("THN");
        endLocation.setLatitude(58.283489);
        endLocation.setLongitude(12.285821);

        float distanceInMeters = endLocation.distanceTo(startLocation);
        int mTotalCount = mBadCount + mOkCount + mGoodCount;

        Calendar calender = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String stringDate = sdf.format(calender.getTime());

        Session session = new Session(0, 58.36014, 12.344412, 58.283489, 12.285821, distanceInMeters, mTotalCount, mBadCount, mOkCount, mGoodCount, stringDate);

        try {
            // Save current session to sessions
            DataService.getInstance().writeSessionToSessions(session);
            // Save all sesions to internal storage
            DataService.getInstance().writeSessionsToDatabase("DATABASE");
        } catch (IOException e) {
            e.printStackTrace();
        }

        resetCounts();
        mTimer.cancel();
    }
}
