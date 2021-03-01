package net.gpstrackapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import net.gpstrackapp.geomodel.GeoModel;
import net.gpstrackapp.geomodel.GeoModelManager;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

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
            Log.e(getLogStart(), "geoModelID is null, finish activity");
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

    public void onSaveButtonClicked(View view) {
        String name = nameText.getText().toString();
        String date = dateText.getText().toString();
        String creator = creatorText.getText().toString();

        if (name.isEmpty()) {
            name = null;
        }
        geoModel.setObjectName(name);

        LocalDateTime dateOfCreation = null;
        if (!date.isEmpty()) {
            try {
                dateOfCreation = (LocalDateTime) formatter.parse(date);
            } catch (Exception e) {
                dateText.setError("Enter the date in the format: " + formatterPattern);
                return;
            }
        }
        geoModel.setDateOfCreation(dateOfCreation);

        if (creator.isEmpty()) {
            creator = null;
        }
        geoModel.setCreator(creator);

        String changed = "Changed were applied to geomodel";
        Toast.makeText(this, changed, Toast.LENGTH_SHORT).show();
        Log.d(getLogStart(), changed);
        finish();
    }

    private String getLogStart() {
        return getClass().getSimpleName();
    }
}
