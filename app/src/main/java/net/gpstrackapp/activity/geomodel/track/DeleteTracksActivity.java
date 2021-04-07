package net.gpstrackapp.activity.geomodel.track;

import android.widget.Toast;

import net.gpstrackapp.GPSComponent;
import net.gpstrackapp.activity.geomodel.GeoModelListSelectionActivity;
import net.gpstrackapp.geomodel.RequestGeoModelsCommand;
import net.gpstrackapp.geomodel.track.RequestTracksCommand;
import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.geomodel.track.TrackModelManager;

import java.util.Set;

public class DeleteTracksActivity extends GeoModelListSelectionActivity {
    private final TrackModelManager trackModelManager = GPSComponent.getGPSComponent().getTrackModelManager();

    @Override
    protected void onSelectionFinished(Set<CharSequence> selectedItemIDs) {
        Set<Track> selectedTracks = trackModelManager.getGeoModelsByUUIDs(selectedItemIDs);
        trackModelManager.deleteGeoModelsFromFiles(this, selectedTracks);
        trackModelManager.removeGeoModelsByUUIDs(selectedItemIDs);
        Toast.makeText(this, "The Tracks have been successfully deleted.", Toast.LENGTH_SHORT).show();
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
    public String addUserDescription() {
        return "This will also delete the tracks from local storage!";
    }
}
