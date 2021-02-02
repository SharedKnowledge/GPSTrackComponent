package net.gpstrackapp.geomodel;

import net.gpstrackapp.geomodel.GeoModel;

import java.util.List;

//TODO write: Command Pattern
public interface RequestGeoModelsCommand {
    List<? extends GeoModel> getGeoModels();

    int getNumberOfGeoModels();
}
