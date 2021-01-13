package net.gpstrackapp;

import android.content.Intent;

import net.gpstrackapp.geomodel.GeoModel;
import net.gpstrackapp.geomodel.track.TrackManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DisplayTracksActivity extends GeoModelListSelectionActivity {

    @Override
    protected void onSelectionFinished(Set<CharSequence> selectedItemIDs) {
        Intent intent = new Intent();
        intent.putCharSequenceArrayListExtra("selectedItemIDs", new ArrayList<>(selectedItemIDs));
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected RequestGeoModelsCommand createRequestGeoModelsCommand() {
        return new RequestTracksCommand();
    }
}
