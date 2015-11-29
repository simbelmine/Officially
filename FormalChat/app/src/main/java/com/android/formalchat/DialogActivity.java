package com.android.formalchat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Sve on 6/4/15.
 */
public class DialogActivity extends Activity {
    private static final String PREFS_INFO = "FormalChatUserInfo";
    private SpannableString title;
    private ListView listView;
    private Button doneButton;
    private String[] items;
    private HashMap<String, Object> extrasHashMap;
    private Intent returnIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        extrasHashMap = (HashMap<String, Object>)getIntent().getSerializableExtra("extras");

        getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.night_transp_black_80)));
        getTitleFromExtras();
        setTitle(title);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choice_dialog);

        doneButton = (Button) findViewById(R.id.done);
        listView = (ListView) findViewById(R.id.list);
        items = getArrayItems();
        ArrayAdapter<String> adapter;
        returnIntent = new Intent(getApplicationContext(), MainQuestionsActivity.class);


        if(isMultiChoiceDialog()) {
            doneButton.setVisibility(View.VISIBLE);
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, items);
            listView.setAdapter(adapter);

            populateFromPrefs();
        }
        else {
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
            listView.setAdapter(adapter);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(isMultiChoiceDialog()) {
                    view.setSelected(true);
                    SparseBooleanArray checked = listView.getCheckedItemPositions();
                    StringBuffer selectedItems = new StringBuffer();
                    String comma = "";
                    for(int i = 0; i < checked.size(); i++) {
                        int pos = checked.keyAt(i);
                        if(checked.valueAt(i)) {
                            selectedItems.append(comma);
                            comma = ",";
                            selectedItems.append(items[pos]);
                        }
                    }
                    returnIntent.putExtra("dialog_list_position", position);
                    returnIntent.putExtra("dialog_list_value", selectedItems.toString());
                }
                else {
                    returnIntent.putExtra("dialog_list_position", position);
                    returnIntent.putExtra("dialog_list_value", getValueOnPosition(position));
                    returnResult();
                }
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnResult();
            }
        });
    }

    private void populateFromPrefs() {
        SharedPreferences sharedInfoPreferences = getSharedPreferences(PREFS_INFO, 0);
        if (extrasHashMap.containsKey("dialog_multi_choice_field")) {
            String fieldToUse = (String)extrasHashMap.get("dialog_multi_choice_field");
            if(fieldToUse != null && !fieldToUse.isEmpty()) {
                String choicesToSelect = sharedInfoPreferences.getString(fieldToUse, null);
                if(choicesToSelect != null && !choicesToSelect.isEmpty()) {
                    List<String> choices = getChoices(choicesToSelect);
                    ArrayList<String> itemsList = new ArrayList<>(Arrays.asList(items));
                    for(int pos = 0; pos < itemsList.size(); pos++) {
                        if(choices.contains(itemsList.get(pos))) {
                            listView.setItemChecked(pos, true);
                        }
                    }
                }
            }
        }
    }

    private List<String> getChoices(String choicesToSelect) {
        return Arrays.asList(choicesToSelect.split(","));
    }

    private void returnResult() {
        setResult(getResultCode(), returnIntent);
        finish();
    }

    private int getResultCode() {
         if(extrasHashMap.containsKey("dialog_result_code")) {
            return (int)extrasHashMap.get("dialog_result_code");
        }
        return 0;
    }

    private String[] getArrayItems() {
        if(extrasHashMap.containsKey("dialog_list_items")) {
            return (String[])extrasHashMap.get("dialog_list_items");
        }
        return new String[0];
    }

    private void getTitleFromExtras() {
        if(extrasHashMap.containsKey("dialog_title")) {
            title = (SpannableString)extrasHashMap.get("dialog_title");
        }
    }

    private String getValueOnPosition(int position) {
        return items[position];
    }


    private boolean isMultiChoiceDialog() {
        if(extrasHashMap.containsKey("dialog_multi_choice") && (boolean)extrasHashMap.get("dialog_multi_choice")) {
            return true;
        }
        return false;
    }
}
