package net.gpstrackapp.activity.geomodel.track;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import net.gpstrackapp.GPSComponent;
import net.gpstrackapp.R;
import net.gpstrackapp.activity.geomodel.GeoModelListSelectionActivity;
import net.gpstrackapp.format.ExportFileFormat;
import net.gpstrackapp.format.FileUtils;
import net.gpstrackapp.format.FormatManager;
import net.gpstrackapp.geomodel.RequestGeoModelsCommand;
import net.gpstrackapp.geomodel.track.RequestTracksCommand;
import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.geomodel.track.TrackModelManager;

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
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, availableFormatsUpperCase);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        View spinnerView = findViewById(R.id.gpstracker_list_geomodels_spinner);
        spinnerView.setVisibility(View.VISIBLE);

        spinner = findViewById(R.id.spinner);
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
        final EditText input = new EditText(this);
        input.setSelectAllOnFocus(true);

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setView(input)
                .setTitle("Choose a file name")
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                .create();
        alertDialog.show();

        Button buttonPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        buttonPositive.setOnClickListener(v -> {
            String fileName = input.getText().toString();
            if (FileUtils.isValidFileName(fileName)) {
                createFile(selectedItemIDs, fileName);
                alertDialog.dismiss();
            } else {
                input.setError("Don't use any of these characters: " + System.lineSeparator() + new String(FileUtils.getInvalidChars()));
            }
        });
    }

    private void createFile(Set<CharSequence> selectedItemIDs, String fileName) {
        if (selectedFormat != null) {
            Set<Track> selectedTracks = trackModelManager.getGeoModelsByUUIDs(selectedItemIDs);

            ExportFileFormat format = FormatManager.getExportFormatByFileExtension(selectedFormat);
            fileName += "." + format.getFileExtensionString();

            exportHelper = new ExportHelper(format, selectedTracks, fileName);

            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
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
                DocumentFile dir = DocumentFile.fromTreeUri(this, result);
                exportHelper.exportToFile(dir);
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
    public String addUserDescription() {
        return "Also choose a format in which you want to export the tracks. All selected tracks will be exported together in one file.";
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

        public void exportToFile(DocumentFile dir) {
            try {
                DocumentFile newFile = dir.createFile(format.getMIMEDataType(), fileName);
                OutputStream outputStream = getContentResolver().openOutputStream(newFile.getUri());
                format.exportToFile(ExportTracksActivity.this, tracksToExport, fileName, outputStream);
            } catch (Exception e) {
                Log.e(getLogStart(), e.getLocalizedMessage());
            }
        }
    }
}
