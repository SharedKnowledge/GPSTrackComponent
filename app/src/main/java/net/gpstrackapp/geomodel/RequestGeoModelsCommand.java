package net.gpstrackapp.geomodel;

import java.util.List;

public interface RequestGeoModelsCommand<T extends GeoModel> {
    List<T> getGeoModels();
    int getNumberOfGeoModels();
}
