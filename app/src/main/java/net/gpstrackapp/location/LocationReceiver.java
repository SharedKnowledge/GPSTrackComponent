package net.gpstrackapp.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import net.sharksystem.asap.android.Util;

import java.util.ArrayList;
import java.util.List;


public class LocationReceiver extends BroadcastReceiver {
    private List<ILocationConsumer> consumers = new ArrayList<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle.containsKey("location")) {
            Location location = (Location) bundle.get("location");
            for (ILocationConsumer consumer : consumers) {
                consumer.onLocationChanged(location);
                Log.d(Util.getLogStart(this), "Location added to consumer " + consumer.toString());
            }
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
}
