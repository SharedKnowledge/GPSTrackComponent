package net.gpstrackapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class InitActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!GPSApp.isStarted()) {
            Log.d(this.getLogStart(), "Startup GPSApp");
            GPSApp.initializeGPSApp(this);
        }

        Intent intent = new Intent(this, TrackRecordingMapActivity.class);
        startActivity(intent);
    }

    private String getLogStart() {
        return this.getClass().getSimpleName();
    }
}
