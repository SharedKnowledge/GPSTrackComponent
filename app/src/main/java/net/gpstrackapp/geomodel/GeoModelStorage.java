package net.gpstrackapp.geomodel;

import android.content.Context;

import java.util.Set;

public interface GeoModelStorage<T extends GeoModel> {
    void saveGeoModelToFile(Context ctx, T geoModel);

    void saveGeoModelsToFiles(Context ctx, Set<T> geoModelsToSave);

    void loadAllGeoModelsFromFiles(Context ctx);
}
