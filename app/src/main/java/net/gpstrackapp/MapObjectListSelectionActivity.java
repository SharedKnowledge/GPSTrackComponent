package net.gpstrackapp;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.Set;

public abstract class MapObjectListSelectionActivity extends MapObjectListActivity {
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(getLogStart(), "init Toolbar");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gpstracker_list_selection_action_buttons, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch(item.getItemId()) {
                case R.id.gpstrackerListSelectionDoneButton:
                    this.doDone();
                    return true;
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

    private void doDone() {
        Log.d(getLogStart(), "doDone");
        onSelectionFinished(this.getSelectedItemIDs());
        this.finish();
    }

    protected abstract void onSelectionFinished(Set<String> selectedItemIDs);

    private String getLogStart() {
        return this.getClass().getSimpleName();
    }
}
