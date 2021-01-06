package net.gpstrackapp.overlay;

import net.gpstrackapp.geomodel.track.Track;

import java.util.Map;

public interface TrackDisplayer {
    Map<Track, TrackOverlay> getTracksWithOverlaysHolder();

    boolean addTrackToMap(Track track);

    boolean removeTrackFromMap(Track track);
}
