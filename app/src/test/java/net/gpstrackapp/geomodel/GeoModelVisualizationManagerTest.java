package net.gpstrackapp.geomodel;

import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.geomodel.track.TrackModelManager;
import net.gpstrackapp.geomodel.track.TrackSegment;
import net.gpstrackapp.geomodel.track.TrackVisualizationManager;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class GeoModelVisualizationManagerTest {
    private static TrackModelManager trackModelManager = new TrackModelManager();
    private static TrackVisualizationManager trackVisualizationManager;
    private static Track track1 = new Track(null, "track1", "user1", LocalDateTime.now(), new TrackSegment(null));
    private static Track track2 = new Track(null, "track2", "user1", LocalDateTime.now(), new TrackSegment(null));
    private static Track track3 = new Track(null, "track3", "user1", LocalDateTime.now(), new TrackSegment(null));
    private static Track trackNotInManager = new Track(null, "trackNotInManager", "user1", LocalDateTime.now(), new TrackSegment(null));

    @BeforeClass
    public static void setup() {
        trackModelManager.addGeoModel(track1);
        trackModelManager.addGeoModel(track2);
        trackModelManager.addGeoModel(track3);
    }

    @Before
    public void initVisualizationManager() {
        trackVisualizationManager = new TrackVisualizationManager(trackModelManager);
    }

    @Test
    public void updateGeoModelHolderForExistingGeoModelsInManager() {
        // simulates that user selects two tracks to be displayed
        Set<CharSequence> selectedTrackIDs = new HashSet<>(Arrays.asList(track1.getObjectID(), track2.getObjectID()));
        trackVisualizationManager.setSelectedItemIDs(selectedTrackIDs);

        trackVisualizationManager.updateGeoModelHolder();
        // two tracks should be added
        assertEquals(trackVisualizationManager.getGeoModelOverlaysToAddToMap().size(), 2);
        // no tracks should be removed
        assertEquals(trackVisualizationManager.getGeoModelOverlaysToRemoveFromMap().size(), 0);


        // display track1 again, add track3 and remove track2 as it is not selected again
        selectedTrackIDs = new HashSet<>(Arrays.asList(track1.getObjectID(), track3.getObjectID()));
        trackVisualizationManager.setSelectedItemIDs(selectedTrackIDs);

        trackVisualizationManager.updateGeoModelHolder();
        // track3 should be added
        assertEquals(trackVisualizationManager.getGeoModelOverlaysToAddToMap().size(), 1);
        // track2 should be removed
        assertEquals(trackVisualizationManager.getGeoModelOverlaysToRemoveFromMap().size(), 1);
    }


    @Test
    public void updateGeoModelHolderForNotExistingGeoModelInManager() {
        // simulates that user selects two tracks to be displayed, one is not in the manager (it could have been deleted)
        Set<CharSequence> selectedTrackIDs = new HashSet<>(Arrays.asList(track1.getObjectID(), trackNotInManager.getObjectID()));
        trackVisualizationManager.setSelectedItemIDs(selectedTrackIDs);

        trackVisualizationManager.updateGeoModelHolder();
        // only one track should be added as the other does not exist in the manager
        assertEquals(trackVisualizationManager.getGeoModelOverlaysToAddToMap().size(), 1);
        // no tracks should be removed
        assertEquals(trackVisualizationManager.getGeoModelOverlaysToRemoveFromMap().size(), 0);

        // display track1 again, add track2 and try to remove the track that was not displayed in the first place (should not happen)
        selectedTrackIDs = new HashSet<>(Arrays.asList(track1.getObjectID(), track2.getObjectID()));
        trackVisualizationManager.setSelectedItemIDs(selectedTrackIDs);

        trackVisualizationManager.updateGeoModelHolder();
        // track2 should be added
        assertEquals(trackVisualizationManager.getGeoModelOverlaysToAddToMap().size(), 1);
        // no tracks should be removed
        assertEquals(trackVisualizationManager.getGeoModelOverlaysToRemoveFromMap().size(), 0);
    }

}