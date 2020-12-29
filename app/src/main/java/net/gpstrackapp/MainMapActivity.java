package net.gpstrackapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import net.sharksystem.asap.ASAPException;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

//TODO permission handling, maybe in superclass or split up
public class MainMapActivity extends MapViewActivity {
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainMapActivity", String.valueOf(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)));
        Log.d("MainMapActivity", String.valueOf(Environment.isExternalStorageRemovable()));
        Log.d("MainMapActivity", String.valueOf(Configuration.getInstance().getOsmdroidBasePath()));

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

        super.onCreate(savedInstanceState);
        /*
        recyclerView = (RecyclerView) findViewById(R.id.gpstracker_map_recycler_view);
        RecyclerView.LayoutManager mLayoutManager =
                new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
         */
    }

    ////////////////////////////////////////////////////////////////////////
    //                           mapView methods                          //
    ////////////////////////////////////////////////////////////////////////

    @Override
    protected MapView setUpMapViewAndGet() {
        Context ctx = getApplicationContext();
        MapView mapView = ReusableMapView.getInstance(ctx, TileSourceFactory.MAPNIK);
        return mapView;
    }

    @Override
    protected ViewGroup setUpLayoutAndGet() {
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
    protected void setPerspectiveParameters() {
        setZoom(9.5);
        setCenterCoordinates(52.5200, 13.4050);
    }

    @Override
    protected void addOverlays() {
        //add more overlays
        mapView.getOverlays().add(new CopyrightOverlay(ctx));

        MyLocationNewOverlay myLocationNewOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(ctx), mapView);
        myLocationNewOverlay.enableMyLocation();
        mapView.getOverlays().add(myLocationNewOverlay);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("MainMapActivity", "init Toolbar");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gpstracker_action_buttons, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            //TODO add actions with do...()-Methods
            switch (item.getItemId()) {
                case R.id.record_item:
                    return true;
                case R.id.display_item:
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
        return "GPSApp MainMapActivity";
    }
}