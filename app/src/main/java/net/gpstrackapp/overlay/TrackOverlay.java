package net.gpstrackapp.overlay;

import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.geomodel.track.TrackSegment;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class TrackOverlay extends GeoModelOverlay<Track> {
    private List<TrackSegmentOverlay> trackSegmentOverlays = new ArrayList<>();
    private Track track;

    public TrackOverlay(Track track) {
        super(track);
        this.track = track;

        String polylineToastText = "Track name: " + geoModel.getObjectName() + System.lineSeparator() +
                "Creator: " + geoModel.getCreator() + System.lineSeparator() +
                "Date of creation: " + geoModel.getDateOfCreationAsFormattedString();
        for (TrackSegment trackSegment : track.getTrackSegments()) {
            TrackSegmentOverlay trackSegmentOverlay = new TrackSegmentOverlay(trackSegment, polylineToastText);
            trackSegmentOverlays.add(trackSegmentOverlay);
        }
    }

    public void initializeComponents(MapView mapView) {
        for (TrackSegmentOverlay trackSegmentOverlay : trackSegmentOverlays) {
            trackSegmentOverlay.addStartEndMarkers(mapView);
        }
    }
}
