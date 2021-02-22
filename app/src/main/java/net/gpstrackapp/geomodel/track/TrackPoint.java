package net.gpstrackapp.geomodel.track;

import org.osmdroid.util.GeoPoint;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TrackPoint implements Serializable {
    private GeoPoint geoPoint;
    private LocalDateTime date;

    public TrackPoint(GeoPoint geoPoint, LocalDateTime date) {
        this.geoPoint = geoPoint;
        this.date = date;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getDateAsFormattedString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return formatter.format(getDate());
    }
}
