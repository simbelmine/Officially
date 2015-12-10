package com.android.formalchat.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.android.formalchat.ApplicationOfficially;
import com.android.formalchat.DrawerActivity;
import com.android.formalchat.R;
import com.android.formalchat.ScrollableListView;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Sve on 8/21/15.
 */
public class AllChatsActivity extends DrawerActivity {
    private DrawerLayout drawerLayout;
    private AllChatsAdapter allChatsAdapter;
    private ScrollableListView conversationsListLayout;
    private ArrayList<String> senderIds;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((ApplicationOfficially) getApplication()).subscribeToMessagingChannel();
        super.onCreate(savedInstanceState);

        setTitle();
        inflateDrawerLayout();
        init();
        setOnRefreshListener();

        senderIds = new ArrayList<>();
//        allChatsAdapter = new AllChatsAdapter(AllChatsActivity.this, new ArrayList<ParseObject>());
//        conversationsListLayout.setAdapter(allChatsAdapter);

        if(((ApplicationOfficially)getApplication()).isNetworkAvailable()) {
            getUserChatFriendsList();
        }
        else {
            ((ApplicationOfficially)getApplication()).getSnackbar(this, R.string.no_network, R.color.alert_red).show();
        }
    }

    private void init() {
        conversationsListLayout = (ScrollableListView) findViewById(R.id.chats_listview);
        initSwipeContainer();
    }

    private void initSwipeContainer() {
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        setSwipeAppearance();
    }

    private void setSwipeAppearance() {
        swipeContainer.setColorSchemeResources(
                android.R.color.holo_blue_dark,
                android.R.color.holo_green_dark,
                android.R.color.holo_blue_dark,
                android.R.color.holo_green_dark
        );
    }

    private void inflateDrawerLayout() {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.chats_all_layout, null, false);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.addView(contentView, 0);
    }

    private void setTitle() {
        int title_position = getIntent().getIntExtra("title_position", NONE);
        if(title_position != NONE) {
            setTitle(getResources().getStringArray(R.array.menu_list)[title_position]);
        }
        else {
            setTitle(getResources().getStringArray(R.array.menu_list)[0]);
        }
    }

    private void setOnRefreshListener() {
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(((ApplicationOfficially)getApplication()).isNetworkAvailable()) {
                    getUserChatFriendsList();
                }
                else {
                    swipeContainer.setRefreshing(false);
                    ((ApplicationOfficially)getApplication()).getSnackbar(AllChatsActivity.this, R.string.no_network, R.color.alert_red).show();
                }
            }
        });
    }

    private BroadcastReceiver onIncomingMessage = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent != null) {
                ChatMessage chatMessage = getChatMessageFromIntent(intent);
                if(intent.hasExtra("senderId")) {
                    if(!isSenderMessageExists(intent.getStringExtra("senderId"))) {


                        Log.v(ApplicationOfficially.TAG, "I Received my message in  All Chats - the same sender ID!");

                        senderIds.add(intent.getStringExtra("senderId"));
//                        allChatsAdapter.add(chatMessage);
//                        allChatsAdapter.notifyDataSetChanged();
                    }
                    else {
                        Log.v(ApplicationOfficially.TAG, "I Received my message in  All Chats - just UPDATE the ID!");
//                        allChatsAdapter.updateChatMessage(chatMessage);
                    }
                }
            }
        }
    };

    private boolean isSenderMessageExists(String senderId) {
        if(senderIds.contains(senderId)) {
            return true;
        }
        return false;
    }

    private ChatMessage getChatMessageFromIntent(Intent intent) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setId(chatMessage.getRandomIdNumber()); // dummy
        chatMessage.setUserIdAsString(intent.getStringExtra("senderId"));
        chatMessage.setMessage(intent.getStringExtra("message"));
        chatMessage.setDate(intent.getStringExtra("timeSentMillis"));
        chatMessage.setIsMe(false);

        return chatMessage;
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter iff= new IntentFilter(ApplicationOfficially.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(onIncomingMessage, iff);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onIncomingMessage);
        ((ApplicationOfficially) getApplication()).unsubscribeFromMessageChannel(ParseUser.getCurrentUser().getObjectId());
    }

    private void getUserChatFriendsList() {
        final HashMap<String, Object> params = new HashMap<>();

        ParseCloud.callFunctionInBackground("getAllUserConversations", params, new FunctionCallback<ArrayList<ArrayList>>() {
            @Override
            public void done(ArrayList<ArrayList> listResults, ParseException e) {
                if (e == null && listResults != null) {
                    ArrayList<ParseObject> resultList = listResults.get(0);

                    ArrayList<ParseObject> conversations = new ArrayList<>();
                    for (ParseObject po : resultList) {
                        conversations.add(po);
                    }

                    initAdapter(conversations);
                    swipeContainer.setRefreshing(false);
                }
                else {
                    swipeContainer.setRefreshing(false);
                    ((ApplicationOfficially)getApplication()).getSnackbar(AllChatsActivity.this, R.string.something_wrong, R.color.alert_red).show();
                }
            }
        });




//        ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Chat");
//        query1.whereEqualTo("receiverId", ParseUser.getCurrentUser());
//
//        ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Chat");
//        query2.whereEqualTo("senderId", ParseUser.getCurrentUser().getObjectId().toString());
//
//        List<ParseQuery<ParseObject>> queries = new ArrayList<>();
//        queries.add(query1);
//        queries.add(query2);
//
//        ParseQuery<ParseObject> mainQuery = ParseQuery.or(queries);
//        mainQuery.addDescendingOrder("createdAt");
//        mainQuery.setLimit(10);
//        mainQuery.include("receiverId");
//        mainQuery.findInBackground(new FindCallback<ParseObject>() {
//            @Override
//            public void done(List<ParseObject> listFriends, ParseException e) {
//                if (e == null && listFriends.size() > 0) {
//                    ArrayList<ChatObject> friendsChatsList = new ArrayList<>();
//                    for (ParseObject parseObj : listFriends) {
//                        String sender_id = parseObj.getString("senderId");
//                        String receiver_id = parseObj.getParseObject("receiverId").getObjectId(); // get the exact Pointer Data
//                        ChatObject chat = new ChatObject(sender_id, receiver_id);
//                        friendsChatsList.add(chat);
//                    }
//
//                    executeGetLastUserMessageQuery(friendsChatsList);
//                }
//            }
//        });
    }

    private void initAdapter(ArrayList<ParseObject> conversations) {
        allChatsAdapter = new AllChatsAdapter(this, conversations);
        conversationsListLayout.setAdapter(allChatsAdapter);
    }

    private void executeGetLastUserMessageQuery(ArrayList<ChatObject> friendsChatsList) {
        final ArrayList<String> senderIds = new ArrayList<>();
        ArrayList<String> receiverIds = new ArrayList<>();
        ArrayList<ChatObject> chats = new ArrayList<>();


        for(ChatObject chatObject : friendsChatsList) {
//            senderIds.add(chatObject.getSenderId());
//            receiverIds.add(chatObject.getReceiverId());
            ChatObject tmp = new ChatObject(chatObject.getSenderId(), chatObject.getReceiverId());
            chats.add(tmp);
        }

        ArrayList<ChatObject> nonRepeatedChats = getNonRepeatedChats(chats);
        chats = nonRepeatedChats;


        Log.v(ApplicationOfficially.TAG, "senderIds " + senderIds);
        Log.v(ApplicationOfficially.TAG, "receiverIds " + receiverIds);

        if(senderIds.size() == receiverIds.size()) {
            for(int i = 0; i < chats.size(); i++) {
                Log.v(ApplicationOfficially.TAG, "i = " + i);
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Message");
                query.whereEqualTo("senderId", chats.get(i).getReceiverId());
                query.whereEqualTo("receiverId", chats.get(i).getSenderId());
                query.addDescendingOrder("timeSent");
                query.setLimit(1);
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> listWithMessages, ParseException e) {
                        Log.e(ApplicationOfficially.TAG, "IN   " + listWithMessages);
                        if(e == null && listWithMessages.size() > 0) {
                            Log.v(ApplicationOfficially.TAG, " " + listWithMessages + " --- " + listWithMessages.get(0).getString("messageBody"));

                            allChatsAdapter.add(listWithMessages.get(0));
                            allChatsAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        }
    }

    private ArrayList<ChatObject> getNonRepeatedChats(ArrayList<ChatObject> chats) {
        for(int i = 0; i < chats.size() - 1; i++) {
           for(int k = i+1; k < chats.size(); k++) {
                if(chats.get(i).getSenderId().equals(chats.get(k).getReceiverId()) &&
                        chats.get(i).getReceiverId().equals(chats.get(k).getSenderId())) {
                    chats.remove(i);
                }
           }
        }

        return chats;
    }


    private class ChatObject {
        private String senderId;
        private String receiverId;

        public ChatObject(String senderId, String receiverId) {
            this.senderId = senderId;
            this.receiverId = receiverId;
        }

        public String getReceiverId() {
            return receiverId;
        }

        public String getSenderId() {
            return senderId;
        }
    }
}
