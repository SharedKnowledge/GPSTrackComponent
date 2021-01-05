package net.gpstrackapp.geomodel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import org.osmdroid.util.NetworkLocationIgnorer;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;

import java.util.HashSet;
import java.util.Set;

public class LocationProvider implements IMyLocationProvider, LocationListener {
    private LocationManager locationManager;
    private Location location;

    private IMyLocationConsumer locationConsumer;
    private long locationUpdateMinTime = 0;
    private float locationUpdateMinDistance = 0.0f;
    private NetworkLocationIgnorer ignorer = new NetworkLocationIgnorer();
    private final Set<String> locationSources = new HashSet<>();

    public LocationProvider(Context ctx, boolean useGpsProvider, boolean useNetworkProvider) {
        locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        if (useGpsProvider) {
            locationSources.add(LocationManager.GPS_PROVIDER);
        }
        if (useNetworkProvider) {
            locationSources.add(LocationManager.NETWORK_PROVIDER);
        }
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public void clearLocationSources(){
        locationSources.clear();
    }

    public void addLocationSource(String source){
        locationSources.add(source);
    }

    public Set<String> getLocationSources(){
        return locationSources;
    }

    public long getLocationUpdateMinTime() {
        return locationUpdateMinTime;
    }

    public void setLocationUpdateMinTime(final long milliSeconds) {
        locationUpdateMinTime = milliSeconds;
    }

    public float getLocationUpdateMinDistance() {
        return locationUpdateMinDistance;
    }

    public void setLocationUpdateMinDistance(final float meters) {
        locationUpdateMinDistance = meters;
    }

    public IMyLocationConsumer getLocationConsumer() {
        return locationConsumer;
    }

    //
    // IMyLocationProvider
    //

    //TODO Permission handling
    @SuppressLint("MissingPermission")
    @Override
    public boolean startLocationProvider(IMyLocationConsumer myLocationConsumer) {
        locationConsumer = myLocationConsumer;
        boolean result = false;
        for (final String provider : locationManager.getProviders(true)) {
            if (locationSources.contains(provider)) {
                try {
                    locationManager.requestLocationUpdates(provider, locationUpdateMinTime,
                            locationUpdateMinDistance, this);
                    result = true;
                } catch (Throwable ex) {
                    Log.d(getLogStart(), "Unable to attach listener for location provider " + provider + " check permissions?", ex);
                }
            }
        }
        return result;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void stopLocationProvider() {
        locationConsumer = null;
        if (locationManager != null){
            try {
                locationManager.removeUpdates(this);
            } catch (Throwable ex) {
                Log.d(getLogStart(), "Unable to deattach location listener", ex);
            }
        }
    }

    @Override
    public Location getLastKnownLocation() {
        return location;
    }

    @Override
    public void destroy() {
        stopLocationProvider();
        location = null;
        locationManager = null;
        locationConsumer = null;
        ignorer = null;
    }

    //
    // LocationListener
    //

    @Override
    public void onLocationChanged(final Location location) {
        if (ignorer == null) {
            Log.d(getLogStart(), "location provider, mIgnore is null, unexpected. Location update will be ignored");
            return;
        }
        if (location == null || location.getProvider() == null)
            return;
        // ignore temporary non-gps fix
        if (ignorer.shouldIgnore(location.getProvider(), System.currentTimeMillis()))
            return;

        this.location = location;
        if (locationConsumer != null && location != null)
            locationConsumer.onLocationChanged(location, this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderDisabled(final String provider) {
    }

    @Override
    public void onProviderEnabled(final String provider) {
    }

    private String getLogStart() {
        return this.getClass().getSimpleName();
    }
}
