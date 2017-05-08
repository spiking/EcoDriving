package com.example.currentplacedetailsonmap.activities;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.currentplacedetailsonmap.R;
import com.example.currentplacedetailsonmap.controller.ScoreHandler;
import com.example.currentplacedetailsonmap.fragments.MapFragment;
import com.example.currentplacedetailsonmap.models.LatLngSerializedObject;
import com.example.currentplacedetailsonmap.models.Session;
import com.example.currentplacedetailsonmap.services.DataService;
import com.squareup.seismic.ShakeDetector;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import edu.cmu.pocketsphinx.Assets;


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
    private TextView mCurrentScoreTextView;
    private TextView mAccelerationFeedbackTextView;
    private static int mCounter = 0;

    //Button
    private Button mSessionButton;

    // Values
    private ScoreHandler mScoreHandler;

    private HashMap<Integer, Integer> mScores;
    private boolean voiceFeedbackIsTimedOut;

    // Speech
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private ShakeDetector shakeDetector;

    // Running
    private boolean isRunning;
    private boolean voiceInput;

    // Media player
    private MediaPlayer mMPGood;
    private MediaPlayer mMPBad;

    // Fragment
    private MapFragment mapFragment;

    //Voice
    private VoiceRecognition voiceRec;

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

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
        mScores = new HashMap<>();
        mScoreHandler = new ScoreHandler();

        mMPGood = MediaPlayer.create(this, R.raw.well_done);
        mMPBad = MediaPlayer.create(this, R.raw.take_it_easy);

        initializeSensors();

        mCurrentScoreTextView = (TextView) findViewById(R.id.navigation_acceleration_value);
        mAccelerationFeedbackTextView = (TextView) findViewById(R.id.navigation_feedback);

        resetData();
        startSession();
        isRunning = true;
        voiceInput = false;
        voiceFeedbackIsTimedOut = false;

        // Setup shaking
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        shakeDetector = new ShakeDetector(this);
        shakeDetector.start(mSensorManager);

        FragmentManager manager = getSupportFragmentManager();
        mapFragment = (MapFragment) manager.findFragmentById(R.id.map_fragment);
        mapFragment.startRouteNavigation();

        // Check if user has given permission to record audio
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }

        View[] voiceViews = new View[1];
        voiceViews[0] = findViewById(R.id.voice_result_2);
        voiceRec = new VoiceRecognition(getApplicationContext(), voiceViews, "stop", mSessionButton);
        runRecognizerSetup();
    }

    public void startUITimer() {
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                updateUIContinuesly();
            }
        };
        mTimer.scheduleAtFixedRate(mTimerTask, 500, 500);
    }

    // This method is called directly by the timer, thus it runs on a sub thread
    // Call The runOnUIThread via runnable to be able to update UI elements

    public void updateUIContinuesly() {
        this.runOnUiThread(RunnableUpdateUI);
    }

    public Runnable RunnableUpdateUI = new Runnable() {
        @Override
        public void run() {

            // Log.v("Counter", "RUNNING! " + mCounter++);

            mScores.put(mCounter++, mScoreHandler.getCurrentScore());
            System.out.println("COUNTER = " + mCounter + " VALUE = " + mScoreHandler.getCurrentScore());

            if (mAccelerationValue > 10) {
                return;
            }

            // Not showing this value
            /* double roundedAccelerationValue = Math.abs(Math.round(mAccelerationValue * 100.0) / 100.0); */

            if (mAccelerationValue > 4) {
                updateFeedbackUI(Color.WHITE, "#F44336", R.string.feedback_bad, (int) (-mAccelerationValue * 10), false);
                mScoreHandler.incrementBadCount();
                mScoreHandler.setCurrentStreak(0);

                if (!voiceFeedbackIsTimedOut) {
                    mMPBad.start();
                    voiceFeedbackTimeout();
                    voiceFeedbackIsTimedOut = true;
                }

            } else if (mAccelerationValue > 2) {
                updateFeedbackUI(Color.DKGRAY, "#FFEB3B", R.string.feedback_ok, mScoreHandler.getCurrentScore(), true);
                mScoreHandler.incrementOkCount();
                mScoreHandler.setCurrentStreak(0);
            } else {
                updateFeedbackUI(Color.DKGRAY, "#4CAF50", R.string.feedback_good, 10, false);
                mScoreHandler.incrementGoodCount();
                mScoreHandler.incrementCurrentStreak();

                if (mScoreHandler.getCurrentStreak() >= 20 && !voiceFeedbackIsTimedOut) {
                    mMPGood.start();
                    mScoreHandler.setCurrentStreak(0);
                    voiceFeedbackTimeout();
                    voiceFeedbackIsTimedOut = true;
                }
            }
        }
    };

    public void updateFeedbackUI(int textColor, String backgroundColor, int feedbackString, int scoreChange, boolean feedbackOk) {

        // Dont change score at ok screen

        if (!feedbackOk) {
            mScoreHandler.setCurrentScore(mScoreHandler.getCurrentScore() + scoreChange);
        }

        if (!isRunning) {
            return;
        }

        mCurrentScoreTextView.setTextColor(textColor);
        mAccelerationFeedbackTextView.setTextColor(textColor);
        findViewById(R.id.navigation_layout).setBackgroundColor(Color.parseColor(backgroundColor));
        mAccelerationFeedbackTextView.setText(getString(feedbackString));
        mCurrentScoreTextView.setText(Double.toString(mScoreHandler.getCurrentScore()));

    }

    public void voiceFeedbackTimeout() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                voiceFeedbackIsTimedOut = false; // Reset timeout after 10 sec
            }
        }, 10000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mLinearAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        if(!isRunning) {
            resetUI();
        }

        if(isRunning && voiceInput) {
            voiceInput = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this); // Stop receiving updates

        if (isRunning && !voiceInput) {
            mTimer.cancel();
            mTimer.purge();
            mTimerTask.cancel();
        }

        isRunning = false;
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

        Calendar calender = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String stringDate = sdf.format(calender.getTime());

        // Fetch route from map fragment
        ArrayList<LatLngSerializedObject> mRoute = mapFragment.getRoute();

        Session session = new Session(0, 58.36014, 12.344412, 58.283489, 12.285821, distanceInMeters, mScoreHandler.getHighScore(), mScoreHandler.getCurrentScore(), mScoreHandler.getHigestStreak(), mScoreHandler.getBadCount(), mScoreHandler.getOkCount(), mScoreHandler.getGoodCount(), stringDate, mScores, mRoute);

        try {
            // Save current session to sessions
            DataService.getInstance().writeSessionToSessions(session);
            // Save all sesions to internal storage
            DataService.getInstance().writeSessionsToDatabase("DATABASE");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent i = new Intent(getApplicationContext(), DetailedStatsActivity.class);
        i.putExtra("DATE", session.getDate());
        i.putExtra("SCORE", session.getCurrentScore());
        i.putExtra("ALL_SCORES", mScores);
        i.putExtra("ROUTE", mRoute);
        //TIME
        i.putExtra("TIME", session.getTravelTime());
        startActivity(i);

        mTimer.cancel();
        mTimer.purge();
        mTimerTask.cancel();
        resetData();
    }

    public void resetData() {
        mCounter = 0;
        mScoreHandler.resetScores();
        mScores = new HashMap<>();
    }


    public void sessionButtonClicked(View view) {
        Log.v("SESSION CHANGED", "Session btn clicked!");

        if (isRunning) {
            saveSession();
            isRunning = false;
        } else {
            startSession();
            mapFragment.startRouteNavigation();
            isRunning = true;
        }
    }

    public void resetUI() {
        mCurrentScoreTextView.setTextColor(Color.DKGRAY);
        mAccelerationFeedbackTextView.setTextColor(Color.DKGRAY);
        findViewById(R.id.navigation_layout).setBackgroundColor(Color.parseColor("#4CAF50"));
        mAccelerationFeedbackTextView.setText(getString(R.string.navigation_feedback_start));
        mCurrentScoreTextView.setText("0.0");
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
            voiceInput = true;
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

    /**** Voice Recognition ****/

    private void runRecognizerSetup() {
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(NavigationActivity.this);
                    File assetDir = assets.syncAssets();
                    voiceRec.setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    ((TextView) findViewById(R.id.voice_result))
                            .setText("Failed to init recognizer " + result);
                } else {
                    voiceRec.switchSearch("wakeup"); //Speaking to wake up the recognizer
                }
            }
        }.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                runRecognizerSetup();
            } else {
                voiceRec.cancelVoiceDetection();
            }
        }
    }
}
