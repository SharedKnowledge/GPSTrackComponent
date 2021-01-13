package net.gpstrackapp.geomodel;

import android.os.Parcelable;

import net.gpstrackapp.GPSComponent;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class GeoModel implements Serializable {
    private CharSequence objectId;
    private CharSequence objectName;
    private final Date dateOfCreation;
    private CharSequence creator;

    public GeoModel(CharSequence objectId, CharSequence objectName, CharSequence creator, Date dateOfCreation) {
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

    public Date getDateOfCreation() {
        return dateOfCreation;
    }

    public String getDateOfCreationAsFormattedString() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(getDateOfCreation());
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
