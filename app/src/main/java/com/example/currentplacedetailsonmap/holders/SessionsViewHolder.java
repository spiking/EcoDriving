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
        sessionScore = (TextView) itemView.findViewById(R.id.session_score);
        sessionDate = (TextView) itemView.findViewById(R.id.session_date);

    }

    public void updateUI(Session session) {
        sessionScore.setText(Integer.toString(session.getCurrentScore()) + " poäng");
        sessionDate.setText(session.getDate());
    }
}

