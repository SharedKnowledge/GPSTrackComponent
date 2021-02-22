package net.gpstrackapp.geomodel;

import android.content.Context;

import java.util.Set;

public interface GeoModelStorage<T extends GeoModel> {
    void saveGeoModelToFile(Context ctx, T geoModelToSave);

    void saveGeoModelsToFiles(Context ctx, Set<T> geoModelsToSave);

    void deleteGeoModelFromFile(Context ctx, T geoModelToDelete);

    void deleteGeoModelsFromFiles(Context ctx, Set<T> geoModelsToDelete);

    void loadAllGeoModelsFromFiles(Context ctx);
}
