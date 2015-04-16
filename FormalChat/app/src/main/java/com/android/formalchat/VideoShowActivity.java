package com.android.formalchat;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;

/**
 * Created by Sve on 4/3/15.
 */
public class VideoShowActivity extends Activity {
    private VideoView videoView;
    private File tmpFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_show_layout);

        init();
        Uri uri = Uri.parse(getIntent().getStringExtra("videoUri"));

        tmpFile = new File(uri.toString());
        if(tmpFile.exists()) {
            loadVideo(uri);
        }
    }

    private void init() {
        videoView = (VideoView) findViewById(R.id.video);
    }

    private void loadVideo(Uri uri) {
        MediaController mediaController = new MediaController(VideoShowActivity.this);
        mediaController.setAnchorView(videoView);
        videoView.setVideoURI(uri);
        videoView.requestFocus();
        videoView.setMediaController(mediaController);
        seekTo(videoView, 100);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener()  {
                    @Override
                    public void onSeekComplete(MediaPlayer mp) {
                        videoView.pause();
                    }
                });
            }
        });
    }

    public static void seekTo(VideoView v, int pos) {
        v.start();
        v.seekTo(pos);
    }
}

