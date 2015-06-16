package com.android.formalchat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Sve on 3/19/15.
 */
public class DialogActivtyMultiText extends Activity {
    private static final int resultCode_motto = 100;
    private static final int resultCode_aboutMe = 103;
    private static final String EXTRA_MOTTO = "mottoText";
    private static final String EXTRA_ABOUT_ME = "aboutMeText";
    private static final String EXTRA_PERFECT_SMN = "perfectSmnText";
    private static final String EXTRA_PERFECT_DATE = "perfectDateText";
    private boolean hasMotto;
    private boolean hasAboutMe;
    private EditText editField;
    private Button doneBtn;
    private ArrayList<String> textToEdit;
    Intent returnIntent;
    private HashMap<String, Object> extrasHashMap;
    private SpannableString title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        extrasHashMap = (HashMap<String, Object>)getIntent().getSerializableExtra("extras");
//        hasMotto = isExtraExists(EXTRA_MOTTO);
//        hasAboutMe = isExtraExists(EXTRA_ABOUT_ME);
        getTitleFromExtras();
        //setTitle(title);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multy_text_dialog);
        setTitle();
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

    private void setTitle() {
        TextView titleTextView = (TextView) findViewById(R.id.title_header);
        removeUnderline();
        titleTextView.setText(title);
    }

    private void removeUnderline() {
        UnderlineSpan[] uspans = title.getSpans(0, title.length(), UnderlineSpan.class);
        for(UnderlineSpan us : uspans) {
            title.removeSpan(us);
        }
    }

    private void getTitleFromExtras() {
        if(extrasHashMap.containsKey("dialog_title")) {
            title = (SpannableString)extrasHashMap.get("dialog_title");
        }
    }

    private boolean isExtraExists(String extra) {
        if(getIntent().hasExtra(extra))
            return true;
        return false;
    }

//    private void setTitle() {
//        String title;
//        if(hasMotto) {
//            title = getResources().getString(R.string.motto);
//        }
//        else if(hasAboutMe) {
//            title = getResources().getString(R.string.about_me_lbl);
//        }
//        else {
//            title = "";
//        }
//        setTitle(title);
//    }

    private void doDoneAction() {
        if(editField != null) {
            textToEdit.add(editField.getText().toString());
        }
        returnIntent = new Intent();

        setIntentResult();
        hideKeyboard();
        finish();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(doneBtn.getWindowToken(), 0);
    }

    private void setIntentResult() {
        int resultCode = getResultCode();
        String extraName = getExtraName();
        if(resultCode != 0) {
            returnIntent.putStringArrayListExtra(extraName, textToEdit);
            setResult(resultCode, returnIntent);
        }
    }

    private String getExtraName() {
        if((title.toString()).equals(getResources().getString(R.string.motto))) {
            return EXTRA_MOTTO;
        }
        else if((title.toString()).equals(getResources().getString(R.string.about_me_lbl))) {
            return EXTRA_ABOUT_ME;
        }
        else if((title.toString()).equals(getResources().getString(R.string.perfect_smn_lbl))) {
            return EXTRA_PERFECT_SMN;
        }
        else if((title.toString()).equals(getResources().getString(R.string.perfect_date_lbl))) {
            return EXTRA_PERFECT_DATE;
        }

        return null;
    }

    private int getResultCode() {
        if(extrasHashMap.containsKey("dialog_result_code")) {
            return (int) extrasHashMap.get("dialog_result_code");
        }
        return 0;
    }

    private void populateText() {
        String textToFill;
        if(extrasHashMap.containsKey("dialog_multi_txt")) {
            textToFill = (String) extrasHashMap.get("dialog_multi_txt");
        }
        else
            textToFill = null;

        if (isStarterText(textToFill)) {
            editField.setText("");
        } else {
            editField.setText(textToFill);
            editField.setSelection(textToFill.length());
        }
    }

    private boolean isStarterText(String txt) {
        if(getResources().getString(R.string.change_txt).equals(txt)) {
            return true;
        }
        else if(getResources().getString(R.string.motto).equals(txt)) {
            return true;
        }
        else if(getResources().getString(R.string.introduction_none_txt).equals(txt)) {
            return true;
        }
        else if(getResources().getString(R.string.perfectSmn_none_txt).equals(txt)) {
            return true;
        }
        else if(getResources().getString(R.string.perfectDate_none_txt).equals(txt)) {
            return true;
        }


        return false;
    }
}
