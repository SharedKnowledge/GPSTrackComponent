package net.gpstrackapp.geomodel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class GeoModelManager<T extends GeoModel> {
    protected List<T> geoModels = new ArrayList<>();
    protected static List<GeoModel> geoModelsGlobal = new ArrayList<>();

    public boolean addGeoModel(T geoModel) {
        geoModelsGlobal.add(geoModel);
        return geoModels.add(geoModel);
    }

    public boolean removeGeoModelByUUID(CharSequence uuid) {
        T geoModel = getGeoModelByUUID(uuid);
        if (geoModel != null) {
            geoModelsGlobal.remove(geoModel);
            return geoModels.remove(geoModel);
        } else {
            return false;
        }
    }

    public boolean removeGeoModelsByUUIDs(Set<CharSequence> uuids) {
        boolean allRemoved = true;
        for (CharSequence uuid : uuids) {
            boolean removed = removeGeoModelByUUID(uuid);
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

    public T getGeoModelByUUID(CharSequence uuid) {
        for (T geoModel : geoModels) {
            if (geoModel.getObjectID().equals(uuid)) {
                return geoModel;
            }
        }
        return null;
    }

    public Set<T> getGeoModelsByUUIDs(Set<CharSequence> uuids) {
        Set<T> selectedGeoModels = new HashSet<>();
        for (CharSequence uuid : uuids) {
            T geoModel = getGeoModelByUUID(uuid);
            if (geoModel != null) {
                selectedGeoModels.add(geoModel);
            }
        }
        return selectedGeoModels;
    }

    public static GeoModel getGeoModelByUUIDFromGlobal(CharSequence uuid) {
        for (GeoModel geoModel : geoModelsGlobal) {
            if (geoModel.getObjectID().equals(uuid)) {
                return geoModel;
            }
        }
        return null;
    }

    public static Set<GeoModel> getGeoModelsByUUIDsFromGlobal(Set<CharSequence> uuids) {
        Set<GeoModel> selectedGeoModels = new HashSet<>();
        for (CharSequence uuid : uuids) {
            GeoModel geoModel = getGeoModelByUUIDFromGlobal(uuid);
            if (geoModel != null) {
                selectedGeoModels.add(geoModel);
            }
        }
        return selectedGeoModels;
    }
}
