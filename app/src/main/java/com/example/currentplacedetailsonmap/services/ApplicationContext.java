package com.example.currentplacedetailsonmap.services;

import android.app.Application;
import android.content.Context;

/**
 * Created by Atlas on 2017-04-25.
 */

public class ApplicationContext extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        ApplicationContext.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return ApplicationContext.context;
    }
}
