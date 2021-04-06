package net.gpstrackapp.format;

import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.geomodel.track.TrackModelManager;
import net.gpstrackapp.geomodel.track.TrackPoint;
import net.gpstrackapp.geomodel.track.TrackSegment;

import org.junit.Test;
import org.osmdroid.util.GeoPoint;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import io.jenetics.jpx.GPX;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class GPXFileFormatTest {
    private static TrackModelManager trackModelManager = new TrackModelManager();
    private String appName = "appName";
    private String ownerName = "creator1";

    @Test
    public void conversionToAndFromGPXShouldReturnInput() {
        TrackPoint trackPoint1_1_1 = new TrackPoint(new GeoPoint(80.0, 63.0), LocalDateTime.of(2021, 1, 1, 0, 10));
        TrackPoint trackPoint1_1_2 = new TrackPoint(new GeoPoint(35.0, -27.0), LocalDateTime.of(2021, 1, 1, 0, 11));
        TrackSegment trackSegment1_1 = new TrackSegment(new ArrayList<>(Arrays.asList(trackPoint1_1_1, trackPoint1_1_2)));

        TrackPoint trackPoint1_2_1 = new TrackPoint(new GeoPoint(56.0, -142.0), LocalDateTime.of(2021, 1, 1, 0, 20));
        TrackPoint trackPoint1_2_2 = new TrackPoint(new GeoPoint(-83.0, 61.0), LocalDateTime.of(2021, 1, 1, 0, 21));
        TrackSegment trackSegment1_2 = new TrackSegment(new ArrayList<>(Arrays.asList(trackPoint1_2_1, trackPoint1_2_2)));

        Track track1 = new Track(null, "track1", ownerName, LocalDateTime.now(), new ArrayList<>(Arrays.asList(trackSegment1_1, trackSegment1_2)));

        TrackPoint trackPoint2_1_1 = new TrackPoint(new GeoPoint(72.0, 39.0), LocalDateTime.of(2021, 1, 2, 0, 5));
        TrackPoint trackPoint2_1_2 = new TrackPoint(new GeoPoint(74.0, -127.0), LocalDateTime.of(2021, 1, 2, 0, 6));
        TrackSegment trackSegment2_1 = new TrackSegment(new ArrayList<>(Arrays.asList(trackPoint2_1_1, trackPoint2_1_2)));

        Track track2 = new Track(null, null, null, null, new ArrayList<>(Arrays.asList(trackSegment2_1)));

        GPXFileFormat gpxFileFormat = new GPXFileFormat(trackModelManager);
        GPX gpxFile = gpxFileFormat.generateGPX(new HashSet<>(Arrays.asList(track1, track2)), "gpxFile", appName, ownerName);

        gpxFileFormat.parseObjectsFromGPX(gpxFile, appName);
        List<Track> trackList = trackModelManager.getAll();
        assertEquals(trackList.size(), 2);

        Track track1New = null;
        Track track2New = null;
        /* Tracks get new UUIDs on import and the order of import is not always the same so instead of searching in trackModelManager
            by UUID use the objectName instead. Obviously this only works if all tracks have a different name!
         */
        for (Track track : trackList) {
            if (track.getObjectName().equals(track1.getObjectName())) {
                track1New = track;
            } else {
                track2New = track;
            }
        }
        if (track1New == null || track2New == null) {
            fail("A track could not be found in trackModelManager after parsing from GPX");
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
    }
}