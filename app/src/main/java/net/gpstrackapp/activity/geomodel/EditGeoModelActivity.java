package net.gpstrackapp.activity.geomodel;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.gpstrackapp.R;
import net.gpstrackapp.geomodel.GeoModel;
import net.gpstrackapp.geomodel.GeoModelManager;
import net.sharksystem.asap.android.Util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EditGeoModelActivity extends AppCompatActivity {
    private GeoModel geoModel;
    private TextView idText;
    private EditText nameText, dateText, creatorText;
    private String formatterPattern = GeoModel.getFormatterPattern();
    private DateTimeFormatter formatter = GeoModel.getFormatter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gpstracker_geomodel_edit_drawer_layout);

        // setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.gpstracker_geomodel_edit_toolbar);
        setSupportActionBar(toolbar);

        CharSequence geoModelID = getIntent().getCharSequenceExtra("geoModelID");
        if (geoModelID == null) {
            Toast.makeText(this, "A problem occured while trying to load the geomodel in question", Toast.LENGTH_LONG).show();
            Log.e(Util.getLogStart(this), "geoModelID is null, finish activity");
            finish();
        }
        geoModel = GeoModelManager.getGeoModelByUUIDFromGlobal(geoModelID);

        idText = findViewById(R.id.gpstracker_geomodel_edit_id_value);
        idText.setText(geoModelID);

        nameText = findViewById(R.id.gpstracker_geomodel_edit_name_value);
        String name = geoModel.getObjectName().toString();
        nameText.setText(name != null ? name : "");

        dateText = findViewById(R.id.gpstracker_geomodel_edit_date_value);
        dateText.setHint(formatterPattern);
        String date = geoModel.getDateOfCreationAsFormattedString();
        dateText.setText(date != null ? date : "");

        creatorText = findViewById(R.id.gpstracker_geomodel_edit_creator_value);
        String creator = geoModel.getCreator().toString();
        creatorText.setText(creator != null ? creator : "");
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

    public void onSaveButtonClicked(View view) {
        String name = nameText.getText().toString();
        String creator = creatorText.getText().toString();
        String date = dateText.getText().toString();

        LocalDateTime dateOfCreation = null;
        if (!date.isEmpty()) {
            try {
                dateOfCreation = LocalDateTime.parse(date, formatter);
            } catch (Exception e) {
                dateText.setError("Enter the date in the format: " + formatterPattern);
                return;
            }
        }

        geoModel.setObjectName(name);
        geoModel.setCreator(creator);
        geoModel.setDateOfCreation(dateOfCreation);

        String changed = "Changes were applied to geomodel";
        Toast.makeText(this, changed, Toast.LENGTH_SHORT).show();
        Log.d(Util.getLogStart(this), changed);
        finish();
    }
}
