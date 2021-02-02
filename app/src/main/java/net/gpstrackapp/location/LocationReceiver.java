package net.gpstrackapp.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import net.gpstrackapp.geomodel.ILocationConsumer;
import net.gpstrackapp.geomodel.track.Track;

import java.util.ArrayList;
import java.util.List;


public class LocationReceiver extends BroadcastReceiver {
    private List<ILocationConsumer> consumers = new ArrayList<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Location location = (Location) bundle.get("location");
        if (!consumers.isEmpty()) {
            for (int i = 0; i < consumers.size(); i++) {
                consumers.get(i).onLocationChanged(location);
            }
            Log.d(getLogStart(), "Location added");
        }
    }

    public void addLocationConsumer(ILocationConsumer consumer) {
        consumers.add(consumer);
    }

    public void removeLocationConsumer(ILocationConsumer consumer) {
        consumers.remove(consumer);
    }

    public List<ILocationConsumer> getLocationConsumers() {
        return consumers;
    }

    public boolean hasLocationConsumers() {
        return consumers.isEmpty() ? false : true;
    }

    private String getLogStart() {
        return this.getClass().getSimpleName();
    }
}
