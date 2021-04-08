package net.gpstrackapp.geomodel;

import net.gpstrackapp.mapview.GeoModelOverlay;

import java.util.HashMap;
import java.util.HashSet;
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

    public Set<V> getGeoModelOverlaysToAddToMap() {
        return new HashSet<>(geoModelOverlaysToAddToMap.values());
    }

    public Set<V> getGeoModelOverlaysToRemoveFromMap() {
        return new HashSet<>(geoModelOverlaysToRemoveFromMap.values());
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
    }

    private void removeGeoModelFromHolder(K geoModel) {
        V geoModelOverlay = geoModelWithOverlayHolder.get(geoModel);
        geoModelWithOverlayHolder.remove(geoModel);
    }

    public String createToastText() {
        String toastText = "";
        if (geoModelOverlaysToAddToMap.size() > 0) {
            // use the name of any object in the map
            toastText += "Added \"" + geoModelOverlaysToAddToMap.values().iterator().next().getGeoModel().getObjectName() + "\"";
            if (geoModelOverlaysToAddToMap.size() > 1) {
                toastText += " and " + (geoModelOverlaysToAddToMap.size() - 1) + " more";
            }
            toastText += " to the map.";
        }
        if (geoModelOverlaysToRemoveFromMap.size() > 0) {
            toastText += toastText.equals("") ? "" : System.lineSeparator();
            // use the name of any object in the map
            toastText += "Removed \"" + geoModelOverlaysToRemoveFromMap.values().iterator().next().getGeoModel().getObjectName() + "\"";
            if (geoModelOverlaysToRemoveFromMap.size() > 1) {
                toastText += " and " + (geoModelOverlaysToRemoveFromMap.size() - 1) + " more";
            }
            toastText += " from the map.";
        }
        return toastText;
    }

    private String getLogStart() {
        return GeoModelVisualizationManager.class.getSimpleName();
    }
}
