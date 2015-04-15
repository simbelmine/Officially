package com.android.formalchat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.File;

/**
 * Created by Sve on 4/3/15.
 */
public class VideoShowActivity extends Activity {
    private VideoView videoView;
    private File dir = Environment.getExternalStorageDirectory();
    private String filePath = "/formal_chat/";
    private String fileName;
    private File tmpFile;
    private ParseFile videoFile;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_show_layout);

        init();
        initBroadcastReceiver();

        ParseUser user = ParseUser.getCurrentUser();
        videoFile = user.getParseFile("video");
        fileName = videoFile.getName();

        File targetFolder = new File(dir + filePath);
        if(!targetFolder.exists()) {
            targetFolder.mkdir();
        }

        tmpFile = new File(dir, filePath + fileName);
        if(tmpFile.exists()) {
            loadVideo();
        }
        else {
            startVideoDownloadService();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(VideoDownloadService.NOTIFICATION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    private void init() {
        videoView = (VideoView) findViewById(R.id.video);
    }

    private void initBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                if(bundle != null) {
                    int resultCode = bundle.getInt(VideoDownloadService.RESULT);
                    if(resultCode == RESULT_OK) {
                        loadVideo();
                    }
                    else {
                        Log.e("formalchat", "DoWnLoAd Failed .... !!!");
                    }
                }
            }
        };
    }

    private void startVideoDownloadService() {
        Intent intent = new Intent(this, VideoDownloadService.class);
        intent.putExtra(VideoDownloadService.DIRPATH, dir.getAbsolutePath());
        intent.putExtra(VideoDownloadService.FILEPATH, filePath);

        startService(intent);
    }


    private void loadVideo() {
        Uri uri = Uri.parse(dir+filePath+fileName);

        MediaController mediaController = new MediaController(VideoShowActivity.this);
        mediaController.setAnchorView(videoView);
        videoView.setVideoURI(uri);
        videoView.requestFocus();
        videoView.setMediaController(mediaController);
        videoView.start();
    }
}
