package com.wind.windpic.fragments.profiles;

import android.content.Intent;
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
import android.widget.Toast;

import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.wind.windpic.LoginActivity;
import com.wind.windpic.MainActivity;
import com.wind.windpic.R;
import com.wind.windpic.WindpicApplication;
import com.wind.windpic.fragments.CameraFragment;
import com.wind.windpic.objects.Friend;
import com.wind.windpic.tools.DownloadProfilePhoto;
import com.wind.windpic.tools.Listeners;
import com.wind.windpic.tools.Methods;

/**
 * Created by dianapislaru on 09/10/15.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ProfileFragment";

    private AppCompatActivity mActivity;

    private Button mLogoutButton;
    private ImageView mImageView;
    private TextView mNameTextView;

    // Toolbar
    private Toolbar mToolbar;
    private TextView mTitle;
    private TextView mTitleStatus;
    private ImageButton mMenuButton;
    private ImageButton mCameraButton;
    private ImageButton mRefreshButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Methods.setFragment(Methods.FRAGMENT_PROFILE);
        mActivity = (AppCompatActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Methods.disableFullScreen(getActivity());
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        setUpToolbar(view);
        setHasOptionsMenu(true);

        initViews(view);
        getUserData();

        return view;
    }

    private void initViews(View view) {
        mLogoutButton = (Button) view.findViewById(R.id.fragment_profile_logout_button);
        mImageView = (ImageView) view.findViewById(R.id.fragment_profile_imageView);
        mNameTextView = (TextView) view.findViewById(R.id.fragment_profile_name_textView);

        mLogoutButton.setOnClickListener(this);
    }

    private void getUserData() {
        Friend user = new Friend(MainActivity.CURRENT_USER);
        String name = user.getName();
        mNameTextView.setText(name);
        byte[] data = user.getPicture();
        mImageView.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
    }

    private void setUpToolbar(View view) {
        mToolbar = (Toolbar) view.findViewById(R.id.fragment_profile_toolbar);
        mTitle = (TextView) view.findViewById(R.id.toolbar_title);
        mTitleStatus = (TextView) view.findViewById(R.id.toolbar_status);
        mTitle.setText("My Profile");
        mMenuButton = (ImageButton) view.findViewById(R.id.toolbar_button_menu);
        mCameraButton = (ImageButton) view.findViewById(R.id.toolbar_button_left);
        mRefreshButton = (ImageButton) view.findViewById(R.id.toolbar_button_right);

        mCameraButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_photo_camera_white_24dp));
        mRefreshButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_refresh_white_24dp));
        mMenuButton.setOnClickListener(this);
        mRefreshButton.setOnClickListener(this);
        mCameraButton.setOnClickListener(this);
        mToolbar.setTitle("");
        mTitleStatus.setTextSize(0);
    }

    private void logout() {
        if (!Methods.isInternetAvailable(mActivity)) {
            Toast.makeText(mActivity, "Check your internet connection.", Toast.LENGTH_SHORT).show();
            return;
        }
        removeListeners();
        MainActivity.CURRENT_USER = null;
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                LoginManager.getInstance().logOut();
                Intent intent = new Intent(mActivity, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

    }

    private void openFragment(Fragment fragment) {
        FragmentManager manager = mActivity.getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void removeListeners() {
        String id = MainActivity.CURRENT_USER.getObjectId();
        WindpicApplication.FIREBASE.child("CONNECTIVITY").child(MainActivity.CURRENT_USER.getObjectId()).setValue("offline");
        WindpicApplication.FIREBASE.child("NEW_PHOTOS")
                .child(id).removeEventListener(Listeners.mPhotosListener);
        WindpicApplication.FIREBASE.child("ACCEPTED_FRIEND_REQUESTS")
                .child(id).removeEventListener(Listeners.mAcceptedRequestsListener);
        WindpicApplication.FIREBASE.child("UNFRIEND")
                .child(id).removeEventListener(Listeners.mRemovedFriendListener);
        WindpicApplication.FIREBASE.child("UPDATE")
                .child(id).removeEventListener(Listeners.mUpdateListener);
    }

    private void refresh() {
        // get the new profile picture
        Profile profile = Profile.getCurrentProfile();
        if (profile != null) {
            String name = profile.getName();
            int size = Methods.getScreenSize(mActivity).x;
            DownloadProfilePhoto task = new DownloadProfilePhoto(mActivity, mImageView);
            task.execute(Profile.getCurrentProfile().getProfilePictureUri(size, size).toString());
        }
    }

    @Override
    public void onClick(View view) {
        if (view == mMenuButton) {
            MainActivity.SIDE_MENU.open();
        } else if (view == mCameraButton) {
            openFragment(new CameraFragment());
        } else if (view == mRefreshButton) {
            refresh();
        } else if (view == mLogoutButton) {
            logout();
        }
    }
}
