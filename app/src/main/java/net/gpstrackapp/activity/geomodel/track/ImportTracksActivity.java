package net.gpstrackapp.activity.geomodel.track;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import net.gpstrackapp.activity.ActivityWithDescription;
import net.gpstrackapp.format.FormatManager;
import net.gpstrackapp.R;
import net.gpstrackapp.format.ImportFileFormat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;

public class ImportTracksActivity extends AppCompatActivity implements ActivityWithDescription {
    private final int CHOOSE_FILE_CODE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gpstracker_import_action_drawer_layout);

        // setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.gpstracker_import_toolbar);
        setSupportActionBar(toolbar);

        TextView descriptionView = (TextView) findViewById(R.id.gpstracker_description);
        String description = setActionText();
        String additionalInfo = addOptionalAdditionalInfo();
        if (additionalInfo != null && !additionalInfo.equals("")) {
            description += ":" + System.lineSeparator() + additionalInfo;
        }
        descriptionView.setText(description);
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

    public void onImportButtonClicked(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        Set<String> importFormats = FormatManager.getImportFormats().keySet();
        Set<String> mimeTypes = importFormats.stream()
                .map(format -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(format))
                .collect(Collectors.toSet());

        /*
        TODO When a file is deleted manually the file will sometimes still be shown in the following Activity, most likely because
         it doesn't get refreshed by the storage access framework for some reason. In the Device File Explorer the files
         don't exist anymore. After a restart of the device the files won't be shown anymore, but that's not really a solution.
        */
        String[] mimeTypesArray = mimeTypes.toArray(new String[mimeTypes.size()]);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypesArray);
        intent = Intent.createChooser(intent, "Choose a file to import");
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.getExternalStorageDirectory().toURI());
        startActivityForResult(intent, CHOOSE_FILE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_FILE_CODE) {
            if (resultCode == RESULT_OK) {
                Uri result = data.getData();
                String mimeType = getContentResolver().getType(result);
                ImportFileFormat importFileFormat = FormatManager.getImportFormatByMimeType(mimeType);
                try {
                    if (importFileFormat != null) {
                        InputStream inputStream = getContentResolver().openInputStream(result);
                        try {
                            importFileFormat.importFromFile(this, inputStream);
                        } catch (IOException e) {
                            Log.e(getLogStart(), e.getLocalizedMessage());
                        }
                        Toast.makeText(this, "Import was successful.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "The file could not be imported because the file type " + mimeType + " is not supported", Toast.LENGTH_SHORT).show();
                    }
                } catch (FileNotFoundException e) {
                    Log.e(getLogStart(), e.getLocalizedMessage());
                }
            }
        }
    }

    private String getLogStart() {
        return getClass().getSimpleName();
    }

    @Override
    public String setActionText() {
        return "Import tracks";
    }

    @Override
    public String addOptionalAdditionalInfo() {
        return null;
    }
}
