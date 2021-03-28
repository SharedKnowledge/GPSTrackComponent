package net.gpstrackapp.mapview;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.gpstrackapp.activity.map.ActivityWithAdditionalMapOverlays;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class ConfiguredMapFragment extends Fragment {
    public static final float DEFAULT_ZOOM_LEVEL = 17;
    // set HTW Campus Wilhelminenhof as default location
    public static final String DEFAULT_LATITUDE = "52.457563642191246";
    public static final String DEFAULT_LONGITUDE = "13.526327369714947";

    public static final String PREFS_NAME = "net.gpstrackapp.osm.prefs";
    public static final String PREFS_LATITUDE = "prefsLat";
    public static final String PREFS_LONGITUDE = "prefsLon";
    public static final String PREFS_ZOOM = "prefsZoom";
    public static final String PREFS_TILE_SOURCE = "prefsTileSource";

    private Context ctx;
    private boolean mapViewReady = false;
    private boolean downloadable = false;
    protected MapView mapView = null;
    protected SharedPreferences prefs;
    private CopyrightOverlay copyrightOverlay;
    private MyLocationNewOverlay locationOverlay;
    private RotationGestureOverlay rotationGestureOverlay;
    private GpsMyLocationProvider provider;
    private ITileSource mapSpecificTileSource = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(getLogStart(), "onCreateView");
        Bundle bundle = getArguments();
        this.downloadable = bundle != null ? bundle.getBoolean("downloadable") : false;
        if (downloadable) {
            mapView = new DownloadableTilesMapView(inflater.getContext());
        } else {
            mapView = new MapView(inflater.getContext());
        }
        mapView.setDestroyMode(false);
        return mapView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.ctx = this.getActivity();
        this.mapViewReady = true;

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

    public void addOverlay(Overlay overlay) {
        if (mapViewReady) {
            mapView.getOverlays().add(overlay);
            if (overlay instanceof TrackOverlay) {
                TrackOverlay trackOverlay = (TrackOverlay) overlay;
                trackOverlay.initializeComponents(mapView);
            }
        } else {
            Log.e(getLogStart(), "Could not add Overlay because the activity is not yet created");
        }
    }

    public void removeOverlay(Overlay overlay) {
        if (mapViewReady) {
            mapView.getOverlays().remove(overlay);
        } else {
            Log.e(getLogStart(), "Could not add Overlay because the activity is not yet created");
        }
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
        mapViewReady = false;
    }

    private void loadMapPreferences() {
        Log.d(getLogStart(), "loadMapPreferences");
        prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String latString = prefs.getString(PREFS_LATITUDE, DEFAULT_LATITUDE);
        float lat = Float.parseFloat(latString);
        String lonString = prefs.getString(PREFS_LONGITUDE, DEFAULT_LONGITUDE);
        float lon = Float.parseFloat(lonString);
        GeoPoint lastLocation = new GeoPoint(lat, lon);
        setCenterCoordinates(lastLocation);

        if (mapSpecificTileSource == null) {
            String tileSourceName = prefs.getString(PREFS_TILE_SOURCE, null);
            if (tileSourceName != null) {
                ITileSource tileSource = TileSourceFactory.getTileSource(tileSourceName);
                mapView.setTileSource(tileSource);
            }
        } else {
            mapView.setTileSource(mapSpecificTileSource);
        }

        float zoom = prefs.getFloat(PREFS_ZOOM, DEFAULT_ZOOM_LEVEL);
        // correct toom level according to tile source
        ITileSource tileSource = mapView.getTileProvider().getTileSource();
        int minZoom = tileSource.getMinimumZoomLevel();
        int maxZoom = tileSource.getMaximumZoomLevel();
        zoom = zoom < minZoom ? minZoom : zoom;
        zoom = zoom > maxZoom ? maxZoom : zoom;
        setZoomLevel(zoom);
    }

    private void saveMapPreferences() {
        Log.d(getLogStart(), "saveMapPreferences");
        SharedPreferences.Editor editor = prefs.edit();
        IGeoPoint lastLocation = mapView.getMapCenter();
        if (lastLocation != null) {
            editor.putString(PREFS_LATITUDE, String.valueOf(lastLocation.getLatitude()));
            editor.putString(PREFS_LONGITUDE, String.valueOf(lastLocation.getLongitude()));
        }
        float zoomLevel = (float) mapView.getZoomLevelDouble();
        if (zoomLevel != DEFAULT_ZOOM_LEVEL) {
            editor.putFloat(PREFS_ZOOM, zoomLevel);
        }
        editor.apply();
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

    // use this method to let the mapView only use one specific tile source
    public void setMapSpecificTileSource(ITileSource mapSpecificTileSource) {
        this.mapSpecificTileSource = mapSpecificTileSource;
    }

    public MapView getMapView() {
        return mapView;
    }

    public void invalidateMapView() {
        mapView.invalidate();
    }

    private String getLogStart() {
        return this.getClass().getSimpleName();
    }
}
