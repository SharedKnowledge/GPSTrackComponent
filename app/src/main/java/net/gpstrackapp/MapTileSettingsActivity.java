package net.gpstrackapp;

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

import org.osmdroid.tileprovider.tilesource.ITileSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapTileSettingsActivity extends AppCompatActivity implements ActivityWithDescription {
    private Spinner tileSourceSpinner;
    private Map<String, ITileSource> tileSourceMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gpstracker_tile_settings_drawer_layout);

        // setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.gpstracker_tile_settings_toolbar);
        setSupportActionBar(toolbar);

        TextView descriptionView = (TextView) findViewById(R.id.gpstracker_description);
        String description = setActionText();
        String additionalInfo = addOptionalAdditionalInfo();
        if (additionalInfo != null && !additionalInfo.equals("")) {
            description += ":" + System.lineSeparator() + additionalInfo;
        }
        descriptionView.setText(description);

        List<ITileSource> tileSources = new ArrayList<>(ConfiguredMapView.getValidTileSources());
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item);
        for (ITileSource tileSource : tileSources) {
            tileSourceMap.put(tileSource.name(), tileSource);
            arrayAdapter.add(tileSource.name());
        }
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tileSourceSpinner = findViewById(R.id.spinner);
        tileSourceSpinner.setAdapter(arrayAdapter);
        tileSourceSpinner.setPrompt("Select a file format");
        tileSourceSpinner.setSelection(arrayAdapter.getPosition(ConfiguredMapView.getDefaultTileSource().name()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(getLogStart(), "init action buttons");
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
            Log.e(getLogStart(), "problem on options item selected: " + e.getLocalizedMessage());
        }
        return false;
    }

    public void onSaveAsDefaultButtonClicked(View view) {
        String tileSourceName = tileSourceSpinner.getSelectedItem().toString();
        ITileSource tileSource = tileSourceMap.get(tileSourceName);
        ConfiguredMapView.setDefaultTileSource(tileSource);
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

    private String getLogStart() {
        return getClass().getSimpleName();
    }
}
