package net.gpstrackapp.geomodel.track;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class TrackManager {
    private static List<Track> tracks = new ArrayList<>();

    public static Track createTrack(CharSequence objectName, Date dateOfCreation, List<TrackPoint> trackPoints) {
        Track track = new Track(objectName, dateOfCreation, trackPoints);
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

    public static List<Track> getAllTracks() {
        return tracks;
    }
}
