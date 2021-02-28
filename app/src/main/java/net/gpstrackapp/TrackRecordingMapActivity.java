package net.gpstrackapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;

import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.geomodel.track.TrackModelManager;
import net.gpstrackapp.geomodel.track.TrackSegment;
import net.gpstrackapp.overlay.TrackOverlay;
import net.sharksystem.asap.ASAPException;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.content.DialogInterface.BUTTON_POSITIVE;

/*
TODO On first run after install SOMETIMES onPause() and onResume() of this activity get called alternately in a never ending
    loop, from the second run onwards everything works completely fine. The reason for the call to onPause() escapes me.
    It might have something to do with the LocationService and the missing ACCESS_FINE_LOCATION permission, that the user has
    to agree to. The service doesn't get started and returns START_STICKY but doesn't get restarted for some reason? This would
    explain why everything works fine on the second run as all permissions are already granted.
*/
public class TrackRecordingMapActivity extends MapViewActivity {
    private static final int DISPLAY_ACTIVITY_REQUEST_CODE = 0;
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private Context ctx;
    private static Map<Track, TrackOverlay> trackWithOverlayHolder = new HashMap<>();
    private TrackRecordingPresenter trackRecordingPresenter;
    private TrackModelManager trackModelManager = GPSComponent.getGPSComponent().getTrackModelManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            ctx = GPSComponent.getGPSComponent().getContext().getApplicationContext();
        } catch (ASAPException e) {
            Log.e(getLogStart(), e.getLocalizedMessage());
        }
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        super.onCreate(savedInstanceState);

        requestPermissionsIfNecessary(new String[] {
                // needed to show the current location
                Manifest.permission.ACCESS_FINE_LOCATION,
                // WRITE_EXTERNAL_STORAGE is required in order to show the map
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });

        Log.d(getLogStart(), "onCreate");
    }

    @Override
    protected Presenter setupAndGetPresenter() {
        trackRecordingPresenter = new TrackRecordingPresenter(mapView);
        return trackRecordingPresenter;
    }

    @Override
    protected ITileSource getMapSpecificTileSource() {
        return null;
    }

    // Always called before onResume, so the overlays can be added/removed in onResume
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DISPLAY_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                List<CharSequence> selectedItemIDsList = data.getCharSequenceArrayListExtra("selectedItemIDs");
                Set<CharSequence> selectedItemIDs = new HashSet<>(selectedItemIDsList);
                trackRecordingPresenter.getTrackVisualizer().setSelectedItemIDs(selectedItemIDs);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putCharSequenceArrayList("itemIDsToRestore", new ArrayList<>(trackRecordingPresenter.getTrackVisualizer().getSelectedItemIDs()));
        if (trackRecordingPresenter.getRecordedTrack() != null) {
            savedInstanceState.putCharSequence("recordedTrack", trackRecordingPresenter.getRecordedTrack().getObjectId());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("itemIDsToRestore")) {
            Set<CharSequence> selectedItemIDs = new HashSet<>(savedInstanceState.getCharSequenceArrayList("itemIDsToRestore"));
            trackRecordingPresenter.getTrackVisualizer().setSelectedItemIDs(selectedItemIDs);
        }
        if (savedInstanceState.containsKey("recordedTrack")) {
            CharSequence trackID = savedInstanceState.getCharSequence("recordedTrack");
            Track track = trackModelManager.getGeoModelByUUID(trackID);
            trackRecordingPresenter.registerLocationConsumer(track);
        }
    }

    public static Map<Track, TrackOverlay> getTrackWithOverlayHolder() {
        return trackWithOverlayHolder;
    }

    @Override
    protected ViewGroup setupAndGetMapViewParentLayout() {
        setContentView(R.layout.gpstracker_tracker_mapview_drawer_layout);
        Toolbar toolbar = findViewById(R.id.gpstracker_tracker_mapview_toolbar);
        setSupportActionBar(toolbar);

        RelativeLayout relativeLayout = findViewById(R.id.gpstracker_tracker_mapview_layout_with_toolbar);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(MapView.LayoutParams.MATCH_PARENT,
                MapView.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.BELOW);
        mapView.setLayoutParams(params);

        return relativeLayout;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(getLogStart(), "init action buttons");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gpstracker_tracker_mapview_action_buttons, menu);
        adjustMenuToRecordingState(menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        adjustMenuToRecordingState(menu);
        return true;
    }

    private void adjustMenuToRecordingState(Menu menu) {
        MenuItem recordingItem = menu.findItem(R.id.record_item);
        if (trackRecordingPresenter.isRecordingTrack()) {
            recordingItem.setTitle(getResources().getString(R.string.gpstracker_item_tracks_stop_record_button_text));
            menu.findItem(R.id.track_item).getSubMenu().setGroupEnabled(R.id.group_track_actions, false);
            menu.findItem(R.id.map_item).getSubMenu().setGroupEnabled(R.id.group_map_actions, false);
        } else {
            recordingItem.setTitle(getResources().getString(R.string.gpstracker_item_tracks_start_record_button_text));
            menu.findItem(R.id.track_item).getSubMenu().setGroupEnabled(R.id.group_track_actions, true);
            menu.findItem(R.id.map_item).getSubMenu().setGroupEnabled(R.id.group_map_actions, true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch (item.getItemId()) {
                case R.id.tile_settings_item:
                    startMapTileSettingsActivity();
                    return true;
                case R.id.download_item:
                    startDownloadTilesActivity();
                    return true;
                case R.id.record_item:
                    if (!trackRecordingPresenter.isRecordingTrack()) {
                        showTrackNameDialog();
                    } else {
                        Track recordedTrack = trackRecordingPresenter.getRecordedTrack();
                        trackRecordingPresenter.unregisterLocationConsumer(recordedTrack);
                        invalidateOptionsMenu();
                        showSaveTrackDialog(recordedTrack);
                    }
                    return true;
                case R.id.display_item:
                    startDisplayTracksActivity();
                    return true;
                case R.id.merge_item:
                    startMergeTracksActivity();
                    return true;
                case R.id.save_item:
                    startSaveTracksActivity(null);
                    return true;
                case R.id.delete_item:
                    startDeleteTracksActivity();
                    return true;
                case R.id.import_item:
                    startImportTracksActivity();
                    return true;
                case R.id.export_item:
                    startExportTracksActivity();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        } catch (Exception e) {
            Log.e(getLogStart(), e.getLocalizedMessage());
        }
        return false;
    }

    private void showTrackNameDialog() {
        final EditText input = new EditText(this);

        String name = GPSComponent.getGPSComponent().getASAPApplication().getOwnerName() + "\'s track " + (trackModelManager.count() + 1);
        input.setText(name);
        input.setSelectAllOnFocus(true);

        DialogInterface.OnClickListener listener = (dialog, which) -> {
            String trackName = null;
            switch (which) {
                case BUTTON_POSITIVE:
                    trackName = input.getText().toString();
                    break;
            }
            TrackSegment trackSegment = new TrackSegment(null);
            Track track = new Track(null, trackName,
                    GPSComponent.getGPSComponent().getASAPApplication().getOwnerName(),
                    LocalDateTime.now(), trackSegment);
            trackModelManager.addGeoModel(track);
            trackRecordingPresenter.registerLocationConsumer(track);
            invalidateOptionsMenu();
        };

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setView(input)
                .setTitle("Choose a name for the new track")
                .setPositiveButton(android.R.string.ok, listener)
                .setNegativeButton(android.R.string.cancel, listener)
                .create();
        alertDialog.show();
    }

    private void showSaveTrackDialog(final Track track) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Save recorded track to storage?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    startSaveTracksActivity(track);
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void startMapTileSettingsActivity() {
        Intent intent = new Intent(this, MapTileSettingsActivity.class);
        startActivity(intent);
    }

    private void startDownloadTilesActivity() {
        Intent intent = new Intent(this, DownloadTilesActivity.class);
        GeoPoint lastLocation = mapView.getLastLocation();
        if (lastLocation != null) {
            intent.putExtra("lat", lastLocation.getLatitude());
            intent.putExtra("lon", lastLocation.getLongitude());
        }
        double zoomLevel = mapView.getZoomLevelDouble();
        if (zoomLevel != DEFAULT_ZOOM_LEVEL) {
            intent.putExtra("zoom", mapView.getZoomLevelDouble());
        }
        startActivity(intent);
    }

    private void startDisplayTracksActivity() {
        Intent intent = new Intent(this, DisplayTracksActivity.class);
        Set<CharSequence> selectedItemIDs = trackRecordingPresenter.getTrackVisualizer().getSelectedItemIDs();
        intent.putCharSequenceArrayListExtra("selectedItemIDs", new ArrayList<>(selectedItemIDs));
        startActivityForResult(intent, DISPLAY_ACTIVITY_REQUEST_CODE);
    }

    private void startMergeTracksActivity() {
        Intent intent = new Intent(this, MergeTracksActivity.class);
        startActivity(intent);
    }

    private void startSaveTracksActivity(Track trackToSave) {
        Intent intent = new Intent(this, SaveTracksActivity.class);
        if (trackToSave != null) {
            ArrayList<CharSequence> list = new ArrayList<>(Arrays.asList(trackToSave.getObjectId()));
            intent.putCharSequenceArrayListExtra("selectedItemIDs", list);
        }
        startActivity(intent);
    }

    private void startDeleteTracksActivity() {
        Intent intent = new Intent(this, DeleteTracksActivity.class);
        startActivity(intent);
    }

    private void startImportTracksActivity() {
        Intent intent = new Intent(this, ImportTracksActivity.class);
        startActivity(intent);
    }

    private void startExportTracksActivity() {
        Intent intent = new Intent(this, ExportTracksActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private String getLogStart() {
        return this.getClass().getSimpleName();
    }
}