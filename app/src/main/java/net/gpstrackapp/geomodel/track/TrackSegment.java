package net.gpstrackapp.geomodel.track;

import org.osmdroid.util.GeoPoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TrackSegment implements Serializable {
    private static final long serialVersionUID = 0;

    List<TrackPoint> trackPoints = new ArrayList<>();

    public TrackSegment(List<TrackPoint> trackPoints) {
        if (trackPoints != null) {
            this.trackPoints.addAll(trackPoints);
        }
    }

    public List<TrackPoint> getTrackPoints() {
        return trackPoints;
    }

    public List<GeoPoint> getGeoPoints() {
        List<GeoPoint> geoPoints = new ArrayList<>();
        for (int i = 0; i < trackPoints.size(); i++) {
            geoPoints.add(trackPoints.get(i).getGeoPoint());
        }
        return geoPoints;
    }

    public void addTrackPoint(TrackPoint trackPoint) {
        this.trackPoints.add(trackPoint);
    }
}
