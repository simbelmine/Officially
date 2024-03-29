package com.android.formalchat.chat;

import java.util.Random;

/**
 * Created by Sve on 8/18/15.
 */
public class ChatMessage {
    private static int MIN = 100;
    private static int MAX = 999;
    private long id;
    private boolean isMe;
    private String message;
    private Long userId;
    private String userIdString;
    private String dateTime;

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public boolean getIsme() {
        return isMe;
    }
    public void setIsMe(boolean isMe) {
        this.isMe = isMe;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setUserIdAsString(String userIdString) {
        this.userIdString = userIdString;
    }
    public String getUserIdAsString() {
        return userIdString;
    }

    public String getDate() {
        return dateTime;
    }

    public void setDate(String dateTime) {
        this.dateTime = dateTime;
    }

    protected int getRandomIdNumber() {
        return (new Random().nextInt((MAX - MIN) + 1)) + MIN;
    }
}
