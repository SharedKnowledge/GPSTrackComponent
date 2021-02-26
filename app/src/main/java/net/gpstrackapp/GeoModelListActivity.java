package net.gpstrackapp;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
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
            setContentView(R.layout.gpstracker_drawer_layout);

            // inflate layout in DrawerLayout
            DrawerLayout drawerLayout = findViewById(R.id.gpstracker_drawer_layout);
            View child = getLayoutInflater().inflate(R.layout.gpstracker_with_toolbar, null);
            drawerLayout.addView(child);

            GPSComponent.getGPSComponent().getASAPApplication().setupDrawerLayout(this);

            List<CharSequence> selectedItemIDsList = getIntent().getCharSequenceArrayListExtra("selectedItemIDs");
            if (selectedItemIDsList != null) {
                Set<CharSequence> selectedItemIDs = new HashSet<>(selectedItemIDsList);
                this.selectableContentSource.setPreselection(selectedItemIDs);
            }

            // setup toolbar
            Toolbar toolbar = (Toolbar) findViewById(R.id.gpstracker_list_toolbar);
            setSupportActionBar(toolbar);


            descriptionView = (TextView) findViewById(R.id.gpstracker_description);
            String selectionHint = ":" + System.lineSeparator()
                    + "(Un)Select tracks by clicking on them and press \'"
                    + getResources().getString(R.string.gpstracker_geomodel_list_selection_done_text)
                    + "\'.";
            String description = setActionText() + selectionHint;
            String additionalInfo = setOptionalAdditionalInfo();
            if (additionalInfo != null && !additionalInfo.equals("")) {
                description += " " + additionalInfo;
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
            Log.d(getLogStart(), "problems while setting up activity and content adapter: " + e.getLocalizedMessage());
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
}
