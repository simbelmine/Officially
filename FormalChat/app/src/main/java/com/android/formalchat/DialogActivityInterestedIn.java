package com.android.formalchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by Sve on 3/18/15.
 */
public class DialogActivityInterestedIn extends Activity {
    private int resultCode_interestedIn = 101;
    private ListView interestedInList;
    private String[] items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String title = getResources().getString(R.string.interested_in_title).toUpperCase();
        setTitle(title);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choice_dialog);

        interestedInList = (ListView) findViewById(R.id.list);
        items = getResources().getStringArray(R.array.interested_in_values);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        interestedInList.setAdapter(adapter);

        interestedInList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent returnIntent = new Intent(getApplicationContext(), MainQuestionsActivity.class);
                returnIntent.putExtra("interestedIn_position", position);
                returnIntent.putExtra("interestedIn_value", getValueOnPosition(position));
                setResult(resultCode_interestedIn, returnIntent);
                finish();
            }
        });
    }

    private String getValueOnPosition(int position) {
        return items[position];
    }
}
