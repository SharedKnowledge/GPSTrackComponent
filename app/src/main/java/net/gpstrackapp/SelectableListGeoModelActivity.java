package net.gpstrackapp;

import android.support.v7.app.AppCompatActivity;

import net.sharksystem.asap.android.apps.ASAPActivity;

import java.util.Set;

public abstract class SelectableListGeoModelActivity extends AppCompatActivity {
    protected SelectableGeoModelListContentAdapterHelper selectableContentSource = new SelectableGeoModelListContentAdapterHelper();

    Set<CharSequence> getSelectedItemIDs() {
        return this.selectableContentSource.getSelectedUUIDs();
    }
}
