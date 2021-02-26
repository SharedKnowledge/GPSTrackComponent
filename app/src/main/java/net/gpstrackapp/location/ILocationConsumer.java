package net.gpstrackapp.location;

import android.location.Location;

public interface ILocationConsumer {
    void onLocationChanged(Location location);
}
