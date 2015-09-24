package com.android.formalchat.chat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.android.formalchat.R;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Sve on 9/4/15.
 */
public class CustomParsePushBroadcastReceiver extends ParsePushBroadcastReceiver {
    private static final int NOTIFICATION_ID = 1;
    public static int numMessages = 0;

    @Override
    protected void onPushOpen(Context context, Intent intent) {
////        Bundle bundle = intent.getExtras();
////        for (String key : bundle.keySet()) {
////            Object value = bundle.get(key);
////            Log.d(FormalChatApplication.TAG, String.format("%s *** %s *** (%s)", key,
////                    value.toString(), value.getClass().getName()));
////        }
//
//        Intent i = new Intent(context, ChatActivity.class);
//        i.putExtras(intent.getExtras());
//        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(i);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String action = intent.getAction();
            String channel = intent.getExtras().getString("com.parse.Channel");
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            if(channel != null && channel.equals(ParseUser.getCurrentUser().getObjectId())) {
                if (action.equalsIgnoreCase("com.parse.push.intent.RECEIVE")) {
                    generateNotification(context, intent, json);
                }
            }
        }
        catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private void generateNotification(Context context, Intent intentFromPush, JSONObject json) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtras(intentFromPush.getExtras());
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

        numMessages = 0;
        NotificationManager mNotifM = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        try {
            int drawableResourceId;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                drawableResourceId = R.drawable.push_notification_lollipop_up;
            }
            else{
                drawableResourceId =  R.drawable.ic_launcher;
            }

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(drawableResourceId)
                            .setContentTitle(context.getResources().getString(R.string.app_name))
                            .setContentText(json.getString("message"))
                            .setNumber(++numMessages);

            mBuilder.setAutoCancel(true);
            mBuilder.setContentIntent(contentIntent);

            mNotifM.notify(NOTIFICATION_ID, mBuilder.build());
        }
        catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

//    @Override
//    protected int getSmallIconId(Context context, Intent intent) {
//        return R.drawable.push_notification_lollipop_up;
//    }
//
//    @Override
//    protected Bitmap getLargeIcon(Context context, Intent intent) {
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//            return BitmapFactory.decodeResource(context.getResources(), R.drawable.push_notification_lollipop_up);
//        }
//        else{
//            return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
//        }
//    }
}
