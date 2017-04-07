package com.example.currentplacedetailsonmap.activities;

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

import com.example.currentplacedetailsonmap.R;
import com.example.currentplacedetailsonmap.models.Session;
import com.example.currentplacedetailsonmap.services.DataService;

import java.io.IOException;

import static android.provider.Contacts.SettingsColumns.KEY;

public class NavigationActivity extends AppCompatActivity implements SensorEventListener {

    // Side menu and toolbar customization.
    private Toolbar mToolbar;

    // Sensors
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private float[] mAccelerometerValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAccelerometerValues = new float[3];
        initializeSensors();

        saveSession();
    }

    public void initializeSensors() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void stopSession(View view) {
        Log.v("STOP", "Stop session!");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("Setup!", "Setup!");
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI); // Initialize accelerometer listener
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this); // Stop receiving updates
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor == mAccelerometer) {

            mAccelerometerValues[0] = event.values[0]; // Acceleration minus Gx on the x-axis
            mAccelerometerValues[1] = event.values[1]; // Acceleration minus Gy on the y-axis
            mAccelerometerValues[2] = event.values[2]; // Acceleration minus Gz on the z-axis

/*            Log.v("Data", Float.toString(mAccelerometerValues[0]));
            Log.v("Data", Float.toString(mAccelerometerValues[1]));
            Log.v("Data", Float.toString(mAccelerometerValues[2]));*/
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Chill
    }

    public void saveSession() {

        Log.v("DATA", "Save data to internal storage");

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
/*            DataService.getInstance().writeObject(this, KEY, session);*/

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
