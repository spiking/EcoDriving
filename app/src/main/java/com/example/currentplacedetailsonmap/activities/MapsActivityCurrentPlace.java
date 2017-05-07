package com.example.currentplacedetailsonmap.activities;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.currentplacedetailsonmap.R;
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
import com.squareup.seismic.ShakeDetector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import edu.cmu.pocketsphinx.Assets;

/**
 * An activity that displays a map showing the place at the device's current location.
 */

public class MapsActivityCurrentPlace extends AppCompatActivity
        implements ShakeDetector.Listener {

    // Side menu and toolbar customization.
    private Toolbar mToolbar;
    private Drawer mDrawer;

    // Speech
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private SensorManager mSensorManager;
    private ShakeDetector shakeDetector;
    private static boolean mMapActivityShowing = true;

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private VoiceRecognition voiceRec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);

        // Attaching the layout to the toolbar object
        // Setting toolbar as the ActionBar with setSupportActionBar() call
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        // Setup side menu
        setupNavigationMenu();

        // Setup shaking
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        shakeDetector = new ShakeDetector(this);
        shakeDetector.start(mSensorManager);

        // Load cached data into temporary storage

        try {
            DataService.getInstance().readSessionsFromDatabase("DATABASE");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        // Check if user has given permission to record audio
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }

        View[] voiceViews = new View[1];
        voiceViews[0] = findViewById(R.id.voice_result);
        Intent intent = new Intent(getApplicationContext(), NavigationActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        voiceRec = new VoiceRecognition(getApplicationContext(), voiceViews, "start new trip", intent);
        runRecognizerSetup();
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

        if (!mMapActivityShowing) {
            return;
        }

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input, say "start" to begin navigation
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (result.get(0).toString().equalsIgnoreCase("start")) {
                        Intent intent = new Intent(this, NavigationActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Invalid command!", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }

        }
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
                        new SecondaryDrawerItem().withIdentifier(4).withName("Settings"),
                        new SecondaryDrawerItem().withIdentifier(5).withName("Voice")
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
                            case 5:
                                Log.v("ID", id + " was chosen");
                                loadVoiceView();
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

    public void loadVoiceView() {
        Intent intent = new Intent(this, VoiceRecognitionActivity.class);
        startActivity(intent);
    }

    public void startSession(View view) {
        Log.v("SESSION", "Start button was clicked");

        Intent intent = new Intent(this, NavigationActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapActivityShowing = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapActivityShowing = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapActivityShowing = false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer();
                return true;
            case R.id.option_get_place:
                /*showCurrentPlace();*/
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
