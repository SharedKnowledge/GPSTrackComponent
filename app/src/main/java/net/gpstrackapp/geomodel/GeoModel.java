package net.gpstrackapp.geomodel;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

public abstract class GeoModel implements Serializable {
    private CharSequence objectId;
    private CharSequence objectName;
    private final LocalDateTime dateOfCreation;
    private CharSequence creator;

    public GeoModel(CharSequence objectId, CharSequence objectName, CharSequence creator, LocalDateTime dateOfCreation) {
        this.objectId = objectId == null ? UUID.randomUUID().toString() : objectId;
        this.objectName = objectName;
        this.creator = creator;
        this.dateOfCreation = dateOfCreation;
    }

    public CharSequence getObjectId() {
        return objectId;
    }

    public CharSequence getObjectName() {
        return objectName;
    }

    public LocalDateTime getDateOfCreation() {
        return dateOfCreation;
    }

    public String getDateOfCreationAsFormattedString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return formatter.format(dateOfCreation);
    }

    public CharSequence getCreator() {
        return creator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GeoModel geoModel = (GeoModel) o;
        return objectId.equals(geoModel.objectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectId);
    }
}
