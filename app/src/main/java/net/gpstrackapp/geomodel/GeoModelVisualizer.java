package net.gpstrackapp.geomodel;

import android.util.Log;
import android.widget.Toast;

import net.gpstrackapp.ConfiguredMapView;
import net.gpstrackapp.overlay.GeoModelOverlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class GeoModelVisualizer<K extends GeoModel, V extends GeoModelOverlay> {
    protected Map<K, V> geoModelWithOverlayHolder = new HashMap<>();
    protected ConfiguredMapView mapView;
    private Set<CharSequence> selectedItemIDs = new HashSet<>();

    public GeoModelVisualizer(ConfiguredMapView mapView) {
        this.mapView = mapView;
    }

    public void setSelectedItemIDs(Set<CharSequence> selectedItemIDs) {
        this.selectedItemIDs = selectedItemIDs;
    }

    public Set<CharSequence> getSelectedItemIDs() {
        return selectedItemIDs;
    }

    public void updateGeoModelsOnMapView() {
        Set<K> displayedGeoModels = geoModelWithOverlayHolder.keySet();

        Set<K> geoModelsToRemoveFromMap = new HashSet<>(displayedGeoModels);

        Iterator<CharSequence> iterSelect = selectedItemIDs.iterator();
        List<K> addedGeoModels = new ArrayList<>();
        while (iterSelect.hasNext()) {
            CharSequence itemID = iterSelect.next();
            K geoModel = getGeoModelByUUID(itemID);
            // if geoModel is null then it was deleted since the last map update
            if (geoModel != null) {
                // add every Track that is not yet displayed to the map
                if (!displayedGeoModels.contains(geoModel)) {
                    addGeoModelToHolder(geoModel);
                    addedGeoModels.add(geoModel);
                }
                // remove all Tracks that have to get displayed again from the Set
                geoModelsToRemoveFromMap.remove(geoModel);
            }
        }

        Iterator<K> iterRemove = geoModelsToRemoveFromMap.iterator();
        while (iterRemove.hasNext()) {
            K geoModelToRemove = iterRemove.next();
            removeGeoModelFromHolder(geoModelToRemove);
        }
        List<K> removedGeoModels = new ArrayList<>(geoModelsToRemoveFromMap);

        String toastText = createToastText(addedGeoModels, removedGeoModels);
        if (!toastText.isEmpty()) {
            Toast.makeText(mapView.getContext(), toastText, Toast.LENGTH_LONG).show();
        }
        mapView.invalidate();

        Log.d(getLogStart(), "updateGeoModelsOnMapView: " + mapView.getOverlays().size());
    }

    private void addGeoModelToHolder(K geoModel) {
        V geoModelOverlay = createGeoModelOverlay(geoModel, mapView);
        geoModelWithOverlayHolder.put(geoModel, geoModelOverlay);
        mapView.getOverlays().add(geoModelOverlay);
        Log.d(getLogStart(), "Add Overlay with UUID " + geoModel.getObjectId());
    }

    private void removeGeoModelFromHolder(K geoModel) {
        V geoModelOverlay = geoModelWithOverlayHolder.get(geoModel);
        geoModelWithOverlayHolder.remove(geoModel);
        mapView.getOverlays().remove(geoModelOverlay);
        Log.d(getLogStart(), "Remove Overlay with UUID " + geoModel.getObjectId());
    }

    private String createToastText(List<K> addedGeoModels, List<K> removedGeoModels) {
        String toastText = "";
        if (addedGeoModels.size() > 0) {
            toastText += "Added \"" + addedGeoModels.get(0).getObjectName() + "\"";
            if (addedGeoModels.size() > 1) {
                toastText += " and " + (addedGeoModels.size() - 1) + " more";
            }
            toastText += " to the map.";
        }
        if (removedGeoModels.size() > 0) {
            toastText += toastText.equals("") ? "" : System.lineSeparator();
            toastText += "Removed \"" + removedGeoModels.get(0).getObjectName() + "\"";
            if (removedGeoModels.size() > 1) {
                toastText += " and " + (removedGeoModels.size() - 1) + " more";
            }
            toastText += " from the map.";
        }
        return toastText;
    }

    protected abstract V createGeoModelOverlay(K geoModel, ConfiguredMapView mapView);
    protected abstract K getGeoModelByUUID(CharSequence uuid);

    private String getLogStart() {
        return this.getClass().getSimpleName();
    }
}
