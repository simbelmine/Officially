package com.android.formalchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sve on 2/18/15.
 */
public class UserInfoActivity extends Activity {
    private static final int resultCode_interestedIn = 101;
    private static final int resultCode_lookingFor = 102;
    private static final int resultCode_aboutMe = 103;
    private static final int resultCode_relationship = 104;
    private static final int resultCode_bodyType = 105;
    private static final int resultCode_ethnicity = 106;
    private static final int resultCode_interests = 107;
    private static EditText name;
    private static Spinner gender;
    private static EditText age;
    private static EditText location;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_info);

        initialiseViewItems();
        populateInfoFromParse();
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
        interested_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogActivity(resultCode_interestedIn, DialogActivityInterestedIn.class, null);
            }
        });
        looking_for.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogActivity(resultCode_lookingFor, DialogActivityLookingFor.class, null);
            }
        });
        about_me.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String aboutMeTxt = about_me.getText().toString();
                startDialogActivity(resultCode_aboutMe, DialogActivtyAboutMe.class, aboutMeTxt);
            }
        });
        relationship.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogActivity(resultCode_relationship, DialogActivityRelationship.class, null);
            }
        });
        body_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogActivity(resultCode_bodyType, DialogActivityBodyType.class, null);
            }
        });
        ethnicity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogActivity(resultCode_ethnicity, DialogActivityEthnicity.class, null);
            }
        });
        interests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogActivity(resultCode_interests, DialogActivityInterests.class, null);
            }
        });
    }

    private void startDialogActivity(int resultCode, Class className, String extras) {
        Intent intent = new Intent(getApplicationContext(), className);
        if(extras != null) {
            ArrayList<String> extrasList = new ArrayList<>();
            extrasList.add(extras);
            intent.putStringArrayListExtra("aboutMeList_actvty", extrasList);
        }
        startActivityForResult(intent, resultCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(resultCode) {
            case resultCode_interestedIn:
                interestedIn_position = data.getIntExtra("interestedIn_position", 11);
                String value_ii = data.getStringExtra("interestedIn_value");
                interested_in.setText(value_ii);
                break;
            case resultCode_lookingFor:
                lookingFor_position = data.getIntExtra("lookingFor_position", 11);
                String value_lf = data.getStringExtra("lookingFor_value");
                looking_for.setText(value_lf);
                break;
            case resultCode_aboutMe:
                if(!isAboutMeEmpty()) {
                    ArrayList<String> aboutMe_txt = data.getStringArrayListExtra("aboutMeList");
                    about_me.setText(aboutMe_txt.get(0).toString());
                }
                break;
            case resultCode_relationship:
                relationship_position = data.getIntExtra("relationship_position", 11);
                String value_r = data.getStringExtra("relationship_value");
                relationship.setText(value_r);
                break;
            case resultCode_bodyType:
                bodyType_position = data.getIntExtra("bodyType_position", 11);
                String value_bt = data.getStringExtra("bodyType_value");
                body_type.setText(value_bt);
                break;
            case resultCode_ethnicity:
                ethnicity_position = data.getIntExtra("ethnicity_position", 11);
                String value_e = data.getStringExtra("ethnicity_value");
                ethnicity.setText(value_e);
                break;
            case resultCode_interests:
                interests_position = data.getIntExtra("interests_position", 11);
                String value_i = data.getStringExtra("interests_value");
                interests.setText(value_i);
                break;
            default:
                break;
        }
    }

    private boolean isAboutMeEmpty() {
        return getIntent().hasExtra("aboutMeList");
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
        parseObject.put("name", name.getText().toString());
        parseObject.put("gender", gender.getSelectedItemPosition());
        parseObject.put("age", age.getText().toString());
        parseObject.put("interestedIn", interestedIn_position);
        parseObject.put("lookingFor", lookingFor_position);
        parseObject.put("aboutMe", about_me.getText().toString());

        parseObject.put("relationship", relationship_position);
        parseObject.put("bodyType", bodyType_position);
        parseObject.put("ethnicity", ethnicity_position);
        parseObject.put("interests", interests_position);

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

    private void populateInfoFromParse() {
        String currentUser = getCurrentUser();

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("UserInfo");
        parseQuery.whereEqualTo("loginName", currentUser);
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null) {
                    Log.v("formalchat", "User Info SIZE: " + objects.size());
                    for(ParseObject parseObject : objects) {
                        String name_p = parseObject.getString("name");
                        int gender_p = parseObject.getInt("gender");
                        String age_p = parseObject.getString("age");
                        String location_p = parseObject.getString("location");
                        int interestedIn_p = parseObject.getInt("interestedIn");
                        int lookingFor_p = parseObject.getInt("lookingFor");
                        String aboutMe_p = parseObject.getString("aboutMe");
                        int relationship_p = parseObject.getInt("relationship");
                        int bodyType_p = parseObject.getInt("bodyType");
                        int ethnicity_p = parseObject.getInt("ethnicity");
                        int interests_p = parseObject.getInt("interests");

                        name.setText(name_p);
                        gender.setSelection(gender_p);
                        age.setText(age_p);
                        location.setText(location_p);
//                        interested_in.setSelection(interestedIn_p);
//                        looking_for.setSelection(lookingFor_p);
//                        about_me.setText(aboutMe_p);
//                        relationship.setSelection(relationship_p);
//                        body_type.setSelection(bodyType_p);
//                        ethnicity.setSelection(ethnicity_p);
//                        interests.setSelection(interests_p);

                        interested_in.setText(getNameByPosition(getResources().getStringArray(R.array.interested_in_values), interestedIn_p));
                        looking_for.setText(getNameByPosition(getResources().getStringArray(R.array.looking_for_values), lookingFor_p));
                        about_me.setText(aboutMe_p);
                        relationship.setText(getNameByPosition(getResources().getStringArray(R.array.relationship_values), relationship_p));
                        body_type.setText(getNameByPosition(getResources().getStringArray(R.array.body_type_values), bodyType_p));
                        if(ethnicity_p == 0) {
                            ethnicity.setText(getResources().getString(R.string.choice_txt));
                        }
                        else {
                            ethnicity.setText(getNameByPosition(getResources().getStringArray(R.array.ethnicity_values), ethnicity_p));
                        }
                        if(interests_p == 0) {
                            interests.setText(getResources().getString(R.string.choice_txt));
                        }
                        else {
                            interests.setText(getNameByPosition(getResources().getStringArray(R.array.interests_values), interests_p));
                        }
                    }
                }
                else {
                    Log.e("formalchat", "Error: " + e.getMessage());
                }
            }
        });
    }

    private String getNameByPosition(String[] array, int position) {
        return array[position];
    }

    private String getCurrentUser() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        return currentUser.getUsername();
    }

    private void addListenerOnSpinnerItemSelection() {
        gender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.v("formalchat", "It was selected position: " + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initialiseViewItems() {
        name = (EditText) findViewById(R.id.name_edit);
        gender = (Spinner) findViewById(R.id.gender_edit);
        age = (EditText) findViewById(R.id.age_edit);
        location = (EditText) findViewById(R.id.location_edit);
        interested_in = (TextView) findViewById(R.id.interested_in_edit);
        looking_for = (TextView) findViewById(R.id.looking_for_edit);
        about_me = (TextView) findViewById(R.id.about_me_edit);
        relationship = (TextView) findViewById(R.id.relationship_edit);
        body_type = (TextView) findViewById(R.id.body_type_edit);
        ethnicity = (TextView) findViewById(R.id.ethnicity_edit);
        interests = (TextView) findViewById(R.id.interests_edit);
        saveBtn = (Button) findViewById(R.id.save_btn);
    }


}
