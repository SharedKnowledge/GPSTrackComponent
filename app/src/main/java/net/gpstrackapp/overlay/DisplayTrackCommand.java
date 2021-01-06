package net.gpstrackapp.overlay;

import net.gpstrackapp.geomodel.GeoModel;
import net.gpstrackapp.geomodel.track.Track;

public class DisplayTrackCommand implements DisplayGeoModelCommand {
    private TrackDisplayer trackDisplayer;
    private Track track;

    public DisplayTrackCommand(TrackDisplayer trackDisplayer, Track track) {
        this.trackDisplayer = trackDisplayer;
        this.track = track;
    }

    @Override
    public boolean execute() {
        return trackDisplayer.addTrackToMap(track);
    }
}
