package net.gpstrackapp;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;

import org.osmdroid.util.GeoPoint;

public abstract class MapViewActivity extends AppCompatActivity {
    protected ConfiguredMapView mapView = null;
    protected ViewGroup parentView = null;
    protected Presenter presenter = null;
    private final double DEFAULT_ZOOM_LEVEL = 18;

    protected abstract ConfiguredMapView setupMapViewAndGet();
    protected abstract ViewGroup setupLayoutAndGet();
    protected abstract Presenter setupPresenterAndGet();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mapView = setupMapViewAndGet();
        finishMapSetup();

        presenter = setupPresenterAndGet();
        presenter.onCreate();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // refresh the osmdroid configuration so that overlays can adjust
        mapView.onPause();
        presenter.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // refresh the osmdroid configuration so that overlays can adjust
        mapView.onResume();
        presenter.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        parentView.removeView(mapView);
        presenter.onDestroy();
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

    private void finishMapSetup() {
        Log.d(getLogStart(), "finish map setup");
        setZoomLevel(DEFAULT_ZOOM_LEVEL);
        parentView = this.setupLayoutAndGet();
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
        return this.getClass().getSimpleName();
    }
}
