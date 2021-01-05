package net.gpstrackapp;

import android.location.Location;

import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;

public class LocationConsumerManager implements IMyLocationConsumer {

    @Override
    public void onLocationChanged(Location location, IMyLocationProvider source) {

    }
}
