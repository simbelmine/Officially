package com.android.formalchat.profile;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.android.formalchat.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sve on 4/28/15.
 *
 * Not used anymore. Change made on 5/22/15.
 */
public class ProfileViewGallery extends Activity {
    private Activity activity;
    private ViewPager viewPager;
    private ArrayList<String> imagePaths;
    private ProfilePagerAdapter profilePagerAdapter;
    private boolean videoExists;
    private  ParseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_gallery);

        activity = this;
        imagePaths = new ArrayList<>();
        viewPager = (ViewPager) findViewById(R.id.pager_profile);
        user = ParseUser.getCurrentUser();

        videoExists = isVideoExists();
        if(isNetworkAvailable()) {
            loadImagesFromParseRemote();
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

//                    if(profileImgPath != null) {
//                        imagePaths = setPathInFront(imagePaths, profileImgPath);
//                    }
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

    private String getUserName() {
        ParseUser parseUser = ParseUser.getCurrentUser();
        return parseUser.getUsername();
    }

    private boolean isVideoExists() {

        if(user.containsKey("video")) {
            return true;
        }
        return false;
    }

    private void addVideoToPaths() {
        String videoPath = user.getParseFile("video").getUrl();
        imagePaths = setPathInFront(imagePaths, videoPath);
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
}
