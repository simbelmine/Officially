package com.android.formalchat.profile;
import com.android.formalchat.ZodiacCalculator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.formalchat.BlurredImage;
import com.android.formalchat.DrawerActivity;
import com.android.formalchat.R;
import com.android.formalchat.RoundedImageView;
import com.android.formalchat.VideoDownloadService;
import com.android.formalchat.VideoShowActivity;
import com.android.formalchat.ZodiacCalculator;
import com.android.formalchat.ZodiacSign;
import com.parse.FindCallback;
import com.parse.GetCallback;
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
 * Created by Sve on 6/28/15.
 */
public class ProfileBaseActivity extends DrawerActivity implements View.OnClickListener {
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
    private String shortName;
    private ImageView zodiacSign;

    protected ImageView got_it_img;                     // Remote Profile
    protected RelativeLayout help_video_layout;         // Remote Profile
    protected ImageButton edit_feb_btn;                 // User's Profile

    private TextView name;
    private TextView gender;
    private TextView age;
    private TextView photos_btn;
    private int photos_btn_counter;

    private TextView motto;
    private TextView location;
    private TextView drinking;
    private TextView smoking;
    private TextView religion;
    private TextView height;
    private TextView bodyType;
    private TextView relationship;
    private TextView children;
    private TextView ethnicity;
    private TextView education;
    private TextView aboutMe;
    private TextView perfectSmn;
    private TextView perfectDate;
    private TextView interests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle();
        initView();
        init();
        setZodiacalSign();
        startGalleryPhotosCounter();
        setOnClickListeners();
       //  applyLayoutTransition();  - Only if it's Remote ProfileBaseActivity
        getProfileImgPath();
        if(isNetworkAvailable()) {
            loadBigProfilePicFromParse();
            loadSmallProfilePicFromParse();
        }
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

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.profile, null, false);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.addView(contentView, 0);
    }

    private void init() {
        // *** Main
        sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        user = ParseUser.getCurrentUser();
        imagePaths = new ArrayList<>();
        activity = this;
        isMale = true;
        videoExists = isVideoExists();

        // *** Header
        profilePic = (ImageView) findViewById(R.id.profile_pic);
        smallProfilePic = (RoundedImageView) findViewById(R.id.small_prof_pic);
        name = (TextView) findViewById(R.id.name_edit);
        sexIcon = (ImageView) findViewById(R.id.sex_icon);
        age = (TextView) findViewById(R.id.age_edit);
        edit_feb_btn = (ImageButton) findViewById(R.id.feb_button);
        help_video_layout = (RelativeLayout) findViewById(R.id.help_layout);
        got_it_img = (ImageView) findViewById(R.id.got_it_img);
        photos_btn = (TextView) findViewById(R.id.photos_button);
        zodiacSign = (ImageView) findViewById(R.id.zodiac_sign);

        // *** Footer
        motto = (TextView) findViewById(R.id.motto);
        location = (TextView) findViewById(R.id.location_edit);
        drinking = (TextView) findViewById(R.id.drinking_edit);
        smoking = (TextView) findViewById(R.id.smoking_edit);
        religion = (TextView) findViewById(R.id.religion_edit);
        height = (TextView) findViewById(R.id.height_edit);
        bodyType = (TextView) findViewById(R.id.body_type_edit);
        relationship = (TextView) findViewById(R.id.relationship_edit);
        children = (TextView) findViewById(R.id.children_edit);
        ethnicity = (TextView) findViewById(R.id.ethnicity_edit);
        education = (TextView) findViewById(R.id.education_edit);
        aboutMe = (TextView) findViewById(R.id.about_me_edit);
        perfectSmn = (TextView) findViewById(R.id.perfect_smn_edit);
        perfectDate = (TextView) findViewById(R.id.perfect_date_edit);
        interests = (TextView) findViewById(R.id.interests_edit);
    }

    private boolean isVideoExists() {
        if(user.containsKey("video")) {
            return true;
        }
        return false;
    }

    private void setZodiacalSign() {
        ParseQuery<ParseObject> parseQuery = new ParseQuery<>("UserInfo");
        parseQuery.whereContains("loginName", user.getUsername());
        parseQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if(e == null) {
                    if(parseObject.containsKey("birthday")) {
                        getZodiacalSign(parseObject.get("birthday").toString());
                    }
                    else {
                        zodiacSign.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    private void getZodiacalSign(String birthdayValue) {
        ZodiacCalculator zodiacCalculator = new ZodiacCalculator(getApplicationContext());
        ZodiacSign zodiacSignEnum = zodiacCalculator.calculateZodiacSign(birthdayValue);

        if(zodiacSignEnum != null) {
            zodiacSign.setVisibility(View.VISIBLE);
            zodiacSign.setBackgroundResource(zodiacSignEnum.getImageId());
        }
    }

    private String getCurrentUser() {
        // ************************************* To Do ***************************** //
        // ************************ The User is Different if it's the Current User or Remote User ********************** //
        ParseUser currentUser = ParseUser.getCurrentUser();
        return currentUser.getUsername();
    }

    protected void onStartActivity(Class classToLoad, String extraName, String extraValue) {
        Intent intent = new Intent(this, classToLoad);
        if(extraName != null && extraName != null) {
            intent.putExtra(extraName, extraValue);
        }
        startActivity(intent);
    }

    public void startGalleryPhotosCounter() {
        new PhotosCounterAsyncTask().execute();
    }

    private class PhotosCounterAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                countUserImages();
            } catch (Exception e) {
                Log.e("formalchat", e.getMessage());
            }
            return "Executed";
        }
    }

    private void countUserImages() {
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("UserImages");
        parseQuery.whereEqualTo("userName", getCurrentUser());
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    photos_btn_counter = list.size();
                } else {
                    photos_btn_counter = 0;
                }

                updateUserImagesCounter();
            }
        });
    }

    private void updateUserImagesCounter() {
        String photosBtnTxt = getPhotosBtnTxt();
        photos_btn.setText(photosBtnTxt);
    }

    private String getPhotosBtnTxt() {
        String photosNum = String.valueOf(photos_btn_counter);
        switch (photos_btn_counter) {
            case 0: return "No photos";
            case 1: return photosNum + " photo";
            default: return photosNum + " photos";
        }
    }

    protected void setOnClickListeners() {
//        got_it_img.setOnClickListener(this);
        smallProfilePic.setOnClickListener(this);
        photos_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.got_it_img:
                // ****** Remote Profile Only ***** //
//                ((FrameLayout)help_video_leyout.getParent()).removeView(help_video_leyout);
//                startVideo();
//                break;
//            case R.id.small_prof_pic:
                // TO DO --------------------------------------------
//                if(isRemoteProfile()) {
//                    startVideo();
//                }
//                break;
            case R.id.photos_button:
                onStartActivity(ProfileGallery.class, null, null);
                break;
        }
    }

    protected void startVideo() {
        if(isVideoExists()) {
            downloadVideoIfNotExists();
            String videoPath = dir + filePath + shortName;
            onStartActivity(VideoShowActivity.class, "videoUri", videoPath);
        }
        else {
            Toast.makeText(getApplicationContext(), "Sorry, no video to show", Toast.LENGTH_SHORT).show();
        }
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

    public String getShortImageNameFromUri(String url) {
        return url.substring(url.lastIndexOf("-")+1);
    }

    private void startVideoDownloadService() {
        Intent intent = new Intent(this, VideoDownloadService.class);
        intent.putExtra(VideoDownloadService.DIRPATH, dir.getAbsolutePath());
        intent.putExtra(VideoDownloadService.FILEPATH, filePath);

        startService(intent);
    }

    private void getProfileImgPath() {
        if(user.has("profileImg")) {
            ParseFile pic = user.getParseFile("profileImg");
            profileImgPath = pic.getUrl();
        }
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

    private void populateInfoFromParse() {
        String currentUser = getCurrentUser();

        populateFromUserInfo(currentUser);
        populateFromQuestionary(currentUser);
    }

    private void populateFromUserInfo(String currentUser) {
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("UserInfo");
        parseQuery.whereEqualTo("loginName", currentUser);
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (ParseObject parseObject : objects) {
                        String name_p = parseObject.getString("name");
                        int gender_p = parseObject.getInt("gender");
                        int age_p = parseObject.getInt("age");

                        String motto_p = parseObject.getString("motto");
                        String location_p = parseObject.getString("location");

                        String height_p = parseObject.getString("height");
                        int bodyType_p = parseObject.getInt("bodyType");
                        int relationship_p = parseObject.getInt("relationship");
                        int children_p = parseObject.getInt("children");
                        int education_p = parseObject.getInt("education");
                        String aboutMe_p = parseObject.getString("aboutMe");
                        String perfect_smn_p = parseObject.getString("perfectSmn");
                        String perfect_date_p = parseObject.getString("perfectDate");
                        int interests_p = parseObject.getInt("interests");

                        name.setText(name_p);
                        age.setText(String.valueOf(age_p));

                        if (motto_p == null || "\t".equals(motto_p)) {
                            motto.setText(getResources().getString(R.string.motto));
                        } else {
                            motto.setText(motto_p);
                        }
                        location.setText(location_p);
                        if (height_p == null || height_p.equals("")) {
                            height.setText(getResources().getString(R.string.none_txt));
                        } else {
                            height.setText(height_p);
                        }

                        if (bodyType_p == 0) {
                            bodyType.setText(getResources().getString(R.string.none_txt));
                        } else {
                            bodyType.setText(getNameByPosition(getResources().getStringArray(R.array.body_type_values), bodyType_p));
                        }
                        if (relationship_p == 0) {
                            relationship.setText(getResources().getString(R.string.none_txt));
                        } else {
                            relationship.setText(getNameByPosition(getResources().getStringArray(R.array.relationship_values), relationship_p));
                        }
                        if (children_p == 0) {
                            children.setText(getResources().getString(R.string.none_txt));
                        } else {
                            children.setText(getNameByPosition(getResources().getStringArray(R.array.children_values), relationship_p));
                        }
                        if (education_p == 0) {
                            education.setText(getResources().getString(R.string.none_txt));
                        } else {
                            education.setText(getNameByPosition(getResources().getStringArray(R.array.education_values), education_p));
                        }
                        if (aboutMe_p == null || "\t".equals(aboutMe_p)) {
                            aboutMe.setText(getResources().getString(R.string.introduction_none_txt));
                        } else {
                            aboutMe.setText(aboutMe_p);
                        }
                        if (perfect_smn_p == null || "\t".equals(perfect_smn_p)) {
                            perfectSmn.setText(getResources().getString(R.string.perfectSmn_none_txt));
                        } else {
                            perfectSmn.setText(perfect_smn_p);
                        }
                        if (perfect_date_p == null || "\t".equals(perfect_date_p)) {
                            perfectDate.setText(getResources().getString(R.string.perfectDate_none_txt));
                        } else {
                            perfectDate.setText(perfect_date_p);
                        }
                        if (interests_p == 0) {
                            interests.setText(getResources().getString(R.string.none_txt));
                        } else {
                            interests.setText(getNameByPosition(getResources().getStringArray(R.array.interests_values), interests_p));
                        }

                        setSexIcon(gender_p);
                    }
                } else {
                    Log.e("formalchat", "Error: " + e.getMessage());
                }
            }
        });
    }

    private String getNameByPosition(String[] array, int position) {
        return array[position];
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

    private void populateFromQuestionary(String currentUser) {
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("UserQuestionary");
        parseQuery.whereEqualTo("loginName", currentUser);
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> listObjects, ParseException e) {
                if(e == null) {
                    for (ParseObject parseObject : listObjects) {
                        int drinking_p = parseObject.getInt("yourDrinking");
                        int smoking_p = parseObject.getInt("yourSmoking");
                        int religion_p = parseObject.getInt("yourReligion");
                        int ethnicity_p = parseObject.getInt("yourEthnicity");

                        if (drinking_p == 0) {
                            drinking.setText(getResources().getString(R.string.none_txt));
                        } else {
                            drinking.setText(getNameByPosition(getResources().getStringArray(R.array.a_your_drinking), drinking_p));
                        }
                        if (smoking_p == 0) {
                            smoking.setText(getResources().getString(R.string.none_txt));
                        } else {
                            smoking.setText(getNameByPosition(getResources().getStringArray(R.array.a_your_smoking), smoking_p));
                        }
                        if (religion_p == 0) {
                            religion.setText(getResources().getString(R.string.none_txt));
                        } else {
                            religion.setText(getNameByPosition(getResources().getStringArray(R.array.a_your_religion), religion_p));
                        }
                        if (ethnicity_p == 0) {
                            ethnicity.setText(getResources().getString(R.string.none_txt));
                        } else {
                            ethnicity.setText(getNameByPosition(getResources().getStringArray(R.array.ethnicity_values), ethnicity_p));
                        }
                    }
                }
                else {
                    Log.e("formalchat", "Error: " + e.getMessage());
                }
            }
        });
    }

    protected void setUserInfoToExtras() {
        SharedPreferences sharedInfoPreferences = getSharedPreferences(PREFS_INFO, 0);
        SharedPreferences.Editor editor = sharedInfoPreferences.edit();

        editor.putString("name", getRightText(name.getText().toString()));
        if(isMale) {
            editor.putString("gender", getNameByPosition(getResources().getStringArray(R.array.gender_values), 0));
        }
        else {
            editor.putString("gender", getNameByPosition(getResources().getStringArray(R.array.gender_values), 1));
        }
        editor.putString("age", getRightText(age.getText().toString()));


        editor.putString("motto", getRightText(motto.getText().toString()));
        editor.putString("location", getRightText(location.getText().toString()));
        editor.putString("yourDrinking", getRightText(drinking.getText().toString()));
        editor.putString("yourSmoking", getRightText(smoking.getText().toString()));
        editor.putString("yourReligion", getRightText(religion.getText().toString()));
        editor.putString("height", getRightText(height.getText().toString()));
        editor.putString("bodyType", getRightText(bodyType.getText().toString()));
        editor.putString("relationship", getRightText(relationship.getText().toString()));
        editor.putString("children", getRightText(children.getText().toString()));
        editor.putString("yourEthnicity", getRightText(ethnicity.getText().toString()));
        editor.putString("education", getRightText(education.getText().toString()));
        editor.putString("aboutMe", getRightText(aboutMe.getText().toString()));
        editor.putString("perfectSmn", getRightText(perfectSmn.getText().toString()));
        editor.putString("perfectDate", getRightText(perfectDate.getText().toString()));
        editor.putString("interests", getRightText(interests.getText().toString()));

//        editor.putString("interestedIn", getRightText(interestedIn.getText().toString()));
//        editor.putString("lookingFor", getRightText(lookingFor.getText().toString()));
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
