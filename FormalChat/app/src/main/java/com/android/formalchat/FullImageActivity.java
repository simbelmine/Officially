package com.android.formalchat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

/**
 * Created by Sve on 3/10/15.
 */
public class FullImageActivity extends Activity {
    private static final String PREFS_NAME = "FormalChatPrefs";
    private ImageView fullScreenView;
    private RelativeLayout topImgLayout;
    private ImageButton backBtn;
    private ImageButton menuBtn;
    private boolean isVisible = false;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.full_screen_layout);
        Intent i = getIntent();
        url = i.getExtras().getString("url");

        fullScreenView = (ImageView) findViewById(R.id.full_screen_img);
        Picasso.with(this)
                .load(url)
                .into(fullScreenView);

        topImgLayout = (RelativeLayout) findViewById(R.id.top_btns_layout);
        backBtn = (ImageButton) findViewById(R.id.back_btn);
        menuBtn = (ImageButton) findViewById(R.id.menu_btn);

        fullScreenView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isVisible) {
                    topImgLayout.setVisibility(View.VISIBLE);
                    backBtn.setVisibility(View.VISIBLE);
                    menuBtn.setVisibility(View.VISIBLE);
                    isVisible = true;
                }
                else {
                    topImgLayout.setVisibility(View.GONE);
                    backBtn.setVisibility(View.GONE);
                    menuBtn.setVisibility(View.GONE);
                    isVisible = false;
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(FullImageActivity.this, menuBtn);
                popupMenu.getMenuInflater().inflate(R.menu.pop_up, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getOrder()) {
                            case 0:
                                //Put Metod to delete from Local and from Parse
                                deleteImageFromParse();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();
            }
        });
    }

    private void deleteImageFromParse() {
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("UserImages");
        parseQuery.whereEqualTo("photo", getImageNameFromUri());

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
        File dir = new File(Environment.getExternalStorageDirectory() + "/formal_chat");
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
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("refreshOnDelete", true);
        editor.commit();
    }

    private String getImageNameFromUri() {
        return url.substring(url.lastIndexOf("/")+1);
    }

    private String getShortImageNameFromUri() {
        return url.substring(url.lastIndexOf("-")+1);
    }
}
