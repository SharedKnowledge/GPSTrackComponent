package net.gpstrackapp.overlay;

import net.gpstrackapp.geomodel.track.Track;

public class HideTrackCommand implements HideGeoModelCommand {
    private TrackDisplayer trackDisplayer;
    private Track track;

    public HideTrackCommand(TrackDisplayer trackDisplayer, Track track) {
        this.trackDisplayer = trackDisplayer;
        this.track = track;
    }

    @Override
    public boolean execute() {
        return trackDisplayer.removeTrackFromMap(track);
    }
}
