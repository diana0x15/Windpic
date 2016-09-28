package com.wind.windpic.fragments;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.wind.windpic.MainActivity;
import com.wind.windpic.R;
import com.wind.windpic.WindpicApplication;
import com.wind.windpic.adapters.ChatAdapter;
import com.wind.windpic.fragments.profiles.FriendProfileFragment;
import com.wind.windpic.objects.Friend;
import com.wind.windpic.objects.Message;
import com.wind.windpic.tools.Methods;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dianapislaru on 09/10/15.
 */
public class ChatFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ChatFragment";
    private static final String EXTRA_FRIEND = "friend";

    private Activity mActivity;
    public static Friend mFriend;
    public static String mChatId;
    private String mFriendId;
    private String mCurrentUserId;
    private String mCurrentUserName = "";
    public static ArrayList<Message> mMessages;
    public static ChatAdapter mAdapter;
    public static boolean mIsFriendOnline;

    private Button mSendButton;
    private EditText mEditText;
    private RecyclerView mRecyclerView;

    // Toolbar
    private Toolbar mToolbar;
    private TextView mToolbarTitle;
    public static TextView mToolbarStatus;
    private ImageButton mMenuButton;
    private ImageButton mCameraButton;
    private ImageButton mPhotosButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Methods.setFragment(Methods.FRAGMENT_CHAT);
        initValues();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Methods.disableFullScreen(getActivity());
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        setupToolbar(view);
        initViews(view);
        loadMessages();
        removeNotifications();
        return view;
    }

    private void initValues() {
        mActivity = getActivity();
        mFriend = getArguments().getParcelable(EXTRA_FRIEND);
        mCurrentUserId = MainActivity.CURRENT_USER.getObjectId();
        Friend user = new Friend(MainActivity.CURRENT_USER);
        mCurrentUserName = user.getName();
        mFriendId = mFriend.getId();

        if (mCurrentUserId.compareTo(mFriendId) < 0) {
            mChatId = mCurrentUserId + mFriendId;
        } else {
            mChatId = mFriendId + mCurrentUserId;
        }
        WindpicApplication.DATABASE.setChat(mChatId);
        mMessages = new ArrayList<>();
        mAdapter = new ChatAdapter(this.getContext(), mMessages, mFriend);

        WindpicApplication.FIREBASE.child("CONNECTIVITY").child(mFriendId).addValueEventListener(mConnectivityListener);
        WindpicApplication.FIREBASE.child("CHATS").child(mChatId).addChildEventListener(mMessageListener);
    }

    private void initViews(View view) {
        mSendButton = (Button) view.findViewById(R.id.fragment_chat_send_button);
        mEditText = (EditText) view.findViewById(R.id.fragment_chat_editText);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_chat_recyclerView);
        mSendButton.setEnabled(false);
        mSendButton.setOnClickListener(this);

        // Setup RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);

        setupEditTextEvents();
    }

    private void createNewMessage(String text) {
        // GMT time zone
        Message message = new Message();
        Message message2 = new Message();
        message.setText(text);
        message2.setText(text);
        message.setChatId(mChatId);
        message2.setChatId(mChatId);
        message.setSenderId(mCurrentUserId);
        message2.setSenderId(mCurrentUserId);
        // Default time zone
        message.setDate(Methods.getCurrentDate());
        message2.setDate(Methods.changeTimeZoneToGMT(Methods.getCurrentDate()));
        mMessages.add(message);
        mEditText.setText("");
        mAdapter.notifyDataSetChanged();
        mRecyclerView.smoothScrollToPosition(mMessages.size() - 1);
        // Add message to Database
        WindpicApplication.DATABASE.addMessage(message);

        // Set last message
        Map<String,String> last_message = new HashMap<>();
        last_message.put(MainActivity.CURRENT_USER.getObjectId(), "seen");
        last_message.put(mFriendId, "unseen");
        last_message.put("text", message2.getText());
        last_message.put("date", message2.getDate());
        WindpicApplication.FIREBASE.child("LAST_MESSAGES").child(mChatId).setValue(last_message);

//        // send message
        if (mIsFriendOnline) {
            // Send Firebase Message
            WindpicApplication.FIREBASE.child("CHATS").child(mChatId).push().setValue(message2);
        } else {
            // Send Parse Push
            Log.i(TAG, "PUSH DATE: " + message2.getDate());
            sendPush(message2);
        }


    }

    private void sendPush(Message message) {
        ParsePush push = new ParsePush();

        ParseObject user = ParseObject.createWithoutData("_User", mFriendId);

        // Set push query
        ParseQuery query = ParseInstallation.getQuery();
        query.whereEqualTo("user", user);
        push.setQuery(query);

        // Add the pushMessage
        push.setData(message.toPushMessage(mCurrentUserName));

        // Send push
        push.sendInBackground();
    }

    private void makeLastMessageSeen() {
        Map<String,String> lastMessage = mFriend.getLastMessage();
        if (lastMessage != null) {
            WindpicApplication.FIREBASE.child("LAST_MESSAGES").child(mChatId).child(MainActivity.CURRENT_USER.getObjectId()).setValue("seen");
        }
        WindpicApplication.MESSAGES = false;
        Log.i(TAG, "chat messages false");
    }

    private void loadMessages() {
        Cursor result = WindpicApplication.DATABASE.getMessages(mChatId, 0);
        if (result != null && result.getCount() != 0) {
            while (result.moveToNext()) {
                Message chatMessage = new Message();
                chatMessage.setText(result.getString(0));
                chatMessage.setChatId(result.getString(1));
                chatMessage.setSenderId(result.getString(2));
                chatMessage.setDate(result.getString(3));
                mMessages.add(chatMessage);
                mAdapter.notifyDataSetChanged();
                mRecyclerView.smoothScrollToPosition(mMessages.size() - 1);
            }
        }
    }

    private void setupToolbar(View view) {
        mToolbar = (Toolbar) view.findViewById(R.id.fragment_chat_toolbar);
        mToolbarTitle = (TextView) view.findViewById(R.id.toolbar_title);
        mToolbarStatus = (TextView) view.findViewById(R.id.toolbar_status);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mMenuButton = (ImageButton) view.findViewById(R.id.toolbar_button_menu);
        mCameraButton = (ImageButton) view.findViewById(R.id.toolbar_button_left);
        mPhotosButton = (ImageButton) view.findViewById(R.id.toolbar_button_right);
        mCameraButton.setImageBitmap(null);
        mMenuButton.setImageBitmap(null);
        mPhotosButton.setImageBitmap(null);
        mToolbarTitle.setText(mFriend.getName());
        mToolbarTitle.setOnClickListener(this);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(mActivity);
                openFragment(new FriendsFragment(), null);
            }
        });
    }

    private void setupEditTextEvents() {
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) {
                    mSendButton.setEnabled(false);
                } else {
                    mSendButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public static void hideKeyboard(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        if (mChatId != "" && mFriendId != "") {
            WindpicApplication.FIREBASE.child("CONNECTIVITY").child(mFriendId).removeEventListener(mConnectivityListener);
            WindpicApplication.FIREBASE.child("CHATS").child(mChatId).removeEventListener(mMessageListener);
        }
        super.onDestroy();
    }

    private ChildEventListener mMessageListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            if (MainActivity.FRAGMENT_CHAT && dataSnapshot.getValue() != null) {
                Message message = dataSnapshot.getValue(Message.class);
                message.setDate(Methods.changeTimeZoneFromGMT(message.getDate()));
                if (message.getSenderId().equals(mFriendId)) {
                    WindpicApplication.DATABASE.addMessage(message);
                    mMessages.add(message);
                    mAdapter.notifyDataSetChanged();
                    mRecyclerView.smoothScrollToPosition(mMessages.size() - 1);
                    WindpicApplication.FIREBASE.child("CHATS").child(mChatId).child(dataSnapshot.getKey()).removeValue();
                }
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
        }
    };

    private ValueEventListener mConnectivityListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (MainActivity.FRAGMENT_CHAT && ChatFragment.mToolbarStatus != null) {
                if (dataSnapshot.getValue() != null) {
                    String connectivity = dataSnapshot.getValue().toString();
                    mToolbarStatus.setText(connectivity);
                    if (connectivity.equals("online")) {
                        mIsFriendOnline = true;
                    } else {
                        mIsFriendOnline = false;
                    }
                }
            }
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
        }
    };

    private void removeNotifications() {
        NotificationManager notificationManager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        makeLastMessageSeen();
    }

    private void openFragment(Fragment fragment, Friend friend) {
        FragmentManager manager = ((AppCompatActivity) mActivity).getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        if (friend != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(EXTRA_FRIEND, friend);
            fragment.setArguments(bundle);
        }
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @Override
    public void onClick(View view) {
        if (view == mSendButton) {
            createNewMessage(mEditText.getText().toString());
        } else if (view == mToolbarTitle) {
            hideKeyboard(mActivity);
            openFragment(new FriendProfileFragment(), mFriend);
        }
    }

}
