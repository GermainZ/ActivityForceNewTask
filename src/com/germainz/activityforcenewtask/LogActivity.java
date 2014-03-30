package com.germainz.activityforcenewtask;

import android.app.AlertDialog;
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
    final private ArrayList<String> logItems = new ArrayList<String>();
    private String listType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();

        settingsHelper = new SettingsHelper(context);
        listType = settingsHelper.getListType();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, logItems);
        setListAdapter(adapter);
        getLogItems();

        float scale = getResources().getDisplayMetrics().density;
        int padding = (int) (8 * scale + 0.5f);
        getListView().setPadding(padding * 2, padding, padding * 2, padding);
        getListView().setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.pref_log_title);
    }

    @Override
    public void onRestart() {
        getLogItems();
        super.onRestart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logactivity_menu, menu);
        return true;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        String listItem = (String) listView.getItemAtPosition(position);
        if (settingsHelper.addListItem(listItem, listType))
            Toast.makeText(context, R.string.toast_added, Toast.LENGTH_SHORT).show();
        removeLogItem(listItem);
        super.onListItemClick(listView, view, position, id);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                deleteFile(Common.LOG_FILE);
                logItems.clear();
                adapter.notifyDataSetChanged();
                break;
            case R.id.action_help:
                AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
                helpBuilder.setTitle(R.string.button_help);
                helpBuilder.setMessage(getString(R.string.logging_help,
                        getString(R.string.acceptable_activity_help)));
                helpBuilder.setPositiveButton(R.string.button_ok, null);
                helpBuilder.show();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;
        }
        return true;
    }

    private void getLogItems() {
        BufferedReader input = null;
        logItems.clear();
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
        adapter.notifyDataSetChanged();
    }

    private void removeLogItem(String logItem) {
        for (int i = logItems.size() - 1; i >= 0; i--) {
            if (logItem.equals(logItems.get(i)))
                logItems.remove(i);
        }
        adapter.notifyDataSetChanged();
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
    }

}