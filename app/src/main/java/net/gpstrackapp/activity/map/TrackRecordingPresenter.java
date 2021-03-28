package net.gpstrackapp.activity.map;

import net.gpstrackapp.activity.LifecycleObject;
import net.gpstrackapp.geomodel.track.TrackVisualizationManager;
import net.gpstrackapp.mapview.TrackOverlay;

import java.util.Set;

public class TrackRecordingPresenter implements LifecycleObject {
    private TrackVisualizationManager trackVisualizer;
    private ViewWithOverlays view;
    private String toastText = "";

    public TrackRecordingPresenter(ViewWithOverlays view) {
        this.trackVisualizer = new TrackVisualizationManager();
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

        Set<TrackOverlay> addToMap = trackVisualizer.getGeoModelOverlaysToAddToMap();
        Set<TrackOverlay> removeFromMap = trackVisualizer.getGeoModelOverlaysToRemoveFromMap();

        for (TrackOverlay trackOverlay : addToMap) {
            view.addOverlay(trackOverlay);
        }
        for (TrackOverlay trackOverlay : removeFromMap) {
            view.removeOverlay(trackOverlay);
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
