package com.example.currentplacedetailsonmap.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.currentplacedetailsonmap.R;
import com.example.currentplacedetailsonmap.models.Session;

/**
 * Created by Atlas on 2017-04-25.
 */

public class SessionsViewHolder extends RecyclerView.ViewHolder {

    private TextView sessionDate;
    private TextView sessionScore;

    public SessionsViewHolder(View itemView) {
        super(itemView);

        sessionDate = (TextView) itemView.findViewById(R.id.session_date);
        sessionScore = (TextView) itemView.findViewById(R.id.session_score);

    }

    public void updateUI(Session session) {
        sessionDate.setText(session.getDate());
        sessionScore.setText(Integer.toString(session.getTotalPoints()));
    }
}

