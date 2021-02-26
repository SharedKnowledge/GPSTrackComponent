package net.gpstrackapp.geomodel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class GeoModelManager<T extends GeoModel> {
    protected List<T> geoModels = new ArrayList<>();

    public boolean addGeoModel(T geoModel) {
        return geoModels.add(geoModel);
    }

    public boolean removeGeoModelByUUID(CharSequence UUID) {
        T track = getGeoModelByUUID(UUID);
        if (track != null) {
            return geoModels.remove(track);
        } else {
            return false;
        }
    }

    public boolean removeGeoModelsByUUIDs(Set<CharSequence> UUIDs) {
        boolean allRemoved = true;
        Iterator<CharSequence> iterator = UUIDs.iterator();
        while (iterator.hasNext()) {
            boolean removed = removeGeoModelByUUID(iterator.next());
            allRemoved = allRemoved ? removed : false;
        }
        return allRemoved;
    }

    public List<T> getAll() {
        return geoModels;
    }

    public int count() {
        return geoModels.size();
    }

    public T getGeoModelByUUID(CharSequence UUID) {
        for (int i = 0; i < geoModels.size(); i++) {
            if (geoModels.get(i).getObjectId().equals(UUID)) {
                return geoModels.get(i);
            }
        }
        return null;
    }

    public Set<T> getGeoModelsByUUIDs(Set<CharSequence> UUIDs) {
        Iterator<CharSequence> iterator = UUIDs.iterator();
        Set<T> selectedGeoModels = new HashSet<>();
        while (iterator.hasNext()) {
            T geoModel = getGeoModelByUUID(iterator.next());
            if (geoModel != null) {
                selectedGeoModels.add(geoModel);
            }
        }
        return selectedGeoModels;
    }
}
