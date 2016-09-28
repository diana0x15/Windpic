package com.wind.windpic.fragments.profiles;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.wind.windpic.R;
import com.wind.windpic.tools.Methods;

/**
 * Created by dianapislaru on 17/10/15.
 */
public class RequestProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private static final String USER_PHOTO = "photo";
    private static final String USER_NAME = "name";
    private static final String USER_AGE = "age";
    private static final String USER_GENDER = "gender";
    private static final String USER_DISTANCE = "distance";

    private AppCompatActivity mActivity;

    private ImageView mImageView;
    private ImageButton mCloseButton;

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
        View view = inflater.inflate(R.layout.fragment_request_profile, container, false);
        setHasOptionsMenu(true);

        initViews(view);
        getUserData();

        return view;
    }

    private void initViews(View view) {
        mImageView = (ImageView) view.findViewById(R.id.fragment_request_profile_imageView);
        mCloseButton = (ImageButton) view.findViewById(R.id.fragment_request_profile_close_button);
        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = mActivity.getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.remove(RequestProfileFragment.this);
                transaction.commit();
            }
        });
    }

    private void getUserData() {
        Bundle bundle = getArguments();
        byte[] data = bundle.getByteArray(USER_PHOTO);
        if (data != null) {
            mImageView.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
        }
    }



}
