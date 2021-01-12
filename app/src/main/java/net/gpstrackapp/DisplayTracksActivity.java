package net.gpstrackapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.geomodel.track.TrackManager;
import net.gpstrackapp.overlay.DisplayTrackCommand;
import net.gpstrackapp.overlay.HideTrackCommand;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DisplayTracksActivity extends MapObjectListSelectionActivity {

    @Override
    protected void onSelectionFinished(Set<CharSequence> selectedItemIDs) {
        Intent intent = new Intent();
        intent.putCharSequenceArrayListExtra("selectedItemIDs", new ArrayList<>(selectedItemIDs));
        setResult(RESULT_OK, intent);
        finish();
    }
}
