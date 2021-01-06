package net.gpstrackapp;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import net.sharksystem.asap.android.Util;

public abstract class MapObjectListActivity extends SelectableListMapObjectActivity {
    private RecyclerView recyclerView;
    private MapObjectListContentAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(this.getLogStart(), "onCreate");

        try {
            setContentView(R.layout.gpstracker_map_drawer_layout);

            this.getASAPApplication().setupDrawerLayout(this);

            recyclerView = (RecyclerView) findViewById(R.id.gpstracker_map_recycler_view);

            adapter = new MapObjectListContentAdapter(this, this.selectableContentSource);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(adapter);
            Log.d(this.getLogStart(), "attached content adapter");
        } catch (Exception e) {
            Log.d(this.getLogStart(), "problems while setting up activity and content adapter: " + e.getLocalizedMessage());
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
}
