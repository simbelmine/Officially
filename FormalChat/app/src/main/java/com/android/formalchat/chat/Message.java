package com.android.formalchat.chat;

import android.util.Log;

import com.android.formalchat.ApplicationOfficially;
import com.parse.ParseClassName;
import com.parse.ParseObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Sve on 8/20/15.
 */

@ParseClassName("Message")
public class Message extends ParseObject {
    // JSON tags
    public static final String JSON_MESSAGE_TAG = "message";
    public static final String JSON_SENDER_ID_TAG = "senderId";
    public static final String JSON_RECEIVER_ID_TAG = "receiverId";
    public static final String JSON_SENDER_NAME_TAG = "senderName";
    public static final String JSON_RECEIVER_NAME_TAG = "receiverName";
    public static final String JSON_TIME_SENT_MILLIS_TAG = "timeSentMillis";

    public Message() {}

    public static Message newInstance(String senderId, String receiverId, String senderName, String receiverName, String messageBody) {
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setSenderName(senderName);
        message.setReceiverName(receiverName);
        message.setMessageBody(messageBody);
        return message;
    }

    public static Message messageFromJSON(JSONObject messageObject) {
        Message message = new Message();

        try {
            String messageBody = messageObject.getString(JSON_MESSAGE_TAG);
            Log.v(ApplicationOfficially.TAG, "messageBody = " + messageBody);
            message.setMessageBody(messageBody);

            String senderId = messageObject.getString(JSON_SENDER_ID_TAG);
            Log.v(ApplicationOfficially.TAG, "messageBody = " + messageBody);
            message.setSenderId(senderId);

            String receiverId = messageObject.getString(JSON_RECEIVER_ID_TAG);
            Log.v(ApplicationOfficially.TAG, "receiverId = " + receiverId);
            message.setReceiverId(receiverId);

            String senderName = messageObject.getString(JSON_SENDER_NAME_TAG);
            message.setSenderName(senderName);

            String receiverName = messageObject.getString(JSON_RECEIVER_NAME_TAG);
            message.setReceiverName(receiverName);

            long timeSentMillis = messageObject.getLong(JSON_TIME_SENT_MILLIS_TAG);
            Log.v(ApplicationOfficially.TAG, "timeSentMillis = " + timeSentMillis);
//            Date timeSent = new Date(timeSentMillis);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, 2015);
            cal.set(Calendar.MONTH, 3);
            cal.set(Calendar.DAY_OF_MONTH, 5);
            cal.set(Calendar.HOUR_OF_DAY, 3);
            cal.set(Calendar.MINUTE, 10);
            cal.set(Calendar.SECOND, 11);
            cal.set(Calendar.MILLISECOND, 12);
            Date timeSent = cal.getTime();
            Log.v(ApplicationOfficially.TAG, "timeSent = " + timeSent.toString());

            message.setTimeSent(timeSent);

            // Return the message
            return message;
        } catch (JSONException e) {
            e.printStackTrace();

            // If error was presented return null
            return null;
        }
    }

    public JSONObject toJSON() {
        JSONObject messageObject = new JSONObject();
        try {
            messageObject.put(JSON_SENDER_ID_TAG, getSenderId());
            messageObject.put(JSON_RECEIVER_ID_TAG, getReceiverId());
            messageObject.put(JSON_SENDER_NAME_TAG, getSenderName());
            messageObject.put(JSON_RECEIVER_NAME_TAG, getReceiverName());
            messageObject.put(JSON_MESSAGE_TAG, getMessageBody());
            messageObject.put(JSON_TIME_SENT_MILLIS_TAG, getTimeSent().getTime());
            return messageObject;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setMessageBody(String messageBody) {
        put("messageBody", messageBody);
    }

    public void setTimeSent(Date timeSent) {
        put("timeSent", timeSent);
    }

    private void setSenderId(String senderId) {
        put("senderId", senderId);
    }

    public String getSenderId() {
        return getString("senderId");
    }

    private void setSenderName(String senderName) {
        put("senderName", senderName);
    }

    public String getSenderName() {
        return getString("senderName");
    }

    public String getMessageBody() {
        return getString("messageBody");
    }

    public Date getTimeSent() {
        return getDate("timeSent");
    }

    public void setReceiverId(String receiverId) {
        put("receiverId", receiverId);
    }

    public String getReceiverId() {
        return getString("receiverId");
    }

    public void setReceiverName(String receiverName) {
        put("receiverName", receiverName);
    }

    public String getReceiverName() {
        return getString("receiverName");
    }

    public String getFormattedTimeSent() {
        Date timeSent = getTimeSent();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy h:m:s a");
        return dateFormat.format(timeSent);
    }
}

