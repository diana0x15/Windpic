package com.wind.windpic.adapters;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.wind.windpic.MainActivity;
import com.wind.windpic.R;
import com.wind.windpic.WindpicApplication;
import com.wind.windpic.fragments.ChatFragment;
import com.wind.windpic.objects.Friend;
import com.wind.windpic.objects.Message;
import com.wind.windpic.tools.Methods;
import com.wind.windpic.views.CircleImageView;

import java.util.ArrayList;
import java.util.Map;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder> {

    private static final String TAG = "FriendsAdapter";
    private static final String EXTRA_FRIEND = "friend";

    private Activity mActivity;
    public static ArrayList<Friend> mFriends;
    private int position;

    public FriendsAdapter(Activity activity, ArrayList<Friend> friends) {
        mActivity = activity;
        mFriends = friends;
    }

    @Override
    public FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_list_item, parent, false);
        FriendsViewHolder viewHolder = new FriendsViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(FriendsViewHolder holder, int position) {
        holder.bindFriend(mFriends.get(position));
    }

    @Override
    public int getItemCount() {
        return mFriends.size();
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public class FriendsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public CircleImageView mPictureImageView;
        public TextView mNameTextView;
        public TextView mMessageTextView;
        public TextView mDateTextView;
        public FrameLayout mNotificationLayout;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            mPictureImageView = (CircleImageView) itemView.findViewById(R.id.friend_list_item_image);
            mNameTextView = (TextView) itemView.findViewById(R.id.friend_list_item_name);
            mMessageTextView = (TextView) itemView.findViewById(R.id.friend_list_item_message);
            mDateTextView = (TextView) itemView.findViewById(R.id.friend_list_item_date);
            mNotificationLayout = (FrameLayout) itemView.findViewById(R.id.notification_layout);

            itemView.setOnClickListener(this);
        }

        public void bindFriend(Friend friend) {
            mNameTextView.setText(friend.getName());
            mPictureImageView.setImageBitmap(BitmapFactory.decodeByteArray(friend.getPicture(), 0, friend.getPicture().length));
            Map<String,String> lastMessage = friend.getLastMessage();
            Message lastMessageData = WindpicApplication.DATABASE.getLastMessage(friend.getId());
            if (lastMessage != null && lastMessage.get(MainActivity.CURRENT_USER.getObjectId()) != null) {
                mMessageTextView.setText(lastMessage.get("text"));
                mDateTextView.setText(getDisplayDate(Methods.getCurrentDate(), lastMessage.get("date")));
                if (lastMessage.get(MainActivity.CURRENT_USER.getObjectId()).equals("unseen")) {
                    mNotificationLayout.setVisibility(View.VISIBLE);
                } else {
                    mNotificationLayout.setVisibility(View.INVISIBLE);
                }
            } else if (lastMessageData != null) {
                mMessageTextView.setText(lastMessageData.getText());
                mDateTextView.setText(getDisplayDate(Methods.getCurrentDate(), lastMessageData.getDate()));
            }
        }

        @Override
        public void onClick(View v) {
            ChatFragment fragment = new ChatFragment();

            Bundle bundle = new Bundle();
            bundle.putParcelable(EXTRA_FRIEND, mFriends.get(getAdapterPosition()));
            fragment.setArguments(bundle);

            FragmentManager manager = ((AppCompatActivity) mActivity).getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();
        }
    }


    public void moveToTop(String id) {
        int index = 0;
        Friend friend = mFriends.get(0);
        for (Friend f : mFriends) {
            if (f.getId().equals(id)) {
                index = mFriends.indexOf(f);
                friend = f;
                break;
            }
        }
        mFriends.remove(index);
        mFriends.add(0, friend);
    }

    private String getDisplayDate(String now, String messageDate) {
        if (messageDate == null)
            return null;
        String hour = Methods.getTime(messageDate);
        String day = Methods.getDay(messageDate);
        String today = Methods.getDay(now);

        if (today.equals(day)) {
            return hour;
        }
        return day;
    }

}










