package com.android.formalchat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sve on 5/21/15.
 */
public class ProfileGallery extends DrawerActivity {
    private static final String PREFS_NAME = "FormalChatPrefs";
    private SharedPreferences sharedPreferences;
    private Activity activity;
    private GridView gridView;
    private boolean videoExists;
    private  ParseUser user;
    private ArrayList<String> imagePaths;
    private ProfileGalleryAdapter galleryAdapter;
    private ProfileAddImageDialog addImgWithDialog;
    private DrawerLayout drawerLayout;
    private ListView leftDrawerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.answersHolder.profile_gallery);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.profile_gallery, null, false);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.addView(contentView, 0);
//        getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        activity = this;
        sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        gridView = (GridView) findViewById(R.id.grid_layout_profile);
        imagePaths = new ArrayList<>();
        user = ParseUser.getCurrentUser();
        videoExists = isVideoExists();

        setTitle();
        if(isNetworkAvailable()) {
            if (videoExists) {
                addVideoToPaths();
            }
            getImagesFromParse();
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
            imagePaths.clear();
            if(isNetworkAvailable()) {
                if (videoExists) {
                    addVideoToPaths();
                }
                getImagesFromParse();
            }
            sharedPreferences.edit().putBoolean("refresh", false).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.gallery_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
                startActivity(new Intent(this, VideoRecordActivity.class));
                return true;
            case android.R.id.home:
                if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
                return true;
            default:
                return false;
        }
    }

    private void setTitle() {
        int title_position = getIntent().getIntExtra("title_position", NONE);
        if(title_position == DrawerActivity.PROFILE_ID) {
            setTitle(getResources().getString(R.string.gallery));
        }
        else
        if(title_position != NONE) {
            setTitle(getResources().getStringArray(R.array.menu_list)[title_position]);
        }
        else if (title_position == NONE) {
            setTitle(getResources().getString(R.string.gallery));
        }
    }

    private void getImagesFromParse() {
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
