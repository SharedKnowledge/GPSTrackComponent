package net.gpstrackapp;

import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.geomodel.track.TrackManager;

import java.util.Set;

public class SaveTracksActivity extends GeoModelListSelectionActivity {
    @Override
    protected void onSelectionFinished(Set<CharSequence> selectedItemIDs) {
        Set<Track> selectedTracks = TrackManager.getTracksByUUIDs(selectedItemIDs);
        TrackManager.saveTracksToFiles(this, selectedTracks);
        finish();
    }

    @Override
    protected RequestGeoModelsCommand createRequestGeoModelsCommand() {
        return new RequestTracksCommand();
    }
}
