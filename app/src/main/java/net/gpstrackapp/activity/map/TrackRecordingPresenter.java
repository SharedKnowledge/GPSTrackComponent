package net.gpstrackapp.activity.map;

import android.widget.Toast;

import net.gpstrackapp.activity.LifecycleObject;
import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.geomodel.track.TrackVisualizationManager;
import net.gpstrackapp.overlay.TrackOverlay;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    public void onPause() {

    }

    @Override
    public void onResume() {
        //add and remove TrackOverlays
        trackVisualizer.updateGeoModelHolder();

        Map<Track, TrackOverlay> addToMap = trackVisualizer.getGeoModelOverlaysToAddToMap();
        Map<Track, TrackOverlay> removeFromMap = trackVisualizer.getGeoModelOverlaysToRemoveFromMap();

        for (Map.Entry<Track, TrackOverlay> entry : addToMap.entrySet()) {
            TrackOverlay trackOverlay = entry.getValue();
            view.addOverlay(trackOverlay);
        }
        for (Map.Entry<Track, TrackOverlay> entry : removeFromMap.entrySet()) {
            TrackOverlay trackOverlay = entry.getValue();
            view.removeOverlay(trackOverlay);
        }
        toastText = trackVisualizer.createToastText(
                addToMap.keySet().stream().collect(Collectors.toList()),
                removeFromMap.keySet().stream().collect(Collectors.toList()));
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
