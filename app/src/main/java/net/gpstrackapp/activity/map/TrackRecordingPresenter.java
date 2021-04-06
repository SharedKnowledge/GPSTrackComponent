package net.gpstrackapp.activity.map;

import net.gpstrackapp.GPSComponent;
import net.gpstrackapp.activity.LifecycleObject;
import net.gpstrackapp.geomodel.track.TrackVisualizationManager;
import net.gpstrackapp.mapview.TrackOverlay;

import java.util.Set;

public class TrackRecordingPresenter implements LifecycleObject {
    private TrackVisualizationManager trackVisualizer;
    private ViewWithOverlays view;
    private String toastText = "";

    public TrackRecordingPresenter(ViewWithOverlays view) {
        this.trackVisualizer = new TrackVisualizationManager(GPSComponent.getGPSComponent().getTrackModelManager());
        this.view = view;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {
        //add and remove TrackOverlays
        trackVisualizer.updateGeoModelHolder();

        Set<TrackOverlay> overlaysToAddToMap = trackVisualizer.getGeoModelOverlaysToAddToMap();
        for (TrackOverlay overlay : overlaysToAddToMap) {
            view.addOverlay(overlay);
        }

        Set<TrackOverlay> overlaysToRemoveFromMap = trackVisualizer.getGeoModelOverlaysToRemoveFromMap();
        for (TrackOverlay overlay : overlaysToRemoveFromMap) {
            view.removeOverlay(overlay);
        }
        toastText = trackVisualizer.createToastText();
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {

    }

    public void setSelectedItemIDs(Set<CharSequence> selectedItemIDs) {
        trackVisualizer.setSelectedItemIDs(selectedItemIDs);
    }

    public Set<CharSequence> getSelectedItemIDs() {
        return trackVisualizer.getSelectedItemIDs();
    }

    public String getToastText() {
        return toastText;
    }
}
