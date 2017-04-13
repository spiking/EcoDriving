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
import java.util.Timer;
import java.util.TimerTask;

import static android.provider.Contacts.SettingsColumns.KEY;

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
            } else if (mAccelerationValue > 1.5) {
                mAccelerationValueTextView.setTextColor(Color.DKGRAY);
                mAccelerationFeedbackTextView.setTextColor(Color.DKGRAY);
                findViewById(R.id.navigation_layout).setBackgroundColor(Color.LTGRAY);
                mAccelerationFeedbackTextView.setText("OKEY!");
                mAccelerationValueTextView.setText(Float.toString(mAccelerationValue));
            } else {
                mAccelerationValueTextView.setTextColor(Color.DKGRAY);
                mAccelerationFeedbackTextView.setTextColor(Color.DKGRAY);
                findViewById(R.id.navigation_layout).setBackgroundColor(Color.GREEN);
                mAccelerationFeedbackTextView.setText("GREAT!");
                mAccelerationValueTextView.setText(Float.toString(mAccelerationValue));
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

            Log.v("Linear Acceleration X: ", Float.toString(mLinearAccelerationValues[0]));
            Log.v("Linear Acceleration Y: ", Float.toString(mLinearAccelerationValues[1]));
            Log.v("Linear Acceleration Z: ", Float.toString(mLinearAccelerationValues[2])); // Only using Z-value atm

            mAccelerationValue = Math.abs(event.values[2]);

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Chill
    }

    // Not in use

    public void saveSession() {

        Location startLocation = new Location("VBG");
        startLocation.setLatitude(58.36014);
        startLocation.setLongitude(12.344412);

        Location endLocation = new Location("THN");
        endLocation.setLatitude(58.283489);
        endLocation.setLongitude(12.285821);

        float distanceInMeters = endLocation.distanceTo(startLocation);
        System.out.println(distanceInMeters);

        Session session = new Session(1, 58.36014, 12.344412, 58.283489, 12.285821, distanceInMeters, 10);

        try {

            // Save data to internal storage
            DataService.getInstance().writeObject(this, KEY, session);

            // Retrieve data from internal storage
            Session savedSession = (Session) DataService.getInstance().readObject(this, KEY);

            // Print data
            Log.v("DATA", savedSession.toString());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
