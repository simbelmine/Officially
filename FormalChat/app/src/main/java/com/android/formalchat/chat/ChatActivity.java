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
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.formalchat.DrawerActivity;
import com.android.formalchat.FormalChatApplication;
import com.android.formalchat.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.pubnub.api.Pubnub;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Sve on 8/18/15.
 */
public class ChatActivity extends DrawerActivity {
    private static final int MESSAGES_TO_LOAD_LIMIT = 20;
    private DrawerLayout drawerLayout;
    private ListView messageContainer;
    private EditText messageEdit;
    private Button sendButton;
    private ProgressBar chatProgressBar;
    private ChatAdapter chatAdapter;
    private ArrayList<ChatMessage> chatHistory;

    private String senderId;
    private ParseUser friend;
    private String remoteUserName;

    private Date lastMessageDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((FormalChatApplication) getApplication()).subscribeToMessagingChannel();

        super.onCreate(savedInstanceState);

        setTitle();
        initView();
        init();

        remoteUserName = getIntent().getStringExtra("username_remote");
        chatHistory = new ArrayList<>();

        setOnClickListeners();
        setOnMessageContainerScrollListener();
    }

    private BroadcastReceiver onIncomingMessage = new BroadcastReceiver() {
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
        LocalBroadcastManager.getInstance(this).registerReceiver(onIncomingMessage, iff);

        loadChatHistory();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onIncomingMessage);
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
        chatProgressBar = (ProgressBar) findViewById(R.id.chat_progress_bar);
    }

    private void setOnMessageContainerScrollListener() {
        messageContainer.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int currentFirstVisibleItem;
            private int currentVisibleItemCount;
            private int currentScrollState;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                this.currentScrollState = scrollState;
                this.isScrollCompleted(view);
            }

            private void isScrollCompleted(AbsListView view) {
                if (this.currentVisibleItemCount > 0 && this.currentScrollState == SCROLL_STATE_IDLE) {
                    View currView = view.getChildAt(0);
                    int top = (currView == null) ? 0 : -currView.getTop();

                    if (top == 0) {
                        chatProgressBar.setVisibility(View.VISIBLE);
                        loadChatHistory();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                this.currentFirstVisibleItem = firstVisibleItem;
                this.currentVisibleItemCount = visibleItemCount;
            }
        });
    }

    private void setOnClickListeners() {
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseQuery<ParseUser> friendQuery = ParseUser.getQuery();

                Log.v(FormalChatApplication.TAG, "remote senderId = " + senderId);
                Log.v(FormalChatApplication.TAG, "remote UserName = " + remoteUserName);

                if (senderId != null && remoteUserName == null) {
                    friendQuery.whereEqualTo("objectId", senderId);
                    findInBackground(friendQuery);
                } else if (senderId == null && remoteUserName != null) {
                    friendQuery.whereEqualTo("username", remoteUserName);
                    findInBackground(friendQuery);
                } else if (senderId != null && remoteUserName != null) {
                    friendQuery.whereEqualTo("username", remoteUserName);
                    findInBackground(friendQuery);
                }
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
                if (e == null && parseUsers.size() > 0) {
                    friend = parseUsers.get(0);
                    MessagingUser sender = (MessagingUser) ParseUser.getCurrentUser();
                    String senderId = sender.getObjectId();
                    String receiverId = friend.getObjectId();

                    Log.e(FormalChatApplication.TAG, "sender name = " + sender.getUsername() + " id = " + senderId);
                    Log.e(FormalChatApplication.TAG, "friend receiverId = " + receiverId);

                    Message message = createMessageObject(senderId, receiverId);
                    Pubnub pubnub = createPubNubObject();
                    sendChatMessage(pubnub, sender, message);
                    showCurrentMessageToChat();

                    saveReceiverIdToParseInstalation(receiverId);
                    sendPushNotificationToUser(senderId, receiverId, message);
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter message in field", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private Message createMessageObject(String senderId, String receiverId) {
        Log.v(FormalChatApplication.TAG, "txt = " + messageEdit.getText());
        return Message.newInstance(senderId, receiverId, messageEdit.getText().toString());
    }

    private Pubnub createPubNubObject() {
        return new Pubnub(getString(R.string.pubnub_publish_key),
                getString(R.string.pubnub_subscribe_key));
    }

    private void sendChatMessage(Pubnub pubnub, MessagingUser sender, Message message) {
        sender.sendMessage(ChatActivity.this, pubnub, message);
    }

    private void showCurrentMessageToChat() {
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

    private void displayMessage(ChatMessage chatMessage) {
        chatAdapter.add(chatMessage);
        chatAdapter.notifyDataSetChanged();
        scroll();
    }

    private void saveReceiverIdToParseInstalation(String receiverId) {
        ParseInstallation parseInstallation = ParseInstallation.getCurrentInstallation();
        parseInstallation.put("receiverId", receiverId);
        parseInstallation.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.v(FormalChatApplication.TAG, "Parse Installation was SUCCESSFUL");
                } else {
                    Log.e(FormalChatApplication.TAG, "Parse Installation was was NOT SUCCESSFUL");
                    Log.e(FormalChatApplication.TAG, "Parse Installation Error : " + e.getMessage());
                }
            }
        });
    }

    private void sendPushNotificationToUser(String senderId, String receiverId, Message message) {
        JSONObject data;
        try {
            data = new JSONObject();
            data.put("alert", message.getMessageBody());
            data.put("senderId", senderId);


            ParsePush push = new ParsePush();
            push.setChannel(receiverId);
//        push.setMessage(message.getMessageBody());
            push.setData(data);
            push.sendInBackground();

        } catch (JSONException je) {
            je.printStackTrace();
        }


    }

    private void scroll() {
        messageContainer.setSelection(messageContainer.getCount() - 1);
    }

    private void loadChatHistory() {
        final String senderId = "rVxRWVEQmv";
        if(getIntent().hasExtra("com.parse.Data") || senderId != null) {
//            try {
//                JSONObject jsonObject = new JSONObject(getIntent().getExtras().getString("com.parse.Data"));
//                if(jsonObject.has("senderId")) {
            if(senderId != null) {
//                    String pushNotificationSenderId = jsonObject.getString("senderId");
                String pushNotificationSenderId = senderId;
                String currentUserId = ParseUser.getCurrentUser().getObjectId();
                executeGetMessagesQuery(getMessagesQuery(currentUserId, pushNotificationSenderId), pushNotificationSenderId);
            }
//            }
//            catch (JSONException ex) {
//                ex.printStackTrace();
//            }
        }
    }

    private void executeGetMessagesQuery(ParseQuery<ParseObject> query, String pushNotificationSenderId) {
        query.setLimit(MESSAGES_TO_LOAD_LIMIT);
        query.addDescendingOrder("timeSent");

        if(lastMessageDate != null) {
            query.whereLessThan("timeSent", lastMessageDate);
            findMessagesInBackground(query, pushNotificationSenderId);
        }
        else {
            findMessagesInBackground(query, pushNotificationSenderId);
        }

    }

    private void findMessagesInBackground(ParseQuery<ParseObject> query, final String pushNotificationSenderId) {
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null && list.size() > 0) {
                    updateLastMessageDate(list.get(list.size() - 1).getDate("timeSent"));

                    ArrayList<ChatMessage> currentChatHistory = new ArrayList<>();
                    for (int i = list.size() - 1; i >= 0; i--) {
                        ParseObject po = list.get(i);

                        ChatMessage msg = new ChatMessage();
                        msg.setId(list.indexOf(po));
                        setWhoSentMsg(msg, po.get("senderId"), pushNotificationSenderId);
                        String message = (String) po.get("messageBody");
                        msg.setMessage(message);
                        msg.setDate(DateFormat.getDateTimeInstance().format(po.getDate("timeSent")));
                        currentChatHistory.add(msg);
                    }

                    currentChatHistory.addAll(chatHistory);
                    chatHistory = new ArrayList<>();
                    chatHistory = currentChatHistory;

                    initAdapter(chatHistory);
                } else {
                    Log.e(FormalChatApplication.TAG, "ChatActivity parse ex: " + e + "  or list with msgs is < 0");
                    chatProgressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void initAdapter(ArrayList<ChatMessage> chatHistory) {
        if(chatAdapter != null) {
            chatAdapter.updateChatMessages(chatHistory);
            setTopPosition();
        }
        else {
            chatAdapter = new ChatAdapter(ChatActivity.this, chatHistory);
            messageContainer.setAdapter(chatAdapter);
        }
    }

    private void setTopPosition() {
        int top = (messageContainer == null) ? 0 : messageContainer.getTop();
        messageContainer.setSelectionFromTop(MESSAGES_TO_LOAD_LIMIT, top);
        chatProgressBar.setVisibility(View.GONE);
    }

    private void updateLastMessageDate(Date date) {
        lastMessageDate = date;
    }

    private ParseQuery<ParseObject> getMessagesQuery(String currentUserId, String pushNotificationSenderId) {
        ParseQuery<ParseObject> queryPart1 = ParseQuery.getQuery("Message");
        queryPart1.whereEqualTo("senderId", currentUserId.toString());
        queryPart1.whereEqualTo("receiverId", pushNotificationSenderId);

        // build second AND condition
        ParseQuery<ParseObject> queryPart2 = ParseQuery.getQuery("Message");
        queryPart2.whereEqualTo("senderId", pushNotificationSenderId);
        queryPart2.whereEqualTo("receiverId", currentUserId.toString());

        List<ParseQuery<ParseObject>> queries = new ArrayList<>();
        queries.add(queryPart1);
        queries.add(queryPart2);

        return ParseQuery.or(queries);
    }

    private void setWhoSentMsg(ChatMessage msg, Object senderId, String pushNotificationSenderId) {
        if(senderId != null && pushNotificationSenderId != null) {
            if(senderId.equals(pushNotificationSenderId)) {
                msg.setIsMe(false);
            }
            else {
                msg.setIsMe(true);
            }

        }
    }

}
