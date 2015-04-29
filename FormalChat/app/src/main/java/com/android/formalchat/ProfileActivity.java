package com.android.formalchat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sve on 2/4/15.
 */
public class ProfileActivity extends DrawerActivity implements View.OnClickListener {
    private static final String PREFS_NAME = "FormalChatPrefs";
    private static final String PREFS_INFO = "FormalChatUserInfo";
    public static final int NONE = 101;
    private SharedPreferences sharedPreferences;
    private ProfileAddImageDialog addImgWithDialog;
    private DrawerLayout drawerLayout;
    private String profileImgPath;
    private LinearLayout exclamationLayout;
    private ImageButton edit_feb_btn;
    private ParseUser user;
    private ArrayList<String> imagePaths;
    private Activity activity;
    private boolean videoExists;
    private ImageView profilePic;
    private RoundedImageView smallProfilePic;
    private ImageView sexIcon;
    private  boolean isMale;

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
        View contentView = inflater.inflate(R.layout.profile, null, false);
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
        getProfileImgPath();
        if(isNetworkAvailable()) {
            loadBigProfilePicFromParse();
            loadSmallProfilePicFromParse();
        }
        initVideoWarningMessage();
        addViewListeners();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (videoExists) {
            exclamationLayout.setVisibility(View.INVISIBLE);
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

    private void init() {
        // *** Header
        exclamationLayout = (LinearLayout) findViewById(R.id.exclamation_layout);
        edit_feb_btn = (ImageButton) findViewById(R.id.feb_button);
        profilePic = (ImageView) findViewById(R.id.profile_pic);
        smallProfilePic = (RoundedImageView) findViewById(R.id.small_prof_pic);
        sexIcon = (ImageView) findViewById(R.id.sex_icon);
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

    private void initVideoWarningMessage() {
        if(!videoExists) {
            exclamationLayout.setVisibility(View.VISIBLE);
        }
        else {
            exclamationLayout.setVisibility(View.INVISIBLE);
        }
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

    private void addViewListeners() {
        exclamationLayout.setOnClickListener(this);
        edit_feb_btn.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(isNetworkAvailable()) {
            initVideoWarningMessage();
            populateInfoFromParse();
        }
        else {
            // Get from local
            //getImagesFromLocalStorage();
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.exclamation_layout:
                startActivity(VideoRecordActivity.class);
                break;
            case R.id.feb_button:
                PopupMenu popupMenu = new PopupMenu(ProfileActivity.this, edit_feb_btn);
                popupMenu.getMenuInflater().inflate(R.menu.profile_edit_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        return onItemClicked(item);
                    }
                });
                popupMenu.show();
        }
    }

    private boolean onItemClicked(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_profile:
                startActivity(UserInfoActivity.class);
                return true;
            case R.id.view_gallery:
                startActivity(ProfileViewGallery.class);
                return true;
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
                startActivity(VideoRecordActivity.class);
                return true;
            default:
                return false;
        }
    }

    private void startActivity(Class classToLoad) {
        Intent intent = new Intent(this, classToLoad);
        startActivity(intent);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void loadBigProfilePicFromParse() {
        if(user.has("profileImgName")) {
            final String profileImgName = user.get("profileImgName").toString();
            ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("UserImages");
            parseQuery.whereEqualTo("userName", getCurrentUser());
            parseQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {
                    if(list.size() > 0) {
                        for(ParseObject po : list) {
                            String picUrl = ((ParseFile) po.get("photo")).getUrl();
                            String nameFromUrl = getShortImageNameFromUri(picUrl);

                            if(profileImgName.equals(nameFromUrl)) {
                                ParseFile parseFile = ((ParseFile) po.get("photo"));
                                parseFile.getDataInBackground(new GetDataCallback() {
                                    @Override
                                    public void done(byte[] bytes, ParseException e) {
                                        if (e == null) {
                                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                            Bitmap blurredBitmap = fastblur(bmp, 50);
                                            profilePic.setImageBitmap(blurredBitmap);
                                        }
                                    }
                                });


                                //Picasso.with(getApplicationContext()).load(picUrl).into(profilePic);

                            }
                        }
                    }
                }
            });
        }
    }

    public Bitmap fastblur(Bitmap sentBitmap, int radius) {

        // Stack Blur v1.0 from
        // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
        //
        // Java Author: Mario Klingemann <mario at quasimondo.com>
        // http://incubator.quasimondo.com
        // created Feburary 29, 2004
        // Android port : Yahel Bouaziz <yahel at kayenko.com>
        // http://www.kayenko.com
        // ported april 5th, 2012

        // This is a compromise between Gaussian Blur and Box blur
        // It creates much better looking blurs than Box Blur, but is
        // 7x faster than my Gaussian Blur implementation.
        //
        // I called it Stack Blur because this describes best how this
        // filter works internally: it creates a kind of moving stack
        // of colors whilst scanning through the image. Thereby it
        // just has to add one new block of color to the right side
        // of the stack and remove the leftmost color. The remaining
        // colors on the topmost layer of the stack are either added on
        // or reduced by one, depending on if they are on the right or
        // on the left side of the stack.
        //
        // If you are using this algorithm in your code please add
        // the following line:
        //
        // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
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

                        motto.setText(motto_p);
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
                        if(aboutMe_p == null) {
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
        editor.putString("motto", motto.getText().toString());
        editor.putString("name", name.getText().toString());
        if(isMale) {
            editor.putString("gender", getNameByPosition(getResources().getStringArray(R.array.gender_values), 0));
        }
        else {
            editor.putString("gender", getNameByPosition(getResources().getStringArray(R.array.gender_values), 1));
        }
        editor.putString("age", age.getText().toString());
        editor.putString("location", location.getText().toString());
        editor.putString("interestedIn", interestedIn.getText().toString());
        editor.putString("lookingFor", lookingFor.getText().toString());
        editor.putString("aboutMe",aboutMe.getText().toString());
        editor.putString("relationship", relationship.getText().toString());
        editor.putString("bodyType", bodyType.getText().toString());
        editor.putString("ethnicity", ethnicity.getText().toString());
        editor.putString("interests", interests.getText().toString());
        editor.commit();
    }
}
