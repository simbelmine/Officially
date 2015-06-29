package com.android.formalchat;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.formalchat.profile.ProfileGallery;
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
                showVideoUploadedNotification(R.string.video_upload_notif_title, R.string.video_upload_notif_text_warning);
            }

        } catch (IOException e) {
            Log.e("formalchat", e.getMessage());
        }
    }

    private void saveInBackground(ParseUser user, ParseFile parseFile) {
        Log.v("formalchat", "saveInBackground...");
        user.put("video", parseFile);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.v("formalchat", "saveInBackground...in...");
                    onDoneSaveTransaction();
                }
                else {
                    Log.e("formalchat", e.getMessage());
                    showVideoUploadedNotification(R.string.video_upload_notif_title, R.string.video_upload_notif_text_warning);
                    return;
                }
            }
        });
    }

    private void showVideoUploadedNotification(int titleId, int textId) {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setContentTitle(getResources().getString(titleId))
                .setContentText(getResources().getString(textId))
                .setSmallIcon(R.drawable.ic_launcher)
                .setOngoing(false)
                .setContentIntent(getPendingIntent());
        notificationManager.notify(ID, notificationBuilder.build());
    }

    private PendingIntent getPendingIntent() {
        Intent resultIntent = new Intent(this, ProfileGallery.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        return pendingIntent;
    }

    private void onDoneSaveTransaction() {
        Log.e("formalchat", "Video was saved Successfully !");
        showVideoUploadedNotification(R.string.video_upload_notif_title, R.string.video_upload_notif_text);
        moveVideoToUsableFolder();
        sendBroadcastMessage();
    }

    private void moveVideoToUsableFolder() {
        File startFolder = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/.formal_chat/video_in/" + out_videoName);
        File destinationFolder = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/.formal_chat/" + out_videoName);

        if(startFolder.exists() && destinationFolder.exists()) {
            startFolder.renameTo(destinationFolder);
        }
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
