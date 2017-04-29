package com.example.currentplacedetailsonmap.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.currentplacedetailsonmap.R;

public class DetailedStatsActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_stats);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras =  getIntent().getExtras();

        if (extras != null) {
            String date = extras.getString("date");
            int score = extras.getInt("score");
            System.out.println(date);
            System.out.println(score);
        }


    }

}
