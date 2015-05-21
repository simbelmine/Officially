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
    private static final int resultCode_interestedIn = 101;
    private static final int resultCode_lookingFor = 102;
    private static final int resultCode_aboutMe = 103;
    private static final int resultCode_relationship = 104;
    private static final int resultCode_bodyType = 105;
    private static final int resultCode_ethnicity = 106;
    private static final int resultCode_interests = 107;
    private static final String EXTRA_MOTTO = "mottoText";
    private static final String EXTRA_ABOUT_ME = "aboutMeText";
    private static final String PREFS_INFO = "FormalChatUserInfo";
    private static final String PREFS_INFO_LOCAL = "FormalChatUserInfoLocal";
    private static TextView motto;
    private static TextView name;
    private static TextView gender;
    private static TextView age;
    private static TextView location;
    private static TextView interested_in;
    private static TextView looking_for;
    private static TextView about_me;
    private static TextView relationship;
    private static TextView body_type;
    private static TextView ethnicity;
    private static TextView interests;
    private static Button saveBtn;
    private int interestedIn_position;
    private int lookingFor_position;
    private int relationship_position;
    private int bodyType_position;
    private int ethnicity_position;
    private int interests_position;
    private SharedPreferences sharedInfoPreferences_local;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_info);

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
        interested_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogActivity(resultCode_interestedIn, DialogActivityInterestedIn.class, null, null);
            }
        });
        looking_for.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogActivity(resultCode_lookingFor, DialogActivityLookingFor.class, null, null);
            }
        });
        about_me.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String aboutMeTxt = about_me.getText().toString();
                startDialogActivity(resultCode_aboutMe, DialogActivtyMultiText.class, EXTRA_ABOUT_ME, aboutMeTxt);
            }
        });
        relationship.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogActivity(resultCode_relationship, DialogActivityRelationship.class, null, null);
            }
        });
        body_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogActivity(resultCode_bodyType, DialogActivityBodyType.class, null, null);
            }
        });
        ethnicity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogActivity(resultCode_ethnicity, DialogActivityEthnicity.class, null, null);
            }
        });
        interests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogActivity(resultCode_interests, DialogActivityInterests.class, null, null);
            }
        });
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
            case resultCode_interestedIn:
                interestedIn_position = data.getIntExtra("interestedIn_position", 11);
                String value_ii = data.getStringExtra("interestedIn_value");
                interested_in.setText(value_ii);
                editor.putInt("interestedIn", interestedIn_position);
                break;
            case resultCode_lookingFor:
                lookingFor_position = data.getIntExtra("lookingFor_position", 11);
                String value_lf = data.getStringExtra("lookingFor_value");
                looking_for.setText(value_lf);
                editor.putInt("lookingFor", lookingFor_position);
                break;
            case resultCode_aboutMe:
                if(!isExtraEmpty(EXTRA_ABOUT_ME)) {
                    ArrayList<String> aboutMe_txt = data.getStringArrayListExtra(EXTRA_ABOUT_ME);
                    about_me.setText(aboutMe_txt.get(0).toString());
                }
                break;
            case resultCode_relationship:
                relationship_position = data.getIntExtra("relationship_position", 11);
                String value_r = data.getStringExtra("relationship_value");
                relationship.setText(value_r);
                editor.putInt("relationship", relationship_position);
                break;
            case resultCode_bodyType:
                bodyType_position = data.getIntExtra("bodyType_position", 11);
                String value_bt = data.getStringExtra("bodyType_value");
                body_type.setText(value_bt);
                editor.putInt("bodyType", bodyType_position);
                break;
            case resultCode_ethnicity:
                ethnicity_position = data.getIntExtra("ethnicity_position", 11);
                String value_e = data.getStringExtra("ethnicity_value");
                ethnicity.setText(value_e);
                editor.putInt("ethnicity", ethnicity_position);
                break;
            case resultCode_interests:
                interests_position = data.getIntExtra("interests_position", 11);
                String value_i = data.getStringExtra("interests_value");
                interests.setText(value_i);
                editor.putInt("interests", interests_position);
                break;
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
        parseObject.put("aboutMe", about_me.getText().toString());
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
        motto = (TextView) findViewById(R.id.motto);
        name = (TextView) findViewById(R.id.name_edit);
        gender = (TextView) findViewById(R.id.gender_edit);
        age = (TextView) findViewById(R.id.age_edit);
        location = (TextView) findViewById(R.id.location_edit);
        interested_in = (TextView) findViewById(R.id.interested_in_edit);
        looking_for = (TextView) findViewById(R.id.looking_for_edit);
        about_me = (TextView) findViewById(R.id.about_me_edit);
        relationship = (TextView) findViewById(R.id.relationship_edit);
        body_type = (TextView) findViewById(R.id.body_type_edit);
        ethnicity = (TextView) findViewById(R.id.ethnicity_edit);
        interests = (TextView) findViewById(R.id.interests_edit);
        saveBtn = (Button) findViewById(R.id.save_btn);
    }

    private void populateInfoFromExtras() {
        SharedPreferences sharedInfoPreferences = getSharedPreferences(PREFS_INFO, 0);
        motto.setText(sharedInfoPreferences.getString("motto", getResources().getString(R.string.motto)));
        name.setText(sharedInfoPreferences.getString("name", getResources().getString(R.string.change_txt)));
        gender.setText(sharedInfoPreferences.getString("gender", getResources().getString(R.string.choice_txt)));
        age.setText(sharedInfoPreferences.getString("age", getResources().getString(R.string.choice_txt)));
        location.setText(sharedInfoPreferences.getString("location", ""));
        interested_in.setText(sharedInfoPreferences.getString("interestedIn", getResources().getString(R.string.choice_txt)));
        looking_for.setText(sharedInfoPreferences.getString("lookingFor", getResources().getString(R.string.choice_txt)));
        about_me.setText(sharedInfoPreferences.getString("aboutMe", getResources().getString(R.string.change_txt)));
        relationship.setText(sharedInfoPreferences.getString("relationship", getResources().getString(R.string.choice_txt)));
        body_type.setText(sharedInfoPreferences.getString("bodyType", getResources().getString(R.string.choice_txt)));
        ethnicity.setText(sharedInfoPreferences.getString("ethnicity", getResources().getString(R.string.choice_txt)));
        interests.setText(sharedInfoPreferences.getString("interests", getResources().getString(R.string.choice_txt)));
    }

}
