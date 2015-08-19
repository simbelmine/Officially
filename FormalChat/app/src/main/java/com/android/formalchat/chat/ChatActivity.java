package com.android.formalchat.chat;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.android.formalchat.DrawerActivity;
import com.android.formalchat.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle();
        initView();
        init();

        setOnSendMsgListener();
        loadDummyHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();

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

    private void setOnSendMsgListener() {
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageEdit.getText().toString();
                if(TextUtils.isEmpty(messageText)) {
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
}
