package net.gpstrackapp.geomodel.track;

import net.gpstrackapp.GPSComponent;
import net.gpstrackapp.geomodel.GeoModelVisualizationManager;
import net.gpstrackapp.overlay.TrackOverlay;

public class TrackVisualizationManager extends GeoModelVisualizationManager<Track, TrackOverlay> {
    private TrackModelManager trackModelManager = GPSComponent.getGPSComponent().getTrackModelManager();

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
