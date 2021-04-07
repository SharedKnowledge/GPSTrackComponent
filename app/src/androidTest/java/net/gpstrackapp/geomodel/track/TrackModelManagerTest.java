package net.gpstrackapp.geomodel.track;

import android.content.Context;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import org.junit.BeforeClass;
import org.junit.Test;
import org.osmdroid.util.GeoPoint;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TrackModelManagerTest {
    private TrackModelManager trackModelManager = new TrackModelManager();
    private static Track track1;
    private static Track track2;
    private static Context ctx;

    @BeforeClass
    public static void setup() {
        ctx = ApplicationProvider.getApplicationContext();
        TrackPoint trackPoint1_1_1 = new TrackPoint(new GeoPoint(80.0, 63.0), LocalDateTime.of(2021, 1, 1, 0, 10));
        TrackPoint trackPoint1_1_2 = new TrackPoint(new GeoPoint(35.0, -27.0), LocalDateTime.of(2021, 1, 1, 0, 11));
        TrackSegment trackSegment1_1 = new TrackSegment(new ArrayList<>(Arrays.asList(trackPoint1_1_1, trackPoint1_1_2)));

        TrackPoint trackPoint1_2_1 = new TrackPoint(new GeoPoint(56.0, -142.0), LocalDateTime.of(2021, 1, 1, 0, 20));
        TrackPoint trackPoint1_2_2 = new TrackPoint(new GeoPoint(-83.0, 61.0), LocalDateTime.of(2021, 1, 1, 0, 21));
        TrackSegment trackSegment1_2 = new TrackSegment(new ArrayList<>(Arrays.asList(trackPoint1_2_1, trackPoint1_2_2)));

        track1 = new Track(null, "track1", null, LocalDateTime.now(), new ArrayList<>(Arrays.asList(trackSegment1_1, trackSegment1_2)));

        TrackPoint trackPoint2_1_1 = new TrackPoint(new GeoPoint(72.0, 39.0), LocalDateTime.of(2021, 1, 2, 0, 5));
        TrackPoint trackPoint2_1_2 = new TrackPoint(new GeoPoint(74.0, -127.0), LocalDateTime.of(2021, 1, 2, 0, 6));
        TrackSegment trackSegment2_1 = new TrackSegment(new ArrayList<>(Arrays.asList(trackPoint2_1_1, trackPoint2_1_2)));

        track2 = new Track(null, null, null, null, new ArrayList<>(Arrays.asList(trackSegment2_1)));
    }

    @Test
    public void saveAndLoadAndDeleteForValidTracksShouldWork() {
        int savedTracksCount = trackModelManager.getAll().size();
        Log.d("TrackModelManagerTest", "" + savedTracksCount);
        trackModelManager.saveGeoModelsToFiles(ctx, new HashSet<>(Arrays.asList(track1, track2)));

        trackModelManager.loadAllGeoModelsFromFiles(ctx);
        // make sure tracks were saved and loaded, 2 new tracks should be now in trackModelManager
        assertEquals(savedTracksCount + 2, trackModelManager.getAll().size());

        Track track1New = trackModelManager.getGeoModelByUUID(track1.getObjectID());
        Track track2New = trackModelManager.getGeoModelByUUID(track2.getObjectID());

        if (track1New == null || track2New == null) {
            fail("A track could not be found in trackModelManager after loading from file storage");
        }

        assertEquals(track1New.getObjectName(), track1.getObjectName());
        assertEquals(track1New.getCreator(), track1.getCreator());
        assertEquals(track1New.getDateOfCreation(), track1.getDateOfCreation());
        assertEquals(track1New.getTrackSegments().size(), track1.getTrackSegments().size());
        for (int i = 0; i < track1New.getTrackSegments().size(); i++) {
            TrackSegment trackSegment = track1.getTrackSegments().get(i);
            TrackSegment trackSegmentNew = track1New.getTrackSegments().get(i);

            for (int j = 0; j < trackSegmentNew.getTrackPoints().size(); j++) {
                TrackPoint trackPoint = trackSegment.getTrackPoints().get(j);
                TrackPoint trackPointNew = trackSegment.getTrackPoints().get(j);
                assertEquals(trackPoint.getGeoPoint().getLatitude(), trackPointNew.getGeoPoint().getLatitude(), 0.001);
                assertEquals(trackPoint.getGeoPoint().getLongitude(), trackPointNew.getGeoPoint().getLongitude(), 0.001);
                assertEquals(trackPoint.getGeoPoint().getAltitude(), trackPointNew.getGeoPoint().getAltitude(), 0.001);
                assertEquals(trackPoint.getDate(), trackPointNew.getDate());
            }
        }

        assertEquals(track2New.getObjectName(), track2.getObjectName());
        assertEquals(track2New.getCreator(), track2.getCreator());
        assertEquals(track2New.getDateOfCreation(), track2.getDateOfCreation());
        assertEquals(track2New.getTrackSegments().size(), track2.getTrackSegments().size());
        for (int i = 0; i < track2New.getTrackSegments().size(); i++) {
            TrackSegment trackSegment = track2.getTrackSegments().get(i);
            TrackSegment trackSegmentNew = track2New.getTrackSegments().get(i);

            for (int j = 0; j < trackSegmentNew.getTrackPoints().size(); j++) {
                TrackPoint trackPoint = trackSegment.getTrackPoints().get(j);
                TrackPoint trackPointNew = trackSegment.getTrackPoints().get(j);
                assertEquals(trackPoint.getGeoPoint().getLatitude(), trackPointNew.getGeoPoint().getLatitude(), 0.001);
                assertEquals(trackPoint.getGeoPoint().getLongitude(), trackPointNew.getGeoPoint().getLongitude(), 0.001);
                assertEquals(trackPoint.getGeoPoint().getAltitude(), trackPointNew.getGeoPoint().getAltitude(), 0.001);
                assertEquals(trackPoint.getDate(), trackPointNew.getDate());
            }
        }

        trackModelManager.deleteGeoModelsFromFiles(ctx, new HashSet<>(Arrays.asList(track1New, track2New)));
        // this simulates what happens in DeleteTracksActivity
        trackModelManager.removeGeoModelsByUUIDs(new HashSet<>(Arrays.asList(track1New.getObjectID(), track2New.getObjectID())));
        trackModelManager.loadAllGeoModelsFromFiles(ctx);
        assertEquals(savedTracksCount, trackModelManager.getAll().size());
    }

    @Test
    public void savingForNullTracksShouldReturnFalse() {
        assertFalse(trackModelManager.saveGeoModelsToFiles(ctx, null));

        track2 = null;
        assertFalse(trackModelManager.saveGeoModelsToFiles(ctx, new HashSet<>(Arrays.asList(track1, track2))));
    }

    @Test
    public void deletingForNullTracksShouldReturnFalse() {
        assertFalse(trackModelManager.deleteGeoModelsFromFiles(ctx, null));

        track2 = null;
        assertFalse(trackModelManager.deleteGeoModelsFromFiles(ctx, new HashSet<>(Arrays.asList(track1, track2))));
    }

    @Test
    public void savingAndDeletingForNullTrackSegmentShouldReturnTrue() {
        track1.addTrackSegment(null);
        assertTrue(trackModelManager.saveGeoModelsToFiles(ctx, new HashSet<>(Arrays.asList(track1, track2))));
        assertTrue(trackModelManager.deleteGeoModelsFromFiles(ctx, new HashSet<>(Arrays.asList(track1, track2))));
    }
}