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
import android.widget.Toast;

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
    private Sensor mRotationSensor;
    private float[] mOrientationAngles;
    private Sensor mProximitySensor;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private float mAccelerationValue;
    private float[] mAccelerationArray;
    private int mAccelerationCounter;
    private boolean mProximityBoolean;

    //TextView
    private TextView mCurrentScoreTextView;
    private TextView mAccelerationFeedbackTextView;
    private TextView animationText;
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

    // Mode
    private String mDriveMode;

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
        mOrientationAngles = new float[3];
        mAccelerationValue = 0;
        mAccelerationArray = new float[5];
        mAccelerationCounter = 0;
        mScores = new HashMap<>();
        mScoreHandler = new ScoreHandler();
        mProximityBoolean = false;


        mMPGood = MediaPlayer.create(this, R.raw.well_done);
        mMPBad = MediaPlayer.create(this, R.raw.take_it_easy);

        initializeSensors();

        mCurrentScoreTextView = (TextView) findViewById(R.id.navigation_acceleration_value);
        mAccelerationFeedbackTextView = (TextView) findViewById(R.id.navigation_feedback);
        animationText = (TextView) findViewById(R.id.pointAnimation);

        resetData();
        startSession();
        isRunning = true;
        voiceFeedbackIsTimedOut = false;
        mDriveMode = DataService.getInstance().getDriveMode();

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

            mScores.put(mCounter++, mScoreHandler.getCurrentScore());

            if (mDriveMode.equals("CAR")) {

                if (mAccelerationValue > 1.25) {
                    updateFeedbackUI(Color.WHITE, "#F44336", R.string.feedback_bad, (int) (-mAccelerationValue * 10), false);
                    mScoreHandler.incrementBadCount();
                    mScoreHandler.setCurrentStreak(0);
                    mapFragment.addRedScreenMarker();

                    if (!voiceFeedbackIsTimedOut) {
                        mMPBad.start();
                        voiceFeedbackTimeout();
                        voiceFeedbackIsTimedOut = true;
                    }

                } else if (mAccelerationValue > 1.0) {
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

            if (mDriveMode.equals("BIKE")) {

                if (mAccelerationValue > 1.5) {
                    updateFeedbackUI(Color.WHITE, "#F44336", R.string.feedback_bad, (int) (-mAccelerationValue * 10), false);
                    mScoreHandler.incrementBadCount();
                    mScoreHandler.setCurrentStreak(0);
                    mapFragment.addRedScreenMarker();

                    if (!voiceFeedbackIsTimedOut) {
                        mMPBad.start();
                        voiceFeedbackTimeout();
                        voiceFeedbackIsTimedOut = true;
                    }

                } else if (mAccelerationValue > 1.25) {
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
        /*animateScore(scoreChange, 20, Color.WHITE, false);*/
        mAccelerationFeedbackTextView.setText(getString(feedbackString));
        mCurrentScoreTextView.setText(Double.toString(mScoreHandler.getCurrentScore()));

    }

    /*private void animateScore(int score, float textSize, int textColor, boolean idle){
        if(!idle) {
            int totalScore = mScoreHandler.getCurrentScore();

            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) animationText.getLayoutParams();

            //Flytta animationen beroende på antal poäng
            if(totalScore < 100){
                Log.v("ANIMATION", "margin 10");
                params.leftMargin = 150;
            }
            else if(totalScore >= 100 && totalScore < 1000){
                Log.v("ANIMATION", "margin 100");
                params.leftMargin = 200;
            }
            else if(totalScore >= 1000 && totalScore < 10000){
                Log.v("ANIMATION", "margin 1000");
                params.leftMargin = 150;

            }
            else{
                Log.v("ANIMATION", "margin 10000");
                params.leftMargin = 150;
            }

            animationText.setLayoutParams(params);
            animationText.setTextSize(textSize);
            animationText.setTextColor(textColor);
            animationText.setText("+" + score);

            Animation in = new AlphaAnimation(0.0f, 1.0f);
            in.setDuration(300);

            Animation out = new AlphaAnimation(1.0f, 0.0f);
            out.setDuration(300);

            animationText.startAnimation(in);
            animationText.startAnimation(out);
        }else{
            animationText.setText("");
        }


    }*/

    public void voiceFeedbackTimeout() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                voiceFeedbackIsTimedOut = false; // Reset timeout after 15 sec
            }
        }, 15000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mLinearAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mRotationSensor, SensorManager.SENSOR_DELAY_NORMAL);

        if (DataService.getInstance().getProximityAccess().equals("TRUE")) {
            mSensorManager.registerListener(this, mProximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        if (!isRunning) {
            resetUI();
            createRecognizer("start trip", false);
            Log.v("VOICE", "RESUME - START TRIP");
            mProximityBoolean = true;
        }

        mDriveMode = DataService.getInstance().getDriveMode();

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

        // Acceleration = gravity + linear acceleration <=> linear acceleration = acceleration - gravity

        if (event.sensor == mLinearAccelerometer) {

            if (mAccelerationCounter < 4) {
                mAccelerationCounter++;
            } else {
                mAccelerationCounter = 0;
            }

            mLinearAccelerationValues[0] = event.values[0]; // x-value
            mLinearAccelerationValues[1] = event.values[1]; // y-value
            mLinearAccelerationValues[2] = event.values[2]; // z-value

            // Pitch, angle of rotation about the x axis (plane parallel to the screen and to the ground)

            float deviceAngle = mOrientationAngles[1] * (float) (180 / Math.PI);
            float mX = Math.abs(mLinearAccelerationValues[0]);
            float mY = Math.abs(mLinearAccelerationValues[1]);
            float mZ = Math.abs(mLinearAccelerationValues[2]);

            if (deviceAngle > -45 && deviceAngle < 45) {

                // Vertical focus
                // Filter vertical jump in vertical mode

                if (mY > 3) {
                    mAccelerationArray[mAccelerationCounter] = 0;
                } else {
                    mAccelerationArray[mAccelerationCounter] = mZ;
                }

            } else {

                // Horizontal focus
                // Filter vertical jump in horizontal mode

                if (mZ > 3) {
                    mAccelerationArray[mAccelerationCounter] = 0;
                } else {
                    mAccelerationArray[mAccelerationCounter] = mY;
                }
            }

            mAccelerationValue = getAverageAcceleration();

/*            Log.v("VALUE", " = " + mAccelerationCounter);
            Log.v("VALUE", " = " + mAccelerationValue);
            Log.v("VALUE X", "X = " + event.values[0]);
            Log.v("VALUE Y", "Y = " + event.values[1]);
            Log.v("VALUE Z", "Z = " + event.values[2]);*/
        }

        if (event.sensor == mRotationSensor) {

            if (event.values.length > 4) {
                float[] truncatedRotationVector = new float[4];
                System.arraycopy(event.values, 0, truncatedRotationVector, 0, 4);
                update(truncatedRotationVector);
            } else {
                update(event.values);
            }
        }

        if (event.sensor == mProximitySensor) {
            if (event.values[0] == 0) {
                if (mProximityBoolean) {
                    Log.v("PROXIMITY", "CLOSE");
                    mSessionButton.performClick();
                    mProximityBoolean = false;
                } else {
                    mProximityBoolean = true;
                }

            } else {
                Log.v("PROXIMITY", "FAR");
            }
        }
    }

    private void update(float[] vectors) {
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, vectors);
        int worldAxisX = SensorManager.AXIS_X;
        int worldAxisZ = SensorManager.AXIS_Z;
        float[] adjustedRotationMatrix = new float[9];
        SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisX, worldAxisZ, adjustedRotationMatrix);
        mOrientationAngles = new float[3];
        SensorManager.getOrientation(adjustedRotationMatrix, mOrientationAngles);
    }

    private float getAverageAcceleration() {
        float sum = 0;
        for (int i = 0; i < mAccelerationArray.length; i++) {
            sum += mAccelerationArray[i];
        }
        return sum / mAccelerationArray.length;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Chill
    }

    public void initializeSensors() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mLinearAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        if (DataService.getInstance().getProximityAccess().equals("TRUE")) {
            mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }

        mSensorManager.registerListener(this, mLinearAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mRotationSensor, SensorManager.SENSOR_DELAY_NORMAL);

        if (DataService.getInstance().getProximityAccess().equals("TRUE")) {
            mSensorManager.registerListener(this, mProximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
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

        int x = DataService.getInstance().getSessionMapSize() - 1;

        Log.v("INDEX", "INDEX = " + x);

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
            mapFragment.resetRedScreenMarkers();
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
        mAccelerationFeedbackTextView.setTextSize(50);
        mCurrentScoreTextView.setText("0.0");
        mCurrentScoreTextView.setTextSize(50);
        mSessionButton.setText(getString(R.string.start_button));
    }

    public void startSession() {
        resetData();
        startUITimer();
        mAccelerationFeedbackTextView.setTextSize(65);
        mCurrentScoreTextView.setTextSize(65);
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
                    Toast.makeText(getApplicationContext(), "Could not init voice recognition", Toast.LENGTH_SHORT);
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
        createRecognizer("start trip", true);
        super.onBackPressed();
    }
}
