package net.gpstrackapp.geomodel.track;

import android.location.Location;

import net.gpstrackapp.GPSComponent;
import net.gpstrackapp.geomodel.GeoModel;
import net.gpstrackapp.overlay.TrackOverlay;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Track extends GeoModel implements IMyLocationConsumer {
    private List<TrackPoint> trackPoints;

    public Track(List<TrackPoint> trackPoints, CharSequence objectName, Date dateOfCreation) {
        super(objectName, dateOfCreation, GPSComponent.getGPSComponent().getASAPApplication().getOwnerName());
        this.trackPoints = trackPoints;
    }

    public Track(List<TrackPoint> trackPoints, CharSequence objectName) {
        this(trackPoints, objectName, null);
    }

    public Track(CharSequence objectName) {
        this(new ArrayList<TrackPoint>(), objectName, null);
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
    public void onLocationChanged(Location location, IMyLocationProvider source) {
        GeoPoint geoPoint = new GeoPoint(
                location.getLatitude(),
                location.getLongitude(),
                location.getAltitude());
        Date date = new Date(location.getTime());
        TrackPoint trackPoint = new TrackPoint(geoPoint, date);
        addTrackPoint(trackPoint);
    }
}
