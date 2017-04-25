package com.example.currentplacedetailsonmap.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.currentplacedetailsonmap.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainListFragment extends Fragment {

    private SessionListFragment mListFragment;
    private Toolbar mToolbar;

    public MainListFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static MainListFragment newInstance() {
        MainListFragment fragment = new MainListFragment();
        return fragment;
    }
;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_main_list, container, false);
        mListFragment = (SessionListFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.container_session_list);

        if(mListFragment == null) {
            mListFragment = SessionListFragment.newInstance();
            getActivity().getSupportFragmentManager().beginTransaction().add(R.id.container_session_list, mListFragment).commit();
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        activity.setSupportActionBar(mToolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        return view;
    }

}
