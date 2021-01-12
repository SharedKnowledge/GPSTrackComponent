package net.gpstrackapp;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;

public abstract class MapViewActivity extends AppCompatActivity {
    protected MyMapView mapView = null;
    protected ViewGroup parentView = null;
    protected Presenter presenter = null;
    private final double DEFAULT_ZOOM_LEVEL = 18;

    protected abstract MyMapView setupMapViewAndGet();
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
        mapView.onPause();
        presenter.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
