package net.gpstrackapp.geomodel.track;

import android.location.Location;

import org.osmdroid.util.GeoPoint;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TrackPoint implements Serializable {
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

    public String getDateAsFormattedString() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(getDate());
    }
}
