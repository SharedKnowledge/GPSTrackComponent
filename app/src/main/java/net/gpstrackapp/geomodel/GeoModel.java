package net.gpstrackapp.geomodel;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

public abstract class GeoModel implements Serializable {
    private static final long serialVersionUID = 0;
    private static String formatterPattern = "yyyy-MM-dd HH:mm:ss";
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatterPattern);

    private CharSequence objectID;
    private CharSequence objectName;
    private LocalDateTime dateOfCreation;
    private CharSequence creator;

    public GeoModel(CharSequence objectID, CharSequence objectName, CharSequence creator, LocalDateTime dateOfCreation) {
        this.objectID = objectID == null ? UUID.randomUUID().toString() : objectID;
        setObjectName(objectName);
        setCreator(creator);
        setDateOfCreation(dateOfCreation);
    }

    public void setObjectName(CharSequence objectName) {
        this.objectName = objectName == null ? "" : objectName;
    }

    public void setCreator(CharSequence creator) {
        this.creator = creator == null ? "" : creator;
    }

    public void setDateOfCreation(LocalDateTime dateOfCreation) {
        if (dateOfCreation != null) {
            if (dateOfCreation.isAfter(LocalDateTime.now())) {
                this.dateOfCreation = LocalDateTime.now();
                return;
            }
        }
        this.dateOfCreation = dateOfCreation;
    }

    public CharSequence getObjectID() {
        return objectID;
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
        return objectID.equals(geoModel.objectID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectID);
    }
}
