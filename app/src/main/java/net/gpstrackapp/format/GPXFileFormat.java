package net.gpstrackapp.format;

import android.content.Context;
import android.util.Log;

import net.gpstrackapp.GPSComponent;
import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.geomodel.track.TrackModelManager;
import net.gpstrackapp.geomodel.track.TrackPoint;
import net.gpstrackapp.geomodel.track.TrackSegment;

import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Latitude;
import io.jenetics.jpx.Length;
import io.jenetics.jpx.Longitude;
import io.jenetics.jpx.Metadata;
import io.jenetics.jpx.WayPoint;

public class GPXFileFormat implements ExportFileFormat, ImportFileFormat {
    private final TrackModelManager trackModelManager = GPSComponent.getGPSComponent().getTrackModelManager();

    // semicolon
    private String delimiter = String.valueOf('\u003B');

    @Override
    public String getMIMEDataType() {
        return "application/gpx+xml";
    }

    @Override
    public String getFileExtensionString() {
        return "gpx";
    }

    @Override
    public void exportToFile(Context ctx, Set<Track> tracksToExport, String fileName, OutputStream outputStream) throws IOException {
        String appName = ctx.getApplicationInfo().loadLabel(ctx.getPackageManager()).toString();
        GPX gpx = generateGPX(tracksToExport, fileName, appName);
        gpx.write(gpx, outputStream);
    }

    private GPX generateGPX(Set<Track> tracksToExport, String fileName, String appName) {
        Metadata metadata = Metadata.builder()
                .author(GPSComponent.getGPSComponent().getASAPApplication().getOwnerName().toString())
                .time(System.currentTimeMillis())
                .name(fileName)
                .build();

        List<io.jenetics.jpx.Track> gpxTracks = new ArrayList<>();
        // iterate over Tracks
        for (Track track : tracksToExport) {
            // TODO can later be done with an extension to gpx that declares additional fields instead of writing it into the description
            // use the description property of track to set creator and date of track
            String desc = "";
            CharSequence creator = track.getCreator();
            desc += creator == null ? "" : creator;
            desc += delimiter;
            LocalDateTime dateOfCreation = track.getDateOfCreation();
            desc += dateOfCreation == null ? "" : dateOfCreation.toString();

            io.jenetics.jpx.Track.Builder trackBuilder = io.jenetics.jpx.Track.builder().name(track.getObjectName().toString()).desc(desc);
            List<TrackSegment> trackSegments = track.getTrackSegments();

            // iterate over TrackSegments
            for (TrackSegment trackSegment : trackSegments) {
                List<WayPoint> wayPoints = new ArrayList<>();

                // iterate over TrackPoints / WayPoints
                for (TrackPoint trackPoint : trackSegment.getTrackPoints()) {
                    GeoPoint geoPoint = trackPoint.getGeoPoint();
                    LocalDateTime date = trackPoint.getDate();
                    ZonedDateTime dateZoned = date != null ? date.atZone(ZoneId.systemDefault()) : null;
                    wayPoints.add(WayPoint.of(
                            Latitude.ofDegrees(geoPoint.getLatitude()),
                            Longitude.ofDegrees(geoPoint.getLongitude()),
                            Length.of(geoPoint.getAltitude(), Length.Unit.METER),
                            dateZoned
                    ));
                }
                io.jenetics.jpx.TrackSegment segment = io.jenetics.jpx.TrackSegment.builder().points(wayPoints).build();
                trackBuilder = trackBuilder.addSegment(segment);
            }
            io.jenetics.jpx.Track gpxTrack = trackBuilder.build();
            gpxTracks.add(gpxTrack);
        }
        GPX gpx = GPX.of(appName, metadata, null, null, gpxTracks);
        return gpx;
    }

    @Override
    public void importFromFile(Context ctx, InputStream inputStream) throws IOException {
        String appName = ctx.getApplicationInfo().loadLabel(ctx.getPackageManager()).toString();
        GPX gpx = GPX.read(inputStream);

        // import Tracks
        List<io.jenetics.jpx.Track> gpxTracks = gpx.tracks().collect(Collectors.toList());
        // iterate over Tracks
        for (int i = 0; i < gpxTracks.size(); i++) {
            io.jenetics.jpx.Track gpxTrack = gpxTracks.get(i);
            String trackName = gpxTrack.getName().isPresent() ? gpxTrack.getName().get() : null;
            String creator = null;
            LocalDateTime dateOfCreation = null;
            // file was originally exported from this app, use the description property of track to get creator and date of track
            if (gpx.getCreator().equals(appName)) {
                String desc = gpxTrack.getDescription().orElse("");
                if (!desc.isEmpty()) {
                    String[] descParts = desc.split(delimiter);
                    if (descParts.length == 2) {
                        // creator
                        creator = descParts[0].isEmpty() ? null : descParts[0];
                        // date
                        String dateString = descParts[1];
                        if (!dateString.isEmpty()) {
                            try {
                                dateOfCreation = LocalDateTime.parse(dateString);
                            } catch (DateTimeParseException e) {
                                Log.d(getLogStart(), "Could not parse date string of a track. " + System.lineSeparator()
                                        + "String to parse was: " + e.getParsedString() + System.lineSeparator()
                                        + "Error message: " + e.getLocalizedMessage());
                            }
                        }
                    } else {
                        Log.d(getLogStart(), "The description parameter was not properly formatted on export from this app. "
                                + "For this reason some attributes of a track could not be properly imported");
                    }
                }
            }

            List<io.jenetics.jpx.TrackSegment> gpxTrackSegments = gpxTrack.getSegments();
            List<TrackSegment> trackSegments = new ArrayList<>();

            // iterate over TrackSegments
            for (io.jenetics.jpx.TrackSegment gpxTrackSegment : gpxTrackSegments) {
                TrackSegment trackSegment = new TrackSegment(null);
                List<WayPoint> gpxWayPoints = gpxTrackSegment.getPoints();

                // iterate over TrackPoints / WayPoints
                for (WayPoint gpxWayPoint : gpxWayPoints) {
                    Latitude lat = gpxWayPoint.getLatitude();
                    Longitude lon = gpxWayPoint.getLongitude();
                    Optional<Length> alt = gpxWayPoint.getElevation();
                    GeoPoint geoPoint = alt.isPresent() ?
                            new GeoPoint(lat.doubleValue(), lon.doubleValue()) :
                            new GeoPoint(lat.doubleValue(), lon.doubleValue(), alt.get().doubleValue());
                    LocalDateTime pointTime = gpxWayPoint.getTime().orElse(null).toLocalDateTime();
                    TrackPoint trackPoint = new TrackPoint(geoPoint, pointTime);
                    trackSegment.addTrackPoint(trackPoint);
                }
                trackSegments.add(trackSegment);
            }
            Track track = new Track(null, trackName, creator, dateOfCreation, trackSegments);
            trackModelManager.addGeoModel(track);
        }
    }

    private String getLogStart() {
        return getClass().getSimpleName();
    }
}
