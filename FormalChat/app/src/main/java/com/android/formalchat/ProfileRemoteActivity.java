package com.android.formalchat;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sve on 5/29/15.
 */
public class ProfileRemoteActivity extends DrawerActivity {
    private static final String PREFS_NAME = "FormalChatPrefs";
    private static final String PREFS_INFO = "FormalChatUserInfo";
    public static final int NONE = 101;
    private File dir = Environment.getExternalStorageDirectory();
    private String filePath = "/.formal_chat/";
    private SharedPreferences sharedPreferences;
    private ProfileAddImageDialog addImgWithDialog;
    private DrawerLayout drawerLayout;
    private String profileImgPath;
    private ParseUser user;
    private ArrayList<String> imagePaths;
    private Activity activity;
    private boolean videoExists;
    private ImageView profilePic;
    private RoundedImageView smallProfilePic;
    private ImageView sexIcon;
    private  boolean isMale;
    private ImageView got_it_img;
    private RelativeLayout help_video_leyout;
    private String shortName;

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
        View contentView = inflater.inflate(R.layout.profile_remote, null, false);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.addView(contentView, 0);
        sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        user = ParseUser.getCurrentUser();
        imagePaths = new ArrayList<>();
        activity = this;
        isMale = true;

        setTitle();
        videoExists = isVideoExists();

