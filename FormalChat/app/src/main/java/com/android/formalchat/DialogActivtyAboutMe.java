package com.android.formalchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Sve on 3/19/15.
 */
public class DialogActivtyAboutMe extends Activity {
    private static final int resultCode_aboutMe = 103;
    private EditText editAboutMe;
    private Button doneBtn;
    private ArrayList<String> aboutMeList;
    Intent returnIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String title = getResources().getString(R.string.about_me_lbl).toUpperCase();
        setTitle(title);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multy_text_dialog);

        aboutMeList = new ArrayList<>();
        editAboutMe = (EditText) findViewById(R.id.edit_aboutMe);
        doneBtn = (Button) findViewById(R.id.done);

        if(isEmptyAboutMe()) {
            populateAboutMe();
        }

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doDoneAction();
            }
        });

    }

    private void doDoneAction() {
        if(editAboutMe != null) {
            aboutMeList.add(editAboutMe.getText().toString());
        }
        returnIntent = new Intent(getApplicationContext(), MainQuestionsActivity.class);
        Log.v("formalchat", "aboutMe dialog = " + aboutMeList.get(0).toString());
        returnIntent.putStringArrayListExtra("aboutMeList", aboutMeList);
        setResult(resultCode_aboutMe, returnIntent);
        finish();
    }

    private void populateAboutMe() {
        ArrayList<String> aboutMeTxt = getIntent().getExtras().getStringArrayList("aboutMeList_actvty");
        if(isStarterText(aboutMeTxt.get(0))) {
            editAboutMe.setText("");
        } else {
            editAboutMe.setText(aboutMeTxt.get(0));
            editAboutMe.setSelection(aboutMeTxt.get(0).length());
        }

    }

    private boolean isStarterText(String txt) {
        if(getResources().getString(R.string.multy_txt).equals(txt)) {
            return true;
        }
        return false;
    }

    private boolean isEmptyAboutMe() {
        return getIntent().hasExtra("aboutMeList_actvty");
    }
}
