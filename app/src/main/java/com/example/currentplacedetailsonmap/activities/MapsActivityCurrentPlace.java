package com.example.currentplacedetailsonmap.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.currentplacedetailsonmap.R;
import com.example.currentplacedetailsonmap.fragments.MapFragment;
import com.example.currentplacedetailsonmap.services.DataService;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;

/**
 * An activity that displays a map showing the place at the device's current location.
 */

public class MapsActivityCurrentPlace extends AppCompatActivity implements SensorEventListener {

    // Side menu and toolbar customization.
    private Toolbar mToolbar;
    private Drawer mDrawer;

    // Button
    private Button mStartButton;

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_ALL = 1;
    private VoiceRecognition voiceRec;

    // Sensors
    private SensorManager mSensorManager;
    private Sensor mSensor;

    // Map
    private MapFragment mMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);

        // Attaching the layout to the toolbar object
        // Setting toolbar as the ActionBar with setSupportActionBar() call
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mStartButton = (Button) findViewById(R.id.start_button);

        // Setup side menu
        setupNavigationMenu();

        // mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        // Load cached data into temporary storage

        try {
            DataService.getInstance().readSessionsFromDatabase("DATABASE");
            DataService.getInstance().readMapColorTypeFromDatabase("MAP_COLOR");
            DataService.getInstance().readDriveModeFromDatabase("DRIVE_MODE");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO};

        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_REQUEST_ALL);
        }

        FragmentManager manager = getSupportFragmentManager();
        mMapFragment = (MapFragment) manager.findFragmentById(R.id.map_fragment);

        View[] voiceViews = new View[1];
        voiceViews[0] = findViewById(R.id.voice_result);
        Intent intent = new Intent(getApplicationContext(), NavigationActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        voiceRec = new VoiceRecognition(getApplicationContext(), voiceViews, "start trip", intent);
        runRecognizerSetup();
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    public void setupNavigationMenu() {
        // Create the AccountHeader
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.city1)
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();

        // Initialize mDrawer
        mDrawer = new DrawerBuilder()
                .withActivity(this)
                .withDisplayBelowStatusBar(false)
                .withTranslucentStatusBar(false)
                .withAccountHeader(headerResult)
                .withDrawerLayout(R.layout.material_drawer_fits_not)
                .addDrawerItems(
                        new PrimaryDrawerItem().withIdentifier(1).withName("Account Name").withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.md_red_700)),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withIdentifier(2).withName("Statistics"),
                        new SecondaryDrawerItem().withIdentifier(3).withName("Tutorial"),
                        new SecondaryDrawerItem().withIdentifier(4).withName("Settings")
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        return true;
                    }
                })
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        int id = (int) drawerItem.getIdentifier();
                        switch (id) {
                            case 1:
                                Log.v("ID", id + " was chosen");
                                break;
                            case 2:
                                Log.v("ID", id + " was chosen");
                                loadStatsView();
                                break;
                            case 3:
                                Log.v("ID", id + " was chosen");
                                loadSlidesView();
                                break;
                            case 4:
                                Log.v("ID", id + " was chosen");
                                loadSettingsView();
                                break;
                            default:
                                break;
                        }

                        mDrawer.closeDrawer();
                        return true;
                    }
                })
                .build();

        mDrawer.addStickyFooterItem(new PrimaryDrawerItem().withName("Eco Driving Inc."));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    public void loadStatsView() {
        Intent intent = new Intent(this, StatsActivity.class);
        startActivity(intent);
    }

    public void loadSlidesView() {
        Intent intent = new Intent(this, WelcomeSlidesActivity.class);
        intent.putExtra("SHOW_ONCE_MORE", "Show");

        // Verify that the intent will resolve to an activity
        if (intent.resolveActivity(getPackageManager()) != null) {
            Log.v("LOAD", "Welcome slides activity was started");
            startActivity(intent);
        }
    }

    public void loadSettingsView() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void startSession(View view) {
        Log.v("SESSION", "Start button was clicked");

        Intent intent = new Intent(this, NavigationActivity.class);
        intent.putExtra("INITIAL", "YES");
        startActivity(intent);

        voiceRec.cancelVoiceDetection();
    }

    @Override
    public void onResume() {
        super.onResume();
        // mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        // mSensorManager.unregisterListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0] == 0) {
            mStartButton.performClick();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer();
                return true;
            case R.id.option_get_place:
                /* showCurrentPlace(); */
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**** Voice Recognition ****/

    private void runRecognizerSetup() {
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task

        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(MapsActivityCurrentPlace.this);
                    File assetDir = assets.syncAssets();
                    if (voiceRec != null) {
                        voiceRec.setupRecognizer(assetDir);
                    }
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
                    if (voiceRec != null) {
                        voiceRec.switchSearch("wakeup");
                    }
                }
            }
        }.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                runRecognizerSetup();
                mMapFragment.setPermissionGranted();
                mMapFragment.updateLocationUI();
                mMapFragment.getDeviceLocation();
            } else {
                if (voiceRec != null) {
                    voiceRec.cancelVoiceDetection();
                }
            }
        }
    }
}
