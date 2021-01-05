package net.gpstrackapp.geomodel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public abstract class GeoModel<T extends GeoModel> {
    private String objectId = UUID.randomUUID().toString();
    private CharSequence objectName;
    private final Date dateOfCreation;
    private CharSequence creator;
    private boolean displayed = false;

    public GeoModel(CharSequence objectName, Date dateOfCreation, CharSequence creator) {
        this.objectName = objectName;
        this.dateOfCreation = dateOfCreation;
        this.creator = creator;
    }

    public String getObjectId() {
        return objectId;
    }

    public CharSequence getObjectName() {
        return objectName;
    }

    public Date getDateOfCreation() {
        return dateOfCreation;
    }

    public CharSequence getCreator() {
        return creator;
    }

    public boolean isDisplayed() {
        return displayed;
    }
}
