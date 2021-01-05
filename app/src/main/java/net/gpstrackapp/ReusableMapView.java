package net.gpstrackapp;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.overlay.TrackDisplayer;
import net.gpstrackapp.overlay.TrackOverlay;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

//TODO evtl. eine uebergeordnete Klasse, welche die Grundeigenschaften implementiert (damit man mehrere derartige MapViews haben kann ohne den Code kopieren zu müssen)
public final class ReusableMapView extends MapView implements TrackDisplayer {
    private static ReusableMapView instance = null;
    //TODO change TileSource (nicht Mapnik) und in validTileSources entfernen, besser ist jedoch validTileSources nur fuer Download und Laden von Offline-Tiles zu verwenden
    private static final ITileSource DEFAULT_TILE_SOURCE = TileSourceFactory.MAPNIK;
    private ITileSource tileSource = DEFAULT_TILE_SOURCE;
    private MyLocationNewOverlay myLocationNewOverlay;
    private Map<Track, TrackOverlay> trackDisplayMap = new HashMap<>();
    private static Set<ITileSource> validTileSources = new HashSet<ITileSource>(Arrays.asList(
            TileSourceFactory.MAPNIK,
            TileSourceFactory.USGS_TOPO,
            TileSourceFactory.USGS_SAT));

    private ReusableMapView(Context ctx) {
        super(ctx);
    }

    public static ReusableMapView getInstance(Context ctx) {
        if (ReusableMapView.instance == null) {
            ReusableMapView.instance = new ReusableMapView(ctx);
            ReusableMapView.instance.addOverlays(ctx);
        }
        return ReusableMapView.instance;
    }

    public static ReusableMapView getInstance(Context ctx, ITileSource tileSource) {
        getInstance(ctx);
        try {
            ReusableMapView.instance.setTileSource(tileSource);
        } catch (IllegalArgumentException e) {
            //TODO Logging anpassen
            Log.d(ReusableMapView.instance.getLogStart(), e.getLocalizedMessage());
        }
        return ReusableMapView.instance;
    }

    public Map<Track, TrackOverlay> getTrackDisplayMap() {
        return trackDisplayMap;
    }

    @Override
    public void setTileSource(ITileSource tileSource) throws IllegalArgumentException {
        if (validTileSources.contains(tileSource)) {
            this.tileSource = tileSource;
            super.setTileSource(tileSource);
        } else {
            throw new IllegalArgumentException("The passed TileSource parameter is invalid." + System.lineSeparator() +
                    "For policy reasons only the following TileSources are valid:" + System.lineSeparator() +
                    getValidTileSourcesAsString()
            );
        }
    }

    @Override
    public boolean addTrackToMap(Track track) {
        TrackOverlay trackOverlay = new TrackOverlay(track.getGeoPoints());
        trackDisplayMap.put(track, trackOverlay);
        boolean added = ReusableMapView.instance.getOverlayManager().add(trackOverlay);
        ReusableMapView.instance.invalidate();
        return added;
    }

    @Override
    public boolean removeTrackFromMap(Track track) {
        trackDisplayMap.remove(track);
        boolean removed = ReusableMapView.instance.getOverlayManager().remove(track);
        ReusableMapView.instance.invalidate();
        return removed;
    }


    private void addOverlays(Context ctx) {
        ReusableMapView.instance.getOverlays().add(new CopyrightOverlay(ctx));

        myLocationNewOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(ctx), ReusableMapView.instance);
        myLocationNewOverlay.enableMyLocation();
        myLocationNewOverlay.enableFollowLocation();
        ReusableMapView.instance.getOverlays().add(myLocationNewOverlay);
    }

    public GeoPoint getLastLocation() {
        if (myLocationNewOverlay != null) {
            myLocationNewOverlay.getLastFix();
            return myLocationNewOverlay.getMyLocation();
        }
        return null;
    }

    public static String getValidTileSourcesAsString() {
        String tileSourceString = "";
        // basically the foreach implementation
        for (Iterator<ITileSource> i = validTileSources.iterator(); i.hasNext();) {
            ITileSource tileSource = i.next();
            tileSourceString += tileSource.name();
            if (i.hasNext()) {
                tileSourceString += "," + System.lineSeparator();
            }
        }
        return tileSourceString;
    }

    private String getLogStart() {
        return "ReusableMapView: ";
    }
}
