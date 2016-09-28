package com.wind.windpic.tools;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.wind.windpic.MainActivity;
import com.wind.windpic.WindpicApplication;
import com.wind.windpic.fragments.ChatFragment;
import com.wind.windpic.fragments.FriendsFragment;
import com.wind.windpic.objects.Database;
import com.wind.windpic.objects.Friend;
import com.wind.windpic.objects.Message;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dianapislaru on 16/10/15.
 */
public class PushReceiver extends ParsePushBroadcastReceiver {

    public static final String TAG = "PushReceiver";
    private static final String EXTRA_FRIEND = "Friend";
    private static final String EXTRA_PUSH = "push";

    private Message mMessage;
    private Friend mFriend;

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        super.onPushReceive(context, intent);
        getPushInfo(intent);

        String id = mMessage.getSenderId();
        if (WindpicApplication.DATABASE == null) {
            ParseUser currentUser = ParseUser.getCurrentUser();
            if (currentUser == null)
                return;
            MainActivity.CURRENT_USER = currentUser;
            WindpicApplication.DATABASE = new Database(context);
        }
        WindpicApplication.DATABASE.addMessage(mMessage);

        if (MainActivity.FRAGMENT_FRIENDS && FriendsFragment.mAdapter != null) {
            FriendsFragment.mAdapter.moveToTop(id);
            FriendsFragment.mAdapter.notifyDataSetChanged();
        }
        if (MainActivity.FRAGMENT_CHAT && ChatFragment.mMessages != null) {
            ChatFragment.mMessages.add(mMessage);
        }
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        super.onPushOpen(context, intent);
    }

    private void getPushInfo(Intent intent) {
        Bundle extras = intent.getExtras();
        String jsonData = extras.getString("com.parse.Data");

        try {
            JSONObject json = new JSONObject(jsonData);
            mMessage = new Message();
            mMessage.setText(json.getString("alert"));
            mMessage.setDate(Methods.changeTimeZoneFromGMT(Methods.getTime(json.getString("date"))));
            String senderId = json.getString("senderId");
            mMessage.setSenderId(senderId);
            mMessage.setChatId(getChatId(senderId));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getChatId(String id) {
        String id1 = MainActivity.CURRENT_USER.getObjectId();
        if (id1.compareTo(id) < 0) {
            return id1 + id;
        } else {
            return id + id1;
        }
    }

}
