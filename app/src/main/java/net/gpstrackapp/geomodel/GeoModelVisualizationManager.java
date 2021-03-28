package net.gpstrackapp.geomodel;

import android.util.Log;

import net.gpstrackapp.mapview.GeoModelOverlay;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class GeoModelVisualizationManager<K extends GeoModel, V extends GeoModelOverlay<K>> {
    protected Map<K, V> geoModelWithOverlayHolder = new HashMap<>();
    protected Map<K, V> geoModelOverlaysToAddToMap = new HashMap<>();
    protected Map<K, V> geoModelOverlaysToRemoveFromMap = new HashMap<>();
    private Set<CharSequence> selectedItemIDs = new HashSet<>();

    protected abstract V createGeoModelOverlay(K geoModel);
    protected abstract K getGeoModelByUUID(CharSequence uuid);

    public void setSelectedItemIDs(Set<CharSequence> selectedItemIDs) {
        this.selectedItemIDs = selectedItemIDs;
    }

    public Set<CharSequence> getSelectedItemIDs() {
        return selectedItemIDs;
    }

    public Map<K, V> getGeoModelOverlaysToAddToMap() {
        return geoModelOverlaysToAddToMap;
    }

    public Map<K, V> getGeoModelOverlaysToRemoveFromMap() {
        return geoModelOverlaysToRemoveFromMap;
    }

    public void updateGeoModelHolder() {
        geoModelOverlaysToRemoveFromMap = new HashMap<>(geoModelWithOverlayHolder);

        geoModelOverlaysToAddToMap = new HashMap<>();
        for (CharSequence itemID : selectedItemIDs) {
            K geoModel = getGeoModelByUUID(itemID);
            // if geoModel is null then it was deleted since the last map update
            if (geoModel != null) {
                // add every Track that is not yet displayed
                if (!geoModelWithOverlayHolder.containsKey(geoModel)) {
                    addGeoModelToHolder(geoModel);
                }
                // remove all Tracks that have to get displayed again
                geoModelOverlaysToRemoveFromMap.remove(geoModel);
            }
        }

        for (K geoModelToRemove : geoModelOverlaysToRemoveFromMap.keySet()) {
            removeGeoModelFromHolder(geoModelToRemove);
        }
    }

    private void addGeoModelToHolder(K geoModel) {
        V geoModelOverlay = createGeoModelOverlay(geoModel);
        geoModelWithOverlayHolder.put(geoModel, geoModelOverlay);
        geoModelOverlaysToAddToMap.put(geoModel, geoModelOverlay);
        Log.d(getLogStart(), "Add Overlay with UUID " + geoModel.getObjectID());
    }

    private void removeGeoModelFromHolder(K geoModel) {
        V geoModelOverlay = geoModelWithOverlayHolder.get(geoModel);
        geoModelWithOverlayHolder.remove(geoModel);
        Log.d(getLogStart(), "Remove Overlay with UUID " + geoModel.getObjectID());
    }

    public String createToastText(List<K> addedGeoModels, List<K> removedGeoModels) {
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

    private String getLogStart() {
        return GeoModelVisualizationManager.class.getSimpleName();
    }
}
