package net.gpstrackapp.overlay;

import android.widget.Toast;

import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.geomodel.track.TrackPoint;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.List;

public class TrackOverlay extends GeoModelOverlay<Track> {
    private Marker start, end;
    private Polyline line;
    private Track track;

    public TrackOverlay(Track track) {
        super(track);
        this.track = track;
        this.line = createLine(track.getGeoPoints());
        this.add(line);
    }

    public void addStartEndMarkers(MapView mapView) {
        if (!track.getTrackPoints().isEmpty()) {
            List<GeoPoint> geoPoints = line.getPoints();
            start = createStart(track.getTrackPoints().get(0), mapView);
            end = createEnd(track.getTrackPoints().get(geoPoints.size() - 1), mapView);
            add(start);
            add(end);
        }
    }

    private Polyline createLine(List<GeoPoint> linePoints) {
        Polyline polyline = new Polyline();
        polyline.setPoints(linePoints);

        polyline.setOnClickListener(new Polyline.OnClickListener() {
            @Override
            public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos) {
                Toast.makeText(mapView.getContext(),
                        "Track name: " + geoModel.getObjectName() + System.lineSeparator() +
                                "Creator: " + geoModel.getCreator() + System.lineSeparator() +
                                "Date of creation: " + geoModel.getDateOfCreationAsFormattedString(), Toast.LENGTH_LONG).show();
                return false;
            }
        });
        return polyline;
    }

    private Marker createStart(TrackPoint startPoint, MapView mapView) {
        Marker marker = new Marker(mapView);
        marker.setPosition(startPoint.getGeoPoint());
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("Track recording started at: " + startPoint.getDateAsFormattedString());
        return marker;
    }

    private Marker createEnd(TrackPoint endPoint, MapView mapView) {
        Marker marker = new Marker(mapView);
        marker.setPosition(endPoint.getGeoPoint());
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("Track recording ended at: " + endPoint.getDateAsFormattedString());
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
