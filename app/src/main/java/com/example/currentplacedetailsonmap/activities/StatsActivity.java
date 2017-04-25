package com.example.currentplacedetailsonmap.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.currentplacedetailsonmap.R;
import com.example.currentplacedetailsonmap.fragments.MainListFragment;
import com.example.currentplacedetailsonmap.fragments.SessionListFragment;

public class StatsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private SessionListFragment mListFragment;
    private MainListFragment mMainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMainFragment = (MainListFragment)getSupportFragmentManager().findFragmentById(R.id.container_main);

        if (mMainFragment == null) {
            mMainFragment = MainListFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container_main, mMainFragment)
                    .commit();
        }

    }

}
