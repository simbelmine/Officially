package com.android.formalchat;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.formalchat.questionary.QuestionaryActivity;
import com.parse.GetCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Sve on 2/19/15.
 */
public class MainQuestionsActivity extends BaseActivity {
    public static final String PREFS_NAME = "FormalChatPrefs";
    private static final String EXTRA_ABOUT_ME = "aboutMeText";
    private static final int INIT_YEAR = 1980;
    private static final int INIT_MONTH = 0;
    private static final int INIT_DAY = 1;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private EditText name;
    private Button male_btn;
    private Button female_btn;
    private TextView age;
    private EditText location;
    private Button done_btn;
    private Boolean isMaleClicked;
    private ImageView name_check;
    private ImageView age_check;
    private DatePickerDialog datePickerDialog;
    private SimpleDateFormat dateFormatter;
    private TextView userEmail;
    private ParseUser parseUser;
    private PermissionsHelper permissionsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_questions_layout);
        sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        editor = sharedPreferences.edit();
        permissionsHelper = new PermissionsHelper(MainQuestionsActivity.this);

        initToolbar();
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.material_gray)));
        findAllById();
        init();
        initGenderButtons();
        initDatePickerDialog();
        Log.v("formalchat", "Location: " + getCurrentLocation());
        setOnClickListeners();
        onDoneBtnPressed();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
    }

    private void setOnClickListeners() {
        male_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMaleClicked = true;
                initGenderButtons();
            }
        });

        female_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMaleClicked = false;
                initGenderButtons();
            }
        });

        name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(name.getWindowToken(), 0);
                    updateFields();
                    return true;
                }
                return false;
            }
        });
        name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    name_check.setVisibility(View.INVISIBLE);
                }
            }
        });

        age.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                age.clearFocus();
                age_check.setVisibility(View.INVISIBLE);
                datePickerDialog.show();
            }
        });

    }

    private void onDoneBtnPressed() {
        done_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyVariables()) {
                    if(isNetworkAvailable()) {
                        saveVariablesToParse();
                        startQuestionaryActivity();
                    }
                    else {
                        getSnackbar(MainQuestionsActivity.this, R.string.no_network, R.color.alert_red).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "You missed something. Check again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateFields() {
        name.clearFocus();
        age.clearFocus();
        done_btn.requestFocusFromTouch();
    }


    private boolean verifyVariables() {
        if(!TextUtils.isEmpty(name.getText()) && !TextUtils.isEmpty(age.getText())) {
            return true;
        }
        updateChecks();
        return false;
    }

    private void updateChecks() {
        if(TextUtils.isEmpty(name.getText())) {
            name_check.setVisibility(View.VISIBLE);
            name_check.setImageDrawable(getResources().getDrawable(R.drawable.wrong));
        }
        if(TextUtils.isEmpty(age.getText())) {
            age_check.setVisibility(View.VISIBLE);
            age_check.setImageDrawable(getResources().getDrawable(R.drawable.wrong));
        }
    }

    private void startQuestionaryActivity() {
        Intent intent = new Intent(this, QuestionaryActivity.class);
        startActivity(intent);
    }

    private void saveVariablesToParse() {
        final String userName = parseUser.getUsername();
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("UserInfo");
        parseQuery.whereContains("loginName", userName);
        parseQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, com.parse.ParseException e) {
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
        userInfo.setBirthday(age.getText().toString());
        userInfo.setAge(getAge());
        userInfo.setLocation(location.getText().toString());

        userInfo.saveInBackground(new SaveCallback() {
            @Override
            public void done(com.parse.ParseException e) {
                if (e == null) {
                    Log.e("formalchat", "Questionary was saved Successfully !");
                    setDoneQuestions();
                } else {
                    Log.e("formalchat", "Error saving: " + e.getMessage());
                }
            }
        });
    }

    private void saveToExistingUserInfo(ParseObject parseObject, String userName) {
        parseObject.put("loginName", userName);
        parseObject.put("name", name.getText().toString());
        parseObject.put("gender", getCorrectGender());
        parseObject.put("birthday", age.getText().toString());
        parseObject.put("age", getAge());
        parseObject.put("location", location.getText().toString());

        parseObject.saveInBackground(new SaveCallback() {

            @Override
            public void done(com.parse.ParseException e) {
                if (e == null) {
                    Log.e("formalchat", "Questionary was saved Successfully !");
                    setDoneQuestions();
                } else {
                    Log.e("formalchat", "Error saving: " + e.getMessage());
                }
            }
        });
    }

    private int getCorrectGender() {
        if(isMaleClicked)
            return 0;
        else
            return 1;
    }

    private int getAge() {
        Calendar currentDate = Calendar.getInstance();
        int currentYear = currentDate.get(Calendar.YEAR);
        int birthDayYear = getBirthDayYear();

        return currentYear - birthDayYear;
    }

    private int getBirthDayYear() {
        Calendar calendar = Calendar.getInstance();
        Date date;
        try {
            date = dateFormatter.parse(age.getText().toString());
            calendar.setTime(date);
            return calendar.get(Calendar.YEAR);
        }
        catch (ParseException ex) {
            Log.v("formalchat", ex.getMessage());
        }
        return 0;
    }


    private String getUserEmail() {
        if(parseUser.containsKey("username")) {
            return parseUser.get("username").toString();
        }
        return null;
    }

    private String getCurrentLocation() {
        if(Geocoder.isPresent()) {
            Log.v(ApplicationOfficially.TAG, "is Geocodet present ? => " + Geocoder.isPresent());
            try {
                Location lastLoc = getLastLocation();

                if(lastLoc != null) {
                    Log.v("formalchat", "Last location: " + lastLoc.getLatitude() + "  " + lastLoc.getLongitude());
                    Geocoder gcd = new Geocoder(this);
                    List<Address> addresses = gcd.getFromLocation(lastLoc.getLatitude(), lastLoc.getLongitude(), 1);

                    if (addresses.size() > 0) {
                        Address address = addresses.get(0);
                        String message = String.format("%s, %s",
                                address.getCountryCode(), address.getLocality());
                        Log.v("formalchat", "***********************  " + message);
                        return message;
                    }
                    else {
                        return "Waiting for location...";
                    }
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
        if(permissionsHelper.isAllPermissionsGranted) {
            try {
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                long GPSLocationTime = 0;
                if (null != locationGPS) {
                    GPSLocationTime = locationGPS.getTime();
                }

                long NetLocationTime = 0;

                if (null != locationNet) {
                    NetLocationTime = locationNet.getTime();
                }

                if (0 < GPSLocationTime - NetLocationTime) {
                    return locationGPS;
                } else {
                    return locationNet;
                }
            }
            catch (SecurityException ex) {
                Log.e(ApplicationOfficially.TAG, "Security Exeption : " + ex.getMessage());
            }
        }
        else {
            permissionsHelper.checkForPermissions();
        }

        return null;
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
        male_btn = (Button) findViewById(R.id.male_btn);
        female_btn = (Button) findViewById(R.id.female_btn);
        age = (TextView) findViewById(R.id.age_edit);
        location = (EditText) findViewById(R.id.location_edit);
        name_check = (ImageView) findViewById(R.id.name_check);
        age_check = (ImageView) findViewById(R.id.age_check);
        done_btn = (Button) findViewById(R.id.done_btn);
        userEmail = (TextView) findViewById(R.id.user_email);
    }

    private void init() {
        parseUser = ParseUser.getCurrentUser();
        isMaleClicked = true;
        name.setImeOptions(EditorInfo.IME_ACTION_DONE);
        location.setText(getCurrentLocation());
        userEmail.setText(getUserEmail());
    }

    private void initGenderButtons() {
        if(isMaleClicked) {
            male_btn.setBackgroundResource(R.drawable.rounded_btns_gray_active);
            male_btn.setTextColor(getResources().getColor(R.color.white));
            female_btn.setBackgroundResource(R.drawable.rounded_btns_gray);
            female_btn.setTextColor(getResources().getColor(R.color.action_bar));
        }
        else {
            male_btn.setBackgroundResource(R.drawable.rounded_btns_gray);
            male_btn.setTextColor(getResources().getColor(R.color.action_bar));
            female_btn.setBackgroundResource(R.drawable.rounded_btns_gray_active);
            female_btn.setTextColor(getResources().getColor(R.color.white));
        }
    }

    private void setDoneQuestions() {
        parseUser.put("doneMainQuestions", true);
        parseUser.saveInBackground();
    }

    private void initDatePickerDialog() {
        Calendar initDate = Calendar.getInstance();
        initDate.set(INIT_YEAR, INIT_MONTH, INIT_DAY);
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        datePickerDialog = new DatePickerDialog(this, new OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                if(verifyYear(year)) {
                    age.setText(dateFormatter.format(newDate.getTime()));
                }
                else {
                    age.setText("");
                }
            }
        }, initDate.get(Calendar.YEAR), initDate.get(Calendar.MONTH), initDate.get(Calendar.DAY_OF_MONTH));
        if(isBeforeVersion21()) {
            datePickerDialog.setTitle(dateFormatter.format(initDate.getTime()));
        }
    }

    private boolean verifyYear(int year) {
        if(year >= Calendar.getInstance().get(Calendar.YEAR)) {
            showAlertDialog();
            return false;
        }
        return true;
    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);
        alertDialogBuilder.setTitle(getResources().getString(R.string.alert_title));
        alertDialogBuilder
                .setMessage(getResources().getString(R.string.alert_text))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.alert_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                        return;
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private boolean isBeforeVersion21() {
        if(Integer.valueOf(Build.VERSION.SDK_INT) < 21)
            return true;
        return false;
    }
}
