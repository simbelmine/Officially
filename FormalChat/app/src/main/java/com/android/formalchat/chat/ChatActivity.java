package com.android.formalchat.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.formalchat.DrawerActivity;
import com.android.formalchat.FormalChatApplication;
import com.android.formalchat.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pubnub.api.Pubnub;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Sve on 8/18/15.
 */
public class ChatActivity extends DrawerActivity {
    private DrawerLayout drawerLayout;
    private ListView messageContainer;
    private EditText messageEdit;
    private Button sendButton;
    private ChatAdapter chatAdapter;
    private ArrayList<ChatMessage> chatHistory;

    private String senderId;
    private ParseUser friend;
    private String remoteUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((FormalChatApplication) getApplication()).subscribeToMessagingChannel();

        super.onCreate(savedInstanceState);

        setTitle();
        initView();
        init();

        remoteUserName = getIntent().getStringExtra("username_remote");

        setOnClickListeners();
        loadDummyHistory();
    }

    private BroadcastReceiver onIncommingMessage = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null) {
                senderId = intent.getStringExtra("senderId");

                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setId(122); // dummy
                chatMessage.setMessage(intent.getStringExtra("message"));
                chatMessage.setDate(intent.getStringExtra("timeSentMillis"));
                chatMessage.setIsMe(false);

                displayMessage(chatMessage);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter iff= new IntentFilter(FormalChatApplication.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(onIncommingMessage, iff);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onIncommingMessage);
        closeSoftKeyboard();
    }

    private void setTitle() {
        int title_position = getIntent().getIntExtra("title_position", NONE);
        if(title_position == DrawerActivity.PROFILE_ID) {
            getActionBar().setTitle(getResources().getString(R.string.profile));
        }
        else
        if(title_position != NONE) {
            getActionBar().setTitle(getResources().getStringArray(R.array.menu_list)[title_position]);
        }
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.chat_layout, null, false);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.addView(contentView, 0);
    }

    private void init() {
        messageContainer = (ListView) findViewById(R.id.messagesContainer);
        messageEdit = (EditText) findViewById(R.id.messageEdit);
        sendButton = (Button) findViewById(R.id.chatSendButton);
    }

    private void setOnClickListeners() {
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseQuery<ParseUser> friendQuery = ParseUser.getQuery();

                Log.v(FormalChatApplication.TAG, "remote senderId = " + senderId);
                Log.v(FormalChatApplication.TAG, "remote UserName = " + remoteUserName);

                if(senderId != null && remoteUserName == null) {
                    friendQuery.whereEqualTo("objectId", senderId);
                    findInBackground(friendQuery);
                }
                else if(senderId == null && remoteUserName != null) {
                    friendQuery.whereEqualTo("username", remoteUserName);
                    findInBackground(friendQuery);
                }
                else if(senderId != null && remoteUserName != null) {
                    friendQuery.whereEqualTo("username", remoteUserName);
                    findInBackground(friendQuery);
                }

//                String messageText = messageEdit.getText().toString();
//                if (TextUtils.isEmpty(messageText)) {
//                    return;
//                }
//
//                ChatMessage chatMessage = new ChatMessage();
//                chatMessage.setId(122); // dummy
//                chatMessage.setMessage(messageText);
//                chatMessage.setDate(DateFormat.getDateTimeInstance().format(new Date()));
//                chatMessage.setIsMe(true);
//
//                messageEdit.setText("");
//                displayMessage(chatMessage);
            }
        });
    }

    private void closeSoftKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void findInBackground(ParseQuery<ParseUser> friendQuery) {
        friendQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                if(e == null && parseUsers.size() > 0) {
                    friend = parseUsers.get(0);
                    MessagingUser sender = (MessagingUser) ParseUser.getCurrentUser();
                    String senderId = sender.getObjectId();
                    String receiverId = friend.getObjectId();

                    // Create message object
                    Log.v(FormalChatApplication.TAG, "txt = " + messageEdit.getText());
                    Message message = Message.newInstance(senderId, receiverId, messageEdit.getText().toString());

                    // Create Pubnub object
                    Pubnub pubnub = new Pubnub(getString(R.string.pubnub_publish_key),
                            getString(R.string.pubnub_subscribe_key));

                    // Send the message
                    sender.sendMessage(ChatActivity.this, pubnub, message);

                    // Show message to Chat
                    String messageText = messageEdit.getText().toString();
                    if (TextUtils.isEmpty(messageText)) {
                        return;
                    }
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setId(122); // dummy
                    chatMessage.setMessage(messageText);
                    chatMessage.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                    chatMessage.setIsMe(true);

                    messageEdit.setText("");
                    displayMessage(chatMessage);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Please enter message in field", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void displayMessage(ChatMessage chatMessage) {
        chatAdapter.add(chatMessage);
        chatAdapter.notifyDataSetChanged();
        scroll();
    }

    private void scroll() {
        messageContainer.setSelection(messageContainer.getCount() - 1);
    }

    private void loadDummyHistory() {
        chatHistory = new ArrayList<>();

        ChatMessage msg = new ChatMessage();
        msg.setId(1); // dummy
        msg.setIsMe(false);
        msg.setMessage("Hi");
        msg.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        chatHistory.add(msg);

        ChatMessage msg1 = new ChatMessage();
        msg1.setId(2);
        msg1.setIsMe(false);
        msg1.setMessage("How r u doing???");
        msg1.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        chatHistory.add(msg1);

        chatAdapter = new ChatAdapter(ChatActivity.this, new ArrayList<ChatMessage>());
        messageContainer.setAdapter(chatAdapter);

        for(ChatMessage message : chatHistory) {
            displayMessage(message);
        }
    }


    // ###### PubNub ###### //

}
