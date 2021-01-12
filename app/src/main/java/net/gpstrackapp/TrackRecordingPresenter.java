package net.gpstrackapp;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.geomodel.track.TrackManager;

import org.osmdroid.views.MapView;

import java.util.Calendar;

public class TrackRecordingPresenter implements Presenter {
    private TrackLocationReceiver trackLocationReceiver;
    private Intent serviceIntent;
    private Context ctx;

    private TrackManager trackManager;

    public TrackRecordingPresenter(MyMapView mapView) {
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
        Log.d(getLogStart(), "Start Track Recording");
        Track trackToRecord = TrackManager.createTrack(trackName, Calendar.getInstance().getTime(), null);
        trackLocationReceiver.registerRecordedTrack(trackToRecord);
    }

    public void stopTrackRecording() {
        Log.d(getLogStart(), "Stop Track Recording");
        trackLocationReceiver.unregisterRecordedTrack();
    }

    public boolean isRecordingTrack() {
        return trackLocationReceiver.getRecordedTrack() != null;
    }

    private void startLocationService() {
        serviceIntent = new Intent(ctx.getApplicationContext(), LocationService.class);
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForegroundService(serviceIntent);
        } else {
            this.startService(serviceIntent);
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
