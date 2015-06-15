package com.android.formalchat;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.netcompss.ffmpeg4android.GeneralUtils;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.PrefixFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by Sve on 4/2/15.
 */
public class VideoRecordActivity extends Activity implements View.OnClickListener{
    private static final int REQUEST_VIDEO_CAPTURE = 1;
    private static final String VIDEO_EXTENSION = ".mp4";
    private Button startRecordingBtn;

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
        String destinationFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.formal_chat/";
        String videoName;

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            videoName = getVideoName(videoUri);
            compressVideo(destinationFolder, videoName);
            showCompleteMessage();
            finish();
        }
    }

    private void showCompleteMessage() {
        Toast.makeText(this, "Your Video will appear shortly in your Gallery.", Toast.LENGTH_LONG).show();
    }

    private String getVideoName(Uri videoUri) {
        String videoUrl = videoUri.getPath();
        return videoUrl.substring(videoUrl.lastIndexOf("/")+1);
    }

    private void compressVideo(String startFolder, String videoName) {
        Intent intent = new Intent(this, VideoCompressService.class);
        intent.putExtra("destinationFolder", startFolder);
        intent.putExtra("videoName", videoName);

        GeneralUtils.copyLicenseFromAssetsToSDIfNeeded(this, startFolder);

        startService(intent);
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
        File root = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/.formal_chat/");
        // Create the storage directory if it does not exist
        if (! root.exists()){
            root.mkdirs();
        }
        // Create a media file name
        String videoName = "VID_intro";
        File mediaFile = new File(root, videoName + VIDEO_EXTENSION);
        return mediaFile;
    }

    private static String getTimeStamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }
}
