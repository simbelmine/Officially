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
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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
    public static final int NONE = 101;
    private SharedPreferences sharedPreferences;
    private ProfilePagerAdapter profilePagerAdapter;
    private ViewPager viewPager;
    private Button addImgBtn;
    private Button editBtn;
    private ProfileAddImageDialog addImgWithDialog;
    private DrawerLayout drawerLayout;
    private String profileImgPath;
    private LinearLayout exclamationLayout;
    private Button btn;
    private ParseUser user;
    private ArrayList<String> imagePaths;
    private Activity activity;
    private boolean videoExists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.profile_layout, null, false);
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
        initVideoMessage();
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
        if(title_position != NONE) {
            getActionBar().setTitle(getResources().getStringArray(R.array.menu_list)[title_position]);
        }
    }

    private void init() {
        viewPager = (ViewPager) findViewById(R.id.pager_profile);
        addImgBtn = (Button) findViewById(R.id.add_btn);
        editBtn = (Button) findViewById(R.id.edit_btn);
        exclamationLayout = (LinearLayout) findViewById(R.id.exclamation_layout);
        btn = (Button) findViewById(R.id.btn);
    }

    private void initVideoMessage() {
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
        addImgBtn.setOnClickListener(this);
        editBtn.setOnClickListener(this);
        exclamationLayout.setOnClickListener(this);
        btn.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isNeededToRefresh()) {
            setNewSharedPrefs();
            recreate();
        }

        if(isNetworkAvailable()) {
            if(!videoExists) {
                exclamationLayout.setVisibility(View.VISIBLE);
            }
            else {
                exclamationLayout.setVisibility(View.INVISIBLE);
            }
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
            case R.id.add_btn:
                if(isNetworkAvailable()){
                    addImgWithDialog = new ProfileAddImageDialog();
                    FragmentManager manager = getSupportFragmentManager();
                    addImgWithDialog.show(manager, "add_img_dialog");
                }
                else {
                    Toast.makeText(getApplicationContext(), "You are Offline", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.edit_btn:
                startActivity(UserInfoActivity.class);
                break;
            case R.id.exclamation_layout:
                startActivity(VideoRecordActivity.class);
                break;
            case R.id.btn:
                startActivity(VideoRecordActivity.class);
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
}
