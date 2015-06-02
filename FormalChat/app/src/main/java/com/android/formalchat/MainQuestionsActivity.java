package com.android.formalchat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sve on 2/19/15.
 */
public class MainQuestionsActivity extends Activity {
    public static final String PREFS_NAME = "FormalChatPrefs";
    private static final String EXTRA_ABOUT_ME = "aboutMeText";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static final int resultCode_interestedIn = 101;
    private static final int resultCode_lookingFor = 102;
    private static final int resultCode_aboutMe = 103;
    private static EditText name;
    private static CheckBox gender_male;
    private static CheckBox gender_female;
    private static EditText age;
    private static EditText location;
    //private static Spinner interested_in;
    private static TextView interested_in;
    private int interestedIn_position;
    //    private static Spinner looking_for;
    private TextView looking_for;
    private int lookingFor_position;
    private static TextView about_me;
    private static Button done_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_questions_layout);
        sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        editor = sharedPreferences.edit();

        findAllById();
        Log.v("formalchat", "Location: " + getCurrentLocation());
        location.setText(getCurrentLocation());
        setOnClickListeners();
        onDoneBtnPressed();
    }

    private void setOnClickListeners() {
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

        gender_male.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    gender_male.setChecked(true);
                    gender_female.setChecked(false);
                }
            }
        });
        gender_female.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    gender_male.setChecked(false);
                    gender_female.setChecked(true);
                }
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
                    ArrayList<String> aboutMe_txt = data.getStringArrayListExtra(EXTRA_ABOUT_ME);
                    about_me.setText(aboutMe_txt.get(0).toString());
                }
            default:
                break;
        }
    }

    private boolean isAboutMeEmpty() {
        return getIntent().hasExtra(EXTRA_ABOUT_ME);
    }

    private void onDoneBtnPressed() {
        done_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(verifyVariables()) {
                    setDoneQuestions();
                    saveVariablesToParse();
                    startQuestionaryActivity();
                }
                else {
                    Toast.makeText(getApplicationContext(), "You missed something. Check again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean verifyVariables() {
        if(gender_male.isChecked() || gender_female.isChecked()) {
            return true;
        }
        return false;
    }

    private void startQuestionaryActivity() {
        Intent intent = new Intent(this, QuestionaryActivity.class);
        startActivity(intent);
    }

    private void saveVariablesToParse() {
        ParseUser parseUser = ParseUser.getCurrentUser();
        final String userName = parseUser.getUsername();

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("UserInfo");
        parseQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    saveToExistingUserInfo(parseObject, userName);
                } else {
                    Log.e("formalchat", "DOESN/'T EXISTS");

                    saveToNonExistingUserInfo(userName);
                }
            }
        });
    }

    private void saveToNonExistingUserInfo(String userName) {
        UserInfo userInfo = new UserInfo();
        userInfo.setLoginName(userName);

        userInfo.setName(name.getText().toString());
        userInfo.setGender(getCorrectGender());
        userInfo.setAge(age.getText().toString());
        userInfo.setLocation(location.getText().toString());
        //userInfo.setInterestedIn(interested_in.getSelectedItemPosition());
        userInfo.setInterestedIn(interestedIn_position);
        //userInfo.setLookingFor(looking_for.getSelectedItemPosition());
        userInfo.setLookingFor(lookingFor_position);
        if(!getResources().getString(R.string.multi_txt).equals(about_me.getText().toString())) {
            userInfo.setAboutMe(about_me.getText().toString());
        }

        userInfo.saveInBackground(new SaveCallback() {
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

    private int getCorrectGender() {
        if(gender_male.isChecked()) {
            return 0;
        }
        else if(gender_female.isChecked()) {
            return 1;
        }
        return 11;
    }

    private void saveToExistingUserInfo(ParseObject parseObject, String userName) {
        parseObject.put("loginName", userName);
        parseObject.put("name", name.getText().toString());
        parseObject.put("gender", getCorrectGender());
        parseObject.put("age", age.getText().toString());
        parseObject.put("location", location.getText().toString());
        // parseObject.put("interestedIn", interested_in.getSelectedItemPosition());
        parseObject.put("interestedIn", interestedIn_position);
        //parseObject.put("lookingFor", looking_for.getSelectedItemPosition());
        parseObject.put("lookingFor", lookingFor_position);
        if(!getResources().getString(R.string.multi_txt).equals(about_me.getText().toString())) {
            parseObject.put("aboutMe", about_me.getText().toString());
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

    private String getCurrentLocation() {
        if(Geocoder.isPresent()) {
            try {
                Location lastLoc = getLastLocation();
                Log.v("formalchat", "Last location: " + lastLoc.getLatitude() + "  " + lastLoc.getLongitude());
                Geocoder gcd = new Geocoder(this);
                List<Address> addresses = gcd.getFromLocation(lastLoc.getLatitude(), lastLoc.getLongitude(), 1);

                if (addresses.size() > 0) {
                    Address address = addresses.get(0);
                    String message = String.format("%s, %s",
                            address.getCountryCode(), address.getLocality());
                    Log.v("formalchat", "***********************  " + message );
                    return message;
                }
                else {
                    return "Waiting for location...";
                }
            } catch (IOException e) {
                Log.e("formalchat", "Error message: " + e.getMessage());
            }
        }
        else {
            Toast.makeText(this, "Something went wrong. Check your Internet connection or GPS!", Toast.LENGTH_LONG).show();
        }
        return null;
    }


    private Location getLastLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if ( 0 < GPSLocationTime - NetLocationTime ) {
            return locationGPS;
        }
        else {
            return locationNet;
        }

    }

    private String getCurrentLocationByZipCode() {
        Geocoder geocoder = new Geocoder(this);
        String zipCode = location.getText().toString();
        try {
            List<Address> addresses = geocoder.getFromLocationName(zipCode, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String message = String.format("%s, %s",
                        address.getCountryCode(), address.getLocality());
                //Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                return message;
            }
            else {
                // Display appropriate message when Geocoder services are not available
                Toast.makeText(this, "Unable to geocode zipcode", Toast.LENGTH_LONG).show();
            }
        }
        catch(IOException ex) {

        }
        return " ";
    }

    private void findAllById() {
        name = (EditText) findViewById(R.id.name_edit);
        //gender = (Spinner) findViewById(R.id.gender_edit);
        gender_male = (CheckBox) findViewById(R.id.male_check);
        gender_female = (CheckBox) findViewById(R.id.female_check);
        age = (EditText) findViewById(R.id.age_edit);
        location = (EditText) findViewById(R.id.location_edit);
        //interested_in = (Spinner) findViewById(R.id.interested_in_edit);
        interested_in = (TextView) findViewById(R.id.interested_in_edit);
        //looking_for = (Spinner) findViewById(R.id.looking_for_edit);
        looking_for = (TextView) findViewById((R.id.looking_for_edit));
        about_me = (TextView) findViewById(R.id.about_me_edit);
        done_btn = (Button) findViewById(R.id.done_btn);
    }

    private void setDoneQuestions() {
        editor.putBoolean("questions_done", true);
        editor.commit();
    }
}
