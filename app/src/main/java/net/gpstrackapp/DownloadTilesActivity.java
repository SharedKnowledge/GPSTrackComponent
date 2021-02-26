package net.gpstrackapp;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

public class DownloadTilesActivity extends MapViewActivity {

    @Override
    protected ConfiguredMapView setupMapViewAndGet() {
        mapView = new ConfiguredMapView(this);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        return mapView;
    }

    @Override
    protected ViewGroup setupLayoutAndGet() {
        setContentView(R.layout.gpstracker_tracker_mapview_drawer_layout);
        Toolbar toolbar = findViewById(R.id.gpstracker_tracker_mapview_with_toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = findViewById(R.id.gpstracker_tracker_mapview_drawer_layout);
        DrawerLayout.LayoutParams params = new DrawerLayout.LayoutParams(MapView.LayoutParams.MATCH_PARENT,
                MapView.LayoutParams.MATCH_PARENT);
        params.setMargins(0, (int) getResources().getDimension(R.dimen.marginUnderToolbar), 0, 0);
        mapView.setLayoutParams(params);

        return drawerLayout;
    }

    @Override
    protected Presenter setupPresenterAndGet() {
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(getLogStart(), "init action buttons");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gpstracker_tile_download_action_buttons, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch (item.getItemId()) {
                case R.id.confirm_item:
                    return true;
                case R.id.abort_item:
                    this.finish();
                    return true;
                default:
                    // If we got here, the user's action was not recognized.
                    // Invoke the superclass to handle it.
                    return super.onOptionsItemSelected(item);
            }
        } catch (Exception e) {
            Log.d(getLogStart(), "problem on options item selected: " + e.getLocalizedMessage());
        }
        return false;
    }

    private String getLogStart() {
        return getClass().getSimpleName();
    }
}
