package com.android.formalchat.chat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.android.formalchat.R;
import com.parse.ParsePushBroadcastReceiver;

/**
 * Created by Sve on 9/4/15.
 */
public class CustomParsePushBroadcastReceiver extends ParsePushBroadcastReceiver {
    @Override
    protected void onPushOpen(Context context, Intent intent) {
//        Bundle bundle = intent.getExtras();
//        for (String key : bundle.keySet()) {
//            Object value = bundle.get(key);
//            Log.d(FormalChatApplication.TAG, String.format("%s *** %s *** (%s)", key,
//                    value.toString(), value.getClass().getName()));
//        }

        Intent i = new Intent(context, ChatActivity.class);
        i.putExtras(intent.getExtras());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    @Override
    protected int getSmallIconId(Context context, Intent intent) {
        return R.drawable.push_notification_lollipop_up;
    }

    @Override
    protected Bitmap getLargeIcon(Context context, Intent intent) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.push_notification_lollipop_up);
        }
        else{
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
        }
    }
}
