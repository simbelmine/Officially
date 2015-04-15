package com.android.formalchat;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.api.GoogleApiClient;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Sve on 4/2/15.
 */
public class VideoRecordActivity extends Activity implements View.OnClickListener{
    static final int REQUEST_VIDEO_CAPTURE = 1;
    private Button startRecordingBtn;
    private String videoName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_record_layout);

        init();
        setOnclickListeners();
    }

    private void init() {
        startRecordingBtn = (Button) findViewById(R.id.start_video_btn);
    }

    private void setOnclickListeners() {
        startRecordingBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_video_btn:
                dispatchTakeVideoIntent();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            File videoFile = new File(videoUri.getPath());
            saveVideoToParse(videoFile);
        }
    }

    private void saveVideoToParse(File videoFile) {
        ParseUser user = ParseUser.getCurrentUser();
        try {
            byte[] data = FileUtils.readFileToByteArray(videoFile);//Convert any file, image or video into byte array
            ParseFile parseFile = new ParseFile(videoName, data);
            user.put("video", parseFile);
            user.saveInBackground();

        } catch (IOException e) {
            Log.e("formalchat", e.getMessage());
        }
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            // Performing this check is important because if you call startActivityForResult()
            // using an intent that no app can handle, your app will crash.
            // So as long as the result is not null, it's safe to use the intent.
            Uri videoUri = Uri.fromFile(getMediaFileUri());
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    private File getMediaFileUri() {
        File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());
        // Create the storage directory if it does not exist
        if (! root.exists()){
            root.mkdirs();
        }
        // Create a media file name
        videoName = "VID_"+ getTimeStamp();
        File mediaFile = new File(root, videoName);
        return mediaFile;
    }

    private static String getTimeStamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }
}
