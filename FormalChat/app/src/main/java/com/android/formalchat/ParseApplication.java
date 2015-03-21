package com.android.formalchat;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by Sve on 1/28/15.
 */
public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        ParseObject.registerSubclass(UserImages.class);
        ParseObject.registerSubclass(UserInfo.class);
        ParseObject.registerSubclass(UserQuestionary.class);
        Parse.initialize(this, getString(R.string.app_id), getString(R.string.client_key));
    }
}
