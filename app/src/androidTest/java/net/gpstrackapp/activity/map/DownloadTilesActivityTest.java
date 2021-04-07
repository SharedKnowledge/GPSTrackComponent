package net.gpstrackapp.activity.map;

import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.gpstrackapp.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class DownloadTilesActivityTest {
    @Rule
    public ActivityScenarioRule<DownloadTilesActivity> activityRule
            = new ActivityScenarioRule<>(DownloadTilesActivity.class);

    @Before
    public void setup() {
        onView(withId(R.id.confirm_item)).perform(click());
        onView(withText("Cache Manager")).check(matches(isDisplayed()));
        onView(withText("Start Download")).perform(click());
    }

    @Test
    public void validInputsShouldBeDownloadable() {
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_north)).perform(replaceText(String.valueOf(0.0)));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_south)).perform(replaceText(String.valueOf(1.0)));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_west)).perform(replaceText(String.valueOf(0.0)));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_east)).perform(replaceText(String.valueOf(1.0)));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_slider_zoom_min)).perform(setProgress(0));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_slider_zoom_max)).perform(setProgress(0));
        // there is only 1 tile on zoom level 0 so it should be downloadable
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_estimate)).check(matches(not(hasAnyErrorText())));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_execute_job)).check(matches(isEnabled()));
    }

    @Test
    public void tooManyTilesShouldNotBeDownloadable() {
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_north)).perform(replaceText(String.valueOf(0.0)));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_south)).perform(replaceText(String.valueOf(80.0)));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_west)).perform(replaceText(String.valueOf(0.0)));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_east)).perform(replaceText(String.valueOf(80.0)));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_slider_zoom_min)).perform(setProgress(0));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_slider_zoom_max)).perform(setProgress(10));
        // error is only present if the tile count is too large
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_estimate)).check(matches(hasAnyErrorText()));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_execute_job)).check(matches(not(isEnabled())));
    }

    @Test
    public void inputValueOfWrongTypeShouldNotBeDownloadableButCauseNoError() {
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_north)).perform(replaceText("this is not a double value"));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_south)).perform(replaceText("this is not a double value"));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_west)).perform(replaceText("this is not a double value"));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_east)).perform(replaceText("this is not a double value"));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_slider_zoom_min)).perform(setProgress(0));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_slider_zoom_max)).perform(setProgress(0));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_execute_job)).check(matches(isEnabled()));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_execute_job)).perform(click());
        // window is still displayed because the download didn't start
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_execute_job)).check(matches(isDisplayed()));
    }

    @Test
    public void emptyInputValueShouldNotBeDownloadableButCauseNoError() {
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_north)).perform(replaceText(""));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_south)).perform(replaceText(""));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_west)).perform(replaceText(""));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_east)).perform(replaceText(""));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_slider_zoom_min)).perform(setProgress(0));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_slider_zoom_max)).perform(setProgress(0));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_execute_job)).check(matches(isEnabled()));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_execute_job)).perform(click());
        // window is still displayed because the download didn't start
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_execute_job)).check(matches(isDisplayed()));
    }

    @Test
    public void invalidLatitudeShouldNotBeDownloadable() {
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_north)).perform(replaceText(String.valueOf(0.0)));
        // value is too large
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_south)).perform(replaceText(String.valueOf(100.0)));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_west)).perform(replaceText(String.valueOf(0.0)));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_east)).perform(replaceText(String.valueOf(1.0)));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_slider_zoom_min)).perform(setProgress(0));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_slider_zoom_max)).perform(setProgress(0));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_execute_job)).check(matches(not(isEnabled())));
    }

    @Test
    public void invalidLongitudeShouldNotBeDownloadable() {
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_north)).perform(replaceText(String.valueOf(0.0)));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_south)).perform(replaceText(String.valueOf(1.0)));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_west)).perform(replaceText(String.valueOf(0.0)));
        // value is too large
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_east)).perform(replaceText(String.valueOf(200.0)));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_slider_zoom_min)).perform(setProgress(0));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_slider_zoom_max)).perform(setProgress(0));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_execute_job)).check(matches(not(isEnabled())));
    }

    @Test
    public void invalidArchiveFileNameShouldNotBeDownloadable() {
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_north)).perform(replaceText(String.valueOf(0.0)));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_south)).perform(replaceText(String.valueOf(1.0)));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_west)).perform(replaceText(String.valueOf(0.0)));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_east)).perform(replaceText(String.valueOf(1.0)));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_slider_zoom_min)).perform(setProgress(0));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_slider_zoom_max)).perform(setProgress(0));
        // invalid file name
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_output)).perform(replaceText("/"));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_execute_job)).perform(click());
        // window is still displayed because the download didn't start
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_execute_job)).check(matches(isDisplayed()));
    }

    @Test
    public void emptyArchiveFileNameShouldNotBeDownloadable() {
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_north)).perform(replaceText(String.valueOf(0.0)));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_south)).perform(replaceText(String.valueOf(1.0)));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_west)).perform(replaceText(String.valueOf(0.0)));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_east)).perform(replaceText(String.valueOf(1.0)));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_slider_zoom_min)).perform(setProgress(0));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_slider_zoom_max)).perform(setProgress(0));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_cache_output)).perform(replaceText(""));
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_execute_job)).perform(click());
        // window is still displayed because the download didn't start
        onView(withId(R.id.gpstracker_tile_download_alert_dialog_execute_job)).check(matches(isDisplayed()));
    }

    public static ViewAction setProgress(final int progress) {
        return new ViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                SeekBar seekBar = (SeekBar) view;
                seekBar.setProgress(progress);
            }
            @Override
            public String getDescription() {
                return "Set a progress on a SeekBar";
            }
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(SeekBar.class);
            }
        };
    }

    public static Matcher<View> hasAnyErrorText() {
        return new BoundedMatcher<View, TextView>(TextView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has error text: ");
            }

            @Override
            protected boolean matchesSafely(TextView view) {
                return view.getError() != null;
            }
        };
    }
}