package net.gpstrackapp;

import android.content.Context;
import android.util.Log;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.Overlay;
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
    //TODO change TileSource (nicht Mapnik) und in validTileSources entfernen, besser ist jedoch validTileSources nur fuer Download und Laden von Offline-Tiles zu verwenden
    private static final ITileSource DEFAULT_TILE_SOURCE = TileSourceFactory.MAPNIK;
    private ITileSource tileSource = DEFAULT_TILE_SOURCE;
    private static Set<ITileSource> validTileSources = new HashSet<ITileSource>(Arrays.asList(
            TileSourceFactory.MAPNIK,
            TileSourceFactory.OpenTopo,
            TileSourceFactory.USGS_TOPO,
            TileSourceFactory.USGS_SAT));
    private Context ctx;

    public ConfiguredMapView(Context ctx) {
        super(ctx);
        this.ctx = ctx;
        setupOverlays();
        getZoomController().setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);
        setMultiTouchControls(true);
        Log.d(getLogStart(), "Constructor");
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
        return this.getClass().getSimpleName();
    }
}
