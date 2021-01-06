package net.gpstrackapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.geomodel.track.TrackManager;
import net.gpstrackapp.overlay.TrackDisplayer;
import net.sharksystem.asap.ASAPException;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

//TODO permission handling, maybe in superclass or split up
public class MainMapActivity extends MapViewActivity {
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private Context ctx;
    private IMyLocationProvider locationProvider;
    private Track recordedTrack;
    private ReusableTrackMapView reusableTrackMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(getLogStart(), "onCreate");
        /*
        Log.d("MainMapActivity", String.valueOf(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)));
        Log.d("MainMapActivity", String.valueOf(Environment.isExternalStorageRemovable()));
        Log.d("MainMapActivity", String.valueOf(Configuration.getInstance().getOsmdroidBasePath()));
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

        if (locationProvider == null) {
            locationProvider = new GpsMyLocationProvider(this);
        }

        super.onCreate(savedInstanceState);
    }

    ////////////////////////////////////////////////////////////////////////
    //                           mapView methods                          //
    ////////////////////////////////////////////////////////////////////////

    @Override
    protected MapView setupMapViewAndGet() {
        reusableTrackMapView = GPSComponent.getGPSComponent().getAttributeContainer().getReusableTrackMapView();
        reusableTrackMapView.setTileSource(TileSourceFactory.MAPNIK);
        return reusableTrackMapView;
    }

    @Override
    protected ViewGroup setupLayoutAndGet() {
        setContentView(R.layout.gpstracker_map_drawer_layout);
        Toolbar toolbar = findViewById(R.id.gpstracker_with_all_actions_toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = findViewById(R.id.gpstracker_map_drawer_layout);
        DrawerLayout.LayoutParams params = new DrawerLayout.LayoutParams(MapView.LayoutParams.MATCH_PARENT,
                MapView.LayoutParams.MATCH_PARENT);
        params.setMargins(0, (int) getResources().getDimension(R.dimen.marginUnderToolbar), 0, 0);
        mapView.setLayoutParams(params);

        return drawerLayout;
    }

    @Override
    protected double setupZoomLevel() {
        return 9.5;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("MainMapActivity", "init Toolbar");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gpstracker_mapview_action_buttons, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch (item.getItemId()) {
                case R.id.record_item:
                    if (!isRecordingTrack()) {
                        showTrackNameDialog();
                    } else {
                        stopTrackRecording();
                    }
                    return true;
                case R.id.display_item:
                    startDisplayTracksActivity();
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
        Date now = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowFormatted = df.format(now);
        input.setText(nowFormatted);
        input.setSelectAllOnFocus(true);

        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String trackName = input.getText().toString();
                startTrackRecording(trackName);
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

    private void startTrackRecording(String trackName) {
        Log.d(getLogStart(), "Start Track Recording");
        Track track = TrackManager.createTrack(trackName, Calendar.getInstance().getTime(), null);
        locationProvider.startLocationProvider(track);
        recordedTrack = track;
    }

    private void stopTrackRecording() {
        Log.d(getLogStart(), "Stop Track Recording");
        locationProvider.stopLocationProvider();
        recordedTrack = null;
    }

    public boolean isRecordingTrack() {
        if (recordedTrack != null)
            return true;
        return false;
    }

    private void startDisplayTracksActivity() {
        Intent intent = new Intent(this, DisplayTracksActivity.class);
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