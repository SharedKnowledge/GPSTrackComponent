package net.gpstrackapp.activity.geomodel.track;

import android.content.Intent;

import net.gpstrackapp.activity.geomodel.GeoModelListSelectionActivity;
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

    @Override
    public String setActionText() {
        return "Display tracks";
    }

    @Override
    public String addUserDescription() {
        return "Already displayed tracks are preselected.";
    }
}
