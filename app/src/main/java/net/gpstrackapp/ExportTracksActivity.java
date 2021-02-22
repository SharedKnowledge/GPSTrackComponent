package net.gpstrackapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import net.gpstrackapp.geomodel.RequestGeoModelsCommand;
import net.gpstrackapp.geomodel.track.RequestTracksCommand;
import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.geomodel.track.TrackModelManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

//after selection navigate to new Activity, in which user selects format
public class ExportTracksActivity extends GeoModelListSelectionActivity implements AdapterView.OnItemSelectedListener {
    private final int CREATE_FILE_CODE = 1;
    private Spinner spinner;
    private TrackModelManager trackModelManager = GPSComponent.getGPSComponent().getTrackModelManager();
    private String selectedFormat;
    private String[] availableFormats;
    private ExportTracksActivity.ExportHelper exportHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Set<String> formats = getFormatStrings();
        availableFormats = formats.toArray(new String[formats.size()]);
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, availableFormats);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        View spinnerView = findViewById(R.id.gpstracker_spinner);
        spinnerView.setVisibility(View.VISIBLE);

        spinner = findViewById(R.id.format_spinner);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(this);
        spinner.setPrompt("Select a file format");

        selectedFormat = availableFormats.length > 0 ? availableFormats[0] : null;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedFormat = availableFormats[position];
    }

    @Override
    protected void onSelectionFinished(Set<CharSequence> selectedItemIDs) {
        showTrackNameDialog(selectedItemIDs);
    }

    private void showTrackNameDialog(Set<CharSequence> selectedItemIDs) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose a file name");

        final EditText input = new EditText(this);
        input.setSelectAllOnFocus(true);

        builder.setView(input);
        builder.setPositiveButton("OK", (dialog, which) -> {
            String trackName = input.getText().toString();
            createFile(selectedItemIDs, trackName);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void createFile(Set<CharSequence> selectedItemIDs, String trackName) {
        if (selectedFormat != null) {
            Set<Track> selectedTracks = trackModelManager.getGeoModelsByUUIDs(selectedItemIDs);
            ExportFileFormat format = FormatManager.getExportFormatByFileExtension(selectedFormat);
            trackName += "." + format.getFileExtensionString();

            exportHelper = new ExportHelper(format, selectedTracks, trackName);

            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType(format.getMIMEDataType());
            intent.putExtra(Intent.EXTRA_TITLE, trackName);
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.getExternalStorageDirectory().toURI());

            startActivityForResult(intent, CREATE_FILE_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_FILE_CODE) {
            if (resultCode == RESULT_OK) {
                Uri result = data.getData();
                try {
                    OutputStream outputStream = getContentResolver().openOutputStream(result);
                    exportHelper.exportToFile(outputStream);
                } catch (FileNotFoundException e) {
                    Log.d(getLogStart(), e.getLocalizedMessage());
                }
                finish();
            }
        }
    }

    public String getSelectedFormat() {
        return selectedFormat;
    }

    @Override
    protected RequestGeoModelsCommand createRequestGeoModelsCommand() {
        return new RequestTracksCommand();
    }

    protected Set<String> getFormatStrings() {
        Set<String> exportFormats = FormatManager.getExportFormats().keySet();
        exportFormats = exportFormats.stream()
                .map(format -> format.toUpperCase())
                .collect(Collectors.toSet());
        return exportFormats;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private String getLogStart() {
        return getClass().getSimpleName();
    }

    protected class ExportHelper {
        private ExportFileFormat format;
        private Set<Track> tracksToExport;
        private String trackName;

        public ExportHelper(ExportFileFormat format, Set<Track> tracksToExport, String trackName) {
            this.format = format;
            this.tracksToExport = tracksToExport;
            this.trackName = trackName;
        }

        public void exportToFile(OutputStream outputStream) {
            try {
                format.exportToFile(ExportTracksActivity.this, tracksToExport, trackName, outputStream);
            } catch (ParserConfigurationException | IOException e) {
                Log.d(getLogStart(), e.getLocalizedMessage());
            }
        }
    }
}
