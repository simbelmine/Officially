package com.android.formalchat;

import com.android.formalchat.chat.Message;
import com.parse.ParseUser;

/**
 * Created by Sve on 8/20/15.
 */
public interface MessageListener {
    public void onMessageReceived(ParseUser sender, Message message);
}
