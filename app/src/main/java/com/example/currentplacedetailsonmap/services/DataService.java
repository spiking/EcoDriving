package com.example.currentplacedetailsonmap.services;

import android.content.Context;

import com.example.currentplacedetailsonmap.models.Session;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Atlas on 2017-04-05.
 */

public class DataService {

    // Singleton object

    private static final DataService instance = new DataService();

    public static DataService getInstance() {
        return instance;
    }

    private static ArrayList<Session> sessions = new ArrayList<>();

    public String mMapColor = "LIGHT";

    private DataService() {

    }

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
        ArrayList<Session> savedSessions = (ArrayList<Session>) is.readObject();
        sessions = savedSessions;
        is.close();
        fis.close();
    }

    public static void writeSessionToSessions(Session session) {
        sessions.add(session);
    }

    public static ArrayList<Session> getAllSessions() {
        Collections.sort(sessions, new Comparator<Session>(){
            public int compare(Session s1, Session s2) {
                return s2.getDate().compareToIgnoreCase(s1.getDate());
            }
        });

        return sessions;
    }

    public String getMapColor() {
        return mMapColor;
    }

    public void setMapColorDark(String mMapColor) {
        this.mMapColor = mMapColor;
    }
}
