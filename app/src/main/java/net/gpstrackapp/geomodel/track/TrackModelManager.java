package net.gpstrackapp.geomodel.track;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import net.gpstrackapp.geomodel.GeoModel;
import net.gpstrackapp.geomodel.GeoModelManager;
import net.gpstrackapp.geomodel.GeoModelStorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class TrackModelManager extends GeoModelManager<Track> implements GeoModelStorage<Track> {
    public void saveGeoModelToFile(Context ctx, Track trackToSave) {
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

    public void saveGeoModelsToFiles(Context ctx, Set<Track> tracksToSave) {
        Iterator<Track> iterator = tracksToSave.iterator();
        while (iterator.hasNext()) {
            Track trackToSave = iterator.next();
            saveGeoModelToFile(ctx, trackToSave);
        }
        Toast.makeText(ctx, "The Tracks have been successfully saved.", Toast.LENGTH_LONG).show();
    }

    public void loadAllGeoModelsFromFiles(Context ctx) {
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
            this.add(track);
        }
    }

    private String getLogStart() {
        return this.getClass().getSimpleName();
    }


}
