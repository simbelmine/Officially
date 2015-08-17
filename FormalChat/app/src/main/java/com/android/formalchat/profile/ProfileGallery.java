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
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.File;
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
    private SwipeRefreshLayout swipeContainer;
    private Menu menu;

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
        initSwipeContainer();

        loadDataAccordingUser();
    }

    private void initSwipeContainer() {
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        setSwipeAppearance();
        setOnRefreshListener();
    }

    private void setSwipeAppearance() {
        swipeContainer.setColorSchemeResources(
                android.R.color.holo_blue_dark,
                android.R.color.holo_green_dark,
                android.R.color.holo_blue_dark,
                android.R.color.holo_green_dark
        );
    }

    private void setOnRefreshListener() {
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadDataAccordingUser();
            }
        });
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

        swipeContainer.setRefreshing(false);
    }

    private void loadPicturesFromParse() {
        if(isNetworkAvailable()) {
            populateResourcesFromParse();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Picture Upload
        IntentFilter intentFilterPictureUpload = new IntentFilter(ProfileAddImageDialog.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNoticePictureUpload, intentFilterPictureUpload);
        // Video Upload
        IntentFilter intentFilterUploadVideo = new IntentFilter(VideoUploadService.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNoticeUploadVideo, intentFilterUploadVideo);
        // Video Download
        IntentFilter intentFilterDownloadVideo = new IntentFilter(VideoDownloadService.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNoticeDownloadVideo, intentFilterDownloadVideo);
        // Picture Delete
        IntentFilter intentFilterDeletedPicture = new IntentFilter(FullImageActivity.ACTION_DELETED);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNoticePictureDeleted, intentFilterDeletedPicture);
        // Delete All pictures (by selection)
        IntentFilter intentFilterDeleteAllPictures = new IntentFilter(ProfileGalleryAdapter.ACTION_DELETE_ALL);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNoticeDeleteAllPictures, intentFilterDeleteAllPictures);
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNoticePictureUpload);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNoticeUploadVideo);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNoticeDownloadVideo);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNoticePictureDeleted);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNoticeDeleteAllPictures);
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
        this.menu = menu;
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.gallery_menu, menu);
        hideOptionFromMenu(R.id.delete_all);
        return true;
    }

    private void hideOptionFromMenu(int itemId) {
        menu.findItem(itemId).setVisible(false);
    }

    private void showOptionFromMenu(int itemId) {
        menu.findItem(itemId).setVisible(true);
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
            case R.id.delete_all:
                if(galleryAdapter != null) {
                    ArrayList<String> shortSelectedImageNames = getShortImageNames(getPicNamesByPosition());
                    deleteAllonParse(shortSelectedImageNames);
                }
                return true;
            default:
                return false;
        }
    }

    private ArrayList<String> getShortImageNames(ArrayList<String> picNamesByPosition) {
        ArrayList<String> shortNames = new ArrayList<>();
        for(String longName : picNamesByPosition) {
            shortNames.add(getParseImgNameFromUri(longName));
        }

        return shortNames;
    }

    private ArrayList<String> getPicNamesByPosition() {
        ArrayList<String> selectedImageNames = new ArrayList<>();
        if(galleryAdapter != null) {
            List<String> imagePaths = galleryAdapter.getImagePaths();
            ArrayList<Integer> selectedImagePositions = galleryAdapter.getSelectedItems();

            if (selectedImagePositions != null && imagePaths != null) {
                for (Integer position : selectedImagePositions) {
                    if(position < imagePaths.size()) {
                        selectedImageNames.add(imagePaths.get(position));
                    }
                }
            }
        }

        return  selectedImageNames;
    }

    private void deleteAllonParse(ArrayList<String> picNamesByPosition) {
        ParseQuery<ParseObject> parseQuery = new ParseQuery("UserImages");
        parseQuery.whereContainedIn("photo", picNamesByPosition);

        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> imagesList, ParseException e) {
                if (e == null) {
                    if (imagesList.size() > 0) {
//                        deleteAllonLocal(imagesList);

                        for(ParseObject po : imagesList) {
                            ProfileGalleryUtils profileGalleryUtils = new ProfileGalleryUtils(getApplicationContext(), user, po);

                            if(profileGalleryUtils.isProfilePic()) {
                                profileGalleryUtils.deleteProfileImgFromParse();
                                profileGalleryUtils.deleteBlurrredImageFromLocal();
                            }

                            profileGalleryUtils.deleteImgFromParse();
                        }

                        galleryAdapter.clearSelectedItems();
                        sendBroadcastMessage(ProfileGalleryAdapter.ACTION_DELETE_ALL, "showDeleteAll", false);
                    }
                } else {
                    Log.e("formalchat", "Delete command: " + e.getMessage());
                }
            }
        });
    }

    private void deleteAllonLocal(List<ParseObject> selectedItems) {
        File dir = new File(Environment.getExternalStorageDirectory() + "/.formal_chat");

        for(ParseObject po : selectedItems) {
            String fileName = getShortImageNameFromUri(((ParseFile) po.get("photo")).getName());
            File file = new File(dir, fileName);
            if(file.exists()) {
                file.delete();
            }
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
            galleryAdapter.clearSelectedItems();
            if (isNetworkAvailable()) {
                populateResourcesFromParse();
            }
        }
    };

    private BroadcastReceiver onNoticeDeleteAllPictures = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isNetworkAvailable()) {
                if(intent.hasExtra("showDeleteAll")) {
                    if(intent.getBooleanExtra("showDeleteAll", false)) {
                        showOptionFromMenu(R.id.delete_all);
                    }
                    else {
                        hideOptionFromMenu(R.id.delete_all);
                    }
                }
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
                ArrayList<String> imagesPaths = new ArrayList<>();
                ArrayList<String> imageThumbnailsPaths = new ArrayList<>();
                if (list.size() > 0) {
                    if (videoExists) {
                        imagesPaths = addVideoToPaths(imagesPaths, "video");
                        imageThumbnailsPaths = addVideoToPaths(imageThumbnailsPaths, "video_thumbnail");
                    }

                    for (ParseObject po : list) {
                        String picUrl = ((ParseFile) po.get("photo")).getUrl();
                        imagesPaths.add(picUrl);

                        String picThumbnailUrl = ((ParseFile) po.get("thumbnail_photo")).getUrl();
                        imageThumbnailsPaths.add(picThumbnailUrl);
                    }

                    //gridView.setAdapter(new ProfileGalleryAdapter(activity, getApplicationContext(), imagePaths));
                    initAdapter(imagesPaths, imageThumbnailsPaths);

                } else {
                    Log.v("formalchat", "listsize is 0 ");
                    // If list with images is 0, but there is a video
                    if (videoExists) {
                        imagesPaths = addVideoToPaths(imagesPaths, "video");
                        imageThumbnailsPaths = addVideoToPaths(imageThumbnailsPaths, "video_thumbnail");
                        initAdapter(imagesPaths, imageThumbnailsPaths);
                    }
                }

                if (e != null) {
                    Log.v("formalchat", "e = " + e.getMessage());
                }
            }
        });
    }

    private void initAdapter(ArrayList<String> imagesPaths, ArrayList<String> imageThumbnailsPaths) {
        if (galleryAdapter != null) {
            galleryAdapter.updateImages(imagesPaths, imageThumbnailsPaths, user);
        } else {
            galleryAdapter = new ProfileGalleryAdapter(activity, getApplicationContext(), imagesPaths, imageThumbnailsPaths, user);
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

    private ArrayList<String> addVideoToPaths(ArrayList<String> paths, String videoParseTag) {
        String videoPath = user.getParseFile(videoParseTag).getUrl();
        paths.add(videoPath);
        return paths;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public String getParseImgNameFromUri(String name) {
        return name.substring(name.lastIndexOf("/")+1);
    }

    public String getShortImageNameFromUri(String name) {
        return name.substring(name.lastIndexOf("-")+1);
    }

    private void sendBroadcastMessage(String action, String extraName, boolean extraValue) {
        Intent sender = new Intent(action);
        if(extraName != null) {
            sender.putExtra(extraName, extraValue);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(sender);
    }
}
