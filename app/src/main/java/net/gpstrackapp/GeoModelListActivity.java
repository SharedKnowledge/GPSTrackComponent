package net.gpstrackapp;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import net.gpstrackapp.geomodel.RequestGeoModelsCommand;
import net.sharksystem.asap.android.Util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class GeoModelListActivity extends SelectableListGeoModelActivity implements ActivityWithDescription {
    private RecyclerView recyclerView;
    private TextView descriptionView;
    private GeoModelListContentAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(getLogStart(), "onCreate");

        try {
            setContentView(R.layout.gpstracker_list_geomodels_drawer_layout);

            List<CharSequence> selectedItemIDsList = getIntent().getCharSequenceArrayListExtra("selectedItemIDs");
            if (selectedItemIDsList != null) {
                Set<CharSequence> selectedItemIDs = new HashSet<>(selectedItemIDsList);
                this.selectableContentSource.setPreselection(selectedItemIDs);
            }

            // setup toolbar
            Toolbar toolbar = (Toolbar) findViewById(R.id.gpstracker_list_geomodels_toolbar);
            setSupportActionBar(toolbar);

            descriptionView = (TextView) findViewById(R.id.gpstracker_description);

            String description = setActionText();
            String additionalInfo = addOptionalAdditionalInfo();
            if (additionalInfo != null && !additionalInfo.equals("")) {
                description += ":" + System.lineSeparator() + additionalInfo;
            }
            descriptionView.setText(description);

            recyclerView = (RecyclerView) findViewById(R.id.gpstracker_list_geomodels_recycler_view);

            adapter = new GeoModelListContentAdapter(this, this.selectableContentSource, createRequestGeoModelsCommand());
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(adapter);
            Log.d(getLogStart(), "attached content adapter");
        } catch (Exception e) {
            Log.e(getLogStart(), "problems while setting up activity and content adapter: " + e.getLocalizedMessage());
        }
    }

    protected abstract RequestGeoModelsCommand createRequestGeoModelsCommand();

    protected void onResume() {
        super.onResume();
        if(this.adapter != null) {
            Log.d(Util.getLogStart(this), "onResume: assume data set changed.");
            this.adapter.notifyDataSetChanged();
        } else {
            Log.e(Util.getLogStart(this), "onResume: content adapter not initialized?!");
        }
    }

    private String getLogStart() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String addOptionalAdditionalInfo() {
        return "(Un)Select tracks by clicking on them and press \'"
                + getResources().getString(R.string.gpstracker_list_geomodels_selection_done_text) + "\'. "
                + addUserDescription();
    }

    public abstract String addUserDescription();
}
