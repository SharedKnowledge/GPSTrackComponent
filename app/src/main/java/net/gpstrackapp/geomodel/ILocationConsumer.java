package net.gpstrackapp.geomodel;

import android.location.Location;

public interface ILocationConsumer {
    void onLocationChanged(Location location);
}
