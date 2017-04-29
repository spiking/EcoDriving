package com.example.currentplacedetailsonmap.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.currentplacedetailsonmap.R;
import com.example.currentplacedetailsonmap.models.Session;
import com.example.currentplacedetailsonmap.services.DataService;
import com.squareup.seismic.ShakeDetector;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class NavigationActivity extends AppCompatActivity implements SensorEventListener, ShakeDetector.Listener {

    // Side menu and toolbar customization.
    private Toolbar mToolbar;

    // Sensors
    private SensorManager mSensorManager;
    private Sensor mLinearAccelerometer;
    private float[] mLinearAccelerationValues;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private float mAccelerationValue;

    //TextView
    private TextView mAccelerationValueTextView;
    private TextView mAccelerationFeedbackTextView;
    private static int mCounter = 0;

    //Button
    private Button mSessionButton;

    // Values
    private int mBadCount;
    private int mOkCount;
    private int mGoodCount;

    // Speech
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private ShakeDetector shakeDetector;

    // Running
    private boolean isRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSessionButton = (Button) findViewById(R.id.session_button);

        mLinearAccelerationValues = new float[3];
        mAccelerationValue = 0;

        initializeSensors();

        mAccelerationValueTextView = (TextView) findViewById(R.id.navigation_acceleration_value);
        mAccelerationFeedbackTextView = (TextView) findViewById(R.id.navigation_feedback);

        resetData();
        startSession();
        isRunning = true;

        // Setup shaking
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        shakeDetector = new ShakeDetector(this);
        shakeDetector.start(mSensorManager);

    }

    public void startUITimer() {
        mTimer = new Timer();

        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                updateUI();
            }
        };

        mTimer.scheduleAtFixedRate(mTimerTask, 500, 500);
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
            isRunning = true;

            if (mAccelerationValue > 2.5) {
                mAccelerationValueTextView.setTextColor(Color.WHITE);
                mAccelerationFeedbackTextView.setTextColor(Color.WHITE);
                findViewById(R.id.navigation_layout).setBackgroundColor(Color.parseColor("#F44336"));
                mAccelerationFeedbackTextView.setText(getString(R.string.feedback_bad));
                mAccelerationValueTextView.setText(Float.toString(mAccelerationValue));
                mBadCount++;
            } else if (mAccelerationValue > 1.5) {
                mAccelerationValueTextView.setTextColor(Color.DKGRAY);
                mAccelerationFeedbackTextView.setTextColor(Color.DKGRAY);
                findViewById(R.id.navigation_layout).setBackgroundColor(Color.parseColor("#FFEB3B"));
                mAccelerationFeedbackTextView.setText(getString(R.string.feedback_ok));
                mAccelerationValueTextView.setText(Float.toString(mAccelerationValue));
                mOkCount++;
            } else {
                mAccelerationValueTextView.setTextColor(Color.DKGRAY);
                mAccelerationFeedbackTextView.setTextColor(Color.DKGRAY);
                findViewById(R.id.navigation_layout).setBackgroundColor(Color.parseColor("#4CAF50"));
                mAccelerationFeedbackTextView.setText(getString(R.string.feedback_good));
                mAccelerationValueTextView.setText(Float.toString(mAccelerationValue));
                mGoodCount++;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mLinearAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        if(!isRunning) {
            resetUI();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this); // Stop receiving updates
        if (isRunning) {
            mTimer.cancel();
            mTimer.purge();
            mTimerTask.cancel();
        }
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

            /*Log.v("Acceleration total: ", Float.toString(mAccelerationValue));*/

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Chill
    }

    public void initializeSensors() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mLinearAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorManager.registerListener(this, mLinearAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void saveSession() {

        // Mostly random data atm

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

        mTimer.cancel();
        mTimer.purge();
        mTimerTask.cancel();
        resetData();

        Intent intent = new Intent(this, DetailedStatsActivity.class);
        startActivity(intent);
    }

    public void resetData() {
        mBadCount = mOkCount = mGoodCount = mCounter = 0;
    }


    public void sessionButtonClicked(View view) {
        Log.v("SESSION CHANGED", "Session btn clicked!");

        if (isRunning) {
            saveSession();
            isRunning = false;
        } else {
            System.out.println("START SESSION!");
            startSession();
            isRunning = true;
        }
    }

    public void resetUI() {
        mAccelerationValueTextView.setTextColor(Color.DKGRAY);
        mAccelerationFeedbackTextView.setTextColor(Color.DKGRAY);
        findViewById(R.id.navigation_layout).setBackgroundColor(Color.parseColor("#4CAF50"));
        mAccelerationFeedbackTextView.setText(getString(R.string.navigation_feedback_start));
        mAccelerationValueTextView.setText("0.0");
        mSessionButton.setText(getString(R.string.start_button));
    }

    public void startSession() {
        resetData();
        startUITimer();
        mSessionButton.setText(getString(R.string.session_button));
    }

    /**
     * Shake phone to get speech input
     */
    public void hearShake() {
        promptSpeechInput();
    }

    /**
     * Showing google speech input dialog, shake to show
     */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input, say "stop" stop navigation
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    System.out.println(result.get(0).toString());

                    if (result.get(0).toString().equalsIgnoreCase("stopp")) {
                        mSessionButton.performClick();
                    } else {
                        Toast.makeText(this, "Invalid command!", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }

        }
    }
}
