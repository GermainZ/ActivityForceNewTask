package com.germainz.activityforcenewtask;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class LogActivity extends ListActivity {

    private SettingsHelper settingsHelper;
    private ArrayAdapter adapter;
    private Context context;
    private ArrayList<String> logItems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();

        settingsHelper = new SettingsHelper(context);
        logItems = getLogItems();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, logItems);
        setListAdapter(adapter);

        float scale = getResources().getDisplayMetrics().density;
        int padding = (int) (8 * scale + 0.5f);
        getListView().setPadding(padding * 2, padding, padding * 2, padding);
        getListView().setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.pref_log_title);
    }

    @Override
    public void onRestart() {
        logItems = getLogItems();
        adapter.clear();
        adapter.addAll(logItems);
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logactivity_menu, menu);
        return true;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        String blacklistItem = (String) listView.getItemAtPosition(position);
        if (settingsHelper.addBlacklistItem(blacklistItem))
            Toast.makeText(context, R.string.toast_added, Toast.LENGTH_SHORT).show();
        removeLogItem(blacklistItem);
        super.onListItemClick(listView, view, position, id);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                deleteFile(Common.LOG_FILE);
                logItems.clear();
                adapter.clear();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;
        }
        return true;
    }

    public ArrayList<String> getLogItems() {
        BufferedReader input = null;
        ArrayList<String> logItems = new ArrayList<String>();
        try {
            input = new BufferedReader(new InputStreamReader(context.openFileInput(Common.LOG_FILE)));
            String line;
            while ((line = input.readLine()) != null) {
                logItems.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return logItems;
    }

    public void removeLogItem(String logItem) {
        for (int i = logItems.size() - 1; i >= 0; i--) {
            if (logItem.equals(logItems.get(i))) {
                logItems.remove(i);
                adapter.remove(logItem);
            }
        }
        String eol = System.getProperty("line.separator");
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(context.openFileOutput(Common.LOG_FILE, Context.MODE_PRIVATE)));
            for (String line : logItems)
                writer.write(line + eol);
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
        return;
    }

}