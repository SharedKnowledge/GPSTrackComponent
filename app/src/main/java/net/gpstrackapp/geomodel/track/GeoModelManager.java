package net.gpstrackapp.geomodel.track;

import android.util.Log;

import net.gpstrackapp.MyMapView;
import net.gpstrackapp.geomodel.GeoModel;
import net.gpstrackapp.overlay.GeoModelOverlay;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public abstract class GeoModelManager <K extends GeoModel, V extends GeoModelOverlay> {
    protected Map<K, V> geoModelWithOverlayHolder = new HashMap<>();
    protected MyMapView mapView;
    private Set<CharSequence> selectedItemIDs = new HashSet<>();

    public GeoModelManager(MyMapView mapView) {
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
        while (iterSelect.hasNext()) {
            CharSequence itemID = iterSelect.next();
            K geoModel = getGeoModelByUUID(itemID);
            // add every Track that is not yet displayed to the map
            if (!displayedGeoModels.contains(geoModel)) {
                addGeoModelToHolder(geoModel);
            }
            // remove all Tracks that have to get displayed again from the Set
            geoModelsToRemoveFromMap.remove(geoModel);
        }

        Iterator<K> iterRemove = geoModelsToRemoveFromMap.iterator();
        while (iterRemove.hasNext()) {
            K geoModelToRemove = iterRemove.next();
            removeGeoModelFromHolder(geoModelToRemove);
        }
        mapView.invalidate();
    }

    private void addGeoModelToHolder(K geoModel) {
        V geoModelOverlay = createGeoModelOverlay(geoModel);
        geoModelWithOverlayHolder.put(geoModel, geoModelOverlay);
        mapView.getOverlays().add(geoModelOverlay);
        Log.d(getLogStart(), "add Overlay with uuid " + geoModel.getObjectId());
    }

    private void removeGeoModelFromHolder(K geoModel) {
        V geoModelOverlay = geoModelWithOverlayHolder.get(geoModel);
        geoModelWithOverlayHolder.remove(geoModel);
        mapView.getOverlays().remove(geoModelOverlay);
        Log.d(getLogStart(), "remove Overlay with uuid " + geoModel.getObjectId());
    }

    protected abstract V createGeoModelOverlay(K geoModel);

    protected abstract K getGeoModelByUUID(CharSequence uuid);

    private String getLogStart() {
        return this.getClass().getSimpleName();
    }
}
