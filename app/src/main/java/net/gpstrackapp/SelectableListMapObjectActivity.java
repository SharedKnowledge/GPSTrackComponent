package net.gpstrackapp;

import net.sharksystem.asap.android.apps.ASAPActivity;

public abstract class SelectableListMapObjectActivity extends ASAPActivity {
    public SelectableListMapObjectActivity() {
        super(GPSComponent.getGPSComponent().getASAPApplication());
    }
}
