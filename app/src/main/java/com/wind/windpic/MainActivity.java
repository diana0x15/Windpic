package com.wind.windpic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.wind.windpic.fragments.CameraFragment;
import com.wind.windpic.fragments.ChatFragment;
import com.wind.windpic.fragments.FriendsFragment;
import com.wind.windpic.fragments.PreferencesFragment;
import com.wind.windpic.fragments.RequestsFragment;
import com.wind.windpic.fragments.profiles.ProfileFragment;
import com.wind.windpic.objects.Database;
import com.wind.windpic.objects.Friend;
import com.wind.windpic.side_menu.SideMenu;
import com.wind.windpic.side_menu.SideMenuItem;
import com.wind.windpic.tools.FriendsListener;
import com.wind.windpic.tools.Listeners;
import com.wind.windpic.tools.PhotosListener;
import com.wind.windpic.tools.ProfileTracker;
import com.wind.windpic.tools.RequestsListener;
import com.wind.windpic.tools.UserLocation;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";

    public static boolean FRAGMENT_LOGIN = false;
    public static boolean FRAGMENT_CAMERA = false;
    public static boolean FRAGMENT_FRIENDS = false;
    public static boolean FRAGMENT_CHAT = false;
    public static boolean FRAGMENT_PROFILE = false;
    public static boolean FRAGMENT_REQUESTS = false;
    public static boolean FRAGMENT_PREVIEW = false;
    public static boolean FRAGMENT_PHOTOS = false;
    public static boolean FRAGMENT_PREFERENCES = false;
    private static final String EXTRA_FRIEND = "Friend";
    private static final String EXTRA_PUSH = "push";
    private static final String EXTRA_FROM_MENU = "menu";

    public static ParseUser CURRENT_USER;
    public static UserLocation USER_LOCATION;
    public static SideMenu SIDE_MENU;
    public static ProfileTracker PROFILE_TRACKER;

    public static SideMenuItem itemProfile;
    public static SideMenuItem itemRequests;
    public static SideMenuItem itemSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CURRENT_USER = ParseUser.getCurrentUser();

        setupSideMenu();

        checkConnectivity();
        if (CURRENT_USER != null) {
            WindpicApplication.DATABASE = new Database(this);
            CURRENT_USER.fetchInBackground();

            USER_LOCATION = new UserLocation(this);
            USER_LOCATION.connect();
            WindpicApplication.FIREBASE.child("CONNECTIVITY").child(CURRENT_USER.getObjectId()).setValue("online");
            addListeners();

            FriendsListener.getFriends();
            RequestsListener.getRequests(CURRENT_USER);
            PhotosListener.getPhotos();

            checkNewAge();

            Intent pushIntent = getIntent();
            if (pushIntent.getBooleanExtra(EXTRA_PUSH, false)) {
                Friend friend = pushIntent.getParcelableExtra(EXTRA_FRIEND);
                openFragment(new ChatFragment(), friend);
            } else {
                openFragment(new CameraFragment(), null);
            }

        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private void checkNewAge() {
        if (CURRENT_USER != null) {
            int currentAge = MainActivity.CURRENT_USER.getInt("age");
            int newAge = LoginActivity.getAge(CURRENT_USER.getString("birthday"));
            Log.i(TAG, "cur: " + currentAge + " new: " + newAge);
            if (currentAge != newAge) {
                MainActivity.CURRENT_USER.put("age", newAge);
                MainActivity.CURRENT_USER.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        String id = MainActivity.CURRENT_USER.getObjectId();
                        List<ParseUser> friends = MainActivity.CURRENT_USER.getList("friends");
                        for (ParseUser f : friends) {
                            WindpicApplication.FIREBASE.child("UPDATE").child(f.getObjectId()).child(id).setValue(id);
                        }
                    }
                });

            }
        }
    }

    private void openFragment(Fragment fragment, Friend friend) {
        if (friend != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(EXTRA_FRIEND, friend);
            fragment.setArguments(bundle);
        }
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void openFragmentFromMenu(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void checkConnectivity() {
        if (CURRENT_USER != null && AccessToken.getCurrentAccessToken() == null) {
            ParseUser.logOut();
        }
        if (CURRENT_USER == null && AccessToken.getCurrentAccessToken() != null) {
            LoginManager.getInstance().logOut();
        }
    }

    private void setupSideMenu() {
        // attach to current activity;
        SIDE_MENU = new SideMenu(this);
        SIDE_MENU.setBackgroundImage(getResources().getDrawable(R.drawable.menu_background));
        SIDE_MENU.attachToActivity(this);

        // create menu items;
        itemProfile = new SideMenuItem(this, "Profile", R.drawable.ic_account_box_white_24dp);
        itemRequests = new SideMenuItem(this, "Friend Requests", R.drawable.ic_favorite_border_white_24dp);
        itemSettings = new SideMenuItem(this, "Preferences", R.drawable.ic_settings_white_24dp);
        itemProfile.setOnClickListener(this);
        itemRequests.setOnClickListener(this);
        itemSettings.setOnClickListener(this);
        SIDE_MENU.addItem(itemProfile);
        SIDE_MENU.addItem(itemRequests);
        SIDE_MENU.addItem(itemSettings);
    }

    @Override
    protected void onStop() {
        if (USER_LOCATION != null) {
            USER_LOCATION.disconnect();
        }
        super.onStop();
    }

    private void pauseCameraPreview() {
        if (CameraFragment.mWindCamera != null) {
            CameraFragment.mWindCamera.pausePreview();
        }
    }

    private void stopCameraPreview() {
        if (CameraFragment.mWindCamera != null) {
            CameraFragment.mWindCamera.stopPreview();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (SIDE_MENU.isOpened()) {
            SIDE_MENU.close();
        }
        if (FRAGMENT_CAMERA) {
            pauseCameraPreview();
        }
    }

    // Disable the hardware menu button
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public void onBackPressed() {
        stopCameraPreview();
        if (SIDE_MENU.isOpened()) {
            SIDE_MENU.close();
        } else if (FRAGMENT_PREVIEW) {
            openFragment(new CameraFragment(), null);
            FRAGMENT_PREVIEW = false;
        } else if (FRAGMENT_CHAT) {
            ChatFragment.hideKeyboard(this);
            openFragment((new FriendsFragment()), null);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (USER_LOCATION != null) {
            USER_LOCATION.connect();
        }
    }

    @Override
    public void onClick(View view) {
        if (view == itemProfile) {
            stopCameraPreview();
            openFragmentFromMenu(new ProfileFragment());
        } else if (view == itemRequests) {
            stopCameraPreview();
            openFragmentFromMenu(new RequestsFragment());
        } else if (view == itemSettings) {
            stopCameraPreview();
            PreferencesFragment fragment = new PreferencesFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean(EXTRA_FROM_MENU, true);
            fragment.setArguments(bundle);
            openFragmentFromMenu(fragment);
        }
        SIDE_MENU.close();
    }

    @Override
    protected void onDestroy() {
        if (CURRENT_USER != null) {
            WindpicApplication.FIREBASE.child("CONNECTIVITY").child(CURRENT_USER.getObjectId()).setValue("offline");
        }
        if (PROFILE_TRACKER != null) {
            PROFILE_TRACKER.stopTracking();
        }
        if (MainActivity.CURRENT_USER != null) {
            WindpicApplication.FIREBASE.child("NEW_PHOTOS")
                    .child(CURRENT_USER.getObjectId())
                    .removeEventListener(Listeners.mPhotosListener);
            WindpicApplication.FIREBASE.child("ACCEPTED_FRIEND_REQUESTS")
                    .child(CURRENT_USER.getObjectId())
                    .removeEventListener(Listeners.mAcceptedRequestsListener);
            WindpicApplication.FIREBASE.child("UNFRIEND")
                    .child(CURRENT_USER.getObjectId())
                    .removeEventListener(Listeners.mRemovedFriendListener);
            WindpicApplication.FIREBASE.child("UPDATE")
                    .child(CURRENT_USER.getObjectId())
                    .removeEventListener(Listeners.mUpdateListener);
        }
        super.onDestroy();
    }

    private void addListeners() {
        WindpicApplication.DATABASE = new Database(this);
        WindpicApplication.FIREBASE.child("NEW_PHOTOS")
                .child(CURRENT_USER.getObjectId())
                .addChildEventListener(Listeners.mPhotosListener);
        WindpicApplication.FIREBASE.child("ACCEPTED_FRIEND_REQUESTS")
                .child(CURRENT_USER.getObjectId())
                .addChildEventListener(Listeners.mAcceptedRequestsListener);
        WindpicApplication.FIREBASE.child("UNFRIEND")
                .child(CURRENT_USER.getObjectId())
                .addChildEventListener(Listeners.mRemovedFriendListener);
        WindpicApplication.FIREBASE.child("UPDATE")
                .child(CURRENT_USER.getObjectId())
                .addChildEventListener(Listeners.mUpdateListener);
    }

    public static void showRequestNotifications() {
        if (itemRequests != null) {
            itemRequests.showNotifications();
        }
    }

    public static void hideRequestNotifications() {
        if (itemRequests != null) {
            itemRequests.hideNotifications();
        }
    }
}
