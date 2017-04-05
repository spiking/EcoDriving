package com.example.currentplacedetailsonmap.models;

import java.io.Serializable;

/**
 * Created by Atlas on 2017-04-05.
 */

public class Session implements Serializable {

    private String id;
    private float startLatitude;
    private float startLongitude;
    private float endLatitude;
    private float endLongitude;
    private float distance;
    private int score;

    public Session(String id, float startLatitude, float startLongitude, float endLatitude, float endLongitude, float distance, int score) {
        this.id = id;
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.endLatitude = endLatitude;
        this.endLongitude = endLongitude;
        this.distance = distance;
        this.score = score;
    }

    @Override
    public String toString() {
        return "ID = " + id + "\n" + "StartLatitude = " + startLatitude + "\n" + "StartLongitude = " + startLongitude + "\n" + "EndLatitude = " + endLatitude + "\n" + "EndLongitude = " + endLatitude + "\n" + "Distance = " + distance + "\n" + "Score = " + score;
    }
}
