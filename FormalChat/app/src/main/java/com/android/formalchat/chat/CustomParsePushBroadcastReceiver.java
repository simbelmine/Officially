package com.android.formalchat.chat;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.parse.ParsePushBroadcastReceiver;

/**
 * Created by Sve on 9/4/15.
 */
public class CustomParsePushBroadcastReceiver extends ParsePushBroadcastReceiver {
    @Override
    protected void onPushOpen(Context context, Intent intent) {
        Toast.makeText(context, "The push notification was Clicked!", Toast.LENGTH_SHORT).show();
    }
}
