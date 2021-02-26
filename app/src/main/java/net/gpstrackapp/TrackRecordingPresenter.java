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
        setTrackLocationReceiver();
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
        unsetTrackLocationReceiver();
        stopLocationService();
    }

    public TrackVisualizer getTrackVisualizer() {
        return trackVisualizer;
    }

    @Override
    public void registerLocationConsumer(ILocationConsumer consumer) {
        if (consumer instanceof Track) {
            if (!isRecordingTrack()) {
                recordedTrack = (Track) consumer;
            } else {
                Log.d(getLogStart(), "A Track is already being recorded. Unregister the track first to record a new one.");
                return;
            }
        }
        locationReceiver.addLocationConsumer(consumer);
        Log.d(getLogStart(), "Track recording (re)started");
        Toast.makeText(ctx, "Track recording (re)started", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void unregisterLocationConsumer(ILocationConsumer consumer) {
        if (consumer.equals(recordedTrack)) {
            recordedTrack = null;
        }
        locationReceiver.removeLocationConsumer(consumer);
        Log.d(getLogStart(), "Track recording stopped");
        Toast.makeText(ctx, "Track recording stopped", Toast.LENGTH_SHORT).show();
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

    //TODO Interface fuer das Registrieren eines Receivers?
    private void setTrackLocationReceiver() {
        IntentFilter filter = new IntentFilter("LOCATION UPDATE");
        ctx.registerReceiver(locationReceiver, filter);
        Log.d(getLogStart(), "register receiver");
    }

    private void unsetTrackLocationReceiver() {
        ctx.unregisterReceiver(locationReceiver);
        Log.d(getLogStart(), "unregister receiver");
    }

    private String getLogStart() {
        return this.getClass().getSimpleName();
    }
}
