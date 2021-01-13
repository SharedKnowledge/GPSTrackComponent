package net.gpstrackapp;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.geomodel.track.TrackManager;

import java.util.Calendar;

public class TrackRecordingPresenter implements Presenter {
    private TrackLocationReceiver trackLocationReceiver;
    private Intent serviceIntent;
    private Context ctx;

    private TrackManager trackManager;

    public TrackRecordingPresenter(ConfiguredMapView mapView) {
        this.trackManager = new TrackManager(mapView);
        this.ctx = mapView.getContext();
    }

    @Override
    public void onCreate() {
        //start service here so that user can navigate to other components in SN2 without ending the service
        startLocationService();
        if (trackLocationReceiver == null) {
            trackLocationReceiver = new TrackLocationReceiver();
        }
        registerTrackLocationReceiver();
    }

    @Override
    public void onPause() { }

    @Override
    public void onResume() {
        //add and remove TrackOverlays
        trackManager.updateGeoModelsOnMapView();
    }

    @Override
    public void onDestroy() {
        unregisterTrackLocationReceiver();
        stopLocationService();
    }

    public TrackManager getTrackManager() {
        return trackManager;
    }

    public void startTrackRecording(String trackName) {
        Track trackToRecord = TrackManager.createTrack(null, trackName,
                GPSComponent.getGPSComponent().getASAPApplication().getOwnerName(),
                Calendar.getInstance().getTime(), null);
        trackLocationReceiver.registerRecordedTrack(trackToRecord);
        Log.d(getLogStart(), "Track recording started");
        Toast.makeText(ctx, "Track recording started", Toast.LENGTH_SHORT).show();
    }

    public void restartTrackRecording(Track track) {
        trackLocationReceiver.registerRecordedTrack(track);
        Log.d(getLogStart(), "Track recording restarted");
    }

    public void stopTrackRecording() {
        trackLocationReceiver.unregisterRecordedTrack();
        Log.d(getLogStart(), "Track recording stopped");
        Toast.makeText(ctx, "Track recording stopped", Toast.LENGTH_SHORT).show();
    }

    public boolean isRecordingTrack() {
        return trackLocationReceiver.getRecordedTrack() != null;
    }

    public Track getRecordedTrack() {
        return trackLocationReceiver.getRecordedTrack();
    }

    private void startLocationService() {
        serviceIntent = new Intent(ctx.getApplicationContext(), LocationService.class);
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ctx.startForegroundService(serviceIntent);
        } else {
            ctx.startService(serviceIntent);
        }
        */
        ctx.startService(serviceIntent);
        Log.d(getLogStart(), "start location service");
    }

    private void stopLocationService() {
        ctx.stopService(serviceIntent);
        Log.d(getLogStart(), "stop location service");
    }

    //TODO Interface fuer das Registrieren eines Receivers?
    private void registerTrackLocationReceiver() {
        IntentFilter filter = new IntentFilter("LOCATION UPDATE");
        ctx.registerReceiver(trackLocationReceiver, filter);
        Log.d(getLogStart(), "register receiver");
    }

    private void unregisterTrackLocationReceiver() {
        ctx.unregisterReceiver(trackLocationReceiver);
        Log.d(getLogStart(), "unregister receiver");
    }

    private String getLogStart() {
        return this.getClass().getSimpleName();
    }
}
