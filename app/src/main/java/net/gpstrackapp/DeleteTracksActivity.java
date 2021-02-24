package net.gpstrackapp;

import android.widget.Toast;

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
        Toast.makeText(this, "Deleting was successful.", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected RequestGeoModelsCommand createRequestGeoModelsCommand() {
        return new RequestTracksCommand();
    }

    @Override
    public String setActionText() {
        return "Delete tracks";
    }

    @Override
    public String setOptionalAdditionalInfo() {
        return "This will also delete the tracks from local storage!";
    }
}
