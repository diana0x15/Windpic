package com.wind.windpic.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.wind.windpic.MainActivity;
import com.wind.windpic.R;
import com.wind.windpic.WindpicApplication;
import com.wind.windpic.adapters.RequestsListAdapter;
import com.wind.windpic.tools.Methods;

/**
 * Created by dianapislaru on 10/10/15.
 */
public class RequestsFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "RequestsFragment";

    private Activity mActivity;
    private RequestsListAdapter mAdapter;

    private ListView mListView;
    private TextView mEmptyTextView;

    // Toolbar
    private Toolbar mToolbar;
    private TextView mTitle;
    private TextView mTitleStatus;
    private ImageButton mMenuButton;
    private ImageButton mCameraButton;
    private ImageButton mFriendsButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Methods.setFragment(Methods.FRAGMENT_REQUESTS);
        mActivity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Methods.disableFullScreen(mActivity);
        View view = inflater.inflate(R.layout.fragment_requests, container, false);
        setUpToolbar(view);

        mListView = (ListView) view.findViewById(R.id.fragment_requests_listView);
        mEmptyTextView = (TextView) view.findViewById(R.id.fragment_requests_empty_textView);

        //getRequests
        if (WindpicApplication.REQUESTS_LIST.size() == 0) {
            mEmptyTextView.setVisibility(View.VISIBLE);
        } else {
            mEmptyTextView.setVisibility(View.INVISIBLE);
            mAdapter = new RequestsListAdapter(mActivity, mListView);
            mListView.setAdapter(mAdapter);
        }

        return view;
    }

    private void setUpToolbar(View view) {
        mToolbar = (Toolbar) view.findViewById(R.id.fragment_toolbar);
        mTitle = (TextView) view.findViewById(R.id.toolbar_title);
        mTitleStatus = (TextView) view.findViewById(R.id.toolbar_status);
        mMenuButton = (ImageButton) view.findViewById(R.id.toolbar_button_menu);
        mCameraButton = (ImageButton) view.findViewById(R.id.toolbar_button_left);
        mFriendsButton = (ImageButton) view.findViewById(R.id.toolbar_button_right);
        mCameraButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_photo_camera_white_24dp));
        mFriendsButton.setImageBitmap(null);
        mMenuButton.setOnClickListener(this);
        mCameraButton.setOnClickListener(this);
        mTitleStatus.setTextSize(0);
        mToolbar.setTitle("");
        mTitle.setText("Friend Requests");
    }

    private void openFragment(Fragment fragment) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @Override
    public void onClick(View view) {
        if (view == mMenuButton) {
            MainActivity.SIDE_MENU.open();
        } else if (view == mCameraButton) {
            openFragment(new CameraFragment());
        }
    }
}
