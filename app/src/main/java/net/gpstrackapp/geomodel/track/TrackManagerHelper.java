package net.gpstrackapp.geomodel.track;

public class TrackManagerHelper {
    private static TrackManager globalTrackManager = null;

    public void setGlobalTrackManager(TrackManager trackManager) {
        TrackManagerHelper.globalTrackManager = trackManager;
    }
}
