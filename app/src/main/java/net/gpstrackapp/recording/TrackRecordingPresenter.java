package net.gpstrackapp.recording;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import net.gpstrackapp.Presenter;
import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.geomodel.track.TrackVisualizer;
import net.gpstrackapp.location.ILocationConsumer;
import net.gpstrackapp.location.LocationReceiver;
import net.gpstrackapp.location.LocationService;
import net.gpstrackapp.overlay.ConfiguredMapView;

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
        serviceIntent = new Intent(ctx, LocationService.class);
        /*
        Start the location service here (and stop it in onDestroy) to enable the user to navigate to other
        components in SN2 while recording as the service isn't ended. Alternatively move this block to onResume
        and stopLocationService to onPause to record only while using this component.
        In newer SDK versions the notification channel may not be needed anymore.
        */

        if (!LocationService.hasAskedUserPermission()) {
            showStartInForegroundDialog();
            LocationService.setAskedUserPermission(true);
        } else {
            startLocationService();
        }

        if (locationReceiver == null) {
            locationReceiver = new LocationReceiver();
        }
        setLocationReceiver();
        Log.d(getLogStart(), "onCreate");
    }

    @Override
    public void onResume() {
        /* see comment in onCreate
        if (!LocationService.hasAskedUserPermission()) {
            showStartInForegroundDialog();
            LocationService.setAskedUserPermission(true);
        } else {
            startLocationService();
        }
        */

        //add and remove TrackOverlays
        trackVisualizer.updateGeoModelsOnMapView();
    }

    @Override
    public void onPause() {
        //stopLocationService();
    }

    @Override
    public void onDestroy() {
        stopLocationService();
        unsetLocationReceiver();
        Log.d(getLogStart(), "onDestroy");
    }

    public TrackVisualizer getTrackVisualizer() {
        return trackVisualizer;
    }

    private void showStartInForegroundDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage("Start location service in foreground? Recording can only continue in doze mode if you select "
                    + "\'Yes\', but it will drain the battery more. In case this is not needed select \'No\'.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    LocationService.setStartInForeground(true);
                    startLocationService();
                    dialog.dismiss();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    LocationService.setStartInForeground(false);
                    startLocationService();
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
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
        if (LocationService.shouldStartInForeground() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ctx.startForegroundService(serviceIntent);
            Log.d(getLogStart(), "start location service as foreground service");
        } else {
            ctx.startService(serviceIntent);
            Log.d(getLogStart(), "start location service as normal service");
        }
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
