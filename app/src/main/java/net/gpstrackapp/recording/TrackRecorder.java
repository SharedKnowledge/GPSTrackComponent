package net.gpstrackapp.recording;

import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;

import net.gpstrackapp.activity.LifecycleObject;
import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.location.ILocationConsumer;
import net.gpstrackapp.location.LocationReceiver;
import net.sharksystem.asap.android.Util;

public class TrackRecorder implements LifecycleObject, Recorder {
    private LocationReceiver locationReceiver;
    private Context ctx;
    private Track recordedTrack;

    public TrackRecorder(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void onCreate() {
        Log.d(Util.getLogStart(this), "onCreate");

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
        Log.d(Util.getLogStart(this), "onDestroy");
        unsetLocationReceiver();
    }

    @Override
    public void registerLocationConsumer(ILocationConsumer consumer) {
        locationReceiver.addLocationConsumer(consumer);
        if (consumer instanceof Track) {
            if (!isRecordingTrack()) {
                Log.d(Util.getLogStart(this), "Register track");
                recordedTrack = (Track) consumer;
            } else {
                Log.d(Util.getLogStart(this), "A Track is already being recorded. Unregister the track first to record a new one.");
                return;
            }
        }
    }

    @Override
    public void unregisterLocationConsumer(ILocationConsumer consumer) {
        locationReceiver.removeLocationConsumer(consumer);
        if (consumer.equals(recordedTrack)) {
            Log.d(Util.getLogStart(this), "Unregister track");
            recordedTrack = null;
        }
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

    @Override
    public void setLocationReceiver() {
        IntentFilter filter = new IntentFilter("LOCATION UPDATE");
        ctx.registerReceiver(locationReceiver, filter);
        Log.d(Util.getLogStart(this), "register receiver");
    }

    @Override
    public void unsetLocationReceiver() {
        ctx.unregisterReceiver(locationReceiver);
        Log.d(Util.getLogStart(this), "unregister receiver");
    }
}
