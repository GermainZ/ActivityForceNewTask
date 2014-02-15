package com.germainz.activityforcenewtask;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.XSharedPreferences;

public class SettingsHelper {
    private XSharedPreferences xSharedPreferences = null;
    private SharedPreferences sharedPreferences = null;
    private Context context = null;

    // Called from module's classes.
    public SettingsHelper() {
        xSharedPreferences = new XSharedPreferences(Common.PACKAGE_NAME, Common.PREFS);
        xSharedPreferences.makeWorldReadable();
    }

    // Called from activities.
    public SettingsHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(Common.PREFS, Context.MODE_WORLD_READABLE);
        this.context = context;
    }

    // The methods below are only called from the module's class (XSharedPreferences)
    public boolean isModDisabled() {
        return xSharedPreferences.getBoolean(Common.PREF_DISABLED, false);
    }

    public boolean isLogEnabled() {
        return xSharedPreferences.getBoolean(Common.PREF_LOG_ENABLED, false);
    }

    public boolean isBlacklistEnabled() {
        return xSharedPreferences.getBoolean(Common.PREF_BLACKLIST_ENABLED, false);
    }

    public void reload() {
        xSharedPreferences.reload();
    }

    // The methods below are only called from activities (SharedPreferences)
    public boolean addBlacklistItem(String blacklistItem) {
        Set<String> set = new HashSet<String>();
        Set<String> blacklistItems = getBlacklistItems();
        if (blacklistItems.contains(blacklistItem)) {
            Toast.makeText(context, R.string.toast_duplicate, Toast.LENGTH_SHORT).show();
            return false;
        }
        set.addAll(blacklistItems);
        set.add(blacklistItem);
        SharedPreferences.Editor prefEditor = sharedPreferences.edit();
        prefEditor.putStringSet(Common.PREF_BLACKLIST, set);
        prefEditor.apply();
        return true;
    }

    public void removeBlacklistItem(String blacklistItem) {
        SharedPreferences.Editor prefEditor = sharedPreferences.edit();
        Set<String> stringSet = new HashSet<String>(getBlacklistItems());
        stringSet.remove(blacklistItem);
        prefEditor.putStringSet(Common.PREF_BLACKLIST, stringSet);
        prefEditor.apply();
    }

    // These methods can be called from both
    public boolean isBlacklisted(String s) {
        Set<String> set = getBlacklistItems();
        if (set.contains(s))
            return true;
        return false;
    }

    public Set getBlacklistItems() {
        Set<String> set = new HashSet<String>();
        if (sharedPreferences != null)
            return sharedPreferences.getStringSet(Common.PREF_BLACKLIST, set);
        else if (xSharedPreferences != null)
            return xSharedPreferences.getStringSet(Common.PREF_BLACKLIST, set);
        return set;
    }

}