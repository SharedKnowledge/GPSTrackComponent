package net.gpstrackapp.activity.map;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v7.widget.Toolbar;

import net.gpstrackapp.R;
import net.gpstrackapp.activity.ActivityWithDescription;
import net.gpstrackapp.format.FileUtils;
import net.gpstrackapp.mapview.ConfiguredMapFragment;
import net.gpstrackapp.mapview.DownloadableTilesMapView;
import net.sharksystem.asap.android.Util;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.cachemanager.CacheManager;
import org.osmdroid.tileprovider.modules.SqliteArchiveTileWriter;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourcePolicyException;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.gridlines.LatLonGridlineOverlay2;

import java.io.File;

public class DownloadTilesActivity extends AppCompatActivity implements ActivityWithDescription, ActivityWithAdditionalMapOverlays, View.OnClickListener, SeekBar.OnSeekBarChangeListener, TextWatcher {
    private static final int TILE_DOWNLOAD_LIMIT_COUNT = 250;
    private int zoomMinTileSource, zoomMaxTileSource;

    private SeekBar zoomMinSeekBar, zoomMaxSeekBar;
    private EditText cacheNorth, cacheSouth, cacheEast, cacheWest, cacheOutput;
    private TextView cacheEstimate, zoomMinCurrent, zoomMaxCurrent;
    private Button executeJob, cancelAlert;

    private AlertDialog alertDialog = null;
    private AlertDialog downloadPrompt = null;
    private SqliteArchiveTileWriter writer = null;
    private CacheManager mgr = null;

