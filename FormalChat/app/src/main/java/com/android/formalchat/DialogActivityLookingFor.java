package com.android.formalchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by Sve on 3/19/15.
 */
public class DialogActivityLookingFor extends Activity {
    private int resultCode_lookingFor = 102;
    private ListView lookingForList;
    private String[] items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String title = getResources().getString(R.string.looking_for_title).toUpperCase();
        setTitle(title);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choice_dialog);

        lookingForList = (ListView) findViewById(R.id.list);
        items = getResources().getStringArray(R.array.looking_for_values);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        lookingForList.setAdapter(adapter);

        lookingForList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent returnIntent = new Intent(getApplicationContext(), MainQuestionsActivity.class);
                returnIntent.putExtra("lookingFor_position", position);
                returnIntent.putExtra("lookingFor_value", getValueOnPosition(position));
                setResult(resultCode_lookingFor, returnIntent);
                finish();
            }
        });
    }

    private String getValueOnPosition(int position) {
        return items[position];
    }
}
