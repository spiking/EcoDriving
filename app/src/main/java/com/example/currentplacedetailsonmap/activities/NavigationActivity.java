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

        initializeSensors();
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
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI); // Initialize accelerometer listener
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this); // Stop receiving updates
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void saveData() {

        Log.v("DATA", "Save data to internal storage");

        Location startLocation = new Location("");
        startLocation.setLatitude(0.0d);
        startLocation.setLongitude(0.0d);

        Location finishLocation = new Location("");
        finishLocation.setLatitude(0.0d);
        finishLocation.setLongitude(0.0d);

        float distanceInMeters = finishLocation.distanceTo(startLocation);

        Session session = new Session("123", 0.0f, 0.0f, 0.0f, 0.0f, distanceInMeters, 100);

        try {

            // Save the list of entries to internal storage
            DataService.getInstance().writeObject(this, KEY, session);

            // Retrieve the list from internal storage
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
