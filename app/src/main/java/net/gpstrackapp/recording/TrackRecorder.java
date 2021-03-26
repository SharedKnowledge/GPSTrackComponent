package net.gpstrackapp.recording;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import net.gpstrackapp.activity.LifecycleObject;
import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.location.ILocationConsumer;
import net.gpstrackapp.location.LocationReceiver;
import net.gpstrackapp.location.LocationService;

public class TrackRecorder implements LifecycleObject, Recorder {
    private LocationReceiver locationReceiver;
    private Intent serviceIntent;
    private Context ctx;
    private Track recordedTrack;

    public TrackRecorder(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void onCreate() {
        Log.d(getLogStart(), "onCreate");
        serviceIntent = new Intent(ctx, LocationService.class);
        /*
        Start the location service here (and stop it in onDestroy) to enable the user to navigate to other
        components in SN2 while recording because the service isn't destroyed. In newer SDK versions the
        notification channel may not be needed anymore.
        */
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (!LocationService.hasAskedUserPermission()) {
                showStartInForegroundDialog();
                LocationService.setAskedUserPermission(true);
            } else {
                startLocationService();
            }
        }

        if (locationReceiver == null) {
            locationReceiver = new LocationReceiver();
        }
        setLocationReceiver();
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {
        Log.d(getLogStart(), "onDestroy");
        stopLocationService();
        unsetLocationReceiver();
    }

    private void showStartInForegroundDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage("Run location service in foreground? Recording can only continue in doze mode if you select "
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
        Log.d(getLogStart(), "register track");
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
