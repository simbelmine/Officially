package com.android.formalchat.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.File;
import java.util.List;

/**
 * Created by Sve on 8/16/15.
 */
public class ProfileGalleryUtils {
    private String blurredImgName = "blurred_profile.jpg";
    private Context context;
    private ParseUser user;
    private ParseObject parseObject;
    String parseProfileImgName;
    String currentShortImgName;

    public ProfileGalleryUtils(Context context, ParseUser user, ParseObject parseObject) {
        this.context = context;
        this.user = user;
        this.parseObject = parseObject;
        this.parseProfileImgName = getParseProfileImgName();
        this.currentShortImgName = getCurrentShortImgName();
    }

    public String getParseProfileImgName() {
        if(user != null) {
            return (String)(user.get("profileImgName"));
        }
        return null;
    }

    public String getCurrentShortImgName() {
        if(parseObject != null) {
           return getShortImageNameFromUri(((ParseFile) parseObject.get("photo")).getName());
        }
        return null;
    }

    public boolean isProfilePic() {
        if(currentShortImgName.equals(parseProfileImgName)) {
            return true;
        }
        return false;
    }

    public void deleteProfileImgFromParse()  {
        ParseQuery parseQueryDeleteProfilePic = user.getQuery();
        parseQueryDeleteProfilePic.whereContains("profileImgName", currentShortImgName);
        parseQueryDeleteProfilePic.findInBackground(new FindCallback() {
            @Override
            public void done(List list, ParseException e) {
                if(e == null && list.size() > 0) {
                    ((ParseObject) list.get(0)).remove("profileImgName");
                    ((ParseObject) list.get(0)).remove("profileImg");
                    ((ParseObject) list.get(0)).saveInBackground();
                }
            }
        });
    }

    public void deleteImgFromParse() {
        parseObject.deleteInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                Log.v("formalchat", "Picture has been deleted successfully");
                sendBroadcastMessage(FullImageActivity.ACTION_DELETED);
            }
        });
    }

    public String getShortImageNameFromUri(String name) {
        return name.substring(name.lastIndexOf("-")+1);
    }

    private void sendBroadcastMessage(String action) {
        Intent sender = new Intent(action);
        LocalBroadcastManager.getInstance(context).sendBroadcast(sender);
    }

    public void deleteBlurrredImageFromLocal() {
        File dir = new File(Environment.getExternalStorageDirectory() + "/.formal_chat");
        File[] dirImages = dir.listFiles();

        if(dirImages.length != 0) {
            for(File img : dirImages) {
                if(blurredImgName.equals(img.getName())) {
                    img.delete();
                    return;
                }
            }
        }
    }
}
