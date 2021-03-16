package net.gpstrackapp.activity.map;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;

import net.gpstrackapp.Presenter;
import net.gpstrackapp.overlay.ConfiguredMapView;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.GeoPoint;

public abstract class MapViewActivity extends AppCompatActivity {
    protected ConfiguredMapView mapView = null;
    protected ViewGroup parentView = null;
    protected Presenter presenter = null;
    protected final double DEFAULT_ZOOM_LEVEL = 17;
    // set HTW Campus Wilhelminenhof as default location
    protected final double DEFAULT_LATITUDE = 52.457563642191246;
    protected final double DEFAULT_LONGITUDE = 13.526327369714947;

    protected abstract ViewGroup setupAndGetMapViewParentLayout();
    protected abstract Presenter setupAndGetPresenter();
    protected abstract ITileSource getMapSpecificTileSource();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(getLogStart(), "onCreate");
        super.onCreate(savedInstanceState);

        mapView = new ConfiguredMapView(this);
        finishMapSetup(getIntent());

        presenter = setupAndGetPresenter();
        if (presenter != null) {
            presenter.onCreate();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (presenter != null) {
            presenter.onStart();
        }
    }

    @Override
    protected void onResume() {
        Log.d(getLogStart(), "onResume");
        super.onResume();

        /* if the user changes the default tilesource in the settings then set the new tilesource here
         if the subclass does not specify a specific tilesource to be used in getMapSpecificTileSource() */
        ITileSource customTileSource = getMapSpecificTileSource();
        if (customTileSource != null) {
            mapView.setTileSource(customTileSource);
        } else {
            mapView.setTileSource(ConfiguredMapView.getDefaultTileSource());
            Log.d(getLogStart(), mapView.getTileProvider().toString());
        }

        // refresh the osmdroid configuration so that overlays can adjust
        mapView.onResume();
        if (presenter != null) {
            presenter.onResume();
        }
    }

    @Override
    protected void onPause() {
        Log.d(getLogStart(), "onPause");
        super.onPause();

        // refresh the osmdroid configuration so that overlays can adjust
        mapView.onPause();
        if (presenter != null) {
            presenter.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (presenter != null) {
            presenter.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(getLogStart(), "onDestroy");
        if (presenter != null) {
            presenter.onDestroy();
        }
        parentView.removeView(mapView);
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        GeoPoint lastLocation = mapView.getLastLocation();
        if (lastLocation != null) {
            savedInstanceState.putDouble("lat", lastLocation.getLatitude());
            savedInstanceState.putDouble("lon", lastLocation.getLongitude());
        }
        double zoomLevel = mapView.getZoomLevelDouble();
        if (zoomLevel != DEFAULT_ZOOM_LEVEL) {
            savedInstanceState.putDouble("zoom", mapView.getZoomLevelDouble());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("lat") && savedInstanceState.containsKey("lon")) {
            double lat = savedInstanceState.getDouble("lat");
            double lon = savedInstanceState.getDouble("lon");
            GeoPoint lastLocation = new GeoPoint(lat, lon);
            setCenterCoordinates(lastLocation);

            //Update location of location overlay
            Location location = new Location("");
            location.setLatitude(lastLocation.getLatitude());
            location.setLongitude(lastLocation.getLongitude());
            mapView.getProvider().onLocationChanged(location);
        }
        if (savedInstanceState.containsKey("zoom")) {
            setZoomLevel(savedInstanceState.getDouble("zoom"));
        }
    }

    private void finishMapSetup(Intent intent) {
        Log.d(getLogStart(), "finish map setup");
        GeoPoint lastLocation = new GeoPoint(
                intent.getDoubleExtra("lat", DEFAULT_LATITUDE),
                intent.getDoubleExtra("lon", DEFAULT_LONGITUDE));
        setCenterCoordinates(lastLocation);
        setZoomLevel(intent.getDoubleExtra("zoom", DEFAULT_ZOOM_LEVEL));

        //Update location of location overlay
        if (lastLocation.getLatitude() != DEFAULT_LATITUDE || lastLocation.getLongitude() != DEFAULT_LONGITUDE) {
            Location location = new Location("");
            location.setLatitude(lastLocation.getLatitude());
            location.setLongitude(lastLocation.getLongitude());
            mapView.getProvider().onLocationChanged(location);
        }

        parentView = this.setupAndGetMapViewParentLayout();
        parentView.addView(mapView);
    }

    private void setZoomLevel(double zoom) {
        mapView.getController().setZoom(zoom);
    }

    protected void setCenterCoordinates(GeoPoint locaction) {
        GeoPoint centerPoint = new GeoPoint(locaction.getLatitude(), locaction.getLongitude());
        mapView.getController().setCenter(centerPoint);
    }

    private String getLogStart() {
        return MapViewActivity.class.getSimpleName();
    }
}
