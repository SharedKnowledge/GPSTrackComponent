package net.gpstrackapp.overlay;

import android.graphics.Color;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

//TODO schreiben: Decorator-Pattern fuer getComponentsToDisplay(), da es die gleiche Methode seiner Superklasse aufruft
//TODO schreiben: leider ist TrackMapOverlay an eine Map gebunden (durch Marker), muss neues erstellen um es einer anderen Map hinzuzufuegen
public class TrackMapOverlay extends PolyLineMapOverlay {
    private Marker start, end;
    private PolyLineMapOverlay line;

    public TrackMapOverlay(PolyLineMapOverlay line, MapView mapView) {
        super(line.getLinePoints());
        this.line = line;
        List<GeoPoint> trackPoints = line.getLinePoints();
        this.start = createStart(trackPoints.get(0), mapView);
        this.end = createEnd(trackPoints.get(trackPoints.size() - 1), mapView);
    }

    private Marker createStart(GeoPoint startPoint, MapView mapView) {
        Marker marker = new Marker(mapView);
        marker.setPosition(startPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTextLabelForegroundColor(Color.GREEN);
        marker.setTextIcon("Start");
        return marker;
    }

    private Marker createEnd(GeoPoint endPoint, MapView mapView) {
        Marker marker = new Marker(mapView);
        marker.setPosition(endPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTextLabelForegroundColor(Color.RED);
        marker.setTextIcon("End");
        return marker;
    }

    public Marker getStart() {
        return start;
    }

    public Marker getEnd() {
        return end;
    }

    public List<Overlay> getComponentsToDisplay() {
        List<Overlay> overlays = new ArrayList<>();
        overlays.add(getStart());
        overlays.add(getEnd());
        overlays.addAll(line.getComponentsToDisplay());
        return overlays;
    }
}
