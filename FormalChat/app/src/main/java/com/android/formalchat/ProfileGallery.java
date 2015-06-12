package com.android.formalchat;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.GridView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Sve on 5/21/15.
 */
public class ProfileGallery extends Activity {
    private static final String PREFS_NAME = "FormalChatPrefs";
    private SharedPreferences sharedPreferences;
    private Activity activity;
    private GridView gridView;
    private boolean videoExists;
    private  ParseUser user;
    private ArrayList<String> imagePaths;
    private ProfileGalleryAdapter galleryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_gallery);
        getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        activity = this;
        sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        gridView = (GridView) findViewById(R.id.grid_layout_profile);
        imagePaths = new ArrayList<>();
        user = ParseUser.getCurrentUser();
        videoExists = isVideoExists();

        if(isNetworkAvailable()) {
            if (videoExists) {
                addVideoToPaths();
            }

            ParseUser user = ParseUser.getCurrentUser();
            ParseQuery<ParseObject> query = ParseQuery.getQuery("UserImages");
            query.whereEqualTo("userName", user.getUsername());
            query.orderByAscending("createdAt");
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {
                    if (list.size() > 0) {
                        for (ParseObject po : list) {
                            String picUrl = ((ParseFile) po.get("photo")).getUrl();
                            imagePaths.add(picUrl);
                        }

                        //gridView.setAdapter(new ProfileGalleryAdapter(activity, getApplicationContext(), imagePaths));
                        initAdapter();

                    } else {
                        Log.v("formalchat", "listsize is 0 ");
                    }

                    if (e != null) {
                        Log.v("formalchat", "e = " + e.getMessage());
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if(sharedPreferences.contains("refresh") && sharedPreferences.getBoolean("refresh", false)) {
            File dir = Environment.getExternalStorageDirectory();
            String filePath = "/.formal_chat";

            File dir_folder = new File(dir, filePath);
            File[] files = dir_folder.listFiles();
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File file1, File file2) {
                    if (file1.lastModified() > file2.lastModified()) {
                        return 1;
                    }
                    else if (file1.lastModified() < file2.lastModified()) {
                        return -1;
                    }
                    else {
                        return 0;
                    }
                }
            });

            ArrayList<String> imagePaths = new ArrayList<>();
            for(File f : files) {
                if("IMG".equals(f.getName().substring(0, 3))) {
                    imagePaths.add("file:" + f.getAbsolutePath());
                }
            }

            galleryAdapter.updateImages(imagePaths);
            sharedPreferences.edit().putBoolean("refresh", false).commit();
        }
    }

    private void initAdapter() {
        if (galleryAdapter != null) {
            galleryAdapter.updateImages(imagePaths);
        } else {
            galleryAdapter = new ProfileGalleryAdapter(activity, getApplicationContext(), imagePaths);
            gridView.setAdapter(galleryAdapter);
        }
    }

    private boolean isVideoExists() {
        if(user.containsKey("video")) {
            return true;
        }
        return false;
    }

    private void addVideoToPaths() {
        String videoPath = user.getParseFile("video").getUrl();
        imagePaths.add(videoPath);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
