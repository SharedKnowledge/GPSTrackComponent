package net.gpstrackapp.activity.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import net.gpstrackapp.R;
import net.gpstrackapp.activity.ActivityWithDescription;
import net.sharksystem.asap.android.Util;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.gpstrackapp.mapview.ConfiguredMapFragment.PREFS_NAME;
import static net.gpstrackapp.mapview.ConfiguredMapFragment.PREFS_TILE_SOURCE;

public class MapTileSettingsActivity extends AppCompatActivity implements ActivityWithDescription {
    private Spinner tileSourceSpinner;
    private Map<String, ITileSource> tileSourceMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gpstracker_tile_settings_drawer_layout);

        // setup toolbar
        Toolbar toolbar = findViewById(R.id.gpstracker_tile_settings_toolbar);
        setSupportActionBar(toolbar);

        TextView descriptionView = findViewById(R.id.gpstracker_description);
        String description = setActionText();
        String additionalInfo = addOptionalAdditionalInfo();
        if (additionalInfo != null && !additionalInfo.equals("")) {
            description += ":" + System.lineSeparator() + additionalInfo;
        }
        descriptionView.setText(description);

        List<ITileSource> tileSources = new ArrayList<>(TileSourceFactory.getTileSources());
        //ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        for (ITileSource tileSource : tileSources) {
            tileSourceMap.put(tileSource.name(), tileSource);
            arrayAdapter.add(tileSource.name());
        }

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tileSourceSpinner = findViewById(R.id.spinner);
        tileSourceSpinner.setAdapter(arrayAdapter);
        tileSourceSpinner.setPrompt("Select a tile source");
        tileSourceSpinner.setSelection(arrayAdapter.getPosition(sharedPreferences.getString(PREFS_TILE_SOURCE, TileSourceFactory.DEFAULT_TILE_SOURCE.name())));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(Util.getLogStart(this), "init action buttons");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gpstracker_abort_action_button, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch (item.getItemId()) {
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

    public void onSaveAsDefaultButtonClicked(View view) {
        String tileSourceName = tileSourceSpinner.getSelectedItem().toString();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(PREFS_TILE_SOURCE, tileSourceName)
                .apply();
        finish();
    }

    @Override
    public String setActionText() {
        return "Map Tile Settings";
    }

    @Override
    public String addOptionalAdditionalInfo() {
        return "Choose the tile source you want to set as default. All maps that don't specify a custom tile source will use this source.";
    }
}
