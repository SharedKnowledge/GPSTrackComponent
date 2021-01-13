package net.gpstrackapp.geomodel.track;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import net.gpstrackapp.ConfiguredMapView;
import net.gpstrackapp.geomodel.GeoModel;
import net.gpstrackapp.geomodel.GeoModelManager;
import net.gpstrackapp.overlay.TrackOverlay;

import org.osmdroid.views.MapView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class TrackManager extends GeoModelManager<Track, TrackOverlay> {
    private static List<Track> tracks = new ArrayList<>();

    public TrackManager(ConfiguredMapView mapView) {
        super(mapView);
    }

    public static Track createTrack(CharSequence objectID, CharSequence objectName, CharSequence creator, Date dateOfCreation, List<TrackPoint> trackPoints) {
        Track track = new Track(objectID, objectName, creator, dateOfCreation, trackPoints);
        TrackManager.addExistingTrack(track);
        return track;
    }

    public static boolean destroyTrack(CharSequence uuid) {
        Track track = TrackManager.getTrackByUUID(uuid);
        if (track != null) {
            return TrackManager.tracks.remove(track);
        } else {
            return false;
        }
    }

    public static boolean addExistingTrack(Track track) {
        return TrackManager.tracks.add(track);
    }

    public static Track getTrackByUUID(CharSequence uuid) {
        for (int i = 0; i < tracks.size(); i++) {
            if (TrackManager.tracks.get(i).getObjectId().equals(uuid)) {
                return TrackManager.tracks.get(i);
            }
        }
        return null;
    }

    public static List<Track> getAllTracks() {
        return TrackManager.tracks;
    }

    public static int getNumberOfTracks() {
        return TrackManager.tracks.size();
    }

    public static void saveTrackToFile(Context ctx, Track trackToSave) {
        try {
            FileOutputStream fos = ctx.openFileOutput(trackToSave.getObjectId().toString(), Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(trackToSave);
            oos.close();
            Log.d(getLogStart(), "Saved track: " + trackToSave.getObjectName());
        } catch (IOException e) {
            Log.d(getLogStart(), "A problem occurred while while trying to save a track." + System.lineSeparator() + e.getMessage());
            Toast.makeText(ctx, "A problem occurred while while trying to save a track.", Toast.LENGTH_LONG).show();
        }
    }

    public static void saveTracksToFiles(Context ctx, Set<Track> tracksToSave) {
        Iterator<Track> iterator = tracksToSave.iterator();
        while (iterator.hasNext()) {
            Track trackToSave = iterator.next();
            TrackManager.saveTrackToFile(ctx, trackToSave);
        }
        Toast.makeText(ctx, "The Tracks have been successfully saved.", Toast.LENGTH_LONG).show();
    }

    public static void loadAllTracksFromFiles(Context ctx) {
        File[] files = ctx.getFilesDir().listFiles();
        Log.d(getLogStart(), "Attempt to load " + files.length + " tracks from storage");
        Set<Track> tracks = new HashSet<>();
        for (int i = 0; i < files.length; i++) {
            try {
                FileInputStream fis = ctx.openFileInput(files[i].getName());
                ObjectInputStream ois = new ObjectInputStream(fis);
                Track loadedTrack = (Track) ois.readObject();
                tracks.add(loadedTrack);
                Log.d(getLogStart(), "Loaded track with UUID: " + loadedTrack.getObjectId());
            } catch (IOException | ClassNotFoundException e) {
                Log.d(getLogStart(), "A problem occurred while while trying to load a track." + System.lineSeparator() + e.getLocalizedMessage());
            }
        }

        Iterator<Track> iterator = tracks.iterator();
        while (iterator.hasNext()) {
            Track track = iterator.next();
            TrackManager.addExistingTrack(track);
        }
    }


    @Override
    protected TrackOverlay createGeoModelOverlay(Track track, ConfiguredMapView mapView) {
        TrackOverlay trackOverlay = new TrackOverlay(track);
        trackOverlay.addStartEndMarkers(mapView);
        return trackOverlay;
    }

    @Override
    protected Track getGeoModelByUUID(CharSequence uuid) {
        return TrackManager.getTrackByUUID(uuid);
    }

    public static Set<Track> getTracksByUUIDs(Set<CharSequence> uuids) {
        Iterator<CharSequence> iterator = uuids.iterator();
        Set<Track> tracks = new HashSet<>();
        while (iterator.hasNext()) {
            Track track = TrackManager.getTrackByUUID(iterator.next());
            if (track != null) {
                tracks.add(track);
            }
        }
        return tracks;
    }

    private static String getLogStart() {
        return TrackManager.class.getSimpleName();
    }
}
