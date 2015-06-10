package com.android.formalchat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sve on 2/4/15.
 */
public class ProfileActivity extends DrawerActivity implements View.OnClickListener {
    private static final String PREFS_NAME = "FormalChatPrefs";
    private static final String PREFS_INFO = "FormalChatUserInfo";
    public static final int NONE = 101;
    private SharedPreferences sharedPreferences;
    private ProfileAddImageDialog addImgWithDialog;
    private DrawerLayout drawerLayout;
    private String profileImgPath;
    private LinearLayout exclamationLayout;
    private ImageButton edit_feb_btn;
    private ParseUser user;
    private ArrayList<String> imagePaths;
    private Activity activity;
    private boolean videoExists;
    private ImageView profilePic;
    private RoundedImageView smallProfilePic;
    private ImageView sexIcon;
    private  boolean isMale;

    private TextView name;
    private TextView gender;
    private TextView age;

    private TextView motto;
    private TextView location;
    private TextView drinking;
    private TextView smoking;
    private TextView religion;
    private TextView height;
    private TextView bodyType;
    private TextView relationship;
    private TextView children;
    private TextView ethnicity;
    private TextView education;
    private TextView aboutMe;
    private TextView perfectSmn;
    private TextView perfectDate;
    private TextView interests;

    //private TextView interestedIn;
    //private TextView lookingFor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.profile, null, false);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.addView(contentView, 0);
        sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        user = ParseUser.getCurrentUser();
        imagePaths = new ArrayList<>();
        activity = this;
        isMale = true;

        setTitle();
        videoExists = isVideoExists();

