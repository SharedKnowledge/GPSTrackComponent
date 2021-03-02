package net.gpstrackapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IMapView;
import org.osmdroid.tileprovider.cachemanager.CacheManager;
import org.osmdroid.tileprovider.modules.SqliteArchiveTileWriter;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.TileSourcePolicyException;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.MapView;

import java.io.File;

public class DownloadTilesActivity extends MapViewActivity implements ActivityWithDescription, View.OnClickListener, SeekBar.OnSeekBarChangeListener, TextWatcher {
    private static final int TILE_COUNT_DOWNLOAD_LIMIT = 250;
    private SeekBar zoom_min, zoom_max;
    private EditText cache_north, cache_south, cache_east, cache_west, cache_output;
    private TextView cache_estimate, zoom_min_current, zoom_max_current;
    private Button executeJob;

    private AlertDialog alertDialog = null;
    private AlertDialog downloadPrompt = null;
    private SqliteArchiveTileWriter writer = null;
    private CacheManager mgr = null;

    @Override
    protected ViewGroup setupAndGetMapViewParentLayout() {
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
    protected Presenter setupAndGetPresenter() {
        return null;
    }

    @Override
    protected ITileSource getMapSpecificTileSource() {
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
                    showCacheManagerDialog();
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
            Log.e(getLogStart(), "problem on options item selected: " + e.getLocalizedMessage());
        }
        return false;
    }

    private void showCacheManagerDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set title
        alertDialogBuilder.setTitle(R.string.gpstracker_cache_manager);

