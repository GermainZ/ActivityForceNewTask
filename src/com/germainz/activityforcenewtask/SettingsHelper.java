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

    public void reload() {
        xSharedPreferences.reload();
    }

    // The methods below are only called from activities (SharedPreferences)
    public boolean addListItem(String listItem) {
        Set<String> set = new HashSet<String>();
        Set<String> listItems = getListItems();
        if (listItems.contains(listItem)) {
            Toast.makeText(context, R.string.toast_duplicate, Toast.LENGTH_SHORT).show();
            return false;
        }
        set.addAll(listItems);
        set.add(listItem);
        SharedPreferences.Editor prefEditor = sharedPreferences.edit();
        prefEditor.putStringSet(Common.WHITELIST, set);
        prefEditor.apply();
        return true;
    }

    public void removeListItem(String listItem) {
        SharedPreferences.Editor prefEditor = sharedPreferences.edit();
        Set<String> stringSet = new HashSet<String>(getListItems());
        stringSet.remove(listItem);
        prefEditor.putStringSet(Common.WHITELIST, stringSet);
        prefEditor.apply();
    }

    // These methods can be called from both
    public boolean isListed(String s) {
        Set<String> set = getListItems();
        return set.contains(s);
    }

    public Set getListItems() {
        Set<String> set = new HashSet<String>();
        if (sharedPreferences != null)
            return sharedPreferences.getStringSet(Common.WHITELIST, set);
        else if (xSharedPreferences != null)
            return xSharedPreferences.getStringSet(Common.WHITELIST, set);
        return set;
    }

}