package net.gpstrackapp.geomodel.track;

import android.location.Location;

import net.gpstrackapp.GPSComponent;
import net.gpstrackapp.geomodel.GeoModel;
import net.gpstrackapp.geomodel.ILocationConsumer;

import org.osmdroid.util.GeoPoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Track extends GeoModel implements ILocationConsumer, Serializable {
    private List<TrackPoint> trackPoints = new ArrayList<>();

    public Track(CharSequence objectID, CharSequence objectName, CharSequence creator, Date dateOfCreation, List<TrackPoint> trackPoints) {
        super(objectID, objectName, creator, dateOfCreation);
        if (trackPoints != null) {
            this.trackPoints.addAll(trackPoints);
        }
    }

    public void addTrackPoint(TrackPoint trackPoint) {
        this.trackPoints.add(trackPoint);
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

    @Override
    public void onLocationChanged(Location location) {
        GeoPoint geoPoint = new GeoPoint(
                location.getLatitude(),
                location.getLongitude(),
                location.getAltitude());
        Date date = new Date(location.getTime());
        TrackPoint trackPoint = new TrackPoint(geoPoint, date);
        addTrackPoint(trackPoint);
    }
}
