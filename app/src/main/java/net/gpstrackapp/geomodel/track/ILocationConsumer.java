package net.gpstrackapp.geomodel.track;

import android.location.Location;

public interface ILocationConsumer {
    void onLocationChanged(Location location);
}
