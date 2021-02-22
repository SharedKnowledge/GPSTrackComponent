package net.gpstrackapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import net.gpstrackapp.geomodel.track.Track;

import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

public class ImportTracksActivity extends AppCompatActivity {
    private final int CHOOSE_FILE_CODE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gpstracker_drawer_layout);

        // inflate layout in DrawerLayout
        DrawerLayout drawerLayout = findViewById(R.id.gpstracker_list_drawer_layout);
        View child = getLayoutInflater().inflate(R.layout.gpstracker_button_with_toolbar, null);
        drawerLayout.addView(child);

        // setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.gpstracker_reduced_toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(getLogStart(), "init Toolbar");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gpstracker_reduced_action_buttons, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch (item.getItemId()) {
                case R.id.abortButton:
                    this.finish();
                    return true;
                default:
                    // If we got here, the user's action was not recognized.
                    // Invoke the superclass to handle it.
                    return super.onOptionsItemSelected(item);
            }
        } catch (Exception e) {
            Log.d(getLogStart(), "problem on options item selected: " + e.getLocalizedMessage());
        }
        return false;
    }

    public void onImportButtonClicked(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Set<String> importFormats = FormatManager.getImportFormats().keySet();
        Set<String> mimeTypes = importFormats.stream()
                .map(format -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(format))
                .collect(Collectors.toSet());

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
                            importFileFormat.importFromFile(inputStream);
                        } catch (IOException e) {
                            Log.d(getLogStart(), e.getLocalizedMessage());
                        }
                    } else {
                        Toast.makeText(this, "The file could not be imported because the file type " + mimeType + " is not supported", Toast.LENGTH_SHORT).show();
                    }
                } catch (FileNotFoundException e) {
                    Log.d(getLogStart(), e.getLocalizedMessage());
                }
            }
        }
    }

    private String getLogStart() {
        return getClass().getSimpleName();
    }
}
