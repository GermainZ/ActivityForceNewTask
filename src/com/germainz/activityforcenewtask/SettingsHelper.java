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

    // Only needed from the module's class (XSharedPreferences)
    public boolean isModDisabled() {
        return xSharedPreferences.getBoolean("pref_disabled", false);
    }

    // Only needed from the module's class (XSharedPreferences)
    public boolean isLogEnabled() {
        return xSharedPreferences.getBoolean("pref_log_enable", false);
    }

    public boolean isBlacklisted(String s) {
        Set<String> set = getBlacklistItems();
        if (set.contains(s))
            return true;
        return false;
    }

    public Set getBlacklistItems() {
        Set<String> set = new HashSet<String>();
        return getStringSet("blacklist", set);
    }

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
        prefEditor.putStringSet("blacklist", set);
        prefEditor.apply();
        return true;
    }

    public void removeBlacklistItem(String blacklistItem) {
        SharedPreferences.Editor prefEditor = sharedPreferences.edit();
        Set<String> stringSet = new HashSet<String>(getBlacklistItems());
        stringSet.remove(blacklistItem);
        prefEditor.putStringSet("blacklist", stringSet);
        prefEditor.apply();
    }

    public Set getLogItems() {
        Set<String> set = new HashSet<String>();
        return getStringSet("log", set);
    }

    public void addLogItem(String logItem) {
        Set<String> set = new HashSet<String>();
        set.addAll(getLogItems());
        set.add(logItem);
        SharedPreferences.Editor prefEditor = sharedPreferences.edit();
        prefEditor.putStringSet("log", set);
        prefEditor.apply();
    }

    public void clearLog() {
        SharedPreferences.Editor prefEditor = sharedPreferences.edit();
        prefEditor.remove("log");
        prefEditor.apply();
    }

    private Set getStringSet(String key, Set<String> defValue) {
        if (sharedPreferences != null)
            return sharedPreferences.getStringSet(key, defValue);
        else if (xSharedPreferences != null)
            return xSharedPreferences.getStringSet(key, defValue);
        return defValue;
    }

    // Only needed for XSharedPreferences
    public void reload() {
        xSharedPreferences.reload();
    }
}