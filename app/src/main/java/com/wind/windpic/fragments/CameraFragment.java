package com.wind.windpic.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.wind.windpic.MainActivity;
import com.wind.windpic.R;
import com.wind.windpic.WindpicApplication;
import com.wind.windpic.camera.WindCamera;
import com.wind.windpic.tools.Methods;

/**
 * Created by dianapislaru on 09/10/15.
 */
public class CameraFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "CameraFragment";
    public static final String KEY_CAMERA_ID = "camera id";

    private AppCompatActivity mActivity;
    private int savedCameraId;

    // Toolbar
    private ImageButton mMenuButton;
    private ImageButton mFriendsButton;
    private ImageButton mPhotosButton;
    private static FrameLayout mRequestsNotificationsLayout;
    private static FrameLayout mMessagesNotificationsLayout;
    private static FrameLayout mPhotosNotificationsLayout;

    public static WindCamera mWindCamera;
    private RelativeLayout mCameraContainer;
    private SurfaceView mCameraView;
    private ImageButton mCaptureButton;
    private ImageButton mSwitchButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Methods.setFragment(Methods.FRAGMENT_CAMERA);
        mActivity = (AppCompatActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Methods.enableFullScreen(getActivity());
        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        setUpToolbar(view);

        mCameraContainer = (RelativeLayout) view.findViewById(R.id.camera_preview_container);
        mCameraView = (SurfaceView) view.findViewById(R.id.camera_preview_view);
        mCaptureButton = (ImageButton) view.findViewById(R.id.capture_button);
        mSwitchButton = (ImageButton) view.findViewById(R.id.fragment_camera_switch);

        // Get savedCameraId (set to 0 if there is no savedCameraId)
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        savedCameraId = sharedPref.getInt(KEY_CAMERA_ID, 0);

        mWindCamera = new WindCamera(mActivity, mCameraContainer, mCameraView, savedCameraId);

        mCaptureButton.setOnClickListener(this);
        mMenuButton.setOnClickListener(this);
        mSwitchButton.setOnClickListener(this);

        return view;
    }

    private void setUpToolbar(View view) {
        mMenuButton = (ImageButton) view.findViewById(R.id.toolbar_button_menu);
        mFriendsButton = (ImageButton) view.findViewById(R.id.toolbar_button_left);
        mPhotosButton = (ImageButton) view.findViewById(R.id.toolbar_button_right);
        mRequestsNotificationsLayout = (FrameLayout) view.findViewById(R.id.notification_button_menu_layout);
        mMessagesNotificationsLayout = (FrameLayout) view.findViewById(R.id.notification_button_left_layout);
        mPhotosNotificationsLayout = (FrameLayout) view.findViewById(R.id.notification_button_right_layout);
        mPhotosButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_image_white_36dp));
        mFriendsButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_chat_white_36dp));
        mMenuButton.setOnClickListener(this);
        mPhotosButton.setOnClickListener(this);
        mFriendsButton.setOnClickListener(this);
    }

    private void openFragment(Fragment fragment) {
        saveCameraId();
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveCameraId();
    }

    private void saveCameraId() {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(KEY_CAMERA_ID, mWindCamera.currentCameraId);
        editor.commit();
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

    public static void updatePhotosNotifications(boolean notifications) {
        if (mPhotosNotificationsLayout == null) return;
        if (notifications) {
            mPhotosNotificationsLayout.setVisibility(View.VISIBLE);
        } else {
            mPhotosNotificationsLayout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Add notification icons
        updateMessagesNotifications(WindpicApplication.MESSAGES);
        updateRequestsNotifications(WindpicApplication.REQUESTS);
        updatePhotosNotifications(WindpicApplication.PHOTOS);
    }

    @Override
    public void onClick(View view) {
        if (view == mCaptureButton && mWindCamera.isCameraOn()) {
            mWindCamera.takePicture();
        } else if (view == mMenuButton) {
            MainActivity.SIDE_MENU.open();
        } else if (view == mPhotosButton) {
            mWindCamera.stopPreview();
            openFragment(new PhotosFragment());
        } else if (view == mFriendsButton) {
            mWindCamera.stopPreview();
            openFragment(new FriendsFragment());
        } else if (view == mSwitchButton) {
            mWindCamera.switchCamera();
        }
    }
}
