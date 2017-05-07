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
    private Switch mSwitch;
    private TextView mSwitchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSwitch = (Switch) findViewById(R.id.map_color_switch);
        mSwitchText = (TextView) findViewById(R.id.map_color_text);

        if (!DataService.getInstance().getMapColor().equals("DARK")) {
            mSwitch.setChecked(false);
        } else {
            mSwitch.setChecked(true);
        }

        mSwitch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                if(mSwitch.isChecked()) {
                    System.out.println("CHECKED");
                    try {
                        DataService.getInstance().setMapColorDark("DARK");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("NOT CHECKED");
                    try {
                        DataService.getInstance().setMapColorDark("LIGHT");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
