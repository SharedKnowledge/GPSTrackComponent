package net.gpstrackapp;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

//TODO evtl. Interface
public class TileSourceHandler {
    private static final ITileSource DEFAULT_TILE_SOURCE = TileSourceFactory.USGS_TOPO;
    private static ITileSource tileSource = DEFAULT_TILE_SOURCE;
    private static TileSourceHandler instance = null;

    private TileSourceHandler(ITileSource tileSource) {
        this.tileSource = tileSource;
    }

    public static TileSourceHandler init(ITileSource tileSource) {
        if (TileSourceHandler.instance == null)
            TileSourceHandler.instance = new TileSourceHandler(tileSource);
        return TileSourceHandler.instance;
}

    public static TileSourceHandler getInstance() {
        return instance;
    }

    public void setTileSource(ITileSource tileSource) {
        TileSourceHandler.instance.tileSource = tileSource;
    }

    public ITileSource getTileSource() {
        return TileSourceHandler.instance.tileSource;
    }
}
