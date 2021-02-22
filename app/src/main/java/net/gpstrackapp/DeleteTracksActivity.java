package net.gpstrackapp;

import net.gpstrackapp.geomodel.RequestGeoModelsCommand;
import net.gpstrackapp.geomodel.track.RequestTracksCommand;
import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.geomodel.track.TrackModelManager;

import java.util.Set;

public class DeleteTracksActivity extends GeoModelListSelectionActivity {
    private TrackModelManager trackModelManager = GPSComponent.getGPSComponent().getTrackModelManager();

    @Override
    protected void onSelectionFinished(Set<CharSequence> selectedItemIDs) {
        Set<Track> selectedTracks = trackModelManager.getGeoModelsByUUIDs(selectedItemIDs);
        trackModelManager.deleteGeoModelsFromFiles(this, selectedTracks);
        trackModelManager.removeGeoModelsByUUIDs(selectedItemIDs);
        finish();
    }

    @Override
    protected RequestGeoModelsCommand createRequestGeoModelsCommand() {
        return new RequestTracksCommand();
    }
}
