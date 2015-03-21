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
public class DialogActivityEthnicity extends Activity {
    private static final int resultCode_ethnicity = 106;
    private ListView ethnicityList;
    private String[] items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String title = getResources().getString(R.string.ethnicity_lbl).toUpperCase();
        setTitle(title);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choice_dialog);

        ethnicityList = (ListView) findViewById(R.id.list);
        items = getResources().getStringArray(R.array.ethnicity_values);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        ethnicityList.setAdapter(adapter);

        ethnicityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent returnIntent = new Intent(getApplicationContext(), MainQuestionsActivity.class);
                returnIntent.putExtra("ethnicity_position", position);
                returnIntent.putExtra("ethnicity_value", getValueOnPosition(position));
                setResult(resultCode_ethnicity, returnIntent);
                finish();
            }
        });
    }

    private String getValueOnPosition(int position) {
        return items[position];
    }
}
