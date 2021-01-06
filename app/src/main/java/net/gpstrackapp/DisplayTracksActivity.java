package net.gpstrackapp;

import android.content.Intent;
import android.os.Bundle;

import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.geomodel.track.TrackManager;
import net.gpstrackapp.overlay.DisplayTrackCommand;
import net.gpstrackapp.overlay.HideTrackCommand;
import net.gpstrackapp.overlay.TrackDisplayer;

import java.util.Iterator;
import java.util.Set;

public class DisplayTracksActivity extends MapObjectListSelectionActivity {
    private ReusableTrackMapView trackDisplayer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.trackDisplayer = GPSComponent.getGPSComponent().getAttributeContainer().getReusableTrackMapView();
    }

    @Override
    protected void onSelectionFinished(Set<String> selectedItemIDs) {
        Set<Track> displayedTracks = trackDisplayer.getTracksWithOverlaysHolder().keySet();

        // Set that stores the Tracks that are still displayed
        Set<Track> tracksToRemoveFromMap = displayedTracks;

        Iterator<String> iterSelect = selectedItemIDs.iterator();
        while (iterSelect.hasNext()) {
            String itemID = iterSelect.next();
            // add every Track that is not yet displayed to the map
            if (!displayedTracks.contains(itemID)) {
                Track track = TrackManager.getTrackByID(itemID);
                DisplayTrackCommand displayTrackCommand = new DisplayTrackCommand(trackDisplayer, track);
                displayTrackCommand.execute();
            }
            // remove all Tracks that have to get displayed again from the Set
            tracksToRemoveFromMap.remove(TrackManager.getTrackByID(itemID));
        }

        // for every Track that is still displayed and has to be removed create a hideTrackCommand
        Iterator<Track> iterRemove = tracksToRemoveFromMap.iterator();
        while (iterRemove.hasNext()) {
            Track trackToRemove = iterRemove.next();
            HideTrackCommand hideTrackCommand = new HideTrackCommand(trackDisplayer, trackToRemove);
            hideTrackCommand.execute();
        }

        Intent intent = new Intent(this, MainMapActivity.class);
        startActivity(intent);
    }
}
