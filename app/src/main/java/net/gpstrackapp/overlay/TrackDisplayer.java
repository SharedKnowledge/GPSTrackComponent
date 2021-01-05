package net.gpstrackapp.overlay;

import net.gpstrackapp.geomodel.track.Track;

public interface TrackDisplayer {
    boolean addTrackToMap(Track track);

    boolean removeTrackFromMap(Track track);
}