        init();
        getProfileImgPath();
        if(isNetworkAvailable()) {
            loadBigProfilePicFromParse();
            loadSmallProfilePicFromParse();
        }
        initVideoWarningMessage();
        addViewListeners();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (videoExists) {
            exclamationLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void setTitle() {
        int title_position = getIntent().getIntExtra("title_position", NONE);
        if(title_position == DrawerActivity.PROFILE_ID) {
            getActionBar().setTitle(getResources().getString(R.string.profile));
        }
        else
        if(title_position != NONE) {
            getActionBar().setTitle(getResources().getStringArray(R.array.menu_list)[title_position]);
        }
    }

    private void init() {
        // *** Header
        exclamationLayout = (LinearLayout) findViewById(R.id.exclamation_layout);
        edit_feb_btn = (ImageButton) findViewById(R.id.feb_button);
        profilePic = (ImageView) findViewById(R.id.profile_pic);
        smallProfilePic = (RoundedImageView) findViewById(R.id.small_prof_pic);
        name = (TextView) findViewById(R.id.name_edit);
        sexIcon = (ImageView) findViewById(R.id.sex_icon);
        age = (TextView) findViewById(R.id.age_edit);

        // *** Footer
        motto = (TextView) findViewById(R.id.motto);
        location = (TextView) findViewById(R.id.location_edit);
        drinking = (TextView) findViewById(R.id.drinking_edit);
        smoking = (TextView) findViewById(R.id.smoking_edit);
        religion = (TextView) findViewById(R.id.religion_edit);
        height = (TextView) findViewById(R.id.height_edit);
        bodyType = (TextView) findViewById(R.id.body_type_edit);
        relationship = (TextView) findViewById(R.id.relationship_edit);
        children = (TextView) findViewById(R.id.children_edit);
        ethnicity = (TextView) findViewById(R.id.ethnicity_edit);
        education = (TextView) findViewById(R.id.education_edit);
        aboutMe = (TextView) findViewById(R.id.about_me_edit);
        perfectSmn = (TextView) findViewById(R.id.perfect_smn_edit);
        perfectDate = (TextView) findViewById(R.id.perfect_date_edit);
        interests = (TextView) findViewById(R.id.interests_edit);

//        interestedIn = (TextView) findViewById(R.id.interested_in_edit);
//        lookingFor = (TextView) findViewById(R.id.looking_for_edit);
    }

    private void initVideoWarningMessage() {
        if(!videoExists) {
            exclamationLayout.setVisibility(View.VISIBLE);
        }
        else {
            exclamationLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void setSexIcon(int sex) {
        if(sex == 0) {
            sexIcon.setImageDrawable(getResources().getDrawable(R.drawable.male));
        }
        else if(sex == 1){
            sexIcon.setImageDrawable(getResources().getDrawable(R.drawable.female));
            isMale = false;
        }
    }

    private boolean isVideoExists() {
        if(user.containsKey("video")) {
            return true;
        }
        return false;
    }

    private void addViewListeners() {
        exclamationLayout.setOnClickListener(this);
        edit_feb_btn.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(isNetworkAvailable()) {
            initVideoWarningMessage();
            populateInfoFromParse();
        }
        else {
            // Get from local
            //getImagesFromLocalStorage();
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.exclamation_layout:
                startActivity(VideoRecordActivity.class);
                break;
            case R.id.feb_button:
                PopupMenu popupMenu = new PopupMenu(ProfileActivity.this, edit_feb_btn);
                popupMenu.getMenuInflater().inflate(R.menu.profile_edit_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        return onItemClicked(item);
                    }
                });
                popupMenu.show();
        }
    }

    private boolean onItemClicked(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_profile:
                setUserInfoToExtras();
                startActivity(UserInfoActivity.class);
                return true;
            case R.id.view_gallery:
                startActivity(ProfileGallery.class);
                return true;
            case R.id.add_pic:
                if(isNetworkAvailable()){
                    addImgWithDialog = new ProfileAddImageDialog();
                    FragmentManager manager = getSupportFragmentManager();
                    addImgWithDialog.show(manager, "add_img_dialog");
                }
                else {
                    Toast.makeText(getApplicationContext(), "You are Offline", Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.record_video:
                startActivity(VideoRecordActivity.class);
                return true;
            case R.id.profile_remote:
                startActivity(ProfileRemoteActivity.class);
                return true;
            default:
                return false;
        }
    }

    private void startActivity(Class classToLoad) {
        Intent intent = new Intent(this, classToLoad);
        startActivity(intent);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void loadBigProfilePicFromParse() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.formal_chat";
        if(isProfPicExists(path)) {
            Bitmap myBitmap = BitmapFactory.decodeFile(path + "/blurred_profile.jpg");
            profilePic.setImageBitmap(myBitmap);
        }
        else
        {
            if (user.has("profileImgName")) {
                final String profileImgName = user.get("profileImgName").toString();
                ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("UserImages");
                parseQuery.whereEqualTo("userName", getCurrentUser());
                parseQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> list, ParseException e) {
                        if (list.size() > 0) {
                            for (ParseObject po : list) {
                                String picUrl = ((ParseFile) po.get("photo")).getUrl();
                                String nameFromUrl = getShortImageNameFromUri(picUrl);

                                if (profileImgName.equals(nameFromUrl)) {

                                    ParseFile parseFile = ((ParseFile) po.get("photo"));
                                    parseFile.getDataInBackground(new GetDataCallback() {
                                        @Override
                                        public void done(byte[] bytes, ParseException e) {
                                            if (e == null) {
                                                Bitmap blurredBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                                BlurredImage bm = new BlurredImage();
                                                Bitmap bitmap = bm.getBlurredImage(blurredBitmap, 50);
                                                profilePic.setImageBitmap(bitmap);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    private boolean isProfPicExists(String path) {
        File dir = new File(path);
        File[] files_list = dir.listFiles();

        for(int f = 0; f < files_list.length; f++) {
            if("blurred_profile.jpg".equals(files_list[f].getName())) {
                return true;
            }
        }
        return false;
    }


    private void loadSmallProfilePicFromParse() {
        if(profileImgPath != null) {
            Picasso.with(this).load(profileImgPath).into(smallProfilePic);
        }
    }

    private void getProfileImgPath() {
        // profileImgPath = sharedPreferences.getString("profPic", null);
        if(user.has("profileImg")) {
            ParseFile pic = user.getParseFile("profileImg");
            profileImgPath = pic.getUrl();
        }
    }

    private void populateInfoFromParse() {
        String currentUser = getCurrentUser();

        populateFromUserInfo(currentUser);
        populateFromQuestionary(currentUser);
    }

    private void populateFromQuestionary(String currentUser) {
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("UserQuestionary");
        parseQuery.whereEqualTo("loginName", currentUser);
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> listObjects, ParseException e) {
                if(e == null) {
                    for (ParseObject parseObject : listObjects) {
                        int drinking_p = parseObject.getInt("yourDrinking");
                        int smoking_p = parseObject.getInt("yourSmoking");
                        int religion_p = parseObject.getInt("yourReligion");
                        int ethnicity_p = parseObject.getInt("yourEthnicity");

                        if (drinking_p == 0) {
                            drinking.setText(getResources().getString(R.string.none_txt));
                        } else {
                            drinking.setText(getNameByPosition(getResources().getStringArray(R.array.a_your_drinking), drinking_p));
                        }
                        if (smoking_p == 0) {
                            smoking.setText(getResources().getString(R.string.none_txt));
                        } else {
                            smoking.setText(getNameByPosition(getResources().getStringArray(R.array.a_your_smoking), smoking_p));
                        }
                        if (religion_p == 0) {
                            religion.setText(getResources().getString(R.string.none_txt));
                        } else {
                            religion.setText(getNameByPosition(getResources().getStringArray(R.array.a_your_religion), religion_p));
                        }
                        if (ethnicity_p == 0) {
                            ethnicity.setText(getResources().getString(R.string.none_txt));
                        } else {
                            ethnicity.setText(getNameByPosition(getResources().getStringArray(R.array.ethnicity_values), ethnicity_p));
                        }
                    }
                }
                else {
                    Log.e("formalchat", "Error: " + e.getMessage());
                }
            }
        });
    }

    private void populateFromUserInfo(String currentUser) {
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("UserInfo");
        parseQuery.whereEqualTo("loginName", currentUser);
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (ParseObject parseObject : objects) {
                        String name_p = parseObject.getString("name");
                        int gender_p = parseObject.getInt("gender");
                        String age_p = parseObject.getString("age");

                        String motto_p = parseObject.getString("motto");
                        String location_p = parseObject.getString("location");

                        String height_p = parseObject.getString("height");
                        int bodyType_p = parseObject.getInt("bodyType");
                        int relationship_p = parseObject.getInt("relationship");
                        int children_p = parseObject.getInt("children");
                        int education_p = parseObject.getInt("education");
                        String aboutMe_p = parseObject.getString("aboutMe");
                        String perfect_smn_p = parseObject.getString("perfectSmn");
                        String perfect_date_p = parseObject.getString("perfectDate");
                        int interests_p = parseObject.getInt("interests");

                        name.setText(name_p);
                        age.setText(age_p);

                        if(motto_p == null || "\t".equals(motto_p)) {
                            motto.setText(getResources().getString(R.string.motto));
                        }
                        else {
                            motto.setText(motto_p);
                        }
                        location.setText(location_p);
                        if(height_p == null || height_p.equals("")) {
                            height.setText(getResources().getString(R.string.none_txt));
                        }
                        else {
                            height.setText(height_p);
                        }

                        if(bodyType_p == 0) {
                            bodyType.setText(getResources().getString(R.string.none_txt));
                        }
                        else {
                            bodyType.setText(getNameByPosition(getResources().getStringArray(R.array.body_type_values), bodyType_p));
                        }
                        if(relationship_p == 0) {
                            relationship.setText(getResources().getString(R.string.none_txt));
                        }
                        else {
                            relationship.setText(getNameByPosition(getResources().getStringArray(R.array.relationship_values), relationship_p));
                        }
                        if(children_p == 0) {
                            children.setText(getResources().getString(R.string.none_txt));
                        }
                        else {
                            children.setText(getNameByPosition(getResources().getStringArray(R.array.children_values), relationship_p));
                        }
                        if (education_p == 0) {
                            education.setText(getResources().getString(R.string.none_txt));
                        } else {
                            education.setText(getNameByPosition(getResources().getStringArray(R.array.education_values), education_p));
                        }
                        if(aboutMe_p == null || "\t".equals(aboutMe_p)) {
                            aboutMe.setText(getResources().getString(R.string.introduction_none_txt));
                        }
                        else {
                            aboutMe.setText(aboutMe_p);
                        }
                        if(perfect_smn_p == null || "\t".equals(perfect_smn_p)) {
                            perfectSmn.setText(getResources().getString(R.string.perfectSmn_none_txt));
                        }
                        else {
                            perfectSmn.setText(perfect_smn_p);
                        }
                        if(perfect_date_p == null || "\t".equals(perfect_date_p)) {
                            perfectDate.setText(getResources().getString(R.string.perfectDate_none_txt));
                        }
                        else {
                            perfectDate.setText(perfect_date_p);
                        }
                        if (interests_p == 0) {
                            interests.setText(getResources().getString(R.string.none_txt));
                        } else {
                            interests.setText(getNameByPosition(getResources().getStringArray(R.array.interests_values), interests_p));
                        }

                        //setUserInfoToExtras();
                        setSexIcon(gender_p);
                    }
                } else {
                    Log.e("formalchat", "Error: " + e.getMessage());
                }
            }
        });
    }

    private String getCurrentUser() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        return currentUser.getUsername();
    }

    private String getNameByPosition(String[] array, int position) {
        return array[position];
    }

    public String getShortImageNameFromUri(String url) {
        return url.substring(url.lastIndexOf("-")+1);
    }

    private void setUserInfoToExtras() {
        SharedPreferences sharedInfoPreferences = getSharedPreferences(PREFS_INFO, 0);
        SharedPreferences.Editor editor = sharedInfoPreferences.edit();

        editor.putString("name", getRightText(name.getText().toString()));
        if(isMale) {
            editor.putString("gender", getNameByPosition(getResources().getStringArray(R.array.gender_values), 0));
        }
        else {
            editor.putString("gender", getNameByPosition(getResources().getStringArray(R.array.gender_values), 1));
        }
        editor.putString("age", getRightText(age.getText().toString()));


        editor.putString("motto", getRightText(motto.getText().toString()));
        editor.putString("location", getRightText(location.getText().toString()));
        editor.putString("yourDrinking", getRightText(drinking.getText().toString()));
        editor.putString("yourSmoking", getRightText(smoking.getText().toString()));
        editor.putString("yourReligion", getRightText(religion.getText().toString()));
        editor.putString("height", getRightText(height.getText().toString()));
        editor.putString("bodyType", getRightText(bodyType.getText().toString()));
        editor.putString("relationship", getRightText(relationship.getText().toString()));
        editor.putString("children", getRightText(children.getText().toString()));
        editor.putString("yourEthnicity", getRightText(ethnicity.getText().toString()));
        editor.putString("education", getRightText(education.getText().toString()));
        editor.putString("aboutMe", getRightText(aboutMe.getText().toString()));
        editor.putString("perfectSmn", getRightText(perfectSmn.getText().toString()));
        editor.putString("perfectDate", getRightText(perfectDate.getText().toString()));
        editor.putString("interests", getRightText(interests.getText().toString()));

//        editor.putString("interestedIn", getRightText(interestedIn.getText().toString()));
//        editor.putString("lookingFor", getRightText(lookingFor.getText().toString()));
        editor.commit();
    }

    private String getRightText(String s) {
        String none_txt = getResources().getString(R.string.none_txt);
        if(none_txt.equals(s)) {
            return null;
        }
        return s;
    }
}
