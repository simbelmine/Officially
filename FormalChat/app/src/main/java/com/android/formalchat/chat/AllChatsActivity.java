package com.android.formalchat.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.android.formalchat.DrawerActivity;
import com.android.formalchat.FormalChatApplication;
import com.android.formalchat.R;
import com.android.formalchat.ScrollableListView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Sve on 8/21/15.
 */
public class AllChatsActivity extends DrawerActivity {
    private DrawerLayout drawerLayout;
    private AllChatsAdapter allChatsAdapter;
    private ScrollableListView messagesLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.chats_all_layout, null, false);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.addView(contentView, 0);

        messagesLayout = (ScrollableListView) findViewById(R.id.chats_listview);

        Log.v(FormalChatApplication.TAG, "All Chats: messageLayout = " + messagesLayout);
        allChatsAdapter = new AllChatsAdapter(AllChatsActivity.this, new ArrayList<ChatMessage>());
        Log.v(FormalChatApplication.TAG, "All Chats: allChatsAdapter = " + allChatsAdapter);
        messagesLayout.setAdapter(allChatsAdapter);
    }

    private BroadcastReceiver onIncommingMessage = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null) {
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setId(133); // dummy
                chatMessage.setUserIdAsString(intent.getStringExtra("senderId"));
                chatMessage.setMessage(intent.getStringExtra("message"));
                chatMessage.setDate(intent.getStringExtra("timeSentMillis"));
                chatMessage.setIsMe(false);

                allChatsAdapter.add(chatMessage);
                allChatsAdapter.notifyDataSetChanged();
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
    }
}
