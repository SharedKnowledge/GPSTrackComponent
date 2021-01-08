package net.gpstrackapp.geomodel.track;

import net.gpstrackapp.overlay.TrackOverlay;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class TrackManager {
    private static List<Track> tracks = new ArrayList<>();

    public static Track createTrack(CharSequence objectName, Date dateOfCreation, List<TrackPoint> trackPoints) {
        Track track = new Track(objectName, dateOfCreation, trackPoints);
        tracks.add(track);
        return track;
    }

    public static boolean destroyTrack(String objectID) {
        Track track = getTrackByID(objectID);
        if (track != null) {
            return tracks.remove(track);
        } else {
            return false;
        }
    }

    public static Track getTrackByID(String objectID) {
        for (int i = 0; i < tracks.size(); i++) {
            if (tracks.get(i).getObjectId().equals(objectID)) {
                return tracks.get(i);
            }
        }
        return null;
    }

    public static Track getTrackByPosition(int position) {
        return tracks.get(position);
    }

    public static List<Track> getAllTracks() {
        return tracks;
    }

    public static int getNumberOfTracks() {
        return tracks.size();
    }
}
