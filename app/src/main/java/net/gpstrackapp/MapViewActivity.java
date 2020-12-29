package net.gpstrackapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;

public abstract class MapViewActivity extends AppCompatActivity {
    protected MapView mapView = null;
    protected ViewGroup layout = null;

    protected abstract MapView setUpMapViewAndGet();
    protected abstract void addOverlays();
    protected abstract ViewGroup setUpLayoutAndGet();
    protected abstract void setPerspectiveParameters();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mapView = setUpMapViewAndGet();
        if (mapView != null) {
            finishMapSetup();
        } else {
            throw new NullPointerException("mapView is null!");
        }
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

    private void finishMapSetup() {
        setPerspectiveParameters();
        addZoomControls();
        addOverlays();
        layout = this.setUpLayoutAndGet();
        layout.addView(mapView);
    }

    protected void setZoom(double zoom) {
        mapView.getController().setZoom(zoom);
    }

    protected void setCenterCoordinates(double lat, double lon) {
        GeoPoint centerPoint = new GeoPoint(lat, lon);
        mapView.getController().setCenter(centerPoint);
    }

    private void addZoomControls() {
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);
        mapView.setMultiTouchControls(true);
    }
}
