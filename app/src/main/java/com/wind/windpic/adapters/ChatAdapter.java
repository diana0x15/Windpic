package com.wind.windpic.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wind.windpic.MainActivity;
import com.wind.windpic.R;
import com.wind.windpic.objects.Friend;
import com.wind.windpic.objects.Message;
import com.wind.windpic.tools.Methods;
import com.wind.windpic.views.CircleImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by dianapislaru on 13/10/15.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private static final String TAG = "ChatAdapter";
    private static final int TYPE_LEFT = 0;
    private static final int TYPE_RIGHT = 1;
    private static final int TYPE_HEADER = 2;

    private Context mContext;
    private ArrayList<Message> mMessages;
    private String mFriendId;
    private String mChatId;
    public Bitmap mFriendPicture;
    private Bitmap mCurrentUserPicture;

    public ChatAdapter(Context context, ArrayList<Message> messages, Friend friend) {
        mContext = context;
        mMessages = messages;
        mFriendId = friend.getId();
        mFriendPicture = BitmapFactory.decodeByteArray(friend.getPicture(), 0, friend.getPicture().length);
        Friend mCurrentUser = new Friend(MainActivity.CURRENT_USER);
        mCurrentUserPicture = BitmapFactory.decodeByteArray(mCurrentUser.getPicture(), 0, mCurrentUser.getPicture().length);
        String currentUserId = mCurrentUser.getId();
        if (currentUserId.compareTo(mFriendId) < 0) {
            mChatId = currentUserId + mFriendId;
        } else {
            mChatId = mFriendId + currentUserId;
        }
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            case TYPE_RIGHT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_right, parent, false);
                break;
            case TYPE_LEFT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_left, parent, false);
                break;
            case TYPE_HEADER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_header, parent, false);
                break;
        }
        ChatViewHolder viewHolder = new ChatViewHolder(view, viewType);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        holder.bindMessage(position);
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = mMessages.get(position);
        if (message.getSenderId().equals(mFriendId)) {
            return TYPE_LEFT;
        } else {
            return TYPE_RIGHT;
        }
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {

        public static final String TAG = "ChatViewHolder";
        private static final int TYPE_LEFT = 0;
        private static final int TYPE_RIGHT = 1;
        private static final int TYPE_HEADER = 2;

        private int mViewType;

        public CircleImageView mPictureImageView;
        public TextView mMessageTextView;
       // public TextView mTimeTextView;
        public TextView mNewDateTextView;

        public ChatViewHolder(View itemView, int viewType) {
            super(itemView);
            mViewType = viewType;
            if (viewType == TYPE_HEADER) {

            } else {
                mPictureImageView = (CircleImageView) itemView.findViewById(R.id.chat_item_image);
                mMessageTextView = (TextView) itemView.findViewById(R.id.chat_item_text);
              //  mTimeTextView = (TextView) itemView.findViewById(R.id.chat_item_time);
                mNewDateTextView = (TextView) itemView.findViewById(R.id.chat_item_date_text);
            }
        }

        public void bindMessage(int position) {
            Message message = mMessages.get(position);

            Message previousMessage = null;
            if (position > 0) {
                previousMessage = mMessages.get(position - 1);
            }
            if (previousMessage == null || isNewDate(message.getDate(), previousMessage.getDate())) {
                mNewDateTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimensionPixelSize(R.dimen.chat_item_date_size));
                mNewDateTextView.setText(Methods.getDay(message.getDate()) + " " + Methods.getTime(message.getDate()));
            } else {
                mNewDateTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimension(R.dimen.chat_item_date_empty_size));
            }
            mMessageTextView.setText(message.getText());
        //    mTimeTextView.setText(Methods.getTime(message.getDate()));

            if (mViewType == 0) {
                mPictureImageView.setImageBitmap(mFriendPicture);
            } else if (mViewType == 1) {
                mPictureImageView.setImageBitmap(mCurrentUserPicture);
            }
        }
    }

    private boolean isNewDate(String string1, String string2) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        if (string1 == null || string2 == null) {
            return false;
        }

        Date date1 = null;
        Date date2 = null;
        try {
            date1 = inputFormat.parse(string1);
            date2 = inputFormat.parse(string2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long diffInMs = Math.abs(date1.getTime() - date2.getTime());
        long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs);

        return diffInSec > 600;
    }
}
