package net.gpstrackapp;

import android.content.Context;

public class AttributeContainer {
    private ReusableTrackMapView reusableTrackMapView;

    public AttributeContainer(Context ctx) {
        this.reusableTrackMapView = new ReusableTrackMapView(ctx);
    }

    public void setReusableTrackMapView(ReusableTrackMapView reusableTrackMapView) {
        this.reusableTrackMapView = reusableTrackMapView;
    }

    public ReusableTrackMapView getReusableTrackMapView() {
        return reusableTrackMapView;
    }
}
