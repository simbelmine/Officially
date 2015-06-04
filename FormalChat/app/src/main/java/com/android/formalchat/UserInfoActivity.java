package com.android.formalchat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Sve on 2/18/15.
 */
public class UserInfoActivity extends Activity {
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

    private static Button saveBtn;
//    private static TextView interested_in;
//    private static TextView looking_for;
//    private int interestedIn_position;
//    private int lookingFor_position;
    private SharedPreferences sharedInfoPreferences_local;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_info_new);

        sharedInfoPreferences_local = getSharedPreferences(PREFS_INFO_LOCAL, 0);
        editor = sharedInfoPreferences_local.edit();

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

    private void setOnClickListeners() {

        // *** Multi Choice *** //
        motto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mottoText = motto.getText().toString();
                startDialogActivity(resultCode_motto, DialogActivtyMultiText.class, EXTRA_MOTTO, mottoText);
            }
        });
        drinking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogActivity(resultCode_drinking, DialogActivityRelationship.class, null, null);
            }
        });
        smoking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogActivity(resultCode_smoking, DialogActivityRelationship.class, null, null);
            }
        });
        religion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogActivity(resultCode_religion, DialogActivityRelationship.class, null, null);
            }
        });
        height.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogActivity(resultCode_height, DialogActivityRelationship.class, null, null);
            }
        });
        body_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogActivity(resultCode_bodyType, DialogActivityBodyType.class, null, null);
            }
        });
        relationship.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogActivity(resultCode_relationship, DialogActivityRelationship.class, null, null);
            }
        });
        children.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogActivity(resultCode_children, DialogActivityRelationship.class, null, null);
            }
        });
        ethnicity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogActivity(resultCode_ethnicity, DialogActivityEthnicity.class, null, null);
            }
        });
        education.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogActivity(resultCode_education, DialogActivityEthnicity.class, null, null);
            }
        });
        about_me.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String aboutMeTxt = about_me.getText().toString();
                startDialogActivity(resultCode_aboutMe, DialogActivtyMultiText.class, EXTRA_ABOUT_ME, aboutMeTxt);
            }
        });
        perfectSmn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String perfectSmnTxt = perfectSmn.getText().toString();
                startDialogActivity(resultCode_perfectSmn, DialogActivtyMultiText.class, EXTRA_PERFECT_SMN, perfectSmnTxt);
            }
        });
        perfectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String perfectDateTxt = perfectDate.getText().toString();
                startDialogActivity(resultCode_perfectDate, DialogActivtyMultiText.class, EXTRA_PERFECT_DATE, perfectDateTxt);
            }
        });
        interests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogActivity(resultCode_interests, DialogActivityInterests.class, null, null);
            }
        });


