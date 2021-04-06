package net.gpstrackapp.geomodel.track;

import net.gpstrackapp.geomodel.GeoModelVisualizationManager;
import net.gpstrackapp.mapview.TrackOverlay;

public class TrackVisualizationManager extends GeoModelVisualizationManager<Track, TrackOverlay> {
    private TrackModelManager trackModelManager;

    public TrackVisualizationManager(TrackModelManager trackModelManager) {
        this.trackModelManager = trackModelManager;
    }

    @Override
    protected TrackOverlay createGeoModelOverlay(Track track) {
        TrackOverlay trackOverlay = new TrackOverlay(track);
        return trackOverlay;
    }

    @Override
    protected Track getGeoModelByUUID(CharSequence uuid) {
        return trackModelManager.getGeoModelByUUID(uuid);
    }
}
