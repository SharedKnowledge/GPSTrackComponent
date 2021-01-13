package net.gpstrackapp;

import net.gpstrackapp.geomodel.GeoModel;
import net.gpstrackapp.geomodel.track.TrackManager;

import java.util.List;

public class RequestTracksCommand implements RequestGeoModelsCommand {
    @Override
    public List<? extends GeoModel> getGeoModels() {
        return TrackManager.getAllTracks();
    }

    @Override
    public int getNumberOfGeoModels() {
        return TrackManager.getNumberOfTracks();
    }
}
