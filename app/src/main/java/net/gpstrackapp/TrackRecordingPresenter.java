package net.gpstrackapp;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import net.gpstrackapp.location.ILocationConsumer;
import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.geomodel.track.TrackVisualizer;
import net.gpstrackapp.location.LocationService;
import net.gpstrackapp.location.LocationReceiver;

public class TrackRecordingPresenter implements Presenter, Recorder {
    private LocationReceiver locationReceiver;
    private Intent serviceIntent;
    private Context ctx;
    private Track recordedTrack;
    private TrackVisualizer trackVisualizer;

    public TrackRecordingPresenter(ConfiguredMapView mapView) {
        this.trackVisualizer = new TrackVisualizer(mapView);
        this.ctx = mapView.getContext();
    }

    @Override
    public void onCreate() {
        //start service here so that user can navigate to other components in SN2 without ending the service
        startLocationService();
        if (locationReceiver == null) {
            locationReceiver = new LocationReceiver();
        }
        setLocationReceiver();
        Log.d(getLogStart(), "onCreate");
    }

    @Override
    public void onPause() { }

    @Override
    public void onResume() {
        //add and remove TrackOverlays
        trackVisualizer.updateGeoModelsOnMapView();
    }

    @Override
    public void onDestroy() {
        unsetLocationReceiver();
        stopLocationService();
    }

    public TrackVisualizer getTrackVisualizer() {
        return trackVisualizer;
    }

    @Override
    public void registerLocationConsumer(ILocationConsumer consumer) {
        locationReceiver.addLocationConsumer(consumer);
        if (consumer instanceof Track) {
            if (!isRecordingTrack()) {
                recordedTrack = (Track) consumer;
            } else {
                Log.d(getLogStart(), "A Track is already being recorded. Unregister the track first to record a new one.");
                return;
            }
        }
        Log.d(getLogStart(), "Recording (re)started");
        Toast.makeText(ctx, "Recording (re)started", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void unregisterLocationConsumer(ILocationConsumer consumer) {
        locationReceiver.removeLocationConsumer(consumer);
        if (consumer.equals(recordedTrack)) {
            recordedTrack = null;
        }
        Log.d(getLogStart(), "Recording stopped");
        Toast.makeText(ctx, "Recording stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean isRecording() {
        return locationReceiver.hasLocationConsumers();
    }

    public boolean isRecordingTrack() {
        return getRecordedTrack() == null ? false : true;
    }

    public Track getRecordedTrack() {
        return recordedTrack;
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

    @Override
    public void setLocationReceiver() {
        IntentFilter filter = new IntentFilter("LOCATION UPDATE");
        ctx.registerReceiver(locationReceiver, filter);
        Log.d(getLogStart(), "register receiver");
    }

    @Override
    public void unsetLocationReceiver() {
        ctx.unregisterReceiver(locationReceiver);
        Log.d(getLogStart(), "unregister receiver");
    }

    private String getLogStart() {
        return this.getClass().getSimpleName();
    }
}
