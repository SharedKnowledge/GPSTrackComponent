package net.gpstrackapp.geomodel;

import java.util.List;

public interface RequestGeoModelsCommand {
    List<? extends GeoModel> getGeoModels();
    int getNumberOfGeoModels();
}
