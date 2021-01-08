package net.gpstrackapp;

import android.support.v7.app.AppCompatActivity;

import net.sharksystem.asap.android.apps.ASAPActivity;

import java.util.Set;

public abstract class SelectableListMapObjectActivity extends AppCompatActivity {
    protected SelectableMapObjectListContentAdapterHelper selectableContentSource = new SelectableMapObjectListContentAdapterHelper();

    Set<String> getSelectedItemIDs() {
        return this.selectableContentSource.getSelectedUUIDs();
    }
}
