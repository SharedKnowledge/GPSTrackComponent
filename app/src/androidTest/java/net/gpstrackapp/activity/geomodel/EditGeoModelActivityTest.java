package net.gpstrackapp.activity.geomodel;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.gpstrackapp.R;
import net.gpstrackapp.geomodel.GeoModel;
import net.gpstrackapp.geomodel.track.Track;
import net.gpstrackapp.geomodel.track.TrackModelManager;
import net.gpstrackapp.geomodel.track.TrackSegment;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class EditGeoModelActivityTest {
    private static String nameString;
    private static String creatorString;
    private static String dateString;
    private static String defaultNameString = "trackName";
    private static String defaultCreatorString = "creatorName";
    private static LocalDateTime defaultDate = LocalDateTime.of(2021, 1, 1, 0, 0);
    private static DateTimeFormatter formatterPattern = GeoModel.getFormatter();
    private static CharSequence geoModelID = UUID.randomUUID().toString();
    private static Intent startIntent;
    private static Track track;
    private static TrackModelManager trackModelManager = new TrackModelManager();

    static {
        track = new Track(geoModelID, defaultNameString, defaultCreatorString, defaultDate, new TrackSegment(null));
        trackModelManager.addGeoModel(track);

        startIntent = new Intent(ApplicationProvider.getApplicationContext(), EditGeoModelActivity.class);
        // use the track in the activity
        startIntent.putExtra("geoModelID", geoModelID);
    }

    @Rule
    public ActivityScenarioRule<EditGeoModelActivity> activityRule
            = new ActivityScenarioRule<>(startIntent);

    @Before
    public void setup() {
        track.setObjectName(defaultNameString);
        track.setCreator(defaultCreatorString);
        track.setDateOfCreation(defaultDate);
    }

    @Test
    public void validInputShouldSave() {
        nameString = "validName";
        creatorString = "validCreator";
        dateString = LocalDateTime.now().format(formatterPattern);
        typeUserInputAndSave(nameString, creatorString, dateString);
        assertEquals(track.getObjectName(), nameString);
        assertEquals(track.getCreator(), creatorString);
        assertEquals(track.getDateOfCreationAsFormattedString(), dateString);
    }

    @Test
    public void emptyValuesShouldSave() {
        nameString = "";
        creatorString = "";
        dateString = "";
        typeUserInputAndSave(nameString, creatorString, dateString);
        assertEquals(track.getObjectName(), nameString);
        assertEquals(track.getCreator(), creatorString);
        // date cannot be empty and should be null instead
        assertNull(track.getDateOfCreation());
    }

    @Test
    public void wrongDateFormatShouldNotSave() {
        nameString = "validName";
        creatorString = "validCreator";
        // use a different pattern for date format
        dateString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        typeUserInputAndSave(nameString, creatorString, dateString);
        assertEquals(track.getObjectName(), defaultNameString);
        assertEquals(track.getCreator(), defaultCreatorString);
        assertEquals(track.getDateOfCreation(), defaultDate);
    }

    @Test
    public void invalidDateStringShouldNotSave() {
        nameString = "validName";
        creatorString = "validCreator";
        // invalid date
        dateString = "invalidDateString";
        typeUserInputAndSave(nameString, creatorString, dateString);
        assertEquals(track.getObjectName(), defaultNameString);
        assertEquals(track.getCreator(), defaultCreatorString);
        assertEquals(track.getDateOfCreation(), defaultDate);
    }

    private void typeUserInputAndSave(String nameString, String creatorString, String dateString) {
        onView(withId(R.id.gpstracker_geomodel_edit_name_value)).perform(replaceText(nameString));
        onView(withId(R.id.gpstracker_geomodel_edit_creator_value)).perform(replaceText(creatorString));
        onView(withId(R.id.gpstracker_geomodel_edit_date_value)).perform(replaceText(dateString));
        onView(withId(R.id.gpstracker_geomodel_edit_save_button)).perform(click());
    }
}