//        interested_in.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startDialogActivity(resultCode_interestedIn, DialogActivityInterestedIn.class, null, null);
//            }
//        });
//        looking_for.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startDialogActivity(resultCode_lookingFor, DialogActivityLookingFor.class, null, null);
//            }
//        });
    }

    private void startDialogActivity(int resultCode, Class className, String extraName, String extraText) {
        Intent intent = new Intent(getApplicationContext(), className);
        if(extraText != null) {
            ArrayList<String> extrasList = new ArrayList<>();
            extrasList.add(extraText);
            intent.putStringArrayListExtra(extraName, extrasList);
        }
        startActivityForResult(intent, resultCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(resultCode) {
            case resultCode_motto:
                if(!isExtraEmpty(EXTRA_MOTTO)) {
                    ArrayList<String> motto_txt = data.getStringArrayListExtra(EXTRA_MOTTO);
                    motto.setText(motto_txt.get(0).toString());
                }
                break;
            case resultCode_drinking:
                drinking_position = data.getIntExtra("drinking_position", 11);
                String value_d = data.getStringExtra("drinking_value");
                drinking.setText(value_d);
                editor.putInt("drinking", drinking_position);
                break;
            case resultCode_smoking:
                smoking_position = data.getIntExtra("smoking_position", 11);
                String value_s = data.getStringExtra("smoking_value");
                smoking.setText(value_s);
                editor.putInt("smoking", smoking_position);
                break;
            case resultCode_religion:
                religion_position = data.getIntExtra("religion_position", 11);
                String value_re = data.getStringExtra("religion_value");
                religion.setText(value_re);
                editor.putInt("religion", religion_position);
                break;
            case resultCode_bodyType:
                bodyType_position = data.getIntExtra("bodyType_position", 11);
                String value_bt = data.getStringExtra("bodyType_value");
                body_type.setText(value_bt);
                editor.putInt("bodyType", bodyType_position);
                break;
            case resultCode_relationship:
                relationship_position = data.getIntExtra("relationship_position", 11);
                String value_r = data.getStringExtra("relationship_value");
                relationship.setText(value_r);
                editor.putInt("relationship", relationship_position);
                break;
            case resultCode_children:
                children_position = data.getIntExtra("children_position", 11);
                String value_c = data.getStringExtra("children_value");
                children.setText(value_c);
                editor.putInt("children", children_position);
                break;
            case resultCode_ethnicity:
                ethnicity_position = data.getIntExtra("ethnicity_position", 11);
                String value_e = data.getStringExtra("ethnicity_value");
                ethnicity.setText(value_e);
                editor.putInt("ethnicity", ethnicity_position);
                break;
            case resultCode_education:
                education_position = data.getIntExtra("education_position", 11);
                String value_ed = data.getStringExtra("education_value");
                education.setText(value_ed);
                editor.putInt("ethnicity", education_position);
                break;
            case resultCode_aboutMe:
                if(!isExtraEmpty(EXTRA_ABOUT_ME)) {
                    ArrayList<String> aboutMe_txt = data.getStringArrayListExtra(EXTRA_ABOUT_ME);
                    about_me.setText(aboutMe_txt.get(0).toString());
                }
                break;
            case resultCode_perfectSmn:
                if(!isExtraEmpty(EXTRA_PERFECT_SMN)) {
                    ArrayList<String> perfectSmn_txt = data.getStringArrayListExtra(EXTRA_PERFECT_SMN);
                    perfectSmn.setText(perfectSmn_txt.get(0).toString());
                }
                break;
            case resultCode_perfectDate:
                if(!isExtraEmpty(EXTRA_PERFECT_DATE)) {
                    ArrayList<String> perfectSmn_txt = data.getStringArrayListExtra(EXTRA_PERFECT_DATE);
                    perfectDate.setText(perfectSmn_txt.get(0).toString());
                }
                break;
            case resultCode_interests:
                interests_position = data.getIntExtra("interests_position", 11);
                String value_i = data.getStringExtra("interests_value");
                interests.setText(value_i);
                editor.putInt("interests", interests_position);
                break;

//            case resultCode_interestedIn:
//                interestedIn_position = data.getIntExtra("interestedIn_position", 11);
//                String value_ii = data.getStringExtra("interestedIn_value");
//                interested_in.setText(value_ii);
//                editor.putInt("interestedIn", interestedIn_position);
//                break;
//            case resultCode_lookingFor:
//                lookingFor_position = data.getIntExtra("lookingFor_position", 11);
//                String value_lf = data.getStringExtra("lookingFor_value");
//                looking_for.setText(value_lf);
//                editor.putInt("lookingFor", lookingFor_position);
//                break;
            default:
                break;
        }
        editor.commit();
    }

    private boolean isExtraEmpty(String extra) {
        return getIntent().hasExtra(extra);
    }

    private void goToProfileActivity() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    private void saveChangesToParse() {
        ParseUser parseUser = ParseUser.getCurrentUser();
        final String userName = parseUser.getUsername();

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("UserInfo");
        parseQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if(e == null) {
                    Log.e("formalchat", "EXISTS...." + parseObject.getString("name"));

                    saveToExistingUserInfo(parseObject, userName);
                }
                else {
                    Log.e("formalchat", "DOESN/'T EXISTS");
                }
            }
        });
    }

    private void saveToExistingUserInfo(ParseObject parseObject, String userName) {
        Map<String, ?> sharedPrefsMap = sharedInfoPreferences_local.getAll();

        for(Map.Entry<String, ?> entry : sharedPrefsMap.entrySet()) {
            Log.v("formalchat", entry.getKey() + " : " + entry.getValue());
            parseObject.put(entry.getKey(), entry.getValue());
        }

        parseObject.put("motto", motto.getText().toString());
        if(getResources().getString(R.string.change_txt).equals(about_me.getText().toString())) {
            parseObject.put("aboutMe", "");
        }
        else {
            parseObject.put("aboutMe", about_me.getText().toString());
        }
        parseObject.put("name", name.getText().toString());
        parseObject.put("age", age.getText().toString());

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
        SharedPreferences sharedInfoPreferences = getSharedPreferences(PREFS_INFO, 0);
        name.setText(sharedInfoPreferences.getString("name", getResources().getString(R.string.change_txt)));
        age.setText(sharedInfoPreferences.getString("age", getResources().getString(R.string.choice_txt)));

        motto.setText(sharedInfoPreferences.getString("motto", getResources().getString(R.string.motto)));
        drinking.setText(sharedInfoPreferences.getString("drinking", getResources().getString(R.string.choice_txt)));
        smoking.setText(sharedInfoPreferences.getString("smoking", getResources().getString(R.string.choice_txt)));
        religion.setText(sharedInfoPreferences.getString("religion", getResources().getString(R.string.choice_txt)));
        body_type.setText(sharedInfoPreferences.getString("bodyType", getResources().getString(R.string.choice_txt)));
        relationship.setText(sharedInfoPreferences.getString("relationship", getResources().getString(R.string.choice_txt)));
        children.setText(sharedInfoPreferences.getString("children", getResources().getString(R.string.choice_txt)));
        ethnicity.setText(sharedInfoPreferences.getString("ethnicity", getResources().getString(R.string.choice_txt)));
        education.setText(sharedInfoPreferences.getString("education", getResources().getString(R.string.choice_txt)));
        about_me.setText(sharedInfoPreferences.getString("aboutMe", getResources().getString(R.string.introduction_none_txt)));
        perfectSmn.setText(sharedInfoPreferences.getString("perfectSmn", getResources().getString(R.string.perfectSmn_none_txt)));
        perfectDate.setText(sharedInfoPreferences.getString("perfectDate", getResources().getString(R.string.perfectDate_none_txt)));
        interests.setText(sharedInfoPreferences.getString("interests", getResources().getString(R.string.choice_txt)));

//        interested_in.setText(sharedInfoPreferences.getString("interestedIn", getResources().getString(R.string.choice_txt)));
//        looking_for.setText(sharedInfoPreferences.getString("lookingFor", getResources().getString(R.string.choice_txt)));
    }

}
