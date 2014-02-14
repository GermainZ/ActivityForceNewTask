package com.germainz.activityforcenewtask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Logger extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        String componentString = intent.getStringExtra("componentString");
        addLogItem(context, componentString);
    }

    public void addLogItem(Context context, String logItem) {
        String eol = System.getProperty("line.separator");
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(context.openFileOutput(Common.LOG_FILE, Context.MODE_APPEND)));
            writer.write(logItem + eol);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
