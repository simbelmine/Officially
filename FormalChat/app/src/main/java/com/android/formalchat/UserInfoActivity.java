package com.android.formalchat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.android.formalchat.profile.ProfileActivityCurrent;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sve on 2/18/15.
 */
public class UserInfoActivity extends AppCompatActivity {
    private static final int resultCode_motto = 100;
    private static final int resultCode_drinking = 101;
    private static final int resultCode_smoking = 102;
    private static final int resultCode_religion = 103;
    private static final int resultCode_height = 104;
    private static final int resultCode_bodyType = 105;
    private static final int resultCode_relationship = 106;
    private static final int resultCode_children = 107;
    private static final int resultCode_ethnicity = 108;
    private static final int resultCode_education = 109;
    private static final int resultCode_aboutMe = 110;
    private static final int resultCode_perfectSmn = 111;
    private static final int resultCode_perfectDate = 112;
    private static final int resultCode_interests = 113;
    //    private static final int resultCode_interestedIn = 101;
//    private static final int resultCode_lookingFor = 102;
    private static final String EXTRA_MOTTO = "mottoText";
    private static final String EXTRA_ABOUT_ME = "aboutMeText";
    private static final String EXTRA_PERFECT_SMN = "perfectSmnText";
    private static final String EXTRA_PERFECT_DATE = "perfectDateText";
    private static final String PREFS_INFO = "FormalChatUserInfo";
    private static final String PREFS_INFO_LOCAL = "FormalChatUserInfoLocal";
    private static final String PREFS_QUESTIONARY_LOCAL = "FormalChatUserQuestionaryLocal";
    private static TextView name;
    private static TextView age;

    private static TextView motto;
    private TextView drinking;
    private TextView smoking;
    private TextView religion;
    private TextView height;
    private static TextView body_type;
    private static TextView relationship;
    private TextView children;
    private static TextView ethnicity;
    private TextView education;
    private static TextView about_me;
    private TextView perfectSmn;
    private TextView perfectDate;
    private static TextView interests;

    private int drinking_position;
    private int smoking_position;
    private int religion_position;
    private int bodyType_position;
    private int relationship_position;
    private int children_position;
    private int ethnicity_position;
    private int education_position;
    private int interests_position;
    private int height_position;

    private static Button saveBtn;
    //    private static TextView interested_in;
//    private static TextView looking_for;
//    private int interestedIn_position;
//    private int lookingFor_position;
    private SharedPreferences infoPrefs_local;
    private SharedPreferences.Editor infoEditor;
    private SharedPreferences questionaryPrefs_local;
    private SharedPreferences.Editor questionaryEditor;
    private SharedPreferences sharedInfoPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_info_new);

        initToolbar();
        initActionBar();

        sharedInfoPreferences = getSharedPreferences(PREFS_INFO, 0);
        infoPrefs_local = getSharedPreferences(PREFS_INFO_LOCAL, 0);
        infoEditor = infoPrefs_local.edit();
        questionaryPrefs_local = getSharedPreferences(PREFS_QUESTIONARY_LOCAL, 0);
        questionaryEditor = questionaryPrefs_local.edit();

        initialiseViewItems();
        populateInfoFromExtras();
        setOnClickListeners();
