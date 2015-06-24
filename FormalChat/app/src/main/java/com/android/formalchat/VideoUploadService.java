package com.android.formalchat;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.PrefixFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by Sve on 6/24/15.
 */
public class VideoUploadService extends IntentService {
    public static final String ACTION = "VideoUpload";
    private static final int ID = 1;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private String destinationFolder;
    private String out_videoName;

    public VideoUploadService() {
        super("VideoUploadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        destinationFolder = intent.getStringExtra("destinationFolder");
        out_videoName = intent.getStringExtra("out_videoName");

        File videoFile = getVideoFile(destinationFolder);
        saveVideoToParse(videoFile, out_videoName);
    }

    private File getVideoFile(String destinationFolder) {
        Collection<File> files =  FileUtils.listFiles(new File(destinationFolder), new PrefixFileFilter("out"), null);
        if(files.size() != 0) {
            return (File)files.toArray()[0];
        }

        return null;
    }

    private void saveVideoToParse(File videoFile, String videoName) {
        ParseUser user = ParseUser.getCurrentUser();
        try {
            byte[] data = FileUtils.readFileToByteArray(videoFile); //Convert any file, image or video into byte array
            ParseFile parseFile = new ParseFile(videoName, data);

            if(isNetworkAvailable()) {
                saveInBackground(user, parseFile);
            }
            else {
                saveEventually(user, parseFile);
            }

        } catch (IOException e) {
            Log.e("formalchat", e.getMessage());
        }
    }

    private void saveInBackground(ParseUser user, ParseFile parseFile) {
        Log.v("formalchat", "saveInBackground...");
        user.put("video", parseFile);
        showUploadNotification();
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.v("formalchat", "saveInBackground...in...");
                    onDoneSaveTransaction();
                }
            }
        });
    }

    private void saveEventually(final ParseUser user, final ParseFile parseFile) {
        user.saveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null) {
                    user.put("video", parseFile);
                    user.saveInBackground();
                }
            }
        });
    }

    private void showUploadNotification() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setContentTitle("Video Upload")
                .setContentText("Download in progress")
                .setSmallIcon(R.drawable.ic_launcher)
                .setOngoing(true);

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        int progress;
                        // Do the operation 20 times
                        for (progress = 0; progress <= 100; progress+= 5) {
                            // Set the progress indicator to (max value, current completition percentage, determinate state)
                            notificationBuilder.setProgress(100, progress, true);
                            notificationManager.notify(ID, notificationBuilder.build());
                        }
                    }
                }
        ).start();
    }

    private void hideUploadNotification() {
        notificationBuilder.setContentText("Upload complete")
                .setOngoing(false)
                .setProgress(0, 0, false);
        notificationManager.notify(ID, notificationBuilder.build());
    }

    private void onDoneSaveTransaction() {
        Log.e("formalchat", "Video was saved Successfully !");
        //hide notification for uploading - or just show error on the same notification
        hideUploadNotification();
        sendBroadcastMessage();
    }

    private void sendBroadcastMessage() {
        Intent intent = new Intent(ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
