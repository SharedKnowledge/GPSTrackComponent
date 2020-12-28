package net.gpstrackapp;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class TrackModel extends GeoModel {
    private Map<GeoPoint, Date> trackPoints;
    private static TrackMapObject trackMapObject = null;

    public TrackModel(Map<GeoPoint, Date> trackPoints, CharSequence objectName, Date dateOfCreation) {
        super(objectName, dateOfCreation, GPSComponent.getGPSComponent().getASAPApplication().getOwnerName());
        this.trackPoints = trackPoints;
    }

    public TrackModel(Map<GeoPoint, Date> trackPoints, CharSequence objectName) {
        this(trackPoints, objectName, null);
    }

    public Map<GeoPoint, Date> getTrackPointsWithDates() {
        return trackPoints;
    }

    public List<GeoPoint> getTrackPoints() {
        return new ArrayList<>(trackPoints.keySet());
    }
}
