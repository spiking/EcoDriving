package com.example.currentplacedetailsonmap.models;

import java.io.Serializable;

/**
 * Created by Atlas on 2017-04-05.
 */

public class Session implements Serializable {

    private int id;
    private double startLatitude;
    private double startLongitude;
    private double endLatitude;
    private double endLongitude;
    private double distance;
    private int score;

    public Session(int id, double startLatitude, double startLongitude, double endLatitude, double endLongitude, double distance, int score) {
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
