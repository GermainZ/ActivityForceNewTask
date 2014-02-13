package com.germainz.activityforcenewtask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Logger extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        SettingsHelper settingsHelper = new SettingsHelper(context);
        String componentString = intent.getStringExtra("componentString");
        settingsHelper.addLogItem(componentString);
    }
}
