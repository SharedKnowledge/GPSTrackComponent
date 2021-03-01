package net.gpstrackapp.geomodel;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

public abstract class GeoModel implements Serializable {
    private static final long serialVersionUID = 0;
    private static String formatterPattern = "yyyy-MM-dd HH:mm:ss";
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatterPattern);

    private CharSequence objectId;
    private CharSequence objectName;
    private LocalDateTime dateOfCreation;
    private CharSequence creator;

    public GeoModel(CharSequence objectId, CharSequence objectName, CharSequence creator, LocalDateTime dateOfCreation) {
        this.objectId = objectId == null ? UUID.randomUUID().toString() : objectId;
        this.objectName = objectName;
        this.creator = creator;
        this.dateOfCreation = dateOfCreation;
    }

    public void setObjectName(CharSequence objectName) {
        this.objectName = objectName;
    }

    public void setDateOfCreation(LocalDateTime dateOfCreation) {
        this.dateOfCreation = dateOfCreation;
    }

    public void setCreator(CharSequence creator) {
        this.creator = creator;
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
        return dateOfCreation != null ? formatter.format(dateOfCreation) : null;
    }

    public CharSequence getCreator() {
        return creator;
    }

    public static String getFormatterPattern() {
        return formatterPattern;
    }

    public static DateTimeFormatter getFormatter() {
        return formatter;
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
