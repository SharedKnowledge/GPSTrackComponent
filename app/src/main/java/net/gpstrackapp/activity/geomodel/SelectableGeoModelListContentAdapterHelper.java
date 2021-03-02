package net.gpstrackapp.activity.geomodel;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

class SelectableGeoModelListContentAdapterHelper {
    private Set<CharSequence> selectedItemIDs = new HashSet<>();
    private Set<CharSequence> uuidSet = new HashSet<>();
    private Set<CharSequence> preselectedUUIDSet = null;

    void setPreselection(Set<CharSequence> preselection) {
        this.preselectedUUIDSet = preselection;
    }

    void setSelectedText(CharSequence itemID, View selectableItemView, TextView selectedTextView) {
        Log.d(getLogStart(), "setSelectedText()");
        selectableItemView.setTag(itemID);
        selectedTextView.setText(
                this.selectedItemIDs.contains(itemID) ? "SELECTED" : "");
    }

    void setSelectedText(CharSequence itemID, CharSequence uid,
                         View selectableItemView, TextView selectedTextView) {

        if(this.preselectedUUIDSet != null && !preselectedUUIDSet.isEmpty()) {
            if(this.preselectedUUIDSet.remove(uid)) { // pre-select once
                this.selectedItemIDs.add(itemID);
                this.uuidSet.add(uid);
            }
        }

        this.setSelectedText(itemID, selectableItemView, selectedTextView);
    }

    void onAction(RecyclerView.Adapter adapter, View view, CharSequence uuid) {
        CharSequence itemID = (CharSequence) view.getTag();

        if(this.selectedItemIDs.contains(itemID)) {
            this.selectedItemIDs.remove(itemID);
            this.uuidSet.remove(uuid);
        } else {
            this.selectedItemIDs.add(itemID);
            this.uuidSet.add(uuid);
        }

        adapter.notifyDataSetChanged();
    }

    Set<CharSequence> getSelectedUUIDs() {
        return this.uuidSet;
    }

    private String getLogStart() {
        return this.getClass().getSimpleName();
    }
}
