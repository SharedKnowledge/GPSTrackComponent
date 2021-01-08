package net.gpstrackapp;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import net.sharksystem.asap.android.Util;

public abstract class MapObjectListActivity extends SelectableListMapObjectActivity {
    private RecyclerView recyclerView;
    private MapObjectListContentAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(getLogStart(), "onCreate");

        try {
            setContentView(R.layout.gpstracker_list_drawer_layout);

            GPSComponent.getGPSComponent().getASAPApplication().setupDrawerLayout(this);

            //TODO preselect in subclass (by using abstract method and implementing it e.g. in DisplayTracksActivity)

            // setup toolbar
            Toolbar toolbar = (Toolbar) findViewById(R.id.gpstracker_list_with_toolbar);
            setSupportActionBar(toolbar);

            recyclerView = (RecyclerView) findViewById(R.id.gpstracker_list_recycler_view);

            //TODO zusaetzlich GeoModelManager uebergeben, da dieser gebraucht wird
            adapter = new MapObjectListContentAdapter(this, this.selectableContentSource);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(adapter);
            Log.d(getLogStart(), "attached content adapter");
        } catch (Exception e) {
            Log.d(getLogStart(), "problems while setting up activity and content adapter: " + e.getLocalizedMessage());
        }
    }

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
