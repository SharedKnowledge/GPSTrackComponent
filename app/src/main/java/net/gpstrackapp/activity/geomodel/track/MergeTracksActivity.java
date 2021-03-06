package net.gpstrackapp.activity.geomodel.track;

import android.support.v7.app.AlertDialog;
import android.widget.EditText;
import android.widget.Toast;

import net.gpstrackapp.GPSComponent;
import net.gpstrackapp.activity.geomodel.GeoModelListSelectionActivity;
import net.gpstrackapp.geomodel.RequestGeoModelsCommand;
import net.gpstrackapp.geomodel.track.RequestTracksCommand;
import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.geomodel.track.TrackModelManager;

import java.util.Set;

public class MergeTracksActivity extends GeoModelListSelectionActivity {
    private final TrackModelManager trackModelManager = GPSComponent.getTrackModelManager();

    @Override
    protected void onSelectionFinished(Set<CharSequence> selectedItemIDs) {
        if (selectedItemIDs.size() > 1) {
            showTrackNameDialog(selectedItemIDs);
        } else {
            finish();
        }
    }

    private void showTrackNameDialog(Set<CharSequence> selectedItemIDs) {
        final EditText input = new EditText(this);
        input.setSelectAllOnFocus(true);

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setView(input)
                .setTitle("Choose a name for the merged track")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String trackName = input.getText().toString();
                    mergeTracks(selectedItemIDs, trackName);
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                .create();
       alertDialog.show();
    }

    private void mergeTracks(Set<CharSequence> selectedItemIDs, String trackName) {
        Set<Track> selectedTracks = trackModelManager.getGeoModelsByUUIDs(selectedItemIDs);
        String ownerName = GPSComponent.getGPSComponent().getASAPApplication().getOwnerName().toString();
        trackModelManager.mergeTracks(selectedTracks, trackName, ownerName);
        Toast.makeText(this, "The Tracks have been successfully merged.", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected RequestGeoModelsCommand createRequestGeoModelsCommand() {
        return new RequestTracksCommand();
    }

    @Override
    public String setActionText() {
        return "Merge tracks";
    }

    @Override
    public String addUserDescription() {
        return "A new track with all the segments from the selected tracks will be created. The selected tracks will not be deleted.";
    }
}
