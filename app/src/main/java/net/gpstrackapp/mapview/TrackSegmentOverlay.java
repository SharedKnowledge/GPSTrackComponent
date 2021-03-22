package net.gpstrackapp.mapview;

import android.widget.Toast;

import net.gpstrackapp.geomodel.track.TrackPoint;
import net.gpstrackapp.geomodel.track.TrackSegment;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.List;

public class TrackSegmentOverlay extends FolderOverlay {
    private TrackSegment trackSegment;
    private Marker start, end;
    private Polyline line;

    public TrackSegmentOverlay(TrackSegment trackSegment, String polyLineToastText) {
        this.trackSegment = trackSegment;
        this.line = createLine(trackSegment.getGeoPoints(), polyLineToastText);
        this.add(line);
    }

    public void addStartEndMarkers(MapView mapView) {
        if (!trackSegment.getTrackPoints().isEmpty()) {
            List<GeoPoint> geoPoints = line.getActualPoints();
            start = createStart(trackSegment.getTrackPoints().get(0), mapView);
            end = createEnd(trackSegment.getTrackPoints().get(geoPoints.size() - 1), mapView);
            add(start);
            add(end);
        }
    }

    private Polyline createLine(List<GeoPoint> linePoints, String polyLineToastText) {
        Polyline polyline = new Polyline();
        polyline.setPoints(linePoints);

        polyline.setOnClickListener((polyline1, mapView, eventPos) -> {
            Toast.makeText(mapView.getContext(), polyLineToastText, Toast.LENGTH_LONG).show();
            return false;
        });
        return polyline;
    }

    private Marker createStart(TrackPoint startPoint, MapView mapView) {
        Marker marker = new Marker(mapView);
        marker.setPosition(startPoint.getGeoPoint());
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("Track recording of this segment started at: " + startPoint.getDateAsFormattedString());
        return marker;
    }

    private Marker createEnd(TrackPoint endPoint, MapView mapView) {
        Marker marker = new Marker(mapView);
        marker.setPosition(endPoint.getGeoPoint());
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("Track recording of this segment ended at: " + endPoint.getDateAsFormattedString());
        return marker;
    }
}
