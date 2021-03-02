package net.gpstrackapp.overlay;

import net.gpstrackapp.geomodel.GeoModel;

import org.osmdroid.views.overlay.FolderOverlay;

public abstract class GeoModelOverlay<T extends GeoModel> extends FolderOverlay {
    protected GeoModel geoModel;

    public GeoModelOverlay(T geomodel) {
        this.geoModel = geomodel;
    }
}

