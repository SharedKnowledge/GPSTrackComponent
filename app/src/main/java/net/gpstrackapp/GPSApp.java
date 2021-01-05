package net.gpstrackapp;

import android.app.Activity;
import android.util.Log;

import net.sharksystem.asap.android.apps.ASAPApplication;

import java.util.ArrayList;
import java.util.Collection;

public class GPSApp extends ASAPApplication {
    public static final String GPS_APPNAME = "ASAP_GPS_APP";
    private static GPSApp singleton = null;

    private GPSApp(Collection<CharSequence> appFormats, Activity initialActivity) {
        super(appFormats, initialActivity);
    }

    @Override
    public void startASAPApplication() {
        super.startASAPApplication();
    }

    public static boolean isStarted() { return GPSApp.singleton != null; }

    static GPSApp initializeGPSApp(Activity initialActivity) {
        if(GPSApp.singleton == null) {
            Collection<CharSequence> formats = new ArrayList<>();
            formats.add(GPS_APPNAME);

            GPSApp.singleton = new GPSApp(formats, initialActivity);

            GPSComponent.initialize(GPSApp.singleton);

            // launch
            Log.d(getLogStart(), "launch App");
            GPSApp.singleton.startASAPApplication();
        } // else - already initialized - nothing happens.
        return GPSApp.singleton;
    }

    private static String getLogStart() {
        return GPSApp.class.getSimpleName();
    }
}
