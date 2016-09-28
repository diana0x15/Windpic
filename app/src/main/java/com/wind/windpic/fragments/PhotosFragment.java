package com.wind.windpic.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.wind.windpic.MainActivity;
import com.wind.windpic.R;
import com.wind.windpic.WindpicApplication;
import com.wind.windpic.objects.Photo;
import com.wind.windpic.swipe_images.SwipeImage;
import com.wind.windpic.tools.Methods;

/**
 * Created by dianapislaru on 09/10/15.
 */
public class PhotosFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "PhotosFragment";

    private Activity mActivity;
    public static SwipeImage mSwipe;
    public static Photo mFrontPhoto;
    public static Photo mBackPhoto;
    private Button mRefreshButton;

    // Toolbar
    private Toolbar mToolbar;
    private TextView mTitle;
    private TextView mTitleStatus;
    private ImageButton mMenuButton;
    private ImageButton mCameraButton;
    private ImageButton mFriendsButton;
    private static FrameLayout mRequestsNotificationsLayout;
    private static FrameLayout mMessagesNotificationsLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Methods.setFragment(Methods.FRAGMENT_PHOTOS);
        mActivity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Methods.disableFullScreen(mActivity);
        View view;
        // Check for internet connection.
        if (!Methods.isInternetAvailable(mActivity)) {
            // No internet connection.
            view = inflater.inflate(R.layout.no_internet_connection, container, false);
            mRefreshButton = (Button) view.findViewById(R.id.no_internet_refresh_button);
            mRefreshButton.setOnClickListener(this);
        } else {
            view = inflater.inflate(R.layout.fragment_photos, container, false);
            mSwipe = new SwipeImage(mActivity, view);
            getPhotos();
        }
        setUpToolbar(view);
        return view;
    }

    private void getPhotos() {
        Log.i(TAG, WindpicApplication.PHOTOS_LIST.size() + "");

        if (WindpicApplication.PHOTOS_LIST.size() > 0) {
            mFrontPhoto = WindpicApplication.PHOTOS_LIST.get(0);
            WindpicApplication.PHOTOS_LIST.remove(0);
        }
        if (WindpicApplication.PHOTOS_LIST.size() > 0) {
            mBackPhoto = WindpicApplication.PHOTOS_LIST.get(0);
        }
        mSwipe.show();
    }

    private void setUpToolbar(View view) {
        mToolbar = (Toolbar) view.findViewById(R.id.fragment_toolbar);
        mTitle = (TextView) view.findViewById(R.id.toolbar_title);
        mTitleStatus = (TextView) view.findViewById(R.id.toolbar_status);
        mMenuButton = (ImageButton) view.findViewById(R.id.toolbar_button_menu);
        mCameraButton = (ImageButton) view.findViewById(R.id.toolbar_button_left);
        mFriendsButton = (ImageButton) view.findViewById(R.id.toolbar_button_right);
        mRequestsNotificationsLayout = (FrameLayout) view.findViewById(R.id.notification_button_menu_layout);
        mMessagesNotificationsLayout = (FrameLayout) view.findViewById(R.id.notification_button_right_layout);
        mCameraButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_photo_camera_white_24dp));
        mFriendsButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_chat_white_24dp));
        mMenuButton.setOnClickListener(this);
        mCameraButton.setOnClickListener(this);
        mFriendsButton.setOnClickListener(this);
        mTitleStatus.setTextSize(0);
        mToolbar.setTitle("");
        mTitle.setText("Photos");
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

    public static void updateMessagesNotifications(boolean notifications) {
        if (mMessagesNotificationsLayout == null) return;
        if (notifications) {
            mMessagesNotificationsLayout.setVisibility(View.VISIBLE);
        } else {
            mMessagesNotificationsLayout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Add notification icons
        updateMessagesNotifications(WindpicApplication.MESSAGES);
        updateRequestsNotifications(WindpicApplication.REQUESTS);
    }

    @Override
    public void onClick(View view) {
        if (view == mMenuButton) {
            MainActivity.SIDE_MENU.open();
        } else if (view == mCameraButton) {
            openFragment(new CameraFragment());
        } else if (view == mFriendsButton) {
            openFragment(new FriendsFragment());
        } else if (view == mRefreshButton) {
            openFragment(new PhotosFragment());
        }
    }
}
