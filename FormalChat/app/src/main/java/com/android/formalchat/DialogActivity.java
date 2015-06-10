package com.android.formalchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.HashMap;

/**
 * Created by Sve on 6/4/15.
 */
public class DialogActivity extends Activity {
    private SpannableString title;
    private ListView listView;
    private String[] items;
    HashMap<String, Object> extrasHashMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        extrasHashMap = (HashMap<String, Object>)getIntent().getSerializableExtra("extras");

        getTitleFromExtras();
        setTitle(title);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choice_dialog);

        listView = (ListView) findViewById(R.id.list);
        items = getArrayItems();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent returnIntent = new Intent(getApplicationContext(), MainQuestionsActivity.class);
                returnIntent.putExtra("dialog_list_position", position);
                returnIntent.putExtra("dialog_list_value", getValueOnPosition(position));
                setResult(getResultCode(), returnIntent);
                finish();
            }
        });
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
}
