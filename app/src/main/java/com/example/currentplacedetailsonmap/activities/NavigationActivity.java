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
    private float mAccelerationValue;

    //TextView
    private TextView mAccelerationValueTextView;
    private TextView mAccelerationFeedbackTextView;
    private static int mCounter = 0;

    //Button
    private Button mStopButton;

    // Values
    private int mBadCount;
    private int mOkCount;
    private int mGoodCount;

    // Speech
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private ShakeDetector shakeDetector;
    private static boolean mMapActivityShowing = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mStopButton = (Button) findViewById(R.id.stop_button);

        mLinearAccelerationValues = new float[3];
        mAccelerationValue = 0;

        initializeSensors();

        mAccelerationValueTextView = (TextView) findViewById(R.id.navigation_acceleration_value);
        mAccelerationFeedbackTextView = (TextView) findViewById(R.id.navigation_feedback);

        mTimer = new Timer();
        startUITimer();

        resetCounts();

        // Setup shaking
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        shakeDetector = new ShakeDetector(this);
        shakeDetector.start(mSensorManager);

    }

    public void resetCounts() {
        mBadCount = mOkCount = mGoodCount = mCounter = 0;
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
                findViewById(R.id.navigation_layout).setBackgroundColor(Color.parseColor("#F44336"));
                mAccelerationFeedbackTextView.setText("BAD!");
                mAccelerationValueTextView.setText(Float.toString(mAccelerationValue));
                mBadCount++;
            } else if (mAccelerationValue > 1.5) {
                mAccelerationValueTextView.setTextColor(Color.DKGRAY);
                mAccelerationFeedbackTextView.setTextColor(Color.DKGRAY);
                findViewById(R.id.navigation_layout).setBackgroundColor(Color.parseColor("#FFEB3B"));
                mAccelerationFeedbackTextView.setText("OKEY!");
                mAccelerationValueTextView.setText(Float.toString(mAccelerationValue));
                mOkCount++;
            } else {
                mAccelerationValueTextView.setTextColor(Color.DKGRAY);
                mAccelerationFeedbackTextView.setTextColor(Color.DKGRAY);
                findViewById(R.id.navigation_layout).setBackgroundColor(Color.parseColor("#4CAF50"));
                mAccelerationFeedbackTextView.setText("GOOD!");
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

            /*Log.v("Acceleration total: ", Float.toString(mAccelerationValue));*/

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Chill
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

        resetCounts();
        mTimer.cancel();
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
                        mStopButton.performClick();
                    } else {
                       Toast.makeText(this, "Invalid command!", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }

        }
    }
}
