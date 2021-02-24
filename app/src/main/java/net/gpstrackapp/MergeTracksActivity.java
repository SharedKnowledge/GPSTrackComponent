package net.gpstrackapp;

import android.support.v7.app.AlertDialog;
import android.widget.EditText;
import android.widget.Toast;

import net.gpstrackapp.geomodel.RequestGeoModelsCommand;
import net.gpstrackapp.geomodel.track.RequestTracksCommand;
import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.geomodel.track.TrackModelManager;
import net.gpstrackapp.geomodel.track.TrackSegment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MergeTracksActivity extends GeoModelListSelectionActivity {
    private TrackModelManager trackModelManager = GPSComponent.getGPSComponent().getTrackModelManager();

    @Override
    protected void onSelectionFinished(Set<CharSequence> selectedItemIDs) {
        if (selectedItemIDs.size() > 1) {
            showTrackNameDialog(selectedItemIDs);
        } else {
            finish();
        }
    }

    private void showTrackNameDialog(Set<CharSequence> selectedItemIDs) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose a file name");

        final EditText input = new EditText(this);
        input.setSelectAllOnFocus(true);

        builder.setView(input);
        builder.setPositiveButton("OK", (dialog, which) -> {
            String trackName = input.getText().toString();
            mergeTracks(selectedItemIDs, trackName);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void mergeTracks(Set<CharSequence> selectedItemIDs, String trackName) {
        Set<Track> selectedTracks = trackModelManager.getGeoModelsByUUIDs(selectedItemIDs);
        List<TrackSegment> trackSegments = selectedTracks.stream()
                .flatMap(track -> track.getTrackSegments().stream())
                .collect(Collectors.toList());
        Track track = new Track(null, trackName,
                GPSComponent.getGPSComponent().getASAPApplication().getOwnerName(),
                LocalDateTime.now(), trackSegments);
        trackModelManager.addGeoModel(track);
        Toast.makeText(this, "Merging was successful.", Toast.LENGTH_SHORT).show();
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
    public String setOptionalAdditionalInfo() {
        return "A new track with all the segments from the selected tracks will be created. The selected tracks will not be deleted.";
    }
}
