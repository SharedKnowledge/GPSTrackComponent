package net.gpstrackapp;

import android.support.v7.app.AppCompatActivity;

import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.overlay.DisplayTrackCommand;
import net.gpstrackapp.overlay.HideTrackCommand;
import net.gpstrackapp.overlay.TrackOverlay;

public class DisplayTracksActivity extends MapObjectListActivity {

    private void addTrackToMap(Track track) {
        TrackOverlay trackOverlay = new TrackOverlay(track.getGeoPoints());
        ReusableMapView mapView = ReusableMapView.getInstance(this);
        mapView.getTrackDisplayMap().put(track, trackOverlay);

        DisplayTrackCommand displayTrackCommand = new DisplayTrackCommand(mapView, track);
        displayTrackCommand.addToMap();
    }

    private void removeTrackFromMap(Track track) {
        ReusableMapView mapView = ReusableMapView.getInstance(this);
        mapView.getTrackDisplayMap().remove(track);

        HideTrackCommand hideTrackCommand = new HideTrackCommand(mapView, track);
        hideTrackCommand.removeFromMap();
    }
}
