package net.gpstrackapp;

import org.osmdroid.util.GeoPoint;

import java.util.Date;

public class TrackPoint {
    private GeoPoint geoPoint;
    private Date date;

    public TrackPoint(GeoPoint geoPoint, Date date) {
        this.geoPoint = geoPoint;
        this.date = date;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public Date getDate() {
        return date;
    }
}
