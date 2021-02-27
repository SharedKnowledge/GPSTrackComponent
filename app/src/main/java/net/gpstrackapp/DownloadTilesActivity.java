package net.gpstrackapp;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

public class DownloadTilesActivity extends MapViewActivity implements ActivityWithDescription {

    @Override
    protected ConfiguredMapView setupMapViewAndGet() {
        mapView = new ConfiguredMapView(this);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        return mapView;
    }

    @Override
    protected ViewGroup setupLayoutAndGet() {
        setContentView(R.layout.gpstracker_tile_download_drawer_layout);
        Toolbar toolbar = findViewById(R.id.gpstracker_tile_download_toolbar);
        setSupportActionBar(toolbar);

        TextView descriptionView = (TextView) findViewById(R.id.gpstracker_description);
        String description = setActionText();
        String additionalInfo = addOptionalAdditionalInfo();
        if (additionalInfo != null && !additionalInfo.equals("")) {
            description += ":" + System.lineSeparator() + additionalInfo;
        }
        descriptionView.setText(description);

        RelativeLayout relativeLayout = findViewById(R.id.gpstracker_tile_download_layout_with_toolbar);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(MapView.LayoutParams.MATCH_PARENT,
                MapView.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.BELOW, R.id.gpstracker_tile_download_description);
        mapView.setLayoutParams(params);

        return relativeLayout;
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

    @Override
    public String setActionText() {
        return "Download map tiles";
    }

    @Override
    public String addOptionalAdditionalInfo() {
        return "Adjust the map to show the area you want to download. Press \'"
                + getResources().getString(R.string.gpstracker_item_map_download_tiles_done_text) + "\' once you've finished.";
    }
}
