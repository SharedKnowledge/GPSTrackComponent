package net.gpstrackapp.overlay;

import android.graphics.Color;
import android.widget.Toast;

import net.gpstrackapp.geomodel.GeoModel;
import net.gpstrackapp.geomodel.track.Track;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.List;

//TODO Interface, von dem diese Klasse ableitet waere gut
public class TrackOverlay extends FolderOverlay {
    private Marker start, end;
    private Polyline line;

    public TrackOverlay(List<GeoPoint> geoPoints) {
        this.line = createLine(geoPoints);
        this.add(line);
    }

    public void addStartEndMarkers(MapView mapView) {
        List<GeoPoint> geoPoints = line.getPoints();
        this.start = createStart(geoPoints.get(0), mapView);
        this.end = createEnd(geoPoints.get(geoPoints.size() - 1), mapView);
        this.add(start);
        this.add(end);
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

    public Polyline getLine() {
        return line;
    }

    public Marker getStart() {
        return start;
    }

    public Marker getEnd() {
        return end;
    }
}
