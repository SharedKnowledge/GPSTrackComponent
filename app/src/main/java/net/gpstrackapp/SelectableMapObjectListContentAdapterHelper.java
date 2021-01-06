package net.gpstrackapp;

import android.view.View;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

class SelectableMapObjectListContentAdapterHelper {
    private Set<String> selectedItemIDs = new HashSet<>();
    private Set<String> uuidSet = new HashSet<>();

    void setSelectedText(CharSequence itemID, View selectableItemView, TextView selectedTextView) {
        selectableItemView.setTag(itemID);
        selectedTextView.setText(
                this.selectedItemIDs.contains(itemID) ? "SELECTED" : "");
    }

    Set<String> getSelectedUUIDs() {
        return this.uuidSet;
    }
}
