package com.wind.windpic.objects;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dianapislaru on 12/10/15.
 */
public class Message {

    private static final String TAG = "Message";
    private static final String JSON_MESSAGE = "message";
    private static final String JSON_SENDER = "sender";
    private static final String JSON_CHAT = "conversation";
    private static final String JSON_DATE = "date";

    private String mText;
    private String mChatId;
    private String mSenderId;
    private String mDate;

    public Message() { }

    public Message(JSONObject json) {
        try {
            mText = json.getString(JSON_MESSAGE);
            mSenderId = json.getString(JSON_SENDER);
            mChatId = json.getString(JSON_CHAT);
            mDate = json.getString(JSON_DATE);
        } catch(JSONException e) { }
    }

    public JSONObject toPushMessage(String name) {
        JSONObject json = new JSONObject();
        try {
            json.put("title", name);
            json.put("alert", mText);
            json.put("senderId", mSenderId);
            json.put("date", mDate);
            json.put("content-available", 1);
            json.put("badge", "Increment");
            json.put("sound", "default");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public String getText() {
        return mText;
    }

    public String getChatId() {
        return mChatId;
    }

    public String getSenderId() {
        return mSenderId;
    }

    public String getDate() {
        return mDate;
    }


    public void setText(String text) {
        mText = text;
    }

    public void setChatId(String chatId) {
        mChatId = chatId;
    }

    public void setSenderId(String senderId) {
        mSenderId = senderId;
    }

    public void setDate(String date) {
        mDate = date;
    }

}
