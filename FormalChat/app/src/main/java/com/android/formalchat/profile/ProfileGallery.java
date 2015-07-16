package com.android.formalchat.profile;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.formalchat.DrawerActivity;
import com.android.formalchat.R;
import com.android.formalchat.VideoDownloadService;
import com.android.formalchat.VideoRecordActivity;
import com.android.formalchat.VideoUploadService;
import com.parse.FindCallback;
import com.parse.GetCallback;
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
    public static final String RESULT = "result";
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
        setTitle();

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.profile_gallery, null, false);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.addView(contentView, 0);
//        getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        activity = this;
        sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        gridView = (GridView) findViewById(R.id.grid_layout_profile);
        imagePaths = new ArrayList<>();
        loadDataAccordingUser();
    }

    private void loadDataAccordingUser() {
        if(getIntent().hasExtra("userNameProfile")) {
            String userName = getIntent().getStringExtra("userNameProfile");
            if(!userName.equals("")) {
                ParseQuery<ParseUser> parseQuery = ParseUser.getQuery();
                parseQuery.whereEqualTo("username", userName);
                parseQuery.getFirstInBackground(new GetCallback<ParseUser>() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {
                        if(e == null) {
                            user = parseUser;
                            videoExists = isVideoExists();
                            loadPicturesFromParse();
                        }
                    }
                });
            }
        }
        else {
            user = ParseUser.getCurrentUser();
            videoExists = isVideoExists();
            loadPicturesFromParse();
        }
    }

    private void loadPicturesFromParse() {
        if(isNetworkAvailable()) {
            populateResourcesFromParse();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilterPictureUpload = new IntentFilter(ProfileAddImageDialog.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNoticePictureUpload, intentFilterPictureUpload);
        IntentFilter intentFilterUploadVideo = new IntentFilter(VideoUploadService.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNoticeUploadVideo, intentFilterUploadVideo);
        IntentFilter intentFilterDownloadVideo = new IntentFilter(VideoDownloadService.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNoticeDownloadVideo, intentFilterDownloadVideo);
        IntentFilter intentFilterDeletedPicture = new IntentFilter(FullImageActivity.ACTION_DELETED);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNoticePictureDeleted, intentFilterDeletedPicture);
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNoticeUploadVideo);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNoticeDownloadVideo);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        if(sharedPreferences.contains("refresh") && sharedPreferences.getBoolean("refresh", false)) {
//            imagePaths.clear();
//            if(isNetworkAvailable()) {
//                if (videoExists) {
//                    addVideoToPaths();
//                }
//                getImagesFromParse();
//            }
//            sharedPreferences.edit().putBoolean("refresh", false).commit();
//        }
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

    private BroadcastReceiver onNoticePictureUpload = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() != null) {
                if (isNetworkAvailable()) {
                    populateResourcesFromParse();
                }
            }
        }
    };

    private BroadcastReceiver onNoticeDownloadVideo = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if(bundle != null && bundle.containsKey(RESULT)) {
                if(RESULT_OK == bundle.getInt(RESULT)) {
                    galleryAdapter.notifyDataSetChanged();
                }
            }
            else {
                Log.e("formalchat", "DoWnLoAd Failed .... !!!");
            }
        }
    };
    private BroadcastReceiver onNoticeUploadVideo = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
           galleryAdapter.notifyDataSetChanged();
        }
    };

    private BroadcastReceiver onNoticePictureDeleted = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isNetworkAvailable()) {
                populateResourcesFromParse();
            }
        }
    };

    private void populateResourcesFromParse() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserImages");
        query.whereEqualTo("userName", user.getUsername());
        query.orderByAscending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (list.size() > 0) {
                    ArrayList<String> imagesPaths = new ArrayList<>();

                    if (videoExists) {
                        imagesPaths = addVideoToPaths(imagesPaths);
                    }

                    for (ParseObject po : list) {
                        String picUrl = ((ParseFile) po.get("photo")).getUrl();
                        imagesPaths.add(picUrl);
                    }
                    Log.v("formalchat", "findInBackground images = " + imagePaths.size());
                    //gridView.setAdapter(new ProfileGalleryAdapter(activity, getApplicationContext(), imagePaths));
                    initAdapter(imagesPaths);

                } else {
                    Log.v("formalchat", "listsize is 0 ");
                }

                if (e != null) {
                    Log.v("formalchat", "e = " + e.getMessage());
                }
            }
        });
    }

    private void initAdapter(ArrayList<String> imagesPaths) {
        if (galleryAdapter != null) {
            Log.v("formalchat", "adapter NOT null = " + imagesPaths.size());
            galleryAdapter.updateImages(imagesPaths);
        } else {
            Log.v("formalchat", "adapter null = " + imagesPaths.size());
            galleryAdapter = new ProfileGalleryAdapter(activity, getApplicationContext(), imagesPaths);
            gridView.setAdapter(galleryAdapter);
        }
    }

    private boolean isVideoExists() {
        if(user != null) {
            if (user.containsKey("video")) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<String> addVideoToPaths(ArrayList<String> imagesPaths) {
        String videoPath = user.getParseFile("video").getUrl();
        imagesPaths.add(videoPath);
        return imagesPaths;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
