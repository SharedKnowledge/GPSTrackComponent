package net.gpstrackapp.geomodel.track;

import net.gpstrackapp.GPSComponent;
import net.gpstrackapp.geomodel.GeoModelVisualizationManager;
import net.gpstrackapp.mapview.TrackOverlay;

public class TrackVisualizationManager extends GeoModelVisualizationManager<Track, TrackOverlay> {
    private TrackModelManager trackModelManager = GPSComponent.getTrackModelManager();

    @Override
    protected TrackOverlay createGeoModelOverlay(Track track) {
        return new TrackOverlay(track);
    }

    @Override
    protected Track getGeoModelByUUID(CharSequence uuid) {
        return trackModelManager.getGeoModelByUUID(uuid);
    }
}
