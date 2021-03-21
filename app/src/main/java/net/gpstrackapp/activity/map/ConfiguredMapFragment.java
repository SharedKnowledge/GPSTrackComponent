package net.gpstrackapp.activity.map;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.gpstrackapp.overlay.ConfiguredMapView;
import net.gpstrackapp.overlay.TrackOverlay;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.gridlines.LatLonGridlineOverlay2;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class ConfiguredMapFragment extends Fragment {
    private Context ctx;
    public static final float DEFAULT_ZOOM_LEVEL = 17;
    // set HTW Campus Wilhelminenhof as default location
    public static final String DEFAULT_LATITUDE = "52.457563642191246";
    public static final String DEFAULT_LONGITUDE = "13.526327369714947";

    public static final String PREFS_NAME = "net.gpstrackapp.osm.prefs";
    public static final String PREFS_LATITUDE = "prefsLat";
    public static final String PREFS_LONGITUDE = "prefsLon";
    public static final String PREFS_ZOOM = "prefsZoom";

    private CopyrightOverlay copyrightOverlay;
    private MyLocationNewOverlay locationOverlay;
    private RotationGestureOverlay rotationGestureOverlay;
    private GpsMyLocationProvider provider;

    protected ConfiguredMapView mapView = null;
    protected SharedPreferences prefs;

    private ITileSource mapSpecificTileSource = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(getLogStart(), "onCreateView");
        mapView = new ConfiguredMapView(inflater.getContext());
        mapView.setDestroyMode(false);
        return mapView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.ctx = this.getActivity();

        setupOverlays(ctx);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);
        mapView.setMultiTouchControls(true);
    }

    private void setupOverlays(Context ctx) {
        Log.d(getLogStart(), "setup Overlays");
        mapView.getOverlays().clear();
        if (copyrightOverlay == null) {
            copyrightOverlay = new CopyrightOverlay(ctx);
        }
        mapView.getOverlays().add(copyrightOverlay);

        // check location permission
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (provider == null) {
                provider = new GpsMyLocationProvider(ctx);
            }

            if (locationOverlay == null) {
                locationOverlay = new MyLocationNewOverlay(provider, mapView);
                locationOverlay.enableMyLocation();
                locationOverlay.enableFollowLocation();
                mapView.getOverlays().add(locationOverlay);
            }
        }

        if (rotationGestureOverlay == null) {
            rotationGestureOverlay = new RotationGestureOverlay(mapView);
            rotationGestureOverlay.setEnabled(true);
        }
        mapView.getOverlays().add(rotationGestureOverlay);

        // setup map specific overlays
        Activity activity = this.getActivity();
        if (activity instanceof ActivityWithAdditionalMapOverlays) {
            ActivityWithAdditionalMapOverlays mapOverlaysActivity = (ActivityWithAdditionalMapOverlays) activity;
            mapOverlaysActivity.setupAdditionalOverlays(mapView);
        }
    }

    public void addTrackOverlay(TrackOverlay trackOverlay) {
        mapView.getOverlays().add(trackOverlay);
        trackOverlay.initializeComponents(mapView);
    }

    public void removeTrackOverlay(TrackOverlay trackOverlay) {
        mapView.getOverlays().remove(trackOverlay);
    }

    public GpsMyLocationProvider getProvider() {
        return provider;
    }

    public GeoPoint getLastLocation() {
        if (locationOverlay != null) {
            return locationOverlay.getMyLocation();
        }
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadMapPreferences();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(getLogStart(), "onResume");
        /* if the user changes the default tilesource in the settings then set the new tilesource here
         if the subclass does not specify a specific tilesource to be used in getMapSpecificTileSource() */
        if (mapSpecificTileSource != null) {
            mapView.setTileSource(mapSpecificTileSource);
        } else {
            mapView.setTileSource(ConfiguredMapView.getDefaultTileSource());
        }

        // refresh the osmdroid configuration so that overlays can adjust
        mapView.onResume();
    }

    @Override
    public void onPause() {
        Log.d(getLogStart(), "onPause");
        saveMapPreferences();

        // refresh the osmdroid configuration so that overlays can adjust
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        Log.d(getLogStart(), "onDestroyView");
        super.onDestroyView();
        mapView.onDetach();
    }

    private void loadMapPreferences() {
        Log.d(getLogStart(), "loadMapPreferences");
        prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String latString = prefs.getString(PREFS_LATITUDE, DEFAULT_LATITUDE);
        float lat = Float.valueOf(latString);
        String lonString = prefs.getString(PREFS_LONGITUDE, DEFAULT_LONGITUDE);
        float lon = Float.valueOf(lonString);
        GeoPoint lastLocation = new GeoPoint(lat, lon);
        setCenterCoordinates(lastLocation);

        Log.d(getLogStart(), "lat: " + lat);
        Log.d(getLogStart(), "lon: " + lon);

        float zoom = prefs.getFloat(PREFS_ZOOM, DEFAULT_ZOOM_LEVEL);
        setZoomLevel(zoom);

        GpsMyLocationProvider provider = this.getProvider();
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
            editor.putString(PREFS_LATITUDE, String.valueOf(lastLocation.getLatitude()));
            editor.putString(PREFS_LONGITUDE, String.valueOf(lastLocation.getLongitude()));

            Log.d(getLogStart(), "lat: " + lastLocation.getLatitude());
            Log.d(getLogStart(), "lon: " + lastLocation.getLongitude());
        }
        float zoomLevel = (float) mapView.getZoomLevelDouble();
        if (zoomLevel != DEFAULT_ZOOM_LEVEL) {
            editor.putFloat(PREFS_ZOOM, zoomLevel);
        }
        editor.commit();
    }

    public void setZoomLevel(double zoom) {
        mapView.getController().setZoom(zoom);
    }

    public double getZoomLevel() {
        return mapView.getZoomLevelDouble();
    }

    protected void setCenterCoordinates(GeoPoint location) {
        GeoPoint centerPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        mapView.getController().setCenter(centerPoint);
    }

    public void setMapSpecificTileSource(ITileSource mapSpecificTileSource) {
        this.mapSpecificTileSource = mapSpecificTileSource;
    }

    public ConfiguredMapView getMapView() {
        return mapView;
    }

    public void invalidateMapView() {
        mapView.invalidate();
    }

    private String getLogStart() {
        return this.getClass().getSimpleName();
    }
}
