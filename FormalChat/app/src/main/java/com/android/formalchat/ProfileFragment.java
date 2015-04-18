package com.android.formalchat;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * Created by Sve on 4/16/15.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {
    private static final String PREFS_NAME = "FormalChatPrefs";
    public static final int NONE = 101;
    private static final int ADD_IMG_DIALOG = 1;
    private SharedPreferences sharedPreferences;
    private ProfilePagerAdapter profilePagerAdapter;
    private ViewPager viewPager;
    private Button addImgBtn;
    private Button editBtn;
    private ProfileAddImageDialog addImgWithDialog;
    private String profileImgPath;
    private LinearLayout exclamationLayout;
    private Button btn;
    private ParseUser user;
    private ArrayList<String> imagePaths;
    private Activity activity;
    private boolean videoExists;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.profile_layout, container, false);

        sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, 0);
        user = ParseUser.getCurrentUser();
        activity = getActivity();

        setTitle();
        videoExists = isVideoExists();

        viewPager = (ViewPager) rootView.findViewById(R.id.pager_profile);
        init(rootView);
        if (isNetworkAvailable()) {
            AsyncTask<Void, Void, Void> loadingTask = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    loadImagesFromParseRemote();
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    initVideoMessage();
                    addViewListeners();
                    getProfileImgPath();
                }
            };
            loadingTask.execute();
        }

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_btn:
                if (isNetworkAvailable()) {
                    showDialog();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "You are Offline", Toast.LENGTH_LONG).show();
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

    private void showDialog() {
        addImgWithDialog = new ProfileAddImageDialog();
        addImgWithDialog.setTargetFragment(this, ADD_IMG_DIALOG);
        addImgWithDialog.show(getFragmentManager().beginTransaction(), "add_img_dialog");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ADD_IMG_DIALOG:
                if (resultCode == Activity.RESULT_OK) {
                    loadImagesFromParseRemote();
                } else if (resultCode == Activity.RESULT_CANCELED){
                    Log.v("formalchat", "onActivityResult CANCELED");
                }
                break;
        }
    }

    private void setTitle() {
        int title_position = getActivity().getIntent().getIntExtra("title_position", NONE);
        if (title_position != NONE) {
            getActivity().getActionBar().setTitle(getResources().getStringArray(R.array.menu_list)[title_position]);
        }
    }

    private void init(View rootView) {
        viewPager = (ViewPager) rootView.findViewById(R.id.pager_profile);
        addImgBtn = (Button) rootView.findViewById(R.id.add_btn);
        editBtn = (Button) rootView.findViewById(R.id.edit_btn);
        exclamationLayout = (LinearLayout) rootView.findViewById(R.id.exclamation_layout);
        btn = (Button) rootView.findViewById(R.id.btn);
    }

    private void initVideoMessage() {
        if (!videoExists) {
            exclamationLayout.setVisibility(View.VISIBLE);
        } else {
            exclamationLayout.setVisibility(View.INVISIBLE);
        }
    }

    private boolean isVideoExists() {
        if (user.containsKey("video")) {
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


    private void addVideoToPaths() {
        String videoPath = user.getParseFile("video").getUrl();
        imagePaths = setPathInFront(imagePaths, videoPath);
    }

    private void startActivity(Class classToLoad) {
        Intent intent = new Intent(getActivity(), classToLoad);
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

        if (dirImages.length != 0) {
            for (int counter = 0; counter < dirImages.length; counter++) {
                path = "file:" + dirImages[counter].getPath().toString();
                imagePaths.add(path);
            }

            profilePagerAdapter = new ProfilePagerAdapter(activity, getActivity().getApplicationContext(), imagePaths);
            viewPager.setAdapter(profilePagerAdapter);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
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
                    imagePaths = new ArrayList<>();
                    for (ParseObject po : imagesList) {
                        imagePaths.add(((ParseFile) po.get("photo")).getUrl());
                    }

                    if (profileImgPath != null) {
                        imagePaths = setPathInFront(imagePaths, profileImgPath);
                    }
                    if (videoExists) {
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
        Log.e("formalchat", "pagerAdapter = " + profilePagerAdapter);
        if (profilePagerAdapter != null) {
            profilePagerAdapter.updateImages(imagePaths);
        } else {
            profilePagerAdapter = new ProfilePagerAdapter(activity, getActivity().getApplicationContext(), imagePaths);
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
        for (int pathIdx = 0; pathIdx < imagePaths.size(); pathIdx++) {
            if (path.equals(imagePaths.get(pathIdx))) {
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