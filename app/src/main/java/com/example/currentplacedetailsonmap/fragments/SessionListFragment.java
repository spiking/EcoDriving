package com.example.currentplacedetailsonmap.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.currentplacedetailsonmap.R;
import com.example.currentplacedetailsonmap.adapters.SessionsAdapter;
import com.example.currentplacedetailsonmap.services.DataService;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SessionListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SessionListFragment extends Fragment {

    public SessionListFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static SessionListFragment newInstance() {
        SessionListFragment fragment = new SessionListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_session_list, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_sessions);
        recyclerView.setHasFixedSize(true);

        ArrayList sessions = DataService.getInstance().getAllSessions();
        Collections.reverse(sessions);

        SessionsAdapter adapter = new SessionsAdapter(sessions);
        recyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(layoutManager);

        return  view;
    }

}
