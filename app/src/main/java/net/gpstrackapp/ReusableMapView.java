package net.gpstrackapp;

import android.content.Context;
import android.util.Log;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ReusableMapView extends MapView {
    private static ReusableMapView instance = null;
    //TODO change TileSource
    private static final ITileSource DEFAULT_TILE_SOURCE = TileSourceFactory.MAPNIK;
    private ITileSource tileSource = DEFAULT_TILE_SOURCE;
    private static Set<ITileSource> validTileSources = new HashSet<ITileSource>(Arrays.asList(
            TileSourceFactory.MAPNIK,
            TileSourceFactory.USGS_TOPO,
            TileSourceFactory.USGS_SAT));

    private ReusableMapView(Context context) {
        super(context);
    }

    public static ReusableMapView getInstance(Context context) {
        if (ReusableMapView.instance == null) {
            ReusableMapView.instance = new ReusableMapView(context);
        }
        return ReusableMapView.instance;
    }

    public static ReusableMapView getInstance(Context context, ITileSource tileSource) {
        getInstance(context);
        try {
            ReusableMapView.instance.setTileSource(tileSource);
        } catch (IllegalArgumentException e) {
            //TODO Logging anpassen
            Log.d(ReusableMapView.instance.getLogStart(), e.getLocalizedMessage());
        }
        return ReusableMapView.instance;
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
        return "CentralMapView: ";
    }
}
