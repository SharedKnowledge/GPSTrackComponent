package net.gpstrackapp.activity.map;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import net.gpstrackapp.GPSComponent;
import net.gpstrackapp.R;
import net.gpstrackapp.activity.geomodel.track.DeleteTracksActivity;
import net.gpstrackapp.activity.geomodel.track.DisplayTracksActivity;
import net.gpstrackapp.activity.geomodel.track.ExportTracksActivity;
import net.gpstrackapp.activity.geomodel.track.ImportTracksActivity;
import net.gpstrackapp.activity.geomodel.track.MergeTracksActivity;
import net.gpstrackapp.activity.geomodel.track.SaveTracksActivity;
import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.geomodel.track.TrackModelManager;
import net.gpstrackapp.geomodel.track.TrackSegment;
import net.gpstrackapp.mapview.ConfiguredMapFragment;
import net.gpstrackapp.recording.TrackRecorder;

import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;

public class TrackRecordingMapActivity extends AppCompatActivity implements ActivityWithAdditionalMapOverlays {
    private static final int DISPLAY_ACTIVITY_REQUEST_CODE = 0;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private static final String WRITE_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String ACCESS_FINE_LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;

    private ConfiguredMapFragment configuredMapFragment;
    private TrackRecorder trackRecorder;
    private TrackRecordingPresenter trackRecordingPresenter;
    private final TrackModelManager trackModelManager = GPSComponent.getGPSComponent().getTrackModelManager();
    private boolean askedForPermissions = false;

