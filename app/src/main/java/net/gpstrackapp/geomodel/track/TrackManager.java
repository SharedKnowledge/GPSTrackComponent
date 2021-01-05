package net.gpstrackapp.geomodel.track;

import java.util.ArrayList;
import java.util.List;

public class TrackManager {
    private static List<Track> tracks = new ArrayList<>();

    public static boolean addTrack(Track track) {
        return tracks.add(track);
    }

    public static boolean removeTrack(Track track) {
        return tracks.remove(track);
    }

    public static List<Track> getTracks() {
        return tracks;
    }
}
