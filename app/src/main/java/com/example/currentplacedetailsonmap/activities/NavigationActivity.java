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

import static android.provider.Contacts.SettingsColumns.KEY;
import static java.lang.Math.abs;

public class NavigationActivity extends AppCompatActivity implements SensorEventListener {

    // Side menu and toolbar customization.
    private Toolbar mToolbar;

    // Sensors
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private float[] mAccelerometerValues;

    //TextView
    private TextView acceleration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAccelerometerValues = new float[3];
        initializeSensors();

        //creates the textview which will display the acceleration on the screen
        acceleration=(TextView)findViewById(R.id.acceleration);

        saveSession();
    }

    public void initializeSensors() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this,mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
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

            //Simplified total acceleration
            float totalAcc = event.values[0]+event.values[1]+event.values[2];



            if(abs(totalAcc)>15){
                findViewById(R.id.navigation_layout).setBackgroundColor(Color.RED);
                acceleration.setText("BAD! \n Acceleration: "+ Float.toString(totalAcc));
            }else{
                findViewById(R.id.navigation_layout).setBackgroundColor(Color.GREEN);
                acceleration.setText("GOOD! \n Acceleration: "+ Float.toString(totalAcc));
            }

            //acceleration.setText("X: "+event.values[0]+"\nY: "+event.values[1]+"\nZ: "+event.values[2]);

            /*          Log.v("Data", Float.toString(mAccelerometerValues[0]));
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
