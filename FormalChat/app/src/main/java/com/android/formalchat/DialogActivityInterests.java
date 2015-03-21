package com.android.formalchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by Sve on 3/20/15.
 */
public class DialogActivityInterests extends Activity {
    private static final int resultCode_interests = 107;
    private ListView interestsList;
    private String[] items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String title = getResources().getString(R.string.interests_lbl).toUpperCase();
        setTitle(title);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choice_dialog);

        interestsList = (ListView) findViewById(R.id.list);
        items = getResources().getStringArray(R.array.interests_values);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        interestsList.setAdapter(adapter);

        interestsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent returnIntent = new Intent(getApplicationContext(), MainQuestionsActivity.class);
                returnIntent.putExtra("interests_position", position);
                returnIntent.putExtra("interests_value", getValueOnPosition(position));
                setResult(resultCode_interests, returnIntent);
                finish();
            }
        });
    }

    private String getValueOnPosition(int position) {
        return items[position];
    }
}
