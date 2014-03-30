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
    private String listType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settingsHelper = new SettingsHelper(getApplicationContext());
        listType = settingsHelper.getListType();
        ArrayList<String> listItems = new ArrayList<String>(settingsHelper.getListItems(listType));
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        setListAdapter(adapter);

        float scale = getResources().getDisplayMetrics().density;
        int padding = (int) (8 * scale + 0.5f);
        getListView().setPadding(padding * 2, padding, padding * 2, padding);
        getListView().setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        if (listType.equals(Common.PREF_BLACKLIST))
            setTitle(R.string.list_type_entries_blacklist);
        else
            setTitle(R.string.list_type_entries_whitelist);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.blacklist_menu, menu);
        return true;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        String listItem = (String) listView.getItemAtPosition(position);
        removeListItem(listItem, listType);
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
                        addListItem(userInput, listType);
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
            case R.id.action_help:
                AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
                helpBuilder.setTitle(R.string.button_help);
                if (listType.equals(Common.PREF_BLACKLIST))
                    helpBuilder.setMessage(getString(R.string.blacklist_help,
                            getString(R.string.acceptable_activity_help)));
                else
                    helpBuilder.setMessage(getString(R.string.whitelist_help,
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

    private void addListItem(String s, String listType) {
        boolean isNotDuplicate = settingsHelper.addListItem(s, listType);
        if (isNotDuplicate)
            adapter.add(s);
    }

    private void removeListItem(String s, String listType) {
        settingsHelper.removeListItem(s, listType);
        adapter.remove(s);
    }

}