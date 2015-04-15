package com.android.formalchat;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Sve on 4/6/15.
 */
public class VideoDownloadService extends IntentService {
    private int result = Activity.RESULT_CANCELED;
    public static final String DIRPATH = "dirpath";
    public static final String FILEPATH = "filepath";
    public static final String RESULT = "result";
    public static final String NOTIFICATION = "com.android.formalchat.service.receiver";
    private ParseUser user;
    private String dirPath;
    private String filePath;


    public VideoDownloadService() {
        super("VideoDownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        dirPath = intent.getStringExtra(DIRPATH);
        filePath = intent.getStringExtra(FILEPATH);
        user = ParseUser.getCurrentUser();


        ParseFile videoFile = getFileFromParse();
        String fileName = getParseFileName(videoFile);
        File tmpFile = new File(dirPath + filePath + fileName);

        downloadVideo(videoFile, tmpFile);
    }

    private ParseFile getFileFromParse() {
        return user.getParseFile("video");
    }

    private String getParseFileName(ParseFile file) {
        return file.getName();
    }

    private void downloadVideo(ParseFile videoFile, File tmpFile) {
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
                        result = Activity.RESULT_OK;
                    }
                    else {
                        Log.e("formalchat", e.getMessage());
                    }

                    publishResults(result);
                }
            });
        }
        catch (IOException ex){
            Log.v("formalchat", ex.getMessage());
        }
    }

    private void publishResults(int result) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(RESULT, result);
        sendBroadcast(intent);
    }
}