//        addListenerOnSpinnerItemSelection();
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChangesToParse();
                goToProfileActivity();
            }
        });
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
    }

    private void initActionBar() {
        getSupportActionBar().hide();
    }

    private void setOnClickListeners() {
        // *** Multi Choice *** //
        motto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> extras = new HashMap<>();
                SpannableString title = new SpannableString(getResources().getString(R.string.motto));
                title.setSpan(new UnderlineSpan(), 0, title.length(), 0);
                extras.put("dialog_title", title);
                extras.put("dialog_result_code", resultCode_motto);
                extras.put("dialog_multi_txt", motto.getText().toString());
                startDialogActivity(resultCode_motto, extras);
            }
        });
        drinking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> extras = new HashMap<>();
                SpannableString title = new SpannableString(getResources().getString(R.string.drinking_lbl));
                title.setSpan(new UnderlineSpan(), 0, title.length(), 0);
                extras.put("dialog_title",title);
                extras.put("dialog_result_code", resultCode_drinking);
                extras.put("dialog_list_items", getResources().getStringArray(R.array.a_your_drinking));

                startDialogActivity(resultCode_drinking, extras);
            }
        });
        smoking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> extras = new HashMap<>();
                SpannableString title = new SpannableString(getResources().getString(R.string.smoking_lbl));
                title.setSpan(new UnderlineSpan(), 0, title.length(), 0);
                extras.put("dialog_title",title);
                extras.put("dialog_result_code", resultCode_smoking);
                extras.put("dialog_list_items", getResources().getStringArray(R.array.a_your_smoking));

                startDialogActivity(resultCode_smoking, extras);
            }
        });
        religion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> extras = new HashMap<>();
                SpannableString title = new SpannableString(getResources().getString(R.string.religion_lbl));
                title.setSpan(new UnderlineSpan(), 0, title.length(), 0);
                extras.put("dialog_title",title);
                extras.put("dialog_result_code", resultCode_religion);
                extras.put("dialog_list_items", getResources().getStringArray(R.array.a_your_religion));

                startDialogActivity(resultCode_religion, extras);
            }
        });
        height.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final NumberPicker picker = getNumberPicker();
                final FrameLayout parent = new FrameLayout(UserInfoActivity.this);
                addNumberPickerToLayout(parent, picker);
                createNumberPickerDialog(parent, picker);
            }
        });
        body_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> extras = new HashMap<>();
                SpannableString title = new SpannableString(getResources().getString(R.string.body_type_lbl));
                title.setSpan(new UnderlineSpan(), 0, title.length(), 0);
                extras.put("dialog_title", title);
                extras.put("dialog_result_code", resultCode_bodyType);
                extras.put("dialog_list_items", getResources().getStringArray(R.array.body_type_values));

                startDialogActivity(resultCode_bodyType, extras);
            }
        });
        relationship.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> extras = new HashMap<>();
                SpannableString title = new SpannableString(getResources().getString(R.string.relationship_lbl));
                title.setSpan(new UnderlineSpan(), 0, title.length(), 0);
                extras.put("dialog_title", title);
                extras.put("dialog_result_code", resultCode_relationship);
                extras.put("dialog_list_items", getResources().getStringArray(R.array.relationship_values));

                startDialogActivity(resultCode_relationship, extras);
            }
        });
        children.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> extras = new HashMap<>();
                SpannableString title = new SpannableString(getResources().getString(R.string.children_lbl));
                title.setSpan(new UnderlineSpan(), 0, title.length(), 0);
                extras.put("dialog_title", title);
                extras.put("dialog_result_code", resultCode_children);
                extras.put("dialog_list_items", getResources().getStringArray(R.array.children_values));

                startDialogActivity(resultCode_children, extras);
            }
        });
        ethnicity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> extras = new HashMap<>();
                SpannableString title = new SpannableString(getResources().getString(R.string.ethnicity_lbl));
                title.setSpan(new UnderlineSpan(), 0, title.length(), 0);
                extras.put("dialog_title", title);
                extras.put("dialog_result_code", resultCode_ethnicity);
                extras.put("dialog_layout_id", R.layout.choice_dialog);
                extras.put("dialog_list_items", getResources().getStringArray(R.array.ethnicity_values));

                startDialogActivity(resultCode_ethnicity, extras);
            }
        });
        education.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> extras = new HashMap<>();
                SpannableString title = new SpannableString(getResources().getString(R.string.education_lbl));
                title.setSpan(new UnderlineSpan(), 0, title.length(), 0);
                extras.put("dialog_title", title);
                extras.put("dialog_result_code", resultCode_education);
                extras.put("dialog_list_items", getResources().getStringArray(R.array.education_values));

                startDialogActivity(resultCode_education, extras);
            }
        });
        about_me.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> extras = new HashMap<>();
                SpannableString title = new SpannableString(getResources().getString(R.string.about_me_lbl));
                title.setSpan(new UnderlineSpan(), 0, title.length(), 0);
                extras.put("dialog_title", title);
                extras.put("dialog_result_code", resultCode_aboutMe);
                extras.put("dialog_multi_txt", about_me.getText().toString());
                startDialogActivity(resultCode_aboutMe, extras);
            }
        });
        perfectSmn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> extras = new HashMap<>();
                SpannableString title = new SpannableString(getResources().getString(R.string.perfect_smn_lbl));
                title.setSpan(new UnderlineSpan(), 0, title.length(), 0);
                extras.put("dialog_title", title);
                extras.put("dialog_result_code", resultCode_perfectSmn);
                extras.put("dialog_multi_txt", perfectSmn.getText().toString());
                startDialogActivity(resultCode_perfectSmn, extras);
            }
        });
        perfectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> extras = new HashMap<>();
                SpannableString title = new SpannableString(getResources().getString(R.string.perfect_date_lbl));
                title.setSpan(new UnderlineSpan(), 0, title.length(), 0);
                extras.put("dialog_title", title);
                extras.put("dialog_result_code", resultCode_perfectDate);
                extras.put("dialog_multi_txt", perfectDate.getText().toString());
                startDialogActivity(resultCode_perfectDate, extras);
            }
        });
        interests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> extras = new HashMap<>();
                SpannableString title = new SpannableString(getResources().getString(R.string.interests_lbl));
                title.setSpan(new UnderlineSpan(), 0, title.length(), 0);
                extras.put("dialog_title", title);
                extras.put("dialog_result_code", resultCode_interests);
                extras.put("dialog_list_items", getResources().getStringArray(R.array.interests_values));
                extras.put("dialog_multi_choice", true);
                extras.put("dialog_multi_choice_field", "interests");

                startDialogActivity(resultCode_interests, extras);
            }
        });
    }

    private void createNumberPickerDialog(FrameLayout parent, final NumberPicker picker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserInfoActivity.this, R.style.AlertDialog);
        builder.setTitle(getResources().getString(R.string.height_lbl));
        builder.setView(parent);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                height.setText(String.valueOf(picker.getValue()));
                infoEditor.putInt("height", picker.getValue());
                infoEditor.commit();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.night_transp_black_80)));
        dialog.show();
    }

    private void addNumberPickerToLayout(FrameLayout parent, NumberPicker picker) {
        parent.addView(picker, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER));
    }

    private NumberPicker getNumberPicker() {
        NumberPicker picker =  new NumberPicker(UserInfoActivity.this);
        picker.setMinValue(21);
        picker.setMaxValue(107);
        if(sharedInfoPreferences.contains("height")) {
            int heightValue = Integer.parseInt(sharedInfoPreferences.getString("height", "0"));
            if(heightValue != 0) {
                picker.setValue(heightValue);
            }
        }
        else {
            picker.setValue(66);
        }

        picker.setBackgroundColor(getResources().getColor(R.color.items_40));
        picker.setScaleY((float)0.75);

        return picker;
    }

    private void startDialogActivity(int resultCode, HashMap<String, Object> extras) {
        Intent intent;
        if(resultCode == resultCode_motto || resultCode == resultCode_aboutMe ||
                resultCode == resultCode_perfectSmn || resultCode == resultCode_perfectDate) {
            intent = new Intent(getApplicationContext(), DialogActivtyMultiText.class);
        }
        else {
            intent = new Intent(getApplicationContext(), DialogActivity.class);
        }

        intent.putExtra("extras", extras);

        startActivityForResult(intent, resultCode);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(resultCode) {
            case resultCode_motto:
                if(!isExtraEmpty(EXTRA_MOTTO)) {
                    String motto_txt = getExtraText(EXTRA_MOTTO, data);
                    if(motto_txt != null) {
                        //motto.setText(motto_txt);
                        setText(EXTRA_MOTTO, motto_txt);
                        infoEditor.putString("motto", motto_txt);
                        infoEditor.commit();
                    }
                }
                break;
            case resultCode_drinking:
                drinking_position = data.getIntExtra("dialog_list_position", 0);
                String value_d = data.getStringExtra("dialog_list_value");
                drinking.setText(value_d);
                questionaryEditor.putInt("yourDrinking", drinking_position);
                questionaryEditor.commit();
                break;
            case resultCode_smoking:
                smoking_position = data.getIntExtra("dialog_list_position", 0);
                String value_s = data.getStringExtra("dialog_list_value");
                smoking.setText(value_s);
                questionaryEditor.putInt("yourSmoking", smoking_position);
                questionaryEditor.commit();
                break;
            case resultCode_religion:
                religion_position = data.getIntExtra("dialog_list_position", 0);
                String value_re = data.getStringExtra("dialog_list_value");
                religion.setText(value_re);
                questionaryEditor.putInt("yourReligion", religion_position);
                questionaryEditor.commit();
                break;
            case resultCode_bodyType:
                bodyType_position = data.getIntExtra("dialog_list_position", 0);
                String value_bt = data.getStringExtra("dialog_list_value");
                body_type.setText(value_bt);
                infoEditor.putInt("bodyType", bodyType_position);
                infoEditor.commit();
                break;
            case resultCode_relationship:
                relationship_position = data.getIntExtra("dialog_list_position", 0);
                String value_r = data.getStringExtra("dialog_list_value");
                relationship.setText(value_r);
                infoEditor.putInt("relationship", relationship_position);
                infoEditor.commit();
                break;
            case resultCode_children:
                children_position = data.getIntExtra("dialog_list_position", 0);
                String value_c = data.getStringExtra("dialog_list_value");
                children.setText(value_c);
                infoEditor.putInt("children", children_position);
                infoEditor.commit();
                break;
            case resultCode_ethnicity:
                ethnicity_position = data.getIntExtra("dialog_list_position", 0);
                String value_e = data.getStringExtra("dialog_list_value");
                ethnicity.setText(value_e);
                questionaryEditor.putInt("yourEthnicity", ethnicity_position);
                questionaryEditor.commit();
                break;
            case resultCode_education:
                education_position = data.getIntExtra("dialog_list_position", 0);
                String value_ed = data.getStringExtra("dialog_list_value");
                education.setText(value_ed);
                infoEditor.putInt("education", education_position);
                infoEditor.commit();
                break;
            case resultCode_aboutMe:
                if(!isExtraEmpty(EXTRA_ABOUT_ME)) {
                    String aboutMe_txt = getExtraText(EXTRA_ABOUT_ME, data);
                    if(aboutMe_txt != null) {
                        about_me.setText(aboutMe_txt);
                        infoEditor.putString("aboutMe", aboutMe_txt);
                        infoEditor.commit();
                    }
                }
                break;
            case resultCode_perfectSmn:
                if(!isExtraEmpty(EXTRA_PERFECT_SMN)) {
                    String perfectSmn_txt = getExtraText(EXTRA_PERFECT_SMN, data);
                    perfectSmn.setText(perfectSmn_txt);
                    infoEditor.putString("perfectSmn", perfectSmn_txt);
                    infoEditor.commit();
                }
                break;
            case resultCode_perfectDate:
                if(!isExtraEmpty(EXTRA_PERFECT_DATE)) {
                    String perfectDate_txt = getExtraText(EXTRA_PERFECT_DATE, data);
                    if(perfectDate_txt != null) {
                        perfectDate.setText(perfectDate_txt);
                        infoEditor.putString("perfectDate", perfectDate_txt);
                        infoEditor.commit();
                    }
                }
                break;
            case resultCode_interests:
                interests_position = data.getIntExtra("dialog_list_position", 0);
                String value_i = data.getStringExtra("dialog_list_value");
                interests.setText(value_i);
                infoEditor.putString("interests", value_i);
                infoEditor.commit();
                break;
            default:
                break;
        }
    }

    private void setText(String extra, String txt) {
        switch (extra) {
            case EXTRA_MOTTO:
                if(txt.equals("\t"))
                    motto.setText(getResources().getString(R.string.motto));
                else
                    motto.setText(txt);
                break;
            case EXTRA_ABOUT_ME:
                if(txt.equals(""))
                    about_me.setText(getResources().getString(R.string.introduction_none_txt));
                else
                    about_me.setText(txt);
                break;
            case EXTRA_PERFECT_SMN:
                if(txt.equals(""))
                    perfectSmn.setText(getResources().getString(R.string.perfectSmn_none_txt));
                else
                    perfectSmn.setText(txt);
                break;
            case EXTRA_PERFECT_DATE:
                if(txt.equals(""))
                    perfectDate.setText(getResources().getString(R.string.perfectDate_none_txt));
                else
                    perfectDate.setText(txt);
                break;
        }
    }

    private String getExtraText(String extra, Intent data) {
        ArrayList<String> aboutMe_list = data.getStringArrayListExtra(extra);
        String stringValue = "";

        for (String s : aboutMe_list) {
            stringValue += s + "\t"; // *** can use \b to go one position back, if it's necessary
        }

        return stringValue;
    }

    private boolean isExtraEmpty(String extra) {
        return getIntent().hasExtra(extra);
    }

    private void goToProfileActivity() {
        Intent intent = new Intent(this, ProfileActivityCurrent.class);
        startActivity(intent);
    }

    private void saveChangesToParse() {
        ParseQuery<ParseObject> queryQuestionary = ParseQuery.getQuery("UserQuestionary");
        Map<String, ?> mapQuestionary = questionaryPrefs_local.getAll();
        save(mapQuestionary, queryQuestionary);

        ParseQuery<ParseObject> queryUserInfo = ParseQuery.getQuery("UserInfo");
        Map<String, ?> sharedPrefsMap = infoPrefs_local.getAll();
        save(sharedPrefsMap, queryUserInfo);
    }

    private void save(final Map<String, ?> sharedPrefsMap, ParseQuery<ParseObject> parseQuery) {
        parseQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if(e == null) {
                    saveToExistingUserInfo(sharedPrefsMap, parseObject);
                }
                else {
                    Log.e("formalchat", "User Info: No result from search");
                }
            }
        });
    }

    private void saveToExistingUserInfo(Map<String, ?> sharedPrefsMap, ParseObject parseObject) {
        for(Map.Entry<String, ?> entry : sharedPrefsMap.entrySet()) {
//            Log.v("formalchat", entry.getKey() + " : " + entry.getValue());
            parseObject.put(entry.getKey().toString(), entry.getValue());
        }

        parseObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.e("formalchat", "Questionary was saved Successfully !");
                } else {
                    Log.e("formalchat", "Error saving: " + e.getMessage());
                }
            }
        });
    }

