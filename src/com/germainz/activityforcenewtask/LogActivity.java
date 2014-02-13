package com.germainz.activityforcenewtask;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class LogActivity extends ListActivity {

    private SettingsHelper settingsHelper;
    private ArrayAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settingsHelper = new SettingsHelper(getApplicationContext());
        ArrayList<String> logItems = new ArrayList<String>(settingsHelper.getLogItems());
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, logItems);
        setListAdapter(adapter);

        float scale = getResources().getDisplayMetrics().density;
        int padding = (int) (8 * scale + 0.5f);
        getListView().setPadding(padding * 2, padding, padding * 2, padding);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.pref_log_title);
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
                settingsHelper.clearLog();
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

}