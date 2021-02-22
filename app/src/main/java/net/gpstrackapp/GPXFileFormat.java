package net.gpstrackapp;

import android.content.Context;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Latitude;
import io.jenetics.jpx.Length;
import io.jenetics.jpx.Longitude;
import io.jenetics.jpx.Metadata;
import io.jenetics.jpx.WayPoint;

public class GPXFileFormat implements ExportFileFormat, ImportFileFormat {
    private TrackModelManager trackModelManager = GPSComponent.getGPSComponent().getTrackModelManager();

    @Override
    public String getMIMEDataType() {
        return "application/gpx+xml";
    }

    @Override
    public String getFileExtensionString() {
        return "gpx";
    }

    @Override
    public void exportToFile(Context ctx, Set<Track> tracksToExport, String trackName, OutputStream outputStream) throws IOException {
        String appName = ctx.getApplicationInfo().loadLabel(ctx.getPackageManager()).toString();
        GPX gpx = generateGPX(tracksToExport, trackName, appName);
        gpx.write(gpx, outputStream);

        /*
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document document = dBuilder.newDocument();

        Element gpxElement = document.createElement("gpx");
        document.appendChild(gpxElement);
        Attr gpxCreator = document.createAttribute(ctx.getApplicationInfo().loadLabel(ctx.getPackageManager()).toString());
         */
    }

    private GPX generateGPX(Set<Track> tracksToExport, String trackName, String appName) {
        //TODO extension that lets one give more details for track segments, so that id, name, creator, etc. don't get lost when user does import -> export
        Metadata metadata = Metadata.builder()
                .author(GPSComponent.getGPSComponent().getASAPApplication().getOwnerName().toString())
                .time(System.currentTimeMillis())
                .name(trackName)
                .build();

        List<io.jenetics.jpx.Track> gpxTracks = new ArrayList<>();
        // iterate over Tracks
        for (Track track : tracksToExport) {
            io.jenetics.jpx.Track.Builder trackBuilder = io.jenetics.jpx.Track.builder().name(trackName);
            List<TrackSegment> trackSegments = track.getTrackSegments();

            // iterate over TrackSegments
            for (TrackSegment trackSegment : trackSegments) {
                List<WayPoint> wayPoints = new ArrayList<>();

                // iterate over TrackPoints / WayPoints
                for (TrackPoint trackPoint : trackSegment.getTrackPoints()) {
                    GeoPoint geoPoint = trackPoint.getGeoPoint();
                    wayPoints.add(WayPoint.of(
                            Latitude.ofDegrees(geoPoint.getLatitude()),
                            Longitude.ofDegrees(geoPoint.getLongitude()),
                            Length.of(geoPoint.getAltitude(), Length.Unit.METER),
                            trackPoint.getDate().atZone(ZoneId.systemDefault())
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
    public void importFromFile(InputStream inputStream) throws IOException {
        GPX gpx = GPX.read(inputStream);

        Optional<Metadata> metaOpt = gpx.getMetadata();
        LocalDateTime time = null;
        if (metaOpt.isPresent()) {
            Metadata metadata = metaOpt.get();
            ZonedDateTime zonedDateTime = metadata.getTime().orElse(null);
            time = zonedDateTime.toLocalDateTime();
        }

        // import Tracks
        List<io.jenetics.jpx.Track> gpxTracks = gpx.tracks().collect(Collectors.toList());
        // iterate over Tracks
        for (int i = 0; i < gpxTracks.size(); i++) {
            io.jenetics.jpx.Track gpxTrack = gpxTracks.get(i);
            String trackName = gpxTrack.getName().isPresent() ?
                    gpxTrack.getName().get() + " - Abschnitt " + (i + 1) :
                    null;

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
            Track track = new Track(null, trackName, null, time, trackSegments);
            trackModelManager.addGeoModel(track);
        }

        /*
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);
         */
    }
}