    // invalidate map when going online
    private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                configuredMapFragment.invalidateMapView();
            } catch (NullPointerException e) {
                Log.e(getLogStart(), "mapView is null");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(getLogStart(), "onCreate");
        if (!askedForPermissions) {
            askedForPermissions = true;
            requestPermissionsIfNecessary(new String[] {
                    // needed to show the current location (location provider)
                    ACCESS_FINE_LOCATION_PERMISSION,
                    // WRITE_EXTERNAL_STORAGE is required for offline tile provider and storing files (e.g. track export)
                    WRITE_EXTERNAL_STORAGE_PERMISSION
            });
        }

        IConfigurationProvider conf = Configuration.getInstance();
        // use external storage directory if permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // archives are placed here
            conf.setOsmdroidBasePath(new File(Environment.getExternalStorageDirectory()
                    + File.separator + "osmdroid"));
            // tile cache db
            conf.setOsmdroidTileCache(new File(Environment.getExternalStorageDirectory()
                    + File.separator + "osmdroid" + File.separator + "tiles"));
        }

        setContentView(R.layout.gpstracker_tracker_mapview_drawer_layout);
        Toolbar toolbar = findViewById(R.id.gpstracker_tracker_mapview_toolbar);
        setSupportActionBar(toolbar);

        configuredMapFragment = new ConfiguredMapFragment();
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.gpstracker_mapfragment_container, configuredMapFragment)
                .commit();

        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        trackRecorder = new TrackRecorder(this);
        trackRecorder.onCreate();

        trackRecordingPresenter = new TrackRecordingPresenter(new ViewWithOverlays() {
            @Override
            public void addOverlay(Overlay overlay) {
                configuredMapFragment.addOverlay(overlay);
            }

            @Override
            public void removeOverlay(Overlay overlay) {
                configuredMapFragment.removeOverlay(overlay);
            }
        });
        trackRecordingPresenter.onCreate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        trackRecorder.onStart();
        trackRecordingPresenter.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        trackRecorder.onResume();
        trackRecordingPresenter.onResume();

        String toastText = trackRecordingPresenter.getToastText();
        if (!toastText.isEmpty()) {
            Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        trackRecorder.onPause();
        trackRecordingPresenter.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        trackRecorder.onStop();
        trackRecordingPresenter.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(getLogStart(), "onDestroy");
        trackRecorder.onDestroy();
        trackRecordingPresenter.onDestroy();
        unregisterReceiver(networkReceiver);
        super.onDestroy();
    }

    @Override
    public void setupAdditionalOverlays(MapView mapView) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(mapView);
        scaleBarOverlay.setCentred(true);
        scaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, (int) getResources().getDimension(R.dimen.margin_top_scale_bar));
        mapView.getOverlays().add(scaleBarOverlay);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(getLogStart(), "init action buttons");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gpstracker_tracker_mapview_action_buttons, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        adjustMenuToRecordingState(menu);
        return true;
    }

    // Always called before onResume, so the track overlays can be added/removed in onResume
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DISPLAY_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                List<CharSequence> selectedItemIDsList = data.getCharSequenceArrayListExtra("selectedItemIDs");
                Set<CharSequence> selectedItemIDs = new HashSet<>(selectedItemIDsList);
                trackRecordingPresenter.setSelectedItemIDs(selectedItemIDs);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putCharSequenceArrayList("itemIDsToRestore", new ArrayList<>(trackRecordingPresenter.getSelectedItemIDs()));
        if (trackRecorder.getRecordedTrack() != null) {
            savedInstanceState.putCharSequence("recordedTrack", trackRecorder.getRecordedTrack().getObjectId());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("itemIDsToRestore")) {
            Set<CharSequence> selectedItemIDs = new HashSet<>(savedInstanceState.getCharSequenceArrayList("itemIDsToRestore"));
            trackRecordingPresenter.setSelectedItemIDs(selectedItemIDs);
        }
        if (savedInstanceState.containsKey("recordedTrack")) {
            CharSequence trackID = savedInstanceState.getCharSequence("recordedTrack");
            Track track = trackModelManager.getGeoModelByUUID(trackID);
            trackRecorder.registerLocationConsumer(track);
        }
    }

    private void adjustMenuToRecordingState(Menu menu) {
        Log.d(getLogStart(), "Adjust menu");
        MenuItem recordingItem = menu.findItem(R.id.record_item);
        if (trackRecorder.isRecordingTrack()) {
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
                // --- map tiles ---
                case R.id.tile_settings_item:
                    startMapTileSettingsActivity();
                    return true;
                case R.id.download_item:
                    startDownloadTilesActivity();
                    return true;

                // --- tracks ---
                case R.id.record_item:
                    if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION_PERMISSION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (!trackRecorder.isRecordingTrack()) {
                            showTrackNameDialog();
                        } else {
                            Track recordedTrack = trackRecorder.getRecordedTrack();
                            trackRecorder.unregisterLocationConsumer(recordedTrack);
                            invalidateOptionsMenu();
                            showSaveTrackDialog(recordedTrack);
                        }
                    } else {
                        Toast.makeText(this, "You cannot record tracks without granting location permission", Toast.LENGTH_LONG).show();
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
                case BUTTON_NEGATIVE:
                    dialog.dismiss();
                    return;
            }

            TrackSegment trackSegment = new TrackSegment(null);
            Track track = new Track(null, trackName,
                    GPSComponent.getGPSComponent().getASAPApplication().getOwnerName(),
                    LocalDateTime.now(), trackSegment);
            trackModelManager.addGeoModel(track);
            trackRecorder.registerLocationConsumer(track);
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
                .setPositiveButton("Yes", (dialog, which) -> {
                    startSaveTracksActivity(track);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void startMapTileSettingsActivity() {
        Intent intent = new Intent(this, MapTileSettingsActivity.class);
        startActivity(intent);
    }

    private void startDownloadTilesActivity() {
        Intent intent = new Intent(this, DownloadTilesActivity.class);
        GeoPoint lastLocation = configuredMapFragment.getLastLocation();
        if (lastLocation != null) {
            intent.putExtra("lat", lastLocation.getLatitude());
            intent.putExtra("lon", lastLocation.getLongitude());
        }
        double zoomLevel = configuredMapFragment.getZoomLevel();
        if (zoomLevel != ConfiguredMapFragment.DEFAULT_ZOOM_LEVEL) {
            intent.putExtra("zoom", zoomLevel);
        }
        startActivity(intent);
    }


    private void startDisplayTracksActivity() {
        Intent intent = new Intent(this, DisplayTracksActivity.class);
        Set<CharSequence> selectedItemIDs = trackRecordingPresenter.getSelectedItemIDs();
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(getLogStart(), "onRequestPermissionsResult");
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                // recreate this activity if any permissions were granted
                recreate();
                break;
            }
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        Log.d(getLogStart(), "requestPermissionsIfNecessary");
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // TODO shouldShowRequestPermissionRationale can be added to explain to the user why permissions are required
                // Permission is not yet granted
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