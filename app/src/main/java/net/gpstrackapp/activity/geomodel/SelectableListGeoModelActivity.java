package net.gpstrackapp.activity.geomodel;

import android.support.v7.app.AppCompatActivity;

import java.util.Set;

public abstract class SelectableListGeoModelActivity extends AppCompatActivity {
    protected SelectableGeoModelListContentAdapterHelper selectableContentSource = new SelectableGeoModelListContentAdapterHelper();

    Set<CharSequence> getSelectedItemIDs() {
        return this.selectableContentSource.getSelectedUUIDs();
    }
}
