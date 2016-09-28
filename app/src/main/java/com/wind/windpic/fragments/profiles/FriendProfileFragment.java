package com.wind.windpic.fragments.profiles;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.wind.windpic.MainActivity;
import com.wind.windpic.R;
import com.wind.windpic.WindpicApplication;
import com.wind.windpic.fragments.ChatFragment;
import com.wind.windpic.fragments.FriendsFragment;
import com.wind.windpic.objects.Friend;
import com.wind.windpic.tools.Methods;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by dianapislaru on 17/10/15.
 */
public class FriendProfileFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ProfileFragment";
    private static final String EXTRA_FRIEND = "friend";

    private AppCompatActivity mActivity;
    private Friend mFriend;

    private ImageView mImageView;
    private TextView mNameTextView;
    private TextView mInfoTextView;
    private Button mUnFriendButton;

    // Toolbar
    private Toolbar mToolbar;
    private TextView mTitle;
    private TextView mTitleStatus;
    private ImageButton mMenuButton;
    private ImageButton mCameraButton;
    private ImageButton mPhotosButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     //   Methods.setFragment(Methods.FRAGMENT_PROFILE);
        mActivity = (AppCompatActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Methods.disableFullScreen(getActivity());
        View view = inflater.inflate(R.layout.fragment_friend_profile, container, false);
        setHasOptionsMenu(true);

        initViews(view);
        getUserData();
        setUpToolbar(view);

        return view;
    }

    private void initViews(View view) {
        mUnFriendButton = (Button) view.findViewById(R.id.fragment_friend_profile_unfriend_button);
        mImageView = (ImageView) view.findViewById(R.id.fragment_friend_profile_imageView);
        mNameTextView = (TextView) view.findViewById(R.id.fragment_friend_profile_name_textView);
        mInfoTextView = (TextView) view.findViewById(R.id.fragment_friend_profile_info_textView);

        mUnFriendButton.setOnClickListener(this);
    }

    private void getUserData() {
        Bundle bundle = getArguments();
        mFriend = bundle.getParcelable(EXTRA_FRIEND);
        byte[] data = mFriend.getPicture();
        if (data != null) {
            mImageView.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
        }
        mNameTextView.setText(mFriend.getName() + ", " + mFriend.getAge());
        int distance = mFriend.getDistanceInMiles();
        if (distance <= 1) {
            mInfoTextView.setText("less than a mile away");
        } else {
            mInfoTextView.setText(distance + " miles away");
        }
        mUnFriendButton.setText("Unfriend " + mFriend.getFirstName());

    }

    private void setUpToolbar(View view) {
        mToolbar = (Toolbar) view.findViewById(R.id.fragment_friend_profile_toolbar);
        mTitle = (TextView) view.findViewById(R.id.toolbar_title);
        mTitleStatus = (TextView) view.findViewById(R.id.toolbar_status);
        mTitle.setText("");
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mMenuButton = (ImageButton) view.findViewById(R.id.toolbar_button_menu);
        mCameraButton = (ImageButton) view.findViewById(R.id.toolbar_button_left);
        mPhotosButton = (ImageButton) view.findViewById(R.id.toolbar_button_right);
        mPhotosButton.setImageBitmap(null);
        mCameraButton.setImageBitmap(null);
        mMenuButton.setImageBitmap(null);
        mToolbar.setTitle("");
        mTitleStatus.setTextSize(0);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openChatFragment();
            }
        });
    }

    private void openChatFragment() {
        ChatFragment fragment = new ChatFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_FRIEND, mFriend);
        fragment.setArguments(bundle);

        FragmentManager manager = mActivity.getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @Override
    public void onClick(View view) {
        if (view == mUnFriendButton) {
            // UnFriend
            WindpicApplication.FRIENDS_LIST.remove(mFriend);

            List<ParseUser> friends = MainActivity.CURRENT_USER.getList("friends");
            for (int i = 0; i < friends.size(); ++i) {
                ParseUser friend = friends.get(i);
                if (friend.getObjectId().equals(mFriend.getId())) {
                    MainActivity.CURRENT_USER.getList("friends").remove(i);
                    MainActivity.CURRENT_USER.saveInBackground();
                    friend.unpinInBackground();
                    FriendsFragment fragment = new FriendsFragment();
                    FragmentManager manager = mActivity.getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.add(R.id.fragment_container, fragment);
                    transaction.commit();
                    WindpicApplication.FIREBASE.child("UNFRIEND").child(friend.getObjectId()).push()
                            .setValue(MainActivity.CURRENT_USER.getObjectId());
                    return;
                }
            }
        }
    }
}
