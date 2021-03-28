package net.gpstrackapp.geomodel.track;

import net.gpstrackapp.GPSComponent;
import net.gpstrackapp.geomodel.RequestGeoModelsCommand;

import java.util.List;

public class RequestTracksCommand implements RequestGeoModelsCommand<Track> {
    TrackModelManager trackModelManager = GPSComponent.getGPSComponent().getTrackModelManager();

    @Override
    public List<Track> getGeoModels() {
        return trackModelManager.getAll();
    }

    @Override
    public int getNumberOfGeoModels() {
        return trackModelManager.count();
    }
}
