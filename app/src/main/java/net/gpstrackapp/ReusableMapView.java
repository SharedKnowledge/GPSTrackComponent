package net.gpstrackapp;

import android.content.Context;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ReusableMapView extends MapView {
    private MyLocationNewOverlay myLocationNewOverlay;
    //TODO change TileSource (nicht Mapnik) und in validTileSources entfernen, besser ist jedoch validTileSources nur fuer Download und Laden von Offline-Tiles zu verwenden
    private static final ITileSource DEFAULT_TILE_SOURCE = TileSourceFactory.MAPNIK;
    private ITileSource tileSource = DEFAULT_TILE_SOURCE;
    private static Set<ITileSource> validTileSources = new HashSet<ITileSource>(Arrays.asList(
            TileSourceFactory.MAPNIK,
            TileSourceFactory.USGS_TOPO,
            TileSourceFactory.USGS_SAT));

    public ReusableMapView(Context ctx) {
        super(ctx);
        addOverlays(ctx);
    }

    private void addOverlays(Context ctx) {
        this.getOverlays().add(new CopyrightOverlay(ctx));

        myLocationNewOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(ctx), this);
        myLocationNewOverlay.enableMyLocation();
        myLocationNewOverlay.enableFollowLocation();
        this.getOverlays().add(myLocationNewOverlay);
    }

    public GeoPoint getLastLocation() {
        if (myLocationNewOverlay != null) {
            myLocationNewOverlay.getLastFix();
            return myLocationNewOverlay.getMyLocation();
        }
        return null;
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
        return "ReusableTrackMapView: ";
    }
}
