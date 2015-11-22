package com.android.formalchat;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.File;

/**
 * Created by Sve on 4/3/15.
 */
public class VideoShowActivity extends Activity {
    private static final String FILE_DIR = "/.formal_chat/";
    private File dir = Environment.getExternalStorageDirectory();
    private VideoView videoView;
    private File tmpFile;
    private TextView textViewMessage;
    private ProgressBar videoProgressBar;
    private ParseUser user;
    private String fullVideoFileName;
    private String shortVideoFileName;
    private MediaController mediaController;
    private boolean isFullScreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initWindowView();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_show_layout);
        user = ParseUser.getCurrentUser();


        init();
        hideTextViewMessage();

        fullVideoFileName = getFullVideoFileName();
        shortVideoFileName = fullVideoFileName != null ? getShortFileNameFromUri(fullVideoFileName) : null;


        if(isVideoExistsOnServer()) {
            if(!isVideoFileExists()) {
                downloadVideo();
            }
            else {
//                Uri uri = Uri.parse(getIntent().getStringExtra("videoUri"));
//                if (isURL(uri)) {
//                    loadVideo(uri);
//                }

                String videoPath = dir + FILE_DIR + shortVideoFileName;
                Uri uri = Uri.fromFile(new File(videoPath));
                loadVideo(uri);
            }
        }
        else {
            showTextViewMessage();
        }




//        if (getIntent().hasExtra("videoUri")) {
//            Uri uri = Uri.parse(getIntent().getStringExtra("videoUri"));
//
//
//            tmpFile = new File(uri.toString());
//            if (tmpFile.exists() || isURL(uri)) {
//                loadVideo(uri);
//            }
//        }
//        else {
//            showTextViewMessage();
//        }
    }

    private void initWindowView() {
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
    }

    private boolean isURL(Uri uri) {
        String httpString = "http";
        String stringToCompare = uri.toString().substring(0,4);

        if(httpString.equals(stringToCompare)){
            return true;
        }
        return false;
    }

    private void init() {
        videoView = (VideoView) findViewById(R.id.video);
        textViewMessage = (TextView) findViewById(R.id.vide_warning);
        videoProgressBar = (ProgressBar) findViewById(R.id.show_video_progress);
        videoProgressBar.setVisibility(View.VISIBLE);
    }

    private void loadVideo(Uri uri) {
        try {
            mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            videoView.setVideoURI(uri);
            videoView.requestFocus();
            videoView.setMediaController(mediaController);
            videoProgressBar.setVisibility(View.INVISIBLE);
            videoView.setVisibility(View.VISIBLE);
            videoView.start();
        }
        catch (Exception ex) {
            videoProgressBar.setVisibility(View.INVISIBLE);
            ex.printStackTrace();
            Log.e(ApplicationOfficially.TAG, "*** Error : " + ex.getMessage());
        }
    }

    private void downloadVideo() {
        File targetFolder = new File(dir + FILE_DIR);
        if (!targetFolder.exists()) {
            targetFolder.mkdir();
        }

        File tmpFile = new File(dir, FILE_DIR + shortVideoFileName);
        if (!tmpFile.exists()) {
            startVideoDownloadService();
        }
    }

    private void startVideoDownloadService() {
        Intent intent = new Intent(this, VideoDownloadService.class);
        intent.putExtra(VideoDownloadService.DIRPATH, dir.getAbsolutePath());
        intent.putExtra(VideoDownloadService.FILEPATH, FILE_DIR);

        startService(intent);
    }

    private String getFullVideoFileName() {
        if(user != null) {
            ParseFile videoFile = user.getParseFile("video");
            return videoFile.getName();
        }
        return null;
    }

    public String getShortFileNameFromUri(String url) {
        return url.substring(url.lastIndexOf("-")+1);
    }

    private boolean isVideoExistsOnServer() {
        if(user != null) {
            if (user.containsKey("video")) {
                Log.v(ApplicationOfficially.TAG, " Yes: video Exists on Server !");
                return true;
            }
        }
        return false;
    }

    private boolean isVideoFileExists() {
        if(shortVideoFileName != null) {
            String videoPath = dir + FILE_DIR + shortVideoFileName;
            Log.e(ApplicationOfficially.TAG, " videoPath = " + videoPath);
            if (new File(videoPath).exists()) {
                Log.v(ApplicationOfficially.TAG, " Yes: video File Exists !");
                return true;
            }
        }
        return false;
    }

    private void hideTextViewMessage() {
        textViewMessage.setVisibility(View.GONE);
    }

    private void showTextViewMessage() {
        textViewMessage.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }
}

