package com.example.currentplacedetailsonmap.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.currentplacedetailsonmap.models.Session;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Created by Atlas on 2017-04-05.
 */

public class DataService {

    // Singleton object

    private static final DataService instance = new DataService();

    public static DataService getInstance() {
        return instance;
    }

    private static Map<Integer, Session> sessions = new TreeMap<>();

    public static String mMapColor = "LIGHT";
    public static String mDriveMode = "CAR";
    public static String mProximityAccess = "FALSE";

    public static void writeSessionsToDatabase(String fileName) throws IOException {
        FileOutputStream fos = ApplicationContext.getAppContext().openFileOutput(fileName, Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(sessions);
        os.close();
        fos.close();
    }


    public static void readSessionsFromDatabase(String fileName) throws IOException, ClassNotFoundException {
        FileInputStream fis = ApplicationContext.getAppContext().openFileInput(fileName);
        ObjectInputStream is = new ObjectInputStream(fis);
        Map<Integer, Session> savedSessions = (TreeMap<Integer, Session>) is.readObject();
        sessions = savedSessions;
        is.close();
        fis.close();
    }

    public static void saveToSharedPreferences(String key, String value) {
        SharedPreferences preferences = ApplicationContext.getAppContext().getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Log.v("PREF", "ADDING " + key + " " + value);
        editor.putString(key, value);
        editor.commit();
    }

    public static void readFromSharedPreferences(String key) {
        SharedPreferences preferences = ApplicationContext.getAppContext().getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE);

        String value = preferences.getString(key, null);

        if (key.equals("DRIVE_MODE")) {
            mDriveMode = value;
        } else if (key.equals("MAP_COLOR")) {
            mMapColor = value;
        } else if (key.equals("PROXIMITY_ACCESS")) {
            mProximityAccess = value;
        } else {
            Log.wtf("WTF", "This should never get called");
        }
    }

    public static void writeSessionToSessions(Session session) {
        sessions.put(sessions.size(), session);
    }

    public static Map<Integer, Session> getAllSessions() {
        Map<Integer, Session> sortedSessions = new TreeMap<>(Collections.reverseOrder());
        sortedSessions.putAll(sessions);
        sessions = sortedSessions;
        printMap(sessions);
        return sessions;
    }

    public String getDriveMode() {
        if(mDriveMode == null){
            mDriveMode = "CAR";
        }
        return mDriveMode;
    }

    public static void setDriveMode(String mNewDriveMode) throws IOException {
        mDriveMode = mNewDriveMode;
        saveToSharedPreferences("DRIVE_MODE", mDriveMode);
    }

    public String getMapColor() {
        if(mMapColor == null){
            mMapColor = "LIGHT";
        }
        return mMapColor;
    }

    public static void setMapColorDark(String mNewMapColor) throws IOException {
        mMapColor = mNewMapColor;
        saveToSharedPreferences("MAP_COLOR", mMapColor);
    }

    public String getProximityAccess() {
        if(mProximityAccess == null){
            mProximityAccess = "FALSE";
        }
        return mProximityAccess;
    }

    public static void setProximityAccess(String mNewProximityAccess) throws IOException {
        mProximityAccess = mNewProximityAccess;
        saveToSharedPreferences("PROXIMITY_ACCESS", mProximityAccess);
    }

    public Map<Integer, Session> getSessions() {
        return sessions;
    }

    public int getSessionMapSize() {
        return sessions.size();
    }

    public static void printMap(Map<Integer, Session> map) {
        for (Entry<Integer, Session> entry : map.entrySet()) {
            System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue().getDate());
        }
    }
}
