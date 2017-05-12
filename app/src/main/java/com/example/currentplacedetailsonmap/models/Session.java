package com.example.currentplacedetailsonmap.models;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Atlas on 2017-04-05.
 */

public class Session implements Serializable, Comparable {

    private String id;
    private double startLatitude;
    private double startLongitude;
    private double endLatitude;
    private double endLongitude;
    private int highScore;
    private int currentScore;
    private int highestStreak;
    private int badPoints;
    private int okPoints;
    private int goodPoints;
    private String date;
    private double distance;
    private long time;
    private HashMap<Integer, Integer> allScores;
    private ArrayList<LatLngSerializedObject> route;
    private ArrayList<LatLngSerializedObject> redScreenMarkers;

    public Session(String id, double startLatitude, double startLongitude, double endLatitude, double endLongitude,
                   int highScore, int currentScore, int highestStreak, int badPoints, int okPoints,
                   int goodPoints, String date,
                   HashMap<Integer, Integer> allScores, ArrayList<LatLngSerializedObject> route,
                   ArrayList<LatLngSerializedObject> redScreenMarkers,
                   double distance, long time) {
        this.id = id;
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.endLatitude = endLatitude;
        this.endLongitude = endLongitude;
        this.highScore = highScore;
        this.currentScore = currentScore;
        this.highestStreak = highestStreak;
        this.badPoints = badPoints;
        this.okPoints = okPoints;
        this.goodPoints = goodPoints;
        this.date = date;
        this.allScores = allScores;
        this.route = route;
        this.redScreenMarkers = redScreenMarkers;
        this.distance = distance;
        this.time = time;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        if(o instanceof Session) {
            return date.compareTo(((Session) o).getDate());
        }
        return 0;
    }

    public HashMap<Integer, Integer> getAllScores() {
        return allScores;
    }

    public ArrayList<LatLngSerializedObject> getRoute() {
        return route;
    }

    public ArrayList<LatLngSerializedObject> getRedScreenMarkers() {
        return redScreenMarkers;
    }

    public String getDate() {
        return date;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public long getTravelTime() {
        return time;
    }

    public double getTravelDistance() {
        return distance;
    }

}
