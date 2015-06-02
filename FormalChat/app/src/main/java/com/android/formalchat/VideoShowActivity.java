package com.android.formalchat;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_show_layout);

        init();
        hideTextViewMessage();
        if(getIntent().hasExtra("videoUri")) {
            Uri uri = Uri.parse(getIntent().getStringExtra("videoUri"));
            Log.v("formalchat", "### videoUri = " + uri.toString());

            tmpFile = new File(uri.toString());
            if (tmpFile.exists()) {
                loadVideo(uri);
            }
        }
        else {
            showTextViewMessage();
        }
    }

    private void init() {
        videoView = (VideoView) findViewById(R.id.video);
        textViewMessage = (TextView) findViewById(R.id.vide_warning);
    }

    private void loadVideo(Uri uri) {
        MediaController mediaController = new MediaController(VideoShowActivity.this);
        mediaController.setAnchorView(videoView);
        videoView.setVideoURI(uri);
        videoView.requestFocus();
        videoView.setMediaController(mediaController);
        videoView.start();
    }

    private void hideTextViewMessage() {
        textViewMessage.setVisibility(View.GONE);
    }

    private void showTextViewMessage() {
        textViewMessage.setVisibility(View.VISIBLE);
    }
}

