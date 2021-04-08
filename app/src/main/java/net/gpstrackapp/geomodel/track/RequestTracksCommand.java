package net.gpstrackapp.geomodel.track;

import net.gpstrackapp.GPSComponent;
import net.gpstrackapp.geomodel.GeoModel;
import net.gpstrackapp.geomodel.RequestGeoModelsCommand;

import java.util.List;

public class RequestTracksCommand implements RequestGeoModelsCommand {
    TrackModelManager trackModelManager = GPSComponent.getTrackModelManager();

    @Override
    public List<? extends GeoModel> getGeoModels() {
        return trackModelManager.getAll();
    }

    @Override
    public int getNumberOfGeoModels() {
        return trackModelManager.count();
    }
}
