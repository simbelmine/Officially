package com.android.formalchat;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;

/**
 * Created by Sve on 4/3/15.
 */
public class VideoShowActivity extends Activity {
    private VideoView videoView;
    private File tmpFile;
    private TextView textViewMessage;
    private ProgressBar videoProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_show_layout);

        init();
        hideTextViewMessage();
        if(getIntent().hasExtra("videoUri")) {
            Uri uri = Uri.parse(getIntent().getStringExtra("videoUri"));

            tmpFile = new File(uri.toString());
            if (tmpFile.exists() || isURL(uri)) {
                loadVideo(uri);
            }
        }
        else {
            showTextViewMessage();
        }
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
            MediaController mediaController = new MediaController(VideoShowActivity.this);
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
        }
    }

    private void hideTextViewMessage() {
        textViewMessage.setVisibility(View.GONE);
    }

    private void showTextViewMessage() {
        textViewMessage.setVisibility(View.VISIBLE);
    }
}

