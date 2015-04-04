package com.android.formalchat;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_show_layout);

        init();

        ParseUser user = ParseUser.getCurrentUser();
        videoFile = user.getParseFile("video");
        fileName = videoFile.getName();
        tmpFile = new File(dir, filePath + fileName);
        if(tmpFile.exists()) {
            loadVideo();
        }
        else {
            new LongDownload().execute("");
        }
    }

    private void init() {
        videoView = (VideoView) findViewById(R.id.video);
    }

    private class LongDownload extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            downloadVideo();
            return "Downloaded";
        }

        @Override
        protected void onPostExecute(String s) {
            loadVideo();
        }
    }

    private void downloadVideo() {
        final FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(tmpFile);
            videoFile.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] bytes, ParseException e) {
                    if(e == null) {
                        try {
                            fileOutputStream.write(bytes);
                            fileOutputStream.flush();
                            fileOutputStream.close();
                        } catch (IOException ex) {
                            Log.v("formalchat", ex.getMessage());
                        }
                    }
                }
            });
        }
        catch (IOException ex){
            Log.v("formalchat", ex.getMessage());
        }
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
