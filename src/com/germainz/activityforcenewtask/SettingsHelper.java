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
    private Set<String> listItems;
    private String listType;

    // Called from module's classes.
    public SettingsHelper() {
        xSharedPreferences = new XSharedPreferences(Common.PACKAGE_NAME, Common.PREFS);
        xSharedPreferences.makeWorldReadable();
        listType = getListType();
        listItems = getListItems(listType);
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
        if (xSharedPreferences.hasFileChanged()) {
            xSharedPreferences.reload();
            listType = getListType();
            listItems = getListItems(listType);
        }
    }

    public boolean isListed(String s) {
        return listItems.contains(s);
    }

    // The methods below are only called from activities (SharedPreferences)
    public boolean addListItem(String listItem, String listType) {
        Set<String> set = new HashSet<String>();
        Set<String> listItems = getListItems(listType);
        if (listItems.contains(listItem)) {
            Toast.makeText(context, R.string.toast_duplicate, Toast.LENGTH_SHORT).show();
            return false;
        }
        set.addAll(listItems);
        set.add(listItem);
        SharedPreferences.Editor prefEditor = sharedPreferences.edit();
        prefEditor.putStringSet(listType, set);
        prefEditor.apply();
        return true;
    }

    public void removeListItem(String listItem, String listType) {
        SharedPreferences.Editor prefEditor = sharedPreferences.edit();
        Set<String> stringSet = new HashSet<String>(getListItems(listType));
        stringSet.remove(listItem);
        prefEditor.putStringSet(listType, stringSet);
        prefEditor.apply();
    }

    // These methods can be called from both
    public Set<String> getListItems(String listType) {
        Set<String> set = new HashSet<String>();
        if (sharedPreferences != null)
            return sharedPreferences.getStringSet(listType, set);
        else if (xSharedPreferences != null)
            return xSharedPreferences.getStringSet(listType, set);
        return set;
    }

    public String getListType() {
        String listTypeValue;
        if (sharedPreferences != null)
            listTypeValue = sharedPreferences.getString(Common.PREF_LIST_TYPE, Common.PREF_LIST_DEFAULT);
        else if (xSharedPreferences != null)
            if (listType == null)
                listTypeValue = xSharedPreferences.getString(Common.PREF_LIST_TYPE, Common.PREF_LIST_DEFAULT);
            else
                return listType;
        else
            return null;
        if (listTypeValue.equals(Common.PREF_LIST_WHITELIST))
            return Common.PREF_WHITELIST;
        else if (listTypeValue.equals(Common.PREF_LIST_BLACKLIST))
            return Common.PREF_BLACKLIST;
        return null;
    }


}