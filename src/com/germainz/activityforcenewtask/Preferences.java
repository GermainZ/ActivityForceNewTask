package com.germainz.activityforcenewtask;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class Preferences extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null)
            getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();

    }

    public static class PrefsFragment extends PreferenceFragment {

        @SuppressWarnings("deprecation")
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
            addPreferencesFromResource(R.xml.prefs);
            final Activity activity = getActivity();

            final Preference pref_show_app_icon = this.findPreference("pref_show_app_icon");
            pref_show_app_icon.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    PackageManager p = activity.getPackageManager();
                    int state = (Boolean) newValue ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                    final ComponentName alias = new ComponentName(activity, "com.germainz.activityforcenewtask.Preferences-Alias");
                    p.setComponentEnabledSetting(alias, state, PackageManager.DONT_KILL_APP);
                    return true;
                }
            });

            final Preference pref_list = this.findPreference("pref_list");
            pref_list.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    Intent i = new Intent(activity, BlacklistActivity.class);
                    startActivity(i);
                    return true;
                }
            });

            final Preference pref_log = this.findPreference("pref_log");
            pref_log.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    Intent i = new Intent(activity, LogActivity.class);
                    startActivity(i);
                    return true;
                }
            });

        }
    }
}