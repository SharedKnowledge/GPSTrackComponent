package net.gpstrackapp.mapview;

import android.content.Context;
import android.util.Log;

import net.sharksystem.asap.android.Util;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DownloadableTilesMapView extends MapView {
    // this is only the default on first start, from the second start onwards the previously last used tile source is used
    private static ITileSource defaultTileSource = TileSourceFactory.OpenTopo;
    private ITileSource selectedTileSource = defaultTileSource;
    // add Tile Sources to use for the maps here, make sure to read the terms of service before adding them
    private static Set<ITileSource> validTileSources = new HashSet<>(Arrays.asList(
        TileSourceFactory.OpenTopo
        /*
        At the moment the USGS TileSources return a Not Found error for tiles on zoom levels 9 or
        higher, as can be checked on these sites by trying to load the Start Tile of Level ID 9 or higher:
        TileSourceFactory.USGS_TOPO: https://basemap.nationalmap.gov/arcgis/rest/services/USGSTopo/MapServer
        TileSourceFactory.USGS_SAT: https://basemap.nationalmap.gov/arcgis/rest/services/USGSImageryTopo/MapServer
         */
    ));

    public DownloadableTilesMapView(Context ctx) {
        super(ctx);
        this.setTileSource(selectedTileSource);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(Util.getLogStart(this), "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(Util.getLogStart(this), "onPause");
    }

    @Override
    public void setTileSource(ITileSource tileSource) {
        if (!validTileSources.contains(tileSource)) {
            tileSource = defaultTileSource;
        }
        this.selectedTileSource = tileSource;
        super.setTileSource(tileSource);

        double minZoom = tileSource.getMinimumZoomLevel();
        double maxZoom = tileSource.getMaximumZoomLevel();
        // adjust zoom level, otherwise tiles won't render because they don't exist for these zoom levels
        if (getZoomLevelDouble() < minZoom) {
            getController().setZoom(minZoom);
        } else if (getZoomLevelDouble() > maxZoom) {
            getController().setZoom(maxZoom);
        }
    }

    public static ITileSource getDefaultTileSource() {
        return defaultTileSource;
    }

    public static Set<ITileSource> getValidTileSources() {
        return validTileSources;
    }
}
