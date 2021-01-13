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
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;

import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.geomodel.track.TrackManager;
import net.gpstrackapp.overlay.TrackOverlay;
import net.sharksystem.asap.ASAPException;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//TODO permission handling, maybe in superclass or split up
public class TrackRecordingMapActivity extends MapViewActivity {
    private static final int DISPLAY_ACTIVITY_REQUEST_CODE = 0;
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private Context ctx;
    private static Map<Track, TrackOverlay> trackWithOverlayHolder = new HashMap<>();
    private TrackRecordingPresenter trackRecordingPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(getLogStart(), "onCreate");
        /*
        Log.d("TrackRecordingMapActivity", String.valueOf(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)));
        Log.d("TrackRecordingMapActivity", String.valueOf(Environment.isExternalStorageRemovable()));
        Log.d("TrackRecordingMapActivity", String.valueOf(Configuration.getInstance().getOsmdroidBasePath()));
        */
        try {
            ctx = GPSComponent.getGPSComponent().getContext();
        } catch (ASAPException e) {
            //TODO anpassen
            e.printStackTrace();
        }
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        requestPermissionsIfNecessary(new String[] {
                // if you need to show the current location, uncomment the line below
                Manifest.permission.ACCESS_FINE_LOCATION,
                // WRITE_EXTERNAL_STORAGE is required in order to show the map
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });
    }

    @Override
    protected Presenter setupPresenterAndGet() {
        trackRecordingPresenter = new TrackRecordingPresenter(mapView);
        return trackRecordingPresenter;
    }

    // Always called before onResume, so the overlays can be added/removed in onResume
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DISPLAY_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                List<CharSequence> selectedItemIDsList = data.getCharSequenceArrayListExtra("selectedItemIDs");
                Set<CharSequence> selectedItemIDs = new HashSet<>(selectedItemIDsList);
                trackRecordingPresenter.getTrackManager().setSelectedItemIDs(selectedItemIDs);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putCharSequenceArrayList("savedItemIDs", new ArrayList<>(trackRecordingPresenter.getTrackManager().getSelectedItemIDs()));
        if (trackRecordingPresenter.getRecordedTrack() != null) {
            savedInstanceState.putCharSequence("recordedTrack", trackRecordingPresenter.getRecordedTrack().getObjectId());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("savedItemIDs")) {
            Set<CharSequence> selectedItemIDs = new HashSet<>(savedInstanceState.getCharSequenceArrayList("savedItemIDs"));
            trackRecordingPresenter.getTrackManager().setSelectedItemIDs(selectedItemIDs);
        }
        if (savedInstanceState.containsKey("recordedTrack")) {
            CharSequence trackID = savedInstanceState.getCharSequence("recordedTrack");
            Track track = TrackManager.getTrackByUUID(trackID);
            trackRecordingPresenter.restartTrackRecording(track);
        }
    }

    ////////////////////////////////////////////////////////////////////////
    //                           mapView methods                          //
    ////////////////////////////////////////////////////////////////////////

    @Override
    protected ConfiguredMapView setupMapViewAndGet() {
        mapView = new ConfiguredMapView(this);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        return mapView;
    }

    public static Map<Track, TrackOverlay> getTrackWithOverlayHolder() {
        return trackWithOverlayHolder;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(getLogStart(), "init Toolbar");
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
            Log.d(getLogStart(), "disable group");
        } else {
            recordingItem.setTitle(getResources().getString(R.string.gpstracker_item_tracks_start_record_button_text));
            menu.findItem(R.id.track_item).getSubMenu().setGroupEnabled(R.id.group_track_actions, true);
            Log.d(getLogStart(), "enable group");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch (item.getItemId()) {
                case R.id.record_item:
                    if (!trackRecordingPresenter.isRecordingTrack()) {
                        showTrackNameDialog();
                    } else {
                        Track recordedTrack = trackRecordingPresenter.getRecordedTrack();
                        trackRecordingPresenter.stopTrackRecording();
                        invalidateOptionsMenu();
                        showSaveTrackDialog(recordedTrack);
                    }
                    return true;
                case R.id.display_item:
                    startDisplayTracksActivity();
                    return true;
                case R.id.save_item:
                    startSaveTracksActivity(null);
                    return true;
                case R.id.import_item:
                    return true;
                case R.id.export_item:
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        } catch (Exception e) {
            Log.d(this.getLogStart(), e.getLocalizedMessage());
        }
        return super.onOptionsItemSelected(item);
    }

    private void showTrackNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose a name for the new track");

        final EditText input = new EditText(this);

        //TODO integrate Creator in String
        String name = GPSComponent.getGPSComponent().getASAPApplication().getOwnerName() + "\'s track " + (TrackManager.getNumberOfTracks() + 1);
        input.setText(name);
        input.setSelectAllOnFocus(true);

        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String trackName = input.getText().toString();
                trackRecordingPresenter.startTrackRecording(trackName);
                invalidateOptionsMenu();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void showSaveTrackDialog(final Track track) {
        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        startSaveTracksActivity(track);
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Save recorded track to storage?")
                .setPositiveButton("Yes" , clickListener)
                .setNegativeButton("No" , clickListener)
                .show();
    }

    private void startDisplayTracksActivity() {
        Intent intent = new Intent(this, DisplayTracksActivity.class);
        Set<CharSequence> selectedItemIDs = trackRecordingPresenter.getTrackManager().getSelectedItemIDs();
        intent.putCharSequenceArrayListExtra("selectedItemIDs", new ArrayList<>(selectedItemIDs));
        startActivityForResult(intent, DISPLAY_ACTIVITY_REQUEST_CODE);
    }

    private void startSaveTracksActivity(Track trackToSave) {
        Intent intent = new Intent(this, SaveTracksActivity.class);
        if (trackToSave != null) {
            ArrayList<CharSequence> list = new ArrayList<>(Arrays.asList(trackToSave.getObjectId()));
            intent.putCharSequenceArrayListExtra("selectedItemIDs", list);
        }
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