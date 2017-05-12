package com.example.currentplacedetailsonmap.services;

import android.content.Context;

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

    public static void readMapColorTypeFromDatabase(String fileName) throws IOException, ClassNotFoundException {
        FileInputStream fis = ApplicationContext.getAppContext().openFileInput(fileName);
        ObjectInputStream is = new ObjectInputStream(fis);
        mMapColor = (String) is.readObject();
        is.close();
        fis.close();
    }

    public static void saveMapColorTypeToDatabase(String fileName) throws IOException {
        FileOutputStream fos = ApplicationContext.getAppContext().openFileOutput(fileName, Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(mMapColor);
        os.close();
        fos.close();
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

    public String getMapColor() {
        return mMapColor;
    }

    public void setMapColorDark(String mMapColor) throws IOException {
        this.mMapColor = mMapColor;
        saveMapColorTypeToDatabase("MAP_COLOR");
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
