package hk.com.dataworld.iattendance;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import static hk.com.dataworld.iattendance.Constants.PREF_LOCALE;
import static hk.com.dataworld.iattendance.Constants.PREF_SERVER_ADDRESS;


public class SettingsActivity extends AppCompatActivity {

    private static final boolean ALWAYS_SIMPLE_PREFS = false;
    private static final GeneralPreferenceFragment pref_general = new GeneralPreferenceFragment();

    private static final String TAG = SettingsActivity.class.getSimpleName();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(R.id.settings_layout, new GeneralPreferenceFragment())
                .commit();
        //setupSimplePreferencesScreen();
    }

//    @Override
//    protected void onPostCreate(Bundle savedInstanceState) {
//        super.onPostCreate(savedInstanceState);
//        setupSimplePreferencesScreen();
//    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        Log.i("recreate", "recreate");
        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                pref_general).commit();
    }

    /**
     * {@inheritDoc}
     */
//    @Override
//    public boolean onIsMultiPane() {
//        return isXLargeTablet(this) && !isSimplePreferences(this);
//    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            boolean fInvalidValue = false;

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

                switch (preference.getKey()) {
                    case "pref_zone": {
                        Log.i(TAG, "pref_zone");
                    }
                    break;
                }
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
                switch (preference.getKey()) {
                    case "pref_server_addr": {
                        // Try connect to see if setting is valid
                        //TODO: Try connect to see if setting is valid
                    }
                    break;
                }
            }
            return !fInvalidValue;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            LocaleListPreference language = (LocaleListPreference) findPreference(PREF_LOCALE);

            //Language
            language.setEntries(new CharSequence[]{getString(R.string.pref_language_english), getString(R.string.pref_language_chinese)});
            language.setEntryValues(new CharSequence[]{"en", "zh"});

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(PREF_SERVER_ADDRESS));
//            bindPreferenceSummaryToValue(findPreference("pref_device_name"));
            bindPreferenceSummaryToValue(language);
        }
    }
}
