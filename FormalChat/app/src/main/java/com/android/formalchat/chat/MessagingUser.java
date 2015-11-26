package com.android.formalchat.chat;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.android.formalchat.ApplicationOfficially;
import com.parse.FunctionCallback;
import com.parse.ParseClassName;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Sve on 8/20/15.
 */

@ParseClassName("_User")
public class MessagingUser extends ParseUser {
    public static final String PREFS_NAME = "FormalChatPrefs_Chat";
    public MessagingUser() {}

    public void sendMessage(final Activity activity, final Pubnub pubnub, final Message message) {
        final String receiverId = message.getReceiverId();
        final String senderId = message.getSenderId();
        message.setTimeSent(new Date());


        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.e(ApplicationOfficially.TAG, "Message Object ID = " + message.getObjectId());
                    message.setMessageId(message.getObjectId());


                    JSONObject messageObject = message.toJSON();
                    Log.v(ApplicationOfficially.TAG, "my message = " + messageObject);

                    subscribeChannelToPubNub(pubnub, receiverId);

                    Callback messageCallback = getMessageCallback(activity);
                    pubnub.publish(receiverId, messageObject, messageCallback);


                    setConversationToParse(message, senderId, receiverId);
                } else {
                    Log.e(ApplicationOfficially.TAG, "Error saving message:" + e.toString());

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, "Error sending message", Toast.LENGTH_LONG).show();

                        }
                    });
                }
            }
        });
    }

    private void setConversationToParse(Message messageObj, String senderId, String receiverId) {
        final HashMap<String, Object> params = new HashMap<>();

        params.put("messageId", messageObj.getObjectId());
        params.put("messageText", messageObj.getString("messageBody"));
        params.put("senderId", senderId);
        params.put("senderName", messageObj.getString("senderName"));
        params.put("receiverName", messageObj.getString("receiverName"));
        params.put("receiverId", receiverId);

        ParseCloud.callFunctionInBackground("setConversation", params, new FunctionCallback<ArrayList<ArrayList>>() {
            @Override
            public void done(ArrayList<ArrayList> arrayLists, ParseException e) {

            }
        });
    }

    private Callback getMessageCallback(final Activity activity) {
        return new Callback() {
            @Override
            public void successCallback(String channel, Object response) {
                Log.v(ApplicationOfficially.TAG, "Message callback response = " + response.toString());

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Add the message to the adapter

                    }
                });

                // Unsubscribe from the channel once the message is sent
                //pubnub.unsubscribe(receiverId);
            }

            @Override
            public void errorCallback(String channel, PubnubError error) {
                Log.e(ApplicationOfficially.TAG, "Message callback error = " + error.toString());

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, "Error sending message", Toast.LENGTH_LONG).show();

                    }
                });

                // Unsubscribe from the channel once the message is sent
                // pubnub.unsubscribe(receiverId);
            }
        };
    }

    private void subscribeChannelToPubNub(Pubnub pubnub, String receiverId) {
        try {
            pubnub.subscribe(receiverId, new Callback() {
                @Override
                public void successCallback(String channel, Object message) {
                    Log.v(ApplicationOfficially.TAG, "Subscribe was successful");
                    super.successCallback(channel, message);
                }

                @Override
                public void errorCallback(String channel, PubnubError error) {
                    Log.e(ApplicationOfficially.TAG, "Subscribe was NOT successful");
                    super.errorCallback(channel, error);
                }
            });
        } catch (PubnubException pubNubException) {
            pubNubException.printStackTrace();
        }
    }
}

