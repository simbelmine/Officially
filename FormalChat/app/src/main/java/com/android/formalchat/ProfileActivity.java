package com.android.formalchat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
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
public class ProfileActivity extends DrawerActivity {
    private static final String PREFS_NAME = "FormalChatPrefs";
    public static final int NONE = 101;
    private SharedPreferences sharedPreferences;
    private ProfilePagerAdapter profilePagerAdapter;
    private ViewPager viewPager;
    private Button addImgBtn;
    private Button editBtn;
    private ImageView imgProfile;
    private ProfileAddImageDialog addImgWithDialog;
    private DrawerLayout drawerLayout;
    private String profileImgPath;

    private ArrayList<Drawable> drawablesList;
    private ArrayList<String> pathsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.profile_layout, null, false);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.addView(contentView, 0);
        drawablesList = new ArrayList<>();
        pathsList = new ArrayList<>();
        sharedPreferences = getSharedPreferences(PREFS_NAME, 0);

        setTitle();

        viewPager = (ViewPager) findViewById(R.id.pager_profile);

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

        getProfileImgPath();
    }

    private void setTitle() {
        int title_position = getIntent().getIntExtra("title_position", NONE);
        if(title_position != NONE) {
            getActionBar().setTitle(getResources().getStringArray(R.array.menu_list)[title_position]);
        }
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

                    ArrayList<String> imagePaths = new ArrayList<>();
                    for (ParseObject po : imagesList) {
                        imagePaths.add(((ParseFile)po.get("photo")).getUrl());
                    }

                    if(profileImgPath != null) {
                        imagePaths = getProfileImg(imagePaths);
                    }
                    if (profilePagerAdapter != null) {
                        profilePagerAdapter.updateImages(imagePaths);
                    } else {
                        profilePagerAdapter = new ProfilePagerAdapter(getApplicationContext(), imagePaths);
                        viewPager.setAdapter(profilePagerAdapter);
                    }

                } else {
                    Log.d("formalchat", "Error: " + e.getMessage());
                }
            }
        });
    }

    private ArrayList<String> getProfileImg(ArrayList<String> imagePaths) {
        ArrayList<String> imagePathsNew = new ArrayList<>();
        imagePathsNew.add(profileImgPath);
        imagePathsNew.addAll(getNewPathList(imagePaths, profileImgPath));

        return imagePathsNew;
    }

    private ArrayList<String> getNewPathList(ArrayList<String> imagePaths, String profileImgPath) {
        for(int path = 0; path < imagePaths.size(); path++) {
            if(profileImgPath.equals(imagePaths.get(path))) {
                imagePaths.remove(path);
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
