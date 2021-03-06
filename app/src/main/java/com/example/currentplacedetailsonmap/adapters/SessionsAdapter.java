package com.example.currentplacedetailsonmap.adapters;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.currentplacedetailsonmap.R;
import com.example.currentplacedetailsonmap.activities.DetailedStatsActivity;
import com.example.currentplacedetailsonmap.holders.SessionsViewHolder;
import com.example.currentplacedetailsonmap.models.Session;
import com.example.currentplacedetailsonmap.services.DataService;

import java.util.ArrayList;

/**
 * Created by Atlas on 2017-04-25.
 */

public class SessionsAdapter extends RecyclerView.Adapter<SessionsViewHolder> {

    private ArrayList<Session> sessions;

    public SessionsAdapter(ArrayList<Session> sessions) {
        this.sessions = sessions;
    }

    @Override
    public void onBindViewHolder(SessionsViewHolder holder, final int position) {
        final Session session = sessions.get(position);
        holder.updateUI(session);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext().getApplicationContext(), DetailedStatsActivity.class);
                int index = DataService.getInstance().getSessionMapSize() - 1 - position; // Reversed since ordered on date
                i.putExtra("INDEX", index);
                v.getContext().startActivity(i);
            }
        });
    }


    @Override
    public int getItemCount() {
        return sessions.size();
    }

    @Override
    public SessionsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View card = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_session, parent, false);
        return new SessionsViewHolder(card);
    }
}
