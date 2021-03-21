package net.gpstrackapp.geomodel.track;

import net.gpstrackapp.GPSComponent;
import net.gpstrackapp.geomodel.GeoModelVisualizer;
import net.gpstrackapp.overlay.ConfiguredMapView;
import net.gpstrackapp.overlay.TrackOverlay;

public class TrackVisualizer extends GeoModelVisualizer<Track, TrackOverlay> {
    private TrackModelManager trackModelManager = GPSComponent.getGPSComponent().getTrackModelManager();

    public TrackVisualizer(ConfiguredMapView mapView) {
        super(mapView);
    }

    @Override
    protected TrackOverlay createGeoModelOverlay(Track track, ConfiguredMapView mapView) {
        TrackOverlay trackOverlay = new TrackOverlay(track);
        trackOverlay.initializeComponents(mapView);
        return trackOverlay;
    }

    @Override
    protected Track getGeoModelByUUID(CharSequence uuid) {
        return trackModelManager.getGeoModelByUUID(uuid);
    }
}
