package com.android.formalchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

/**
 * Created by Sve on 3/19/15.
 */
public class DialogActivtyMultiText extends Activity {
    private static final int resultCode_motto = 100;
    private static final int resultCode_aboutMe = 103;
    private static final String EXTRA_MOTTO = "mottoText";
    private static final String EXTRA_ABOUT_ME = "aboutMeText";
    private boolean hasMotto;
    private boolean hasAboutMe;
    private EditText editField;
    private Button doneBtn;
    private ArrayList<String> textToEdit;
    Intent returnIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        hasMotto = isExtraExists(EXTRA_MOTTO);
        hasAboutMe = isExtraExists(EXTRA_ABOUT_ME);
        setTitle();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multy_text_dialog);

        textToEdit = new ArrayList<>();
        editField = (EditText) findViewById(R.id.edit_aboutMe);
        doneBtn = (Button) findViewById(R.id.done);

        populateText();

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doDoneAction();
            }
        });

    }

    private boolean isExtraExists(String extra) {
        if(getIntent().hasExtra(extra))
            return true;
        return false;
    }

    private void setTitle() {
        String title;
        if(hasMotto) {
            title = getResources().getString(R.string.motto).toUpperCase();
        }
        else if(hasAboutMe) {
            title = getResources().getString(R.string.about_me_lbl).toUpperCase();
        }
        else {
            title = "";
        }
        setTitle(title);
    }

    private void doDoneAction() {
        if(editField != null) {
            textToEdit.add(editField.getText().toString());
        }
        returnIntent = new Intent();

        setIntentResult();
        finish();
    }

    private void setIntentResult() {
        String extraName = null;
        int resultCode = 0;
        if(hasMotto) {
            extraName = EXTRA_MOTTO;
            resultCode = resultCode_motto;
        }
        else if(hasAboutMe) {
            extraName = EXTRA_ABOUT_ME;
            resultCode = resultCode_aboutMe;
        }

        if(extraName != null || resultCode != 0) {
            returnIntent.putStringArrayListExtra(extraName, textToEdit);
            setResult(resultCode, returnIntent);
        }
    }

    private void populateText() {
        ArrayList<String> textToFill;
        if(hasMotto) {
            textToFill = getIntent().getExtras().getStringArrayList(EXTRA_MOTTO);
        }
        else if(hasAboutMe) {
            textToFill = getIntent().getExtras().getStringArrayList(EXTRA_ABOUT_ME);
        }
        else
            textToFill = null;

        if(isStarterText(textToFill.get(0))) {
            editField.setText("");
        } else {
            editField.setText(textToFill.get(0));
            editField.setSelection(textToFill.get(0).length());
        }
    }

    private boolean isStarterText(String txt) {
        if(getResources().getString(R.string.change_txt).equals(txt)) {
            return true;
        }
        else if(getResources().getString(R.string.motto).equals(txt)) {
            return true;
        }

        return false;
    }
}
