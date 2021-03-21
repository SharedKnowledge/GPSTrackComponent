package net.gpstrackapp.activity.map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;

import net.gpstrackapp.overlay.ConfiguredMapView;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;

public abstract class MapViewActivity extends AppCompatActivity {
    protected ConfiguredMapView mapView = null;
    protected ViewGroup parentView = null;
    protected SharedPreferences prefs;

    protected abstract ViewGroup setupLayoutAndGetMapViewParentView();
    protected abstract ITileSource getMapSpecificTileSource();

    // invalidate map when going online
    private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                mapView.invalidate();
            } catch (NullPointerException e) {
                Log.e(getLogStart(), "mapView is null");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(getLogStart(), "onCreate");
        super.onCreate(savedInstanceState);

        mapView = new ConfiguredMapView(this);
        parentView = this.setupLayoutAndGetMapViewParentView();
        if (parentView != null) {
            parentView.addView(mapView);
        } else {
            Log.e(getLogStart(), "Parent view for map view was null");
        }

        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onStart() {
        loadMapPreferences();
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(getLogStart(), "onResume");
        /* if the user changes the default tilesource in the settings then set the new tilesource here
         if the subclass does not specify a specific tilesource to be used in getMapSpecificTileSource() */
        ITileSource customTileSource = getMapSpecificTileSource();
        if (customTileSource != null) {
            mapView.setTileSource(customTileSource);
        } else {
            mapView.setTileSource(ConfiguredMapView.getDefaultTileSource());
            Log.d(getLogStart(), mapView.getTileProvider().toString());
        }
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        // refresh the osmdroid configuration so that overlays can adjust
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(getLogStart(), "onPause");
        saveMapPreferences();
        super.onPause();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Configuration.getInstance().save(this, prefs);

        // refresh the osmdroid configuration so that overlays can adjust
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(getLogStart(), "onDestroy");
        mapView.onDetach();
        parentView.removeView(mapView);
        unregisterReceiver(networkReceiver);
        super.onDestroy();
    }

    private void loadMapPreferences() {
        Log.d(getLogStart(), "loadMapPreferences");
        prefs = getSharedPreferences(ConfiguredMapView.PREFS_NAME, Context.MODE_PRIVATE);

        String latString = prefs.getString(ConfiguredMapView.PREFS_LATITUDE, ConfiguredMapView.DEFAULT_LATITUDE);
        float lat = Float.valueOf(latString);
        String lonString = prefs.getString(ConfiguredMapView.PREFS_LONGITUDE, ConfiguredMapView.DEFAULT_LONGITUDE);
        float lon = Float.valueOf(lonString);
        GeoPoint lastLocation = new GeoPoint(lat, lon);
        setCenterCoordinates(lastLocation);

        Log.d(getLogStart(), "lat: " + lat);
        Log.d(getLogStart(), "lon: " + lon);

        float zoom = prefs.getFloat(ConfiguredMapView.PREFS_ZOOM, ConfiguredMapView.DEFAULT_ZOOM_LEVEL);
        setZoomLevel(zoom);

        GpsMyLocationProvider provider = mapView.getProvider();
        // Update location of location overlay
        if (provider != null) {
            Location location = new Location("");
            location.setLatitude(lastLocation.getLatitude());
            location.setLongitude(lastLocation.getLongitude());
            provider.onLocationChanged(location);
        }
    }

    private void saveMapPreferences() {
        Log.d(getLogStart(), "saveMapPreferences");
        SharedPreferences.Editor editor = prefs.edit();
        IGeoPoint lastLocation = mapView.getMapCenter();
        //IGeoPoint lastLocation = mapView.getLastLocation();
        if (lastLocation != null) {
            editor.putString(ConfiguredMapView.PREFS_LATITUDE, String.valueOf(lastLocation.getLatitude()));
            editor.putString(ConfiguredMapView.PREFS_LONGITUDE, String.valueOf(lastLocation.getLongitude()));

            Log.d(getLogStart(), "lat: " + lastLocation.getLatitude());
            Log.d(getLogStart(), "lon: " + lastLocation.getLongitude());
        }
        float zoomLevel = (float) mapView.getZoomLevelDouble();
        if (zoomLevel != ConfiguredMapView.DEFAULT_ZOOM_LEVEL) {
            editor.putFloat(ConfiguredMapView.PREFS_ZOOM, zoomLevel);
        }
        editor.commit();
    }

    private void setZoomLevel(double zoom) {
        mapView.getController().setZoom(zoom);
    }

    protected void setCenterCoordinates(GeoPoint location) {
        GeoPoint centerPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        mapView.getController().setCenter(centerPoint);
    }

    private String getLogStart() {
        return MapViewActivity.class.getSimpleName();
    }
}
