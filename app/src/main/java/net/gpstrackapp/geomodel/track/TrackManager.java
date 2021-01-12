package net.gpstrackapp.geomodel.track;

import net.gpstrackapp.MyMapView;
import net.gpstrackapp.overlay.TrackOverlay;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TrackManager extends GeoModelManager<Track, TrackOverlay> {
    private static List<Track> tracks = new ArrayList<>();

    public TrackManager(MyMapView mapView) {
        super(mapView);
    }

    public static Track createTrack(CharSequence objectName, Date dateOfCreation, List<TrackPoint> trackPoints) {
        Track track = new Track(objectName, dateOfCreation, trackPoints);
        TrackManager.tracks.add(track);
        return track;
    }

    public static boolean destroyTrack(CharSequence uuid) {
        Track track = TrackManager.getTrackByUUID(uuid);
        if (track != null) {
            return TrackManager.tracks.remove(track);
        } else {
            return false;
        }
    }

    public static Track getTrackByUUID(CharSequence uuid) {
        for (int i = 0; i < tracks.size(); i++) {
            if (TrackManager.tracks.get(i).getObjectId().equals(uuid)) {
                return TrackManager.tracks.get(i);
            }
        }
        return null;
    }

    public static Track getTrackByPosition(int position) {
        return TrackManager.tracks.get(position);
    }

    public static List<Track> getAllTracks() {
        return TrackManager.tracks;
    }

    public static int getNumberOfTracks() {
        return TrackManager.tracks.size();
    }

    @Override
    protected TrackOverlay createGeoModelOverlay(Track track) {
        return new TrackOverlay(track.getGeoPoints());
    }

    @Override
    protected Track getGeoModelByUUID(CharSequence uuid) {
        return TrackManager.getTrackByUUID(uuid);
    }
}
