package com.germainz.activityforcenewtask;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class LogActivity extends ListActivity {

    private SettingsHelper settingsHelper;
    private ArrayAdapter adapter;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();

        settingsHelper = new SettingsHelper(context);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getLogItems());
        setListAdapter(adapter);

        float scale = getResources().getDisplayMetrics().density;
        int padding = (int) (8 * scale + 0.5f);
        getListView().setPadding(padding * 2, padding, padding * 2, padding);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.pref_log_title);
    }

    @Override
    public void onResume() {
        adapter.clear();
        adapter.addAll(getLogItems());
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
        settingsHelper.addBlacklistItem(blacklistItem);
        super.onListItemClick(listView, view, position, id);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                deleteFile(Common.LOG_FILE);
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
        BufferedReader input;
        ArrayList<String> logItems = new ArrayList<String>();
        try {
            input = new BufferedReader(new InputStreamReader(context.openFileInput(Common.LOG_FILE)));
            String line;
            while ((line = input.readLine()) != null) {
                logItems.add(line);
            }
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return logItems;
    }

}