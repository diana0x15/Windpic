package com.wind.windpic.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.wind.windpic.MainActivity;
import com.wind.windpic.R;
import com.wind.windpic.WindpicApplication;
import com.wind.windpic.adapters.FriendsAdapter;
import com.wind.windpic.objects.Friend;
import com.wind.windpic.tools.FriendsListComparator;
import com.wind.windpic.tools.Methods;
import com.wind.windpic.views.ListDivider;

import java.util.Collections;

/**
 * Created by dianapislaru on 09/10/15.
 */
public class FriendsFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "FriendsFragment";

    private Activity mActivity;
    public static FriendsAdapter mAdapter;

    private RecyclerView mRecyclerView;
    private static FrameLayout mRequestsNotificationsLayout;
    private static FrameLayout mPhotosNotificationsLayout;
    public static TextView mEmptyTextView;

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
        Methods.setFragment(Methods.FRAGMENT_FRIENDS);
        mActivity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Methods.disableFullScreen(getActivity());
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        setUpToolbar(view);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_friends_recyclerView);
        mEmptyTextView = (TextView) view.findViewById(R.id.fragment_friends_empty_textView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new ListDivider(mActivity));

        displayFriends();

        return view;
    }

    private void displayFriends() {
        if (WindpicApplication.FRIENDS_LIST.size() != 0) {
            mEmptyTextView.setVisibility(View.INVISIBLE);
        } else {
            mEmptyTextView.setVisibility(View.VISIBLE);
        }
        // Display the friends
        mAdapter = new FriendsAdapter(mActivity, WindpicApplication.FRIENDS_LIST);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }


    private void setUpToolbar(View view) {
        mToolbar = (Toolbar) view.findViewById(R.id.fragment_friends_toolbar);
        mTitle = (TextView) view.findViewById(R.id.toolbar_title);
        mTitleStatus = (TextView) view.findViewById(R.id.toolbar_status);
        mMenuButton = (ImageButton) view.findViewById(R.id.toolbar_button_menu);
        mCameraButton = (ImageButton) view.findViewById(R.id.toolbar_button_left);
        mPhotosButton = (ImageButton) view.findViewById(R.id.toolbar_button_right);
        mRequestsNotificationsLayout = (FrameLayout) view.findViewById(R.id.notification_button_menu_layout);
        mPhotosNotificationsLayout = (FrameLayout) view.findViewById(R.id.notification_button_right_layout);
        mCameraButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_photo_camera_white_24dp));
        mPhotosButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_image_white_24dp));
        mMenuButton.setOnClickListener(this);
        mCameraButton.setOnClickListener(this);
        mPhotosButton.setOnClickListener(this);
        mTitleStatus.setTextSize(0);
        mToolbar.setTitle("");
        mTitle.setText("Friends");
    }

    private void openFragment(Fragment fragment) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    public static void updateRequestsNotifications(boolean notifications) {
        if (mRequestsNotificationsLayout == null) return;
        if (notifications) {
            mRequestsNotificationsLayout.setVisibility(View.VISIBLE);
        } else {
            mRequestsNotificationsLayout.setVisibility(View.INVISIBLE);
        }
    }

    public static void updatePhotosNotifications(boolean notifications) {
        if (mPhotosNotificationsLayout == null) return;
        if (notifications) {
            mPhotosNotificationsLayout.setVisibility(View.VISIBLE);
        } else {
            mPhotosNotificationsLayout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        if (view == mMenuButton) {
            MainActivity.SIDE_MENU.open();
        } else if (view == mCameraButton) {
            openFragment(new CameraFragment());
        } else if (view == mPhotosButton) {
            openFragment(new PhotosFragment());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Methods.setFragment(Methods.FRAGMENT_FRIENDS);
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        if (WindpicApplication.FRIENDS_LIST != null) {
            Collections.sort(WindpicApplication.FRIENDS_LIST, new FriendsListComparator());
        }
        updatePhotosNotifications(WindpicApplication.PHOTOS);
        updateRequestsNotifications(WindpicApplication.REQUESTS);
    }

    private static String getChatId(String id) {
        String id1 = MainActivity.CURRENT_USER.getObjectId();
        if (id1.compareTo(id) < 0) {
            return id1 + id;
        } else {
            return id + id1;
        }
    }

    public static void showMessageNotificationIcon(Friend friend) {
        CameraFragment.updateMessagesNotifications(true);
        PhotosFragment.updateMessagesNotifications(true);

        if (FriendsFragment.mAdapter != null) {
            FriendsFragment.mAdapter.notifyDataSetChanged();
        }

        if (MainActivity.FRAGMENT_CHAT) {
            WindpicApplication.FIREBASE.child("LAST_MESSAGES").child(getChatId(friend.getId())).child(MainActivity.CURRENT_USER.getObjectId()).setValue("seen");
        }
    }

}
