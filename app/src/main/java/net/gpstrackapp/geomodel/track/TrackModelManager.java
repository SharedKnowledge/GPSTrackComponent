package net.gpstrackapp.geomodel.track;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import net.gpstrackapp.GPSComponent;
import net.gpstrackapp.geomodel.GeoModelManager;
import net.gpstrackapp.geomodel.GeoModelStorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TrackModelManager extends GeoModelManager<Track> implements GeoModelStorage<Track> {
    private static final String SUBDIR_NAME = "tracks";
    public void mergeTracks(Context ctx, Set<Track> tracksToMerge, String newTrackName) {
        List<TrackSegment> trackSegments = tracksToMerge.stream()
                .flatMap(track -> track.getTrackSegments().stream())
                .collect(Collectors.toList());
        Track track = new Track(null, newTrackName,
                GPSComponent.getGPSComponent().getASAPApplication().getOwnerName(),
                LocalDateTime.now(), trackSegments);
        addGeoModel(track);
        Toast.makeText(ctx, "The Tracks have been successfully merged.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean saveGeoModelToFile(Context ctx, Track trackToSave) {
        if (trackToSave == null) {
            return false;
        }
        try {
            File dir = new File(ctx.getFilesDir(), SUBDIR_NAME);
            dir.mkdirs();
            File fileToSave = new File(dir, trackToSave.getObjectID().toString());
            FileOutputStream fos = new FileOutputStream(fileToSave);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(trackToSave);
            oos.close();
            Log.d(getLogStart(), "Saved track: " + trackToSave.getObjectName());
            return true;
        } catch (IOException e) {
            Log.e(getLogStart(), "A problem occurred while trying to save a track." + System.lineSeparator() + e.getLocalizedMessage());
            return false;
        }
    }

    @Override
    public boolean saveGeoModelsToFiles(Context ctx, Set<Track> tracksToSave) {
        if (tracksToSave == null) {
            return false;
        }
        boolean allSaved = true;
        for (Track trackToSave : tracksToSave) {
            allSaved = saveGeoModelToFile(ctx, trackToSave) && allSaved;
        }
        return allSaved;
    }

    @Override
    public boolean deleteGeoModelFromFile(Context ctx, Track trackToDelete) {
        if (trackToDelete == null) {
            return false;
        }
        File dir = new File(ctx.getFilesDir(), SUBDIR_NAME);
        dir.mkdirs();
        File fileToDelete = new File(dir, trackToDelete.getObjectID().toString());
        Log.d(getLogStart(), "Deleted track: " + trackToDelete.getObjectName());
        // just returns false if file does not exist
        return fileToDelete.delete();
    }

    @Override
    public boolean deleteGeoModelsFromFiles(Context ctx, Set<Track> tracksToDelete) {
        if (tracksToDelete == null) {
            return false;
        }
        boolean allDeleted = true;
        for (Track trackToDelete : tracksToDelete) {
            allDeleted = deleteGeoModelFromFile(ctx, trackToDelete) && allDeleted;
        }
        return allDeleted;
    }

    @Override
    public boolean loadAllGeoModelsFromFiles(Context ctx) {
        boolean allLoaded = true;
        File dir = new File(ctx.getFilesDir(), SUBDIR_NAME);
        dir.mkdirs();
        File[] files = dir.listFiles(File::isFile);
        Log.d(getLogStart(), "Attempt to load " + files.length + " tracks from storage");
        Set<Track> tracks = new HashSet<>();
        for (File file : files) {
            try {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis);
                Track loadedTrack = (Track) ois.readObject();
                tracks.add(loadedTrack);
                Log.d(getLogStart(), "Loaded track with UUID: " + loadedTrack.getObjectID());
            } catch (IOException | ClassNotFoundException e) {
                Log.e(getLogStart(), "A problem occurred while trying to load a track." + System.lineSeparator() + e.getLocalizedMessage());
                allLoaded = false;
            }
        }
        for (Track track : tracks) {
            this.addGeoModel(track);
        }
        return allLoaded;
    }

    private String getLogStart() {
        return this.getClass().getSimpleName();
    }
}
