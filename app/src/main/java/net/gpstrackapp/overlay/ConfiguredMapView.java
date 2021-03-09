package net.gpstrackapp.overlay;

import android.content.Context;
import android.util.Log;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ConfiguredMapView extends MapView {
    private MyLocationNewOverlay locationOverlay;
    private GpsMyLocationProvider provider;
    private static ITileSource defaultTileSource = TileSourceFactory.OpenTopo;
    private ITileSource selectedTileSource = defaultTileSource;
    // add Tile Sources to use for the maps here, make sure to read the terms of service before adding them
    private static Set<ITileSource> validTileSources = new HashSet<>(Arrays.asList(
            // MAPNIK and WIKIMEDIA not usable for downloads
            TileSourceFactory.MAPNIK,
            TileSourceFactory.WIKIMEDIA,
            TileSourceFactory.OpenTopo
            /*
            At the moment the USGS TileSources return a Not Found error for tiles on zoom levels 9 or
            higher, as can be checked on these sites by trying to load the Start Tile of Level ID 9 or higher:
            TileSourceFactory.USGS_TOPO: https://basemap.nationalmap.gov/arcgis/rest/services/USGSTopo/MapServer
            TileSourceFactory.USGS_SAT: https://basemap.nationalmap.gov/arcgis/rest/services/USGSImageryTopo/MapServer
             */
    ));
    private Context ctx;

    public ConfiguredMapView(Context ctx) {
        super(ctx);
        this.ctx = ctx;
        this.setTileSource(selectedTileSource);
        setupOverlays();
        getZoomController().setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);
        setMultiTouchControls(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(getLogStart(), "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(getLogStart(), "onPause");
    }

    private void setupOverlays() {
        Log.d(getLogStart(), "setup Overlays");
        this.getOverlays().clear();
        this.getOverlays().add(new CopyrightOverlay(ctx));

        provider = new GpsMyLocationProvider(ctx);
        locationOverlay = new MyLocationNewOverlay(provider, this);
        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();
        this.getOverlays().add(locationOverlay);

        RotationGestureOverlay rotationGestureOverlay = new RotationGestureOverlay(this);
        rotationGestureOverlay.setEnabled(true);
        this.getOverlays().add(rotationGestureOverlay);
    }

    public GpsMyLocationProvider getProvider() {
        return provider;
    }

    public GeoPoint getLastLocation() {
        if (locationOverlay != null) {
            return locationOverlay.getMyLocation();
        }
        return null;
    }

    @Override
    public void setTileSource(ITileSource tileSource) {
        if (validTileSources.contains(tileSource)) {
            this.selectedTileSource = tileSource;
            super.setTileSource(tileSource);
        } else {
            Log.e(getLogStart(), "The passed TileSource parameter is invalid." + System.lineSeparator() +
                    "For policy reasons only the following TileSources are valid:" + System.lineSeparator() +
                    getValidTileSourcesAsString());
        }
    }

    public static void setDefaultTileSource(ITileSource defaultTileSource) {
        if (validTileSources.contains(defaultTileSource)) {
            ConfiguredMapView.defaultTileSource = defaultTileSource;
        } else {
            Log.e(getLogStart(), "The passed TileSource parameter is invalid." + System.lineSeparator() +
                    "For policy reasons only the following TileSources are valid:" + System.lineSeparator() +
                    getValidTileSourcesAsString());
        }
    }

    public static ITileSource getDefaultTileSource() {
        return defaultTileSource;
    }

    public static Set<ITileSource> getValidTileSources() {
        return validTileSources;
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

    private static String getLogStart() {
        return ConfiguredMapView.class.getSimpleName();
    }
}
