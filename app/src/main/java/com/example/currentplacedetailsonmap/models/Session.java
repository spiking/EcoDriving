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
    private int totalScore;
    private int badPoints;
    private int okPoints;
    private int goodPoints;
    private String date;

    public Session(int id, double startLatitude, double startLongitude, double endLatitude, double endLongitude, double distance, int totalScore, int badPoints, int okPoints, int goodPoints, String date) {
        this.id = id;
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.endLatitude = endLatitude;
        this.endLongitude = endLongitude;
        this.distance = distance;
        this.totalScore = totalScore;
        this.badPoints = badPoints;
        this.okPoints = okPoints;
        this.goodPoints = goodPoints;
        this.date = date;
    }

    @Override
    public String toString() {
        return "ID = " + id + "\n" + "StartLatitude = " + startLatitude + "\n" + "StartLongitude = " + startLongitude +
                "\n" + "EndLatitude = " + endLatitude + "\n" + "EndLongitude = " + endLatitude + "\n" + "Distance = " + distance + "\n" + "Score = " + totalScore + "\n" + "Bad Points = " + badPoints + "\n" + "Ok Points = " + okPoints + "\n" + "Good Points = " + goodPoints + "\n" + "Date = " + date;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public String getDate() {
        return date;
    }
}