    private ConfiguredMapFragment configuredMapFragment;
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        FragmentManager fragmentManager = this.getSupportFragmentManager();
        // to ensure that the fragment is only attached once
        if (fragmentManager.findFragmentByTag(ConfiguredMapFragment.TAG) == null) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("downloadable", true);
            configuredMapFragment = new ConfiguredMapFragment();
            configuredMapFragment.setArguments(bundle);
            fragmentManager.beginTransaction()
                    .add(R.id.gpstracker_mapfragment_container, configuredMapFragment, ConfiguredMapFragment.TAG)
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView = configuredMapFragment.getMapView();
    }

    @Override
    public void setupAdditionalOverlays(MapView mapView) {
        mapView.getOverlays().add(new LatLonGridlineOverlay2());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(Util.getLogStart(this), "init action buttons");
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
            Log.e(Util.getLogStart(this), "problem on options item selected: " + e.getLocalizedMessage());
        }
        return false;
    }

    private void showCacheManagerDialog() {
        DownloadableTilesMapView mapView = (DownloadableTilesMapView) configuredMapFragment.getMapView();
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
                                Log.e(Util.getLogStart(this), e.getLocalizedMessage());
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

        zoomMinTileSource = mapView.getTileProvider().getTileSource().getMinimumZoomLevel();
        zoomMaxTileSource = mapView.getTileProvider().getTileSource().getMaximumZoomLevel();

        int zoom = (int) mapView.getZoomLevelDouble();
        if (zoom < zoomMinTileSource) {
            zoom = zoomMinTileSource;
        } else if (zoom > zoomMaxTileSource) {
            zoom = zoomMaxTileSource;
        }
        mapView.getController().setZoom((double) zoom);

        BoundingBox boundingBox = mapView.getBoundingBox();
        zoomMaxSeekBar = view.findViewById(R.id.gpstracker_tile_download_alert_dialog_slider_zoom_max);
        zoomMaxSeekBar.setMax(zoomMaxTileSource - zoomMinTileSource);
        zoomMaxSeekBar.setProgress(zoom - zoomMinTileSource);
        zoomMaxSeekBar.setOnSeekBarChangeListener(this);
        zoomMaxCurrent = view.findViewById(R.id.gpstracker_tile_download_alert_dialog_zoom_max_current);
        zoomMaxCurrent.setText(String.valueOf(zoomMaxSeekBar.getProgress()));

        zoomMinSeekBar = view.findViewById(R.id.gpstracker_tile_download_alert_dialog_slider_zoom_min);
        zoomMinSeekBar.setMax(zoomMaxTileSource - zoomMinTileSource);
        zoomMinSeekBar.setProgress(zoom - zoomMinTileSource);
        zoomMinSeekBar.setOnSeekBarChangeListener(this);
        zoomMinCurrent = view.findViewById(R.id.gpstracker_tile_download_alert_dialog_zoom_min_current);
        zoomMinCurrent.setText(String.valueOf(zoomMinSeekBar.getProgress()));

        cacheEast = view.findViewById(R.id.gpstracker_tile_download_alert_dialog_cache_east);
        cacheEast.setText(boundingBox.getLonEast() + "");
        cacheNorth = view.findViewById(R.id.gpstracker_tile_download_alert_dialog_cache_north);
        cacheNorth.setText(boundingBox.getLatNorth() + "");
        cacheSouth = view.findViewById(R.id.gpstracker_tile_download_alert_dialog_cache_south);
        cacheSouth.setText(boundingBox.getLatSouth() + "");
        cacheWest = view.findViewById(R.id.gpstracker_tile_download_alert_dialog_cache_west);
        cacheWest.setText(boundingBox.getLonWest() + "");
        cacheEstimate = view.findViewById(R.id.gpstracker_tile_download_alert_dialog_cache_estimate);
        cacheOutput = view.findViewById(R.id.gpstracker_tile_download_alert_dialog_cache_output);

        //change listeners for both validation and to trigger the download estimation
        cacheEast.addTextChangedListener(this);
        cacheNorth.addTextChangedListener(this);
        cacheSouth.addTextChangedListener(this);
        cacheWest.addTextChangedListener(this);
        executeJob = view.findViewById(R.id.gpstracker_tile_download_alert_dialog_execute_job);
        executeJob.setOnClickListener(this);
        cancelAlert = view.findViewById(R.id.gpstracker_tile_download_alert_dialog_cancel_alert);
        cancelAlert.setOnClickListener(this);

        //prompt for input params
        downloadPrompt = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(true)
                .setOnCancelListener(dialog -> {
                    cacheNorth = null;
                    cacheEast = null;
                    cacheSouth = null;
                    cacheWest = null;
                    cacheEstimate = null;
                    cacheOutput = null;
                    zoomMinSeekBar = null;
                    zoomMaxSeekBar = null;
                    executeJob = null;
                })
                .create();
        downloadPrompt.show();
        updateEstimate(false);
    }

    private void updateEstimate(boolean startJob) {
        String chooseDifferentTileSourceMessage = "Please choose a different tile source in the tile source settings and try again.";
        String downloadNotAllowedMessage = "Osmdroid does not allow downloads from this tile source. " + chooseDifferentTileSourceMessage;
        if (cacheNorth != null &&
                cacheEast != null &&
                cacheSouth != null &&
                cacheWest != null &&
                zoomMaxSeekBar != null &&
                zoomMinSeekBar != null &&
                cacheOutput != null) {
            double n, e, s, w;
            try {
                n = Double.parseDouble(cacheNorth.getText().toString());
                e = Double.parseDouble(cacheEast.getText().toString());
                s = Double.parseDouble(cacheSouth.getText().toString());
                w = Double.parseDouble(cacheWest.getText().toString());
            } catch (Exception ex) {
                Toast.makeText(this, "Input cannot be parsed: " + ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.e(Util.getLogStart(this), ex.getLocalizedMessage());
                return;
            }

            if (startJob) {
                String cacheOutputName = cacheOutput.getText().toString();
                Log.d(Util.getLogStart(this), "outputName: " + cacheOutputName);
                if (!FileUtils.isValidFileName(cacheOutputName)) {
                    Toast.makeText(this, "File name contains illegal character(s)", Toast.LENGTH_SHORT).show();
                    Log.e(Util.getLogStart(this), "File name contains illegal character(s)");
                    return;
                }
                String outputName = Configuration.getInstance().getOsmdroidBasePath()
                        + File.separator + cacheOutputName
                        + getResources().getString(R.string.gpstracker_tile_download_file_extension);
                try {
                    writer = new SqliteArchiveTileWriter(outputName);
                } catch (Exception ex) {
                    Toast.makeText(this, "Could not create database at " + outputName, Toast.LENGTH_LONG).show();
                    Log.e(Util.getLogStart(this), "Could not create database at " + outputName
                            + System.lineSeparator() + "Error was: " + ex.getLocalizedMessage());
                    return;
                }

                try {
                    mgr = new CacheManager(mapView, writer);
                } catch (TileSourcePolicyException ex) {
                    Toast.makeText(this, downloadNotAllowedMessage, Toast.LENGTH_LONG).show();
                    Log.e(Util.getLogStart(this), ex.getLocalizedMessage());
                    return;
                }
            } else {
                if (mgr == null) {
                    try {
                        mgr = new CacheManager(mapView);
                    } catch (TileSourcePolicyException ex) {
                        Toast.makeText(this, downloadNotAllowedMessage, Toast.LENGTH_LONG).show();
                        Log.e(Util.getLogStart(this), ex.getLocalizedMessage());
                        return;
                    }
                }
            }
            int zoomMin = zoomMinSeekBar.getProgress() + zoomMinTileSource;
            int zoomMax = zoomMaxSeekBar.getProgress() + zoomMinTileSource;
            zoomMinCurrent.setText(String.valueOf(zoomMin));
            zoomMaxCurrent.setText(String.valueOf(zoomMax));

            BoundingBox bb;
            try {
                bb = new BoundingBox(n, e, s, w);
            } catch (Exception ex) {
                Toast.makeText(this, ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            int tilecount = mgr.possibleTilesInArea(bb, zoomMin, zoomMax);
            cacheEstimate.setText(tilecount + " tiles");
            if (tilecount > TILE_DOWNLOAD_LIMIT_COUNT) {
                cacheEstimate.setError("The tile count exceeds the allowed download limit of " + TILE_DOWNLOAD_LIMIT_COUNT + " Tiles per Download!");
                cacheEstimate.setClickable(true);
                executeJob.setEnabled(false);
                return;
            } else if (!executeJob.isEnabled()) {
                cacheEstimate.setError(null);
                cacheEstimate.setClickable(false);
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
                    Log.e(Util.getLogStart(this), notOnlineTileSourceMessage);
                    return;
                } else if (!((OnlineTileSourceBase) currentTileSource).getTileSourcePolicy().acceptsBulkDownload()) {
                    Toast.makeText(this, downloadNotAllowedMessage, Toast.LENGTH_LONG).show();
                    Log.e(Util.getLogStart(this), downloadNotAllowedMessage);
                    return;
                } else {
                    try {
                        //this triggers the download
                        mgr.downloadAreaAsync(this, bb, zoomMin, zoomMax, new CacheManager.CacheManagerCallback() {
                            @Override
                            public void onTaskComplete() {
                                Toast.makeText(DownloadTilesActivity.this, "Download complete!", Toast.LENGTH_LONG).show();
                                Log.d(Util.getLogStart(this), "Download complete");
                                if (writer != null) {
                                    writer.onDetach();
                                }
                            }

                            @Override
                            public void onTaskFailed(int errors) {
                                Toast.makeText(DownloadTilesActivity.this, "Download complete with " + errors + " errors", Toast.LENGTH_LONG).show();
                                Log.d(Util.getLogStart(this), "Download complete with " + errors + " errors");
                                if (writer != null) {
                                    writer.onDetach();
                                }
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
                        Log.e(Util.getLogStart(this), ex.getLocalizedMessage());
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
            case R.id.gpstracker_tile_download_alert_dialog_execute_job:
                updateEstimate(true);
                break;
            case R.id.gpstracker_tile_download_alert_dialog_cancel_alert:
                if (downloadPrompt != null) {
                    if (downloadPrompt.isShowing()) {
                        downloadPrompt.dismiss();
                    }
                }
                break;
        }
    }
}
