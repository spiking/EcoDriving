package com.example.currentplacedetailsonmap.models;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Atlas on 2017-04-05.
 */

public class Session implements Serializable, Comparable {

    private int id;
    private double startLatitude;
    private double startLongitude;
    private double endLatitude;
    private double endLongitude;
    private double distance;
    private int totalPoints;
    private int badPoints;
    private int okPoints;
    private int goodPoints;
    private String date;
    private HashMap<Integer, Integer> allScores;
    private ArrayList<LatLngSerializedObject> route;

    public Session(int id, double startLatitude, double startLongitude, double endLatitude, double endLongitude, double distance, int totalPoints, int badPoints, int okPoints, int goodPoints, String date, HashMap<Integer, Integer> allScores, ArrayList<LatLngSerializedObject> route) {
        this.id = id;
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.endLatitude = endLatitude;
        this.endLongitude = endLongitude;
        this.distance = distance;
        this.totalPoints = totalPoints;
        this.badPoints = badPoints;
        this.okPoints = okPoints;
        this.goodPoints = goodPoints;
        this.date = date;
        this.allScores = allScores;
        this.route = route;
    }

    @Override
    public String toString() {
        return "ID = " + id + "\n" + "StartLatitude = " + startLatitude + "\n" + "StartLongitude = " + startLongitude +
                "\n" + "EndLatitude = " + endLatitude + "\n" + "EndLongitude = " + endLatitude + "\n" + "Distance = " + distance + "\n" + "Score = " + totalPoints + "\n" + "Bad Points = " + badPoints + "\n" + "Ok Points = " + okPoints + "\n" + "Good Points = " + goodPoints + "\n" + "Date = " + date;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public HashMap<Integer, Integer> getAllScores() {
        return allScores;
    }

    public ArrayList<LatLngSerializedObject> getRoute() {
        return route;
    }

    public String getDate() {
        return date;
    }


    @Override
    public int compareTo(@NonNull Object o) {
        if(o instanceof Session) {
            return date.compareTo(((Session) o).getDate());
        }
        return 0;
    }
}
