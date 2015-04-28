package com.android.formalchat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

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
    private ProfilePagerAdapter profilePagerAdapter;
    private ViewPager viewPager;
    private ProfileAddImageDialog addImgWithDialog;
    private DrawerLayout drawerLayout;
    private String profileImgPath;
    private LinearLayout exclamationLayout;
    private ImageButton edit_feb_btn;
    private ParseUser user;
    private ArrayList<String> imagePaths;
    private Activity activity;
    private boolean videoExists;

    private TextView motto;
    private TextView name;
    private TextView gender;
    private TextView age;
    private TextView location;
    private TextView interestedIn;
    private TextView lookingFor;
    private TextView aboutMe;
    private TextView relationship;
    private TextView bodyType;
    private TextView ethnicity;
    private TextView interests;

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

        setTitle();
        videoExists = isVideoExists();
        viewPager = (ViewPager) findViewById(R.id.pager_profile);

        init();
        if(isNetworkAvailable()) {
            loadImagesFromParseRemote();
        }
        initVideoWarningMessage();
        addViewListeners();
        getProfileImgPath();
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
        viewPager = (ViewPager) findViewById(R.id.pager_profile);
        exclamationLayout = (LinearLayout) findViewById(R.id.exclamation_layout);
        edit_feb_btn = (ImageButton) findViewById(R.id.feb_button);
        // *** Footer
        motto = (TextView) findViewById(R.id.motto);
        name = (TextView) findViewById(R.id.name_edit);
        gender = (TextView) findViewById(R.id.gender_edit);
        age = (TextView) findViewById(R.id.age_edit);
        location = (TextView) findViewById(R.id.location_edit);
        interestedIn = (TextView) findViewById(R.id.interested_in_edit);
        lookingFor = (TextView) findViewById(R.id.looking_for_edit);
        aboutMe = (TextView) findViewById(R.id.about_me_edit);
        relationship = (TextView) findViewById(R.id.relationship_edit);
        bodyType = (TextView) findViewById(R.id.body_type_edit);
        ethnicity = (TextView) findViewById(R.id.ethnicity_edit);
        interests = (TextView) findViewById(R.id.interests_edit);
    }

    private void initVideoWarningMessage() {
        if(!videoExists) {
            exclamationLayout.setVisibility(View.VISIBLE);
        }
        else {
            exclamationLayout.setVisibility(View.INVISIBLE);
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
        if(isNeededToRefresh()) {
            setNewSharedPrefs();
            //recreate();
            initProfilePagerdAdapter();
        }

        if(isNetworkAvailable()) {
            initVideoWarningMessage();
            populateInfoFromParse();
        }
        else {
            // Get from local
            //getImagesFromLocalStorage();
        }
    }

    private void addVideoToPaths() {
        String videoPath = user.getParseFile("video").getUrl();
        imagePaths = setPathInFront(imagePaths, videoPath);
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
                startActivity(UserInfoActivity.class);
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
            default:
                return false;
        }
    }

    private void startActivity(Class classToLoad) {
        Intent intent = new Intent(this, classToLoad);
        startActivity(intent);
    }


    private void setNewSharedPrefs() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("refresh", false);
        editor.commit();
    }

    private boolean isNeededToRefresh() {
        return sharedPreferences.getBoolean("refresh", false);
    }

    private void getImagesFromLocalStorage() {
        File folder = new File(Environment.getExternalStorageDirectory() + "/.formal_chat");
        File[] dirImages = folder.listFiles();
        String path;

        if(dirImages.length != 0) {
            for(int counter = 0; counter < dirImages.length; counter++) {
                path = "file:" + dirImages[counter].getPath().toString();
                imagePaths.add(path);
            }

            profilePagerAdapter = new ProfilePagerAdapter(activity, getApplicationContext(), imagePaths);
            viewPager.setAdapter(profilePagerAdapter);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void loadImagesFromParseRemote() {
        String currentUser = getUserName();

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("UserImages");
        parseQuery.whereEqualTo("userName", currentUser);
        parseQuery.orderByAscending("createdAt");
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> imagesList, ParseException e) {
                if (e == null) {
                    for (ParseObject po : imagesList) {
                        imagePaths.add(((ParseFile)po.get("photo")).getUrl());
                    }

                    if(profileImgPath != null) {
                        imagePaths = setPathInFront(imagePaths, profileImgPath);
                    }
                    if(videoExists) {
                        addVideoToPaths();
                    }

                    initProfilePagerdAdapter();

                } else {
                    Log.d("formalchat", "Error: " + e.getMessage());
                }
            }
        });
    }

    private void initProfilePagerdAdapter() {
        if (profilePagerAdapter != null) {
            profilePagerAdapter.updateImages(imagePaths);
        } else {
            profilePagerAdapter = new ProfilePagerAdapter(activity, getApplicationContext(), imagePaths);
            viewPager.setAdapter(profilePagerAdapter);
        }
    }

    private ArrayList<String> setPathInFront(ArrayList<String> imagePaths, String path) {
        ArrayList<String> imagePathsNew = new ArrayList<>();
        imagePathsNew.add(path);
        imagePathsNew.addAll(getNewPathList(imagePaths, path));

        return imagePathsNew;
    }

    private ArrayList<String> getNewPathList(ArrayList<String> imagePaths, String path) {
        for(int pathIdx = 0; pathIdx < imagePaths.size(); pathIdx++) {
            if(path.equals(imagePaths.get(pathIdx))) {
                imagePaths.remove(pathIdx);
            }
        }
        return imagePaths;
    }

    private void getProfileImgPath() {
        profileImgPath = sharedPreferences.getString("profPic", null);
    }

    private String getUserName() {
        ParseUser parseUser = ParseUser.getCurrentUser();
        return parseUser.getUsername();
    }

    public void onImageUploaded() {
        loadImagesFromParseRemote();
        profilePagerAdapter.notifyDataSetChanged();
    }

    private void populateInfoFromParse() {
        String currentUser = getCurrentUser();

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("UserInfo");
        parseQuery.whereEqualTo("loginName", currentUser);
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (ParseObject parseObject : objects) {
                        String motto_p = parseObject.getString("motto");
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

                        motto.setText(motto_p);
                        name.setText(name_p);
//                        gender.setSelection(gender_p);
                        gender.setText(getNameByPosition(getResources().getStringArray(R.array.gender_values), gender_p));
                        age.setText(age_p);
                        location.setText(location_p);

                        if(interestedIn_p == 0) {
                            interestedIn.setText(getResources().getString(R.string.none_txt));
                        }
                        else {
                            interestedIn.setText(getNameByPosition(getResources().getStringArray(R.array.interested_in_values), interestedIn_p));
                        }
                        if(lookingFor_p == 0) {
                            lookingFor.setText(getResources().getString(R.string.none_txt));
                        }
                        else {
                            lookingFor.setText(getNameByPosition(getResources().getStringArray(R.array.looking_for_values), lookingFor_p));
                        }
                        if(aboutMe_p == null) {
                            aboutMe.setText(getResources().getString(R.string.none_txt));
                        }
                        else {
                            aboutMe.setText(aboutMe_p);
                        }
                        if(relationship_p == 0) {
                            relationship.setText(getResources().getString(R.string.none_txt));
                        }
                        else {
                            relationship.setText(getNameByPosition(getResources().getStringArray(R.array.relationship_values), relationship_p));
                        }
                        if(bodyType_p == 0) {
                            bodyType.setText(getResources().getString(R.string.none_txt));
                        }
                        else {
                            bodyType.setText(getNameByPosition(getResources().getStringArray(R.array.body_type_values), bodyType_p));
                        }
                        if (ethnicity_p == 0) {
                            ethnicity.setText(getResources().getString(R.string.none_txt));
                        } else {
                            ethnicity.setText(getNameByPosition(getResources().getStringArray(R.array.ethnicity_values), ethnicity_p));
                        }
                        if (interests_p == 0) {
                            interests.setText(getResources().getString(R.string.none_txt));
                        } else {
                            interests.setText(getNameByPosition(getResources().getStringArray(R.array.interests_values), interests_p));
                        }

                        setUserInfoToExtras();
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

    private void setUserInfoToExtras() {
        SharedPreferences sharedInfoPreferences = getSharedPreferences(PREFS_INFO, 0);
        SharedPreferences.Editor editor = sharedInfoPreferences.edit();
        editor.putString("motto", motto.getText().toString());
        editor.putString("name", name.getText().toString());
        editor.putString("gender", gender.getText().toString());
        editor.putString("age", age.getText().toString());
        editor.putString("location", location.getText().toString());
        editor.putString("interestedIn", interestedIn.getText().toString());
        editor.putString("lookingFor", lookingFor.getText().toString());
        editor.putString("aboutMe",aboutMe.getText().toString());
        editor.putString("relationship", relationship.getText().toString());
        editor.putString("bodyType", bodyType.getText().toString());
        editor.putString("ethnicity", ethnicity.getText().toString());
        editor.putString("interests", interests.getText().toString());
        editor.commit();
    }
}
