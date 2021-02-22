package net.gpstrackapp.geomodel.track;

import net.gpstrackapp.ConfiguredMapView;
import net.gpstrackapp.GPSComponent;
import net.gpstrackapp.geomodel.GeoModelManager;
import net.gpstrackapp.geomodel.GeoModelVisualizer;
import net.gpstrackapp.overlay.TrackOverlay;

public class TrackVisualizer extends GeoModelVisualizer<Track, TrackOverlay> {
    private TrackModelManager trackModelManager;

    public TrackVisualizer(ConfiguredMapView mapView) {
        super(mapView);
        this.trackModelManager = GPSComponent.getGPSComponent().getTrackModelManager();
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