//    private void addListenerOnSpinnerItemSelection() {
//        gender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                Log.v("formalchat", "It was selected position: " + position);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//    }

    private void initialiseViewItems() {
        name = (TextView) findViewById(R.id.name_edit);
        age = (TextView) findViewById(R.id.age_edit);

        motto = (TextView) findViewById(R.id.motto);
        drinking = (TextView) findViewById(R.id.drinking_edit);
        smoking = (TextView) findViewById(R.id.smoking_edit);
        religion = (TextView) findViewById(R.id.religion_edit);
        height = (TextView) findViewById(R.id.height_edit);
        body_type = (TextView) findViewById(R.id.body_type_edit);
        relationship = (TextView) findViewById(R.id.relationship_edit);
        children = (TextView) findViewById(R.id.children_edit);
        ethnicity = (TextView) findViewById(R.id.ethnicity_edit);
        education = (TextView) findViewById(R.id.education_edit);
        about_me = (TextView) findViewById(R.id.about_me_edit);
        perfectSmn = (TextView) findViewById(R.id.perfect_smn_edit);
        perfectDate = (TextView) findViewById(R.id.perfect_date_edit);
        interests = (TextView) findViewById(R.id.interests_edit);
        saveBtn = (Button) findViewById(R.id.save_btn);
//        interested_in = (TextView) findViewById(R.id.interested_in_edit);
//        looking_for = (TextView) findViewById(R.id.looking_for_edit);
    }

    private void populateInfoFromExtras() {
        name.setText(sharedInfoPreferences.getString("name", getResources().getString(R.string.change_txt)));
        age.setText(sharedInfoPreferences.getString("age", getResources().getString(R.string.choice_txt)));

        motto.setText(sharedInfoPreferences.getString("motto", getResources().getString(R.string.motto)));
        drinking.setText(sharedInfoPreferences.getString("yourDrinking", getResources().getString(R.string.choice_txt)));
        smoking.setText(sharedInfoPreferences.getString("yourSmoking", getResources().getString(R.string.choice_txt)));
        religion.setText(sharedInfoPreferences.getString("yourReligion", getResources().getString(R.string.choice_txt)));
        body_type.setText(sharedInfoPreferences.getString("bodyType", getResources().getString(R.string.choice_txt)));
        relationship.setText(sharedInfoPreferences.getString("relationship", getResources().getString(R.string.choice_txt)));
        children.setText(sharedInfoPreferences.getString("children", getResources().getString(R.string.choice_txt)));
        ethnicity.setText(sharedInfoPreferences.getString("yourEthnicity", getResources().getString(R.string.choice_txt)));
        education.setText(sharedInfoPreferences.getString("education", getResources().getString(R.string.choice_txt)));
        about_me.setText(sharedInfoPreferences.getString("aboutMe", getResources().getString(R.string.introduction_none_txt)));
        perfectSmn.setText(sharedInfoPreferences.getString("perfectSmn", getResources().getString(R.string.perfectSmn_none_txt)));
        perfectDate.setText(sharedInfoPreferences.getString("perfectDate", getResources().getString(R.string.perfectDate_none_txt)));
        interests.setText(sharedInfoPreferences.getString("interests", getResources().getString(R.string.choice_txt)));

        height.setText(sharedInfoPreferences.getString("height", getResources().getString(R.string.choice_txt)));

//        interested_in.setText(sharedInfoPreferences.getString("interestedIn", getResources().getString(R.string.choice_txt)));
//        looking_for.setText(sharedInfoPreferences.getString("lookingFor", getResources().getString(R.string.choice_txt)));
    }

}
