package com.android.formalchat.chat;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.android.formalchat.FormalChatApplication;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Sve on 8/20/15.
 */

@ParseClassName("_User")
public class MessagingUser extends ParseUser {
    public MessagingUser() {}

    public void sendMessage(final Activity activity, final Pubnub pubnub, final Message message) {
        final String receiverId = message.getReceiverId();
        message.setTimeSent(new Date());


        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    JSONObject messageObject = message.toJSON();
                    Log.v(FormalChatApplication.TAG, "my message = " + messageObject);

                    try {
                        pubnub.subscribe(receiverId, new Callback() {
                            @Override
                            public void successCallback(String channel, Object message) {
                                Log.v(FormalChatApplication.TAG, "Subscribe was successful");
                                super.successCallback(channel, message);
                            }

                            @Override
                            public void errorCallback(String channel, PubnubError error) {
                                Log.e(FormalChatApplication.TAG, "Subscribe was NOT successful");
                                super.errorCallback(channel, error);
                            }
                        });
                    } catch (PubnubException pubNubException) {
                        pubNubException.printStackTrace();
                    }

                    Callback messageCallback = new Callback() {
                        @Override
                        public void successCallback(String channel, Object response) {
                            Log.v(FormalChatApplication.TAG, "Message callback response = " + response.toString());

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
                            Log.e(FormalChatApplication.TAG, "Message callback error = " + error.toString());

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

                    pubnub.publish(receiverId, messageObject, messageCallback);
                }
                else {
                    Log.e(FormalChatApplication.TAG, "Error saving message:" + e.toString());

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
}

