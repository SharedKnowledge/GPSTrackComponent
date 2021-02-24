package net.gpstrackapp;

import android.widget.Toast;

import net.gpstrackapp.geomodel.RequestGeoModelsCommand;
import net.gpstrackapp.geomodel.track.RequestTracksCommand;
import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.geomodel.track.TrackModelManager;
import net.gpstrackapp.geomodel.track.TrackVisualizer;

import java.util.Set;

public class SaveTracksActivity extends GeoModelListSelectionActivity {
    private TrackModelManager trackModelManager = GPSComponent.getGPSComponent().getTrackModelManager();

    @Override
    protected void onSelectionFinished(Set<CharSequence> selectedItemIDs) {
        Set<Track> selectedTracks = trackModelManager.getGeoModelsByUUIDs(selectedItemIDs);
        trackModelManager.saveGeoModelsToFiles(this, selectedTracks);
        Toast.makeText(this, "Saving was successful.", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected RequestGeoModelsCommand createRequestGeoModelsCommand() {
        return new RequestTracksCommand();
    }

    @Override
    public String setActionText() {
        return "Save tracks";
    }

    @Override
    public String setOptionalAdditionalInfo() {
        return "All selected tracks will be saved to local storage.";
    }
}
