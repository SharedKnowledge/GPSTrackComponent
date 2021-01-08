package net.gpstrackapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import net.gpstrackapp.geomodel.track.Track;


public class TrackLocationReceiver extends BroadcastReceiver {
    private Track recordedTrack = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(getLogStart(), "Location update received");
        Bundle bundle = intent.getExtras();
        Location location = (Location) bundle.get("location");
        if (recordedTrack != null) {
            recordedTrack.onLocationChanged(location);
        }
    }

    public void registerRecordedTrack(Track track) {
        recordedTrack = track;
    }

    public void unregisterRecordedTrack() {
        recordedTrack = null;
    }

    public Track getRecordedTrack() {
        return recordedTrack;
    }

    private String getLogStart() {
        return this.getClass().getSimpleName();
    }
}
