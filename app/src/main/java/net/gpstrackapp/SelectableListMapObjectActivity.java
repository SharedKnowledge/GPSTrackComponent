package net.gpstrackapp;

import net.sharksystem.asap.android.apps.ASAPActivity;

import java.util.Set;

public abstract class SelectableListMapObjectActivity extends ASAPActivity {
    protected SelectableMapObjectListContentAdapterHelper selectableContentSource = new SelectableMapObjectListContentAdapterHelper();

    public SelectableListMapObjectActivity() {
        super(GPSComponent.getGPSComponent().getASAPApplication());
    }

    Set<String> getSelectedItemIDs() {
        return this.selectableContentSource.getSelectedUUIDs();
    }
}
