package net.gpstrackapp.activity.map;

import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.geomodel.track.TrackModelManager;
import net.gpstrackapp.geomodel.track.TrackSegment;
import net.gpstrackapp.mapview.TrackOverlay;

import org.junit.Before;
import org.junit.Test;
import org.osmdroid.views.overlay.Overlay;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class TrackRecordingPresenterTest {
    private static TrackModelManager trackModelManager = new TrackModelManager();
    private static TrackRecordingPresenter trackRecordingPresenter;
    private static int countAddedOverlays, countRemovedOverlays;
    private static Track track1 = new Track(null, "track1", "user1", LocalDateTime.now(), new TrackSegment(null));
    private static Track track2 = new Track(null, "track2", "user1", LocalDateTime.now(), new TrackSegment(null));
    private static Track track3 = new Track(null, "track3", "user1", LocalDateTime.now(), new TrackSegment(null));
    private static List<TrackOverlay> trackOverlayList = new ArrayList<>();

    @Before
    public void setupBeforeEachTest() {
        trackModelManager = new TrackModelManager();
        trackModelManager.addGeoModel(track1);
        trackModelManager.addGeoModel(track2);
        trackModelManager.addGeoModel(track3);

        trackOverlayList.clear();
        trackRecordingPresenter = new TrackRecordingPresenter(new ViewWithOverlays() {
            @Override
            public void addOverlay(Overlay overlay) {
                TrackOverlay trackOverlay = (TrackOverlay) overlay;
                trackOverlayList.add(trackOverlay);
                countAddedOverlays++;
            }

            @Override
            public void removeOverlay(Overlay overlay) {
                TrackOverlay trackOverlay = (TrackOverlay) overlay;
                trackOverlayList.remove(trackOverlay);
                countRemovedOverlays++;
            }
        });
    }

    @Test
    public void selectingTracksShouldUpdateOverlaysProperly() {
        // simulates that user selects two tracks to be displayed
        Set<CharSequence> selectedTrackIDs = new HashSet<>(Arrays.asList(track1.getObjectID(), track2.getObjectID()));
        trackRecordingPresenter.setSelectedItemIDs(selectedTrackIDs);
        trackRecordingPresenter.onCreate();
        trackRecordingPresenter.onStart();
        trackRecordingPresenter.onResume();

        // two tracks should have been added
        assertEquals(2, countAddedOverlays);
        // no tracks should be removed
        assertEquals(0, countRemovedOverlays);
        // two tracks should now be displayed
        assertEquals(2, trackOverlayList.size());
        // this makes sure that only overlays for selected tracks are being displayed
        for (TrackOverlay trackOverlay : trackOverlayList) {
            selectedTrackIDs.remove(trackOverlay.getGeoModel().getObjectID());
        }
        assertEquals(0, selectedTrackIDs.size());

        // simulate that user navigates to DisplayTracksActivity
        trackRecordingPresenter.onPause();
        trackRecordingPresenter.onStop();

        countAddedOverlays = 0;
        countRemovedOverlays = 0;

        // simulate that user deselects track2, but selects track3 this time (track1 is still selected)
        selectedTrackIDs = new HashSet<>(Arrays.asList(track1.getObjectID(), track3.getObjectID()));
        trackRecordingPresenter.setSelectedItemIDs(selectedTrackIDs);

        // user has selected tracks and comes back to the activity with the presenter
        trackRecordingPresenter.onStart();
        trackRecordingPresenter.onResume();

        // track3 should be added
        assertEquals(1, countAddedOverlays);
        // track2 should be removed
        assertEquals(1, countRemovedOverlays);
        // two tracks should now be displayed
        assertEquals(2, trackOverlayList.size());
        // this makes sure that only overlays for selected tracks are being displayed
        for (TrackOverlay trackOverlay : trackOverlayList) {
            selectedTrackIDs.remove(trackOverlay.getGeoModel().getObjectID());
        }
        assertEquals(0, selectedTrackIDs.size());
    }
}