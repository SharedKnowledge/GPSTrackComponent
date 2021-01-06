package net.gpstrackapp;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;

public abstract class MapViewActivity extends AppCompatActivity {
    protected MapView mapView = null;
    protected ViewGroup parentView = null;

    protected abstract MapView setupMapViewAndGet();
    protected abstract ViewGroup setupLayoutAndGet();
    protected abstract double setupZoomLevel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mapView = setupMapViewAndGet();
        finishMapSetup();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        parentView.removeView(mapView);
        super.onDestroy();
    }

    private void finishMapSetup() {
        setZoom(setupZoomLevel());
        addZoomControls();
        parentView = this.setupLayoutAndGet();
        parentView.addView(mapView);
    }

    private void setZoom(double zoom) {
        mapView.getController().setZoom(zoom);
    }

    private void addZoomControls() {
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);
        mapView.setMultiTouchControls(true);
    }

    protected void setCenterCoordinates(GeoPoint locaction) {
        GeoPoint centerPoint = new GeoPoint(locaction.getLatitude(), locaction.getLongitude());
        mapView.getController().setCenter(centerPoint);
    }
}
