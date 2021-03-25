package net.gpstrackapp.geomodel;

import android.content.Context;

import java.util.Set;

public interface GeoModelStorage<T extends GeoModel> {
    boolean saveGeoModelToFile(Context ctx, T geoModelToSave);
    boolean saveGeoModelsToFiles(Context ctx, Set<T> geoModelsToSave);
    boolean deleteGeoModelFromFile(Context ctx, T geoModelToDelete);
    boolean deleteGeoModelsFromFiles(Context ctx, Set<T> geoModelsToDelete);
    boolean loadAllGeoModelsFromFiles(Context ctx);
}
