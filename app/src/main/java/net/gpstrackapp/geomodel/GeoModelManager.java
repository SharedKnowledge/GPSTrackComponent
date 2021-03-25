package net.gpstrackapp.geomodel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class GeoModelManager<T extends GeoModel> {
    protected List<T> geoModels = new ArrayList<>();
    protected static List<GeoModel> geoModelsGlobal = new ArrayList<>();

    public boolean addGeoModel(T geoModel) {
        geoModelsGlobal.add(geoModel);
        return geoModels.add(geoModel);
    }

    public boolean removeGeoModelByUUID(CharSequence UUID) {
        T geoModel = getGeoModelByUUID(UUID);
        if (geoModel != null) {
            geoModelsGlobal.remove(geoModel);
            return geoModels.remove(geoModel);
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

    public static List<GeoModel> getAllFromGlobal() {
        return geoModelsGlobal;
    }

    public int count() {
        return geoModels.size();
    }

    public static int countGlobal() {
        return geoModelsGlobal.size();
    }

    public T getGeoModelByUUID(CharSequence UUID) {
        for (T geoModel : geoModels) {
            if (geoModel.getObjectId().equals(UUID)) {
                return geoModel;
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

    public static GeoModel getGeoModelByUUIDFromGlobal(CharSequence UUID) {
        for (GeoModel geoModel : geoModelsGlobal) {
            if (geoModel.getObjectId().equals(UUID)) {
                return geoModel;
            }
        }
        return null;
    }

    public static Set<GeoModel> getGeoModelsByUUIDsFromGlobal(Set<CharSequence> UUIDs) {
        Iterator<CharSequence> iterator = UUIDs.iterator();
        Set<GeoModel> selectedGeoModels = new HashSet<>();
        while (iterator.hasNext()) {
            GeoModel geoModel = getGeoModelByUUIDFromGlobal(iterator.next());
            if (geoModel != null) {
                selectedGeoModels.add(geoModel);
            }
        }
        return selectedGeoModels;
    }
}
