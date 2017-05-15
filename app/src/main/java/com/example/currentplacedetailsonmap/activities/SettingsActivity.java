package com.example.currentplacedetailsonmap.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.example.currentplacedetailsonmap.R;
import com.example.currentplacedetailsonmap.services.DataService;

import java.io.IOException;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Switch mMapSwitch;
    private TextView mSwitchText;
    private Switch mDriveModeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMapSwitch = (Switch) findViewById(R.id.map_color_switch);
        mSwitchText = (TextView) findViewById(R.id.map_color_text);

        mDriveModeSwitch = (Switch) findViewById(R.id.drive_mode_switch);

        if (!DataService.getInstance().getMapColor().equals("DARK")) {
            mMapSwitch.setChecked(false);
        } else {
            mMapSwitch.setChecked(true);
        }

        mMapSwitch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                if(mMapSwitch.isChecked()) {
                    try {
                        DataService.getInstance().setMapColorDark("DARK");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        DataService.getInstance().setMapColorDark("LIGHT");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        if (!DataService.getInstance().getDriveMode().equals("CAR")) {
            mDriveModeSwitch.setChecked(false);
        } else {
            mDriveModeSwitch.setChecked(true);
        }

        mDriveModeSwitch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                if(mDriveModeSwitch.isChecked()) {
                    try {
                        DataService.getInstance().setDriveMode("CAR");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        DataService.getInstance().setDriveMode("BIKE");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
