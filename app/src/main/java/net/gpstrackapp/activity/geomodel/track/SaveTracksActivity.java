package net.gpstrackapp.activity.geomodel.track;

import android.widget.Toast;

import net.gpstrackapp.GPSComponent;
import net.gpstrackapp.activity.geomodel.GeoModelListSelectionActivity;
import net.gpstrackapp.geomodel.RequestGeoModelsCommand;
import net.gpstrackapp.geomodel.track.RequestTracksCommand;
import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.geomodel.track.TrackModelManager;

import java.util.Set;

public class SaveTracksActivity extends GeoModelListSelectionActivity {
    private final TrackModelManager trackModelManager = GPSComponent.getGPSComponent().getTrackModelManager();

    @Override
    protected void onSelectionFinished(Set<CharSequence> selectedItemIDs) {
        Set<Track> selectedTracks = trackModelManager.getGeoModelsByUUIDs(selectedItemIDs);
        if (trackModelManager.saveGeoModelsToFiles(this, selectedTracks)) {
            Toast.makeText(this, "The Tracks have been successfully saved.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "At least one track could not be saved.", Toast.LENGTH_SHORT).show();
        }
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
    public String addUserDescription() {
        return "All selected tracks will be saved to local storage.";
    }
}
