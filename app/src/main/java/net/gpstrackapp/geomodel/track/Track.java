package net.gpstrackapp.geomodel.track;

import android.location.Location;
import android.util.Log;

import net.gpstrackapp.geomodel.GeoModel;
import net.gpstrackapp.location.ILocationConsumer;

import org.osmdroid.util.GeoPoint;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Track extends GeoModel implements ILocationConsumer, Serializable {
    private static final long serialVersionUID = 0;
    private List<TrackSegment> trackSegments = new ArrayList<>();

    public Track(CharSequence objectID, CharSequence objectName, CharSequence creator, LocalDateTime dateOfCreation, List<TrackSegment> trackSegments) {
        super(objectID, objectName, creator, dateOfCreation);
        if (trackSegments != null) {
            this.trackSegments.addAll(trackSegments);
        } else {
            this.trackSegments.add(new TrackSegment(null));
        }
    }

    public Track(CharSequence objectID, CharSequence objectName, CharSequence creator, LocalDateTime dateOfCreation, TrackSegment trackSegment) {
        this(objectID, objectName, creator, dateOfCreation, Arrays.asList(trackSegment));
    }

    public void addTrackSegment(TrackSegment trackSegment) {
        trackSegments.add(trackSegment);
    }

    public List<TrackSegment> getTrackSegments() {
        return trackSegments;
    }

    public TrackSegment getLastTrackSegment() {
        if (trackSegments.size() > 0) {
            return trackSegments.get(trackSegments.size() - 1);
        } else {
            Log.d(getLogStart(), "Track hat keine Segmente");
            return null;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        GeoPoint geoPoint = new GeoPoint(
                location.getLatitude(),
                location.getLongitude(),
                location.getAltitude());
        LocalDateTime date = Instant.ofEpochMilli(location.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
        TrackPoint trackPoint = new TrackPoint(geoPoint, date);
        getLastTrackSegment().addTrackPoint(trackPoint);
    }

    private String getLogStart() {
        return getClass().getSimpleName();
    }
}
