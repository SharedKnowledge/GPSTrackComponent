package net.gpstrackapp;

import android.content.Context;

import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.overlay.TrackDisplayer;
import net.gpstrackapp.overlay.TrackOverlay;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ReusableTrackMapView extends ReusableMapView implements TrackDisplayer {
    private Map<Track, TrackOverlay> trackWithOverlayHolder = new HashMap<>();

    public ReusableTrackMapView(Context ctx) {
        super(ctx);
    }

    @Override
    public Map<Track, TrackOverlay> getTracksWithOverlaysHolder() {
        return trackWithOverlayHolder;
    }

    @Override
    public boolean addTrackToMap(Track track) {
        TrackOverlay trackOverlay = new TrackOverlay(track.getGeoPoints());
        trackWithOverlayHolder.put(track, trackOverlay);
        boolean added = this.getOverlayManager().add(trackOverlay);
        this.invalidate();
        return added;
    }

    @Override
    public boolean removeTrackFromMap(Track track) {
        TrackOverlay trackOverlay = trackWithOverlayHolder.get(track);
        trackWithOverlayHolder.remove(track);
        boolean removed = this.getOverlayManager().remove(trackOverlay);
        this.invalidate();
        return removed;
    }
/*
    public void setDisplayedTracks(Set<String> selectedItemIDs, Context ctx) {
        //clear all overlays
        ReusableTrackMapView.instance.getOverlays().clear();

        //add standard overlays
        ReusableTrackMapView.instance.addOverlays(ctx);

        //add track overlays
        List<Track> trackList = TrackManager.getAllTracks();
        for (int i = 0; i < trackList.size(); i++) {
            Track track = trackList.get(i);
            if (selectedItemIDs.contains(track.getObjectId())) {
                addTrackToMap(track);
            }
        }
    }
*/
}
