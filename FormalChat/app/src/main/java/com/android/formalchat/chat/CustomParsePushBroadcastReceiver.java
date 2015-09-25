package com.android.formalchat.chat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.formalchat.FormalChatApplication;
import com.android.formalchat.R;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Sve on 9/4/15.
 */
public class CustomParsePushBroadcastReceiver extends ParsePushBroadcastReceiver {
    private static final int NOTIFICATION_ID = 1;
    public static int numMessages = 0;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onPushOpen(Context context, Intent intent) {

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
            sharedPreferences = context.getSharedPreferences(ChatActivity.PREFS_NAME, 0);


//            Map<String,?> keys = sharedPreferences.getAll();
//
//            for(Map.Entry<String,?> entry : keys.entrySet()){
//                Log.v(FormalChatApplication.TAG, " ### " + entry.getKey() + " : " +
//                        entry.getValue().toString());
//            }

                Bundle bundle = intent.getExtras();
                Log.e(FormalChatApplication.TAG, " Intent Extras ");
                for (String key : bundle.keySet()) {
                    Object value = bundle.get(key);
                    Log.e(FormalChatApplication.TAG, String.format("###### %s %s (%s)", key,
                            value.toString(), value.getClass().getName()));
                }

            Log.v(FormalChatApplication.TAG, "# sharedPrefs = " + sharedPreferences.getString(ChatActivity.CHAT_PARTICIPANT_1, " "));
            Log.v(FormalChatApplication.TAG, "# intent = " + intent.getStringExtra("senderId"));


            String sd = null;
            JSONObject jsonObject = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            if(jsonObject.has("senderId")) {
                sd = jsonObject.getString("senderId");
            }

            if(!sharedPreferences.contains(ChatActivity.CHAT_PARTICIPANT_1) ||
                    !sharedPreferences.getString(ChatActivity.CHAT_PARTICIPANT_1, " ").equals(sd)) {

                if (channel != null && channel.equals(ParseUser.getCurrentUser().getObjectId())) {
                    if (action.equalsIgnoreCase("com.parse.push.intent.RECEIVE")) {
                        generateNotification(context, intent, json);
                    }
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
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENTcv);

        numMessages = 0;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
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

            notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
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
