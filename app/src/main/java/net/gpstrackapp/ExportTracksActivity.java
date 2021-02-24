package net.gpstrackapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import net.gpstrackapp.geomodel.RequestGeoModelsCommand;
import net.gpstrackapp.geomodel.track.RequestTracksCommand;
import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.geomodel.track.TrackModelManager;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        List<String> availableFormatsUpperCase = Arrays.stream(availableFormats)
                .map(format -> format.toUpperCase())
                .collect(Collectors.toList());
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, availableFormatsUpperCase);
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
        showFileNameDialog(selectedItemIDs);
    }

    private void showFileNameDialog(Set<CharSequence> selectedItemIDs) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose a file name");

        final EditText input = new EditText(this);
        input.setSelectAllOnFocus(true);

        builder.setView(input);
        builder.setPositiveButton("OK", (dialog, which) -> {
            //TODO check input for illegal chars
            String fileName = input.getText().toString();
            createFile(selectedItemIDs, fileName);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void createFile(Set<CharSequence> selectedItemIDs, String fileName) {
        if (selectedFormat != null) {
            Set<Track> selectedTracks = trackModelManager.getGeoModelsByUUIDs(selectedItemIDs);

            Log.d(getLogStart(), selectedFormat);
            Log.d(getLogStart(), FormatManager.getExportFormats().toString());
            ExportFileFormat format = FormatManager.getExportFormatByFileExtension(selectedFormat);
            fileName += "." + format.getFileExtensionString();

            exportHelper = new ExportHelper(format, selectedTracks, fileName);

            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType(format.getMIMEDataType());
            intent.putExtra(Intent.EXTRA_TITLE, fileName);
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
                Toast.makeText(this, "Export was successful.", Toast.LENGTH_SHORT).show();
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

    @Override
    public String setActionText() {
        return "Export tracks";
    }

    @Override
    public String setOptionalAdditionalInfo() {
        return "Also choose a format in which you want to export the tracks.";
    }

    protected Set<String> getFormatStrings() {
        Set<String> exportFormats = FormatManager.getExportFormats().keySet();
        return exportFormats;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) { }

    private String getLogStart() {
        return getClass().getSimpleName();
    }

    protected class ExportHelper {
        private ExportFileFormat format;
        private Set<Track> tracksToExport;
        private String fileName;

        public ExportHelper(ExportFileFormat format, Set<Track> tracksToExport, String fileName) {
            this.format = format;
            this.tracksToExport = tracksToExport;
            this.fileName = fileName;
        }

        public void exportToFile(OutputStream outputStream) {
            try {
                format.exportToFile(ExportTracksActivity.this, tracksToExport, fileName, outputStream);
            } catch (Exception e) {
                Log.d(getLogStart(), e.getLocalizedMessage());
            }
        }
    }
}
