package net.gpstrackapp;

import android.content.Intent;

import net.gpstrackapp.geomodel.RequestGeoModelsCommand;
import net.gpstrackapp.geomodel.track.RequestTracksCommand;

import java.util.ArrayList;
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
