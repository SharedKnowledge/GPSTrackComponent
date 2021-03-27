package net.gpstrackapp.mapview;

import net.gpstrackapp.geomodel.GeoModel;

import org.osmdroid.views.overlay.FolderOverlay;

public abstract class GeoModelOverlay<T extends GeoModel> extends FolderOverlay {
    protected T geoModel;

    public GeoModelOverlay(T geomodel) {
        this.geoModel = geomodel;
    }
}

