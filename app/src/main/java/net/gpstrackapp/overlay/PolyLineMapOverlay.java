package net.gpstrackapp.overlay;

import android.widget.Toast;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class PolyLineMapOverlay {
    private Polyline line;

    public PolyLineMapOverlay(List<GeoPoint> linePoints) {
        this.line = createLine(linePoints);
    }

    private Polyline createLine(List<GeoPoint> linePoints) {
        Polyline polyline = new Polyline();
        polyline.setPoints(linePoints);
        polyline.setOnClickListener(new Polyline.OnClickListener() {
            @Override
            public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos) {
                Toast.makeText(mapView.getContext(), "polyline with " + polyline.getPoints().size() + "pts was tapped", Toast.LENGTH_LONG).show();
                return false;
            }
        });
        return polyline;
    }

    public Polyline getLine() {
        return line;
    }

    public List<GeoPoint> getLinePoints() {
        return line.getPoints();
    }

    public List<Overlay> getComponentsToDisplay() {
        List<Overlay> overlays = new ArrayList<>();
        overlays.add(getLine());
        return overlays;
    }
}
