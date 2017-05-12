package com.example.currentplacedetailsonmap.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.currentplacedetailsonmap.R;
import com.example.currentplacedetailsonmap.controller.ScoreHandler;
import com.example.currentplacedetailsonmap.fragments.MapFragment;
import com.example.currentplacedetailsonmap.models.LatLngSerializedObject;
import com.example.currentplacedetailsonmap.models.Session;
import com.example.currentplacedetailsonmap.services.DataService;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import edu.cmu.pocketsphinx.Assets;


public class NavigationActivity extends AppCompatActivity implements SensorEventListener {

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

    // Data
    private ScoreHandler mScoreHandler;
    private HashMap<Integer, Integer> mScores;
    private boolean voiceFeedbackIsTimedOut;

    // Running
    private boolean isRunning;
    private Long mStartTimeStamp;

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
        voiceFeedbackIsTimedOut = false;

        mStartTimeStamp = System.currentTimeMillis() / 1000;
        Log.v("ROUTE", Long.toString(mStartTimeStamp));

        FragmentManager manager = getSupportFragmentManager();
        mapFragment = (MapFragment) manager.findFragmentById(R.id.map_fragment);
        mapFragment.startRouteNavigation();

        // Check if user has given permission to record audio
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }

        // Creates an object for voice recognition
        createRecognizer("cancel trip", false);

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

            if (mAccelerationValue > 5) {
                return;
            }

            // Not showing this value
            /* double roundedAccelerationValue = Math.abs(Math.round(mAccelerationValue * 100.0) / 100.0); */

            if (mAccelerationValue > 2) {
                updateFeedbackUI(Color.WHITE, "#F44336", R.string.feedback_bad, (int) (-mAccelerationValue * 10), false);
                mScoreHandler.incrementBadCount();
                mScoreHandler.setCurrentStreak(0);
                mapFragment.addRedScreenMarker();

                if (!voiceFeedbackIsTimedOut) {
                    mMPBad.start();
                    voiceFeedbackTimeout();
                    voiceFeedbackIsTimedOut = true;
                }

            } else if (mAccelerationValue > 1.5) {
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
        }, 30000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mLinearAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        if (!isRunning) {
            resetUI();
            createRecognizer("start new trip", false);
            Log.v("VOICE", "RESUME - START NEW TRIP");
        }

        Log.v("VOICE", "RESUME");
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

        if (voiceRec != null) {
            voiceRec.cancelVoiceDetection();
        }

        isRunning = false;

        Log.v("VOICE", "PAUSE");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // Basically a HighPass-filter
        // Acceleration = gravity + linear acceleration <=> linear acceleration = acceleration - gravity

        if (event.sensor == mLinearAccelerometer) {
            mLinearAccelerationValues[0] = event.values[0]; // x-value
            mLinearAccelerationValues[1] = event.values[1]; // y-value
            mLinearAccelerationValues[2] = event.values[2]; // x-value

            mAccelerationValue = Math.abs((event.values[0] + event.values[1] + event.values[2])); // Negative value for break

            // Log.v("Acceleration total: ", Float.toString(mAccelerationValue));

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

        Calendar calender = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String stringDate = sdf.format(calender.getTime());

        // Fetch route from map fragment
        ArrayList<LatLngSerializedObject> mRoute = mapFragment.getRoute();
        ArrayList<LatLngSerializedObject> mRedScreens = mapFragment.getRedScreenMarkerPoints();
        double distance = mapFragment.getTravelDistance();
        long travelTime = (System.currentTimeMillis() / 1000) - mStartTimeStamp;

        double startLat = 0.0;
        double startLng = 0.0;

        double endLat = 0.0;
        double endLng = 0.0;

        if (!mRoute.isEmpty()) {

            if (mRoute.get(0) != null) {
                startLat = (mRoute.get(0).getLatLng().latitude);
                startLng = (mRoute.get(0).getLatLng().longitude);
            }

            if (mRoute.get(mRoute.size() - 1) != null) {
                endLat = (mRoute.get(mRoute.size() - 1).getLatLng().latitude);
                endLng = (mRoute.get(mRoute.size() - 1).getLatLng().longitude);
            }

        }

        String uniqueID = UUID.randomUUID().toString();

        Session session = new Session(uniqueID, startLat, startLng, endLat, endLng,
                mScoreHandler.getHighScore(), mScoreHandler.getCurrentScore(), mScoreHandler.getHigestStreak(),
                mScoreHandler.getBadCount(), mScoreHandler.getOkCount(), mScoreHandler.getGoodCount(), stringDate,
                mScores, mRoute, mRedScreens, distance, travelTime);

        try {
            // Save current session to sessions
            DataService.getInstance().writeSessionToSessions(session);
            // Save all sesions to internal storage
            DataService.getInstance().writeSessionsToDatabase("DATABASE");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent i = new Intent(getApplicationContext(), DetailedStatsActivity.class);
        i.putExtra("INDEX", DataService.getInstance().getSessionMapSize() - 1);
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
            Log.v("VOICE", "Session btn clicked!");
            voiceRec.cancelVoiceDetection();
            createRecognizer("cancel trip", false);
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
        mStartTimeStamp = System.currentTimeMillis() / 1000;
        mSessionButton.setText(getString(R.string.session_button));
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

    private void createRecognizer(String keyphrase, boolean backToHome) {
        View[] voiceViews = new View[1];
        voiceViews[0] = findViewById(R.id.voice_result_2);
        if (backToHome) {
            Log.v("VOICE", "backToHome true");
            Intent intent = new Intent(getApplicationContext(), NavigationActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            voiceRec = new VoiceRecognition(getApplicationContext(), voiceViews, keyphrase, intent);
        } else {
            Log.v("VOICE", "backToHome false");
            voiceRec = new VoiceRecognition(getApplicationContext(), voiceViews, keyphrase, mSessionButton);
        }
        runRecognizerSetup();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        Log.v("VOICE", "Back button pressed");
        voiceRec.cancelVoiceDetection();
        createRecognizer("start new trip", true);
        super.onBackPressed();
    }
}
