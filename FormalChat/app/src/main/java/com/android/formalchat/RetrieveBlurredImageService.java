package com.android.formalchat;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Sve on 7/2/15.
 */
public class RetrieveBlurredImageService extends IntentService {
    private static final String FILE_DIR = "/.formal_chat/";
    private static final String PROFILE_PIC_BLURRED = "blurred_profile.jpg";
    private static final String PROFILE_PIC_BLURRED_REMOTE = "blurred_profile_remote.jpg";
    public static final String ACTION="DOWNLOADING_BIG_PROFILE_PIC";
    private static final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + FILE_DIR;
    private ParseUser user;
    private Bitmap bitmapToBlure;
    private Bundle bundle;


    public RetrieveBlurredImageService() {
        super("RetrieveBlurredImageService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        bundle = intent.getExtras();
        getParseUserByName();
    }

    private void getParseUserByName() {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        if(bundle != null && bundle.containsKey("remoteUserName") && bundle.getString("remoteUserName") != null && !bundle.getString("remoteUserName").equals("")) {
            query.whereEqualTo("username", bundle.getString("remoteUserName"));
            query.getFirstInBackground(new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser parseUser, ParseException e) {
                    if(e == null) {
                        user = parseUser;
                        retrieveBlurredImage();
                    }
                }
            });
        }
        else {
            user = ParseUser.getCurrentUser();
            retrieveBlurredImage();
        }
    }

    private void retrieveBlurredImage() {
        if (user.has("profileImgName")) {
            final String profileImgName = user.get("profileImgName").toString();
            ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("UserImages");
            parseQuery.whereContains("userName", user.getUsername());
            parseQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {
                    if (e == null && list.size() > 0) {
                        for (ParseObject po : list) {
                            String shortPhotoName = getShortImageNameFromUri(po.getParseFile("photo").getName());
                            if (shortPhotoName.equals(profileImgName)) {
                                po.getParseFile("photo").getDataInBackground(new GetDataCallback() {
                                    @Override
                                    public void done(byte[] bytes, ParseException e) {
                                        if (e == null && bytes.length > 0) {
                                            bitmapToBlure = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                            if (bitmapToBlure != null) {
                                                new BlureImageTask().execute();
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    } else {
                        sendBroadcastMessage(false);
                    }
                }
            });
        }
        else {
            sendBroadcastMessage(false);
        }
    }

    private class BlureImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            BlurredImage bm = new BlurredImage();
            Bitmap bitmap = bm.getBlurredImage(bitmapToBlure, 50);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap != null) {
                saveCopyToLocal(path, bitmap);
                sendBroadcastMessage(true);
            }
        }
    }

    private void sendBroadcastMessage(boolean success) {
        Intent intent = new Intent(ACTION);

        if(!success) {
            intent.putExtra("retrieveImage", false);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    public String getShortImageNameFromUri(String url) {
        return url.substring(url.lastIndexOf("-")+1);
    }

    private void saveCopyToLocal(String path, Bitmap bitmapToSave) {
        FileOutputStream out = null;
        String picName = getPicNameByExtraValue();
        File fileToSave = new File(path + "/" + picName);
        try {
            out = new FileOutputStream(fileToSave);
            bitmapToSave.compress(Bitmap.CompressFormat.PNG, 100, out);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if(out != null) {
                    out.close();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getPicNameByExtraValue() {
        if(bundle!= null && bundle.containsKey("remoteProfile")) {
            if(bundle.getBoolean("remoteProfile", false)) {
                return PROFILE_PIC_BLURRED_REMOTE;
            }
        }

        return PROFILE_PIC_BLURRED;
    }
}
