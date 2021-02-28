package net.gpstrackapp;

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
    private static ITileSource defaultTileSource = TileSourceFactory.DEFAULT_TILE_SOURCE;
    private ITileSource selectedTileSource = defaultTileSource;
    private static Set<ITileSource> validTileSources = new HashSet<ITileSource>(Arrays.asList(
            TileSourceFactory.MAPNIK,
            TileSourceFactory.OpenTopo,
            /* for some reason the USGS TileSources often return a Not Found error for tiles on higher zoom levels
            while lower zoom levels work perfectly fine */
            TileSourceFactory.USGS_TOPO,
            TileSourceFactory.USGS_SAT));
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
