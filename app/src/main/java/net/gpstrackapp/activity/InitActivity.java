package net.gpstrackapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import net.gpstrackapp.GPSApp;
import net.gpstrackapp.activity.map.TrackRecordingMapActivity;
import net.sharksystem.asap.android.Util;

public class InitActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!GPSApp.isStarted()) {
            Log.d(Util.getLogStart(this), "Startup GPSApp");
            GPSApp.initializeGPSApp(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(Util.getLogStart(this), "start TrackRecordingMapActivity");
        Intent intent = new Intent(this, TrackRecordingMapActivity.class);
        startActivity(intent);
    }
}
