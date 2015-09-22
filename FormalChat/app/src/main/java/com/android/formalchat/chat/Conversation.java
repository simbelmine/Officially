package com.android.formalchat.chat;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Sve on 9/20/15.
 */

@ParseClassName("Conversation")
public class Conversation extends ParseObject {
    public Conversation() {}

    public String getMessageId() {
        return getString("messageId");
    }
    public void setMessageId(String messageId) {
        put("messageId", messageId);
    }

    public String getReceiverId() {
        return getString("receiverId");
    }
    public void setReceiverId(String receiverId) {
        put("receiverId", receiverId);
    }

    public String getSenderId() {
        return getString("senderId");
    }
    public void setSenderId(String senderId) {
        put("senderId", senderId);
    }

    public String getSenderName() {
        return getString("senderName");
    }
    public void setSenderName(String senderName) {
        put("senderName", senderName);
    }

    public String getReceiverName() {
        return getString("receiverName");
    }
    public void setReceiverName(String receiverName) {
        put("receiverName", receiverName);
    }

    public String getMessageText() {
        return getString("messageText");
    }
    public void setMessageText(String messageText) {
        put("messageText", messageText);
    }
}
