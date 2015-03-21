package com.android.formalchat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
public class ProfileActivity extends FragmentActivity {
    private static final String PREFS_NAME = "FormalChatPrefs";
    private ProfilePagerAdapter profilePagerAdapter;
    private ViewPager viewPager;
    private Button addImgBtn;
    private Button editBtn;
    private ImageView imgProfile;
    private ProfileAddImageDialog addImgWithDialog;

    private ArrayList<Drawable> drawablesList;
    private ArrayList<String> pathsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_layout);
        drawablesList = new ArrayList<>();
        pathsList = new ArrayList<>();

//        profilePagerAdapter = new ProfilePagerAdapter(drawablesList, this);
        viewPager = (ViewPager) findViewById(R.id.pager_profile);
//        viewPager.setAdapter(profilePagerAdapter);

        addImgBtn = (Button) findViewById(R.id.add_btn);
        addImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNetworkAvailable()){
                    addImgWithDialog = new ProfileAddImageDialog();
                    FragmentManager manager = getSupportFragmentManager();
                    addImgWithDialog.show(manager, "add_img_dialog");
                }
                else {
                    Toast.makeText(getApplicationContext(), "You are Offline", Toast.LENGTH_LONG).show();
                }
            }
        });

        editBtn = (Button) findViewById(R.id.edit_btn);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startUserInfoActivity();
            }
        });
    }

    private void startUserInfoActivity() {
        Intent intent = new Intent(this, UserInfoActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(isNeededToRefresh()) {
            setNewSharedPrefs();
            recreate();
         }

        if(isNetworkAvailable()) {
            loadImagesFromParseRemote();
        }
        else {
            // Get from local
            getImagesFromLocalStorage();
        }
    }

    private void setNewSharedPrefs() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("refreshOnDelete", false);
        editor.commit();
    }

    private boolean isNeededToRefresh() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        boolean b = sharedPreferences.getBoolean("refreshOnDelete", false);
        return b;
    }

    private void getImagesFromLocalStorage() {
        File folder = new File(Environment.getExternalStorageDirectory() + "/formal_chat");
        File[] dirImages = folder.listFiles();
        ArrayList<String> imagePaths = new ArrayList<>();
        String path;

        if(dirImages.length != 0) {
            for(int counter = 0; counter < dirImages.length; counter++) {
                path = "file:" + dirImages[counter].getPath().toString();
                imagePaths.add(path);
            }

            profilePagerAdapter = new ProfilePagerAdapter(getApplicationContext(), imagePaths);
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
                    Log.d("score", "Retrieved " + imagesList.size() + " images");

                    ArrayList<String> imagePaths = new ArrayList<>();
                    for (ParseObject po : imagesList) {
                        ParseFile imageFile = (ParseFile) po.get("photo");
                        Log.v("formalchat", "photo name = " + imageFile.getName() + " " + imageFile.getUrl());
                        imagePaths.add(((ParseFile)po.get("photo")).getUrl());
                    }

                    if (profilePagerAdapter != null) {
                        profilePagerAdapter.updateImages(imagePaths);
                    } else {
                        profilePagerAdapter = new ProfilePagerAdapter(getApplicationContext(), imagePaths);
                        viewPager.setAdapter(profilePagerAdapter);
                    }

                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
            }
        });
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
