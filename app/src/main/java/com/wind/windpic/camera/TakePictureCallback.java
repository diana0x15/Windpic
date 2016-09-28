package com.wind.windpic.camera;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.wind.windpic.R;
import com.wind.windpic.fragments.CameraFragment;
import com.wind.windpic.fragments.PreviewFragment;

/**
 * Created by dianapislaru on 25/08/15.
 */
public class TakePictureCallback implements Camera.PictureCallback {

    private static final String ARGUMENT_PICTURE_DATA = "picture data";

    private AppCompatActivity mActivity;

    public TakePictureCallback(AppCompatActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        CameraFragment.mWindCamera.stopPreview();
        openPreviewFragment(data);
    }

    private void openPreviewFragment(byte[] data) {
        FragmentManager manager = mActivity.getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        PreviewFragment fragment = new PreviewFragment();
        Bundle bundle = new Bundle();
        bundle.putByteArray(ARGUMENT_PICTURE_DATA, data);
        fragment.setArguments(bundle);
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

}
