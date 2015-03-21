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
public class DialogActivityRelationship extends Activity {
    private static final int resultCode_relationship = 104;
    private ListView relationshipList;
    private String[] items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String title = getResources().getString(R.string.relationship_lbl).toUpperCase();
        setTitle(title);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choice_dialog);

        relationshipList = (ListView) findViewById(R.id.list);
        items = getResources().getStringArray(R.array.relationship_values);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        relationshipList.setAdapter(adapter);

        relationshipList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent returnIntent = new Intent(getApplicationContext(), MainQuestionsActivity.class);
                returnIntent.putExtra("relationship_position", position);
                returnIntent.putExtra("relationship_value", getValueOnPosition(position));
                setResult(resultCode_relationship, returnIntent);
                finish();
            }
        });
    }

    private String getValueOnPosition(int position) {
        return items[position];
    }
}
