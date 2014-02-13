package com.germainz.activityforcenewtask;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class BlacklistActivity extends ListActivity {

    private SettingsHelper settingsHelper;
    private ArrayAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settingsHelper = new SettingsHelper(getApplicationContext());
        ArrayList<String> blacklistItems = new ArrayList<String>(settingsHelper.getBlacklistItems());
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, blacklistItems);
        setListAdapter(adapter);

        float scale = getResources().getDisplayMetrics().density;
        int padding = (int) (8 * scale + 0.5f);
        getListView().setPadding(padding*2, padding, padding*2, padding);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.pref_blacklist_title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.blacklist_menu, menu);
        return true;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        String blacklistItem = (String) listView.getItemAtPosition(position);
        removeBlacklistItem(blacklistItem);
        super.onListItemClick(listView, view, position, id);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.button_add);
                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                builder.setView(input);
                builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String userInput = input.getText().toString();
                        addBlacklistItem(userInput);
                    }
                });
                builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                alert.show();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;
        }
        return true;
    }

    public void addBlacklistItem(String s) {
        settingsHelper.addBlacklistItem(s);
        adapter.add(s);
    }

    public void removeBlacklistItem(String s) {
        settingsHelper.removeBlacklistItem(s);
        adapter.remove(s);
    }

}