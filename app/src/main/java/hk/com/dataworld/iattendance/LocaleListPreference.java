package hk.com.dataworld.iattendance;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import java.util.Locale;

import static hk.com.dataworld.iattendance.Constants.PREF_LOCALE;

public class LocaleListPreference extends ListPreference {

    private Context mContext;

    public LocaleListPreference(Context context) {
        super(context);
        mContext = context;
    }

    public LocaleListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        setLocale(PreferenceManager.getDefaultSharedPreferences(mContext).getString(PREF_LOCALE, "en"));

        ((SettingsActivity) mContext).finish();
        Intent intent = new Intent(mContext, SettingsActivity.class);
        mContext.startActivity(intent);
    }

    public void setLocale(String locale) {
        Resources resources = mContext.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale lc;
        if (locale.length() > 2) {
            lc = new Locale(locale.substring(0, 2), locale.substring(3, 5));
        } else {
            lc = new Locale(locale);
        }
        if (!configuration.locale.equals(lc)) {
            configuration.setLocale(lc);
            resources.updateConfiguration(configuration, null);
        }
    }
}