        // set dialog message
        alertDialogBuilder.setItems(new CharSequence[]{
                        getResources().getString(R.string.gpstracker_cache_current_size),
                        getResources().getString(R.string.gpstracker_cache_download),
                        getResources().getString(R.string.cancel)
                }, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            try {
                                mgr = new CacheManager(mapView);
                            } catch (TileSourcePolicyException e) {
                                Log.e(getLogStart(), e.getLocalizedMessage());
                                dialog.dismiss();
                                return;
                            }
                            showCurrentCacheInfo();
                            break;
                        case 1:
                            downloadJobAlert();
                            break;
                        default:
                            dialog.dismiss();
                            break;
                    }
                }
        );
        // create alert dialog
        alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    private void downloadJobAlert() {
        View view = View.inflate(this, R.layout.gpstracker_tile_download_alert_dialog_layout, null);

        int zoom = (int) mapView.getZoomLevelDouble();

        BoundingBox boundingBox = mapView.getBoundingBox();
        zoom_max = view.findViewById(R.id.slider_zoom_max);
        zoom_max.setMax((int) mapView.getMaxZoomLevel());
        zoom_max.setProgress(zoom);
        zoom_max.setOnSeekBarChangeListener(this);
        zoom_max_current = view.findViewById(R.id.zoom_max_current);
        zoom_max_current.setText(String.valueOf(zoom_max.getProgress()));

        zoom_min = view.findViewById(R.id.slider_zoom_min);
        zoom_max.setMax((int) mapView.getMaxZoomLevel());
        zoom_min.setProgress(zoom);
        zoom_min.setOnSeekBarChangeListener(this);
        zoom_min_current = view.findViewById(R.id.zoom_min_current);
        zoom_min_current.setText(String.valueOf(zoom_min.getProgress()));

        cache_east = view.findViewById(R.id.cache_east);
        cache_east.setText(boundingBox.getLonEast() + "");
        cache_north = view.findViewById(R.id.cache_north);
        cache_north.setText(boundingBox.getLatNorth() + "");
        cache_south = view.findViewById(R.id.cache_south);
        cache_south.setText(boundingBox.getLatSouth() + "");
        cache_west = view.findViewById(R.id.cache_west);
        cache_west.setText(boundingBox.getLonWest() + "");
        cache_estimate = view.findViewById(R.id.cache_estimate);
        cache_output = view.findViewById(R.id.cache_output);

        //change listeners for both validation and to trigger the download estimation
        cache_east.addTextChangedListener(this);
        cache_north.addTextChangedListener(this);
        cache_south.addTextChangedListener(this);
        cache_west.addTextChangedListener(this);
        executeJob = view.findViewById(R.id.executeJob);
        executeJob.setOnClickListener(this);

        //prompt for input params
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(true)
                .setOnCancelListener(dialog -> {
                    cache_north = null;
                    cache_east = null;
                    cache_south = null;
                    cache_west = null;
                    cache_estimate = null;
                    cache_output = null;
                    zoom_min = null;
                    zoom_max = null;
                    executeJob = null;
                })
                .create();
        alertDialog.show();
        updateEstimate(false);
    }

    private void updateEstimate(boolean startJob) {
        String chooseDifferentTileSourceMessage = "Please choose a different tile source in the tile source settings and try again.";
        String downloadNotAllowedMessage = "Osmdroid does not allow downloads from this tile source. " + chooseDifferentTileSourceMessage;
        if (cache_north != null &&
                cache_east != null &&
                cache_south != null &&
                cache_west != null &&
                zoom_max != null &&
                zoom_min != null &&
                cache_output != null) {
            double n = Double.parseDouble(cache_north.getText().toString());
            double e = Double.parseDouble(cache_east.getText().toString());
            double s = Double.parseDouble(cache_south.getText().toString());
            double w = Double.parseDouble(cache_west.getText().toString());

            if (startJob) {
                String outputName = Environment.getExternalStorageDirectory().getAbsolutePath()
                        + File.separator + "osmdroid" + File.separator + cache_output.getText().toString()
                        + R.string.gpstracker_tile_download_file_extension;
                try {
                    writer = new SqliteArchiveTileWriter(outputName);
                } catch (Exception ex) {
                    Toast.makeText(this, "Could not create database at " + outputName, Toast.LENGTH_LONG).show();
                    Log.e(getLogStart(), "Could not create database at " + outputName
                            + System.lineSeparator() + "Error was: " + ex.getLocalizedMessage());
                }
                try {
                    mgr = new CacheManager(mapView, writer);
                } catch (TileSourcePolicyException ex) {
                    Toast.makeText(this, downloadNotAllowedMessage, Toast.LENGTH_LONG).show();
                    Log.e(getLogStart(), ex.getLocalizedMessage());
                    return;
                }
            } else {
                if (mgr == null) {
                    try {
                        mgr = new CacheManager(mapView);
                    } catch (TileSourcePolicyException ex) {
                        Toast.makeText(this, downloadNotAllowedMessage, Toast.LENGTH_LONG).show();
                        Log.e(getLogStart(), ex.getLocalizedMessage());
                        return;
                    }
                }
            }
            int zoomMin = zoom_min.getProgress();
            int zoomMax = zoom_max.getProgress();
            zoom_min_current.setText(String.valueOf(zoomMin));
            zoom_max_current.setText(String.valueOf(zoomMax));
            //nesw
            BoundingBox bb = new BoundingBox(n, e, s, w);
            int tilecount = mgr.possibleTilesInArea(bb, zoomMin, zoomMax);
            cache_estimate.setText(tilecount + " tiles");
            if (tilecount > TILE_COUNT_DOWNLOAD_LIMIT) {
                cache_estimate.setError("The tile count exceeds the allowed download limit of " + TILE_COUNT_DOWNLOAD_LIMIT + " Tiles per Download!");
                cache_estimate.requestFocus();
                executeJob.setEnabled(false);
                return;
            } else if (!executeJob.isEnabled()) {
                cache_estimate.setError(null);
                executeJob.setEnabled(true);
            }

            if (startJob) {
                if (downloadPrompt != null) {
                    downloadPrompt.dismiss();
                    downloadPrompt = null;
                }

                // has to be checked here because handling error in async task is very difficult
                ITileSource currentTileSource = mapView.getTileProvider().getTileSource();
                if (!(currentTileSource instanceof OnlineTileSourceBase)) {
                    String notOnlineTileSourceMessage = "TileSource is not an online tile source.";
                    Toast.makeText(this, notOnlineTileSourceMessage + chooseDifferentTileSourceMessage, Toast.LENGTH_LONG).show();
                    Log.e(getLogStart(), notOnlineTileSourceMessage);
                    return;
                } else if (!((OnlineTileSourceBase) currentTileSource).getTileSourcePolicy().acceptsBulkDownload()) {
                    Toast.makeText(this, downloadNotAllowedMessage, Toast.LENGTH_LONG).show();
                    Log.e(getLogStart(), downloadNotAllowedMessage);
                    return;
                } else {
                    try {
                        //this triggers the download
                        mgr.downloadAreaAsync(this, bb, zoomMin, zoomMax, new CacheManager.CacheManagerCallback() {
                            @Override
                            public void onTaskComplete() {
                                Toast.makeText(DownloadTilesActivity.this, "Download complete!", Toast.LENGTH_LONG).show();
                                if (writer != null)
                                    writer.onDetach();
                            }

                            @Override
                            public void onTaskFailed(int errors) {
                                Toast.makeText(DownloadTilesActivity.this, "Download complete with " + errors + " errors", Toast.LENGTH_LONG).show();
                                if (writer != null)
                                    writer.onDetach();
                            }

                            @Override
                            public void updateProgress(int progress, int currentZoomLevel, int zoomMin, int zoomMax) {

                            }

                            @Override
                            public void downloadStarted() {

                            }

                            @Override
                            public void setPossibleTilesInArea(int total) {

                            }
                        });
                    } catch (TileSourcePolicyException ex) {
                        Toast.makeText(this, downloadNotAllowedMessage, Toast.LENGTH_LONG).show();
                        Log.e(getLogStart(), ex.getLocalizedMessage());
                        return;
                    }
                }
            }
        }
    }

    private void showCurrentCacheInfo() {
        Toast.makeText(this, "Calculating...", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    DownloadTilesActivity.this);

            // set title
            alertDialogBuilder.setTitle(R.string.gpstracker_cache_manager)
                    .setMessage("Cache Capacity (bytes): " + mgr.cacheCapacity() + "\n" +
                            "Cache Usage (bytes): " + mgr.currentCacheUsage());

            // set dialog message
            alertDialogBuilder.setItems(new CharSequence[]{
                            getResources().getString(R.string.cancel)
                    }, (dialog, which) -> dialog.dismiss()
            );

            DownloadTilesActivity.this.runOnUiThread(() -> {
                // show it
                // create alert dialog
                final AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            });
        }).start();
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

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        updateEstimate(false);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        updateEstimate(false);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.executeJob:
                updateEstimate(true);
                break;
        }
    }
}
