package com.android.formalchat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Sve on 3/10/15.
 */
public class FullImageActivity extends Activity {
    private static final String PREFS_NAME = "FormalChatPrefs";
    private static final int CROP_FROM_IMG = 123;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ImageView fullScreenView;
    private RelativeLayout topBtnsLayout;
    private ImageView backBtn;
    private ImageView menuBtn;
    private boolean isVisible = true;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initActionBar();
        setContentView(R.layout.full_screen_layout);

        sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        editor = sharedPreferences.edit();
        Intent i = getIntent();
        url = i.getExtras().getString("url");

        fullScreenView = (ImageView) findViewById(R.id.full_screen_img);
        Picasso.with(this)
                .load(url)
                .into(fullScreenView);

        topBtnsLayout = (RelativeLayout) findViewById(R.id.top_btns_layout);
        backBtn = (ImageView) findViewById(R.id.back_btn);
        menuBtn = (ImageView) findViewById(R.id.menu_btn);

        fullScreenView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isVisible) {
//                    topBtnsLayout.setVisibility(View.VISIBLE);
//                    backBtn.setVisibility(View.VISIBLE);
//                    menuBtn.setVisibility(View.VISIBLE);
                    getActionBar().hide();
                    isVisible = true;
                }
                else {
//                    topBtnsLayout.setVisibility(View.GONE);
//                    backBtn.setVisibility(View.GONE);
//                    menuBtn.setVisibility(View.GONE);
                    getActionBar().show();
                    isVisible = false;
                }
            }
        });

//        backBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });

//        menuBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                PopupMenu popupMenu = new PopupMenu(FullImageActivity.this, menuBtn);
//                popupMenu.getMenuInflater().inflate(R.menu.pop_up, popupMenu.getMenu());
//                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                    @Override
//                    public boolean onMenuItemClick(MenuItem item) {
//                        switch (item.getItemId()) {
//                            case R.id.delete:
//                                deleteImage();
//                                return true;
//                            case R.id.set_as:
//                                startCropActivity();
//                                return true;
//                            default:
//                                return false;
//                        }
//                    }
//                });
//                popupMenu.show();
//            }
//        });
    }

    private void initActionBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.transp_black_20)));
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        getActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data != null) {
            if(resultCode == RESULT_OK) {
                byte[] profileImg = data.getByteArrayExtra("profileImg");
                saveProfileImgToLocal(profileImg);
                setImageAsProfile(profileImg);
                setFlagToSharedPrefs();
                setProfPicNameToSharPrefs();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.full_img_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                deleteImage();
                return true;
            case R.id.set_as:
                startCropActivity();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return false;
        }
    }

    private void saveProfileImgToLocal(byte[] profileImg) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(profileImg, 0, profileImg.length);
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.formal_chat";
        BlurredImage bm = new BlurredImage();
        Bitmap blurredBitmap = bm.getBlurredImage(bitmap, 50);

        File dir = new File(path);
        File profilePic = new File(dir, "blurred_profile.jpg");
        try {
            FileOutputStream out = new FileOutputStream(profilePic);
            blurredBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        }
        catch (IOException ex) {
            Log.e("formalchat", ex.getMessage());
        }
    }

    private void setProfPicNameToSharPrefs() {
        editor.putString("profPic", url);
        editor.putString("profPicName", getShortImageNameFromUri());
        editor.commit();
    }

    private void startCropActivity() {
        Intent intent = new Intent(FullImageActivity.this, CropActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // NB: if it starts with FLAG_ACTIVITY_NEW_TASK it cannot start it for result
        intent.putExtra("url", url);
        startActivityForResult(intent, CROP_FROM_IMG);
    }

    private void setImageAsProfile(byte[] profileImg) {
        ParseUser user = ParseUser.getCurrentUser();
        ParseFile parseProfImg = new ParseFile(profileImg);
        user.put("profileImg", parseProfImg);
        user.put("profileImgName", getShortImageNameFromUri());
        user.saveInBackground();
    }

    private void deleteImage() {
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("UserImages");
        parseQuery.whereEqualTo("photo", getParseImgNameFromUri());

        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> imagesList, ParseException e) {
                if(e == null) {
                    if(imagesList.size() > 0) {
                        imagesList.get(0).deleteInBackground();
                        deleteImageFromLocalStorage();
                        setFlagToSharedPrefs();
                        finish();
                    }
                }
                else {
                    Log.e("formalchat", "Delete command: " + e.getMessage());
                }
            }
        });
    }

    private void deleteImageFromLocalStorage() {
        File dir = new File(Environment.getExternalStorageDirectory() + "/.formal_chat");
        File[] dirImages = dir.listFiles();

        if(dirImages.length != 0) {
            for(File img : dirImages) {
                if(getShortImageNameFromUri().equals(img.getName())) {
                    img.delete();
                    return;
                }
            }
        }
    }

    private void setFlagToSharedPrefs() {
        editor.putBoolean("refresh", true);
        editor.putBoolean("photo_num_changed", true);
        editor.commit();
    }

    public String getParseImgNameFromUri() {
        return url.substring(url.lastIndexOf("/")+1);
    }

    public String getShortImageNameFromUri() {
        return url.substring(url.lastIndexOf("-")+1);
    }
}
