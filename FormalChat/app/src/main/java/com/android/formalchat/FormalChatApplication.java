package com.android.formalchat;

import android.app.Application;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.formalchat.chat.Conversation;
import com.android.formalchat.chat.Message;
import com.android.formalchat.chat.MessagingUser;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.json.JSONObject;


/**
 * Created by Sve on 1/28/15.
 */
public class FormalChatApplication extends Application {
    public final static String TAG = "formalchat";
    public final static String ACTION = "message_received";
    private Pubnub pubnub;

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        ParseObject.registerSubclass(UserImages.class);
        ParseObject.registerSubclass(UserInfo.class);
        ParseObject.registerSubclass(UserQuestionary.class);

        // PubNub
        ParseObject.registerSubclass(MessagingUser.class);
        ParseObject.registerSubclass(Message.class);

        ParseObject.registerSubclass(Conversation.class);

        Parse.initialize(this, getString(R.string.app_id), getString(R.string.client_key));
    }

    public void subscribeToMessagingChannel() {
        String userId = ParseUser.getCurrentUser().getObjectId();
        pubnub = new Pubnub(getString(R.string.pubnub_publish_key), getString(R.string.pubnub_subscribe_key));
        try {


            pubnub.subscribe(userId, new Callback() {

                @Override
                public void successCallback(String channel, Object messageObject) {

                    Log.v(TAG, "Imcomming message object : " + messageObject);
                    // Create Parse Object from JSON
                    final Message message = Message.messageFromJSON((JSONObject) messageObject);

                    Log.v(TAG, "Imcomming message : " + message);
                    Log.v(TAG, "Imcomming message ID : " + message.getSenderId());
                    // Use the message sender ID to find the sender
                    ParseUser.getQuery().getInBackground(message.getSenderId(), new GetCallback<ParseUser>() {
                        @Override
                        public void done(ParseUser sender, ParseException e) {
                            if (e == null) {
                                // Make a call to the callback method of the message listener
                                messageListener.onMessageReceived(sender, message);
                            } else {
                                Log.e(TAG, "Error fetching user for message", e);
                            }
                        }
                    });
                }

                @Override
                public void errorCallback(String channel, PubnubError error) {
                    Log.d(TAG, "SUBSCRIBE : ERROR on channel " + channel
                            + " : " + error.toString());
                }

            });
        } catch (PubnubException e) {
            e.printStackTrace();
            Log.e(TAG, "Error subscribing to channel:" + e.getMessage());
        }
    }

    public void unsubscribeFromMessageChannel(String userId) {
        pubnub.unsubscribe(userId);
    }

    private MessageListener messageListener = new MessageListener() {

        @Override
        public void onMessageReceived(ParseUser sender, Message message) {
            Log.v(TAG, "message \" " + message + " \", was received ");

            Intent intent = new Intent(ACTION);
            intent.putExtra("message", message.getMessageBody());
            intent.putExtra("senderId", message.getSenderId());
            intent.putExtra("receiverId", message.getReceiverId());
            intent.putExtra("timeSentMillis", message.getFormattedTimeSent());
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    };
}