        init();
        setOnClickListeners();
        applyLayoutTransition();
        getProfileImgPath();
        if(isNetworkAvailable()) {
            loadBigProfilePicFromParse();
            loadSmallProfilePicFromParse();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
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
        profilePic = (ImageView) findViewById(R.id.profile_pic);
        smallProfilePic = (RoundedImageView) findViewById(R.id.small_prof_pic);
        sexIcon = (ImageView) findViewById(R.id.sex_icon);
        help_video_leyout = (RelativeLayout) findViewById(R.id.help_layout);
        got_it_img = (ImageView) findViewById(R.id.got_it_img);
        // *** Footer
        motto = (TextView) findViewById(R.id.motto);
        name = (TextView) findViewById(R.id.name_edit);
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

    private void setSexIcon(int sex) {
        if(sex == 0) {
            sexIcon.setImageDrawable(getResources().getDrawable(R.drawable.male));
        }
        else if(sex == 1){
            sexIcon.setImageDrawable(getResources().getDrawable(R.drawable.female));
            isMale = false;
        }
    }

    private boolean isVideoExists() {
        if(user.containsKey("video")) {
            return true;
        }
        return false;
    }

    private void setOnClickListeners() {
        got_it_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((FrameLayout)help_video_leyout.getParent()).removeView(help_video_leyout);
                startVideo();
            }
        });

        smallProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVideo();
            }
        });
    }

    private void startVideo() {
        if(isVideoExists()) {
            downloadVideoIfNotExists();
            String videoPath = dir + filePath + shortName;
            startActivity(VideoShowActivity.class, "videoUri", videoPath);
        }
        else {
            Toast.makeText(getApplicationContext(), "Sorry, no video to show", Toast.LENGTH_SHORT).show();
        }
    }


    private void applyLayoutTransition() {
        LayoutTransition transition = new LayoutTransition();
        help_video_leyout.setLayoutTransition(transition);
    }

    private void downloadVideoIfNotExists() {
        ParseUser user = ParseUser.getCurrentUser();
        ParseFile videoFile = user.getParseFile("video");
        String fileName = videoFile.getName();
        shortName = getShortImageNameFromUri(fileName);

        File targetFolder = new File(dir + filePath);
        if(!targetFolder.exists()) {
            targetFolder.mkdir();
        }

        File tmpFile = new File(dir, filePath + shortName);
        if(!tmpFile.exists()) {
            startVideoDownloadService();
        }
    }

    private void startVideoDownloadService() {
        Intent intent = new Intent(this, VideoDownloadService.class);
        intent.putExtra(VideoDownloadService.DIRPATH, dir.getAbsolutePath());
        intent.putExtra(VideoDownloadService.FILEPATH, filePath);

        startService(intent);
    }

    private Bitmap getVideoThumbnail(Uri videoUri) {
        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(videoUri.getPath(),
                MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
        return thumb;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(isNetworkAvailable()) {
            populateInfoFromParse();
        }
        else {
            // Get from local
            //getImagesFromLocalStorage();
        }
    }

    private void startActivity(Class classToLoad, String extraName, String extraValue) {
        Intent intent = new Intent(this, classToLoad);
        if(extraName != null && extraName != null) {
            intent.putExtra(extraName, extraValue);
        }
        startActivity(intent);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void loadBigProfilePicFromParse() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.formal_chat";
        if(isProfPicExists(path)) {
            Bitmap myBitmap = BitmapFactory.decodeFile(path + "/blurred_profile.jpg");
            profilePic.setImageBitmap(myBitmap);
        }
        else
        {
            if (user.has("profileImgName")) {
                final String profileImgName = user.get("profileImgName").toString();
                ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("UserImages");
                parseQuery.whereEqualTo("userName", getCurrentUser());
                parseQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> list, ParseException e) {
                        if (list.size() > 0) {
                            for (ParseObject po : list) {
                                String picUrl = ((ParseFile) po.get("photo")).getUrl();
                                String nameFromUrl = getShortImageNameFromUri(picUrl);

                                if (profileImgName.equals(nameFromUrl)) {

                                    ParseFile parseFile = ((ParseFile) po.get("photo"));
                                    parseFile.getDataInBackground(new GetDataCallback() {
                                        @Override
                                        public void done(byte[] bytes, ParseException e) {
                                            if (e == null) {
                                                Bitmap blurredBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                                BlurredImage bm = new BlurredImage();
                                                Bitmap bitmap = bm.getBlurredImage(blurredBitmap, 50);
                                                profilePic.setImageBitmap(bitmap);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    private boolean isProfPicExists(String path) {
        File dir = new File(path);
        File[] files_list = dir.listFiles();

        for(int f = 0; f < files_list.length; f++) {
            if("blurred_profile.jpg".equals(files_list[f].getName())) {
                return true;
            }
        }
        return false;
    }


    private void loadSmallProfilePicFromParse() {
        if(profileImgPath != null) {
            Picasso.with(this).load(profileImgPath).into(smallProfilePic);
        }
    }

    private void getProfileImgPath() {
        // profileImgPath = sharedPreferences.getString("profPic", null);
        ParseFile pic = user.getParseFile("profileImg");
        profileImgPath = pic.getUrl();
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

                        if(motto_p == null || "".equals(motto_p)) {
                            motto.setText(getResources().getString(R.string.motto));
                        }
                        else {
                            motto.setText(motto_p);
                        }
                        name.setText(name_p);
//                        gender.setSelection(gender_p);
                        // gender.setText(getNameByPosition(getResources().getStringArray(R.array.gender_values), gender_p));
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
                        if(aboutMe_p == null || "".equals(aboutMe_p)) {
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
                        setSexIcon(gender_p);
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

    public String getShortImageNameFromUri(String url) {
        return url.substring(url.lastIndexOf("-")+1);
    }

    private void setUserInfoToExtras() {
        SharedPreferences sharedInfoPreferences = getSharedPreferences(PREFS_INFO, 0);
        SharedPreferences.Editor editor = sharedInfoPreferences.edit();

        editor.putString("motto", getRightText(motto.getText().toString()));
        editor.putString("name", getRightText(name.getText().toString()));

        if(isMale) {
            editor.putString("gender", getNameByPosition(getResources().getStringArray(R.array.gender_values), 0));
        }
        else {
            editor.putString("gender", getNameByPosition(getResources().getStringArray(R.array.gender_values), 1));
        }

        editor.putString("age", getRightText(age.getText().toString()));
        editor.putString("location", getRightText(location.getText().toString()));
        editor.putString("interestedIn", getRightText(interestedIn.getText().toString()));
        editor.putString("lookingFor", getRightText(lookingFor.getText().toString()));
        editor.putString("aboutMe", getRightText(aboutMe.getText().toString()));
        editor.putString("relationship", getRightText(relationship.getText().toString()));
        editor.putString("bodyType", getRightText(bodyType.getText().toString()));
        editor.putString("ethnicity", getRightText(ethnicity.getText().toString()));
        editor.putString("interests", getRightText(interests.getText().toString()));
        editor.commit();
    }

    private String getRightText(String s) {
        String none_txt = getResources().getString(R.string.none_txt);
        if(none_txt.equals(s)) {
            return null;
        }
        return s;
    }
}
